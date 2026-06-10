<template>
  <div class="dashboard">
    <n-spin :show="loading">
      <n-space vertical size="large">
        <!-- Statistics Cards -->
        <n-grid :cols="4" :x-gap="16" :y-gap="16">
          <n-gi>
            <n-card>
              <n-statistic label="Total Products" :value="stats.totalProducts">
                <template #prefix>
                  <n-icon :component="BookOutline" />
                </template>
              </n-statistic>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card>
              <n-statistic label="Total Orders" :value="stats.totalOrders">
                <template #prefix>
                  <n-icon :component="CartOutline" />
                </template>
              </n-statistic>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card>
              <n-statistic label="Total Revenue" :value="stats.totalRevenue" :precision="2">
                <template #prefix>$</template>
              </n-statistic>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card>
              <n-statistic label="Total Users" :value="stats.totalUsers">
                <template #prefix>
                  <n-icon :component="PeopleOutline" />
                </template>
              </n-statistic>
            </n-card>
          </n-gi>
        </n-grid>

        <!-- Charts Row -->
        <n-grid :cols="2" :x-gap="16" :y-gap="16">
          <n-gi>
            <n-card title="Order Status Distribution">
              <v-chart :option="orderStatusChartOption" style="height: 300px" />
            </n-card>
          </n-gi>
          <n-gi>
            <n-card title="Top 10 Products">
              <v-chart :option="topProductsChartOption" style="height: 300px" />
            </n-card>
          </n-gi>
        </n-grid>

        <!-- Low Stock Warnings -->
        <n-card title="Low Stock Warnings (Stock < 10)">
          <n-data-table
            :columns="lowStockColumns"
            :data="lowStockProducts"
            :bordered="false"
            size="small"
          />
        </n-card>

        <!-- Recent Logs -->
        <n-card title="Recent Operations">
          <n-data-table
            :columns="logColumns"
            :data="recentLogs"
            :bordered="false"
            size="small"
          />
        </n-card>
      </n-space>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { NGrid, NGi, NCard, NStatistic, NIcon, NSpace, NSpin, NDataTable, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { BookOutline, CartOutline, PeopleOutline } from '@vicons/ionicons5'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { orderApi } from '@/api/order'
import { productApi } from '@/api/product'
import { userApi } from '@/api/user'
import { logApi } from '@/api/log'
import type { ProductVO, OrderVO, ApiLogVO } from '@/types'

use([PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const loading = ref(false)
const stats = ref({
  totalProducts: 0,
  totalOrders: 0,
  totalRevenue: 0,
  totalUsers: 0,
})

const orderStatusData = ref<{ status: string; count: number }[]>([])
const topProductsData = ref<{ name: string; sales: number }[]>([])
const lowStockProducts = ref<ProductVO[]>([])
const recentLogs = ref<ApiLogVO[]>([])

const orderStatusChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '0%' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: orderStatusData.value.map(item => ({
        value: item.count,
        name: item.status,
      })),
    },
  ],
}))

const topProductsChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: topProductsData.value.map(p => p.name),
    axisLabel: { rotate: 45, interval: 0 },
  },
  yAxis: { type: 'value' },
  series: [
    {
      type: 'bar',
      data: topProductsData.value.map(p => p.sales),
      itemStyle: { color: '#18a058' },
    },
  ],
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
}))

const lowStockColumns: DataTableColumns<ProductVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'Name', key: 'name' },
  { title: 'Author', key: 'author' },
  { title: 'Stock', key: 'stock', width: 100 },
  {
    title: 'Status',
    key: 'status',
    width: 100,
    render: (row) => {
      const type = row.status === 1 ? 'success' : 'error'
      const text = row.status === 1 ? 'Active' : 'Inactive'
      return h(NTag, { type, size: 'small' }, { default: () => text })
    },
  },
]

const logColumns: DataTableColumns<ApiLogVO> = [
  { title: 'User', key: 'username', width: 120 },
  { title: 'Method', key: 'method', width: 80 },
  { title: 'Path', key: 'path' },
  { title: 'Duration (ms)', key: 'duration', width: 120 },
  {
    title: 'Status',
    key: 'status',
    width: 100,
    render: (row) => {
      const type = row.status === 200 ? 'success' : 'error'
      return h(NTag, { type, size: 'small' }, { default: () => row.status })
    },
  },
  { title: 'Time', key: 'createTime', width: 180 },
]

const loadDashboardData = async () => {
  loading.value = true
  try {
    // Load products
    const productsRes = await productApi.getList({ pageNum: 1, pageSize: 10000 })
    const products = productsRes.data.list || []
    stats.value.totalProducts = products.length

    // Low stock products
    lowStockProducts.value = products.filter(p => p.stock < 10).slice(0, 10)

    // Load orders
    const ordersRes = await orderApi.getList({ pageNum: 1, pageSize: 10000 })
    const orders = ordersRes.data.list || []
    stats.value.totalOrders = orders.length
    stats.value.totalRevenue = orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0)

    // Order status distribution
    const statusMap = new Map<string, number>()
    orders.forEach(o => {
      const status = o.statusText || `Status ${o.status}`
      statusMap.set(status, (statusMap.get(status) || 0) + 1)
    })
    orderStatusData.value = Array.from(statusMap.entries()).map(([status, count]) => ({
      status,
      count,
    }))

    // Top products (by order items)
    const productSales = new Map<string, number>()
    orders.forEach(o => {
      if (o.items) {
        o.items.forEach(item => {
          productSales.set(item.productName, (productSales.get(item.productName) || 0) + item.quantity)
        })
      }
    })
    topProductsData.value = Array.from(productSales.entries())
      .map(([name, sales]) => ({ name, sales }))
      .sort((a, b) => b.sales - a.sales)
      .slice(0, 10)

    // Load users
    const usersRes = await userApi.getList({ pageNum: 1, pageSize: 1 })
    stats.value.totalUsers = usersRes.data.total || 0

    // Load recent logs
    const logsRes = await logApi.getList({ pageNum: 1, pageSize: 10 })
    recentLogs.value = logsRes.data.list || []
  } catch (err) {
    console.error('Failed to load dashboard data:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard {
  max-width: 1600px;
  margin: 0 auto;
}
</style>
