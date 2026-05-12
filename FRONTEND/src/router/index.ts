import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/products'
  },
  {
    path: '/products',
    component: () => import('@/features/products/views/ProductListView.vue'),
    meta: { title: 'Products' }
  },
  {
    path: '/products/new',
    component: () => import('@/features/products/views/ProductCreateView.vue'),
    meta: { title: 'New Product' }
  },
  {
    path: '/inventory/:productId',
    component: () => import('@/features/inventory/views/InventoryView.vue'),
    meta: { title: 'Inventory & Purchase' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach(to => {
  document.title = `${to.meta.title ?? 'Inventory'} | InventoryApp`
})

export default router
