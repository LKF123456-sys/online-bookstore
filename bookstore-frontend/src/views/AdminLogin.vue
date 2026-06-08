<template>
  <!-- 管理员登录页容器：最大宽度 440px，居中展示 -->
  <div class="container" style="max-width:440px">
    <!-- admin-card：管理员专用卡片样式，紫色边框 + 紫色发光阴影 -->
    <div class="card admin-card">
      <div class="admin-header">
        <!-- 终端风格标签：标识管理员入口 -->
        <span class="tag">&gt; admin_access</span>
        <h1>管理后台</h1>
        <p class="text-dim">请输入管理员凭证以继续</p>
      </div>
      <!-- @submit.prevent="handleLogin"：表单提交时阻止默认行为并调用管理员登录函数 -->
      <form @submit.prevent="handleLogin" class="mt-2">
        <div class="mb-2">
          <label>管理员用户名</label>
          <!-- v-model="username"：双向绑定用户名输入值 -->
          <!-- autocomplete="username"：浏览器自动填充用户名 -->
          <input v-model="username" class="input mt-1" required placeholder="admin" autocomplete="username" />
        </div>
        <div class="mb-2">
          <label>密码</label>
          <!-- v-model="password"：双向绑定密码输入值，type="password" 隐藏输入 -->
          <!-- autocomplete="current-password"：浏览器自动填充当前密码 -->
          <input v-model="password" type="password" class="input mt-1" required placeholder="********" autocomplete="current-password" />
        </div>
        <!-- v-if="error"：仅当 error 非空时显示带有 "!" 图标的错误提示 -->
        <div v-if="error" class="error mt-2">
          <span class="err-icon">!</span> {{ error }}
        </div>
        <!-- :disabled="loading"：请求进行中时禁用按钮，文本切换"验证中..."或"进入管理后台" -->
        <button type="submit" class="btn btn-primary" style="width:100%;margin-top:20px" :disabled="loading">
          {{ loading ? '验证中...' : '进入管理后台' }}
        </button>
      </form>
      <div class="footer-link">
        <!-- router-link to="/login"：返回普通用户登录页 -->
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

// router：Vue Router 实例（本页面主要通过 window.location.href 跳转，router 备用）
const router = useRouter()
// auth：Pinia 认证状态管理 store，保存 token 和用户信息
const auth = useAuthStore()
// username：管理员用户名字段，通过 v-model 与输入框双向绑定
const username = ref('')
// password：管理员密码字段，通过 v-model 与输入框双向绑定
const password = ref('')
// error：登录错误信息，非空时模板显示带 "!" 图标的错误提示
const error = ref('')
// loading：登录请求进行中标志，true 时按钮禁用并显示"验证中..."
const loading = ref(false)

/**
 * 处理管理员登录表单提交
 * 调用 /auth/login 接口，与普通登录共用同一个认证 API
 * try 成功分支：
 *   - 检查返回的用户角色 (data.user.role) 是否为 'admin'
 *   - 如果不是管理员角色：设置错误提示"非管理员账号，无权访问管理后台"并提前返回
 *   - 如果是管理员角色：调用 auth.login() 保存认证状态，然后通过 window.location.href 全页跳转到后端管理后台
 * catch 失败分支：
 *   - 将错误信息赋值给 error，模板中显示红色错误提示
 * finally：
 *   - 无论成功或失败都将 loading 设为 false
 */
async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    // 调用后端登录 API：POST /auth/login，传递管理员用户名和密码
    const res = await api.post('/auth/login', {
      username: username.value,
      password: password.value,
    })
    const data = res.data
    // 角色校验：只有 role 为 'admin' 的用户才允许进入管理后台
    if (data?.user?.role !== 'admin') {
      error.value = '非管理员账号，无权访问管理后台'
      return
    }
    // 认证成功后保存 token 和用户信息到 Pinia store
    auth.login(data.token, data.user)
    // 全页跳转到后端管理后台页面（controller 渲染的页面，不是 SPA 路由）
    window.location.href = 'http://localhost:8086/admin/index'
  } catch (e: any) {
    // 登录失败：显示后端返回的错误信息或默认提示
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 管理员卡片：紫色边框 + 紫色发光阴影（secondary 色系），与管理后台视觉统一 */
.admin-card {
  padding: 40px 36px;
  border-color: var(--secondary);
  box-shadow: 0 0 40px var(--secondary-glow);
}
/* 头部区域：居中 */
.admin-header { text-align: center; margin-bottom: 8px; }
/* 终端风格标签：紫色系边框和背景，等宽字体，标识管理员入口 */
.tag {
  display: inline-block; font-family: var(--font-mono); font-size: 11px;
  color: var(--secondary); background: rgba(157,78,221,0.12);
  padding: 2px 12px; border-radius: 4px; border: 1px solid rgba(157,78,221,0.25);
  margin-bottom: 16px;
}
/* 管理后台标题：字号略小于首页主标题 */
.admin-header h1 { font-size: 26px; margin-bottom: 6px; color: var(--text); }
/* 次要文字 */
.text-dim { color: var(--text-dim); font-size: 13px; }
/* 表单标签：等宽字体 + 半透明灰色 */
label { font-weight: 600; font-size: 13px; color: var(--text-secondary); font-family: var(--font-mono); letter-spacing: .5px; }
/* 错误提示框：flex 布局让 "!" 图标与文字水平排列，红色半透明背景 */
.error {
  background: rgba(255,51,102,0.1); color: var(--danger); padding: 12px 16px;
  border-radius: var(--radius-sm); font-size: 13px; border: 1px solid rgba(255,51,102,0.2);
  display: flex; align-items: center; gap: 8px;
}
/* 错误图标：红色圆形背景 + 白色 "!" 文字，醒目提示 */
.err-icon { display: inline-flex; width: 18px; height: 18px; background: var(--danger); color: #fff; border-radius: 50%; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; }
/* 底部链接：居中 */
.footer-link { text-align: center; margin-top: 20px; font-size: 13px; }
</style>
