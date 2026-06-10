import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    try {
      const count = await getUnreadCount()
      unreadCount.value = count
    } catch {
      // Silently fail
    }
  }

  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  function decrementUnread() {
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  }

  function clearUnread() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    fetchUnreadCount,
    setUnreadCount,
    decrementUnread,
    clearUnread,
  }
})
