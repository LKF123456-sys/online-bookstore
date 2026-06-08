<template>
  <div class="container" style="max-width:440px">
    <div class="card admin-card">
      <div class="admin-header">
        <span class="tag">&gt; admin_access</span>
        <h1>管理后台</h1>
        <p class="text-dim">请输入管理员凭证以继续</p>
      </div>
      <form @submit.prevent="handleLogin" class="mt-2">
        <div class="mb-2">
          <label>管理员用户名</label>
          <input v-model="username" class="input mt-1" required placeholder="admin" autocomplete="username" />
        </div>
        <div class="mb-2">
          <label>密码</label>
          <input v-model="password" type="password" class="input mt-1" required placeholder="********" autocomplete="current-password" />
        </div>
        <div v-if="error" class="error mt-2">
          <span class="err-icon">!</span> {{ error }}
        </div>
        <button type="submit" class="btn btn-primary" style="width:100%;margin-top:20px" :disabled="loading">
          {{ loading ? '验证中...' : '进入管理后台' }}
        </button>
      </form>
      <div class="footer-link">
        <router-link to="/login">返回普通登录</router-link>
      </div>
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
  loading.value = true
  error.value = ''
  try {
    const res = await api.post('/auth/login', {
      username: username.value,
      password: password.value,
    })
    const data = res.data
    if (data?.user?.role !== 'admin') {
      error.value = '非管理员账号，无权访问管理后台'
      return
    }
    auth.login(data.token, data.user)
    // 跳转到后端管理后台页面
    window.location.href = 'http://localhost:8086/admin/index'
  } catch (e: any) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-card {
  padding: 40px 36px;
  border-color: var(--secondary);
  box-shadow: 0 0 40px var(--secondary-glow);
}
.admin-header { text-align: center; margin-bottom: 8px; }
.tag {
  display: inline-block; font-family: var(--font-mono); font-size: 11px;
  color: var(--secondary); background: rgba(157,78,221,0.12);
  padding: 2px 12px; border-radius: 4px; border: 1px solid rgba(157,78,221,0.25);
  margin-bottom: 16px;
}
.admin-header h1 { font-size: 26px; margin-bottom: 6px; color: var(--text); }
.text-dim { color: var(--text-dim); font-size: 13px; }
label { font-weight: 600; font-size: 13px; color: var(--text-secondary); font-family: var(--font-mono); letter-spacing: .5px; }
.error {
  background: rgba(255,51,102,0.1); color: var(--danger); padding: 12px 16px;
  border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(255,51,102,0.2);
  display: flex; align-items: center; gap: 8px;
}
.err-icon { display: inline-flex; width: 18px; height: 18px; background: var(--danger); color: #fff; border-radius: 50%; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; }
.footer-link { text-align: center; margin-top: 20px; font-size: 13px; }
</style>
