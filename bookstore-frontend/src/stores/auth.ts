import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVO } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<UserVO | null>(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => user.value?.id ?? 0)
  const username = computed(() => user.value?.username ?? '')

  function setAuth(newToken: string, newUser: UserVO) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem('token', newToken)
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  function setUser(newUser: UserVO) {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    token,
    user,
    isLoggedIn,
    userId,
    username,
    setAuth,
    setUser,
    logout,
  }
})
