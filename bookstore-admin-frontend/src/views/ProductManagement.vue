<template>
  <div>
    <n-space vertical size="large">
      <n-card>
        <n-space justify="space-between" align="center" style="margin-bottom: 16px">
          <n-input
            v-model:value="searchKeyword"
            placeholder="Search products..."
            clearable
            style="width: 300px"
            @update:value="handleSearch"
          />
          <n-button type="primary" @click="handleAdd">Add Product</n-button>
        </n-space>

        <n-data-table
          :columns="columns"
          :data="products"
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
      :title="editingProduct ? 'Edit Product' : 'Add Product'"
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
          <n-input v-model:value="formData.name" placeholder="Product name" />
        </n-form-item>
        <n-form-item label="Author" path="author">
          <n-input v-model:value="formData.author" placeholder="Author" />
        </n-form-item>
        <n-form-item label="Price" path="price">
          <n-input-number v-model:value="formData.price" :min="0" :precision="2" style="width: 100%" />
        </n-form-item>
        <n-form-item label="Stock" path="stock">
          <n-input-number v-model:value="formData.stock" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="Category" path="category">
          <n-select
            v-model:value="formData.category"
            :options="categoryOptions"
            placeholder="Select category"
          />
        </n-form-item>
        <n-form-item label="Cover URL" path="image">
          <n-input v-model:value="formData.image" placeholder="Cover image URL" />
        </n-form-item>
        <n-form-item label="Description" path="description">
          <n-input
            v-model:value="formData.description"
            type="textarea"
            placeholder="Description"
            :rows="3"
          />
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
          {{ editingProduct ? 'Update' : 'Create' }}
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import {
  NSpace, NCard, NInput, NButton, NDataTable, NModal,
  NForm, NFormItem, NInputNumber, NSelect, NTag, NPopconfirm,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules, SelectOption } from 'naive-ui'
import { productApi } from '@/api/product'
import { categoryApi } from '@/api/category'
import type { ProductVO, ProductForm, CategoryVO } from '@/types'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const products = ref<ProductVO[]>([])
const categories = ref<CategoryVO[]>([])
const searchKeyword = ref('')
const showModal = ref(false)
const editingProduct = ref<ProductVO | null>(null)
const formRef = ref<FormInst | null>(null)

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const formData = ref<ProductForm>({
  name: '',
  author: '',
  price: 0,
  stock: 0,
  category: '',
  image: '',
  description: '',
  status: 1,
})

const formRules: FormRules = {
  name: [{ required: true, message: 'Please enter product name', trigger: 'blur' }],
  author: [{ required: true, message: 'Please enter author', trigger: 'blur' }],
  price: [{ required: true, type: 'number', message: 'Please enter price', trigger: 'blur' }],
  stock: [{ required: true, type: 'number', message: 'Please enter stock', trigger: 'blur' }],
  category: [{ required: true, message: 'Please select category', trigger: 'change' }],
}

const categoryOptions = computed<SelectOption[]>(() =>
  categories.value.map(c => ({ label: c.name || c.categoryname, value: c.categoryid || c.id }))
)

const columns: DataTableColumns<ProductVO> = [
  { title: 'ID', key: 'productid', width: 80 },
  { title: 'Name', key: 'name', width: 200 },
  { title: 'Author', key: 'author', width: 150 },
  { title: 'Price', key: 'price', width: 100, render: (row) => `¥${row.price}` },
  { title: 'Stock', key: 'stock', width: 100 },
  { title: 'Sales', key: 'sales', width: 80 },
  { title: 'Category', key: 'category', width: 120 },
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
            { onPositiveClick: () => handleDelete(row.productid || row.id as any) },
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

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await productApi.getList({
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value,
    })
    products.value = res.data.list || res.data.records || []
    pagination.value.itemCount = res.data.total || 0
  } catch (err: any) {
    message.error(err.message || 'Failed to load products')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch (err) {
    console.error('Failed to load categories:', err)
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadProducts()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadProducts()
}

const handleAdd = () => {
  editingProduct.value = null
  formData.value = {
    name: '',
    author: '',
    price: 0,
    stock: 0,
    category: '',
    image: '',
    description: '',
    status: 1,
  }
  showModal.value = true
}

const handleEdit = (product: ProductVO) => {
  editingProduct.value = product
  formData.value = {
    name: product.name,
    author: product.author,
    price: product.price,
    stock: product.stock,
    category: product.category,
    image: product.image || '',
    description: product.description || product.descn || '',
    status: product.status,
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
    if (editingProduct.value) {
      await productApi.update(editingProduct.value.productid || editingProduct.value.id as any, formData.value)
      message.success('Product updated successfully')
    } else {
      await productApi.create(formData.value)
      message.success('Product created successfully')
    }
    showModal.value = false
    loadProducts()
  } catch (err: any) {
    message.error(err.message || 'Operation failed')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (product: ProductVO) => {
  try {
    const newStatus = product.status === 1 ? 0 : 1
    await productApi.updateStatus(product.productid || product.id as any, newStatus)
    message.success('Status updated successfully')
    loadProducts()
  } catch (err: any) {
    message.error(err.message || 'Failed to update status')
  }
}

const handleDelete = async (id: string) => {
  try {
    await productApi.delete(id)
    message.success('Product deleted successfully')
    loadProducts()
  } catch (err: any) {
    message.error(err.message || 'Failed to delete product')
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>
