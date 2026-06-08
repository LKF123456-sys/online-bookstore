<!--
  App.vue - Vue 应用根组件
  作为整个 SPA（单页应用）的根布局模板，包含：
  - 顶部导航栏（AppHeader）
  - 中间主内容区（通过 <router-view> 根据路由动态渲染不同页面组件）
  - 底部页脚（AppFooter）
-->
<template>
  <!-- #app-root：应用根容器，包裹所有顶层结构 -->
  <div id="app-root">
    <!-- AppHeader：顶部导航栏组件，通常包含 Logo、导航菜单、搜索框、用户操作等 -->
    <AppHeader />
    <!-- main.main-content：主内容区域，所有路由页面的内容在此渲染 -->
    <main class="main-content">
      <!--
        <router-view>：Vue Router 的路由出口组件
        根据当前 URL 路径匹配对应的路由，动态渲染该路由所指定的页面组件
        v-slot="{ Component }"：作用域插槽，解构获取当前要渲染的路由组件对象 Component
      -->
      <router-view v-slot="{ Component }">
        <!--
          <transition>：Vue 内置过渡组件，为路由切换提供动画效果
          name="fade"：过渡名称对应 CSS 中 .fade-enter-active 等类名
          mode="out-in"：过渡模式，"out-in" 表示先完成离开动画再执行进入动画，避免两个组件同时可见
        -->
        <transition name="fade" mode="out-in">
          <!--
            <component :is="Component" />：Vue 动态组件
            :is 绑定到路由提供的组件对象，动态渲染当前路由对应的页面组件
          -->
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <!-- AppFooter：底部页脚组件，通常包含版权信息、链接等 -->
    <AppFooter />
  </div>
</template>

<!--
  <script setup lang="ts">：Vue 3 组合式 API 语法糖
  setup 属性表示使用 <script setup> 编译时语法糖（无需手动 return）
  lang="ts" 表示脚本语言为 TypeScript
-->
<script setup lang="ts">
// 导入 AppHeader 顶部导航栏组件（来自 components/AppHeader.vue）
import AppHeader from './components/AppHeader.vue'
// 导入 AppFooter 底部页脚组件（来自 components/AppFooter.vue）
import AppFooter from './components/AppFooter.vue'
</script>

<!--
  <style scoped>：组件作用域样式
  scoped 属性确保此处定义的 CSS 仅作用于当前组件的模板，不会泄漏影响到其他组件
-->
<style scoped>
/* .main-content 主内容区：
   - min-height: calc(100vh - 150px) 最小高度为视口高度减去 150px（预留给 header + footer 的高度）
     确保即使内容较少时，页脚也能至少推到视口底部
   - padding: 32px 0 上下各 32px 内边距，为内容区提供呼吸空间 */
.main-content { min-height: calc(100vh - 150px); padding: 32px 0; }
</style>
