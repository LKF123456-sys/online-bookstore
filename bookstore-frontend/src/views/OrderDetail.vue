<template>
  <div class="container" style="max-width:700px">
    <h1>订单详情</h1>
    <div v-if="loading" class="loading-spinner"></div>
    <div v-else-if="!order" class="text-center mt-3"><p class="text-dim">订单不存在</p></div>
    <div v-else class="card mt-2">
      <div class="order-meta">
        <span class="text-dim">订单号: <strong style="color:var(--text);">#{{ order.orderid }}</strong></span>
        <span :class="'badge badge-' + statusClass(order.status)">{{ order.status || '待支付' }}</span>
        <span class="text-dim" style="font-size:12px;">{{ order.createtime || order.createTime }}</span>
      </div>
      <div class="order-items mt-2">
        <div v-for="item in (order.items || order.orderItems || [])" :key="item.id" class="order-item">
          <span>{{ item.productName || item.title }}</span>
          <span class="text-dim">x{{ item.quantity }}</span>
          <span>&#165;{{ (item.price || 0).toFixed(2) }}</span>
        </div>
      </div>
      <div class="order-total mt-2">
        <strong>总计: &#165;{{ (order.totalprice || 0).toFixed(2) }}</strong>
      </div>
      <div class="order-actions mt-2">
        <button v-if="order.status === '待支付'" class="btn btn-primary" @click="goPay">去支付</button>
        <button v-if="order.status === '待支付'" class="btn btn-outline btn-sm" @click="cancelOrder">取消订单</button>
        <button v-if="order.status === '已发货'" class="btn btn-sm" style="background:linear-gradient(135deg,#00ff88,#00c853);color:#000;" @click="confirmReceipt">确认收货</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/client'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const loading = ref(true)

const statusClassMap: Record<string, string> = { '待支付': 'pending', '已支付': 'paid', '已发货': 'shipped', '已完成': 'done', '已取消': 'cancel' }
function statusClass(s: string) { return statusClassMap[s] || 'pending' }

async function fetch() {
  try {
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data
  } catch (e) { /* empty */ }
  finally { loading.value = false }
}

function goPay() { router.push(`/checkout?orderId=${order.value.orderid}`) }

async function cancelOrder() {
  if (!confirm('确定取消订单？')) return
  try { await api.post(`/orders/${order.value.orderid}/cancel`); fetch() } catch (e: any) { alert(e.message) }
}

async function confirmReceipt() {
  if (!confirm('确认已收到商品？')) return
  try { await api.post(`/orders/${order.value.orderid}/confirm`); fetch() } catch (e: any) { alert(e.message) }
}

onMounted(fetch)
</script>

<style scoped>
.order-meta { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.order-items { border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); padding: 14px 0; }
.order-item { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }
.order-total { text-align: right; font-size: 18px; }
.order-actions { display: flex; gap: 10px; justify-content: flex-end; }
.text-dim { color: var(--text-dim); font-size: 13px; }
</style>
