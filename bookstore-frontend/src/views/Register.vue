<template>
  <!-- 注册页容器：最大宽度 440px，居中展示 -->
  <div class="container" style="max-width:440px">
    <div class="card">
      <h1 class="text-center" style="font-size:24px;margin-bottom:4px;">创建账号</h1>
      <p class="text-center text-dim" style="font-size:13px;margin-bottom:8px;">加入 BookVerse 探索无限书海</p>
      <!-- @submit.prevent="handleRegister"：表单提交时阻止默认行为并调用注册函数 -->
      <form @submit.prevent="handleRegister" class="mt-2">
        <!-- v-model="username"：双向绑定用户名字段 -->
        <div class="mb-2"><label>用户名</label><input v-model="username" class="input mt-1" required placeholder="请输入用户名" /></div>
        <!-- v-model="email"：双向绑定邮箱字段，type="email" 提供邮箱格式校验 -->
        <div class="mb-2"><label>邮箱</label><input v-model="email" type="email" class="input mt-1" required placeholder="example@mail.com" /></div>
        <!-- v-model="password"：双向绑定密码字段，type="password" 隐藏输入字符 -->
        <div class="mb-2"><label>密码</label><input v-model="password" type="password" class="input mt-1" required placeholder="至少6位" /></div>
        <!-- v-model="phone"：双向绑定手机号字段（选填），非必填 -->
        <div class="mb-2"><label>手机号</label><input v-model="phone" class="input mt-1" placeholder="选填" /></div>
        <!-- v-if="error"：仅当 error 非空时显示错误提示 -->
        <div v-if="error" class="error mt-2">{{ error }}</div>
        <!-- :disabled="loading"：请求进行中时禁用按钮，防止重复提交 -->
        <button type="submit" class="btn btn-primary" style="width:100%;margin-top:18px" :disabled="loading">注 册</button>
      </form>
      <p class="text-center mt-2" style="font-size:13px;">已有账号？<!-- router-link to="/login"：跳转到登录页面 --><router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/client'

// router：Vue Router 实例，用于注册成功后跳转到登录页
const router = useRouter()
// username：用户名响应式引用，与输入框 v-model 双向绑定
const username = ref('')
// email：邮箱响应式引用，与输入框 v-model 双向绑定
const email = ref('')
// password：密码响应式引用，与输入框 v-model 双向绑定
const password = ref('')
// phone：手机号响应式引用（选填），与输入框 v-model 双向绑定
const phone = ref('')
// error：注册错误信息，非空时模板中显示红色错误提示
const error = ref('')
// loading：注册请求进行中标志，true 时按钮禁用防止重复提交
const loading = ref(false)

/**
 * 处理注册表单提交
 * 调用 /auth/register 接口发送用户名、邮箱、密码、手机号
 * try 成功分支：
 *   - 注册成功后调用 router.push('/login') 跳转到登录页
 * catch 失败分支：
 *   - 将错误信息赋值给 error，模板中显示红色提示
 * finally：
 *   - 无论成功或失败都将 loading 设为 false
 */
async function handleRegister() {
  loading.value = true; error.value = ''
  try {
    // 调用后端注册 API：POST /auth/register，传递注册信息对象
    await api.post('/auth/register', {
      username: username.value, email: email.value, password: password.value, phone: phone.value
    })
    // 注册成功后跳转到登录页，让用户使用新账号登录
    router.push('/login')
  } catch (e: any) { error.value = e.message }
  finally { loading.value = false }
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
