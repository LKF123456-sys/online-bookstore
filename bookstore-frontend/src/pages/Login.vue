<template>
  <DefaultLayout>
    <div class="auth-container">
      <n-card title="登录书城" class="auth-card">
        <n-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-placement="top"
        >
          <n-form-item label="用户名" path="username">
            <n-input
              v-model:value="form.username"
              placeholder="请输入用户名"
              size="large"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <n-icon><PersonOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item label="密码" path="password">
            <n-input
              v-model:value="form.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              size="large"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <n-icon><LockClosedOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-button
            type="primary"
            block
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </n-button>
        </n-form>

        <div class="auth-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </n-card>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useMessageStore } from '@/stores/message'
import { getCart } from '@/api/cart'
import type { FormInst } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const cartStore = useCartStore()
const messageStore = useMessageStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    authStore.setAuth(res.token, res.user)

    // Load cart and messages
    try {
      const cart = await getCart()
      cartStore.setItems(cart.items || [])
    } catch {
      // silently fail
    }
    messageStore.fetchUnreadCount()

    window.$message?.success('登录成功！')

    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 24px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: rgba(17, 24, 39, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 16px;
  box-shadow: 0 0 40px rgba(99, 102, 241, 0.1);
}

.auth-footer {
  text-align: center;
  margin-top: 16px;
  color: #94a3b8;
}

.auth-footer a {
  color: #6366f1;
  font-weight: 500;
  transition: all 0.3s ease;
}

.auth-footer a:hover {
  color: #00d4ff;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}
</style>
