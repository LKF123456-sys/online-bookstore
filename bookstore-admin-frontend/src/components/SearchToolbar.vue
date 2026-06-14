<!--
  SearchToolbar — 通用搜索/筛选工具栏组件
  封装搜索输入框、筛选下拉、新增按钮等常见管理页面顶部工具栏元素。
  用法示例：
    <SearchToolbar
      v-model:keyword="searchKeyword"
      placeholder="搜索商品..."
      show-add-button
      add-button-text="新增商品"
      @search="handleSearch"
      @add="handleAdd"
    />
-->
<template>
  <n-space justify="space-between" align="center" style="margin-bottom: 16px">
    <!-- 左侧：搜索框 + 筛选下拉（可选） -->
    <n-space align="center">
      <n-input
        v-if="showSearch"
        :value="keyword"
        :placeholder="placeholder"
        clearable
        :style="{ width: searchWidth }"
        @update:value="$emit('update:keyword', $event)"
        @keyup.enter="$emit('search')"
      >
        <template #prefix>
          <n-icon :component="SearchIcon" />
        </template>
      </n-input>

      <!-- 筛选下拉（可选，通过 slot 传入） -->
      <slot name="filters" />
    </n-space>

    <!-- 右侧：操作按钮区域（可选，通过 slot 或 props 控制） -->
    <n-space v-if="showAddButton || $slots.actions" align="center">
      <slot name="actions" />
      <n-button v-if="showAddButton" type="primary" @click="$emit('add')">
        {{ addButtonText }}
      </n-button>
    </n-space>
  </n-space>
</template>

<script setup lang="ts">
import { NSpace, NInput, NButton, NIcon } from 'naive-ui'
import { Search as SearchIcon } from '@vicons/ionicons5'

// 组件属性定义
withDefaults(defineProps<{
  /** 搜索关键词（v-model:keyword） */
  keyword?: string
  /** 搜索框占位文本 */
  placeholder?: string
  /** 搜索框宽度 */
  searchWidth?: string
  /** 是否显示搜索框 */
  showSearch?: boolean
  /** 是否显示新增按钮 */
  showAddButton?: boolean
  /** 新增按钮文本 */
  addButtonText?: string
}>(), {
  keyword: '',
  placeholder: '搜索...',
  searchWidth: '300px',
  showSearch: true,
  showAddButton: false,
  addButtonText: '新增',
})

defineEmits<{
  /** 搜索关键词变化 */
  (e: 'update:keyword', value: string): void
  /** 点击搜索或按回车 */
  (e: 'search'): void
  /** 点击新增按钮 */
  (e: 'add'): void
}>()
</script>
