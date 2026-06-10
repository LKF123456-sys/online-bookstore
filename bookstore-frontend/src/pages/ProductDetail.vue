<template>
  <DefaultLayout>
    <div class="page-container">
      <n-spin :show="loading">
        <template v-if="product">
          <n-breadcrumb style="margin-bottom: 16px">
            <n-breadcrumb-item @click="$router.push('/')">首页</n-breadcrumb-item>
            <n-breadcrumb-item @click="$router.push('/products')">商品</n-breadcrumb-item>
            <n-breadcrumb-item>{{ product.name || product.title }}</n-breadcrumb-item>
          </n-breadcrumb>

          <n-card>
            <div class="product-detail-layout">
              <!-- Product Image -->
              <div class="product-image-section">
                <n-image
                  :src="product.image || placeholderImage"
                  :fallback-src="placeholderImage"
                  object-fit="contain"
                  width="100%"
                  style="max-height: 400px"
                  preview-disabled
                />
              </div>

              <!-- Product Info -->
              <div class="product-info-section">
                <h1 class="product-title">{{ product.name || product.title }}</h1>
                <p class="product-author">作者：{{ product.author || '未知作者' }}</p>

                <n-space vertical :size="12" style="margin-top: 16px">
                  <n-descriptions :column="2" bordered size="small">
                    <n-descriptions-item label="ISBN">{{ product.isbn || '-' }}</n-descriptions-item>
                    <n-descriptions-item label="出版社">{{ product.publisher || '-' }}</n-descriptions-item>
                    <n-descriptions-item label="分类">
                      <n-tag size="small">{{ product.categoryName || product.categoryname || product.category || '-' }}</n-tag>
                    </n-descriptions-item>
                    <n-descriptions-item label="库存">
                      <n-tag :type="product.stock > 0 ? 'success' : 'error'" size="small">
                        {{ product.stock > 0 ? `${product.stock} 件可售` : '已售罄' }}
                      </n-tag>
                    </n-descriptions-item>
                    <n-descriptions-item label="销量">{{ product.sales || 0 }}</n-descriptions-item>
                    <n-descriptions-item label="评分">
                      <n-rate :value="product.rating || 0" readonly size="small" allow-half />
                      <span style="margin-left: 8px; color: #999">({{ product.ratingCount || product.rating_count || 0 }} 条评价)</span>
                    </n-descriptions-item>
                  </n-descriptions>

                  <!-- Price -->
                  <div class="price-section">
                    <span class="price">${{ displayPrice }}</span>
                    <span v-if="hasDiscount" class="price-original">${{ product.price.toFixed(2) }}</span>
                    <n-tag v-if="hasDiscount" type="error" size="small" style="margin-left: 12px">
                      省 ${{ (product.price - (product.salePrice || 0)).toFixed(2) }}
                    </n-tag>
                  </div>

                  <!-- Quantity -->
                  <n-space align="center" :size="12">
                    <span>数量：</span>
                    <n-input-number
                      v-model:value="quantity"
                      :min="1"
                      :max="product.stock"
                      style="width: 120px"
                    />
                  </n-space>

                  <!-- Actions -->
                  <n-space :size="12">
                    <n-button
                      type="primary"
                      size="large"
                      :loading="addingToCart"
                      :disabled="product.stock === 0"
                      @click="handleAddToCart"
                    >
                      {{ product.stock === 0 ? '已售罄' : '加入购物车' }}
                    </n-button>
                    <n-button
                      type="warning"
                      size="large"
                      :loading="buyingNow"
                      :disabled="product.stock === 0"
                      @click="handleBuyNow"
                    >
                      立即购买
                    </n-button>
                  </n-space>
                </n-space>
              </div>
            </div>
          </n-card>

          <!-- Description -->
          <n-card title="商品描述" style="margin-top: 16px">
            <div class="product-description" v-html="product.descn || product.description || '暂无描述。'"></div>
          </n-card>

          <!-- Reviews Section -->
          <n-card title="用户评价" style="margin-top: 16px">
            <template #header-extra>
              <n-button v-if="authStore.isLoggedIn" size="small" @click="showReviewForm = !showReviewForm">
                写评价
              </n-button>
            </template>

            <!-- Review Form -->
            <div v-if="showReviewForm" class="review-form">
              <n-form>
                <n-form-item label="评分">
                  <n-rate v-model:value="reviewForm.rating" />
                </n-form-item>
                <n-form-item label="评价内容">
                  <n-input
                    v-model:value="reviewForm.content"
                    type="textarea"
                    :rows="3"
                    placeholder="分享您对这本书的看法..."
                  />
                </n-form-item>
                <n-button type="primary" :loading="submittingReview" @click="submitReview">
                  提交评价
                </n-button>
              </n-form>
              <n-divider />
            </div>

            <!-- Reviews List -->
            <n-spin :show="reviewsLoading">
              <div v-if="reviews.length > 0">
                <div v-for="review in reviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <n-space align="center" :size="8">
                      <n-avatar :size="32" round>
                        {{ (review.username || 'U')[0].toUpperCase() }}
                      </n-avatar>
                      <strong>{{ review.username || 'User' }}</strong>
                      <n-rate :value="review.rating" readonly size="small" />
                    </n-space>
                    <span class="review-date">{{ formatDate(review.createdAt || review.created_at || '') }}</span>
                  </div>
                  <p class="review-content">{{ review.content }}</p>
                  <n-divider />
                </div>

                <!-- Reviews Pagination -->
                <div v-if="reviewTotal > reviewPageSize" style="display: flex; justify-content: center">
                  <n-pagination
                    v-model:page="reviewPageNum"
                    :page-count="Math.ceil(reviewTotal / reviewPageSize)"
                    size="small"
                    @update:page="fetchReviews"
                  />
                </div>
              </div>
              <n-empty v-else-if="!reviewsLoading" description="暂无评价，快来发表第一条评论吧！" />
            </n-spin>
          </n-card>

          <!-- Recommendations -->
          <div v-if="recommendations.length > 0" style="margin-top: 24px">
            <h2 class="section-title">猜你喜欢</h2>
            <n-grid :x-gap="16" :y-gap="16" :cols="4" responsive="screen" item-responsive>
              <n-gi v-for="rec in recommendations" :key="rec.productid || rec.id" span="4 m:2 l:1">
                <ProductCard :product="rec" />
              </n-gi>
            </n-grid>
          </div>
        </template>
      </n-spin>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import ProductCard from '@/components/ProductCard.vue'
import type { ProductVO, ReviewVO } from '@/types'
import { getProduct, getRecommendProducts } from '@/api/product'
import { getProductReviews, createReview } from '@/api/review'
import { addToCart, getCart } from '@/api/cart'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const product = ref<ProductVO | null>(null)
const loading = ref(true)
const quantity = ref(1)
const addingToCart = ref(false)
const buyingNow = ref(false)

// Reviews
const reviews = ref<ReviewVO[]>([])
const reviewsLoading = ref(false)
const reviewPageNum = ref(1)
const reviewPageSize = ref(10)
const reviewTotal = ref(0)
const showReviewForm = ref(false)
const reviewForm = ref({ rating: 5, content: '' })
const submittingReview = ref(false)

// Recommendations
const recommendations = ref<ProductVO[]>([])

const placeholderImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="400" fill="%23ddd"%3E%3Crect width="300" height="400"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="20"%3ENo Image%3C/text%3E%3C/svg%3E'

const hasDiscount = computed(() => {
  return product.value?.salePrice && product.value.salePrice < product.value.price
})

const displayPrice = computed(() => {
  if (!product.value) return '0.00'
  if (hasDiscount.value) return (product.value.salePrice || 0).toFixed(2)
  return product.value.price.toFixed(2)
})

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

async function fetchProduct() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    product.value = await getProduct(id)
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function fetchReviews() {
  const id = Number(route.params.id)
  if (!id) return
  reviewsLoading.value = true
  try {
    const res = await getProductReviews(id, reviewPageNum.value, reviewPageSize.value)
    reviews.value = res.records || res.list || []
    reviewTotal.value = res.total || 0
  } catch {
    reviews.value = []
    reviewTotal.value = 0
  } finally {
    reviewsLoading.value = false
  }
}

async function submitReview() {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'Login' })
    return
  }
  if (!reviewForm.value.content.trim()) {
    window.$message?.warning('请填写评价内容')
    return
  }
  submittingReview.value = true
  try {
    await createReview({
      productId: (product.value!.productid || product.value!.id) as any,
      orderId: 0,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
    })
    window.$message?.success('评价已提交')
    showReviewForm.value = false
    reviewForm.value = { rating: 5, content: '' }
    await fetchReviews()
  } catch {
    // handled by interceptor
  } finally {
    submittingReview.value = false
  }
}

async function handleAddToCart() {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  addingToCart.value = true
  try {
    await addToCart({ productId: (product.value!.productid || product.value!.id) as any, quantity: quantity.value })
    const cart = await getCart()
    cartStore.setItems(cart.items || [])
    window.$message?.success(`已将 ${quantity.value} 件商品加入购物车`)
  } catch {
    // handled by interceptor
  } finally {
    addingToCart.value = false
  }
}

async function handleBuyNow() {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  buyingNow.value = true
  try {
    await addToCart({ productId: (product.value!.productid || product.value!.id) as any, quantity: quantity.value })
    const cart = await getCart()
    cartStore.setItems(cart.items || [])
    router.push('/checkout')
  } catch {
    // handled by interceptor
  } finally {
    buyingNow.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchProduct(), fetchReviews()])

  // Load recommendations
  try {
    recommendations.value = await getRecommendProducts(4) || []
  } catch {
    recommendations.value = []
  }
})
</script>

<style scoped>
.product-detail-layout {
  display: flex;
  gap: 32px;
}

.product-image-section {
  width: 350px;
  flex-shrink: 0;
}

.product-info-section {
  flex: 1;
}

.product-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.product-author {
  font-size: 1.1rem;
  color: #666;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 16px 0;
}

.price-section .price {
  font-size: 2rem;
  color: #e8803f;
  font-weight: 700;
}

.price-section .price-original {
  font-size: 1.1rem;
  color: #999;
  text-decoration: line-through;
}

.product-description {
  line-height: 1.8;
  color: #555;
}

.review-form {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.review-item {
  padding: 12px 0;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.review-date {
  color: #999;
  font-size: 0.85rem;
}

.review-content {
  color: #555;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .product-detail-layout {
    flex-direction: column;
  }
  .product-image-section {
    width: 100%;
  }
}
</style>
