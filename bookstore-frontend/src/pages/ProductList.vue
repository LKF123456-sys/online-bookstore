<template>
  <DefaultLayout>
    <div class="page-container">
      <n-space vertical :size="16">
        <!-- Search Bar -->
        <n-input-group>
          <n-input
            v-model:value="keyword"
            placeholder="按书名、作者、ISBN搜索..."
            size="large"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon><SearchOutline /></n-icon>
            </template>
          </n-input>
          <n-button type="primary" size="large" @click="handleSearch">搜索</n-button>
        </n-input-group>

        <div class="product-layout">
          <!-- Sidebar Filters -->
          <div class="filter-sidebar">
            <n-card title="图书分类" size="small">
              <n-menu
                v-model:value="selectedCategory"
                :options="categoryMenuOptions"
                @update:value="handleCategoryChange"
              />
            </n-card>
          </div>

          <!-- Product Grid -->
          <div class="product-main">
            <!-- Sort & Info Bar -->
            <div class="sort-bar">
              <n-space align="center" justify="space-between">
                <span class="result-count">共找到 {{ total }} 件商品</span>
                <n-select
                  v-model:value="sortBy"
                  :options="sortOptions"
                  style="width: 180px"
                  size="small"
                  @update:value="handleSortChange"
                />
              </n-space>
            </div>

            <!-- Products -->
            <n-spin :show="loading">
              <n-grid
                v-if="products.length > 0"
                :x-gap="16"
                :y-gap="16"
                :cols="3"
                responsive="screen"
                item-responsive
              >
                <n-gi v-for="product in products" :key="product.productid || product.id" span="3 s:1">
                  <ProductCard :product="product" />
                </n-gi>
              </n-grid>
              <n-empty v-else-if="!loading" description="未找到相关商品" size="huge" />
            </n-spin>

            <!-- Pagination -->
            <div v-if="total > pageSize" class="pagination-wrap">
              <n-pagination
                v-model:page="pageNum"
                :page-count="Math.ceil(total / pageSize)"
                :page-sizes="[12, 24, 48]"
                show-size-picker
                @update:page="fetchProducts"
                @update:page-size="handlePageSizeChange"
              />
            </div>
          </div>
        </div>
      </n-space>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { SearchOutline } from '@vicons/ionicons5'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import ProductCard from '@/components/ProductCard.vue'
import type { ProductVO, Category } from '@/types'
import { getProductList, getCategoryList } from '@/api/product'

const route = useRoute()
const router = useRouter()

const products = ref<ProductVO[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const keyword = ref('')
const selectedCategory = ref<string | null>(null)
const sortBy = ref('default')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)

const sortOptions = [
  { label: '默认排序', value: 'default' },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '价格从高到低', value: 'price_desc' },
  { label: '最新上架', value: 'newest' },
  { label: '销量优先', value: 'sales' },
  { label: '评分最高', value: 'rating' },
]

const categoryMenuOptions = computed(() => {
  const options: any[] = [{ label: '全部分类', key: '__all__' }]
  categories.value.forEach(cat => {
    options.push({ label: cat.categoryname || cat.name || '未知分类', key: String(cat.categoryid || cat.id) })
  })
  return options
})

function handleSearch() {
  pageNum.value = 1
  fetchProducts()
}

function handleCategoryChange(key: string | null) {
  selectedCategory.value = key
  pageNum.value = 1
  fetchProducts()
}

function handleSortChange() {
  pageNum.value = 1
  fetchProducts()
}

function handlePageSizeChange(size: number) {
  pageSize.value = size
  pageNum.value = 1
  fetchProducts()
}

async function fetchProducts() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (keyword.value) params.keyword = keyword.value
    if (selectedCategory.value && selectedCategory.value !== '__all__') params.category = selectedCategory.value
    if (sortBy.value !== 'default') params.sort = sortBy.value

    const res = await getProductList(params)
    products.value = res.records || res.list || []
    total.value = res.total || 0
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }

  // Update URL query
  const query: Record<string, string> = {}
  if (keyword.value) query.keyword = keyword.value
  if (selectedCategory.value) query.category = selectedCategory.value
  if (sortBy.value !== 'default') query.sort = sortBy.value
  if (pageNum.value > 1) query.page = String(pageNum.value)
  router.replace({ query })
}

onMounted(async () => {
  // Load categories
  try {
    categories.value = await getCategoryList() || []
  } catch {
    categories.value = []
  }

  // Read query params
  if (route.query.keyword) keyword.value = route.query.keyword as string
  if (route.query.category) selectedCategory.value = route.query.category as string
  if (route.query.sort) sortBy.value = route.query.sort as string
  if (route.query.page) pageNum.value = Number(route.query.page)

  await fetchProducts()
})

watch(() => route.query, () => {
  if (route.query.keyword) keyword.value = route.query.keyword as string
  if (route.query.category) selectedCategory.value = route.query.category as string
  fetchProducts()
})
</script>

<style scoped>
.product-layout {
  display: flex;
  gap: 20px;
}

.filter-sidebar {
  width: 220px;
  flex-shrink: 0;
}

.product-main {
  flex: 1;
  min-width: 0;
}

.sort-bar {
  background: #fff;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.result-count {
  color: #666;
  font-size: 0.9rem;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
}
</style>
