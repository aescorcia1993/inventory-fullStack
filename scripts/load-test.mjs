/**
 * Prueba de carga – purchase-service
 * Objetivo : ~100 peticiones/seg durante 10 segundos (≈1 000 peticiones)
 * Uso      : node scripts/load-test.mjs
 *
 * El script:
 *  1. Crea un producto de prueba en products-service
 *  2. Inicializa su inventario con 10 000 unidades
 *  3. Ejecuta el bombardeo de compras
 */

import http from 'http'

// ── Configuración ────────────────────────────────────────────────────────────
const PRODUCTS_HOST  = 'localhost'
const PRODUCTS_PORT  = 8081
const PRODUCTS_KEY   = 'products-secret-key-2024'

const INVENTORY_HOST = 'localhost'
const INVENTORY_PORT = 8082
const INVENTORY_KEY  = 'inventory-secret-key-2024'

const PURCHASE_HOST  = 'localhost'
const PURCHASE_PORT  = 8083
const PURCHASE_KEY   = 'purchase-secret-key-2024'

const CANTIDAD   = 1    // unidades por compra
const RPS        = 100  // peticiones por segundo
const DURATION_S = 10   // duración total en segundos
// ─────────────────────────────────────────────────────────────────────────────

// ── Utilidad HTTP ─────────────────────────────────────────────────────────────
function jsonRequest({ host, port, path, method, apiKey, body }) {
  return new Promise((resolve, reject) => {
    const raw = body ? JSON.stringify(body) : ''
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/vnd.api+json',
      'X-API-Key': apiKey,
    }
    if (raw) headers['Content-Length'] = Buffer.byteLength(raw)

    const req = http.request({ hostname: host, port, path, method, headers }, (res) => {
      let data = ''
      res.on('data', chunk => data += chunk)
      res.on('end', () => {
        if (res.statusCode >= 400) {
          reject(new Error(`HTTP ${res.statusCode} en ${method} ${path}: ${data}`))
        } else {
          resolve(JSON.parse(data))
        }
      })
    })
    req.on('error', reject)
    if (raw) req.write(raw)
    req.end()
  })
}

// ── Setup: crear producto e inventario ───────────────────────────────────────
async function setup() {
  console.log('🔧 Creando producto de prueba...')
  const productRes = await jsonRequest({
    host: PRODUCTS_HOST, port: PRODUCTS_PORT,
    path: '/api/v1/products', method: 'POST',
    apiKey: PRODUCTS_KEY,
    body: {
      data: {
        type: 'products',
        attributes: {
          nombre: `LoadTest-${Date.now()}`,
          precio: 9.99,
          descripcion: 'Producto temporal para prueba de carga'
        }
      }
    }
  })
  const productoId = productRes.data.id
  console.log(`   ✅ Producto creado: ${productoId}`)

  console.log('🔧 Inicializando inventario con 10 000 unidades...')
  await jsonRequest({
    host: INVENTORY_HOST, port: INVENTORY_PORT,
    path: '/api/v1/inventory', method: 'POST',
    apiKey: INVENTORY_KEY,
    body: {
      data: {
        type: 'inventory',
        attributes: { productoId, cantidad: 10000 }
      }
    }
  })
  console.log('   ✅ Inventario listo\n')

  return productoId
}

// ── Cuerpo y headers de compra (se arman después de obtener el ID) ────────────
function makePurchaseConfig(productoId) {
  const body = JSON.stringify({
    data: {
      type: 'purchases',
      attributes: { productoId, cantidad: CANTIDAD }
    }
  })
  const headers = {
    'Content-Type': 'application/json',
    'X-API-Key': PURCHASE_KEY,
    'Content-Length': Buffer.byteLength(body)
  }
  return { body, headers }
}

// ── Contadores ───────────────────────────────────────────────────────────────
let sent       = 0
let completed  = 0
let ok         = 0        // 2xx
let clientErr  = 0        // 4xx
let serverErr  = 0        // 5xx
let netErr     = 0        // timeouts / conexión
const latencies = []

function request (body, headers) {
  const start = Date.now()
  sent++

  const req = http.request(
    { hostname: PURCHASE_HOST, port: PURCHASE_PORT, path: '/api/v1/purchases', method: 'POST', headers },
    (res) => {
      res.resume()   // consume body sin procesarlo
      res.on('end', () => {
        const ms = Date.now() - start
        latencies.push(ms)
        completed++

        if      (res.statusCode >= 200 && res.statusCode < 300) ok++
        else if (res.statusCode >= 400 && res.statusCode < 500) clientErr++
        else if (res.statusCode >= 500)                         serverErr++
      })
    }
  )

  req.on('error', () => { netErr++; completed++ })
  req.write(body)
  req.end()
}

// ── Bucle de carga ───────────────────────────────────────────────────────────
function runLoadTest(productoId) {
  const { body, headers } = makePurchaseConfig(productoId)

  console.log(`🚀 Iniciando prueba: ${RPS} req/s durante ${DURATION_S} s`)
  console.log(`   Endpoint : POST http://${PURCHASE_HOST}:${PURCHASE_PORT}/api/v1/purchases`)
  console.log(`   Producto : ${productoId}`)
  console.log(`   Total esperado: ~${RPS * DURATION_S} peticiones\n`)

  const INTERVAL_MS = 1000 / RPS
  let elapsed = 0

  const ticker = setInterval(() => {
    request(body, headers)
    elapsed += INTERVAL_MS
    if (elapsed >= DURATION_S * 1000) {
      clearInterval(ticker)
      const wait = setInterval(() => {
        if (completed >= sent) {
          clearInterval(wait)
          report()
        }
      }, 200)
    }
  }, INTERVAL_MS)
}

// ── Reporte ───────────────────────────────────────────────────────────────────
function percentile (arr, p) {
  if (!arr.length) return 0
  const sorted = [...arr].sort((a, b) => a - b)
  const idx = Math.ceil((p / 100) * sorted.length) - 1
  return sorted[Math.max(0, idx)]
}

function report () {
  const total  = latencies.length
  const avg    = total ? (latencies.reduce((a, b) => a + b, 0) / total).toFixed(1) : 0
  const p50    = percentile(latencies, 50)
  const p90    = percentile(latencies, 90)
  const p99    = percentile(latencies, 99)
  const minLat = total ? Math.min(...latencies) : 0
  const maxLat = total ? Math.max(...latencies) : 0
  const rps    = (ok / DURATION_S).toFixed(1)

  console.log('═══════════════════════════════════════════')
  console.log('           RESULTADOS – Prueba de carga    ')
  console.log('═══════════════════════════════════════════')
  console.log(`  Peticiones enviadas  : ${sent}`)
  console.log(`  Peticiones completadas: ${completed}`)
  console.log(`  ✅ Exitosas (2xx)    : ${ok}`)
  console.log(`  ⚠️  Errores cliente (4xx): ${clientErr}`)
  console.log(`  ❌ Errores servidor (5xx): ${serverErr}`)
  console.log(`  🔌 Errores de red       : ${netErr}`)
  console.log('───────────────────────────────────────────')
  console.log(`  Throughput real      : ${rps} req/s`)
  console.log(`  Latencia promedio    : ${avg} ms`)
  console.log(`  p50                  : ${p50} ms`)
  console.log(`  p90                  : ${p90} ms`)
  console.log(`  p99                  : ${p99} ms`)
  console.log(`  Min                  : ${minLat} ms`)
  console.log(`  Max                  : ${maxLat} ms`)
  console.log('═══════════════════════════════════════════\n')
}

// ── Entry point ───────────────────────────────────────────────────────────────
setup()
  .then(productoId => runLoadTest(productoId))
  .catch(err => {
    console.error('❌ Error durante el setup:', err.message)
    process.exit(1)
  })
