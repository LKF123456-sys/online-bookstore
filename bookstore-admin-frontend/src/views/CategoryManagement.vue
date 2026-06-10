<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-data-table
          :columns="columns"
          :data="categories"
          :loading="loading"
          :bordered="true"
        />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NSpace, NCard, NDataTable, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { categoryApi } from '@/api/category'
import type { CategoryVO } from '@/types'

const message = useMessage()
const loading = ref(false)
const categories = ref<CategoryVO[]>([])

const columns: DataTableColumns<CategoryVO> = [
  { title: 'ID', key: 'categoryid', width: 80 },
  { title: 'Name', key: 'categoryname', width: 200 },
  { title: 'Description', key: 'categorydesc' },
]

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch (err: any) {
    message.error(err.message || 'Failed to load categories')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
})
</script>
