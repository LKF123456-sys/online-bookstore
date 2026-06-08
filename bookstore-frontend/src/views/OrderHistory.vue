<template>
  <div class="container">
    <h1>我的订单</h1>

    <!-- ===== 订单状态筛选标签页 ===== -->
    <div class="tabs mt-2">
      <!-- v-for="s in statuses"：遍历状态数组 ['', '待支付', '已支付', '已发货', '已完成', '已取消'] -->
      <!-- :class="{ active: activeTab === s }"：当前选中标签添加 active 样式 -->
      <!-- @click="activeTab = s; fetch()"：点击切换标签并重新请求对应状态的订单 -->
      <button v-for="s in statuses" :key="s" :class="{ active: activeTab === s }" @click="activeTab = s; fetch()">
        {{ s || '全部' }}
      </button>
    </div>

    <!-- ===== 加载状态 ===== -->
    <!-- v-if="loading"：请求中显示旋转 loading 动画 -->
    <div v-if="loading" class="loading-spinner"></div>

    <!-- ===== 空状态：无订单记录 ===== -->
    <!-- v-else-if="orders.length === 0"：加载完成但无订单时显示终端风格提示 -->
    <div v-else-if="orders.length === 0" class="text-center mt-3" style="padding:60px 0">
      <p class="text-dim" style="font-size:20px;">&gt; NO_ORDERS</p>
      <p class="text-dim">暂无订单</p>
    </div>

    <!-- ===== 订单列表 ===== -->
    <!-- v-else：有订单数据时展示订单卡片列表 -->
    <div v-else>
      <!-- v-for="o in orders"：遍历订单数组 -->
      <div v-for="o in orders" :key="o.orderid" class="order-card card mb-2">
        <!-- 订单头部：订单号 + 状态徽章 + 金额 -->
        <div class="order-header">
          <!-- 订单号：等宽字体灰色 -->
          <span class="order-no">#{{ o.orderid }}</span>
          <!-- 状态徽章：动态 class 由 statusClass 函数根据状态返回对应颜色 -->
          <span :class="'badge badge-' + statusClass(o.status)">{{ o.status || '待支付' }}</span>
          <!-- 订单金额：粗体大号 -->
          <span class="order-price">&#165;{{ (o.totalprice || 0).toFixed(2) }}</span>
        </div>

        <!-- 订单操作按钮 -->
        <div class="order-actions">
          <!-- router-link：跳转到订单详情页 /order/{orderId} -->
          <router-link :to="`/order/${o.orderid}`" class="btn btn-outline btn-sm">详情</router-link>
          <!-- v-if="o.status === '待支付'"：仅"待支付"订单显示"去支付"按钮 -->
          <!-- @click="goPay(o.orderid)"：跳转到结算/支付页 -->
          <button v-if="o.status === '待支付'" class="btn btn-primary btn-sm" @click="goPay(o.orderid)">去支付</button>
          <!-- v-if="o.status === '待支付'"：仅"待支付"订单显示"取消"按钮 -->
          <!-- @click="cancelOrder(o.orderid)"：取消该订单（带确认弹窗） -->
          <button v-if="o.status === '待支付'" class="btn btn-outline btn-sm" @click="cancelOrder(o.orderid)">取消</button>
          <!-- v-if="o.status === '已发货'"：仅"已发货"订单显示绿色"确认收货"按钮 -->
          <!-- @click="confirmReceipt(o.orderid)"：确认收货（带确认弹窗） -->
          <button v-if="o.status === '已发货'" class="btn btn-sm" style="background:linear-gradient(135deg,#00ff88,#00c853);color:#000;" @click="confirmReceipt(o.orderid)">确认收货</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api/client'

// router：Vue Router 实例，用于跳转到支付页面
const router = useRouter()
// auth：Pinia 认证状态管理 store，用于检查登录状态
const auth = useAuthStore()
// orders：订单列表数组
const orders = ref<any[]>([])
// loading：页面加载状态
const loading = ref(true)
// activeTab：当前选中的订单状态标签，空字符串表示"全部"
const activeTab = ref('')
// statuses：订单状态选项数组，空字符串对应"全部"选项
const statuses = ['', '待支付', '已支付', '已发货', '已完成', '已取消']

// statusClassMap：订单状态到 CSS 类名的映射表
const statusClassMap: Record<string, string> = { '待支付': 'pending', '已支付': 'paid', '已发货': 'shipped', '已完成': 'done', '已取消': 'cancel' }
/**
 * 根据订单状态字符串返回对应的 CSS 类名，默认返回 'pending'
 * 用于模板中动态绑定订单状态徽章颜色
 */
function statusClass(s: string) { return statusClassMap[s] || 'pending' }

/**
 * 获取订单列表
 * 调用 GET /orders 接口，传递分页参数和状态筛选
 * try 成功分支：
 *   - 从返回数据中提取 records 赋值给 orders
 * catch 失败分支：
 *   - 清空 orders 数组
 * finally：
 *   - 将 loading 设为 false
 */
async function fetch() {
  loading.value = true
  try {
    // 调用订单列表 API：GET /orders，传递分页和状态筛选参数
    const res = await api.get('/orders', { params: { pageNum: 1, pageSize: 50, status: activeTab.value || undefined } })
    orders.value = (res.data || {}).records || []
  } catch (e) { orders.value = [] }
  finally { loading.value = false }
}

/**
 * 去支付：跳转到结算/支付页，携带订单 ID 查询参数
 */
function goPay(orderId: string) { router.push(`/checkout?orderId=${orderId}`) }

/**
 * 取消订单
 * 先弹出 confirm 确认框，确认后调用 POST /orders/{orderId}/cancel 接口
 * try 成功分支：
 *   - 调用 fetch() 刷新订单列表
 * catch 失败分支：
 *   - 弹出错误信息提示
 */
async function cancelOrder(orderId: string) {
  if (!confirm('确定取消订单？')) return
  try { await api.post(`/orders/${orderId}/cancel`); fetch() } catch (e: any) { alert(e.message) }
}

/**
 * 确认收货
 * 先弹出 confirm 确认框，确认后调用 POST /orders/{orderId}/confirm 接口
 * try 成功分支：
 *   - 调用 fetch() 刷新订单列表
 * catch 失败分支：
 *   - 弹出错误信息提示
 */
async function confirmReceipt(orderId: string) {
  if (!confirm('确认已收到商品？')) return
  try { await api.post(`/orders/${orderId}/confirm`); fetch() } catch (e: any) { alert(e.message) }
}

/**
 * onMounted 生命周期钩子：组件挂载时
 * - 已登录用户：调用 fetch() 请求订单列表
 * - 未登录用户：直接结束 loading
 */
onMounted(() => { if (auth.isLoggedIn) fetch(); else loading.value = false })
</script>

<style scoped>
/* 标签页容器：flex 水平布局，间距 8px，支持换行 */
.tabs { display: flex; gap: 8px; flex-wrap: wrap; }
/* 标签按钮：透明背景 + 边框 + 圆角（胶囊形状），等宽字体 */
.tabs button {
  padding: 6px 18px; background: transparent; border: 1px solid var(--border);
  border-radius: 20px; color: var(--text-secondary); cursor: pointer;
  font-size: 13px; transition: all .25s; font-family: var(--font-mono);
}
/* 选中状态：青色半透明背景 + 青色文字 + 青色边框 */
.tabs button.active {
  background: rgba(0,240,255,0.1); color: var(--primary);
  border-color: rgba(0,240,255,0.3);
}
/* 非选中状态 hover：边框变为灰色 */
.tabs button:hover:not(.active) { border-color: var(--text-dim); }
/* 订单头部：flex 水平布局，两端对齐，支持换行 */
.order-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
/* 订单号：等宽灰色小字 */
.order-no { font-family: var(--font-mono); font-size: 13px; color: var(--text-dim); }
/* 订单金额：粗体大号 */
.order-price { font-weight: 700; font-size: 16px; }
/* 操作按钮区域：flex 水平布局，间距 8px */
.order-actions { display: flex; gap: 8px; margin-top: 14px; }
/* 次要文字 */
.text-dim { color: var(--text-dim); }
</style>
