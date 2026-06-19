import { defineStore } from 'pinia'
import { ref } from 'vue'
import { chatMessage, getChatHistory, clearChatHistory, chatStream } from '@/api/agent'

/**
 * AI 对话 Pinia Store — 管理对话状态和 SSE 流式通信
 */

export interface ChatMessageItem {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  agentName?: string
  timestamp: number
  streaming?: boolean  // 是否正在流式输出中
}

export const useChatStore = defineStore('chat', () => {
  // ===== State =====
  const messages = ref<ChatMessageItem[]>([])
  const currentSessionId = ref<string>('')
  const isStreaming = ref(false)
  const agentType = ref<string>('auto')
  const loading = ref(false)
  let eventSource: EventSource | null = null

  // ===== Actions =====

  /** 生成唯一会话 ID */
  function generateSessionId(): string {
    return crypto.randomUUID ? crypto.randomUUID() :
      Date.now().toString(36) + Math.random().toString(36).substring(2)
  }

  /** 创建新会话 */
  function newSession() {
    // 关闭已有的 SSE 连接
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    messages.value = []
    currentSessionId.value = generateSessionId()
    isStreaming.value = false
  }

  /** 发送消息（SSE 流式） */
  async function sendMessage(content: string) {
    if (!content.trim() || isStreaming.value) return

    // 确保有会话 ID
    if (!currentSessionId.value) {
      currentSessionId.value = generateSessionId()
    }

    // 添加用户消息
    const userMsg: ChatMessageItem = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: content.trim(),
      timestamp: Date.now()
    }
    messages.value.push(userMsg)

    // 添加助手占位消息（流式填充）
    const assistantMsg: ChatMessageItem = {
      id: `assistant-${Date.now()}`,
      role: 'assistant',
      content: '',
      agentName: '思考中...',
      timestamp: Date.now(),
      streaming: true
    }
    messages.value.push(assistantMsg)

    isStreaming.value = true

    // 发起 SSE 流式请求
    eventSource = chatStream(
      content.trim(),
      currentSessionId.value,
      agentType.value,
      // onToken: 逐 token 追加内容
      (token: string) => {
        const lastMsg = messages.value[messages.value.length - 1]
        if (lastMsg && lastMsg.role === 'assistant') {
          lastMsg.content += token
        }
      },
      // onDone: 流结束
      (agentName: string) => {
        const lastMsg = messages.value[messages.value.length - 1]
        if (lastMsg && lastMsg.role === 'assistant') {
          lastMsg.agentName = agentName
          lastMsg.streaming = false
        }
        isStreaming.value = false
        eventSource = null
      },
      // onError: 出错
      (error: string) => {
        const lastMsg = messages.value[messages.value.length - 1]
        if (lastMsg && lastMsg.role === 'assistant') {
          lastMsg.content += `\n\n⚠️ ${error}`
          lastMsg.agentName = '错误'
          lastMsg.streaming = false
        }
        isStreaming.value = false
        eventSource = null
      }
    )
  }

  /** 发送同步消息（非流式，备用） */
  async function sendMessageSync(content: string) {
    if (!content.trim() || loading.value) return

    if (!currentSessionId.value) {
      currentSessionId.value = generateSessionId()
    }

    const userMsg: ChatMessageItem = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: content.trim(),
      timestamp: Date.now()
    }
    messages.value.push(userMsg)

    loading.value = true

    try {
      const result: any = await chatMessage({
        message: content.trim(),
        sessionId: currentSessionId.value,
        agentType: agentType.value
      })

      const assistantMsg: ChatMessageItem = {
        id: `assistant-${Date.now()}`,
        role: 'assistant',
        content: result.reply,
        agentName: result.agentName,
        timestamp: Date.now()
      }
      messages.value.push(assistantMsg)
    } catch (e: any) {
      const errorMsg: ChatMessageItem = {
        id: `error-${Date.now()}`,
        role: 'assistant',
        content: '请求失败，请稍后重试',
        agentName: '错误',
        timestamp: Date.now()
      }
      messages.value.push(errorMsg)
    } finally {
      loading.value = false
    }
  }

  /** 加载历史会话 */
  async function loadHistory(sessionId: string) {
    try {
      currentSessionId.value = sessionId
      const result: any = await getChatHistory(sessionId)
      if (Array.isArray(result)) {
        messages.value = result.map((item: any, index: number) => ({
          id: `history-${index}`,
          role: item.role,
          content: item.content,
          agentName: item.agentName,
          timestamp: new Date(item.createTime).getTime()
        }))
      }
    } catch (e) {
      console.error('加载历史失败:', e)
    }
  }

  /** 清空当前会话 */
  async function clearCurrentSession() {
    if (currentSessionId.value) {
      try {
        await clearChatHistory(currentSessionId.value)
      } catch (e) {
        console.warn('清空历史失败:', e)
      }
    }
    messages.value = []
  }

  /** 停止流式输出 */
  function stopStreaming() {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.streaming) {
      lastMsg.streaming = false
      lastMsg.agentName = '已停止'
    }
    isStreaming.value = false
  }

  return {
    messages,
    currentSessionId,
    isStreaming,
    agentType,
    loading,
    newSession,
    sendMessage,
    sendMessageSync,
    loadHistory,
    clearCurrentSession,
    stopStreaming
  }
})
