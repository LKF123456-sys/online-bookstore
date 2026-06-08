import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/products', name: 'products', component: () => import('../views/ProductList.vue') },
  { path: '/product/:id', name: 'product-detail', component: () => import('../views/ProductDetail.vue') },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/admin/login', name: 'admin-login', component: () => import('../views/AdminLogin.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') },
  { path: '/cart', name: 'cart', component: () => import('../views/Cart.vue') },
  { path: '/orders', name: 'orders', component: () => import('../views/OrderHistory.vue') },
  { path: '/order/:id', name: 'order-detail', component: () => import('../views/OrderDetail.vue') },
  { path: '/checkout', name: 'checkout', component: () => import('../views/Checkout.vue') },
  { path: '/reviews', name: 'reviews', component: () => import('../views/Reviews.vue') },
  { path: '/profile', name: 'profile', component: () => import('../views/Profile.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

export default router
