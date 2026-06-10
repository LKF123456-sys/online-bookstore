<template>
  <DefaultLayout>
    <div class="page-container">
      <div class="page-header">
        <h1 class="section-title">我的消息</h1>
        <n-button type="primary" size="small" :disabled="messages.length === 0" @click="handleMarkAllRead">
          全部标为已读
        </n-button>
      </div>

      <n-card>
        <n-spin :show="loading">
          <div v-if="messages.length > 0" class="messages-list">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-item"
              :class="{ unread: !msg.isRead }"
              @click="handleReadMessage(msg)"
            >
              <div class="message-header">
                <div class="message-title-row">
                  <n-badge dot :show="!msg.isRead" type="error" />
                  <h3 class="message-title">{{ msg.title }}</h3>
                  <n-tag size="tiny" :type="getMessageTypeTag(msg.type)">
                    {{ getMessageTypeText(msg.type) }}
                  </n-tag>
                </div>
                <span class="message-date">{{ formatDate(msg.createdAt) }}</span>
              </div>
              <p class="message-content">{{ msg.content }}</p>
            </div>
          </div>

          <n-empty v-else-if="!loading" description="暂无消息" size="huge" />
        </n-spin>

        <!-- Pagination -->
        <div v-if="total > pageSize" class="pagination-wrap">
          <n-pagination
            v-model:page="pageNum"
            :page-count="Math.ceil(total / pageSize)"
            @update:page="fetchMessages"
          />
        </div>
      </n-card>

      <!-- Message Detail Modal -->
      <n-modal v-model:show="showDetail" preset="card" :title="selectedMessage?.title" style="max-width: 600px">
        <template v-if="selectedMessage">
          <div class="modal-meta">
            <n-tag size="small" :type="getMessageTypeTag(selectedMessage.type)">
              {{ getMessageTypeText(selectedMessage.type) }}
            </n-tag>
            <span>{{ formatDate(selectedMessage.createdAt) }}</span>
          </div>
          <n-divider />
          <p class="modal-content">{{ selectedMessage.content }}</p>
        </template>
      </n-modal>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import type { MessageVO } from '@/types'
import { getMessageList, markAsRead, markAllAsRead } from '@/api/message'
import { useMessageStore } from '@/stores/message'

const messageStore = useMessageStore()

const loading = ref(false)
const messages = ref<MessageVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDetail = ref(false)
const selectedMessage = ref<MessageVO | null>(null)

function getMessageTypeText(type: number): string {
  const map: Record<number, string> = {
    0: '系统',
    1: '订单',
    2: '促销',
  }
  return map[type] || '其他'
}

function getMessageTypeTag(type: number): 'default' | 'info' | 'success' | 'warning' {
  const map: Record<number, 'default' | 'info' | 'success' | 'warning'> = {
    0: 'info',
    1: 'success',
    2: 'warning',
  }
  return map[type] || 'default'
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessageList(pageNum.value, pageSize.value)
    messages.value = res.records || res.list || []
    total.value = res.total || 0
  } catch {
    messages.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function handleReadMessage(msg: MessageVO) {
  selectedMessage.value = msg
  showDetail.value = true

  if (!msg.isRead) {
    try {
      await markAsRead(msg.id)
      msg.isRead = true
      messageStore.decrementUnread()
    } catch {
      // handled
    }
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    messages.value.forEach(m => m.isRead = true)
    messageStore.clearUnread()
    window.$message?.success('所有消息已标为已读')
  } catch {
    // handled
  }
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.messages-list {
  display: flex;
  flex-direction: column;
}

.message-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.message-item:hover {
  background: #fafafa;
}

.message-item.unread {
  background: #f0f9ff;
}

.message-item.unread:hover {
  background: #e6f4ff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.message-date {
  color: #999;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.message-content {
  color: #666;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.modal-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 0.9rem;
}

.modal-content {
  line-height: 1.8;
  color: #555;
  white-space: pre-wrap;
}
</style>
