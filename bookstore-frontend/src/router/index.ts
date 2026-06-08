/**
 * Vue Router 路由配置
 * =====================
 * 使用 createWebHistory 创建 HTML5 History 模式路由
 * 所有路由组件均采用懒加载（动态 import），按需加载以优化首屏性能
 */

import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由表定义
 * 每个路由对象包含：
 * - path：URL 路径
 * - name：路由名称，用于编程式导航
 * - component：懒加载的 Vue 组件（() => import(...) 返回 Promise<VueComponent>）
 */
const routes = [
  // 首页 — 图书列表首页
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },

  // 图书列表页 — 展示全部图书
  { path: '/products', name: 'products', component: () => import('../views/ProductList.vue') },

  // 图书详情页 — 动态路由参数 :id 表示图书 ID
  { path: '/product/:id', name: 'product-detail', component: () => import('../views/ProductDetail.vue') },

  // 用户登录页 — 普通用户登录入口
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },

  // 管理员登录页 — 管理后台登录入口
  { path: '/admin/login', name: 'admin-login', component: () => import('../views/AdminLogin.vue') },

  // 用户注册页
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') },

  // 购物车页
  { path: '/cart', name: 'cart', component: () => import('../views/Cart.vue') },

  // 订单列表页 — 查看历史订单
  { path: '/orders', name: 'orders', component: () => import('../views/OrderHistory.vue') },

  // 订单详情页 — 动态路由参数 :id 表示订单 ID
  { path: '/order/:id', name: 'order-detail', component: () => import('../views/OrderDetail.vue') },

  // 结算/下单页
  { path: '/checkout', name: 'checkout', component: () => import('../views/Checkout.vue') },

  // 评价管理页
  { path: '/reviews', name: 'reviews', component: () => import('../views/Reviews.vue') },

  // 个人资料页
  { path: '/profile', name: 'profile', component: () => import('../views/Profile.vue') },
]

/**
 * createRouter 创建路由实例
 * - history: createWebHistory() 使用 HTML5 History API，URL 不包含 # 号
 * - routes: 挂载路由表
 * - scrollBehavior: 每次路由切换后滚动到页面顶部 ({ top: 0 })
 */
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

export default router
