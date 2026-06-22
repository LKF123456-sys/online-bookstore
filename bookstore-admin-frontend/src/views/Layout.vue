<template>
  <n-layout has-sider style="height: 100vh">
    <n-layout-sider
      bordered
      :collapsed="collapsed"
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      :native-scrollbar="false"
      style="height: 100vh"
    >
      <div class="logo" :class="{ 'logo-collapsed': collapsed }">
        <div class="logo-icon">
          <n-icon size="20">
            <BookOutline />
          </n-icon>
        </div>
        <span v-if="!collapsed">BookVerse</span>
        <span v-else>BV</span>
      </div>
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuClick"
      />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered style="height: 64px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between;">
        <h2 style="margin: 0; font-size: 18px;">{{ currentPageTitle }}</h2>
        <n-space align="center">
          <n-button type="info" size="small" @click="goToFrontend">返回前台</n-button>
          <n-text>{{ authStore.user?.nickname || authStore.user?.username || 'Admin' }}</n-text>
          <n-button type="error" size="small" @click="handleLogout">退出登录</n-button>
        </n-space>
      </n-layout-header>
      <n-layout-content
        content-style="padding: 24px;"
        :native-scrollbar="false"
        style="height: calc(100vh - 64px)"
      >
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NLayout, NLayoutSider, NLayoutHeader, NLayoutContent,
  NMenu, NButton, NSpace, NText,
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { NIcon } from 'naive-ui'
import {
  SpeedometerOutline,
  BookOutline,
  CartOutline,
  PeopleOutline,
  ListOutline,
  TicketOutline,
  ChatbubblesOutline,
  MegaphoneOutline,
  MailOutline,
  CodeSlashOutline,
  DocumentTextOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const collapsed = ref(false)

const renderIcon = (icon: any) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions: MenuOption[] = [
  { label: '仪表盘', key: 'dashboard', icon: renderIcon(SpeedometerOutline) },
  { label: '商品管理', key: 'products', icon: renderIcon(BookOutline) },
  { label: '订单管理', key: 'orders', icon: renderIcon(CartOutline) },
  { label: '用户管理', key: 'users', icon: renderIcon(PeopleOutline) },
  { label: '分类管理', key: 'categories', icon: renderIcon(ListOutline) },
  { label: '优惠券管理', key: 'coupons', icon: renderIcon(TicketOutline) },
  { label: '评价管理', key: 'reviews', icon: renderIcon(ChatbubblesOutline) },
  { label: '公告管理', key: 'announcements', icon: renderIcon(MegaphoneOutline) },
  { label: '消息管理', key: 'messages', icon: renderIcon(MailOutline) },
  { label: '操作日志', key: 'logs', icon: renderIcon(DocumentTextOutline) },
  { label: 'API 文档', key: 'api-docs', icon: renderIcon(CodeSlashOutline) },
]

const activeKey = computed(() => {
  const path = route.path.replace('/admin/', '')
  return path || 'dashboard'
})

const pageTitles: Record<string, string> = {
  dashboard: '仪表盘',
  products: '商品管理',
  orders: '订单管理',
  users: '用户管理',
  categories: '分类管理',
  coupons: '优惠券管理',
  reviews: '评价管理',
  announcements: '公告管理',
  messages: '消息管理',
  logs: '操作日志',
  api-docs: 'API 文档',
}

const currentPageTitle = computed(() => {
  return pageTitles[activeKey.value] || 'Dashboard'
})

const handleMenuClick = (key: string) => {
  router.push(`/admin/${key}`)
}

const handleLogout = () => {
  authStore.logout()
  router.push('/admin/login')
}

// 返回用户前台：开发环境端口5173，生产环境端口80（默认）
const goToFrontend = () => {
  const { protocol, hostname } = window.location
  const isDev = import.meta.env.DEV
  const port = isDev ? ':5173' : ''
  window.location.href = `${protocol}//${hostname}${port}/`
}
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #f1f5f9, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom: 1px solid rgba(99, 102, 241, 0.2);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1, #00d4ff);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
}

.logo-collapsed .logo-icon {
  width: 32px;
  height: 32px;
}

.logo-collapsed {
  font-size: 16px;
}
</style>
