<template>
  <n-layout class="app-layout">
    <AppHeader />
    <n-layout-content class="main-content">
      <slot />
    </n-layout-content>
    <AppFooter />
  </n-layout>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  background: #f5f5f5;
}
</style>
