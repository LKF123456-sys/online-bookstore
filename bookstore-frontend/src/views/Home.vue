<template>
  <div>
    <!-- ===== Hero 区域：首页顶部横幅，展示品牌标语和搜索栏 ===== -->
    <section class="hero">
      <div class="container">
        <!-- 终端风格徽章：营造科技/极客氛围 -->
        <div class="hero-badge">&gt; DISCOVER_NEXT_BOOK.exe</div>
        <h1>探索无限书海</h1>
        <p>科技赋能阅读 · 海量图书 · AI推荐 · 极速配送</p>
        <!-- 搜索栏：v-model 双向绑定 keyword，回车或点击按钮触发搜索跳转 -->
        <div class="search-bar">
          <!-- v-model="keyword"：双向绑定搜索关键词 -->
          <!-- @keyup.enter="search"：回车触发搜索跳转到产品列表页 -->
          <input v-model="keyword" class="input" placeholder="输入书名或作者..." @keyup.enter="search" />
          <!-- @click="search"：点击按钮触发搜索跳转到产品列表页 -->
          <button class="btn btn-primary" @click="search">
            <span class="search-icon">⌕</span> 搜索
          </button>
        </div>
      </div>
      <!-- hero 背景装饰：模拟电路板网格图案 -->
      <div class="hero-circuit"></div>
    </section>

    <!-- ===== 统计数据展示行：展示平台关键数据指标 ===== -->
    <section class="container stats-row mt-3">
      <div class="stat-item"><span class="stat-num">10K+</span><span class="stat-label">精选图书</span></div>
      <div class="stat-item"><span class="stat-num">99%</span><span class="stat-label">好评率</span></div>
      <div class="stat-item"><span class="stat-num">24H</span><span class="stat-label">极速发货</span></div>
      <div class="stat-item"><span class="stat-num">50K+</span><span class="stat-label">活跃读者</span></div>
    </section>

    <!-- ===== 热销推荐区域：展示热门图书 ===== -->
    <section class="container mt-3">
      <div class="section-head">
        <!-- accent-text：高亮 "#" 符号，呼应终端/代码风格 -->
        <h2><span class="accent-text">#</span> 热销推荐</h2>
        <!-- router-link to="/products"：跳转到全部图书列表页 -->
        <router-link to="/products" class="view-all">查看全部 &rarr;</router-link>
      </div>
      <!-- v-if="loading"：加载中时显示 loading 动画 -->
      <div v-if="loading" class="loading-spinner"></div>
      <!-- v-else：加载完成后展示热销图书网格（4列） -->
      <!-- v-for="p in hotProducts"：遍历热销图书数组渲染 ProductCard 组件 -->
      <div v-else class="grid grid-4 mt-2">
        <ProductCard v-for="p in hotProducts" :key="p.id || p.productid" :product="p" />
      </div>
    </section>

    <!-- ===== 精选推荐区域：展示系统推荐图书 ===== -->
    <section class="container mt-3">
      <div class="section-head">
        <h2><span class="accent-text">#</span> 精选推荐</h2>
      </div>
      <!-- v-for="p in recommended"：遍历推荐图书数组渲染 ProductCard 组件 -->
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

// router：Vue Router 实例，用于编程式导航跳转
const router = useRouter()
// keyword：搜索关键词的响应式引用，与搜索输入框 v-model 双向绑定
const keyword = ref('')
// hotProducts：热销图书列表，存储 /products/hot 接口返回的图书数据
const hotProducts = ref<any[]>([])
// recommended：精选推荐图书列表，存储 /products/recommend 接口返回的推荐数据
const recommended = ref<any[]>([])
// loading：页面加载状态，true 时显示 loading 动画，false 时渲染内容
const loading = ref(true)

/**
 * 搜索函数：将用户输入的关键词编码后跳转到产品列表页进行搜索
 * 触发方式：搜索按钮 @click 或输入框 @keyup.enter
 */
async function search() {
  if (keyword.value.trim()) {
    // 使用 encodeURIComponent 对关键词进行 URL 编码，防止特殊字符导致 URL 解析错误
    router.push(`/products?keyword=${encodeURIComponent(keyword.value.trim())}`)
  }
}

/**
 * onMounted 生命周期钩子：组件挂载后并行请求热销图书和推荐图书
 * 使用 Promise.all 同时发起两个请求，提高加载效率
 * try：成功后将返回数据分别赋值给 hotProducts 和 recommended
 * catch：请求失败时静默处理，不展示错误信息（页面已有空状态）
 * finally：无论成败都将 loading 设为 false，展示内容
 */
onMounted(async () => {
  try {
    // Promise.all 并行请求：/products/hot?limit=8（热销图书）和 /products/recommend?limit=8（推荐图书）
    const [hot, rec] = await Promise.all([
      api.get('/products/hot?limit=8'),
      api.get('/products/recommend?limit=8'),
    ])
    hotProducts.value = hot.data || []
    recommended.value = rec.data || []
  } catch (e) { /* 请求失败静默处理，页面展示空状态 */ }
  finally { loading.value = false }
})
</script>

<style scoped>
/* ===== Hero 区域样式 ===== */
.hero {
  /* 相对定位 + 隐藏溢出：为背景装饰元素提供定位参考 */
  position: relative; overflow: hidden;
  /* 渐变背景：青→紫→粉红，营造科技感氛围 */
  background: linear-gradient(135deg, rgba(0,200,214,0.12) 0%, rgba(157,78,221,0.08) 50%, rgba(255,0,110,0.04) 100%);
  border-bottom: 1px solid var(--border);
  text-align: center; padding: 90px 20px 80px;
}
/* 电路板网格装饰：使用 CSS 渐变模拟电路板纹理 */
.hero-circuit {
  position: absolute; inset: 0; pointer-events: none;
  background:
    linear-gradient(90deg, var(--border) 1px, transparent 1px) 0 0 / 80px 80px,
    linear-gradient(var(--border) 1px, transparent 1px) 0 0 / 80px 80px;
  opacity: .3;
}
/* 终端风格徽章：等宽字体 + 青色边框，模拟命令行标签 */
.hero-badge {
  display: inline-block; font-family: var(--font-mono); font-size: 11px;
  color: var(--primary); background: rgba(0,240,255,0.1);
  padding: 4px 16px; border: 1px solid rgba(0,240,255,0.2); border-radius: 4px;
  margin-bottom: 20px; letter-spacing: 2px;
}
/* 主标题：大字号白色 + 青色发光文字阴影（霓虹效果） */
.hero h1 { font-size: 48px; color: #fff; margin-bottom: 12px; text-shadow: 0 0 30px var(--primary-glow); }
/* 副标题：灰色文字，字号适中 */
.hero p { font-size: 16px; color: var(--text-secondary); margin-bottom: 36px; }
/* 搜索栏：flex 水平布局，居中展示，输入框与按钮间距 10px */
.search-bar {
  display: flex; max-width: 540px; margin: 0 auto; gap: 10px;
}
/* 搜索栏输入框：半透明深色背景 + 发光边框 */
.search-bar .input { background: rgba(0,0,0,0.3); border-color: var(--border-glow); }
/* 搜索栏输入框聚焦：边框变为主题青色 */
.search-bar .input:focus { border-color: var(--primary); }
.search-icon { font-size: 18px; }

/* ===== 统计数据行样式 ===== */
/* 4 列网格布局：大屏幕四等分 */
.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 20px; padding: 20px 0;
}
/* 统计项卡片：半透明背景 + 毛玻璃效果 (backdrop-filter: blur) */
.stat-item {
  text-align: center; padding: 20px;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); backdrop-filter: blur(12px);
}
/* 统计数字：渐变文字（青→紫），字体粗大 */
.stat-num {
  display: block; font-size: 28px; font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
/* 统计标签：灰色小字，"精选图书"等说明文字 */
.stat-label { display: block; font-size: 12px; color: var(--text-dim); margin-top: 4px; letter-spacing: 1px; }

/* ===== 区域标题样式 ===== */
/* flex 布局：标题在左，"查看全部"链接在右，底部有分割线 */
.section-head {
  display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid var(--border); padding-bottom: 12px;
}
.section-head h2 { font-size: 22px; color: var(--text); }
/* "#" 前缀高亮为青色 */
.accent-text { color: var(--primary); }
/* "查看全部"链接：等宽小字 */
.view-all { font-size: 13px; font-family: var(--font-mono); }

/* ===== 移动端响应式 ===== */
@media (max-width: 768px) {
  /* 主标题字号缩小 */
  .hero h1 { font-size: 32px; }
  /* 统计行变为 2 列布局 */
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
