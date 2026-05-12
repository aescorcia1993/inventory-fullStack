import api, { deserializeList } from '@/plugins/axios'
import type { Payment } from '../types/payment.types'

export async function fetchPayments(): Promise<Payment[]> {
  const response = await api.get('/api/v1/payments')
  return deserializeList<Omit<Payment, 'id'>>(response.data) as Payment[]
}
