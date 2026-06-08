<template>
  <div class="container">
    <h1>购物车</h1>

    <!-- ===== 加载状态 ===== -->
    <!-- v-if="loading"：请求中显示旋转 loading 动画 -->
    <div v-if="loading" class="loading-spinner"></div>

    <!-- ===== 未登录状态：提示用户先登录 ===== -->
    <!-- v-else-if="!auth.isLoggedIn"：用户未登录时显示登录引导 -->
    <div v-else-if="!auth.isLoggedIn" class="text-center mt-3" style="padding:60px 0">
      <p class="text-dim">请先登录后查看购物车</p>
      <!-- router-link to="/login"：跳转到登录页 -->
      <router-link to="/login" class="btn btn-primary mt-2">去登录</router-link>
    </div>

    <!-- ===== 购物车为空状态 ===== -->
    <!-- v-else-if="cartItems.length === 0"：已登录但购物车无商品时显示 -->
    <div v-else-if="cartItems.length === 0" class="text-center mt-3" style="padding:60px 0">
      <p class="text-dim" style="font-size:20px;">&gt; CART_EMPTY</p>
      <p class="text-dim">购物车是空的</p>
      <!-- router-link to="/products"：跳转到产品列表页选购商品 -->
      <router-link to="/products" class="btn btn-primary mt-2">去选购</router-link>
    </div>

    <!-- ===== 购物车列表 ===== -->
    <!-- v-else：有商品时展示购物车列表 -->
    <div v-else>
      <div class="cart-list mt-2">
        <!-- v-for="item in cartItems"：遍历购物车商品数组 -->
        <div v-for="item in cartItems" :key="item.id || item.cartId" class="cart-item card">
          <!-- 商品信息：名称 + 单价 × 数量 -->
          <div class="item-info">
            <strong>{{ item.productName || item.title }}</strong>
            <span class="text-dim">&#165;{{ (item.price || 0).toFixed(2) }} x {{ item.quantity }}</span>
          </div>
          <!-- 小计：单价 × 数量 -->
          <div class="item-total">
            <strong style="font-size:18px;">&#165;{{ ((item.price || 0) * (item.quantity || 1)).toFixed(2) }}</strong>
          </div>
        </div>
      </div>

      <!-- ===== 购物车底部：合计 + 结算按钮 ===== -->
      <div class="cart-footer mt-3">
        <div class="total-label">
          <span class="text-dim">合计</span>
          <!-- total 计算属性：所有商品价格 × 数量的总和 -->
          <strong style="font-size:24px;">&#165;{{ total.toFixed(2) }}</strong>
        </div>
        <!-- router-link to="/checkout"：跳转到确认订单/结算页面 -->
        <router-link to="/checkout" class="btn btn-primary">去结算</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api/client'

// auth：Pinia 认证状态管理 store，用于检查登录状态
const auth = useAuthStore()
// cartItems：购物车商品数组
const cartItems = ref<any[]>([])
// loading：页面加载状态
const loading = ref(true)

/**
 * total 计算属性：计算购物车所有商品的总价
 * 使用 reduce 遍历 cartItems，累加 price × quantity
 */
const total = computed(() => cartItems.value.reduce((s, i) => s + (i.price || 0) * (i.quantity || 1), 0))

/**
 * onMounted 生命周期钩子：组件挂载后请求购物车数据
 * - 先检查登录状态，未登录直接结束 loading
 * - 调用 GET /orders/cart 接口获取购物车数据
 * - 兼容多种后端返回格式：items / records / cartItems
 * try 成功分支：
 *   - 提取购物车商品列表赋值给 cartItems
 * catch 失败分支：
 *   - 静默处理，cartItems 保持空数组
 * finally：
 *   - 将 loading 设为 false
 */
onMounted(async () => {
  if (!auth.isLoggedIn) { loading.value = false; return }
  try {
    // 调用购物车 API：GET /orders/cart
    const res = await api.get('/orders/cart')
    const data = res.data || {}
    // 兼容多种返回字段名
    cartItems.value = data.items || data.records || data.cartItems || []
  } catch (e) { /* 请求失败静默处理 */ }
  finally { loading.value = false }
})
</script>

<style scoped>
/* 购物车单项：flex 水平布局，两端对齐，居中对齐，底部间距 10px */
.cart-item { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
/* 商品信息：纵向 flex 布局，间距 4px */
.item-info { display: flex; flex-direction: column; gap: 4px; }
/* 次要文字：等宽灰色小字 */
.text-dim { color: var(--text-dim); font-size: 13px; }
/* 小计金额：等宽字体 */
.item-total { font-family: var(--font-mono); }
/* 购物车底部：flex 水平布局，两端对齐，顶部有分割线 */
.cart-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 20px; border-top: 1px solid var(--border);
}
/* 合计标签：纵向 flex 布局 */
.total-label { display: flex; flex-direction: column; gap: 4px; }
</style>
