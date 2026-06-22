import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/admin/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/admin',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/ProductManagement.vue'),
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/OrderManagement.vue'),
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/UserManagement.vue'),
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/CategoryManagement.vue'),
      },
      {
        path: 'coupons',
        name: 'Coupons',
        component: () => import('@/views/CouponManagement.vue'),
      },
      {
        path: 'reviews',
        name: 'Reviews',
        component: () => import('@/views/ReviewManagement.vue'),
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/AnnouncementManagement.vue'),
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/MessageManagement.vue'),
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/LogManagement.vue'),
      },
      {
        path: 'api-docs',
        name: 'ApiDocs',
        component: () => import('@/views/ApiDocs.vue'),
      },
      {
        path: 'gray-release',
        name: 'GrayRelease',
        component: () => import('@/views/GrayReleaseManagement.vue'),
      },
    ],
  },
  {
    path: '/',
    redirect: '/admin/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Route guards
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth !== false && !authStore.isAuthenticated()) {
    next('/admin/login')
  } else if (to.path === '/admin/login' && authStore.isAuthenticated()) {
    next('/admin/dashboard')
  } else {
    next()
  }
})

export default router
