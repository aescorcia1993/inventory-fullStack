<script setup lang="ts">
import type { Product } from '../types/product.types'
import { useRouter } from 'vue-router'

const props = defineProps<{ product: Product }>()
const router = useRouter()

function viewInventory(): void {
  router.push(`/inventory/${props.product.id}`)
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(amount)
}
</script>

<template>
  <article class="product-card">
    <div class="product-card__body">
      <h3 class="product-card__name">{{ product.nombre }}</h3>
      <p v-if="product.descripcion" class="product-card__desc">{{ product.descripcion }}</p>
      <p class="product-card__price">{{ formatCurrency(product.precio) }}</p>
    </div>
    <footer class="product-card__footer">
      <button class="product-card__btn" @click="viewInventory">Ver Inventario</button>
    </footer>
  </article>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/tokens' as t;
@use '@/assets/styles/mixins' as m;

.product-card {
  @include m.card;
  display: flex;
  flex-direction: column;

  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: t.$spacing-2;
  }

  &__name {
    font-size: t.$font-size-lg;
    font-weight: 600;
    color: t.$color-text;
  }

  &__desc {
    font-size: t.$font-size-sm;
    color: t.$color-text-muted;
    flex: 1;
  }

  &__price {
    font-size: t.$font-size-xl;
    font-weight: 700;
    color: t.$color-primary;
  }

  &__footer {
    margin-top: t.$spacing-4;
    border-top: 1px solid t.$color-border;
    padding-top: t.$spacing-4;
  }

  &__btn {
    @include m.button-base;
    width: 100%;
    justify-content: center;
    background-color: t.$color-primary;
    color: white;

    &:hover:not(:disabled) {
      background-color: t.$color-primary-hover;
    }
  }
}
</style>
