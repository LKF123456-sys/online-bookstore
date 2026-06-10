import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserVO } from '@/types'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const user = ref<UserVO | null>(null)

  const isAuthenticated = () => {
    return !!token.value && !!user.value
  }

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password })
    if (res.data.user.role !== 'admin') {
      throw new Error('Access denied: not an admin user')
    }
    token.value = res.data.token
    user.value = res.data.user
  }

  const logout = () => {
    token.value = ''
    user.value = null
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    logout,
  }
})
