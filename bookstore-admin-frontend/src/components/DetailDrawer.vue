<!--
  DetailDrawer — 通用详情抽屉组件
  封装 NDrawer + NDescriptions，以键值对方式展示对象详情，替代各管理页面中重复的抽屉代码。
  用法示例：
    <DetailDrawer
      v-model:show="showDrawer"
      title="订单详情"
      :data="selectedOrder"
      :fields="[
        { label: '订单号', key: 'orderNo' },
        { label: '金额', key: 'totalAmount', render: (val) => `¥${val}` },
        { label: '状态', key: 'status', render: (val) => h(NTag, ...) },
      ]"
    >
      <template #extra>
        额外的内容（如子表格）
      </template>
    </DetailDrawer>
-->
<template>
  <n-drawer v-model:show="show" :width="width" placement="right" @update:show="$emit('update:show', $event)">
    <n-drawer-content :title="title" closable>
      <!-- 详情描述列表 -->
      <n-descriptions v-if="data" bordered :column="column" label-placement="left">
        <n-descriptions-item
          v-for="field in fields"
          :key="field.key"
          :label="field.label"
        >
          <!-- 自定义渲染函数 -->
          <template v-if="field.render">
            <component :is="() => field.render!(getFieldValue(field.key))" />
          </template>
          <!-- 默认文本渲染 -->
          <template v-else>
            {{ formatValue(getFieldValue(field.key), field.format) }}
          </template>
        </n-descriptions-item>
      </n-descriptions>

      <!-- 额外内容区域（通过 slot 传入，如子表格、操作按钮等） -->
      <slot name="extra" />
    </n-drawer-content>
  </n-drawer>
</template>

<script setup lang="ts">
import { NDrawer, NDrawerContent, NDescriptions, NDescriptionsItem } from 'naive-ui'
import type { VNode } from 'vue'

/**
 * 字段配置接口
 */
export interface DetailField {
  /** 显示标签 */
  label: string
  /** 数据中的字段名（支持点号嵌套，如 'user.name'） */
  key: string
  /** 自定义渲染函数 */
  render?: (value: any) => VNode | string
  /** 格式化函数 */
  format?: (value: any) => string
}

const props = withDefaults(defineProps<{
  /** 抽屉是否可见（v-model:show） */
  show: boolean
  /** 抽屉标题 */
  title: string
  /** 数据对象 */
  data: Record<string, any> | null
  /** 字段配置列表 */
  fields: DetailField[]
  /** 抽屉宽度 */
  width?: number | string
  /** 描述列表列数 */
  column?: number
}>(), {
  width: 600,
  column: 1,
})

defineEmits<{
  (e: 'update:show', value: boolean): void
}>()

/**
 * 从数据对象中获取字段值，支持点号分隔的嵌套路径
 * 例如: 'user.address.city' 会访问 data.user.address.city
 */
const getFieldValue = (key: string): any => {
  if (!props.data) return '-'
  const keys = key.split('.')
  let value: any = props.data
  for (const k of keys) {
    value = value?.[k]
  }
  return value
}

/**
 * 格式化值 — 如果提供了 format 函数则使用，否则直接返回值的字符串形式
 */
const formatValue = (value: any, format?: (val: any) => string): string => {
  if (value === null || value === undefined) return '-'
  if (format) return format(value)
  return String(value)
}
</script>
