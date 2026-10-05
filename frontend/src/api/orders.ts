import client from './client'
import type { OrderResponse, PageResponse, CartResponse } from '../types'

export async function getOrders(params?: {
  page?: number
  size?: number
}): Promise<PageResponse<OrderResponse>> {
  const res = await client.get<PageResponse<OrderResponse>>('/api/orders', { params })
  return res.data
}

export async function getOrder(id: number): Promise<OrderResponse> {
  const res = await client.get<OrderResponse>(`/api/orders/${id}`)
  return res.data
}

export async function cancelOrder(id: number): Promise<OrderResponse> {
  const res = await client.post<OrderResponse>(`/api/orders/${id}/cancel`)
  return res.data
}

export async function buyAgain(
  orderId: number,
  orderItemId: number,
  quantity?: number,
): Promise<CartResponse> {
  const res = await client.post<CartResponse>(`/api/orders/${orderId}/buy-again`, {
    orderItemId,
    ...(quantity !== undefined ? { quantity } : {}),
  })
  return res.data
}
