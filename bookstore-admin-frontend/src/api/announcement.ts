import request from './request'
import type { PageResult, AnnouncementVO, AnnouncementForm } from '@/types'

export const announcementApi = {
  getList(params: { pageNum: number; pageSize: number }) {
    return request.get<PageResult<AnnouncementVO>>('/admin/announcement/list', { params })
  },
  create(data: AnnouncementForm) {
    return request.post('/admin/announcement', data)
  },
  update(id: number, data: AnnouncementForm) {
    return request.put(`/admin/announcement/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/admin/announcement/${id}`)
  },
}
