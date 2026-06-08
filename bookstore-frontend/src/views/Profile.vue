<template>
  <!-- 个人资料页容器：最大宽度 600px，居中展示 -->
  <div class="container" style="max-width:600px">
    <h1>个人资料</h1>

    <!-- ===== 用户资料卡片 ===== -->
    <div class="card mb-2">
      <!-- 终端风格徽章 -->
      <div class="section-badge">&gt; user_profile.json</div>

      <!-- ===== 资料查看模式（非编辑状态） ===== -->
      <!-- v-if="!editing"：非编辑状态时以只读行展示用户信息 -->
      <div v-if="!editing">
        <div class="info-row"><span class="label">用户名</span><span>{{ user?.userid }}</span></div>
        <div class="info-row"><span class="label">邮箱</span><span>{{ user?.email }}</span></div>
        <div class="info-row"><span class="label">手机号</span><span>{{ user?.phone || '未设置' }}</span></div>
        <div class="info-row"><span class="label">姓名</span><span>{{ displayFullName || '未设置' }}</span></div>
        <!-- @click="startEdit"：点击进入编辑模式 -->
        <button class="btn btn-primary mt-2" @click="startEdit">编辑资料</button>
      </div>

      <!-- ===== 资料编辑模式 ===== -->
      <!-- v-else：编辑状态时显示表单 -->
      <!-- @submit.prevent="handleUpdateProfile"：表单提交时阻止默认行为并调用更新函数 -->
      <form v-else @submit.prevent="handleUpdateProfile">
        <!-- v-model="editForm.email"：双向绑定邮箱编辑值 -->
        <div class="mb-2"><label>邮箱</label><input v-model="editForm.email" type="email" class="input mt-1" /></div>
        <!-- v-model="editForm.phone"：双向绑定手机号编辑值 -->
        <div class="mb-2"><label>手机号</label><input v-model="editForm.phone" class="input mt-1" /></div>
        <!-- v-model="editForm.firstname"：双向绑定名编辑值 -->
        <div class="mb-2"><label>名</label><input v-model="editForm.firstname" class="input mt-1" /></div>
        <!-- v-model="editForm.lastname"：双向绑定姓编辑值 -->
        <div class="mb-2"><label>姓</label><input v-model="editForm.lastname" class="input mt-1" /></div>
        <!-- v-if="profileError"：仅当 profileError 非空时显示编辑错误提示 -->
        <div v-if="profileError" class="error mt-2">{{ profileError }}</div>
        <!-- 保存和取消按钮：flex 水平排列 -->
        <div style="display:flex;gap:8px;margin-top:16px">
          <!-- :disabled="profileLoading"：请求进行中时禁用保存按钮 -->
          <button type="submit" class="btn btn-primary" :disabled="profileLoading">保存</button>
          <!-- @click="cancelEdit"：取消编辑，恢复到查看模式 -->
          <button type="button" class="btn btn-outline" @click="cancelEdit">取消</button>
        </div>
      </form>
    </div>

    <!-- ===== 修改密码卡片 ===== -->
    <div class="card">
      <div class="section-badge">&gt; change_password.sh</div>
      <!-- @submit.prevent="handleUpdatePassword"：表单提交时阻止默认行为并调用密码更新函数 -->
      <form @submit.prevent="handleUpdatePassword">
        <!-- v-model="pwdForm.oldPassword"：双向绑定旧密码 -->
        <div class="mb-2"><label>旧密码</label><input v-model="pwdForm.oldPassword" type="password" class="input mt-1" required /></div>
        <!-- v-model="pwdForm.newPassword"：双向绑定新密码 -->
        <div class="mb-2"><label>新密码</label><input v-model="pwdForm.newPassword" type="password" class="input mt-1" required placeholder="至少6位" /></div>
        <!-- v-model="pwdForm.confirmPassword"：双向绑定确认新密码 -->
        <div class="mb-2"><label>确认新密码</label><input v-model="pwdForm.confirmPassword" type="password" class="input mt-1" required /></div>
        <!-- v-if="pwdError"：修改密码错误提示 -->
        <div v-if="pwdError" class="error mt-2">{{ pwdError }}</div>
        <!-- v-if="pwdSuccess"：修改密码成功提示（绿色） -->
        <div v-if="pwdSuccess" class="success mt-2">{{ pwdSuccess }}</div>
        <!-- :disabled="pwdLoading"：请求进行中时禁用按钮 -->
        <button type="submit" class="btn btn-primary mt-2" :disabled="pwdLoading">修改密码</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api/client'

// auth：Pinia 认证状态管理 store
const auth = useAuthStore()
// user：computed 计算属性，从 auth store 中获取当前登录用户信息
const user = computed(() => auth.user)

/**
 * displayFullName 计算属性：拼接 firstname + lastname 显示完整姓名
 * 过滤掉空字符串，用空格连接，全空时返回空字符串
 */
const displayFullName = computed(() => {
  const u = user.value
  if (!u) return ''
  return [u.firstname, u.lastname].filter(Boolean).join(' ') || ''
})

// editing：是否处于编辑模式
const editing = ref(false)
// profileLoading：资料更新请求进行中标志
const profileLoading = ref(false)
// profileError：资料更新错误信息
const profileError = ref('')
// editForm：编辑表单数据对象（邮箱、手机号、名、姓），通过 v-model 与各输入框双向绑定
const editForm = ref({ email: '', phone: '', firstname: '', lastname: '' })

/**
 * 进入编辑模式：将当前用户信息填充到编辑表单中
 */
function startEdit() {
  const u = user.value
  if (u) editForm.value = { email: u.email || '', phone: u.phone || '', firstname: u.firstname || '', lastname: u.lastname || '' }
  editing.value = true; profileError.value = ''
}

/**
 * 取消编辑模式：恢复为查看模式，清空错误信息
 */
function cancelEdit() { editing.value = false; profileError.value = '' }

/**
 * 更新用户资料
 * 调用 PUT /auth/profile 接口更新资料，然后重新获取最新资料
 * try 成功分支：
 *   - 调用 API 更新资料
 *   - 重新请求 GET /auth/profile 获取最新数据
 *   - 将最新数据合并到 auth.user 并同步到 localStorage
 *   - 退出编辑模式
 * catch 失败分支：
 *   - 将错误信息赋值给 profileError，页面显示红色提示
 * finally：
 *   - 将 profileLoading 设为 false
 */
async function handleUpdateProfile() {
  profileLoading.value = true; profileError.value = ''
  try {
    // 调用更新资料 API：PUT /auth/profile
    await api.put('/auth/profile', editForm.value)
    // 重新获取资料以获取最新数据
    const res = await api.get('/auth/profile')
    const data = res.data || res
    // 将最新资料合并到 auth store 并持久化到 localStorage
    if (data) { auth.user = { ...auth.user, ...data } as any; localStorage.setItem('user', JSON.stringify(auth.user)) }
    editing.value = false
  } catch (e: any) { profileError.value = e.message }
  finally { profileLoading.value = false }
}

// pwdForm：修改密码表单数据，通过 v-model 与各输入框双向绑定
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
// pwdLoading：密码更新请求进行中标志
const pwdLoading = ref(false)
// pwdError：密码更新错误信息
const pwdError = ref('')
// pwdSuccess：密码更新成功信息（绿色提示）
const pwdSuccess = ref('')

/**
 * 修改密码
 * 先进行前端校验（两次密码一致性、密码长度），再调用 PUT /auth/password 接口
 * 前端校验失败分支：
 *   - 两次密码不一致：设置 pwdError 并 return
 *   - 新密码不足 6 位：设置 pwdError 并 return
 * try 成功分支：
 *   - 设置 pwdSuccess 为"密码修改成功"
 *   - 清空密码表单
 * catch 失败分支：
 *   - 将错误信息赋值给 pwdError
 * finally：
 *   - 将 pwdLoading 设为 false
 */
async function handleUpdatePassword() {
  pwdError.value = ''; pwdSuccess.value = ''
  // 前端校验：两次密码是否一致
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) { pwdError.value = '两次密码不一致'; return }
  // 前端校验：新密码长度至少 6 位
  if (pwdForm.value.newPassword.length < 6) { pwdError.value = '新密码至少6位'; return }
  pwdLoading.value = true
  try {
    // 调用修改密码 API：PUT /auth/password
    await api.put('/auth/password', { oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    pwdSuccess.value = '密码修改成功'
    // 清空表单
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: any) { pwdError.value = e.message }
  finally { pwdLoading.value = false }
}

/**
 * onMounted 生命周期钩子：组件挂载时请求用户资料
 * 调用 GET /auth/profile 接口获取最新资料并更新到 auth store
 * catch：请求失败时静默忽略
 */
onMounted(async () => {
  try {
    // 请求用户资料 API：GET /auth/profile
    const res = await api.get('/auth/profile')
    const data = res.data || res
    // 将最新资料合并到 auth store 并持久化
    if (data) { auth.user = { ...auth.user, ...data } as any; localStorage.setItem('user', JSON.stringify(auth.user)) }
  } catch { /* 请求失败静默忽略 */ }
})
</script>

<style scoped>
/* 终端风格徽章：等宽灰色小字 */
.section-badge {
  font-family: var(--font-mono); font-size: 10px; color: var(--text-dim);
  margin-bottom: 16px; letter-spacing: 1px;
}
/* 信息行：flex 水平布局，底部有分割线 */
.info-row { display: flex; padding: 12px 0; border-bottom: 1px solid var(--border); }
/* 信息行标签：固定宽度 80px + 等宽灰色粗体 */
.info-row .label { width: 80px; color: var(--text-dim); font-weight: 600; flex-shrink: 0; font-size: 13px; }
/* 错误提示框：红色半透明背景 + 红色边框 */
.error { background: rgba(255,51,102,0.1); color: var(--danger); padding: 12px 16px; border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(255,51,102,0.2); }
/* 成功提示框：绿色半透明背景 + 绿色边框 */
.success { background: rgba(0,255,136,0.1); color: var(--success); padding: 12px 16px; border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(0,255,136,0.2); }
/* 表单标签：等宽字体 + 半透明灰色 */
label { font-weight: 600; font-size: 13px; color: var(--text-secondary); font-family: var(--font-mono); letter-spacing: .5px; }
</style>
