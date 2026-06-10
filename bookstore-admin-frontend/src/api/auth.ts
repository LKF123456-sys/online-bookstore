import request from './request'
import type { LoginRequest, LoginResponse, UserVO } from '@/types'

export const authApi = {
  login(data: LoginRequest) {
    return request.post<LoginResponse>('/api/auth/login', data)
  },
  getUserInfo(id: number) {
    return request.get<UserVO>(`/api/user/${id}`)
  },
}
