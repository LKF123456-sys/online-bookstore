import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/auth'
import type { Result } from '@/types'

const service: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 30000,
})

// Request interceptor
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data
    if (res.code !== 200) {
      console.error('API Error:', res.message)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return response
  },
  (error) => {
    console.error('Request Error:', error.message)
    return Promise.reject(error)
  }
)

// Request methods wrapper
const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.get(url, config).then(res => res.data)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.post(url, data, config).then(res => res.data)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.put(url, data, config).then(res => res.data)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<Result<T>> {
    return service.delete(url, config).then(res => res.data)
  },
}

export default request
