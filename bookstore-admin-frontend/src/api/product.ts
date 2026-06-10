import request from './request'
import type { PageResult, ProductVO, ProductForm } from '@/types'

export const productApi = {
  getList(params: { pageNum: number; pageSize: number; keyword?: string }) {
    return request.get<PageResult<ProductVO>>('/api/product/list', { params })
  },
  create(data: ProductForm) {
    return request.post('/admin/product', data)
  },
  update(id: string | number, data: ProductForm) {
    return request.put(`/admin/product/${id}`, data)
  },
  delete(id: string | number) {
    return request.delete(`/admin/product/${id}`)
  },
  updateStatus(id: string | number, status: number) {
    return request.put(`/admin/product/${id}/status`, { status })
  },
}
