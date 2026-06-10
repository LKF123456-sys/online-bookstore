import http from './index'
import type { AnnouncementVO } from '@/types'

export async function getActiveAnnouncements(): Promise<AnnouncementVO[]> {
  try {
    const res: any = await http.get('/api/coupons/announcements')
    if (Array.isArray(res)) return res
    if (res?.records) return res.records
    return []
  } catch {
    return []
  }
}
