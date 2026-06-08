<template>
  <!-- 登录页容器：最大宽度 440px，居中展示 -->
  <div class="container" style="max-width:440px">
    <div class="card">
      <h1 class="text-center" style="font-size:24px;margin-bottom:4px;">用户登录</h1>
      <p class="text-center text-dim" style="font-size:13px;margin-bottom:8px;">欢迎来到 BookVerse</p>
      <!-- @submit.prevent="handleLogin"：表单提交时阻止默认行为并调用 handleLogin 函数 -->
      <form @submit.prevent="handleLogin" class="mt-2">
        <div class="mb-2">
          <label>用户名</label>
          <!-- v-model="username"：双向绑定用户名输入值 -->
          <!-- autocomplete="username"：浏览器自动填充用户名 -->
          <input v-model="username" class="input mt-1" required placeholder="请输入用户名" autocomplete="username" />
        </div>
        <div class="mb-2">
          <label>密码</label>
          <!-- v-model="password"：双向绑定密码输入值，type="password" 隐藏输入字符 -->
          <!-- autocomplete="current-password"：浏览器自动填充当前密码 -->
          <input v-model="password" type="password" class="input mt-1" required placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <!-- v-if="error"：仅当 error 非空时显示错误提示 -->
        <div v-if="error" class="error mt-2">{{ error }}</div>
        <!-- :disabled="loading"：请求进行中时禁用按钮，防止重复提交 -->
        <!-- 按钮文本根据 loading 状态动态切换"登录中..."或"登 录" -->
        <button type="submit" class="btn btn-primary" style="width:100%;margin-top:18px" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <p class="text-center mt-2" style="font-size:13px;">
        还没有账号？<!-- router-link to="/register"：跳转到注册页面 -->
        <router-link to="/register">立即注册</router-link>
        <span style="color:var(--text-dim);margin:0 8px;">|</span>
        <!-- router-link to="/admin/login"：跳转到管理后台登录页，使用 secondary 颜色区分 -->
        <router-link to="/admin/login" style="color:var(--secondary);">管理后台入口</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api/client'

// router：Vue Router 实例，用于登录成功后跳转到首页
const router = useRouter()
// auth：Pinia 认证状态管理 store，管理 token 和用户信息
const auth = useAuthStore()
// username：用户名字段，通过 v-model 与输入框双向绑定
const username = ref('')
// password：密码字段，通过 v-model 与输入框双向绑定
const password = ref('')
// error：登录错误信息，非空时模板中显示红色错误提示
const error = ref('')
// loading：登录请求进行中标志，true 时按钮禁用并显示"登录中..."
const loading = ref(false)

/**
 * 处理登录表单提交
 * 调用 /auth/login 接口发送用户名和密码
 * try 成功分支：
 *   - 获取返回的 token 和 user 数据
 *   - 调用 auth.login() 保存认证状态到 Pinia store
 *   - router.push('/') 跳转到首页
 * catch 失败分支：
 *   - 将错误信息赋值给 error，模板中显示红色提示
 * finally：
 *   - 无论成功或失败都将 loading 设为 false，恢复按钮可用状态
 */
async function handleLogin() {
  loading.value = true; error.value = ''
  try {
    // 调用后端登录 API：POST /auth/login，传递 username 和 password
    const res = await api.post('/auth/login', { username: username.value, password: password.value })
    const data = res.data
    // 将 token 和用户信息保存到认证 store 中
    auth.login(data.token, data.user)
    // 登录成功后跳转到首页
    router.push('/')
  } catch (e: any) {
    // 登录失败：显示后端返回的错误信息或默认错误提示
    error.value = e.message || '登录失败'
  } finally { loading.value = false }
}
</script>

<style scoped>
/* 表单标签样式：等宽字体 + 半透明灰色，科技感 */
label { font-weight: 600; font-size: 13px; color: var(--text-secondary); font-family: var(--font-mono); letter-spacing: .5px; }
/* 错误提示框：红色半透明背景 + 红色边框，与 danger 色系呼应 */
.error { background: rgba(255,51,102,0.1); color: var(--danger); padding: 12px 16px; border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(255,51,102,0.2); }
/* 次要文字：更低的透明度，用于辅助说明 */
.text-dim { color: var(--text-dim); }
</style>
