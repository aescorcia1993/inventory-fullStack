import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import ProductListView from '@/features/products/views/ProductListView.vue'
import { useProductsStore } from '@/stores/products'

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/products', component: ProductListView },
      { path: '/products/new', component: { template: '<div/>' } },
      { path: '/inventory/:productId', component: { template: '<div/>' } }
    ]
  })
}

describe('ProductListView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('shows loading state', async () => {
    const store = useProductsStore()
    store.loading = true
    const router = createTestRouter()
    await router.push('/products')
    const wrapper = mount(ProductListView, { global: { plugins: [router] } })
    expect(wrapper.text()).toContain('Cargando')
  })

  it('shows empty state when no products', async () => {
    const store = useProductsStore()
    store.loading = false
    store.products = []
    vi.spyOn(store, 'fetchProducts').mockResolvedValue()
    const router = createTestRouter()
    await router.push('/products')
    const wrapper = mount(ProductListView, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('No hay productos')
  })

  it('renders product cards', async () => {
    const store = useProductsStore()
    store.loading = false
    store.products = [
      { id: '1', nombre: 'Product A', precio: 10, descripcion: 'desc' }
    ]
    vi.spyOn(store, 'fetchProducts').mockResolvedValue()
    const router = createTestRouter()
    await router.push('/products')
    const wrapper = mount(ProductListView, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.findAll('.product-card').length).toBe(1)
  })

  it('navigates to /products/new on button click', async () => {
    const store = useProductsStore()
    vi.spyOn(store, 'fetchProducts').mockResolvedValue()
    const router = createTestRouter()
    await router.push('/products')
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = mount(ProductListView, { global: { plugins: [router] } })
    await wrapper.find('.product-list__btn-create').trigger('click')
    expect(pushSpy).toHaveBeenCalledWith('/products/new')
  })
})
