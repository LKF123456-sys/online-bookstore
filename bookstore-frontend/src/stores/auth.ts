import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVO } from '@/types'
import { logoutApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserVO | null>(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!user.value)
  const userId = computed(() => user.value?.id ?? 0)
  const username = computed(() => user.value?.username ?? '')

  function setAuth(_token: string, newUser: UserVO) {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  function setUser(newUser: UserVO) {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  function logout() {
    logoutApi().catch(() => {})
    user.value = null
    localStorage.removeItem('user')
  }

  return {
    user,
    isLoggedIn,
    userId,
    username,
    setAuth,
    setUser,
    logout,
  }
})