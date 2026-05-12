<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { fetchPayments } from '../services/payment.service'
import type { Payment } from '../types/payment.types'

const payments = ref<Payment[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
let pollTimer: ReturnType<typeof setInterval> | null = null

const completedCount = computed(() => payments.value.filter(p => p.status === 'COMPLETED').length)
const processingCount = computed(() => payments.value.filter(p => p.status === 'PROCESSING').length)

function shortId(id: string): string {
  return id.slice(0, 8) + '…'
}

function formatDate(iso: string | null): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('es-CO', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

async function load() {
  try {
    payments.value = await fetchPayments()
    error.value = null
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Error al cargar pagos'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  pollTimer = setInterval(load, 3000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div class="payment-history">
    <header class="payment-history__header">
      <h1 class="payment-history__title">Historial de Pagos</h1>
      <div class="payment-history__badges">
        <span class="payment-history__badge payment-history__badge--processing">
          ⚙️ Procesando: {{ processingCount }}
        </span>
        <span class="payment-history__badge payment-history__badge--completed">
          ✅ Completados: {{ completedCount }}
        </span>
      </div>
    </header>

    <p class="payment-history__hint">Se actualiza automáticamente cada 3 segundos.</p>

    <div v-if="loading" class="payment-history__state">Cargando pagos...</div>

    <div v-else-if="error" class="payment-history__state payment-history__state--error">
      {{ error }}
    </div>

    <div v-else-if="payments.length === 0" class="payment-history__state">
      Aún no hay pagos registrados. Realiza una compra para empezar.
    </div>

    <div v-else class="payment-history__table-wrapper">
      <table class="payment-history__table">
        <thead>
          <tr>
            <th>Payment ID</th>
            <th>Purchase ID</th>
            <th>Producto ID</th>
            <th>Cant.</th>
            <th>Total</th>
            <th>Estado</th>
            <th>Recibido</th>
            <th>Procesado</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in payments" :key="p.id">
            <td class="payment-history__mono" :title="p.id">{{ shortId(p.id) }}</td>
            <td class="payment-history__mono" :title="p.purchaseId">{{ shortId(p.purchaseId) }}</td>
            <td class="payment-history__mono" :title="p.productoId">{{ shortId(p.productoId) }}</td>
            <td>{{ p.cantidad }}</td>
            <td>{{ Number(p.total).toFixed(2) }}</td>
            <td>
              <span
                class="payment-history__status"
                :class="p.status === 'COMPLETED'
                  ? 'payment-history__status--completed'
                  : 'payment-history__status--processing'"
              >{{ p.status }}</span>
            </td>
            <td>{{ formatDate(p.receivedAt) }}</td>
            <td>{{ formatDate(p.processedAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/tokens' as t;
@use '@/assets/styles/mixins' as m;

.payment-history {
  &__header {
    @include m.flex-between;
    margin-bottom: t.$spacing-4;
    flex-wrap: wrap;
    gap: t.$spacing-4;
  }

  &__title {
    font-size: t.$font-size-2xl;
    font-weight: 700;
  }

  &__badges {
    display: flex;
    gap: t.$spacing-3;
    flex-wrap: wrap;
  }

  &__badge {
    padding: t.$spacing-1 t.$spacing-3;
    border-radius: 9999px;
    font-size: t.$font-size-sm;
    font-weight: 600;

    &--processing {
      background-color: #fef3c7;
      color: #92400e;
    }

    &--completed {
      background-color: #d1fae5;
      color: #065f46;
    }
  }

  &__hint {
    color: t.$color-text-muted;
    font-size: t.$font-size-sm;
    margin-bottom: t.$spacing-6;
  }

  &__state {
    text-align: center;
    padding: t.$spacing-8;
    color: t.$color-text-muted;

    &--error {
      color: t.$color-danger;
    }
  }

  &__table-wrapper {
    overflow-x: auto;
    border-radius: t.$radius-lg;
    box-shadow: t.$shadow-sm;
  }

  &__table {
    width: 100%;
    border-collapse: collapse;
    background-color: t.$color-surface;
    font-size: t.$font-size-sm;

    th,
    td {
      padding: t.$spacing-3 t.$spacing-4;
      text-align: left;
      border-bottom: 1px solid t.$color-border;
    }

    th {
      background-color: t.$color-bg;
      font-weight: 600;
      color: t.$color-text-muted;
      white-space: nowrap;
    }

    tr:last-child td {
      border-bottom: none;
    }

    tr:hover td {
      background-color: t.$color-bg;
    }
  }

  &__mono {
    font-family: monospace;
    font-size: 0.75rem;
    cursor: default;
  }

  &__status {
    display: inline-block;
    padding: 2px t.$spacing-2;
    border-radius: t.$radius-sm;
    font-weight: 600;
    font-size: 0.75rem;
    white-space: nowrap;

    &--processing {
      background-color: #fef3c7;
      color: #92400e;
    }

    &--completed {
      background-color: #d1fae5;
      color: #065f46;
    }
  }
}
</style>
