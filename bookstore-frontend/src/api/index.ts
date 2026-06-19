import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import { useMessage } from 'naive-ui'

// Create axios instance
const http: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Global message handler - will be set by App.vue
let messageApi: ReturnType<typeof useMessage> | null = null

export function setMessageApi(api: ReturnType<typeof useMessage>) {
  messageApi = api
}

function showMessage(msg: string, type: 'error' | 'success' | 'warning' | 'info' = 'error') {
  if (messageApi) {
    messageApi[type](msg)
  }
}

// Request interceptor - attach httpOnly cookie for auth
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    config.withCredentials = true
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - unwrap Result<T>
http.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // If response has Result wrapper
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        return res.data
      } else {
        // 业务级错误：只 reject，不弹窗（由调用方决定是否提示）
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    }

    // Raw response (no wrapper)
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // 仅鉴权失败时弹窗提示并跳转登录
        const authStore = useAuthStore()
        authStore.logout()
        router.push('/login')
        showMessage('会话过期，请重新登录', 'warning')
      } else if (status === 403) {
        showMessage('访问被拒绝', 'error')
      }
      // 其他 HTTP 错误（500/503/404等）不弹窗，由各 API 的 try-catch 自行处理
    } else if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
      showMessage('网络错误，请检查网络连接', 'error')
    }
    return Promise.reject(error)
  }
)

export default http
