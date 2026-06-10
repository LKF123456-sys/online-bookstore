import http from './index'
import type { LoginRequest, LoginResponse, RegisterRequest, UserVO, UpdatePasswordRequest, UpdateProfileRequest } from '@/types'

export function login(data: LoginRequest): Promise<LoginResponse> {
  return http.post('/api/auth/login', data)
}

export function register(data: RegisterRequest): Promise<void> {
  return http.post('/api/auth/register', data)
}

export function getUser(): Promise<UserVO> {
  return http.get('/api/auth/profile')
}

export function updatePassword(data: UpdatePasswordRequest): Promise<void> {
  return http.put('/api/auth/password', data)
}

export function updateProfile(data: UpdateProfileRequest): Promise<UserVO> {
  return http.put('/api/auth/profile', data)
}
