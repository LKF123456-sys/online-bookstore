<template>
  <div class="container">
    <div v-if="loading" class="loading-spinner"></div>
    <div v-else-if="!product" class="text-center mt-3">
      <p class="text-dim" style="font-size:20px;">&gt; PRODUCT_NOT_FOUND</p>
    </div>
    <div v-else class="detail-grid">
      <div class="detail-img">
        <img v-if="imgSrc"
          :src="imgSrc"
          :alt="product.title"
          @error="imgFail = true"
          v-show="!imgFail" />
        <div v-if="!imgSrc || imgFail" class="img-placeholder">
          <span class="ph-icon">&#x1F4D6;</span>
          <span class="ph-title">{{ (product.title || product.name || 'Book').slice(0, 16) }}</span>
        </div>
      </div>
      <div class="detail-info">
        <div class="info-badge">&gt; product_spec</div>
        <h1>{{ product.title || product.name }}</h1>
        <p class="author">{{ product.author || product.publisher }}</p>
        <div class="price-row">
          <span class="current-price">&#165;{{ (product.price || product.discountprice || 0).toFixed(2) }}</span>
          <span v-if="product.originalprice && product.originalprice > product.price" class="original-price">&#165;{{ product.originalprice.toFixed(2) }}</span>
        </div>
        <p class="stock">{{ product.stock > 0 ? `库存 ${product.stock} 件` : '缺货' }}</p>
        <div class="actions mt-2">
          <button class="btn btn-primary" @click="addToCart" :disabled="product.stock <= 0">加入购物车</button>
          <button class="btn" style="background:linear-gradient(135deg,#00ff88,#00c853);color:#000;box-shadow:0 4px 20px rgba(0,255,136,0.3);" @click="buyNow" :disabled="product.stock <= 0">立即购买</button>
        </div>
        <div class="desc mt-3" v-html="description"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/client'

const route = useRoute()
const router = useRouter()
const product = ref<any>(null)
const loading = ref(true)
const imgFail = ref(false)

const imgSrc = computed(() => {
  if (!product.value) return null
  return product.value.image || product.value.image_url || product.value.img || product.value.cover || null
})

const description = computed(() => {
  if (!product.value) return ''
  return (product.value.description || product.value.detail || '').replace(/\n/g, '<br>')
})

async function addToCart() {
  try {
    await api.post('/orders/cart', { productId: product.value.id || product.value.productid, quantity: 1 })
    alert('已加入购物车')
    router.push('/cart')
  } catch (e: any) { alert(e.message) }
}

function buyNow() { addToCart() }

onMounted(async () => {
  try {
    const id = route.params.id
    const res = await api.get(`/products/${id}`)
    product.value = res.data
  } catch (e) { /* empty */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.detail-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 48px;
}
.detail-img {
  background: linear-gradient(135deg, rgba(0,240,255,0.04), rgba(157,78,221,0.04));
  border: 1px solid var(--border); border-radius: var(--radius);
  overflow: hidden; display: flex; align-items: center; justify-content: center;
  min-height: 400px;
}
.detail-img img { width: 100%; height: auto; }
.img-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  color: var(--text-dim); padding: 60px;
}
.ph-icon { font-size: 64px; opacity: .4; }
.ph-title { font-size: 16px; opacity: .5; font-family: var(--font-mono); }
.info-badge {
  display: inline-block; font-family: var(--font-mono); font-size: 10px;
  color: var(--text-dim); background: rgba(255,255,255,0.03);
  padding: 2px 10px; border: 1px solid var(--border); border-radius: 4px;
  margin-bottom: 12px; letter-spacing: 1px;
}
.detail-info h1 { font-size: 30px; margin-bottom: 6px; color: #fff; }
.author { color: var(--text-dim); font-size: 14px; font-family: var(--font-mono); margin-bottom: 20px; }
.price-row { display: flex; align-items: baseline; gap: 14px; }
.current-price {
  font-size: 36px; font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
.original-price { font-size: 16px; color: var(--text-dim); text-decoration: line-through; }
.stock { margin-top: 10px; font-size: 13px; color: var(--success); font-family: var(--font-mono); }
.actions { display: flex; gap: 14px; }
.desc {
  padding: 24px; background: rgba(255,255,255,0.03); border: 1px solid var(--border);
  border-radius: var(--radius); color: var(--text-secondary); line-height: 1.9; font-size: 14px;
}
@media(max-width:768px){ .detail-grid{ grid-template-columns:1fr; } }
</style>
