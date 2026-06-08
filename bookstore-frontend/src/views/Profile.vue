<template>
  <div class="container" style="max-width:600px">
    <h1>个人资料</h1>

    <div class="card mb-2">
      <div class="section-badge">&gt; user_profile.json</div>
      <div v-if="!editing">
        <div class="info-row"><span class="label">用户名</span><span>{{ user?.userid }}</span></div>
        <div class="info-row"><span class="label">邮箱</span><span>{{ user?.email }}</span></div>
        <div class="info-row"><span class="label">手机号</span><span>{{ user?.phone || '未设置' }}</span></div>
        <div class="info-row"><span class="label">姓名</span><span>{{ displayFullName || '未设置' }}</span></div>
        <button class="btn btn-primary mt-2" @click="startEdit">编辑资料</button>
      </div>
      <form v-else @submit.prevent="handleUpdateProfile">
        <div class="mb-2"><label>邮箱</label><input v-model="editForm.email" type="email" class="input mt-1" /></div>
        <div class="mb-2"><label>手机号</label><input v-model="editForm.phone" class="input mt-1" /></div>
        <div class="mb-2"><label>名</label><input v-model="editForm.firstname" class="input mt-1" /></div>
        <div class="mb-2"><label>姓</label><input v-model="editForm.lastname" class="input mt-1" /></div>
        <div v-if="profileError" class="error mt-2">{{ profileError }}</div>
        <div style="display:flex;gap:8px;margin-top:16px">
          <button type="submit" class="btn btn-primary" :disabled="profileLoading">保存</button>
          <button type="button" class="btn btn-outline" @click="cancelEdit">取消</button>
        </div>
      </form>
    </div>

    <div class="card">
      <div class="section-badge">&gt; change_password.sh</div>
      <form @submit.prevent="handleUpdatePassword">
        <div class="mb-2"><label>旧密码</label><input v-model="pwdForm.oldPassword" type="password" class="input mt-1" required /></div>
        <div class="mb-2"><label>新密码</label><input v-model="pwdForm.newPassword" type="password" class="input mt-1" required placeholder="至少6位" /></div>
        <div class="mb-2"><label>确认新密码</label><input v-model="pwdForm.confirmPassword" type="password" class="input mt-1" required /></div>
        <div v-if="pwdError" class="error mt-2">{{ pwdError }}</div>
        <div v-if="pwdSuccess" class="success mt-2">{{ pwdSuccess }}</div>
        <button type="submit" class="btn btn-primary mt-2" :disabled="pwdLoading">修改密码</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api/client'

const auth = useAuthStore()
const user = computed(() => auth.user)

const displayFullName = computed(() => {
  const u = user.value
  if (!u) return ''
  return [u.firstname, u.lastname].filter(Boolean).join(' ') || ''
})

const editing = ref(false); const profileLoading = ref(false); const profileError = ref('')
const editForm = ref({ email: '', phone: '', firstname: '', lastname: '' })

function startEdit() {
  const u = user.value
  if (u) editForm.value = { email: u.email || '', phone: u.phone || '', firstname: u.firstname || '', lastname: u.lastname || '' }
  editing.value = true; profileError.value = ''
}

function cancelEdit() { editing.value = false; profileError.value = '' }

async function handleUpdateProfile() {
  profileLoading.value = true; profileError.value = ''
  try {
    await api.put('/auth/profile', editForm.value)
    const res = await api.get('/auth/profile')
    const data = res.data || res
    if (data) { auth.user = { ...auth.user, ...data } as any; localStorage.setItem('user', JSON.stringify(auth.user)) }
    editing.value = false
  } catch (e: any) { profileError.value = e.message }
  finally { profileLoading.value = false }
}

const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false); const pwdError = ref(''); const pwdSuccess = ref('')

async function handleUpdatePassword() {
  pwdError.value = ''; pwdSuccess.value = ''
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) { pwdError.value = '两次密码不一致'; return }
  if (pwdForm.value.newPassword.length < 6) { pwdError.value = '新密码至少6位'; return }
  pwdLoading.value = true
  try {
    await api.put('/auth/password', { oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    pwdSuccess.value = '密码修改成功'
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: any) { pwdError.value = e.message }
  finally { pwdLoading.value = false }
}

onMounted(async () => {
  try {
    const res = await api.get('/auth/profile')
    const data = res.data || res
    if (data) { auth.user = { ...auth.user, ...data } as any; localStorage.setItem('user', JSON.stringify(auth.user)) }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.section-badge {
  font-family: var(--font-mono); font-size: 10px; color: var(--text-dim);
  margin-bottom: 16px; letter-spacing: 1px;
}
.info-row { display: flex; padding: 12px 0; border-bottom: 1px solid var(--border); }
.info-row .label { width: 80px; color: var(--text-dim); font-weight: 600; flex-shrink: 0; font-size: 13px; }
.error { background: rgba(255,51,102,0.1); color: var(--danger); padding: 12px 16px; border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(255,51,102,0.2); }
.success { background: rgba(0,255,136,0.1); color: var(--success); padding: 12px 16px; border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(0,255,136,0.2); }
label { font-weight: 600; font-size: 13px; color: var(--text-secondary); font-family: var(--font-mono); letter-spacing: .5px; }
</style>
