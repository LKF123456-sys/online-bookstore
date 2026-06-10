<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-space justify="space-between" align="center" style="margin-bottom: 16px">
          <n-text strong>Announcement Management</n-text>
          <n-button type="primary" @click="handleAdd">Add Announcement</n-button>
        </n-space>

        <n-data-table
          :columns="columns"
          :data="announcements"
          :loading="loading"
          :pagination="pagination"
          :bordered="true"
          @update:page="handlePageChange"
        />
      </n-card>
    </n-space>

    <!-- Add/Edit Modal -->
    <n-modal
      v-model:show="showModal"
      :mask-closable="false"
      preset="dialog"
      :title="editingAnnouncement ? 'Edit Announcement' : 'Add Announcement'"
      style="width: 700px"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="100"
      >
        <n-form-item label="Title" path="title">
          <n-input v-model:value="formData.title" placeholder="Announcement title" />
        </n-form-item>
        <n-form-item label="Content" path="content">
          <n-input
            v-model:value="formData.content"
            type="textarea"
            placeholder="Announcement content"
            :rows="6"
          />
        </n-form-item>
        <n-form-item label="Status" path="status">
          <n-select
            v-model:value="formData.status"
            :options="[
              { label: 'Published', value: 1 },
              { label: 'Draft', value: 0 },
            ]"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="showModal = false">Cancel</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingAnnouncement ? 'Update' : 'Create' }}
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NSpace, NCard, NText, NButton, NDataTable, NModal,
  NForm, NFormItem, NInput, NSelect, NTag, NPopconfirm,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { announcementApi } from '@/api/announcement'
import type { AnnouncementVO, AnnouncementForm } from '@/types'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const announcements = ref<AnnouncementVO[]>([])
const showModal = ref(false)
const editingAnnouncement = ref<AnnouncementVO | null>(null)
const formRef = ref<FormInst | null>(null)

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const formData = ref<AnnouncementForm>({
  title: '',
  content: '',
  status: 1,
})

const formRules: FormRules = {
  title: [{ required: true, message: 'Please enter title', trigger: 'blur' }],
  content: [{ required: true, message: 'Please enter content', trigger: 'blur' }],
}

const columns: DataTableColumns<AnnouncementVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'Title', key: 'title', width: 250 },
  { title: 'Content', key: 'content', ellipsis: { tooltip: true } },
  {
    title: 'Status',
    key: 'status',
    width: 120,
    render: (row) => {
      const type = row.status === 1 ? 'success' : 'default'
      const text = row.status === 1 ? 'Published' : 'Draft'
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
          h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => 'Edit' }),
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

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await announcementApi.getList({
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
    })
    announcements.value = res.data.list || res.data.records || []
    pagination.value.itemCount = res.data.total || 0
  } catch (err: any) {
    message.error(err.message || 'Failed to load announcements')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadAnnouncements()
}

const handleAdd = () => {
  editingAnnouncement.value = null
  formData.value = {
    title: '',
    content: '',
    status: 1,
  }
  showModal.value = true
}

const handleEdit = (announcement: AnnouncementVO) => {
  editingAnnouncement.value = announcement
  formData.value = {
    title: announcement.title,
    content: announcement.content,
    status: announcement.status,
  }
  showModal.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (editingAnnouncement.value) {
      await announcementApi.update(editingAnnouncement.value.id, formData.value)
      message.success('Announcement updated successfully')
    } else {
      await announcementApi.create(formData.value)
      message.success('Announcement created successfully')
    }
    showModal.value = false
    loadAnnouncements()
  } catch (err: any) {
    message.error(err.message || 'Operation failed')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await announcementApi.delete(id)
    message.success('Announcement deleted successfully')
    loadAnnouncements()
  } catch (err: any) {
    message.error(err.message || 'Failed to delete announcement')
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>
