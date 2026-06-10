<template>
  <MessageApiSetter />
  <router-view />
</template>

<script setup lang="ts">
import { defineComponent, h } from 'vue'
import { useMessage } from 'naive-ui'
import { setMessageApi } from '@/api/index'

// Declare global $message
declare global {
  interface Window {
    $message: ReturnType<typeof useMessage> | null
  }
}

const MessageApiSetter = defineComponent({
  name: 'MessageApiSetter',
  setup() {
    const message = useMessage()
    window.$message = message
    setMessageApi(message)
    return () => null
  },
})
</script>
