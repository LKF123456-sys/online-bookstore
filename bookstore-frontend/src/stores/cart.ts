import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CartItem } from '@/types'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])
  const loading = ref(false)

  const itemCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return items.value.reduce((sum, item) => {
      if (item.subtotal != null) {
        return sum + Number(item.subtotal)
      }
      return sum + (item.price || 0) * item.quantity
    }, 0)
  })

  function setItems(newItems: CartItem[]) {
    items.value = newItems
  }

  function updateItem(productId: number | string, quantity: number) {
    const idx = items.value.findIndex(item => (item.productid || item.productId) == productId)
    if (idx >= 0) {
      items.value[idx].quantity = quantity
    }
  }

  function removeItem(productId: number | string) {
    items.value = items.value.filter(item => (item.productid || item.productId) != productId)
  }

  function clearItems() {
    items.value = []
  }

  return {
    items,
    loading,
    itemCount,
    totalPrice,
    setItems,
    updateItem,
    removeItem,
    clearItems,
  }
})
