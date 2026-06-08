/**
 * 认证状态管理 Store（Pinia）
 * ============================
 * 使用 Composition API 风格的 defineStore
 * Store 名称：'auth'
 * 数据持久化：通过 localStorage 存储 token 和 user 信息
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * User 接口定义
 * - userid: 用户唯一标识（必填）
 * - email: 用户邮箱（必填）
 * - phone: 手机号（可选）
 * - role: 角色（可选），如 'admin' 表示管理员
 * - firstname: 名（可选）
 * - lastname: 姓（可选）
 */
interface User {
  userid: string
  email: string
  phone?: string
  role?: string
  firstname?: string
  lastname?: string
}

/**
 * defineStore('auth', ...) 创建名为 'auth' 的 Pinia store
 * 内部使用 Composition API 的 setup 函数模式定义 state、getters、actions
 */
export const useAuthStore = defineStore('auth', () => {
  // ==================== State（状态） ====================

  /**
   * token：JWT 认证令牌
   * - 初始值从 localStorage.getItem('token') 读取，实现页面刷新后登录状态持久化
   * - 若 localStorage 中无 token，默认值为空字符串
   * - 使用 ref 包装，使其成为响应式数据
   */
  const token = ref<string>(localStorage.getItem('token') || '')

  /**
   * user：当前登录用户信息
   * - 初始值从 localStorage.getItem('user') 读取并 JSON.parse 解析
   * - 若 localStorage 中无 user 数据，JSON.parse('null') 返回 null（注意：'null' 字符串被 parse 为 null）
   * - 类型为 User | null，null 表示未登录
   */
  const user = ref<User | null>(JSON.parse(localStorage.getItem('user') || 'null'))

  // ==================== Getters（计算属性） ====================

  /**
   * isLoggedIn：是否已登录
   * - 判断逻辑：!!token.value → token 有值则为 true，空字符串为 false
   * - 使用 computed 确保 token 变化时自动重新计算
   */
  const isLoggedIn = computed(() => !!token.value)

  /**
   * isAdmin：是否为管理员
   * - 判断逻辑：user.value?.role === 'admin'
   * - 使用可选链操作符 ?. 防止 user 为 null 时报错
   */
  const isAdmin = computed(() => user.value?.role === 'admin')

  /**
   * userId：当前用户 ID
   * - 从 user.value?.userid 提取，若 user 为 null 则返回空字符串
   */
  const userId = computed(() => user.value?.userid || '')

  /**
   * displayName：用于界面展示的用户名
   * - 优先使用 userid，若无则回退为 '用户'
   */
  const displayName = computed(() => user.value?.userid || '用户')

  // ==================== Actions（方法） ====================

  /**
   * login 登录操作：
   * 1. 将传入的 token 和 user 写入 store 的响应式状态（token.value / user.value）
   *    使 isLoggedIn、isAdmin 等 getter 立即反应
   * 2. 同时将 token 和 user 写入 localStorage，实现页面刷新后登录状态持久化
   * @param t - JWT token 字符串
   * @param u - 用户信息对象
   */
  function login(t: string, u: User) {
    token.value = t
    user.value = u
    localStorage.setItem('token', t)
    localStorage.setItem('user', JSON.stringify(u))
  }

  /**
   * logout 登出操作：
   * 1. 清空 store 中的 token（设为空字符串）和 user（设为 null）
   * 2. 从 localStorage 中移除 token 和 user 键，清除持久化数据
   *    调用后 isLoggedIn 将变为 false，isAdmin 将变为 false
   */
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 返回需要暴露给组件使用的所有属性和方法
  return { token, user, isLoggedIn, isAdmin, userId, displayName, login, logout }
})
