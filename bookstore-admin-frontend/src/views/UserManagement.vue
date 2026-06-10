<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-space style="margin-bottom: 16px" align="center">
          <n-input
            v-model:value="searchKeyword"
            placeholder="Search users..."
            clearable
            style="width: 300px"
            @update:value="handleSearch"
          />
        </n-space>

        <n-data-table
          :columns="columns"
          :data="users"
          :loading="loading"
          :pagination="pagination"
          :bordered="true"
          @update:page="handlePageChange"
        />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NSpace, NCard, NInput, NDataTable, NTag, NButton, NPopconfirm,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { userApi } from '@/api/user'
import type { UserVO } from '@/types'

const message = useMessage()
const loading = ref(false)
const users = ref<UserVO[]>([])
const searchKeyword = ref('')

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const columns: DataTableColumns<UserVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'Username', key: 'username', width: 150 },
  { title: 'Nickname', key: 'nickname', width: 150 },
  { title: 'Email', key: 'email', width: 200 },
  { title: 'Phone', key: 'phone', width: 150 },
  {
    title: 'Role',
    key: 'role',
    width: 100,
    render: (row) => {
      const type = row.role === 'admin' ? 'error' : 'default'
      return h(NTag, { type, size: 'small' }, { default: () => row.role })
    },
  },
  {
    title: 'Status',
    key: 'status',
    width: 100,
    render: (row) => {
      const type = row.status === 1 ? 'success' : 'error'
      const text = row.status === 1 ? 'Active' : 'Disabled'
      return h(NTag, { type, size: 'small' }, { default: () => text })
    },
  },
  { title: 'Created', key: 'createTime', width: 180 },
  {
    title: 'Actions',
    key: 'actions',
    width: 200,
    render: (row) => {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: row.status === 1 ? 'warning' : 'success',
              onClick: () => handleToggleStatus(row),
            },
            { default: () => row.status === 1 ? 'Disable' : 'Enable' }
          ),
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row.id) },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => 'Delete' }),
              default: () => 'Are you sure?',
            }
          ),
        ],
      })
    },
  },
]

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.getList({
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value,
    })
    users.value = res.data.list || res.data.records || []
    pagination.value.itemCount = res.data.total || 0
  } catch (err: any) {
    message.error(err.message || 'Failed to load users')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadUsers()
}

const handleToggleStatus = async (user: UserVO) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    await userApi.updateStatus(user.id, newStatus)
    message.success('Status updated successfully')
    loadUsers()
  } catch (err: any) {
    message.error(err.message || 'Failed to update status')
  }
}

const handleDelete = async (id: number) => {
  try {
    await userApi.delete(id)
    message.success('User deleted successfully')
    loadUsers()
  } catch (err: any) {
    message.error(err.message || 'Failed to delete user')
  }
}

onMounted(() => {
  loadUsers()
})
</script>
