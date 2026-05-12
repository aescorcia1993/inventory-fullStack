<script setup lang="ts">
import { RouterView, RouterLink } from 'vue-router'
</script>

<template>
  <div class="app">
    <header class="app__header">
      <div class="container">
        <nav class="app__nav">
          <RouterLink to="/products" class="app__logo">InventoryApp</RouterLink>
          <div class="app__links">
            <RouterLink to="/products" class="app__link" active-class="app__link--active">
              Productos
            </RouterLink>
            <RouterLink to="/payments" class="app__link" active-class="app__link--active">
              Pagos
            </RouterLink>
          </div>
        </nav>
      </div>
    </header>

    <main class="app__main">
      <div class="container">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style lang="scss">
@use '@/assets/styles/tokens' as t;
@use '@/assets/styles/mixins' as m;

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  &__header {
    background-color: t.$color-surface;
    border-bottom: 1px solid t.$color-border;
    box-shadow: t.$shadow-sm;
  }

  &__nav {
    @include m.flex-between;
    height: 4rem;
    gap: t.$spacing-6;
  }

  &__logo {
    font-size: t.$font-size-xl;
    font-weight: 700;
    color: t.$color-primary;
  }

  &__links {
    display: flex;
    gap: t.$spacing-4;
  }

  &__link {
    font-size: t.$font-size-sm;
    font-weight: 500;
    color: t.$color-text-muted;
    padding: t.$spacing-1 t.$spacing-2;
    border-radius: t.$radius-md;
    transition: color t.$transition-base, background-color t.$transition-base;

    &:hover,
    &--active {
      color: t.$color-primary;
      background-color: rgb(37 99 235 / 0.08);
    }
  }

  &__main {
    flex: 1;
    padding: t.$spacing-8 0;
  }
}
</style>
