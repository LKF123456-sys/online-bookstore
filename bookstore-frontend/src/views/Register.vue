<template>
  <div class="container" style="max-width:440px">
    <div class="card">
      <h1 class="text-center" style="font-size:24px;margin-bottom:4px;">创建账号</h1>
      <p class="text-center text-dim" style="font-size:13px;margin-bottom:8px;">加入 BookVerse 探索无限书海</p>
      <form @submit.prevent="handleRegister" class="mt-2">
        <div class="mb-2"><label>用户名</label><input v-model="username" class="input mt-1" required placeholder="请输入用户名" /></div>
        <div class="mb-2"><label>邮箱</label><input v-model="email" type="email" class="input mt-1" required placeholder="example@mail.com" /></div>
        <div class="mb-2"><label>密码</label><input v-model="password" type="password" class="input mt-1" required placeholder="至少6位" /></div>
        <div class="mb-2"><label>手机号</label><input v-model="phone" class="input mt-1" placeholder="选填" /></div>
        <div v-if="error" class="error mt-2">{{ error }}</div>
        <button type="submit" class="btn btn-primary" style="width:100%;margin-top:18px" :disabled="loading">注 册</button>
      </form>
      <p class="text-center mt-2" style="font-size:13px;">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/client'

const router = useRouter()
const username = ref(''); const email = ref(''); const password = ref(''); const phone = ref('')
const error = ref(''); const loading = ref(false)

async function handleRegister() {
  loading.value = true; error.value = ''
  try {
    await api.post('/auth/register', {
      username: username.value, email: email.value, password: password.value, phone: phone.value
    })
    router.push('/login')
  } catch (e: any) { error.value = e.message }
  finally { loading.value = false }
}
</script>

<style scoped>
label { font-weight: 600; font-size: 13px; color: var(--text-secondary); font-family: var(--font-mono); letter-spacing: .5px; }
.error { background: rgba(255,51,102,0.1); color: var(--danger); padding: 12px 16px; border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(255,51,102,0.2); }
.text-dim { color: var(--text-dim); }
</style>
