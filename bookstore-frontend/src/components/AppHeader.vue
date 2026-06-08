<template>
  <header class="header">
    <div class="container header-inner">
      <router-link to="/" class="logo">
        <span class="logo-bracket">&lt;</span>
        <span class="logo-text">BookVerse</span>
        <span class="logo-bracket">/&gt;</span>
      </router-link>
      <nav class="nav">
        <router-link to="/products">[全部图书]</router-link>
        <router-link to="/cart">[购物车]</router-link>
        <template v-if="auth.isLoggedIn">
          <router-link to="/orders">[我的订单]</router-link>
          <router-link to="/reviews">[评价]</router-link>
          <router-link to="/profile">{{ auth.displayName }}</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/login" class="admin-link">[管理后台]</router-link>
          <button class="btn btn-outline btn-sm" @click="handleLogout">登出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn btn-primary btn-sm">登录</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.header {
  background: rgba(6, 6, 18, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
  position: sticky; top: 0; z-index: 100;
}
.header-inner {
  display: flex; align-items: center; justify-content: space-between;
  height: 64px;
}
.logo {
  display: flex; align-items: center; gap: 4px;
  font-size: 20px; font-weight: 700; color: var(--primary);
  font-family: var(--font-mono);
  text-shadow: 0 0 15px var(--primary-glow);
}
.logo-bracket { color: var(--text-dim); font-weight: 400; }
.logo:hover { color: #fff; text-shadow: 0 0 25px var(--primary-glow); }
.logo:hover .logo-bracket { color: var(--primary); }
.nav {
  display: flex; align-items: center; gap: 16px; font-size: 13px;
}
.nav a {
  color: var(--text-secondary); transition: all .25s;
  font-family: var(--font-mono); font-size: 12px;
  letter-spacing: .5px;
}
.nav a:hover { color: var(--primary); text-shadow: 0 0 10px var(--primary-glow); }
.nav a.router-link-active { color: var(--primary); }
.admin-link { color: var(--accent) !important; }
.admin-link:hover { color: #fff !important; text-shadow: 0 0 10px var(--accent-glow) !important; }
@media (max-width: 768px) {
  .nav { gap: 10px; font-size: 11px; overflow-x: auto; }
}
</style>
