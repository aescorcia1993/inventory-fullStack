<script setup lang="ts">
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { z } from 'zod'
import { useInventoryStore } from '@/stores/inventory'

const props = defineProps<{ productId: string }>()

const inventoryStore = useInventoryStore()

const schema = toTypedSchema(
  z.object({
    cantidad: z.coerce
      .number({ invalid_type_error: 'Debe ser un número' })
      .int('Debe ser un número entero')
      .positive('La cantidad debe ser mayor que 0')
  })
)

const { handleSubmit, errors, isSubmitting, defineField, resetForm } = useForm({
  validationSchema: schema
})

const [cantidad, cantidadAttrs] = defineField('cantidad')

const onSubmit = handleSubmit(async values => {
  await inventoryStore.makePurchase(props.productId, values.cantidad)
  resetForm()
})
</script>

<template>
  <div class="purchase-form">
    <h2 class="purchase-form__title">Realizar Compra</h2>

    <div
      v-if="inventoryStore.purchaseSuccess"
      class="purchase-form__feedback purchase-form__feedback--success"
    >
      ¡Compra realizada con éxito!
    </div>

    <div
      v-if="inventoryStore.error"
      class="purchase-form__feedback purchase-form__feedback--error"
    >
      {{ inventoryStore.error }}
    </div>

    <form class="purchase-form__form" @submit="onSubmit" novalidate>
      <div class="purchase-form__field">
        <label class="purchase-form__label" for="cantidad">Cantidad *</label>
        <input
          id="cantidad"
          v-model="cantidad"
          v-bind="cantidadAttrs"
          type="number"
          min="1"
          class="purchase-form__input"
          :class="{ 'is-invalid': errors.cantidad }"
          placeholder="1"
        />
        <span v-if="errors.cantidad" class="purchase-form__error-msg">{{ errors.cantidad }}</span>
      </div>

      <button
        type="submit"
        class="purchase-form__btn"
        :disabled="isSubmitting || inventoryStore.loading"
      >
        {{ isSubmitting ? 'Procesando...' : 'Comprar' }}
      </button>
    </form>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/tokens' as t;
@use '@/assets/styles/mixins' as m;

.purchase-form {
  @include m.card;
  margin-top: t.$spacing-6;

  &__title {
    font-size: t.$font-size-xl;
    font-weight: 600;
    margin-bottom: t.$spacing-6;
  }

  &__feedback {
    padding: t.$spacing-3 t.$spacing-4;
    border-radius: t.$radius-md;
    font-size: t.$font-size-sm;
    margin-bottom: t.$spacing-4;

    &--success {
      background-color: rgb(22 163 74 / 0.08);
      border: 1px solid t.$color-success;
      color: t.$color-success;
    }

    &--error {
      background-color: rgb(220 38 38 / 0.08);
      border: 1px solid t.$color-danger;
      color: t.$color-danger;
    }
  }

  &__form {
    display: flex;
    flex-direction: column;
    gap: t.$spacing-4;
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: t.$spacing-1;
  }

  &__label {
    font-size: t.$font-size-sm;
    font-weight: 500;
  }

  &__input {
    @include m.input-base;

    &.is-invalid {
      border-color: t.$color-danger;
    }
  }

  &__error-msg {
    font-size: t.$font-size-sm;
    color: t.$color-danger;
  }

  &__btn {
    @include m.button-base;
    background-color: t.$color-success;
    color: white;
    align-self: flex-start;

    &:hover:not(:disabled) {
      background-color: darken(#16a34a, 8%);
    }
  }
}
</style>
