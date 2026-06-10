<template>
  <DefaultLayout>
    <div class="page-container">
      <h1 class="section-title">我的订单</h1>

      <n-card>
        <n-tabs v-model:value="activeTab" type="line" @update:value="handleTabChange">
          <n-tab-pane name="" tab="全部" />
          <n-tab-pane name="待支付" tab="待支付" />
          <n-tab-pane name="已支付" tab="已支付" />
          <n-tab-pane name="已发货" tab="已发货" />
          <n-tab-pane name="已完成" tab="已完成" />
          <n-tab-pane name="已取消" tab="已取消" />
        </n-tabs>

        <n-spin :show="loading">
          <div v-if="orders.length > 0" class="orders-list">
            <n-card
              v-for="order in orders"
              :key="order.orderid || order.id"
              class="order-card"
              size="small"
              @click="$router.push(`/order/${order.orderid || order.id}`)"
            >
              <div class="order-header">
                <n-space align="center" :size="16">
                  <span class="order-no">订单号：{{ order.orderid || order.orderno || order.orderNo }}</span>
                  <n-tag :type="getStatusType(order.status)" size="small">
                    {{ getStatusText(order.status) }}
                  </n-tag>
                  <span class="order-date">{{ formatDate(order.orderdate || order.createdAt || order.created_at || '') }}</span>
                </n-space>
                <span class="order-total">${{ Number(order.totalprice || order.payamount || order.payAmount || order.totalamount || order.totalAmount || 0).toFixed(2) }}</span>
              </div>

              <div class="order-items-preview">
                <div v-for="item in order.items?.slice(0, 3)" :key="item.id" class="order-item-row">
                  <span class="item-title">{{ item.productName || item.name || item.title || '商品' }}</span>
                  <span class="item-qty">x{{ item.quantity }}</span>
                </div>
                <span v-if="(order.items?.length || 0) > 3" class="more-items">
                  +{{ order.items!.length - 3 }} 件更多商品
                </span>
              </div>

              <div class="order-actions">
                <n-space :size="8">
                  <n-button v-if="order.status === '待支付'" type="primary" size="small" @click.stop="handlePay(order.orderid || order.id as any)">
                    立即付款
                  </n-button>
                  <n-button v-if="order.status === '待支付'" size="small" @click.stop="handleCancel(order.orderid || order.id as any)">
                    取消订单
                  </n-button>
                  <n-button v-if="order.status === '已发货'" type="success" size="small" @click.stop="handleConfirm(order.orderid || order.id as any)">
                    确认收货
                  </n-button>
                  <n-button size="small" quaternary @click.stop="$router.push(`/order/${order.orderid || order.id}`)">
                    查看详情
                  </n-button>
                </n-space>
              </div>
            </n-card>
          </div>

          <n-empty v-else-if="!loading" description="暂无订单" size="huge" />
        </n-spin>

        <!-- Pagination -->
        <div v-if="total > pageSize" class="pagination-wrap">
          <n-pagination
            v-model:page="pageNum"
            :page-count="Math.ceil(total / pageSize)"
            @update:page="fetchOrders"
          />
        </div>
      </n-card>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import type { OrderVO } from '@/types'
import { getOrderList, payOrder, cancelOrder, confirmOrder } from '@/api/order'

const loading = ref(false)
const orders = ref<OrderVO[]>([])
const activeTab = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

function getStatusText(status: string | number): string {
  if (typeof status === 'string') return status
  const map: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '已发货',
    3: '已完成',
    4: '已取消',
  }
  return map[status] || '未知'
}

function getStatusType(status: string | number): 'default' | 'info' | 'success' | 'warning' | 'error' {
  const text = typeof status === 'string' ? status : { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }[status]
  const map: Record<string, 'default' | 'info' | 'success' | 'warning' | 'error'> = {
    '待支付': 'warning',
    '已支付': 'info',
    '已发货': 'info',
    '已完成': 'success',
    '已取消': 'error',
  }
  return map[text || ''] || 'default'
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

function handleTabChange() {
  pageNum.value = 1
  fetchOrders()
}

async function fetchOrders() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (activeTab.value !== '') {
      params.status = activeTab.value
    }
    const res = await getOrderList(params)
    orders.value = res.records || res.list || []
    total.value = res.total || 0
  } catch {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function handlePay(id: string | number) {
  try {
    await payOrder(id)
    window.$message?.success('支付成功！')
    await fetchOrders()
  } catch (e: any) {
    window.$message?.error(e?.response?.data?.message || '支付失败')
  }
}

async function handleCancel(id: string | number) {
  try {
    await cancelOrder(id)
    window.$message?.success('订单已取消')
    await fetchOrders()
  } catch (e: any) {
    window.$message?.error(e?.response?.data?.message || '取消失败')
  }
}

async function handleConfirm(id: string | number) {
  try {
    await confirmOrder(id)
    window.$message?.success('确认收货成功！')
    await fetchOrders()
  } catch (e: any) {
    window.$message?.error(e?.response?.data?.message || '确认失败')
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.order-card {
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.order-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-weight: 600;
  color: #333;
}

.order-date {
  color: #999;
  font-size: 0.85rem;
}

.order-total {
  font-size: 1.2rem;
  font-weight: 700;
  color: #e8803f;
}

.order-items-preview {
  margin-bottom: 12px;
}

.order-item-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 0.9rem;
  color: #666;
}

.item-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 400px;
}

.item-qty {
  color: #999;
  flex-shrink: 0;
}

.more-items {
  color: #999;
  font-size: 0.85rem;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
