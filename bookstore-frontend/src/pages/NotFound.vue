<template>
  <DefaultLayout>
    <div class="not-found-container">
      <n-result
        status="404"
        title="404"
        description="页面未找到"
        size="huge"
      >
        <template #footer>
          <n-button type="primary" @click="$router.push('/')">
            返回首页
          </n-button>
        </template>
      </n-result>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import DefaultLayout from '@/layouts/DefaultLayout.vue'
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
}
</style>
