import client from './client'
import type { OrderResponse, CheckoutRequest } from '../types'

export async function checkout(data: CheckoutRequest): Promise<OrderResponse> {
  const res = await client.post<OrderResponse>('/api/checkout', data)
  return res.data
}
