import api, { deserializeSingle, deserializeList } from '@/plugins/axios'
import type { Product } from '../types/product.types'

export async function fetchProducts(): Promise<Product[]> {
  const response = await api.get('/api/v1/products')
  return deserializeList<Omit<Product, 'id'>>(response.data)
}

export async function fetchProduct(id: string): Promise<Product> {
  const response = await api.get(`/api/v1/products/${id}`)
  return deserializeSingle<Omit<Product, 'id'>>(response.data)
}

export async function createProduct(data: Omit<Product, 'id'>): Promise<Product> {
  const body = {
    data: {
      type: 'products',
      attributes: data
    }
  }
  const response = await api.post('/api/v1/products', body)
  return deserializeSingle<Omit<Product, 'id'>>(response.data)
}
