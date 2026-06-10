import request from './request'
import type { PageResult, ApiLogVO } from '@/types'

export const logApi = {
  getList(params: { pageNum: number; pageSize: number; keyword?: string }) {
    return request.get<PageResult<ApiLogVO>>('/admin/api/log/list', { params })
  },
  getDetail(id: number) {
    return request.get<ApiLogVO>(`/admin/api/log/${id}`)
  },
}
