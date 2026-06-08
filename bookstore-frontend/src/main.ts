/**
 * main.ts - Vue 应用入口文件
 *
 * 本文件是整个 Vue 前端应用的启动入口，负责：
 * 1. 创建 Vue 应用实例
 * 2. 注册核心插件（状态管理 Pinia、路由 Vue Router）
 * 3. 导入全局样式
 * 4. 将应用挂载到 HTML 页面中的 DOM 挂载点
 */

// 从 vue 包导入 createApp 函数，用于创建 Vue 应用实例
import { createApp } from 'vue'
// 从 pinia 包导入 createPinia 函数，用于创建 Pinia 状态管理实例（Vue 的全局状态管理库）
import { createPinia } from 'pinia'
// 导入路由配置实例（来自 ./router/index.ts），包含所有路由规则定义
import router from './router'
// 导入根组件 App.vue，作为 Vue 应用的顶层组件
import App from './App.vue'
// 导入全局样式文件 style.css（CSS 变量、基础重置、工具类等全局样式）
import './style.css'

// 调用 createApp(App) 创建 Vue 应用实例，传入根组件 App
const app = createApp(App)
// app.use(createPinia()) 注册 Pinia 状态管理插件
// createPinia() 创建一个新的 Pinia 实例，作为全局状态容器注入整个应用
app.use(createPinia())
// app.use(router) 注册 Vue Router 路由插件
// 使整个应用具备前端路由能力，可通过 URL 路径切换不同页面视图
app.use(router)
// app.mount('#app') 将 Vue 应用挂载到 index.html 中 id="app" 的 DOM 元素上
// 挂载后，Vue 将接管该 DOM 节点，渲染根组件 App 的内容
app.mount('#app')
