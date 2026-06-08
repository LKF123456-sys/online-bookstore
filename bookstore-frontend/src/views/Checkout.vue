<template>
  <div class="container" style="max-width:600px">
    <h1>确认订单</h1>
    <div v-if="loading" class="loading-spinner"></div>
    <div v-else-if="!order" class="text-center mt-3"><p class="text-dim">订单不存在</p></div>
    <div v-else class="card mt-2">
      <div class="order-summary">
        <span class="text-dim">订单号: <strong style="color:var(--text);">#{{ order.orderid }}</strong></span>
        <span :class="'badge badge-' + statusClass(order.status)">{{ order.status }}</span>
      </div>
      <div class="total-block mt-2">
        <span class="text-dim">应付金额</span>
        <strong class="total-price">&#165;{{ (order.totalprice || 0).toFixed(2) }}</strong>
      </div>
      <div class="pay-section mt-3">
        <button class="btn btn-primary" @click="handlePay" :disabled="paying" style="width:100%;padding:14px;">
          {{ paying ? '支付中...' : '立即支付 &#165;' + (order.totalprice || 0).toFixed(2) }}
        </button>
        <p v-if="payError" class="error mt-2">{{ payError }}</p>
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
const paying = ref(false)
const payError = ref('')

const statusClassMap: Record<string, string> = { '待支付': 'pending', '已支付': 'paid', '已发货': 'shipped', '已完成': 'done', '已取消': 'cancel' }
function statusClass(s: string) { return statusClassMap[s] || 'pending' }

async function fetch() {
  try {
    const orderId = route.query.orderId
    if (!orderId) { loading.value = false; return }
    const res = await api.get(`/orders/${orderId}`)
    order.value = res.data
  } catch (e) { /* empty */ }
  finally { loading.value = false }
}

async function handlePay() {
  paying.value = true; payError.value = ''
  try {
    await api.post(`/orders/${order.value.orderid}/pay`)
    router.push(`/order/${order.value.orderid}`)
  } catch (e: any) { payError.value = e.message }
  finally { paying.value = false }
}

onMounted(fetch)
</script>

<style scoped>
.order-summary { display: flex; align-items: center; gap: 14px; }
.total-block { text-align: center; padding: 24px 0; border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); }
.total-price {
  display: block; font-size: 36px; margin-top: 4px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
.pay-section { text-align: center; }
.text-dim { color: var(--text-dim); font-size: 13px; }
.error { color: var(--danger); font-size: 13px; }
</style>
