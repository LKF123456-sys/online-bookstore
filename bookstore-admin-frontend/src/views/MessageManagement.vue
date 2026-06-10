<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-space justify="space-between" align="center" style="margin-bottom: 16px">
          <n-text strong>Message Management</n-text>
          <n-button type="primary" @click="showBroadcastModal = true">Broadcast Message</n-button>
        </n-space>

        <n-data-table
          :columns="columns"
          :data="messages"
          :loading="loading"
          :pagination="pagination"
          :bordered="true"
          @update:page="handlePageChange"
        />
      </n-card>
    </n-space>

    <!-- Broadcast Modal -->
    <n-modal
      v-model:show="showBroadcastModal"
      :mask-closable="false"
      preset="dialog"
      title="Broadcast Message"
      style="width: 600px"
    >
      <n-input
        v-model:value="broadcastContent"
        type="textarea"
        placeholder="Enter broadcast message..."
        :rows="4"
      />
      <template #action>
        <n-button @click="showBroadcastModal = false">Cancel</n-button>
        <n-button type="primary" :loading="submitting" @click="handleBroadcast">
          Send Broadcast
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NSpace, NCard, NText, NButton, NDataTable, NModal, NInput, NTag,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { messageApi } from '@/api/message'
import type { MessageVO } from '@/types'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const messages = ref<MessageVO[]>([])
const showBroadcastModal = ref(false)
const broadcastContent = ref('')

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const columns: DataTableColumns<MessageVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'User', key: 'username', width: 150 },
  { title: 'Type', key: 'type', width: 120 },
  { title: 'Content', key: 'content', ellipsis: { tooltip: true } },
  { title: 'Created', key: 'createTime', width: 180 },
]

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await messageApi.getList({
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
    })
    messages.value = res.data.list || res.data.records || []
    pagination.value.itemCount = res.data.total || 0
  } catch (err: any) {
    message.error(err.message || 'Failed to load messages')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadMessages()
}

const handleBroadcast = async () => {
  if (!broadcastContent.value.trim()) {
    message.warning('Please enter message content')
    return
  }

  submitting.value = true
  try {
    await messageApi.broadcast({ content: broadcastContent.value })
    message.success('Broadcast sent successfully')
    showBroadcastModal.value = false
    broadcastContent.value = ''
    loadMessages()
  } catch (err: any) {
    message.error(err.message || 'Failed to send broadcast')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadMessages()
})
</script>
