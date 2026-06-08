/**
 * vite.config.ts - Vite 构建工具配置文件
 *
 * Vite 是下一代前端构建工具，基于原生 ES Module 实现极速冷启动和热更新（HMR）。
 * 本文件定义 Vite 构建和开发服务器的各项配置。
 */

// 从 vite 包导入 defineConfig 函数，提供 TypeScript 类型推导和智能提示的配置定义辅助函数
import { defineConfig } from 'vite'
// 从 @vitejs/plugin-vue 包导入 vue 插件，用于让 Vite 支持解析和编译 .vue 单文件组件（SFC）
import vue from '@vitejs/plugin-vue'

// 使用 defineConfig 导出配置对象，获取完整的 TypeScript 类型支持
export default defineConfig({
  // plugins 插件数组：注册 Vite 插件
  // vue() 是 Vite 官方的 Vue 3 插件，负责 .vue 文件的编译（template/script/style 三部分解析）
  plugins: [vue()],

  // server 开发服务器配置：仅在 npm run dev 时生效
  server: {
    // port: 3000 指定开发服务器监听端口为 3000
    // 访问地址为 http://localhost:3000
    port: 3000,

    // proxy 代理配置：用于开发环境下将特定路径的请求转发到后端服务器，解决跨域问题
    proxy: {
      // '/api' 路径代理规则：所有以 /api 开头的请求（如 /api/books、/api/orders）
      '/api': {
        // target: 代理目标地址，将匹配的请求转发到本地 8086 端口的后端服务
        target: 'http://localhost:8086',
        // changeOrigin: true 修改请求头中的 Host 字段为目标服务器的地址
        // 防止后端因 Host 头不匹配而拒绝请求（某些服务器会校验 Host 头）
        changeOrigin: true
      },
      // '/img' 路径代理规则：所有以 /img 开头的请求（如 /img/covers/xxx.jpg 图片资源）
      '/img': {
        // target: 同样转发到本地 8086 端口的后端服务（后端负责提供静态图片资源）
        target: 'http://localhost:8086',
        // changeOrigin: true 同上，修改 Host 头避免后端因来源校验而拒绝
        changeOrigin: true
      }
    }
  }
})
