<template>
  <div>
    <!-- Hero -->
    <section class="hero">
      <div class="container">
        <div class="hero-badge">&gt; DISCOVER_NEXT_BOOK.exe</div>
        <h1>探索无限书海</h1>
        <p>科技赋能阅读 · 海量图书 · AI推荐 · 极速配送</p>
        <div class="search-bar">
          <input v-model="keyword" class="input" placeholder="输入书名或作者..." @keyup.enter="search" />
          <button class="btn btn-primary" @click="search">
            <span class="search-icon">⌕</span> 搜索
          </button>
        </div>
      </div>
      <div class="hero-circuit"></div>
    </section>

    <!-- Stats -->
    <section class="container stats-row mt-3">
      <div class="stat-item"><span class="stat-num">10K+</span><span class="stat-label">精选图书</span></div>
      <div class="stat-item"><span class="stat-num">99%</span><span class="stat-label">好评率</span></div>
      <div class="stat-item"><span class="stat-num">24H</span><span class="stat-label">极速发货</span></div>
      <div class="stat-item"><span class="stat-num">50K+</span><span class="stat-label">活跃读者</span></div>
    </section>

    <!-- Hot Books -->
    <section class="container mt-3">
      <div class="section-head">
        <h2><span class="accent-text">#</span> 热销推荐</h2>
        <router-link to="/products" class="view-all">查看全部 &rarr;</router-link>
      </div>
      <div v-if="loading" class="loading-spinner"></div>
      <div v-else class="grid grid-4 mt-2">
        <ProductCard v-for="p in hotProducts" :key="p.id || p.productid" :product="p" />
      </div>
    </section>

    <!-- Recommended -->
    <section class="container mt-3">
      <div class="section-head">
        <h2><span class="accent-text">#</span> 精选推荐</h2>
      </div>
      <div class="grid grid-4 mt-2">
        <ProductCard v-for="p in recommended" :key="p.id || p.productid" :product="p" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '../components/ProductCard.vue'
import api from '../api/client'

const router = useRouter()
const keyword = ref('')
const hotProducts = ref<any[]>([])
const recommended = ref<any[]>([])
const loading = ref(true)

async function search() {
  if (keyword.value.trim()) {
    router.push(`/products?keyword=${encodeURIComponent(keyword.value.trim())}`)
  }
}

onMounted(async () => {
  try {
    const [hot, rec] = await Promise.all([
      api.get('/products/hot?limit=8'),
      api.get('/products/recommend?limit=8'),
    ])
    hotProducts.value = hot.data || []
    recommended.value = rec.data || []
  } catch (e) { /* empty */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.hero {
  position: relative; overflow: hidden;
  background: linear-gradient(135deg, rgba(0,200,214,0.12) 0%, rgba(157,78,221,0.08) 50%, rgba(255,0,110,0.04) 100%);
  border-bottom: 1px solid var(--border);
  text-align: center; padding: 90px 20px 80px;
}
.hero-circuit {
  position: absolute; inset: 0; pointer-events: none;
  background:
    linear-gradient(90deg, var(--border) 1px, transparent 1px) 0 0 / 80px 80px,
    linear-gradient(var(--border) 1px, transparent 1px) 0 0 / 80px 80px;
  opacity: .3;
}
.hero-badge {
  display: inline-block; font-family: var(--font-mono); font-size: 11px;
  color: var(--primary); background: rgba(0,240,255,0.1);
  padding: 4px 16px; border: 1px solid rgba(0,240,255,0.2); border-radius: 4px;
  margin-bottom: 20px; letter-spacing: 2px;
}
.hero h1 { font-size: 48px; color: #fff; margin-bottom: 12px; text-shadow: 0 0 30px var(--primary-glow); }
.hero p { font-size: 16px; color: var(--text-secondary); margin-bottom: 36px; }
.search-bar {
  display: flex; max-width: 540px; margin: 0 auto; gap: 10px;
}
.search-bar .input { background: rgba(0,0,0,0.3); border-color: var(--border-glow); }
.search-bar .input:focus { border-color: var(--primary); }
.search-icon { font-size: 18px; }

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 20px; padding: 20px 0;
}
.stat-item {
  text-align: center; padding: 20px;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); backdrop-filter: blur(12px);
}
.stat-num {
  display: block; font-size: 28px; font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
.stat-label { display: block; font-size: 12px; color: var(--text-dim); margin-top: 4px; letter-spacing: 1px; }

.section-head {
  display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid var(--border); padding-bottom: 12px;
}
.section-head h2 { font-size: 22px; color: var(--text); }
.accent-text { color: var(--primary); }
.view-all { font-size: 13px; font-family: var(--font-mono); }

@media (max-width: 768px) {
  .hero h1 { font-size: 32px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
