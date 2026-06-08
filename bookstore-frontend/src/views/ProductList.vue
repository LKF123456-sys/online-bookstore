<template>
  <div class="container">
    <div class="section-head mb-2">
      <h1><span class="accent-text">#</span> 全部图书</h1>
    </div>
    <div class="toolbar mt-2">
      <input v-model="searchKeyword" class="input" style="max-width:320px" placeholder="搜索书名、作者..." @keyup.enter="doSearch" />
      <select v-model="sort" class="input" style="max-width:150px" @change="fetch">
        <option value="">默认排序</option>
        <option value="price_asc">价格 ↑</option>
        <option value="price_desc">价格 ↓</option>
        <option value="sales_desc">销量 ↓</option>
      </select>
    </div>
    <div v-if="loading" class="loading-spinner"></div>
    <div v-else-if="products.length === 0" class="text-center mt-3" style="padding:60px 0">
      <p class="text-dim" style="font-size:24px;margin-bottom:12px;">&gt; NO_RESULTS_FOUND</p>
      <p class="text-dim">未找到匹配的图书</p>
    </div>
    <div v-else class="grid grid-4 mt-3">
      <ProductCard v-for="p in products" :key="p.id || p.productid" :product="p" />
    </div>
    <div class="pagination mt-3" v-if="total > pageSize">
      <button :disabled="pageNum <= 1" @click="pageNum--; fetch()">&laquo; 上一页</button>
      <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
      <button :disabled="pageNum >= totalPages" @click="pageNum++; fetch()">下一页 &raquo;</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '../components/ProductCard.vue'
import api from '../api/client'

const route = useRoute()
const products = ref<any[]>([])
const loading = ref(true)
const pageNum = ref(1)
const pageSize = 12
const total = ref(0)
const searchKeyword = ref('')
const sort = ref('')

const totalPages = computed(() => Math.ceil(total.value / pageSize))

async function fetch() {
  loading.value = true
  try {
    const res = await api.get('/products', {
      params: { pageNum: pageNum.value, pageSize, keyword: searchKeyword.value || undefined, sort: sort.value || undefined }
    })
    const data = res.data || {}
    products.value = data.records || []
    total.value = data.total || 0
  } catch (e) { products.value = [] }
  finally { loading.value = false }
}

function doSearch() { pageNum.value = 1; fetch() }

onMounted(() => {
  if (route.query.keyword) { searchKeyword.value = route.query.keyword as string }
  fetch()
})
</script>

<style scoped>
.section-head { border-bottom: 1px solid var(--border); padding-bottom: 12px; }
.accent-text { color: var(--primary); }
.toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.pagination { display: flex; gap: 16px; align-items: center; justify-content: center; }
.pagination button {
  padding: 8px 18px; background: var(--bg-card);
  border: 1px solid var(--border); border-radius: var(--radius-sm);
  color: var(--text-secondary); cursor: pointer; transition: all .25s;
  font-family: var(--font-mono); font-size: 12px;
}
.pagination button:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
.pagination button:disabled { opacity: .3; cursor: not-allowed; }
.page-info { color: var(--text-dim); font-family: var(--font-mono); font-size: 12px; }
.text-dim { color: var(--text-dim); }
</style>
