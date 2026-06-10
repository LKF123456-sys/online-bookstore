<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-space justify="space-between" align="center" style="margin-bottom: 16px">
          <n-text strong>Coupon Management</n-text>
          <n-button type="primary" @click="handleAdd">Add Coupon</n-button>
        </n-space>

        <n-data-table
          :columns="columns"
          :data="coupons"
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
      :title="editingCoupon ? 'Edit Coupon' : 'Add Coupon'"
      style="width: 600px"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="120"
      >
        <n-form-item label="Name" path="name">
          <n-input v-model:value="formData.name" placeholder="Coupon name" />
        </n-form-item>
        <n-form-item label="Type" path="type">
          <n-select
            v-model:value="formData.type"
            :options="[
              { label: 'Fixed Amount', value: 1 },
              { label: 'Percentage', value: 2 },
            ]"
          />
        </n-form-item>
        <n-form-item label="Discount" path="discount">
          <n-input-number v-model:value="formData.discount" :min="0" :precision="2" style="width: 100%" />
        </n-form-item>
        <n-form-item label="Min Amount" path="minAmount">
          <n-input-number v-model:value="formData.minAmount" :min="0" :precision="2" style="width: 100%" />
        </n-form-item>
        <n-form-item label="Start Time" path="startTime">
          <n-date-picker v-model:formatted-value="formData.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
        </n-form-item>
        <n-form-item label="End Time" path="endTime">
          <n-date-picker v-model:formatted-value="formData.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
        </n-form-item>
        <n-form-item label="Total Count" path="totalCount">
          <n-input-number v-model:value="formData.totalCount" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="Status" path="status">
          <n-select
            v-model:value="formData.status"
            :options="[
              { label: 'Active', value: 1 },
              { label: 'Inactive', value: 0 },
            ]"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="showModal = false">Cancel</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingCoupon ? 'Update' : 'Create' }}
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NSpace, NCard, NText, NButton, NDataTable, NModal,
  NForm, NFormItem, NInput, NInputNumber, NSelect, NDatePicker, NTag, NPopconfirm,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { couponApi } from '@/api/coupon'
import type { CouponVO, CouponForm } from '@/types'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const coupons = ref<CouponVO[]>([])
const showModal = ref(false)
const editingCoupon = ref<CouponVO | null>(null)
const formRef = ref<FormInst | null>(null)

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const formData = ref<CouponForm>({
  name: '',
  type: 1,
  discount: 0,
  minAmount: 0,
  startTime: '',
  endTime: '',
  totalCount: 0,
  status: 1,
})

const formRules: FormRules = {
  name: [{ required: true, message: 'Please enter coupon name', trigger: 'blur' }],
  type: [{ required: true, type: 'number', message: 'Please select type', trigger: 'change' }],
  discount: [{ required: true, type: 'number', message: 'Please enter discount', trigger: 'blur' }],
  minAmount: [{ required: true, type: 'number', message: 'Please enter min amount', trigger: 'blur' }],
  totalCount: [{ required: true, type: 'number', message: 'Please enter total count', trigger: 'blur' }],
}

const columns: DataTableColumns<CouponVO> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: 'Name', key: 'name', width: 150 },
  {
    title: 'Type',
    key: 'type',
    width: 120,
    render: (row) => row.type === 1 ? 'Fixed' : 'Percentage',
  },
  { title: 'Discount', key: 'discount', width: 100 },
  { title: 'Min Amount', key: 'minAmount', width: 120 },
  { title: 'Remain', key: 'remainCount', width: 100 },
  {
    title: 'Status',
    key: 'status',
    width: 100,
    render: (row) => {
      const type = row.status === 1 ? 'success' : 'error'
      const text = row.status === 1 ? 'Active' : 'Inactive'
      return h(NTag, { type, size: 'small' }, { default: () => text })
    },
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 240,
    render: (row) => {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => 'Edit' }),
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

const loadCoupons = async () => {
  loading.value = true
  try {
    const res = await couponApi.getList({
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
    })
    coupons.value = res.data.list || res.data.records || []
    pagination.value.itemCount = res.data.total || 0
  } catch (err: any) {
    message.error(err.message || 'Failed to load coupons')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadCoupons()
}

const handleAdd = () => {
  editingCoupon.value = null
  formData.value = {
    name: '',
    type: 1,
    discount: 0,
    minAmount: 0,
    startTime: '',
    endTime: '',
    totalCount: 0,
    status: 1,
  }
  showModal.value = true
}

const handleEdit = (coupon: CouponVO) => {
  editingCoupon.value = coupon
  formData.value = {
    name: coupon.name,
    type: coupon.type,
    discount: coupon.discount,
    minAmount: coupon.minAmount,
    startTime: coupon.startTime,
    endTime: coupon.endTime,
    totalCount: coupon.totalCount,
    status: coupon.status,
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
    if (editingCoupon.value) {
      await couponApi.update(editingCoupon.value.id, formData.value)
      message.success('Coupon updated successfully')
    } else {
      await couponApi.create(formData.value)
      message.success('Coupon created successfully')
    }
    showModal.value = false
    loadCoupons()
  } catch (err: any) {
    message.error(err.message || 'Operation failed')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (coupon: CouponVO) => {
  try {
    const newStatus = coupon.status === 1 ? 0 : 1
    await couponApi.updateStatus(coupon.id, newStatus)
    message.success('Status updated successfully')
    loadCoupons()
  } catch (err: any) {
    message.error(err.message || 'Failed to update status')
  }
}

const handleDelete = async (id: number) => {
  try {
    await couponApi.delete(id)
    message.success('Coupon deleted successfully')
    loadCoupons()
  } catch (err: any) {
    message.error(err.message || 'Failed to delete coupon')
  }
}

onMounted(() => {
  loadCoupons()
})
</script>
