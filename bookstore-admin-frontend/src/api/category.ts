import request from './request'
import type { CategoryVO } from '@/types'

export const categoryApi = {
  getList() {
    return request.get<CategoryVO[]>('/api/category/list')
  },
}
