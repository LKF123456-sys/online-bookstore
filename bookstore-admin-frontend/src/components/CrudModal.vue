<!--
  CrudModal — 通用增删改弹窗组件
  封装 NModal + 标准底部操作按钮（取消/提交），统一管理页面的弹窗交互模式。
  用法示例：
    <CrudModal
      v-model:show="showModal"
      :title="editingItem ? '编辑商品' : '新增商品'"
      :loading="submitting"
      @submit="handleSubmit"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules">
        ...表单项...
      </n-form>
    </CrudModal>
-->
<template>
  <n-modal
    :show="show"
    :mask-closable="maskClosable"
    preset="dialog"
    :title="title"
    :style="{ width: width }"
    @update:show="$emit('update:show', $event)"
  >
    <!-- 表单内容区域（通过默认 slot 传入） -->
    <slot />

    <!-- 底部操作按钮 -->
    <template #action>
      <n-space justify="end">
        <n-button @click="handleCancel">{{ cancelText }}</n-button>
        <n-button
          type="primary"
          :loading="loading"
          :disabled="loading"
          @click="$emit('submit')"
        >
          {{ submitText }}
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { NModal, NButton, NSpace } from 'naive-ui'

// 组件属性定义
withDefaults(defineProps<{
  /** 弹窗是否可见（v-model:show） */
  show: boolean
  /** 弹窗标题 */
  title: string
  /** 提交按钮是否加载中 */
  loading?: boolean
  /** 弹窗宽度 */
  width?: string
  /** 是否允许点击遮罩关闭 */
  maskClosable?: boolean
  /** 取消按钮文本 */
  cancelText?: string
  /** 提交按钮文本 */
  submitText?: string
}>(), {
  loading: false,
  width: '600px',
  maskClosable: false,
  cancelText: '取消',
  submitText: '确定',
})

const emit = defineEmits<{
  /** 弹窗可见性变化 */
  (e: 'update:show', value: boolean): void
  /** 点击提交按钮 */
  (e: 'submit'): void
  /** 点击取消按钮 */
  (e: 'cancel'): void
}>()

/** 取消按钮回调 — 关闭弹窗并触发 cancel 事件 */
const handleCancel = () => {
  emit('update:show', false)
  emit('cancel')
}
</script>
