import http from './index'

/**
 * AI 智能体 API 客户端
 * 提供同步对话、SSE 流式对话、会话历史管理接口
 */

/** 同步对话（一次性返回完整回复） */
export function chatMessage(data: {
  message: string
  sessionId: string
  agentType?: string
}) {
  return http.post('/api/agent/chat', data)
}

/** 获取会话历史 */
export function getChatHistory(sessionId: string) {
  return http.get('/api/agent/history', { params: { sessionId } })
}

/** 清空会话历史 */
export function clearChatHistory(sessionId: string) {
  return http.delete('/api/agent/history', { params: { sessionId } })
}

/**
 * SSE 流式对话 — 通过 EventSource 接收实时 token 流
 *
 * @param message   用户消息
 * @param sessionId 会话ID
 * @param agentType Agent 类型（auto/customer_service/product_recommend/review_analysis）
 * @param onToken   每收到一个 token 时的回调
 * @param onDone    流结束时的回调
 * @param onError   发生错误时的回调
 * @returns EventSource 实例（可用于手动关闭连接）
 */
export function chatStream(
  message: string,
  sessionId: string,
  agentType: string = 'auto',
  onToken: (token: string) => void,
  onDone: (agentName: string) => void,
  onError: (error: string) => void
): EventSource {
  const params = new URLSearchParams({
    message,
    sessionId,
    agentType
  })
  const url = `/api/agent/chat/stream?${params.toString()}`

  const eventSource = new EventSource(url, { withCredentials: true })

  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.done) {
        if (data.error) {
          onError(data.error)
        } else {
          onDone(data.agentName || '助手')
        }
        eventSource.close()
      } else {
        onToken(data.token)
      }
    } catch (e) {
      console.error('解析 SSE 数据失败:', e)
    }
  }

  eventSource.onerror = () => {
    onError('连接中断，请重试')
    eventSource.close()
  }

  return eventSource
}
