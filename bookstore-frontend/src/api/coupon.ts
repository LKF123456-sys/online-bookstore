import http from './index'
import type { CouponVO, PageResult } from '@/types'

export function getCouponList(pageNum: number = 1, pageSize: number = 10): Promise<PageResult<CouponVO>> {
  return http.get('/api/coupons', { params: { pageNum, pageSize } })
}

export function claimCoupon(id: number): Promise<void> {
  return http.post(`/api/coupons/${id}/claim`)
}

export function getMyCoupons(): Promise<CouponVO[]> {
  return http.get('/api/coupons/my')
}
