<template>
  <n-card
    hoverable
    class="product-card"
    :content-style="{ padding: '0' }"
    @click="goToDetail"
  >
    <div class="product-image-wrap">
      <n-image
        :src="productImage"
        :fallback-src="placeholderImage"
        object-fit="cover"
        class="product-image"
        preview-disabled
        lazy
      />
      <n-tag v-if="hasDiscount" type="error" size="small" class="discount-tag">
        促销
      </n-tag>
    </div>
    <div class="product-info">
      <h3 class="product-title">{{ productTitle }}</h3>
      <p class="product-author">{{ productAuthor }}</p>
      <div class="product-rating">
        <n-rate :value="productRating" readonly size="small" allow-half />
        <span class="rating-count">({{ productRatingCount }})</span>
      </div>
      <div class="product-price-row">
        <span class="price">${{ displayPrice }}</span>
        <span v-if="hasDiscount" class="price-original">${{ product.price.toFixed(2) }}</span>
      </div>
      <n-button
        type="primary"
        size="small"
        block
        :loading="adding"
        :disabled="productStock === 0"
        @click.stop="handleAddToCart"
      >
        {{ productStock === 0 ? '已售罄' : '加入购物车' }}
      </n-button>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { ProductVO } from '@/types'
import { addToCart } from '@/api/cart'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { getCart } from '@/api/cart'

const props = defineProps<{
  product: ProductVO
}>()

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const adding = ref(false)

const placeholderImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="280" fill="%23ddd"%3E%3Crect width="200" height="280"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'

const productImage = computed(() => {
  if (props.product.image) {
    return props.product.image
  }
  return placeholderImage
})

const hasDiscount = computed(() => {
  return props.product.salePrice && props.product.salePrice < props.product.price
})

const displayPrice = computed(() => {
  if (hasDiscount.value) {
    return props.product.salePrice!.toFixed(2)
  }
  return props.product.price.toFixed(2)
})

const productTitle = computed(() => props.product.name || props.product.title || '未知商品')
const productAuthor = computed(() => props.product.author || '未知作者')
const productId = computed(() => props.product.productid || props.product.id || 0)
const productRating = computed(() => props.product.rating || 0)
const productRatingCount = computed(() => props.product.ratingCount || props.product.rating_count || 0)
const productStock = computed(() => props.product.stock || 0)

function goToDetail() {
  router.push(`/product/${productId.value}`)
}

async function handleAddToCart() {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: '/cart' } })
    return
  }
  adding.value = true
  try {
    await addToCart({ productId: productId.value as any, quantity: 1 })
    // Refresh cart
    const cart = await getCart()
    cartStore.setItems(cart.items || [])
    window.$message?.success('已加入购物车')
  } catch {
    // Error handled by interceptor
  } finally {
    adding.value = false
  }
}
</script>

<style scoped>
.product-card {
  cursor: pointer;
  transition: all 0.4s ease;
  height: 100%;
  background: rgba(17, 24, 39, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 12px;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-8px);
  border-color: #00d4ff;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.3);
}

.product-image-wrap {
  position: relative;
  height: 220px;
  overflow: hidden;
  background: linear-gradient(135deg, #1e293b, #0a0e1a);
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image {
  transform: scale(1.08);
}

.discount-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.9), rgba(220, 38, 38, 0.9));
  box-shadow: 0 0 15px rgba(239, 68, 68, 0.4);
  backdrop-filter: blur(10px);
}

.product-info {
  padding: 16px;
}

.product-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #f1f5f9;
  transition: color 0.3s ease;
}

.product-card:hover .product-title {
  color: #00d4ff;
}

.product-author {
  font-size: 0.85rem;
  color: #94a3b8;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.rating-count {
  font-size: 0.8rem;
  color: #64748b;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.price {
  color: #00d4ff;
  font-weight: 700;
  font-size: 1.1rem;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
}

.price-original {
  color: #64748b;
  text-decoration: line-through;
  font-size: 0.85rem;
}
</style>
