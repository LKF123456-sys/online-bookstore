<template>
  <div class="container" style="max-width:800px">
    <h1>我的评价</h1>
    <p class="text-dim">您提交过的所有商品评价</p>

    <div v-if="loading" class="loading-spinner"></div>
    <div v-else-if="reviews.length === 0" class="text-center mt-2" style="padding:60px 0">
      <p class="text-dim" style="font-size:20px;">&gt; NO_REVIEWS</p>
      <p class="text-dim">暂无评价记录</p>
    </div>

    <div v-else>
      <div v-for="r in reviews" :key="r.reviewId" class="review-card card mb-2">
        <div style="display:flex;justify-content:space-between;align-items:flex-start">
          <div style="flex:1">
            <div class="review-head">
              <span class="badge badge-paid" v-if="r.productId">{{ r.productName || r.productId }}</span>
              <span class="stars">{{ '★'.repeat(r.rating || 0) }}{{ '☆'.repeat(5 - (r.rating || 0)) }}</span>
              <span class="text-dim" style="font-size:11px;font-family:var(--font-mono);">{{ formatDate(r.createTime) }}</span>
            </div>
            <p style="color:var(--text);margin:10px 0;">{{ r.content }}</p>
            <p v-if="r.reply" class="reply">商家回复：{{ r.reply }}</p>
          </div>
          <button class="btn btn-outline btn-sm" @click="handleDelete(r.reviewId)" style="flex-shrink:0;color:var(--danger);border-color:rgba(255,51,102,.3);">删除</button>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination mt-2">
        <button :disabled="page <= 1" @click="page--; fetchReviews()" class="btn btn-outline btn-sm">上一页</button>
        <span class="page-info">{{ page }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages" @click="page++; fetchReviews()" class="btn btn-outline btn-sm">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '../api/client'

interface Review {
  reviewId: number
  productId?: string; productName?: string
  rating: number; content: string; reply?: string; createTime: string
}

const reviews = ref<Review[]>([])
const page = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

function formatDate(s: string) {
  if (!s) return ''
  return new Date(s).toLocaleDateString('zh-CN')
}

async function fetchReviews() {
  loading.value = true
  try {
    const res = await api.get('/reviews', { params: { pageNum: page.value, pageSize } })
    const data = res.data || res
    reviews.value = (data.records || data.list || data.reviews || []) as Review[]
    total.value = (data.total || data.totalElements || 0) as number
  } catch (e: any) { reviews.value = [] }
  finally { loading.value = false }
}

async function handleDelete(reviewId: number) {
  if (!confirm('确定删除该评价？')) return
  try {
    await api.delete(`/reviews/${reviewId}`)
    reviews.value = reviews.value.filter(r => r.reviewId !== reviewId)
  } catch (e: any) { alert(e.message) }
}

onMounted(fetchReviews)
</script>

<style scoped>
.review-head { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; flex-wrap: wrap; }
.stars { color: #ffb700; font-size: 14px; letter-spacing: 2px; }
.reply { background: rgba(0,240,255,.05); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 10px 14px; font-size: 13px; color: var(--text-secondary); margin-top: 8px; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 10px; }
.page-info { color: var(--text-dim); font-family: var(--font-mono); font-size: 12px; }
.text-dim { color: var(--text-dim); }
</style>
