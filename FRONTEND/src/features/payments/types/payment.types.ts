export interface Payment {
  id: string
  purchaseId: string
  productoId: string
  cantidad: number
  total: number
  status: 'PROCESSING' | 'COMPLETED'
  receivedAt: string
  processedAt: string | null
}
