---
name: senior-vuejs
description: >
  Senior Frontend engineer expertise for Vue.js 3 applications. USE FOR: Vue 3 Composition API,
  Pinia state management, Vue Router, TypeScript with Vue, component design, reactive patterns,
  form validation (VeeValidate/Zod), API integration (Axios/Fetch), error handling, loading states,
  accessibility (ARIA), responsive layouts, unit testing (Vitest + Vue Test Utils), E2E testing
  (Playwright/Cypress), performance optimization (lazy loading, virtual scrolling), build configuration
  (Vite), environment variables, JSON:API client integration, and authentication flows (JWT storage,
  interceptors, route guards), and CSS architecture with BEM (Block Element Modifier) naming
  convention using SCSS/SASS (sass package, @use/@forward module system). DO NOT USE FOR: backend
  topics, infrastructure, or React/Angular.
---

# Senior Vue.js 3 Frontend

You are a senior Vue.js 3 engineer. Every piece of code must be clean, typed, testable, and
accessible. Apply these standards consistently.

---

## Project Structure

```
src/
 assets/           # Static files, global CSS / design tokens
 components/       # Reusable, dumb (presentational) components
  ui/              #   Generic: Button, Input, Modal, Badge…
  common/          #   Shared domain-agnostic: Pagination, DataTable…
 features/         # Feature modules (products, inventory, orders…)
  products/
   components/     #   Smart components for this feature
   composables/    #   Feature-scoped composables
   views/          #   Route-level components
   store/          #   Pinia store for this feature
   api/            #   Axios resource module
   types/          #   TypeScript interfaces / schemas
 router/           # Vue Router config + route guards
 store/            # Global stores (auth, UI)
 composables/      # Global composables (useToast, useErrorHandler…)
 utils/            # Pure helper functions
 plugins/          # Vue plugin registrations (i18n, axios, etc.)
 types/            # Global TypeScript types
```

- **No business logic in views** — delegate to composables and stores.
- **No API calls in components** — only through dedicated `api/` modules or composables.
- **No `any` type** — use TypeScript strictly.

---

## Composition API Patterns

```typescript
// composables/useProducts.ts
import { ref, computed } from 'vue'
import { productsApi } from '@/features/products/api/products.api'
import type { Product } from '@/features/products/types'

export function useProducts() {
  const products = ref<Product[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const total = computed(() => products.value.length)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      products.value = await productsApi.list()
    } catch (err) {
      error.value = 'Error al cargar productos'
    } finally {
      loading.value = false
    }
  }

  return { products, loading, error, total, fetchAll }
}
```

- Always return `{ data, loading, error }` from async composables.
- Use `watchEffect` for derived side-effects; `watch` with `{ immediate: true }` for fetch-on-param-change.
- Clean up subscriptions and timers in `onUnmounted`.

---

## State Management with Pinia

```typescript
// features/products/store/products.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { productsApi } from '../api/products.api'
import type { Product } from '../types'

export const useProductsStore = defineStore('products', () => {
  const items = ref<Product[]>([])
  const selected = ref<Product | null>(null)
  const loading = ref(false)

  const count = computed(() => items.value.length)

  async function load() {
    loading.value = true
    try {
      items.value = await productsApi.list()
    } finally {
      loading.value = false
    }
  }

  function select(product: Product) {
    selected.value = product
  }

  return { items, selected, loading, count, load, select }
})
```

- Use **setup stores** (function syntax) over options stores.
- One store per feature domain.
- Never mutate store state outside the store — expose actions.

---

## API Layer (JSON:API)

```typescript
// features/products/api/products.api.ts
import { http } from '@/plugins/axios'
import type { Product } from '../types'

function deserialize<T>(response: JsonApiResponse<T>): T {
  return { id: response.data.id, ...response.data.attributes } as T
}

export const productsApi = {
  async list(): Promise<Product[]> {
    const { data } = await http.get<JsonApiListResponse<ProductAttributes>>('/api/v1/products')
    return data.data.map(item => deserialize(item))
  },
  async get(id: string): Promise<Product> {
    const { data } = await http.get<JsonApiResponse<ProductAttributes>>(`/api/v1/products/${id}`)
    return deserialize(data)
  },
  async create(payload: CreateProductDto): Promise<Product> {
    const { data } = await http.post<JsonApiResponse<ProductAttributes>>('/api/v1/products', {
      data: { type: 'products', attributes: payload }
    })
    return deserialize(data)
  }
}
```

- Centralise Axios instance in `plugins/axios.ts` with base URL, timeout, and interceptors.
- Add **request interceptor** to inject `Authorization` header from auth store.
- Add **response interceptor** to handle 401 (refresh token or redirect to login) and 422 (surface validation errors).

---

## Component Design

```vue
<!-- components/ui/AppButton.vue -->
<script setup lang="ts">
type Variant = 'primary' | 'secondary' | 'danger'
type Size = 'sm' | 'md' | 'lg'

interface Props {
  variant?: Variant
  size?: Size
  loading?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false
})
</script>

<template>
  <button
    :class="['btn', `btn--${variant}`, `btn--${size}`]"
    :disabled="disabled || loading"
    :aria-busy="loading"
  >
    <span v-if="loading" class="btn__spinner" aria-hidden="true" />
    <slot />
  </button>
</template>
```

- Use `defineProps<Interface>()` with TypeScript — no runtime `PropType`.
- Always emit typed events: `defineEmits<{ (e: 'submit', value: FormData): void }>()`.
- Use `v-bind="$attrs"` in wrapper components to pass HTML attributes down correctly.
- Provide ARIA labels on interactive elements; use semantic HTML first.

---

## Forms & Validation

```typescript
// Use VeeValidate + Zod schema
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { z } from 'zod'

const schema = toTypedSchema(z.object({
  name: z.string().min(3, 'Mínimo 3 caracteres'),
  price: z.number().positive('Debe ser mayor a 0')
}))

const { handleSubmit, errors } = useForm({ validationSchema: schema })
const onSubmit = handleSubmit(async (values) => { … })
```

---

## Routing & Guards

```typescript
// router/index.ts
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
})
```

- Define route meta types via module augmentation (`RouteMeta` interface).
- Lazy-load feature routes: `component: () => import('@/features/products/views/ProductList.vue')`.

---

## Testing Strategy

| Level | Tool | What to test |
|-------|------|--------------|
| Unit (composables/stores) | Vitest | Logic, state transitions, computed |
| Component | Vue Test Utils + Vitest | Rendering, events, slots |
| E2E | Playwright | Critical user flows |

```typescript
// Example composable test
describe('useProducts', () => {
  it('sets loading true while fetching', async () => {
    vi.mocked(productsApi.list).mockResolvedValue([])
    const { loading, fetchAll } = useProducts()
    const promise = fetchAll()
    expect(loading.value).toBe(true)
    await promise
    expect(loading.value).toBe(false)
  })
})
```

---

## Performance

- Lazy-load routes and heavy components (`defineAsyncComponent`).
- Use `v-once` for static content; `v-memo` for expensive list items.
- Avoid large reactive objects; use `shallowRef` for non-reactive nested data.
- Tree-shake UI libraries — import components individually.

---

## SASS Setup

### Installation

```bash
npm install -D sass
```

Vite has built-in SASS support — no additional Vite plugin is needed. The `sass` package (Dart Sass)
is the only preprocessor required.

### Vite configuration — global SASS imports

Inject the design-token partials automatically into every component so you never need manual
`@use` in each file:

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  css: {
    preprocessorOptions: {
      scss: {
        // Dart Sass modern API
        api: 'modern-compiler',
        // Auto-import tokens and mixins into every <style lang="scss"> block
        additionalData: `
          @use '@/assets/styles/tokens' as t;
          @use '@/assets/styles/mixins' as m;
        `
      }
    }
  },
  resolve: {
    alias: { '@': '/src' }
  }
})
```

> **Note:** Use `api: 'modern-compiler'` (Dart Sass ≥ 1.45) to suppress the legacy `@import`
> deprecation warnings. Never use `@import` — always `@use` / `@forward`.

### SASS Module System rules

| Rule | Reason |
|------|--------|
| `@use` instead of `@import` | Scoped namespaces, no global leakage |
| `@forward` in `_index.scss` barrels | Single entry-point per directory |
| `@use 'sass:math'`, `'sass:map'`, etc. | Built-in modules must be loaded explicitly |
| No `@import` anywhere | Deprecated in Dart Sass; causes duplicate output |

```scss
// assets/styles/_index.scss  (barrel — forwards everything)
@forward 'tokens';
@forward 'mixins';
@forward 'reset';
```

```scss
// Correct usage inside a partial
@use 'sass:math';
@use 'sass:map';

$half: math.div(16px, 2);   // NOT 16px / 2
```

### `stylelint` for SCSS

```bash
npm install -D stylelint stylelint-config-standard-scss stylelint-selector-bem-pattern
```

```json
// .stylelintrc.json
{
  "extends": ["stylelint-config-standard-scss"],
  "plugins": ["stylelint-selector-bem-pattern"],
  "rules": {
    "plugin/selector-bem-pattern": {
      "preset": "bem"
    },
    "scss/at-import-no-partial-leading-underscore": true,
    "scss/at-rule-no-unknown": true,
    "no-descending-specificity": true
  }
}
```

Add to `package.json`:

```json
"scripts": {
  "lint:styles": "stylelint \"src/**/*.{vue,scss}\""
}
```

---

## Styles — BEM with SCSS

Use **BEM (Block__Element--Modifier)** for every CSS class. Never use generic class names like
`.container`, `.wrapper`, or `.item` without a block namespace.

### Naming Rules

```
.block                    → standalone component
.block__element           → part of a block (double underscore)
.block--modifier          → variation of a block (double dash)
.block__element--modifier → variation of an element
```

### SCSS File per Component

Each `.vue` file owns its styles in a scoped `<style lang="scss">` block.
Global design tokens (colors, spacing, typography) live in `src/assets/styles/`.

```
src/assets/styles/
 _tokens.scss      # CSS custom properties / SCSS variables
 _mixins.scss      # Reusable mixins (respond-to, truncate…)
 _reset.scss       # Minimal CSS reset
 main.scss         # Imports tokens + reset; no component styles here
```

### Example — ProductCard component

```vue
<!-- features/products/components/ProductCard.vue -->
<template>
  <article class="product-card" :class="{ 'product-card--out-of-stock': !inStock }">
    <header class="product-card__header">
      <h2 class="product-card__title">{{ product.name }}</h2>
      <span class="product-card__badge product-card__badge--price">
        {{ formatCurrency(product.price) }}
      </span>
    </header>

    <div class="product-card__body">
      <p class="product-card__description">{{ product.description }}</p>
    </div>

    <footer class="product-card__footer">
      <AppButton
        class="product-card__action"
        :disabled="!inStock"
        @click="$emit('buy', product.id)"
      >
        Comprar
      </AppButton>
    </footer>
  </article>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/tokens' as t;
@use '@/assets/styles/mixins' as m;

.product-card {
  border: 1px solid t.$color-border;
  border-radius: t.$radius-md;
  padding: t.$spacing-4;
  background: t.$color-surface;
  transition: box-shadow 0.2s ease;

  &:hover {
    box-shadow: t.$shadow-md;
  }

  // --modifier on the block
  &--out-of-stock {
    opacity: 0.5;
    pointer-events: none;
  }

  // __elements
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: t.$spacing-2;
  }

  &__title {
    font-size: t.$font-size-lg;
    font-weight: t.$font-weight-semibold;
    color: t.$color-text-primary;
  }

  &__badge {
    display: inline-block;
    padding: t.$spacing-1 t.$spacing-2;
    border-radius: t.$radius-sm;
    font-size: t.$font-size-sm;

    &--price {
      background: t.$color-primary-100;
      color: t.$color-primary-700;
    }
  }

  &__body {
    margin-bottom: t.$spacing-3;
  }

  &__description {
    color: t.$color-text-secondary;
    @include m.line-clamp(3);
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
  }

  &__action {
    width: 100%;

    @include m.respond-to('md') {
      width: auto;
    }
  }
}
</style>
```

### Design Tokens (`_tokens.scss`)

```scss
// Map tokens to CSS custom properties for runtime theming
:root {
  --color-primary-100: #dbeafe;
  --color-primary-700: #1d4ed8;
  --color-surface:     #ffffff;
  --color-border:      #e2e8f0;
  --color-text-primary:   #1e293b;
  --color-text-secondary: #64748b;

  --spacing-1: 0.25rem;
  --spacing-2: 0.5rem;
  --spacing-3: 0.75rem;
  --spacing-4: 1rem;

  --radius-sm: 4px;
  --radius-md: 8px;

  --font-size-sm: 0.875rem;
  --font-size-lg: 1.125rem;
  --font-weight-semibold: 600;

  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

// SCSS variables referencing the custom properties
$color-primary-100:     var(--color-primary-100);
$color-primary-700:     var(--color-primary-700);
$color-surface:         var(--color-surface);
$color-border:          var(--color-border);
$color-text-primary:    var(--color-text-primary);
$color-text-secondary:  var(--color-text-secondary);
$spacing-1: var(--spacing-1);
$spacing-2: var(--spacing-2);
$spacing-3: var(--spacing-3);
$spacing-4: var(--spacing-4);
$radius-sm: var(--radius-sm);
$radius-md: var(--radius-md);
$font-size-sm: var(--font-size-sm);
$font-size-lg: var(--font-size-lg);
$font-weight-semibold: var(--font-weight-semibold);
$shadow-md: var(--shadow-md);
```

### Mixins (`_mixins.scss`)

```scss
@mixin respond-to($breakpoint) {
  $breakpoints: ('sm': 640px, 'md': 768px, 'lg': 1024px, 'xl': 1280px);
  @media (min-width: map.get($breakpoints, $breakpoint)) { @content; }
}

@mixin line-clamp($lines: 2) {
  display: -webkit-box;
  -webkit-line-clamp: $lines;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@mixin visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
```

### BEM Rules Enforced

- **One block = one component** — the block name matches the Vue component file name in kebab-case.
- Never nest BEM classes deeper than `block__element--modifier` — avoid `block__el1__el2`.
- Never use element selectors (`h2`, `p`) for styling — always a BEM class.
- Modifier classes are always added **in addition to** the base class, never instead of it.
- Use `scoped` styles to prevent leakage; global styles only for design tokens and resets.
- `stylelint` configured with `stylelint-config-standard-scss` + `stylelint-selector-bem-pattern`.

---

## Code Quality

- ESLint with `plugin:vue/vue3-recommended` + `@typescript-eslint/recommended`.
- Prettier with `.prettierrc` committed to the repo.
- No `console.log` in production — use a configurable logger utility.
- All exported functions must have JSDoc summaries.
