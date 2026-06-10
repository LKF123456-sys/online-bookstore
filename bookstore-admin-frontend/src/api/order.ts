import request from './request'
import type { PageResult, OrderVO } from '@/types'

export const orderApi = {
  getList(params: { pageNum: number; pageSize: number; status?: number }) {
    return request.get<PageResult<OrderVO>>('/admin/order/list', { params })
  },
  getDetail(id: number) {
    return request.get<OrderVO>(`/admin/order/${id}`)
  },
  ship(id: number) {
    return request.post(`/admin/order/${id}/ship`)
  },
}
