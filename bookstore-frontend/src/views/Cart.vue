<template>
  <div class="container">
    <h1>购物车</h1>
    <div v-if="loading" class="loading-spinner"></div>
    <div v-else-if="!auth.isLoggedIn" class="text-center mt-3" style="padding:60px 0">
      <p class="text-dim">请先登录后查看购物车</p>
      <router-link to="/login" class="btn btn-primary mt-2">去登录</router-link>
    </div>
    <div v-else-if="cartItems.length === 0" class="text-center mt-3" style="padding:60px 0">
      <p class="text-dim" style="font-size:20px;">&gt; CART_EMPTY</p>
      <p class="text-dim">购物车是空的</p>
      <router-link to="/products" class="btn btn-primary mt-2">去选购</router-link>
    </div>
    <div v-else>
      <div class="cart-list mt-2">
        <div v-for="item in cartItems" :key="item.id || item.cartId" class="cart-item card">
          <div class="item-info">
            <strong>{{ item.productName || item.title }}</strong>
            <span class="text-dim">&#165;{{ (item.price || 0).toFixed(2) }} x {{ item.quantity }}</span>
          </div>
          <div class="item-total">
            <strong style="font-size:18px;">&#165;{{ ((item.price || 0) * (item.quantity || 1)).toFixed(2) }}</strong>
          </div>
        </div>
      </div>
      <div class="cart-footer mt-3">
        <div class="total-label">
          <span class="text-dim">合计</span>
          <strong style="font-size:24px;">&#165;{{ total.toFixed(2) }}</strong>
        </div>
        <router-link to="/checkout" class="btn btn-primary">去结算</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api/client'

const auth = useAuthStore()
const cartItems = ref<any[]>([])
const loading = ref(true)
const total = computed(() => cartItems.value.reduce((s, i) => s + (i.price || 0) * (i.quantity || 1), 0))

onMounted(async () => {
  if (!auth.isLoggedIn) { loading.value = false; return }
  try {
    const res = await api.get('/orders/cart')
    const data = res.data || {}
    cartItems.value = data.items || data.records || data.cartItems || []
  } catch (e) { /* empty */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.cart-item { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.item-info { display: flex; flex-direction: column; gap: 4px; }
.text-dim { color: var(--text-dim); font-size: 13px; }
.item-total { font-family: var(--font-mono); }
.cart-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 20px; border-top: 1px solid var(--border);
}
.total-label { display: flex; flex-direction: column; gap: 4px; }
</style>
