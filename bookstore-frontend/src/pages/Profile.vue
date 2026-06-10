<template>
  <DefaultLayout>
    <div class="page-container">
      <h1 class="section-title">个人中心</h1>

      <n-grid :x-gap="24" :cols="2" responsive="screen" item-responsive>
        <!-- Profile Info -->
        <n-gi span="2 m:1">
          <n-card title="个人信息">
            <n-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-placement="left"
              label-width="100px"
            >
              <n-form-item label="用户名">
                <n-input :value="authStore.user?.username" disabled />
              </n-form-item>
              <n-form-item label="名" path="firstname">
                <n-input v-model:value="profileForm.firstname" placeholder="名" />
              </n-form-item>
              <n-form-item label="姓" path="lastname">
                <n-input v-model:value="profileForm.lastname" placeholder="姓" />
              </n-form-item>
              <n-form-item label="邮箱" path="email">
                <n-input v-model:value="profileForm.email" placeholder="邮箱" />
              </n-form-item>
              <n-form-item label="手机号" path="phone">
                <n-input v-model:value="profileForm.phone" placeholder="手机号" />
              </n-form-item>
              <n-form-item label="地址" path="addr1">
                <n-input v-model:value="profileForm.addr1" placeholder="地址" />
              </n-form-item>
              <n-form-item label="城市" path="city">
                <n-input v-model:value="profileForm.city" placeholder="城市" />
              </n-form-item>
              <n-form-item label="省份" path="state">
                <n-input v-model:value="profileForm.state" placeholder="省份" />
              </n-form-item>
              <n-form-item label="邮编" path="zip">
                <n-input v-model:value="profileForm.zip" placeholder="邮编" />
              </n-form-item>
              <n-form-item label="国家" path="country">
                <n-input v-model:value="profileForm.country" placeholder="国家" />
              </n-form-item>
              <n-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">
                保存修改
              </n-button>
            </n-form>
          </n-card>
        </n-gi>

        <!-- Change Password -->
        <n-gi span="2 m:1">
          <n-card title="修改密码">
            <n-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-placement="left"
              label-width="140px"
            >
              <n-form-item label="当前密码" path="oldPassword">
                <n-input
                  v-model:value="passwordForm.oldPassword"
                  type="password"
                  show-password-on="click"
                  placeholder="当前密码"
                />
              </n-form-item>
              <n-form-item label="新密码" path="newPassword">
                <n-input
                  v-model:value="passwordForm.newPassword"
                  type="password"
                  show-password-on="click"
                  placeholder="新密码"
                />
              </n-form-item>
              <n-form-item label="确认密码" path="confirmPassword">
                <n-input
                  v-model:value="passwordForm.confirmPassword"
                  type="password"
                  show-password-on="click"
                  placeholder="确认新密码"
                />
              </n-form-item>
              <n-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                修改密码
              </n-button>
            </n-form>
          </n-card>
        </n-gi>
      </n-grid>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { getUser, updateProfile, updatePassword } from '@/api/auth'
import type { FormInst, FormRules } from 'naive-ui'

const authStore = useAuthStore()

const profileFormRef = ref<FormInst | null>(null)
const passwordFormRef = ref<FormInst | null>(null)
const profileLoading = ref(false)
const passwordLoading = ref(false)

const profileForm = reactive({
  firstname: '',
  lastname: '',
  email: '',
  phone: '',
  addr1: '',
  city: '',
  state: '',
  zip: '',
  country: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileRules: FormRules = {
  email: { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
}

const passwordRules: FormRules = {
  oldPassword: { required: true, message: '请输入当前密码', trigger: 'blur' },
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (value !== passwordForm.newPassword) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur',
    },
  ],
}

async function loadUserProfile() {
  if (!authStore.userId) return
  try {
    const user = await getUser()
    authStore.setUser(user)
    profileForm.firstname = user.firstname || ''
    profileForm.lastname = user.lastname || ''
    profileForm.email = user.email || ''
    profileForm.phone = user.phone || ''
    profileForm.addr1 = user.addr1 || ''
    profileForm.city = user.city || ''
    profileForm.state = user.state || ''
    profileForm.zip = user.zip || ''
    profileForm.country = user.country || ''
  } catch {
    // handled by interceptor
  }
}

async function handleUpdateProfile() {
  try {
    await profileFormRef.value?.validate()
  } catch {
    return
  }
  profileLoading.value = true
  try {
    const updated = await updateProfile({
      firstname: profileForm.firstname,
      lastname: profileForm.lastname,
      email: profileForm.email,
      phone: profileForm.phone,
      addr1: profileForm.addr1,
      city: profileForm.city,
      state: profileForm.state,
      zip: profileForm.zip,
      country: profileForm.country,
    })
    authStore.setUser(updated)
    window.$message?.success('个人信息更新成功')
  } catch {
    // handled by interceptor
  } finally {
    profileLoading.value = false
  }
}

async function handleChangePassword() {
  try {
    await passwordFormRef.value?.validate()
  } catch {
    return
  }
  passwordLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    window.$message?.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch {
    // handled by interceptor
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>
