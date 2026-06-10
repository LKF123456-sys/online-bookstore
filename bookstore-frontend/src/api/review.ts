import http from './index'
import type { ReviewVO, CreateReviewRequest, PageResult } from '@/types'

export function getProductReviews(productId: number, pageNum: number = 1, pageSize: number = 10): Promise<PageResult<ReviewVO>> {
  return http.get(`/api/reviews/product/${productId}`, { params: { pageNum, pageSize } })
}

export function createReview(data: CreateReviewRequest): Promise<void> {
  return http.post('/api/reviews', data)
}

export function getMyReviews(pageNum: number = 1, pageSize: number = 10): Promise<PageResult<ReviewVO>> {
  return http.get('/api/reviews/user', { params: { pageNum, pageSize } })
}
