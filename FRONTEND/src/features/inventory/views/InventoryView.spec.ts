import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import InventoryView from '@/features/inventory/views/InventoryView.vue'
import { useInventoryStore } from '@/stores/inventory'

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/inventory/:productId', component: InventoryView }]
  })
}

describe('InventoryView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('shows loading state initially', async () => {
    const store = useInventoryStore()
    store.loading = true
    vi.spyOn(store, 'fetchInventory').mockResolvedValue()
    const router = createTestRouter()
    await router.push('/inventory/product-uuid-1')
    const wrapper = mount(InventoryView, { global: { plugins: [router] } })
    expect(wrapper.text()).toContain('Cargando')
  })

  it('shows inventory data when loaded', async () => {
    const store = useInventoryStore()
    store.loading = false
    store.inventory = {
      id: 'inv-1',
      productoId: 'product-uuid-1',
      cantidad: 50,
      updatedAt: new Date().toISOString()
    }
    vi.spyOn(store, 'fetchInventory').mockResolvedValue()
    const router = createTestRouter()
    await router.push('/inventory/product-uuid-1')
    const wrapper = mount(InventoryView, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.find('.inventory-view__stock-value').text()).toBe('50')
  })

  it('shows error when fetch fails', async () => {
    const store = useInventoryStore()
    store.loading = false
    store.error = 'Inventario no encontrado'
    vi.spyOn(store, 'fetchInventory').mockResolvedValue()
    const router = createTestRouter()
    await router.push('/inventory/product-uuid-1')
    const wrapper = mount(InventoryView, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('Inventario no encontrado')
  })
})
