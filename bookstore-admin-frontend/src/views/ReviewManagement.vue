<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-space style="margin-bottom: 16px" align="center">
          <n-select
            v-model:value="blockedFilter"
            :options="blockedOptions"
            placeholder="Filter by status"
            clearable
            style="width: 200px"
            @update:value="handleFilter"
          />
        </n-space>

        <n-data-table
          :columns="columns"
          :data="reviews"
          :loading="loading"
          :pagination="pagination"
          :bordered="true"
          @update:page="handlePageChange"
        />
      </n-card>
    </n-space>

    <!-- Reply Modal -->
    <n-modal
      v-model:show="showReplyModal"
      :mask-closable="false"
      preset="dialog"
      title="Reply to Review"
      style="width: 600px"
    >
      <n-input
        v-model:value="replyContent"
        type="textarea"
        placeholder="Enter your reply..."
        :rows="4"
      />
      <template #action>
        <n-button @click="showReplyModal = false">Cancel</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmitReply">
          Submit Reply
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NSpace, NCard, NSelect, NDataTable, NModal, NInput,
  NTag, NButton, NPopconfirm, NRate,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { reviewApi } from '@/api/review'
import type { ReviewVO } from '@/types'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const reviews = ref<ReviewVO[]>([])
const blockedFilter = ref<string | null>(null)
const showReplyModal = ref(false)
const replyContent = ref('')
const replyingReview = ref<ReviewVO | null>(null)

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const blockedOptions = [
  { label: 'All', value: 'all' },
  { label: 'Normal', value: 'normal' },
  { label: 'Blocked', value: 'blocked' },
]

const columns: DataTableColumns<ReviewVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'User', key: 'username', width: 120 },
  { title: 'Product', key: 'productName', width: 150 },
  {
    title: 'Rating',
    key: 'rating',
    width: 150,
    render: (row) => h(NRate, { value: row.rating, readonly: true, size: 'small' }),
  },
  { title: 'Content', key: 'content', ellipsis: { tooltip: true } },
  {
    title: 'Status',
    key: 'blocked',
    width: 100,
    render: (row) => {
      const type = row.blocked ? 'error' : 'success'
      const text = row.blocked ? 'Blocked' : 'Normal'
      return h(NTag, { type, size: 'small' }, { default: () => text })
    },
  },
  {
    title: 'Top',
    key: 'top',
    width: 80,
    render: (row) => {
      return row.top ? h(NTag, { type: 'warning', size: 'small' }, { default: () => 'Top' }) : '-'
    },
  },
  { title: 'Created', key: 'createTime', width: 180 },
  {
    title: 'Actions',
    key: 'actions',
    width: 320,
    render: (row) => {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: row.blocked ? 'success' : 'warning',
              onClick: () => handleToggleBlock(row),
            },
            { default: () => row.blocked ? 'Unblock' : 'Block' }
          ),
          h(
            NButton,
            {
              size: 'small',
              type: row.top ? 'default' : 'info',
              onClick: () => handleTop(row.id),
            },
            { default: () => row.top ? 'Untop' : 'Top' }
          ),
          h(NButton, { size: 'small', onClick: () => handleReply(row) }, { default: () => 'Reply' }),
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

const loadReviews = async () => {
  loading.value = true
  try {
    let blockedParam: boolean | undefined = undefined
    if (blockedFilter.value === 'normal') {
      blockedParam = false
    } else if (blockedFilter.value === 'blocked') {
      blockedParam = true
    }
    const res = await reviewApi.getList({
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      blocked: blockedParam,
    })
    reviews.value = res.data.list || res.data.records || []
    pagination.value.itemCount = res.data.total || 0
  } catch (err: any) {
    message.error(err.message || 'Failed to load reviews')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  pagination.value.page = 1
  loadReviews()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadReviews()
}

const handleToggleBlock = async (review: ReviewVO) => {
  try {
    if (review.blocked) {
      await reviewApi.unblock(review.id)
      message.success('Review unblocked')
    } else {
      await reviewApi.block(review.id)
      message.success('Review blocked')
    }
    loadReviews()
  } catch (err: any) {
    message.error(err.message || 'Operation failed')
  }
}

const handleTop = async (id: number) => {
  try {
    await reviewApi.top(id)
    message.success('Top status toggled')
    loadReviews()
  } catch (err: any) {
    message.error(err.message || 'Operation failed')
  }
}

const handleReply = (review: ReviewVO) => {
  replyingReview.value = review
  replyContent.value = review.reply || ''
  showReplyModal.value = true
}

const handleSubmitReply = async () => {
  if (!replyingReview.value) return

  submitting.value = true
  try {
    await reviewApi.reply(replyingReview.value.id, replyContent.value)
    message.success('Reply submitted successfully')
    showReplyModal.value = false
    loadReviews()
  } catch (err: any) {
    message.error(err.message || 'Failed to submit reply')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await reviewApi.delete(id)
    message.success('Review deleted successfully')
    loadReviews()
  } catch (err: any) {
    message.error(err.message || 'Failed to delete review')
  }
}

onMounted(() => {
  loadReviews()
})
</script>
