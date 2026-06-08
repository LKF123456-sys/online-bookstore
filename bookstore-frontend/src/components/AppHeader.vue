<template>
  <!-- ==================== 顶部导航栏 ==================== -->
  <!--
    导航栏结构说明：
    - 左侧 Logo：点击跳转到首页，整体为 router-link
    - 右侧导航区：全部图书、购物车等链接 + 登录/未登录状态判断
  -->
  <header class="header">
    <div class="container header-inner">

      <!-- Logo 区域：router-link 包裹，点击跳转到首页(/)  -->
      <router-link to="/" class="logo">
        <span class="logo-bracket">&lt;</span>
        <span class="logo-text">BookVerse</span>
        <span class="logo-bracket">/&gt;</span>
      </router-link>

      <!-- 导航链接区域 -->
      <nav class="nav">
        <!-- 公共导航：所有用户可见 -->
        <router-link to="/products">[全部图书]</router-link>
        <router-link to="/cart">[购物车]</router-link>

        <!--
          v-if/v-else 条件渲染：根据 auth.isLoggedIn 判断当前是否已登录
          - auth.isLoggedIn 是 Pinia store 中的 getter，通过 !!token.value 计算得到
          - 已登录时显示：我的订单、评价、个人资料、管理后台（管理员可见）、登出按钮
          - 未登录时显示：登录按钮
        -->
        <template v-if="auth.isLoggedIn">
          <router-link to="/orders">[我的订单]</router-link>
          <router-link to="/reviews">[评价]</router-link>
          <!-- 显示用户名，点击跳转个人资料页 -->
          <router-link to="/profile">{{ auth.displayName }}</router-link>
          <!-- auth.isAdmin 判断当前用户是否为管理员（user.role === 'admin'），仅管理员可见 -->
          <router-link v-if="auth.isAdmin" to="/admin/login" class="admin-link">[管理后台]</router-link>
          <!-- 登出按钮，点击触发 handleLogout 函数 -->
          <button class="btn btn-outline btn-sm" @click="handleLogout">登出</button>
        </template>

        <template v-else>
          <!-- 未登录状态：仅显示登录按钮 -->
          <router-link to="/login" class="btn btn-primary btn-sm">登录</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

// 获取 Pinia auth store 实例，用于读取登录状态、用户信息和管理员判断
const auth = useAuthStore()
// 获取 vue-router 的 router 实例，用于编程式导航
const router = useRouter()

/**
 * 登出处理函数：
 * 1. 调用 auth.logout() 清除 store 中的 token 和用户信息，并移除 localStorage 中的持久化数据
 * 2. 调用 router.push('/') 将页面导航回首页
 */
function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
/* 顶部导航栏容器：毛玻璃背景 + 底部边框 + 吸顶定位（sticky，层级 100） */
.header {
  background: rgba(6, 6, 18, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
  position: sticky; top: 0; z-index: 100;
}
/* 导航栏内部使用 flex 两端对齐，高度 64px */
.header-inner {
  display: flex; align-items: center; justify-content: space-between;
  height: 64px;
}
/* Logo 样式：flex 排列，等宽字体，主题色，发光阴影 */
.logo {
  display: flex; align-items: center; gap: 4px;
  font-size: 20px; font-weight: 700; color: var(--primary);
  font-family: var(--font-mono);
  text-shadow: 0 0 15px var(--primary-glow);
}
/* Logo 两侧的 <> 括号装饰 */
.logo-bracket { color: var(--text-dim); font-weight: 400; }
.logo:hover { color: #fff; text-shadow: 0 0 25px var(--primary-glow); }
.logo:hover .logo-bracket { color: var(--primary); }
/* 导航链接容器：flex 水平排列，间距 16px */
.nav {
  display: flex; align-items: center; gap: 16px; font-size: 13px;
}
/* 导航链接基础样式：等宽字体，过渡动画 */
.nav a {
  color: var(--text-secondary); transition: all .25s;
  font-family: var(--font-mono); font-size: 12px;
  letter-spacing: .5px;
}
.nav a:hover { color: var(--primary); text-shadow: 0 0 10px var(--primary-glow); }
/* 当前激活路由高亮为主题色 */
.nav a.router-link-active { color: var(--primary); }
/* 管理后台链接：强调色（accent） */
.admin-link { color: var(--accent) !important; }
.admin-link:hover { color: #fff !important; text-shadow: 0 0 10px var(--accent-glow) !important; }
/* 移动端适配：缩小间距并允许横向滚动 */
@media (max-width: 768px) {
  .nav { gap: 10px; font-size: 11px; overflow-x: auto; }
}
</style>
