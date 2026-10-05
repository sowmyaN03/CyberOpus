import client from './client'
import type { CartResponse } from '../types'

export async function getCart(): Promise<CartResponse> {
  const res = await client.get<CartResponse>('/api/cart')
  return res.data
}

export async function addToCart(bookId: number, quantity: number): Promise<CartResponse> {
  const res = await client.post<CartResponse>('/api/cart/items', { bookId, quantity })
  return res.data
}

export async function updateCartItem(itemId: number, quantity: number): Promise<CartResponse> {
  const res = await client.put<CartResponse>(`/api/cart/items/${itemId}`, { quantity })
  return res.data
}

export async function removeCartItem(itemId: number): Promise<CartResponse> {
  const res = await client.delete<CartResponse>(`/api/cart/items/${itemId}`)
  return res.data
}

export async function clearCart(): Promise<void> {
  await client.delete('/api/cart')
}
