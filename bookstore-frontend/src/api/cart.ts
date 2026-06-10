import http from './index'
import type { CartVO, AddToCartRequest } from '@/types'

export function getCart(): Promise<CartVO> {
  return http.get('/api/orders/cart')
}

export function addToCart(data: AddToCartRequest): Promise<void> {
  return http.post('/api/orders/cart', data)
}

export function updateCartItem(productId: number, quantity: number): Promise<void> {
  return http.put(`/api/orders/cart/${productId}`, { productId, quantity })
}

export function removeCartItem(productId: number): Promise<void> {
  return http.delete(`/api/orders/cart/${productId}`)
}

export function clearCart(): Promise<void> {
  return http.delete('/api/orders/cart/clear')
}
