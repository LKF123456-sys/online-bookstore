<template>
  <n-layout-header bordered class="app-header">
    <div class="header-content container">
      <router-link to="/" class="logo">
        <div class="logo-icon">
          <n-icon size="24">
            <BookOutline />
          </n-icon>
        </div>
        <span class="logo-text">BookVerse</span>
      </router-link>

      <n-space align="center" :size="8" class="nav-links">
        <router-link to="/" class="nav-link" active-class="active">首页</router-link>
        <router-link to="/products" class="nav-link" active-class="active">商品</router-link>
        <router-link to="/coupons" class="nav-link" active-class="active">优惠券</router-link>
        <router-link v-if="authStore.isLoggedIn" to="/chat" class="nav-link" active-class="active">
          <n-icon size="14"><ChatbubbleEllipsesOutline /></n-icon>
          智能助手
        </router-link>
        <a v-if="isAdmin" :href="adminUrl" target="_blank" class="nav-link admin-link">
          <n-icon size="14"><SettingsOutline /></n-icon>
          管理后台
        </a>
      </n-space>

      <n-space align="center" :size="16" class="header-actions">
        <!-- Cart -->
        <router-link to="/cart" class="nav-link icon-link">
          <n-badge :value="cartStore.itemCount" :max="99" :show="cartStore.itemCount > 0">
            <n-icon size="22">
              <CartOutline />
            </n-icon>
          </n-badge>
        </router-link>

        <!-- Auth section -->
        <template v-if="authStore.isLoggedIn">
          <!-- Orders -->
          <router-link to="/orders" class="nav-link icon-link">
            <n-icon size="22">
              <ReceiptOutline />
            </n-icon>
          </router-link>

          <!-- Messages -->
          <router-link to="/messages" class="nav-link icon-link">
            <n-badge :value="messageStore.unreadCount" :max="99" :show="messageStore.unreadCount > 0">
              <n-icon size="22">
                <MailOutline />
              </n-icon>
            </n-badge>
          </router-link>

          <!-- User dropdown -->
          <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
            <n-button text class="user-btn">
              <n-icon size="22">
                <PersonCircleOutline />
              </n-icon>
              <span style="margin-left: 6px">{{ authStore.username }}</span>
            </n-button>
          </n-dropdown>
        </template>

        <template v-else>
          <router-link to="/login">
            <n-button type="primary" size="small" class="login-btn">登录</n-button>
          </router-link>
          <router-link to="/register">
            <n-button size="small" class="register-btn">注册</n-button>
          </router-link>
        </template>
      </n-space>
    </div>
  </n-layout-header>
</template>

<script setup lang="ts">
import { h, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  BookOutline,
  CartOutline,
  ReceiptOutline,
  MailOutline,
  PersonCircleOutline,
  SettingsOutline,
  ChatbubbleEllipsesOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useMessageStore } from '@/stores/message'
import { getCart } from '@/api/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const messageStore = useMessageStore()

const isAdmin = computed(() => authStore.isLoggedIn && authStore.user?.role === 'admin')

// 管理后台地址：开发环境端口5174，生产环境端口81
const adminUrl = computed(() => {
  const { protocol, hostname } = window.location
  const isDev = import.meta.env.DEV
  const port = isDev ? ':5174' : ':81'
  return `${protocol}//${hostname}${port}/`
})

const userMenuOptions = [
  { label: '个人中心', key: 'profile' },
  { label: '我的订单', key: 'orders' },
  { label: '我的评价', key: 'reviews' },
  { label: '我的优惠券', key: 'coupons' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' },
]

function handleUserMenu(key: string) {
  switch (key) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'reviews':
      router.push('/reviews')
      break
    case 'coupons':
      router.push('/coupons')
      break
    case 'logout':
      authStore.logout()
      cartStore.clearItems()
      messageStore.clearUnread()
      router.push('/')
      break
  }
}

onMounted(async () => {
  if (authStore.isLoggedIn) {
    try {
      const cart = await getCart()
      cartStore.setItems(cart.items || [])
    } catch {
      // silently fail
    }
    messageStore.fetchUnreadCount()
  }
})
</script>

<style scoped>
.app-header {
  background: rgba(10, 14, 26, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(99, 102, 241, 0.2);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  height: 70px;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6366f1, #00d4ff);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.4);
  animation: brandPulse 3s ease-in-out infinite;
}

@keyframes brandPulse {
  0%, 100% { box-shadow: 0 0 20px rgba(99, 102, 241, 0.4); }
  50% { box-shadow: 0 0 30px rgba(99, 102, 241, 0.6); }
}

.logo-text {
  font-size: 1.4rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #f1f5f9, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  flex: 1;
  margin-left: 40px;
}

.nav-link {
  color: #94a3b8;
  text-decoration: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover,
.nav-link.active {
  color: #00d4ff;
  background: rgba(0, 212, 255, 0.08);
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: #00d4ff;
  border-radius: 1px;
  box-shadow: 0 0 10px #00d4ff;
}

.icon-link {
  padding: 10px;
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.user-btn {
  display: flex;
  align-items: center;
  font-size: 0.95rem;
  color: #94a3b8;
  padding: 8px 14px;
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.user-btn:hover {
  color: #f1f5f9;
  background: rgba(99, 102, 241, 0.08);
  border-color: rgba(99, 102, 241, 0.2);
}

.admin-link {
  color: #a78bfa;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(167, 139, 250, 0.08);
  border: 1px solid rgba(167, 139, 250, 0.2);
}

.admin-link:hover {
  color: #a78bfa;
  background: rgba(167, 139, 250, 0.15);
  border-color: rgba(167, 139, 250, 0.4);
  box-shadow: 0 0 15px rgba(167, 139, 250, 0.2);
}

.login-btn {
  background: linear-gradient(135deg, #6366f1, #00d4ff);
  border: none;
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
}

.login-btn:hover {
  box-shadow: 0 0 25px rgba(99, 102, 241, 0.5);
}

.register-btn {
  border-color: rgba(99, 102, 241, 0.3);
  color: #a5b4fc;
}

.register-btn:hover {
  border-color: #6366f1;
  color: #6366f1;
}
</style>
