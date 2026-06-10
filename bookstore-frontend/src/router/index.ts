import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/Home.vue'),
  },
  {
    path: '/products',
    name: 'ProductList',
    component: () => import('@/pages/ProductList.vue'),
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/pages/ProductDetail.vue'),
    props: true,
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/Register.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/pages/Cart.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/pages/Checkout.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/orders',
    name: 'OrderHistory',
    component: () => import('@/pages/OrderHistory.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/pages/OrderDetail.vue'),
    meta: { requiresAuth: true },
    props: true,
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/pages/Profile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/pages/Messages.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/coupons',
    name: 'Coupons',
    component: () => import('@/pages/Coupons.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/reviews',
    name: 'MyReviews',
    component: () => import('@/pages/MyReviews.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/pages/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

// Route guards
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.guestOnly && authStore.isLoggedIn) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
