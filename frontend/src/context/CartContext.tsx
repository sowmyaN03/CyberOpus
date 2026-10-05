import React, { createContext, useState, useEffect, useCallback } from 'react'
import type { CartResponse, CartItemResponse } from '../types'
import {
  getCart,
  addToCart as apiAddToCart,
  updateCartItem as apiUpdateCartItem,
  removeCartItem as apiRemoveCartItem,
  clearCart as apiClearCart,
} from '../api/cart'
import { useAuth } from '../hooks/useAuth'

interface CartContextValue {
  cart: CartResponse | null
  loading: boolean
  addToCart: (bookId: number, quantity?: number) => Promise<void>
  updateItem: (itemId: number, quantity: number) => Promise<void>
  removeItem: (itemId: number) => Promise<void>
  clearCart: () => Promise<void>
  refreshCart: () => Promise<void>
  itemCount: number
  items: CartItemResponse[]
}

export const CartContext = createContext<CartContextValue>({
  cart: null,
  loading: false,
  addToCart: async () => {},
  updateItem: async () => {},
  removeItem: async () => {},
  clearCart: async () => {},
  refreshCart: async () => {},
  itemCount: 0,
  items: [],
})

export function CartProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  const [cart, setCart] = useState<CartResponse | null>(null)
  const [loading, setLoading] = useState(false)

  const refreshCart = useCallback(async () => {
    if (!user) {
      setCart(null)
      return
    }
    setLoading(true)
    try {
      const data = await getCart()
      setCart(data)
    } catch {
      setCart(null)
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    refreshCart()
  }, [refreshCart])

  async function addToCart(bookId: number, quantity = 1) {
    const data = await apiAddToCart(bookId, quantity)
    setCart(data)
  }

  async function updateItem(itemId: number, quantity: number) {
    const data = await apiUpdateCartItem(itemId, quantity)
    setCart(data)
  }

  async function removeItem(itemId: number) {
    const data = await apiRemoveCartItem(itemId)
    setCart(data)
  }

  async function clearCart() {
    await apiClearCart()
    setCart(null)
  }

  return (
    <CartContext.Provider
      value={{
        cart,
        loading,
        addToCart,
        updateItem,
        removeItem,
        clearCart,
        refreshCart,
        itemCount: cart?.itemCount ?? 0,
        items: cart?.items ?? [],
      }}
    >
      {children}
    </CartContext.Provider>
  )
}
