import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import ProductCard from '@/features/products/components/ProductCard.vue'
import type { Product } from '@/features/products/types/product.types'

const mockProduct: Product = {
  id: '123e4567-e89b-12d3-a456-426614174000',
  nombre: 'Test Product',
  precio: 29.99,
  descripcion: 'A test product description'
}

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/inventory/:productId', component: { template: '<div/>' } }]
  })
}

describe('ProductCard', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders product name and price', () => {
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct },
      global: { plugins: [createTestRouter()] }
    })
    expect(wrapper.find('.product-card__name').text()).toBe('Test Product')
    expect(wrapper.find('.product-card__price').text()).toContain('29')
  })

  it('renders description when provided', () => {
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct },
      global: { plugins: [createTestRouter()] }
    })
    expect(wrapper.find('.product-card__desc').text()).toBe('A test product description')
  })

  it('navigates to inventory on button click', async () => {
    const router = createTestRouter()
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct },
      global: { plugins: [router] }
    })
    await wrapper.find('.product-card__btn').trigger('click')
    expect(pushSpy).toHaveBeenCalledWith(`/inventory/${mockProduct.id}`)
  })

  it('does not render description when absent', () => {
    const productWithoutDesc = { ...mockProduct, descripcion: undefined }
    const wrapper = mount(ProductCard, {
      props: { product: productWithoutDesc },
      global: { plugins: [createTestRouter()] }
    })
    expect(wrapper.find('.product-card__desc').exists()).toBe(false)
  })
})
