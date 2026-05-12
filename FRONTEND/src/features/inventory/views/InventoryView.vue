<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useInventoryStore } from '@/stores/inventory'
import PurchaseForm from '../components/PurchaseForm.vue'

const route = useRoute()
const router = useRouter()
const inventoryStore = useInventoryStore()
const productId = route.params.productId as string

onMounted(() => {
  inventoryStore.fetchInventory(productId)
})

onUnmounted(() => {
  inventoryStore.reset()
})
</script>

<template>
  <div class="inventory-view">
    <header class="inventory-view__header">
      <button class="inventory-view__back" @click="router.back()">← Volver</button>
      <h1 class="inventory-view__title">Inventario y Compra</h1>
    </header>

    <div v-if="inventoryStore.loading && !inventoryStore.inventory" class="inventory-view__state">
      Cargando...
    </div>

    <div
      v-else-if="inventoryStore.error && !inventoryStore.inventory"
      class="inventory-view__state inventory-view__state--error"
    >
      {{ inventoryStore.error }}
    </div>

    <template v-else-if="inventoryStore.inventory">
      <div class="inventory-view__stock">
        <div class="inventory-view__stock-card">
          <span class="inventory-view__stock-label">Stock Disponible</span>
          <span
            class="inventory-view__stock-value"
            :class="{ 'inventory-view__stock-value--low': inventoryStore.inventory.cantidad < 5 }"
          >
            {{ inventoryStore.inventory.cantidad }}
          </span>
        </div>
        <div class="inventory-view__stock-card">
          <span class="inventory-view__stock-label">Última Actualización</span>
          <span class="inventory-view__stock-date">
            {{ new Date(inventoryStore.inventory.updatedAt).toLocaleString('es-ES') }}
          </span>
        </div>
      </div>

      <PurchaseForm :product-id="productId" />
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/tokens' as t;
@use '@/assets/styles/mixins' as m;

.inventory-view {
  max-width: 700px;
  margin: 0 auto;

  &__header {
    margin-bottom: t.$spacing-8;
  }

  &__back {
    @include m.button-base;
    background: none;
    color: t.$color-text-muted;
    padding: 0;
    margin-bottom: t.$spacing-4;

    &:hover {
      color: t.$color-primary;
    }
  }

  &__title {
    font-size: t.$font-size-2xl;
    font-weight: 700;
  }

  &__state {
    text-align: center;
    padding: t.$spacing-8;
    color: t.$color-text-muted;

    &--error {
      color: t.$color-danger;
    }
  }

  &__stock {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: t.$spacing-4;
  }

  &__stock-card {
    @include m.card;
    display: flex;
    flex-direction: column;
    gap: t.$spacing-2;
  }

  &__stock-label {
    font-size: t.$font-size-sm;
    color: t.$color-text-muted;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  &__stock-value {
    font-size: 2.5rem;
    font-weight: 800;
    color: t.$color-success;

    &--low {
      color: t.$color-warning;
    }
  }

  &__stock-date {
    font-size: t.$font-size-sm;
    color: t.$color-text;
  }
}
</style>
