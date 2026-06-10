<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <n-icon size="28">
            <BookOutline />
          </n-icon>
        </div>
        <h1>BookVerse Admin</h1>
        <p>图书管理系统后台</p>
      </div>
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
      >
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="formData.username"
            placeholder="请输入管理员用户名"
            @keyup.enter="handleLogin"
          />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </n-form-item>
        <n-button
          type="primary"
          block
          :loading="loading"
          @click="handleLogin"
          style="margin-top: 12px"
        >
          登录
        </n-button>
      </n-form>
      <div class="login-footer">
        <router-link to="/">返回前台首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NForm, NFormItem, NInput, NButton, NIcon } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { BookOutline } from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await authStore.login(formData.username, formData.password)
    message.success('登录成功')
    router.push('/admin/dashboard')
  } catch (err: any) {
    message.error(err.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #0a0e1a;
  position: relative;
}

.login-page::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(17, 24, 39, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.2);
  box-shadow: 0 0 40px rgba(99, 102, 241, 0.1);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #6366f1, #00d4ff);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto 16px;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.4);
  animation: brandPulse 3s ease-in-out infinite;
}

@keyframes brandPulse {
  0%, 100% { box-shadow: 0 0 20px rgba(99, 102, 241, 0.4); }
  50% { box-shadow: 0 0 30px rgba(99, 102, 241, 0.6); }
}

.login-header h1 {
  font-size: 24px;
  background: linear-gradient(135deg, #f1f5f9, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  font-weight: 800;
}

.login-header p {
  color: #64748b;
  font-size: 14px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(99, 102, 241, 0.2);
}

.login-footer a {
  color: #6366f1;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.login-footer a:hover {
  color: #00d4ff;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}
</style>
