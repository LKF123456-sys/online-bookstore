<template>
  <DefaultLayout>
    <div class="chat-page">
      <div class="chat-container">
        <!-- 侧边栏：会话管理 -->
        <div class="chat-sidebar">
          <div class="sidebar-header">
            <h3>对话列表</h3>
            <n-button type="primary" size="small" @click="handleNewSession">
              + 新对话
            </n-button>
          </div>
          <div class="session-list">
            <div
              v-for="session in sessions"
              :key="session.id"
              class="session-item"
              :class="{ active: session.id === chatStore.currentSessionId }"
              @click="handleSwitchSession(session.id)"
            >
              <span class="session-title">{{ session.title }}</span>
              <span class="session-time">{{ session.time }}</span>
            </div>
          </div>
        </div>

        <!-- 主聊天区域 -->
        <div class="chat-main">
          <!-- 顶部工具栏 -->
          <div class="chat-toolbar">
            <div class="toolbar-left">
              <h2 class="chat-title">BookVerse 智能助手</h2>
              <n-tag v-if="currentAgentName" type="info" size="small" round>
                {{ currentAgentName }}
              </n-tag>
            </div>
            <div class="toolbar-right">
              <n-select
                v-model:value="chatStore.agentType"
                :options="agentOptions"
                size="small"
                style="width: 160px"
              />
              <n-button
                size="small"
                :type="chatStore.messages.length > 0 ? 'warning' : 'default'"
                :disabled="chatStore.messages.length === 0"
                @click="handleClearSession"
              >
                清空对话
              </n-button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div ref="messageListRef" class="message-list">
            <!-- 欢迎消息 -->
            <div v-if="chatStore.messages.length === 0" class="welcome-section">
              <div class="welcome-icon">🤖</div>
              <h3>你好，我是 BookVerse 智能助手</h3>
              <p>我可以帮你查询订单、推荐图书、分析评价。试试以下问题：</p>
              <div class="quick-questions">
                <n-button
                  v-for="(q, i) in quickQuestions"
                  :key="i"
                  size="small"
                  quaternary
                  @click="handleQuickQuestion(q)"
                >
                  {{ q }}
                </n-button>
              </div>
            </div>

            <!-- 消息气泡 -->
            <div
              v-for="msg in chatStore.messages"
              :key="msg.id"
              class="message-row"
              :class="msg.role"
            >
              <div class="message-avatar">
                <span v-if="msg.role === 'user'">👤</span>
                <span v-else>🤖</span>
              </div>
              <div class="message-bubble" :class="msg.role">
                <div v-if="msg.role === 'assistant'" class="agent-badge">
                  {{ msg.agentName || '助手' }}
                </div>
                <div
                  class="message-content"
                  v-html="renderMarkdown(msg.content)"
                />
                <div v-if="msg.streaming" class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-area">
            <div class="input-wrapper">
              <n-input
                v-model:value="inputMessage"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 4 }"
                placeholder="输入你的问题... (Enter 发送, Shift+Enter 换行)"
                :disabled="chatStore.isStreaming"
                @keydown="handleKeydown"
              />
              <div class="input-actions">
                <n-button
                  v-if="!chatStore.isStreaming"
                  type="primary"
                  :disabled="!inputMessage.trim()"
                  @click="handleSend"
                >
                  发送
                </n-button>
                <n-button
                  v-else
                  type="error"
                  @click="chatStore.stopStreaming()"
                >
                  停止
                </n-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, nextTick, watch, onUnmounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const chatStore = useChatStore()

const inputMessage = ref('')
const messageListRef = ref<HTMLElement | null>(null)
const currentAgentName = ref('')

// 快捷问题
const quickQuestions = [
  '我最近的订单有哪些？',
  '有什么好书推荐？',
  '畅销书排行榜',
  '帮我查一下订单状态'
]

// Agent 类型选项
const agentOptions = [
  { label: '自动路由', value: 'auto' },
  { label: '客服助手', value: 'customer_service' },
  { label: '图书推荐', value: 'product_recommend' },
  { label: '评价分析', value: 'review_analysis' }
]

// 会话列表（本地管理）
const sessions = ref([
  { id: chatStore.currentSessionId || 'default', title: '新对话', time: '刚刚' }
])

// 初始化会话
if (!chatStore.currentSessionId) {
  chatStore.newSession()
  sessions.value[0].id = chatStore.currentSessionId
}

// 监听消息变化，自动滚动到底部
watch(
  () => chatStore.messages.length,
  () => {
    nextTick(() => scrollToBottom())
  }
)

// 监听最后一条消息的 content 变化（流式更新时滚动）
watch(
  () => {
    const last = chatStore.messages[chatStore.messages.length - 1]
    return last?.content?.length
  },
  () => {
    nextTick(() => scrollToBottom())
  }
)

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

/** 发送消息 */
async function handleSend() {
  const msg = inputMessage.value.trim()
  if (!msg || chatStore.isStreaming) return

  inputMessage.value = ''
  currentAgentName.value = ''

  await chatStore.sendMessage(msg)

  // 更新会话标题（取第一条消息的前 20 个字符）
  if (chatStore.messages.length <= 2) {
    const currentSession = sessions.value.find(s => s.id === chatStore.currentSessionId)
    if (currentSession) {
      currentSession.title = msg.length > 20 ? msg.substring(0, 20) + '...' : msg
      currentSession.time = '刚刚'
    }
  }
}

/** 键盘事件：Enter 发送，Shift+Enter 换行 */
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

/** 快捷问题点击 */
function handleQuickQuestion(q: string) {
  inputMessage.value = q
  handleSend()
}

/** 新建会话 */
function handleNewSession() {
  chatStore.newSession()
  currentAgentName.value = ''
  sessions.value.unshift({
    id: chatStore.currentSessionId,
    title: '新对话',
    time: '刚刚'
  })
  // 最多保留 20 个会话
  if (sessions.value.length > 20) {
    sessions.value = sessions.value.slice(0, 20)
  }
}

/** 切换会话 */
function handleSwitchSession(sessionId: string) {
  chatStore.loadHistory(sessionId)
  currentAgentName.value = ''
}

/** 清空当前会话 */
function handleClearSession() {
  chatStore.clearCurrentSession()
  currentAgentName.value = ''
}

/** 简单的 Markdown 渲染（基础支持） */
function renderMarkdown(text: string): string {
  if (!text) return ''
  let html = text
    // 转义 HTML
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 代码块
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code class="lang-$1">$2</code></pre>')
    // 行内代码
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    // 粗体
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
    // 换行
    .replace(/\n/g, '<br>')
  return html
}

onUnmounted(() => {
  chatStore.stopStreaming()
})
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 140px);
  padding: 0;
}

.chat-container {
  display: flex;
  height: 100%;
  gap: 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

/* ===== 侧边栏 ===== */
.chat-sidebar {
  width: 260px;
  background: rgba(15, 18, 35, 0.95);
  border-right: 1px solid rgba(99, 102, 241, 0.15);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid rgba(99, 102, 241, 0.15);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 14px;
  color: #e0e0e0;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: all 0.2s;
}

.session-item:hover {
  background: rgba(99, 102, 241, 0.1);
}

.session-item.active {
  background: rgba(99, 102, 241, 0.2);
  border-left: 3px solid #6366f1;
}

.session-title {
  display: block;
  font-size: 13px;
  color: #d0d0d0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 11px;
  color: #666;
  margin-top: 2px;
  display: block;
}

/* ===== 主聊天区域 ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(10, 14, 26, 0.95);
  min-width: 0;
}

.chat-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid rgba(99, 102, 241, 0.15);
  background: rgba(15, 18, 35, 0.8);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-title {
  margin: 0;
  font-size: 16px;
  background: linear-gradient(135deg, #6366f1, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* ===== 消息列表 ===== */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;
}

.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: #999;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.welcome-section h3 {
  color: #e0e0e0;
  margin-bottom: 8px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  justify-content: center;
}

/* ===== 消息气泡 ===== */
.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

.message-row.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  background: rgba(99, 102, 241, 0.1);
}

.message-row.user .message-avatar {
  background: rgba(0, 212, 255, 0.1);
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;
}

.message-bubble.user {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(99, 102, 241, 0.15));
  border: 1px solid rgba(99, 102, 241, 0.3);
  color: #e0e0e0;
}

.message-bubble.assistant {
  background: rgba(30, 35, 55, 0.8);
  border: 1px solid rgba(99, 102, 241, 0.1);
  color: #d0d0d0;
}

.agent-badge {
  font-size: 11px;
  color: #6366f1;
  margin-bottom: 6px;
  font-weight: 500;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message-content :deep(pre) {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  padding: 12px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-content :deep(code) {
  background: rgba(99, 102, 241, 0.15);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.message-content :deep(pre code) {
  background: none;
  padding: 0;
}

.message-content :deep(a) {
  color: #00d4ff;
  text-decoration: none;
}

.message-content :deep(strong) {
  color: #fff;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  margin-top: 8px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #6366f1;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 输入区域 ===== */
.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid rgba(99, 102, 241, 0.15);
  background: rgba(15, 18, 35, 0.8);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper :deep(.n-input) {
  flex: 1;
}

.input-wrapper :deep(.n-input .n-input__textarea-el) {
  background: rgba(30, 35, 55, 0.6);
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: #e0e0e0;
}

.input-actions {
  flex-shrink: 0;
}

/* ===== 滚动条 ===== */
.message-list::-webkit-scrollbar,
.session-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track,
.session-list::-webkit-scrollbar-track {
  background: transparent;
}

.message-list::-webkit-scrollbar-thumb,
.session-list::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.3);
  border-radius: 3px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .chat-sidebar {
    display: none;
  }

  .message-bubble {
    max-width: 85%;
  }
}
</style>
