import api, { deserializeSingle } from '@/plugins/axios'
import type { InventoryItem } from '../types/inventory.types'

export async function initInventory(productId: string, cantidad: number): Promise<void> {
  const body = {
    data: {
      type: 'inventory',
      attributes: { productoId: productId, cantidad }
    }
  }
  await api.post('/api/v1/inventory', body)
}

export async function fetchInventory(productId: string): Promise<InventoryItem> {
  const response = await api.get(`/api/v1/inventory/${productId}`)
  return deserializeSingle<Omit<InventoryItem, 'id'>>(response.data)
}

export async function makePurchase(productId: string, cantidad: number): Promise<void> {
  const body = {
    data: {
      type: 'purchases',
      attributes: { productoId: productId, cantidad }
    }
  }
  await api.post('/api/v1/purchases', body)
}
