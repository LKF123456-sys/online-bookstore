<template>
  <DefaultLayout>
    <div class="page-container">
      <!-- Hero Banner -->
      <div class="hero-banner">
        <h1>欢迎来到书城</h1>
        <p>在海量图书中发现您的下一本好书</p>
        <n-button
          type="warning"
          size="large"
          style="margin-top: 16px"
          @click="$router.push('/products')"
        >
          浏览全部图书
        </n-button>
      </div>

      <!-- Announcements -->
      <n-space v-if="announcements.length > 0" vertical :size="8" style="margin-bottom: 24px">
        <n-alert
          v-for="ann in announcements"
          :key="ann.id"
          :title="ann.title"
          type="info"
          closable
        >
          {{ ann.content }}
        </n-alert>
      </n-space>

      <!-- Categories -->
      <div v-if="categories.length > 0" style="margin-bottom: 32px">
        <h2 class="section-title">图书分类</h2>
        <n-space :size="12" style="flex-wrap: wrap">
          <n-button
            v-for="cat in categories"
            :key="cat.categoryid || cat.id"
            :type="selectedCategory === (cat.categoryid || cat.id) ? 'primary' : 'default'"
            @click="goToCategory(cat.categoryid || cat.id || '')"
          >
            {{ cat.categoryname || cat.name }}
          </n-button>
        </n-space>
      </div>

      <!-- Hot Products -->
      <div style="margin-bottom: 40px">
        <h2 class="section-title">热销图书</h2>
        <n-spin :show="hotLoading">
          <n-grid
            v-if="hotProducts.length > 0"
            :x-gap="16"
            :y-gap="16"
            :cols="4"
            responsive="screen"
            item-responsive
          >
            <n-gi v-for="product in hotProducts" :key="getProductId(product)" span="4 m:2 l:1">
              <ProductCard :product="product" />
            </n-gi>
          </n-grid>
          <n-empty v-else-if="!hotLoading" description="暂无热销图书" />
        </n-spin>
      </div>

      <!-- Recommended Products -->
      <div style="margin-bottom: 40px">
        <h2 class="section-title">为您推荐</h2>
        <n-spin :show="recommendLoading">
          <n-grid
            v-if="recommendProducts.length > 0"
            :x-gap="16"
            :y-gap="16"
            :cols="4"
            responsive="screen"
            item-responsive
          >
            <n-gi v-for="product in recommendProducts" :key="getProductId(product)" span="4 m:2 l:1">
              <ProductCard :product="product" />
            </n-gi>
          </n-grid>
          <n-empty v-else-if="!recommendLoading" description="暂无推荐图书" />
        </n-spin>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import ProductCard from '@/components/ProductCard.vue'
import type { ProductVO, Category, AnnouncementVO } from '@/types'
import { getHotProducts, getRecommendProducts, getCategoryList } from '@/api/product'
import { getActiveAnnouncements } from '@/api/announcement'

const router = useRouter()

function getProductId(product: ProductVO): string | number {
  return product.productid || product.id || 0
}

const hotProducts = ref<ProductVO[]>([])
const recommendProducts = ref<ProductVO[]>([])
const categories = ref<Category[]>([])
const announcements = ref<AnnouncementVO[]>([])
const selectedCategory = ref<number | string | null>(null)
const hotLoading = ref(true)
const recommendLoading = ref(true)

function goToCategory(catId: number | string) {
  selectedCategory.value = catId
  router.push({ path: '/products', query: { category: catId } })
}

onMounted(async () => {
  // Load data in parallel
  const [hotRes, recRes, catRes, annRes] = await Promise.allSettled([
    getHotProducts(8),
    getRecommendProducts(8),
    getCategoryList(),
    getActiveAnnouncements(),
  ])

  if (hotRes.status === 'fulfilled') {
    hotProducts.value = hotRes.value || []
  }
  hotLoading.value = false

  if (recRes.status === 'fulfilled') {
    recommendProducts.value = recRes.value || []
  }
  recommendLoading.value = false

  if (catRes.status === 'fulfilled') {
    categories.value = catRes.value || []
  }

  if (annRes.status === 'fulfilled') {
    announcements.value = annRes.value || []
  }
})
</script>
