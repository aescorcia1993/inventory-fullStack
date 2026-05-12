import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Product } from '@/features/products/types/product.types'
import * as productService from '@/features/products/services/product.service'
import { initInventory } from '@/features/inventory/services/inventory.service'

export const useProductsStore = defineStore('products', () => {
  const products = ref<Product[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchProducts(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      products.value = await productService.fetchProducts()
    } catch (e: unknown) {
      error.value = (e as Error).message ?? 'Error al cargar productos'
    } finally {
      loading.value = false
    }
  }

  async function createProduct(data: Omit<Product, 'id'>, stockInicial: number): Promise<Product> {
    loading.value = true
    error.value = null
    try {
      const created = await productService.createProduct(data)
      products.value.unshift(created)
      await initInventory(created.id, stockInicial)
      return created
    } catch (e: unknown) {
      error.value = (e as Error).message ?? 'Error al crear producto'
      throw e
    } finally {
      loading.value = false
    }
  }

  return { products, loading, error, fetchProducts, createProduct }
})
