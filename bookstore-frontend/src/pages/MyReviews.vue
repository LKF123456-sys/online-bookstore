<template>
  <DefaultLayout>
    <div class="page-container">
      <h1 class="section-title">我的评价</h1>

      <n-card>
        <n-spin :show="loading">
          <div v-if="reviews.length > 0" class="reviews-list">
            <n-card
              v-for="review in reviews"
              :key="review.id"
              class="review-card"
              size="small"
            >
              <div class="review-product">
                <n-image
                  :src="review.productImage || review.productimage || placeholderImage"
                  :fallback-src="placeholderImage"
                  width="60"
                  height="80"
                  object-fit="cover"
                  preview-disabled
                  lazy
                  style="border-radius: 4px; flex-shrink: 0"
                />
                <div class="review-product-info">
                  <h3
                    class="product-title-link"
                    @click="$router.push(`/product/${review.productid || review.productId}`)"
                  >
                    {{ review.productTitle || review.producttitle || review.product?.name || review.product?.title || '商品' }}
                  </h3>
                  <n-rate :value="review.rating" readonly size="small" />
                  <span class="review-date">{{ formatDate(review.createdAt || review.created_at || '') }}</span>
                </div>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </n-card>
          </div>

          <n-empty v-else-if="!loading" description="您还没有发表过评价" size="huge">
            <template #extra>
              <n-button type="primary" @click="$router.push('/orders')">查看订单</n-button>
            </template>
          </n-empty>
        </n-spin>

        <!-- Pagination -->
        <div v-if="total > pageSize" class="pagination-wrap">
          <n-pagination
            v-model:page="pageNum"
            :page-count="Math.ceil(total / pageSize)"
            @update:page="fetchReviews"
          />
        </div>
      </n-card>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import type { ReviewVO } from '@/types'
import { getMyReviews } from '@/api/review'

const loading = ref(false)
const reviews = ref<ReviewVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const placeholderImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="80" fill="%23ddd"%3E%3Crect width="60" height="80"/%3E%3C/svg%3E'

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

async function fetchReviews() {
  loading.value = true
  try {
    const res = await getMyReviews(pageNum.value, pageSize.value)
    reviews.value = res.records || res.list || []
    total.value = res.total || 0
  } catch {
    reviews.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-card {
  transition: box-shadow 0.2s;
}

.review-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.review-product {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.review-product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-title-link {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  margin: 0;
}

.product-title-link:hover {
  color: #18a058;
}

.review-date {
  color: #999;
  font-size: 0.85rem;
}

.review-content {
  color: #555;
  line-height: 1.6;
  padding-left: 72px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
