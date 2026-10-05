import client from './client'
import type { PaymentResponse } from '../types'

export async function getPaymentByOrder(orderId: number): Promise<PaymentResponse> {
  const res = await client.get<PaymentResponse>(`/api/payments/order/${orderId}`)
  return res.data
}
