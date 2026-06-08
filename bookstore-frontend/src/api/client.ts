/**
 * Axios HTTP 客户端封装
 * =======================
 * 基于 axios 创建统一的 HTTP 请求实例，配置：
 * - 基础请求地址（baseURL）和超时时间（timeout）
 * - 请求拦截器：自动附加 JWT Token 到请求头
 * - 响应拦截器：统一提取响应数据、处理 401 未授权
 */

import axios from 'axios'
import { useAuthStore } from '../stores/auth'

/**
 * axios.create 创建 HTTP 客户端实例
 * - baseURL: '/api' → 所有请求都以此为前缀，适合开发环境通过 Vite proxy 转发
 * - timeout: 15000 → 请求超时时间为 15 秒，超时后将进入响应拦截器的错误分支
 */
const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// ==================== 请求拦截器 ====================
/**
 * 请求拦截器：在每个请求发出前执行
 * - 从 Pinia auth store 中获取 token
 * - 如果 token 存在，将以下信息附加到请求头：
 *   1. X-User-Id：用户 ID，供后端识别请求发起者
 *   2. Authorization：Bearer token，JWT 认证凭证
 * - 无论 token 是否存在，都必须 return config 让请求继续发送
 * - 注意：useAuthStore() 必须在拦截器函数内部调用（而非顶部），
 *   因为 Pinia store 需要在 Vue 应用挂载后才能使用，直接顶部调用可能报错
 */
api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers['X-User-Id'] = auth.userId
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

// ==================== 响应拦截器 ====================
/**
 * 响应拦截器：在每个响应回来后执行，分两个分支：
 *
 * 第一个分支（成功回调）：当 HTTP 状态码为 2xx 时执行
 * - res.data 是后端返回的完整响应体，格式为 Result<T>：{ code, message, data }
 * - 若 body.code === 200，直接返回整个 body 对象（即 { code, message, data }）
 *   上层调用者可直接解构 .data 获取业务数据
 * - 若 code 不是 200，抛出错误，带上后端返回的 message 或默认的 '请求失败'
 *
 * 第二个分支（错误回调）：当 HTTP 状态码非 2xx 或网络错误时执行
 * - 若 err.response.status === 401（未授权/Token 过期）：
 *   1. 调用 auth.logout() 清除本地认证状态
 *   2. 使用 window.location.href 硬跳转到 /login 页（而非 router.push）
 *      硬跳转确保所有状态完全重置，避免 Pinia/router 状态残留
 * - 其他错误：抛出带后端错误消息或默认 '网络错误' 的 Error
 */
api.interceptors.response.use(
  (res) => {
    const body = res.data
    // 后端统一返回格式：{ code: 200, message: 'ok', data: ... }
    if (body?.code === 200) return body // Result<T>: {code, message, data}
    // 业务错误时抛出异常，message 字段来自后端响应
    throw new Error(body?.message || '请求失败')
  },
  (err) => {
    // 401：Token 过期或无效，清除登录状态并跳转登录页
    if (err.response?.status === 401) {
      const auth = useAuthStore()
      auth.logout()
      // 使用 window.location.href 硬跳转，确保状态完全重置
      window.location.href = '/login'
    }
    // 抛出网络错误或后端返回的错误消息
    throw new Error(err.response?.data?.message || '网络错误')
  }
)

export default api
