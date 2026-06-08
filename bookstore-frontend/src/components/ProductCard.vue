<template>
  <div class="product-card card" @click="$router.push(`/product/${product.id || product.productid}`)">
    <div class="card-img">
      <img v-if="imgSrc"
        :src="imgSrc"
        :alt="product.title || product.name"
        @error="imgFail = true"
        v-show="!imgFail" />
      <div v-if="!imgSrc || imgFail" class="img-placeholder">
        <span class="ph-icon">&#x1F4D6;</span>
        <span class="ph-text">{{ (product.title || product.name || 'Book').slice(0, 8) }}</span>
      </div>
      <div class="card-glow"></div>
    </div>
    <div class="card-body">
      <h4 class="card-title">{{ product.title || product.name }}</h4>
      <p class="card-author">{{ product.author || product.publisher }}</p>
      <div class="card-footer">
        <span class="price">&#165;{{ (product.price || product.discountprice || 0).toFixed(2) }}</span>
        <span v-if="product.stock <= 0" class="badge badge-cancel">缺货</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = defineProps<{ product: Record<string, any> }>()
const imgFail = ref(false)

const imgSrc = computed(() => {
  if (props.product.image) return props.product.image
  if (props.product.image_url) return props.product.image_url
  if (props.product.img) return props.product.img
  if (props.product.cover) return props.product.cover
  return null
})
</script>

<style scoped>
.product-card {
  cursor: pointer; overflow: hidden;
  padding: 0; transition: all .35s;
  background: var(--bg-card);
}
.product-card:hover {
  transform: translateY(-6px);
  border-color: var(--primary);
  box-shadow: 0 8px 40px var(--primary-glow);
}
.card-img {
  height: 240px; position: relative;
  background: linear-gradient(135deg, rgba(0,240,255,0.04), rgba(157,78,221,0.04));
  overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.card-img img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform .5s;
}
.product-card:hover .card-img img { transform: scale(1.05); }
.card-glow {
  position: absolute; bottom: 0; left: 0; right: 0; height: 2px;
  background: linear-gradient(90deg, transparent, var(--primary), transparent);
  opacity: 0; transition: opacity .35s;
}
.product-card:hover .card-glow { opacity: 1; }
.img-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  color: var(--text-dim);
}
.ph-icon { font-size: 42px; opacity: .4; }
.ph-text { font-size: 12px; opacity: .5; font-family: var(--font-mono); }
.card-body { padding: 16px 20px 20px; }
.card-title {
  font-size: 15px; margin-bottom: 6px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; color: var(--text);
}
.card-author {
  font-size: 12px; color: var(--text-dim);
  margin-bottom: 12px; font-family: var(--font-mono);
}
.card-footer { display: flex; align-items: center; gap: 8px; justify-content: space-between; }
.price {
  font-size: 20px; font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
