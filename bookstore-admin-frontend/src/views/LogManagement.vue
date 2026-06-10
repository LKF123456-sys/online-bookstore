<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-space style="margin-bottom: 16px" align="center">
          <n-input
            v-model:value="searchKeyword"
            placeholder="Search logs..."
            clearable
            style="width: 300px"
            @update:value="handleSearch"
          />
        </n-space>

        <n-data-table
          :columns="columns"
          :data="logs"
          :loading="loading"
          :pagination="pagination"
          :bordered="true"
          @update:page="handlePageChange"
        />
      </n-card>
    </n-space>

    <!-- Detail Drawer -->
    <n-drawer v-model:show="showDrawer" width="600" placement="right">
      <n-drawer-content title="Log Details" closable>
        <template v-if="selectedLog">
          <n-descriptions bordered :column="1" label-placement="left">
            <n-descriptions-item label="ID">{{ selectedLog.id }}</n-descriptions-item>
            <n-descriptions-item label="User">{{ selectedLog.username }}</n-descriptions-item>
            <n-descriptions-item label="Method">
              <n-tag :type="getMethodType(selectedLog.method)" size="small">
                {{ selectedLog.method }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="Path">{{ selectedLog.path }}</n-descriptions-item>
            <n-descriptions-item label="Params">
              <n-code :code="selectedLog.params || '-'" language="json" />
            </n-descriptions-item>
            <n-descriptions-item label="IP">{{ selectedLog.ip }}</n-descriptions-item>
            <n-descriptions-item label="Duration">{{ selectedLog.duration }} ms</n-descriptions-item>
            <n-descriptions-item label="Status">
              <n-tag :type="selectedLog.status === 200 ? 'success' : 'error'" size="small">
                {{ selectedLog.status }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="Time">{{ selectedLog.createTime }}</n-descriptions-item>
          </n-descriptions>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NSpace, NCard, NInput, NDataTable, NDrawer, NDrawerContent,
  NDescriptions, NDescriptionsItem, NTag, NButton, NCode,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { logApi } from '@/api/log'
import type { ApiLogVO } from '@/types'

const message = useMessage()
const loading = ref(false)
const logs = ref<ApiLogVO[]>([])
const searchKeyword = ref('')
const showDrawer = ref(false)
const selectedLog = ref<ApiLogVO | null>(null)

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const getMethodType = (method: string) => {
  const types: Record<string, 'default' | 'info' | 'success' | 'warning' | 'error'> = {
    GET: 'info',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'error',
  }
  return types[method] || 'default'
}

const columns: DataTableColumns<ApiLogVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'User', key: 'username', width: 120 },
  {
    title: 'Method',
    key: 'method',
    width: 100,
    render: (row) => {
      return h(NTag, { type: getMethodType(row.method), size: 'small' }, { default: () => row.method })
    },
  },
  { title: 'Path', key: 'path', ellipsis: { tooltip: true } },
  { title: 'Duration', key: 'duration', width: 120, render: (row) => `${row.duration} ms` },
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
  {
    title: 'Actions',
    key: 'actions',
    width: 100,
    render: (row) => {
      return h(NButton, { size: 'small', onClick: () => handleViewDetail(row) }, { default: () => 'View' })
    },
  },
]

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await logApi.getList({
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value,
    })
    logs.value = res.data.list || res.data.records || []
    pagination.value.itemCount = res.data.total || 0
  } catch (err: any) {
    message.error(err.message || 'Failed to load logs')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadLogs()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadLogs()
}

const handleViewDetail = async (log: ApiLogVO) => {
  try {
    const res = await logApi.getDetail(log.id)
    selectedLog.value = res.data
    showDrawer.value = true
  } catch (err: any) {
    message.error(err.message || 'Failed to load log details')
  }
}

onMounted(() => {
  loadLogs()
})
</script>
