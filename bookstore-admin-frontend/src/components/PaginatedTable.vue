<!--
  PaginatedTable — 通用分页数据表格组件
  封装 NDataTable + 分页逻辑，避免在每个管理页面重复编写分页状态管理和加载逻辑。
  用法示例：
    <PaginatedTable
      :columns="columns"
      :data="products"
      :loading="loading"
      :pagination="pagination"
      @update:page="handlePageChange"
    />
-->
<template>
  <n-data-table
    :columns="columns"
    :data="data"
    :loading="loading"
    :pagination="pagination"
    :bordered="bordered"
    :row-key="rowKey"
    :size="size"
    remote
    @update:page="onPageChange"
    @update:page-size="onPageSizeChange"
  />
</template>

<script setup lang="ts">
import { NDataTable } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

/**
 * 分页配置接口 — 与 Naive UI 的 pagination 对象对齐
 */
export interface PaginationState {
  page: number
  pageSize: number
  itemCount: number
  showSizePicker?: boolean
  pageSizes?: number[]
}

// 组件属性定义
const props = withDefaults(defineProps<{
  /** 表格列配置 */
  columns: DataTableColumns<any>
  /** 表格数据列表 */
  data: any[]
  /** 是否正在加载 */
  loading?: boolean
  /** 分页状态对象 */
  pagination: PaginationState
  /** 是否显示边框，默认 true */
  bordered?: boolean
  /** 行唯一标识字段，用于虚拟滚动优化 */
  rowKey?: (row: any) => string | number
  /** 表格尺寸 */
  size?: 'small' | 'medium' | 'large'
}>(), {
  loading: false,
  bordered: true,
  size: 'medium',
})

const emit = defineEmits<{
  /** 页码变化时触发 */
  (e: 'update:page', page: number): void
  /** 每页数量变化时触发 */
  (e: 'update:pageSize', pageSize: number): void
}>()

/** 页码切换回调 */
const onPageChange = (page: number) => {
  emit('update:page', page)
}

/** 每页数量切换回调 */
const onPageSizeChange = (pageSize: number) => {
  emit('update:pageSize', pageSize)
  emit('update:page', 1)
}
</script>
