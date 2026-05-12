<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { z } from 'zod'
import { useProductsStore } from '@/stores/products'

const router = useRouter()
const productsStore = useProductsStore()

const schema = toTypedSchema(
  z.object({
    nombre: z.string().min(1, 'El nombre es obligatorio'),
    precio: z.coerce.number({ invalid_type_error: 'Debe ser un número' }).positive('El precio debe ser mayor que 0'),
    descripcion: z.string().optional(),
    stockInicial: z.coerce.number({ invalid_type_error: 'Debe ser un número' }).int('Debe ser un número entero').min(0, 'No puede ser negativo')
  })
)

const { handleSubmit, errors, isSubmitting, defineField } = useForm({ validationSchema: schema })

const [nombre, nombreAttrs] = defineField('nombre')
const [precio, precioAttrs] = defineField('precio')
const [descripcion, descripcionAttrs] = defineField('descripcion')
const [stockInicial, stockInicialAttrs] = defineField('stockInicial')

const onSubmit = handleSubmit(async values => {
  await productsStore.createProduct(
    { nombre: values.nombre, precio: values.precio, descripcion: values.descripcion },
    values.stockInicial
  )
  router.push('/products')
})
</script>

<template>
  <div class="product-create">
    <header class="product-create__header">
      <h1 class="product-create__title">Nuevo Producto</h1>
    </header>

    <form class="product-create__form" @submit="onSubmit" novalidate>
      <div v-if="productsStore.error" class="product-create__error">
        {{ productsStore.error }}
      </div>

      <div class="product-create__field">
        <label class="product-create__label" for="nombre">Nombre *</label>
        <input
          id="nombre"
          v-model="nombre"
          v-bind="nombreAttrs"
          type="text"
          class="product-create__input"
          :class="{ 'is-invalid': errors.nombre }"
          placeholder="Nombre del producto"
        />
        <span v-if="errors.nombre" class="product-create__error-msg">{{ errors.nombre }}</span>
      </div>

      <div class="product-create__field">
        <label class="product-create__label" for="precio">Precio *</label>
        <input
          id="precio"
          v-model="precio"
          v-bind="precioAttrs"
          type="number"
          step="0.01"
          min="0"
          class="product-create__input"
          :class="{ 'is-invalid': errors.precio }"
          placeholder="0.00"
        />
        <span v-if="errors.precio" class="product-create__error-msg">{{ errors.precio }}</span>
      </div>

      <div class="product-create__field">
        <label class="product-create__label" for="descripcion">Descripción</label>
        <textarea
          id="descripcion"
          v-model="descripcion"
          v-bind="descripcionAttrs"
          class="product-create__input product-create__textarea"
          :class="{ 'is-invalid': errors.descripcion }"
          placeholder="Descripción opcional"
          rows="3"
        />
        <span v-if="errors.descripcion" class="product-create__error-msg">{{ errors.descripcion }}</span>
      </div>

      <div class="product-create__field">
        <label class="product-create__label" for="stockInicial">Stock Inicial *</label>
        <input
          id="stockInicial"
          v-model="stockInicial"
          v-bind="stockInicialAttrs"
          type="number"
          min="0"
          step="1"
          class="product-create__input"
          :class="{ 'is-invalid': errors.stockInicial }"
          placeholder="0"
        />
        <span v-if="errors.stockInicial" class="product-create__error-msg">{{ errors.stockInicial }}</span>
      </div>

      <div class="product-create__actions">
        <button type="button" class="product-create__btn product-create__btn--secondary" @click="router.back()">
          Cancelar
        </button>
        <button type="submit" class="product-create__btn product-create__btn--primary" :disabled="isSubmitting">
          {{ isSubmitting ? 'Guardando...' : 'Crear Producto' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/tokens' as t;
@use '@/assets/styles/mixins' as m;

.product-create {
  max-width: 600px;
  margin: 0 auto;

  &__header {
    margin-bottom: t.$spacing-8;
  }

  &__title {
    font-size: t.$font-size-2xl;
    font-weight: 700;
  }

  &__form {
    @include m.card;
    display: flex;
    flex-direction: column;
    gap: t.$spacing-6;
  }

  &__error {
    padding: t.$spacing-3 t.$spacing-4;
    background-color: rgb(220 38 38 / 0.08);
    border: 1px solid t.$color-danger;
    border-radius: t.$radius-md;
    color: t.$color-danger;
    font-size: t.$font-size-sm;
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: t.$spacing-1;
  }

  &__label {
    font-size: t.$font-size-sm;
    font-weight: 500;
    color: t.$color-text;
  }

  &__input {
    @include m.input-base;

    &.is-invalid {
      border-color: t.$color-danger;
    }
  }

  &__textarea {
    resize: vertical;
  }

  &__error-msg {
    font-size: t.$font-size-sm;
    color: t.$color-danger;
  }

  &__actions {
    display: flex;
    gap: t.$spacing-4;
    justify-content: flex-end;
  }

  &__btn {
    @include m.button-base;

    &--primary {
      background-color: t.$color-primary;
      color: white;

      &:hover:not(:disabled) {
        background-color: t.$color-primary-hover;
      }
    }

    &--secondary {
      background-color: transparent;
      color: t.$color-text-muted;
      border: 1px solid t.$color-border;

      &:hover:not(:disabled) {
        background-color: t.$color-bg;
      }
    }
  }
}
</style>
