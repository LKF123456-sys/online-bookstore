<template>
  <div class="container">
    <!-- ===== 页面标题区域 ===== -->
    <div class="section-head mb-2">
      <!-- accent-text：高亮 "#" 符号，终端/代码风格 -->
      <h1><span class="accent-text">#</span> 全部图书</h1>
    </div>
    <!-- ===== 工具栏：搜索输入框 + 排序下拉框 ===== -->
    <div class="toolbar mt-2">
      <!-- v-model="searchKeyword"：双向绑定搜索关键词 -->
      <!-- @keyup.enter="doSearch"：回车触发搜索（重置到第 1 页） -->
      <input v-model="searchKeyword" class="input" style="max-width:320px" placeholder="搜索书名、作者..." @keyup.enter="doSearch" />
      <!-- v-model="sort"：双向绑定排序方式 -->
      <!-- @change="fetch"：切换排序方式时重新请求数据 -->
      <select v-model="sort" class="input" style="max-width:150px" @change="fetch">
        <option value="">默认排序</option>
        <option value="price_asc">价格 ↑</option>
        <option value="price_desc">价格 ↓</option>
        <option value="sales_desc">销量 ↓</option>
      </select>
    </div>

    <!-- ===== 加载状态：请求中显示 loading 动画 ===== -->
    <!-- v-if="loading"：加载中时显示旋转动画 -->
    <div v-if="loading" class="loading-spinner"></div>

    <!-- ===== 空状态：无搜索结果时的终端风格提示 ===== -->
    <!-- v-else-if="products.length === 0"：加载完成但无数据时显示空状态 -->
    <div v-else-if="products.length === 0" class="text-center mt-3" style="padding:60px 0">
      <p class="text-dim" style="font-size:24px;margin-bottom:12px;">&gt; NO_RESULTS_FOUND</p>
      <p class="text-dim">未找到匹配的图书</p>
    </div>

    <!-- ===== 产品网格：遍历产品数组渲染 ProductCard 组件（4 列布局） ===== -->
    <!-- v-else：有数据时展示 4 列产品网格 -->
    <!-- v-for="p in products"：遍历 products 数组渲染每个产品的卡片 -->
    <div v-else class="grid grid-4 mt-3">
      <ProductCard v-for="p in products" :key="p.id || p.productid" :product="p" />
    </div>

    <!-- ===== 分页控件：仅当总数据量超过每页数量时显示 ===== -->
    <!-- v-if="total > pageSize"：总条数超过页大小时才显示分页 -->
    <div class="pagination mt-3" v-if="total > pageSize">
      <!-- :disabled="pageNum <= 1"：第 1 页时禁用"上一页"按钮 -->
      <!-- @click="pageNum--; fetch()"：点击后页码减 1 并重新请求数据 -->
      <button :disabled="pageNum <= 1" @click="pageNum--; fetch()">&laquo; 上一页</button>
      <!-- 当前页码 / 总页数 -->
      <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
      <!-- :disabled="pageNum >= totalPages"：最后一页时禁用"下一页"按钮 -->
      <!-- @click="pageNum++; fetch()"：点击后页码加 1 并重新请求数据 -->
      <button :disabled="pageNum >= totalPages" @click="pageNum++; fetch()">下一页 &raquo;</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '../components/ProductCard.vue'
import api from '../api/client'

// route：当前路由对象，用于读取 URL 查询参数（如 keyword）
const route = useRoute()
// products：产品列表数组，存储 /products 接口返回的图书数据
const products = ref<any[]>([])
// loading：页面加载状态，true 时显示 loading 动画
const loading = ref(true)
// pageNum：当前页码，从 1 开始计数
const pageNum = ref(1)
// pageSize：每页显示数量，固定 12 条
const pageSize = 12
// total：图书总条数，用于计算总页数
const total = ref(0)
// searchKeyword：搜索关键词，通过 v-model 与输入框双向绑定
const searchKeyword = ref('')
// sort：排序方式，通过 v-model 与下拉框双向绑定
const sort = ref('')

// totalPages：计算属性，根据 total 和 pageSize 计算出总页数（至少 1 页）
const totalPages = computed(() => Math.ceil(total.value / pageSize))

/**
 * 获取产品列表
 * 调用 GET /products 接口，传递分页、搜索、排序参数
 * try 成功分支：
 *   - 将返回的 records 赋值给 products
 *   - 将返回的 total 赋值给 total（用于分页计算）
 * catch 失败分支：
 *   - 清空 products 数组，页面展示空状态
 * finally：
 *   - 将 loading 设为 false
 */
async function fetch() {
  loading.value = true
  try {
    // 调用产品列表 API：GET /products，传递分页、关键词搜索、排序参数
    const res = await api.get('/products', {
      params: { pageNum: pageNum.value, pageSize, keyword: searchKeyword.value || undefined, sort: sort.value || undefined }
    })
    const data = res.data || {}
    products.value = data.records || []
    total.value = data.total || 0
  } catch (e) { products.value = [] }
  finally { loading.value = false }
}

/**
 * 执行搜索：重置页码为第 1 页后重新请求数据
 * 触发方式：搜索框 @keyup.enter
 */
function doSearch() { pageNum.value = 1; fetch() }

/**
 * onMounted 生命周期钩子：组件挂载时初始化
 * - 如果 URL 中有 keyword 查询参数（从首页搜索跳转而来），将其赋值给搜索框
 * - 调用 fetch() 请求第一页数据
 */
onMounted(() => {
  if (route.query.keyword) { searchKeyword.value = route.query.keyword as string }
  fetch()
})
</script>

<style scoped>
/* 区域标题：底部有分割线 */
.section-head { border-bottom: 1px solid var(--border); padding-bottom: 12px; }
/* "#" 前缀高亮为青色 */
.accent-text { color: var(--primary); }
/* 工具栏：flex 水平布局，搜索框和排序下拉框横向排列，支持换行 */
.toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
/* 分页控件：flex 水平居中布局，按钮与页码间距 16px */
.pagination { display: flex; gap: 16px; align-items: center; justify-content: center; }
/* 分页按钮：深色背景 + 边框 + 等宽字体，hover 时边框和文字变为青色 */
.pagination button {
  padding: 8px 18px; background: var(--bg-card);
  border: 1px solid var(--border); border-radius: var(--radius-sm);
  color: var(--text-secondary); cursor: pointer; transition: all .25s;
  font-family: var(--font-mono); font-size: 12px;
}
/* 分页按钮 hover：仅当非禁用状态时边框和文字变青 */
.pagination button:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
/* 分页按钮禁用状态：透明度降低 + 禁止点击 */
.pagination button:disabled { opacity: .3; cursor: not-allowed; }
/* 页码文字：等宽灰色小字 */
.page-info { color: var(--text-dim); font-family: var(--font-mono); font-size: 12px; }
/* 次要文字 */
.text-dim { color: var(--text-dim); }
</style>
