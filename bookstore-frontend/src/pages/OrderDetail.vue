<template>
  <DefaultLayout>
    <div class="page-container">
      <n-breadcrumb style="margin-bottom: 16px">
        <n-breadcrumb-item @click="$router.push('/orders')">我的订单</n-breadcrumb-item>
        <n-breadcrumb-item>订单详情</n-breadcrumb-item>
      </n-breadcrumb>

      <n-spin :show="loading">
        <template v-if="order">
          <!-- Order Status -->
          <n-card>
            <div class="order-status-header">
              <div>
                <h2>订单号：{{ order.orderid || order.orderno || order.orderNo }}</h2>
                <p class="order-date">下单时间：{{ formatDate(order.orderdate || order.createdAt || order.created_at || '') }}</p>
              </div>
              <n-tag :type="getStatusType(order.status)" size="large">
                {{ getStatusText(order.status) }}
              </n-tag>
            </div>
          </n-card>

          <n-grid :x-gap="16" :cols="3" responsive="screen" item-responsive style="margin-top: 16px">
            <!-- Order Info -->
            <n-gi span="3 m:2">
              <n-space vertical :size="16">
                <!-- Items -->
                <n-card title="订单商品">
                  <n-data-table
                    :columns="itemColumns"
                    :data="order.items"
                    :bordered="false"
                    size="small"
                  />
                </n-card>

                <!-- Shipping Info -->
                <n-card title="物流信息">
                  <n-descriptions :column="1" bordered size="small">
                    <n-descriptions-item label="收货地址">
                      {{ getShippingAddr(order) }}
                    </n-descriptions-item>
                    <n-descriptions-item label="账单地址">
                      {{ getBillingAddr(order) }}
                    </n-descriptions-item>
                    <n-descriptions-item v-if="order.paymentmethod || order.paymentMethod" label="支付方式">
                      {{ order.paymentmethod || order.paymentMethod }}
                    </n-descriptions-item>
                    <n-descriptions-item v-if="order.paymenttime || order.paymentTime" label="支付时间">
                      {{ formatDate(order.paymenttime || order.paymentTime || '') }}
                    </n-descriptions-item>
                    <n-descriptions-item v-if="order.shippingtime || order.shippingTime" label="发货时间">
                      {{ formatDate(order.shippingtime || order.shippingTime || '') }}
                    </n-descriptions-item>
                    <n-descriptions-item v-if="order.completiontime || order.completionTime" label="完成时间">
                      {{ formatDate(order.completiontime || order.completionTime || '') }}
                    </n-descriptions-item>
                  </n-descriptions>
                </n-card>
              </n-space>
            </n-gi>

            <!-- Order Summary -->
            <n-gi span="3 m:1">
              <n-card title="支付汇总" class="summary-card">
                <n-space vertical :size="8">
                  <div class="summary-line">
                    <span>小计：</span>
                    <span>${{ Number(order.totalprice || order.originalprice || order.totalamount || order.totalAmount || 0).toFixed(2) }}</span>
                  </div>
                  <div class="summary-line">
                    <span>运费：</span>
                    <span style="color: #18a058">免运费</span>
                  </div>
                  <div v-if="(order.discountamount || order.discountAmount || 0) > 0" class="summary-line discount">
                    <span>优惠：</span>
                    <span>-${{ (order.discountamount || order.discountAmount || 0).toFixed(2) }}</span>
                  </div>
                  <n-divider />
                  <div class="summary-line total">
                    <span>合计：</span>
                    <span>${{ Number(order.totalprice || order.payamount || order.payAmount || order.totalamount || order.totalAmount || 0).toFixed(2) }}</span>
                  </div>
                </n-space>

                <div style="margin-top: 20px">
                  <n-space vertical :size="8">
                    <n-button
                      v-if="order.status === '待支付'"
                      type="primary"
                      block
                      :loading="actionLoading"
                      @click="handlePay"
                    >
                      立即付款
                    </n-button>
                    <n-button
                      v-if="order.status === '待支付'"
                      block
                      :loading="actionLoading"
                      @click="handleCancel"
                    >
                      取消订单
                    </n-button>
                    <n-button
                      v-if="order.status === '已发货'"
                      type="success"
                      block
                      :loading="actionLoading"
                      @click="handleConfirm"
                    >
                      确认收货
                    </n-button>
                  </n-space>
                </div>
              </n-card>
            </n-gi>
          </n-grid>
        </template>
      </n-spin>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NImage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import type { OrderVO, OrderItemVO } from '@/types'
import { getOrder, payOrder, cancelOrder, confirmOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const actionLoading = ref(false)
const order = ref<OrderVO | null>(null)

const placeholderImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="40" height="56" fill="%23ddd"%3E%3Crect width="40" height="56"/%3E%3C/svg%3E'

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

function getShippingAddr(o: OrderVO): string {
  const parts = [o.shiptofirstname, o.shiptolastname, o.shipaddr1, o.shipaddr2, o.shipcity, o.shipstate, o.shipzip, o.shipcountry].filter(Boolean)
  if (parts.length > 0) return parts.join(' ')
  return o.shippingaddress || o.shippingAddress || '-'
}

function getBillingAddr(o: OrderVO): string {
  const parts = [o.billtofirstname, o.billtolastname, o.billaddr1, o.billaddr2, o.billcity, o.billstate, o.billzip, o.billcountry].filter(Boolean)
  if (parts.length > 0) return parts.join(' ')
  return o.billingaddress || o.billingAddress || '-'
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const itemColumns: DataTableColumns<OrderItemVO> = [
  {
    title: '商品',
    key: 'product',
    render(row) {
      const img = row.image || placeholderImage
      const title = row.productName || row.product?.name || row.title || row.name || '商品'
      return h('div', { style: 'display: flex; align-items: center; gap: 8px; cursor: pointer' }, [
        h(NImage, {
          src: img,
          width: 40,
          height: 56,
          'object-fit': 'cover',
          'preview-disabled': true,
          'fallback-src': placeholderImage,
          lazy: true,
        }),
        h('span', {
          style: 'font-weight: 500',
          onClick: () => (row.productid || row.productId) && router.push(`/product/${row.productid || row.productId}`),
        }, title),
      ])
    },
  },
  {
    title: '单价',
    key: 'price',
    width: 100,
    render(row) {
      return h('span', {}, `$${row.price.toFixed(2)}`)
    },
  },
  {
    title: '数量',
    key: 'quantity',
    width: 80,
  },
  {
    title: '小计',
    key: 'subtotal',
    width: 100,
    render(row) {
      return h('span', { style: 'color: #e8803f; font-weight: 600' }, `$${(row.price * row.quantity).toFixed(2)}`)
    },
  },
]

async function fetchOrder() {
  const id = route.params.id as string
  if (!id) return
  loading.value = true
  try {
    order.value = await getOrder(id)
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  if (!order.value) return
  actionLoading.value = true
  try {
    await payOrder(order.value.orderid || order.value.id as any)
    window.$message?.success('支付成功！')
    await fetchOrder()
  } catch (e: any) {
    window.$message?.error(e?.response?.data?.message || '支付失败，请稍后重试')
  } finally {
    actionLoading.value = false
  }
}

async function handleCancel() {
  if (!order.value) return
  actionLoading.value = true
  try {
    await cancelOrder(order.value.orderid || order.value.id as any)
    window.$message?.success('订单已取消')
    await fetchOrder()
  } catch (e: any) {
    window.$message?.error(e?.response?.data?.message || '取消失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleConfirm() {
  if (!order.value) return
  actionLoading.value = true
  try {
    await confirmOrder(order.value.orderid || order.value.id as any)
    window.$message?.success('确认收货成功！')
    await fetchOrder()
  } catch (e: any) {
    window.$message?.error(e?.response?.data?.message || '确认失败')
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  fetchOrder()
})
</script>

<style scoped>
.order-status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-status-header h2 {
  margin-bottom: 4px;
}

.order-date {
  color: #999;
  font-size: 0.9rem;
}

.summary-card {
  position: sticky;
  top: 80px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
}

.summary-line.discount span:last-child {
  color: #18a058;
}

.summary-line.total {
  font-size: 1.2rem;
  font-weight: 700;
}

.summary-line.total span:last-child {
  color: #e8803f;
}
</style>
