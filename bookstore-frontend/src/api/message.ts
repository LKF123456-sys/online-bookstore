import http from './index'
import type { MessageVO, PageResult } from '@/types'

export async function getMessageList(pageNum: number = 1, pageSize: number = 10): Promise<PageResult<MessageVO>> {
  try {
    return await http.get('/api/message/list', { params: { pageNum, pageSize } })
  } catch {
    return { records: [], list: [], total: 0, pageNum, pageSize }
  }
}

export async function markAsRead(id: number): Promise<void> {
  try {
    await http.put(`/api/message/${id}/read`)
  } catch {
    // 静默处理
  }
}

export async function markAllAsRead(): Promise<void> {
  try {
    await http.put('/api/message/read-all')
  } catch {
    // 静默处理
  }
}

export async function getUnreadCount(): Promise<number> {
  try {
    return await http.get('/api/message/unread-count')
  } catch {
    return 0
  }
}
