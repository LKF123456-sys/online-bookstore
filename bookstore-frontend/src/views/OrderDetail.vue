<template>
  <!-- 订单详情页容器：最大宽度 700px，居中展示 -->
  <div class="container" style="max-width:700px">
    <h1>订单详情</h1>

    <!-- ===== 加载状态 ===== -->
    <!-- v-if="loading"：请求中显示旋转 loading 动画 -->
    <div v-if="loading" class="loading-spinner"></div>

    <!-- ===== 订单不存在状态 ===== -->
    <!-- v-else-if="!order"：加载完成但订单为 null 时显示 -->
    <div v-else-if="!order" class="text-center mt-3"><p class="text-dim">订单不存在</p></div>

    <!-- ===== 订单详情内容 ===== -->
    <!-- v-else：订单数据存在时展示详情 -->
    <div v-else class="card mt-2">
      <!-- 订单元数据行：订单号 + 状态徽章 + 创建时间 -->
      <div class="order-meta">
        <span class="text-dim">订单号: <strong style="color:var(--text);">#{{ order.orderid }}</strong></span>
        <!-- 状态徽章：动态 class 由 statusClass 函数返回 -->
        <span :class="'badge badge-' + statusClass(order.status)">{{ order.status || '待支付' }}</span>
        <span class="text-dim" style="font-size:12px;">{{ order.createtime || order.createTime }}</span>
      </div>

      <!-- 订单商品列表 -->
      <div class="order-items mt-2">
        <!-- v-for="item in (order.items || order.orderItems || [])"：遍历订单商品子项，兼容多种字段名 -->
        <div v-for="item in (order.items || order.orderItems || [])" :key="item.id" class="order-item">
          <span>{{ item.productName || item.title }}</span>
          <span class="text-dim">x{{ item.quantity }}</span>
          <span>&#165;{{ (item.price || 0).toFixed(2) }}</span>
        </div>
      </div>

      <!-- 总计金额 -->
      <div class="order-total mt-2">
        <strong>总计: &#165;{{ (order.totalprice || 0).toFixed(2) }}</strong>
      </div>

      <!-- 操作按钮：根据订单状态显示不同的操作 -->
      <div class="order-actions mt-2">
        <!-- v-if="order.status === '待支付'"：待支付订单显示"去支付"按钮 -->
        <!-- @click="goPay"：跳转到支付页 -->
        <button v-if="order.status === '待支付'" class="btn btn-primary" @click="goPay">去支付</button>
        <!-- v-if="order.status === '待支付'"：待支付订单显示"取消订单"按钮 -->
        <!-- @click="cancelOrder"：取消订单（带确认弹窗） -->
        <button v-if="order.status === '待支付'" class="btn btn-outline btn-sm" @click="cancelOrder">取消订单</button>
        <!-- v-if="order.status === '已发货'"：已发货订单显示绿色"确认收货"按钮 -->
        <!-- @click="confirmReceipt"：确认收货（带确认弹窗） -->
        <button v-if="order.status === '已发货'" class="btn btn-sm" style="background:linear-gradient(135deg,#00ff88,#00c853);color:#000;" @click="confirmReceipt">确认收货</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/client'

// route：当前路由对象，用于从 URL 路径参数中读取订单 ID（route.params.id）
const route = useRoute()
// router：Vue Router 实例，用于跳转到支付页
const router = useRouter()
// order：订单详情对象
const order = ref<any>(null)
// loading：页面加载状态
const loading = ref(true)

// statusClassMap：订单状态到 CSS 类名的映射表
const statusClassMap: Record<string, string> = { '待支付': 'pending', '已支付': 'paid', '已发货': 'shipped', '已完成': 'done', '已取消': 'cancel' }
/**
 * 根据订单状态字符串返回对应的 CSS 类名，默认返回 'pending'
 * 用于模板中动态绑定订单状态徽章的样式
 */
function statusClass(s: string) { return statusClassMap[s] || 'pending' }

/**
 * 获取订单详情
 * 调用 GET /orders/{id} 接口，从路由参数中读取订单 ID
 * try 成功分支：
 *   - 将返回的订单数据赋值给 order
 * catch 失败分支：
 *   - 静默处理，order 保持 null
 * finally：
 *   - 将 loading 设为 false
 */
async function fetch() {
  try {
    // 调用订单详情 API：GET /orders/{id}
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data
  } catch (e) { /* 请求失败静默处理 */ }
  finally { loading.value = false }
}

/**
 * 去支付：跳转到结算/支付页，携带订单 ID 查询参数
 */
function goPay() { router.push(`/checkout?orderId=${order.value.orderid}`) }

/**
 * 取消订单
 * 先弹出 confirm 确认框，确认后调用 POST /orders/{orderId}/cancel 接口
 * try 成功分支：
 *   - 调用 fetch() 重新加载订单数据
 * catch 失败分支：
 *   - 弹出错误信息提示
 */
async function cancelOrder() {
  if (!confirm('确定取消订单？')) return
  try { await api.post(`/orders/${order.value.orderid}/cancel`); fetch() } catch (e: any) { alert(e.message) }
}

/**
 * 确认收货
 * 先弹出 confirm 确认框，确认后调用 POST /orders/{orderId}/confirm 接口
 * try 成功分支：
 *   - 调用 fetch() 重新加载订单数据
 * catch 失败分支：
 *   - 弹出错误信息提示
 */
async function confirmReceipt() {
  if (!confirm('确认已收到商品？')) return
  try { await api.post(`/orders/${order.value.orderid}/confirm`); fetch() } catch (e: any) { alert(e.message) }
}

onMounted(fetch)
</script>

<style scoped>
/* 订单元数据行：flex 水平布局，间距 14px，支持换行 */
.order-meta { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
/* 订单商品列表：上下有分割线，padding 14px */
.order-items { border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); padding: 14px 0; }
/* 订单商品单项：flex 三栏布局（名称 | 数量 | 价格），两端对齐 */
.order-item { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }
/* 总计金额：右对齐，大号字体 */
.order-total { text-align: right; font-size: 18px; }
/* 操作按钮区域：flex 水平布局，右对齐，间距 10px */
.order-actions { display: flex; gap: 10px; justify-content: flex-end; }
/* 次要文字 */
.text-dim { color: var(--text-dim); font-size: 13px; }
</style>
