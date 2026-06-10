import request from './request'
import type { PageResult, ReviewVO } from '@/types'

export const reviewApi = {
  getList(params: { pageNum: number; pageSize: number; blocked?: boolean }) {
    return request.get<PageResult<ReviewVO>>('/admin/review/list', { params })
  },
  block(id: number) {
    return request.post(`/admin/review/${id}/block`)
  },
  unblock(id: number) {
    return request.post(`/admin/review/${id}/unblock`)
  },
  top(id: number) {
    return request.post(`/admin/review/${id}/top`)
  },
  reply(id: number, reply: string) {
    return request.post(`/admin/review/${id}/reply`, { reply })
  },
  delete(id: number) {
    return request.delete(`/admin/review/${id}`)
  },
}
