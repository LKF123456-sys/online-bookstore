<template>
  <div class="container" style="max-width:440px">
    <div class="card">
      <h1 class="text-center" style="font-size:24px;margin-bottom:4px;">用户登录</h1>
      <p class="text-center text-dim" style="font-size:13px;margin-bottom:8px;">欢迎来到 BookVerse</p>
      <form @submit.prevent="handleLogin" class="mt-2">
        <div class="mb-2">
          <label>用户名</label>
          <input v-model="username" class="input mt-1" required placeholder="请输入用户名" autocomplete="username" />
        </div>
        <div class="mb-2">
          <label>密码</label>
          <input v-model="password" type="password" class="input mt-1" required placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <div v-if="error" class="error mt-2">{{ error }}</div>
        <button type="submit" class="btn btn-primary" style="width:100%;margin-top:18px" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <p class="text-center mt-2" style="font-size:13px;">
        还没有账号？<router-link to="/register">立即注册</router-link>
        <span style="color:var(--text-dim);margin:0 8px;">|</span>
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

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  loading.value = true; error.value = ''
  try {
    const res = await api.post('/auth/login', { username: username.value, password: password.value })
    const data = res.data
    auth.login(data.token, data.user)
    router.push('/')
  } catch (e: any) {
    error.value = e.message || '登录失败'
  } finally { loading.value = false }
}
</script>

<style scoped>
label { font-weight: 600; font-size: 13px; color: var(--text-secondary); font-family: var(--font-mono); letter-spacing: .5px; }
.error { background: rgba(255,51,102,0.1); color: var(--danger); padding: 12px 16px; border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(255,51,102,0.2); }
.text-dim { color: var(--text-dim); }
</style>
