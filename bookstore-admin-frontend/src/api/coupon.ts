import request from './request'
import type { PageResult, CouponVO, CouponForm } from '@/types'

export const couponApi = {
  getList(params: { pageNum: number; pageSize: number }) {
    return request.get<PageResult<CouponVO>>('/admin/coupon/list', { params })
  },
  create(data: CouponForm) {
    return request.post('/admin/coupon', data)
  },
  update(id: number, data: CouponForm) {
    return request.put(`/admin/coupon/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/admin/coupon/${id}`)
  },
  updateStatus(id: number, status: number) {
    return request.put(`/admin/coupon/${id}/status`, { status })
  },
}
