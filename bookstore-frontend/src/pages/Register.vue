<template>
  <DefaultLayout>
    <div class="auth-container">
      <n-card title="注册账号" class="auth-card">
        <n-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-placement="top"
        >
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="form.username" placeholder="请输入用户名" size="large">
              <template #prefix>
                <n-icon><PersonOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item label="邮箱" path="email">
            <n-input v-model:value="form.email" placeholder="请输入邮箱" size="large">
              <template #prefix>
                <n-icon><MailOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item label="手机号" path="phone">
            <n-input v-model:value="form.phone" placeholder="请输入手机号" size="large">
              <template #prefix>
                <n-icon><CallOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item label="密码" path="password">
            <n-input
              v-model:value="form.password"
              type="password"
              show-password-on="click"
              placeholder="请设置密码"
              size="large"
            >
              <template #prefix>
                <n-icon><LockClosedOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item label="确认密码" path="confirmPassword">
            <n-input
              v-model:value="form.confirmPassword"
              type="password"
              show-password-on="click"
              placeholder="请确认密码"
              size="large"
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
            @click="handleRegister"
          >
            注册
          </n-button>
        </n-form>

        <div class="auth-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </n-card>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { PersonOutline, MailOutline, CallOutline, LockClosedOutline } from '@vicons/ionicons5'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { register } from '@/api/auth'
import type { FormInst, FormRules } from 'naive-ui'

const router = useRouter()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  phone: { required: true, message: '请输入手机号', trigger: 'blur' },
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (value !== form.password) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      email: form.email,
      phone: form.phone,
    })
    window.$message?.success('注册成功！请登录。')
    router.push('/login')
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
}

.auth-footer {
  text-align: center;
  margin-top: 16px;
  color: #666;
}

.auth-footer a {
  color: #18a058;
  font-weight: 500;
}
</style>
