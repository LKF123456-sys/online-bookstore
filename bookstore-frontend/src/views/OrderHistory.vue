<template>
  <div class="container">
    <h1>我的订单</h1>
    <div class="tabs mt-2">
      <button v-for="s in statuses" :key="s" :class="{ active: activeTab === s }" @click="activeTab = s; fetch()">
        {{ s || '全部' }}
      </button>
    </div>
    <div v-if="loading" class="loading-spinner"></div>
    <div v-else-if="orders.length === 0" class="text-center mt-3" style="padding:60px 0">
      <p class="text-dim" style="font-size:20px;">&gt; NO_ORDERS</p>
      <p class="text-dim">暂无订单</p>
    </div>
    <div v-else>
      <div v-for="o in orders" :key="o.orderid" class="order-card card mb-2">
        <div class="order-header">
          <span class="order-no">#{{ o.orderid }}</span>
          <span :class="'badge badge-' + statusClass(o.status)">{{ o.status || '待支付' }}</span>
          <span class="order-price">&#165;{{ (o.totalprice || 0).toFixed(2) }}</span>
        </div>
        <div class="order-actions">
          <router-link :to="`/order/${o.orderid}`" class="btn btn-outline btn-sm">详情</router-link>
          <button v-if="o.status === '待支付'" class="btn btn-primary btn-sm" @click="goPay(o.orderid)">去支付</button>
          <button v-if="o.status === '待支付'" class="btn btn-outline btn-sm" @click="cancelOrder(o.orderid)">取消</button>
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

const router = useRouter()
const auth = useAuthStore()
const orders = ref<any[]>([])
const loading = ref(true)
const activeTab = ref('')
const statuses = ['', '待支付', '已支付', '已发货', '已完成', '已取消']

const statusClassMap: Record<string, string> = { '待支付': 'pending', '已支付': 'paid', '已发货': 'shipped', '已完成': 'done', '已取消': 'cancel' }
function statusClass(s: string) { return statusClassMap[s] || 'pending' }

async function fetch() {
  loading.value = true
  try {
    const res = await api.get('/orders', { params: { pageNum: 1, pageSize: 50, status: activeTab.value || undefined } })
    orders.value = (res.data || {}).records || []
  } catch (e) { orders.value = [] }
  finally { loading.value = false }
}

function goPay(orderId: string) { router.push(`/checkout?orderId=${orderId}`) }

async function cancelOrder(orderId: string) {
  if (!confirm('确定取消订单？')) return
  try { await api.post(`/orders/${orderId}/cancel`); fetch() } catch (e: any) { alert(e.message) }
}

async function confirmReceipt(orderId: string) {
  if (!confirm('确认已收到商品？')) return
  try { await api.post(`/orders/${orderId}/confirm`); fetch() } catch (e: any) { alert(e.message) }
}

onMounted(() => { if (auth.isLoggedIn) fetch(); else loading.value = false })
</script>

<style scoped>
.tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.tabs button {
  padding: 6px 18px; background: transparent; border: 1px solid var(--border);
  border-radius: 20px; color: var(--text-secondary); cursor: pointer;
  font-size: 13px; transition: all .25s; font-family: var(--font-mono);
}
.tabs button.active {
  background: rgba(0,240,255,0.1); color: var(--primary);
  border-color: rgba(0,240,255,0.3);
}
.tabs button:hover:not(.active) { border-color: var(--text-dim); }
.order-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.order-no { font-family: var(--font-mono); font-size: 13px; color: var(--text-dim); }
.order-price { font-weight: 700; font-size: 16px; }
.order-actions { display: flex; gap: 8px; margin-top: 14px; }
.text-dim { color: var(--text-dim); }
</style>
