import http from './index'
import type { OrderVO, CreateOrderRequest, PageResult } from '@/types'

export function createOrder(data: CreateOrderRequest): Promise<OrderVO> {
  return http.post('/api/orders', data)
}

export function getOrder(id: string | number): Promise<OrderVO> {
  return http.get(`/api/orders/${id}`)
}

export function getOrderList(params: { pageNum?: number; pageSize?: number; status?: number | string }): Promise<PageResult<OrderVO>> {
  return http.get('/api/orders', { params })
}

export function payOrder(id: string | number): Promise<void> {
  return http.post(`/api/orders/${id}/pay`)
}

export function cancelOrder(id: string | number): Promise<void> {
  return http.post(`/api/orders/${id}/cancel`)
}

export function confirmOrder(id: string | number): Promise<void> {
  return http.post(`/api/orders/${id}/confirm`)
}
