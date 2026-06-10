import request from './request'
import type { PageResult, UserVO } from '@/types'

export const userApi = {
  getList(params: { pageNum: number; pageSize: number; keyword?: string }) {
    return request.get<PageResult<UserVO>>('/admin/user/list', { params })
  },
  updateStatus(id: number, status: number) {
    return request.put(`/admin/user/${id}/status`, { status })
  },
  delete(id: number) {
    return request.delete(`/admin/user/${id}`)
  },
}
