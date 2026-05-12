<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductsStore } from '@/stores/products'
import ProductCard from '../components/ProductCard.vue'

const router = useRouter()
const productsStore = useProductsStore()

onMounted(() => {
  productsStore.fetchProducts()
})
</script>

<template>
  <div class="product-list">
    <header class="product-list__header">
      <h1 class="product-list__title">Productos</h1>
      <button class="product-list__btn-create" @click="router.push('/products/new')">
        + Nuevo Producto
      </button>
    </header>

    <div v-if="productsStore.loading" class="product-list__state">Cargando...</div>

    <div v-else-if="productsStore.error" class="product-list__state product-list__state--error">
      {{ productsStore.error }}
    </div>

    <div v-else-if="productsStore.products.length === 0" class="product-list__state">
      No hay productos. ¡Crea uno!
    </div>

    <div v-else class="product-list__grid">
      <ProductCard
        v-for="product in productsStore.products"
        :key="product.id"
        :product="product"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/tokens' as t;
@use '@/assets/styles/mixins' as m;

.product-list {
  &__header {
    @include m.flex-between;
    margin-bottom: t.$spacing-8;
    flex-wrap: wrap;
    gap: t.$spacing-4;
  }

  &__title {
    font-size: t.$font-size-2xl;
    font-weight: 700;
  }

  &__btn-create {
    @include m.button-base;
    background-color: t.$color-primary;
    color: white;

    &:hover:not(:disabled) {
      background-color: t.$color-primary-hover;
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: t.$spacing-6;

    @include m.respond-to('sm') {
      grid-template-columns: repeat(2, 1fr);
    }

    @include m.respond-to('lg') {
      grid-template-columns: repeat(3, 1fr);
    }
  }

  &__state {
    text-align: center;
    padding: t.$spacing-8;
    color: t.$color-text-muted;

    &--error {
      color: t.$color-danger;
    }
  }
}
</style>
