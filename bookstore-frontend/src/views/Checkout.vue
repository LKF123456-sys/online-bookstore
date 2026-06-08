<template>
  <!-- 确认订单页容器：最大宽度 600px，居中展示 -->
  <div class="container" style="max-width:600px">
    <h1>确认订单</h1>

    <!-- ===== 加载状态 ===== -->
    <!-- v-if="loading"：请求中显示旋转 loading 动画 -->
    <div v-if="loading" class="loading-spinner"></div>

    <!-- ===== 订单不存在状态 ===== -->
    <!-- v-else-if="!order"：加载完成但订单为 null 时显示提示 -->
    <div v-else-if="!order" class="text-center mt-3"><p class="text-dim">订单不存在</p></div>

    <!-- ===== 订单确认内容 ===== -->
    <!-- v-else：订单数据存在时显示订单摘要和支付按钮 -->
    <div v-else class="card mt-2">
      <!-- 订单摘要行：订单号 + 状态徽章 -->
      <div class="order-summary">
        <span class="text-dim">订单号: <strong style="color:var(--text);">#{{ order.orderid }}</strong></span>
        <!-- 状态徽章：动态 class 根据 statusClass 函数返回的 CSS 类名控制颜色 -->
        <span :class="'badge badge-' + statusClass(order.status)">{{ order.status }}</span>
      </div>

      <!-- 应付金额区域：居中展示大号金额 -->
      <div class="total-block mt-2">
        <span class="text-dim">应付金额</span>
        <!-- 大号渐变文字（青→紫）显示应付金额 -->
        <strong class="total-price">&#165;{{ (order.totalprice || 0).toFixed(2) }}</strong>
      </div>

      <!-- 支付区域：支付按钮 + 错误提示 -->
      <div class="pay-section mt-3">
        <!-- @click="handlePay"：点击触发支付 -->
        <!-- :disabled="paying"：支付进行中时禁用按钮，防止重复支付 -->
        <button class="btn btn-primary" @click="handlePay" :disabled="paying" style="width:100%;padding:14px;">
          {{ paying ? '支付中...' : '立即支付 &#165;' + (order.totalprice || 0).toFixed(2) }}
        </button>
        <!-- v-if="payError"：仅当支付出错时显示红色错误提示 -->
        <p v-if="payError" class="error mt-2">{{ payError }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/client'

// route：当前路由对象，用于读取 URL 查询参数 orderId
const route = useRoute()
// router：Vue Router 实例，支付成功后跳转到订单详情页
const router = useRouter()
// order：订单详情对象，存储 /orders/{orderId} 接口返回的订单数据
const order = ref<any>(null)
// loading：页面加载状态
const loading = ref(true)
// paying：支付进行中标志，true 时按钮禁用并显示"支付中..."
const paying = ref(false)
// payError：支付错误信息，非空时显示红色提示
const payError = ref('')

// statusClassMap：订单状态到 CSS 类名的映射表
const statusClassMap: Record<string, string> = { '待支付': 'pending', '已支付': 'paid', '已发货': 'shipped', '已完成': 'done', '已取消': 'cancel' }
/**
 * 根据订单状态字符串返回对应的 CSS 类名，默认返回 'pending'
 * 用于模板中动态绑定订单状态徽章的样式
 */
function statusClass(s: string) { return statusClassMap[s] || 'pending' }

/**
 * 获取订单详情
 * 从 URL 查询参数中读取 orderId，调用 GET /orders/{orderId} 接口
 * try 成功分支：
 *   - 将返回的订单数据赋值给 order
 * catch 失败分支：
 *   - 静默处理，order 保持 null
 * finally：
 *   - 将 loading 设为 false
 */
async function fetch() {
  try {
    // 从 URL 查询参数中获取订单 ID
    const orderId = route.query.orderId
    if (!orderId) { loading.value = false; return }
    // 调用订单详情 API：GET /orders/{orderId}
    const res = await api.get(`/orders/${orderId}`)
    order.value = res.data
  } catch (e) { /* 请求失败静默处理 */ }
  finally { loading.value = false }
}

/**
 * 处理支付
 * 调用 POST /orders/{orderId}/pay 接口触发支付
 * try 成功分支：
 *   - 支付成功后跳转到订单详情页 /order/{orderId}
 * catch 失败分支：
 *   - 将错误信息赋值给 payError，页面显示红色提示
 * finally：
 *   - 将 paying 设为 false，恢复按钮可用
 */
async function handlePay() {
  paying.value = true; payError.value = ''
  try {
    // 调用支付 API：POST /orders/{orderId}/pay
    await api.post(`/orders/${order.value.orderid}/pay`)
    // 支付成功：跳转到订单详情页
    router.push(`/order/${order.value.orderid}`)
  } catch (e: any) { payError.value = e.message }
  finally { paying.value = false }
}

onMounted(fetch)
</script>

<style scoped>
/* 订单摘要行：flex 水平布局，间距 14px */
.order-summary { display: flex; align-items: center; gap: 14px; }
/* 金额区域：居中，上下有分割线，padding 24px */
.total-block { text-align: center; padding: 24px 0; border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); }
/* 应付金额：大号渐变文字（青→紫），块级显示 */
.total-price {
  display: block; font-size: 36px; margin-top: 4px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
/* 支付区域：居中 */
.pay-section { text-align: center; }
/* 次要文字 */
.text-dim { color: var(--text-dim); font-size: 13px; }
/* 错误提示：红色文字 */
.error { color: var(--danger); font-size: 13px; }
</style>
