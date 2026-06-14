<!--
  StatusTag — 通用状态标签组件
  将状态数值转换为带颜色的 NTag 标签，消除各管理页面中重复的 render 函数。
  用法示例：
    <StatusTag :status="product.status" />
    <StatusTag :status="user.status" active-text="正常" inactive-text="禁用" />
    <StatusTag :status="order.status" :type-map="orderTypeMap" :text-map="orderTextMap" />
-->
<template>
  <n-tag :type="tagType" size="small">
    {{ tagText }}
  </n-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NTag } from 'naive-ui'
import type { TagProps } from 'naive-ui'

const props = withDefaults(defineProps<{
  /** 状态值（数字或字符串） */
  status: number | string
  /** 激活状态对应的文本 */
  activeText?: string
  /** 非激活状态对应的文本 */
  inactiveText?: string
  /** 激活状态的判定值，默认为 1 */
  activeValue?: number | string
  /** 自定义状态值 -> Tag 颜色映射 */
  typeMap?: Record<string | number, TagProps['type']>
  /** 自定义状态值 -> 显示文本映射 */
  textMap?: Record<string | number, string>
}>(), {
  activeText: '启用',
  inactiveText: '禁用',
  activeValue: 1,
})

/** 计算标签颜色类型 */
const tagType = computed<TagProps['type']>(() => {
  if (props.typeMap && props.status in props.typeMap) {
    return props.typeMap[props.status] || 'default'
  }
  return props.status === props.activeValue ? 'success' : 'error'
})

/** 计算标签显示文本 */
const tagText = computed<string>(() => {
  if (props.textMap && props.status in props.textMap) {
    return props.textMap[props.status] || String(props.status)
  }
  return props.status === props.activeValue ? props.activeText : props.inactiveText
})
</script>
