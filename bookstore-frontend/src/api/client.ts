import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器 — 自动附加 JWT Token
api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers['X-User-Id'] = auth.userId
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

// 响应拦截器 — 统一提取 result.data
api.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body?.code === 200) return body // Result<T>: {code, message, data}
    throw new Error(body?.message || '请求失败')
  },
  (err) => {
    if (err.response?.status === 401) {
      const auth = useAuthStore()
      auth.logout()
      window.location.href = '/login'
    }
    throw new Error(err.response?.data?.message || '网络错误')
  }
)

export default api
