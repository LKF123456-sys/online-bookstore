import request from './request'
import type { PageResult, MessageVO, BroadcastRequest } from '@/types'

export const messageApi = {
  getList(params: { pageNum: number; pageSize: number }) {
    return request.get<PageResult<MessageVO>>('/admin/message/list', { params })
  },
  broadcast(data: BroadcastRequest) {
    return request.post('/admin/message/broadcast', data)
  },
}
