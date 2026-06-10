<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-space style="margin-bottom: 16px" align="center">
          <n-select
            v-model:value="statusFilter"
            :options="statusOptions"
            placeholder="Filter by status"
            clearable
            style="width: 200px"
            @update:value="handleFilter"
          />
        </n-space>

        <n-data-table
          :columns="columns"
          :data="orders"
          :loading="loading"
          :pagination="pagination"
          :bordered="true"
          @update:page="handlePageChange"
        />
      </n-card>
    </n-space>

    <!-- Order Detail Drawer -->
    <n-drawer v-model:show="showDrawer" width="600" placement="right">
      <n-drawer-content title="Order Details" closable>
        <template v-if="selectedOrder">
          <n-descriptions bordered :column="1" label-placement="left">
            <n-descriptions-item label="Order No">{{ selectedOrder.orderNo }}</n-descriptions-item>
            <n-descriptions-item label="Customer">{{ selectedOrder.username }}</n-descriptions-item>
            <n-descriptions-item label="Total Amount">${{ selectedOrder.totalAmount }}</n-descriptions-item>
            <n-descriptions-item label="Status">
              <n-tag :type="getStatusType(selectedOrder.status)" size="small">
                {{ selectedOrder.statusText }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="Address">{{ selectedOrder.address }}</n-descriptions-item>
            <n-descriptions-item label="Phone">{{ selectedOrder.phone }}</n-descriptions-item>
            <n-descriptions-item label="Remark">{{ selectedOrder.remark || '-' }}</n-descriptions-item>
            <n-descriptions-item label="Created">{{ selectedOrder.createTime }}</n-descriptions-item>
            <n-descriptions-item label="Paid">{{ selectedOrder.payTime || '-' }}</n-descriptions-item>
            <n-descriptions-item label="Shipped">{{ selectedOrder.shipTime || '-' }}</n-descriptions-item>
            <n-descriptions-item label="Completed">{{ selectedOrder.completeTime || '-' }}</n-descriptions-item>
          </n-descriptions>

          <n-divider>Order Items</n-divider>

          <n-data-table
            :columns="itemColumns"
            :data="selectedOrder.items || []"
            :bordered="true"
            size="small"
          />
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NSpace, NCard, NSelect, NDataTable, NDrawer, NDrawerContent,
  NDescriptions, NDescriptionsItem, NTag, NButton, NDivider, NPopconfirm,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { orderApi } from '@/api/order'
import type { OrderVO, OrderItemVO } from '@/types'

const message = useMessage()
const loading = ref(false)
const orders = ref<OrderVO[]>([])
const statusFilter = ref<number | null>(null)
const showDrawer = ref(false)
const selectedOrder = ref<OrderVO | null>(null)

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const statusOptions = [
  { label: 'Pending', value: 0 },
  { label: 'Paid', value: 1 },
  { label: 'Shipped', value: 2 },
  { label: 'Completed', value: 3 },
  { label: 'Cancelled', value: 4 },
]

const getStatusType = (status: number) => {
  const types: Record<number, 'default' | 'info' | 'warning' | 'success' | 'error'> = {
    0: 'default',
    1: 'info',
    2: 'warning',
    3: 'success',
    4: 'error',
  }
  return types[status] || 'default'
}

const columns: DataTableColumns<OrderVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'Order No', key: 'orderNo', width: 180 },
  { title: 'Customer', key: 'username', width: 120 },
  { title: 'Amount', key: 'totalAmount', width: 120, render: (row) => `$${row.totalAmount}` },
  {
    title: 'Status',
    key: 'status',
    width: 120,
    render: (row) => {
      return h(NTag, { type: getStatusType(row.status), size: 'small' }, { default: () => row.statusText })
    },
  },
  { title: 'Created', key: 'createTime', width: 180 },
  {
    title: 'Actions',
    key: 'actions',
    width: 200,
    render: (row) => {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleViewDetail(row) }, { default: () => 'View' }),
          row.status === 1
            ? h(
                NPopconfirm,
                { onPositiveClick: () => handleShip(row.id) },
                {
                  trigger: () => h(NButton, { size: 'small', type: 'primary' }, { default: () => 'Ship' }),
                  default: () => 'Confirm ship this order?',
                }
              )
            : null,
        ],
      })
    },
  },
]

const itemColumns: DataTableColumns<OrderItemVO> = [
  { title: 'Product', key: 'productName' },
  { title: 'Price', key: 'price', width: 100, render: (row) => `$${row.price}` },
  { title: 'Qty', key: 'quantity', width: 80 },
  { title: 'Subtotal', key: 'subtotal', width: 100, render: (row) => `$${row.subtotal}` },
]

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.getList({
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      status: statusFilter.value ?? undefined,
    })
    orders.value = res.data.list || res.data.records || []
    pagination.value.itemCount = res.data.total || 0
  } catch (err: any) {
    message.error(err.message || 'Failed to load orders')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  pagination.value.page = 1
  loadOrders()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadOrders()
}

const handleViewDetail = async (order: OrderVO) => {
  try {
    const res = await orderApi.getDetail(order.id)
    selectedOrder.value = res.data
    showDrawer.value = true
  } catch (err: any) {
    message.error(err.message || 'Failed to load order details')
  }
}

const handleShip = async (id: number) => {
  try {
    await orderApi.ship(id)
    message.success('Order shipped successfully')
    loadOrders()
  } catch (err: any) {
    message.error(err.message || 'Failed to ship order')
  }
}

onMounted(() => {
  loadOrders()
})
</script>
