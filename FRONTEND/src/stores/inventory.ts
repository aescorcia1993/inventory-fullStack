import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { InventoryItem } from '@/features/inventory/types/inventory.types'
import * as inventoryService from '@/features/inventory/services/inventory.service'

export const useInventoryStore = defineStore('inventory', () => {
  const inventory = ref<InventoryItem | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const purchaseSuccess = ref(false)

  async function fetchInventory(productId: string): Promise<void> {
    loading.value = true
    error.value = null
    try {
      inventory.value = await inventoryService.fetchInventory(productId)
    } catch (e: unknown) {
      const status = (e as { response?: { status?: number } }).response?.status
      if (status === 404) {
        // Auto-initialize inventory for products created before this fix
        await inventoryService.initInventory(productId, 0)
        inventory.value = await inventoryService.fetchInventory(productId)
      } else {
        error.value = (e as Error).message ?? 'Error al cargar inventario'
      }
    } finally {
      loading.value = false
    }
  }

  async function makePurchase(productId: string, cantidad: number): Promise<void> {
    loading.value = true
    error.value = null
    purchaseSuccess.value = false
    try {
      await inventoryService.makePurchase(productId, cantidad)
      purchaseSuccess.value = true
      await fetchInventory(productId)
    } catch (e: unknown) {
      error.value = (e as Error).message ?? 'Error al realizar compra'
      throw e
    } finally {
      loading.value = false
    }
  }

  function reset(): void {
    inventory.value = null
    error.value = null
    purchaseSuccess.value = false
  }

  return { inventory, loading, error, purchaseSuccess, fetchInventory, makePurchase, reset }
})
