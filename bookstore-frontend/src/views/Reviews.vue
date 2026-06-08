<template>
  <!-- 评价页容器：最大宽度 800px，居中展示 -->
  <div class="container" style="max-width:800px">
    <h1>我的评价</h1>
    <p class="text-dim">您提交过的所有商品评价</p>

    <!-- ===== 加载状态 ===== -->
    <!-- v-if="loading"：请求中显示旋转 loading 动画 -->
    <div v-if="loading" class="loading-spinner"></div>

    <!-- ===== 空状态：无评价记录 ===== -->
    <!-- v-else-if="reviews.length === 0"：加载完成但无评价时显示终端风格提示 -->
    <div v-else-if="reviews.length === 0" class="text-center mt-2" style="padding:60px 0">
      <p class="text-dim" style="font-size:20px;">&gt; NO_REVIEWS</p>
      <p class="text-dim">暂无评价记录</p>
    </div>

    <!-- ===== 评价列表 ===== -->
    <!-- v-else：有评价数据时展示评价卡片列表 -->
    <div v-else>
      <!-- v-for="r in reviews"：遍历评价数组 -->
      <div v-for="r in reviews" :key="r.reviewId" class="review-card card mb-2">
        <!-- flex 水平布局：评价内容在左，删除按钮在右 -->
        <div style="display:flex;justify-content:space-between;align-items:flex-start">
          <div style="flex:1">
            <!-- 评价头部：产品名称徽章 + 星级 + 日期 -->
            <div class="review-head">
              <!-- v-if="r.productId"：产品 ID 存在时显示产品名称徽章 -->
              <span class="badge badge-paid" v-if="r.productId">{{ r.productName || r.productId }}</span>
              <!-- 星级：实心星（★） × 评分 + 空心星（☆） × (5 - 评分) -->
              <span class="stars">{{ '★'.repeat(r.rating || 0) }}{{ '☆'.repeat(5 - (r.rating || 0)) }}</span>
              <!-- 评价日期：等宽灰色小字 -->
              <span class="text-dim" style="font-size:11px;font-family:var(--font-mono);">{{ formatDate(r.createTime) }}</span>
            </div>
            <!-- 评价内容 -->
            <p style="color:var(--text);margin:10px 0;">{{ r.content }}</p>
            <!-- v-if="r.reply"：商家回复存在时显示，带特殊样式的回复框 -->
            <p v-if="r.reply" class="reply">商家回复：{{ r.reply }}</p>
          </div>
          <!-- @click="handleDelete(r.reviewId)"：删除评价按钮，红色边框 -->
          <button class="btn btn-outline btn-sm" @click="handleDelete(r.reviewId)" style="flex-shrink:0;color:var(--danger);border-color:rgba(255,51,102,.3);">删除</button>
        </div>
      </div>

      <!-- ===== 分页控件：仅当总记录数超过每页数量时显示 ===== -->
      <!-- v-if="total > pageSize"：总条数超过页大小时才显示分页 -->
      <div v-if="total > pageSize" class="pagination mt-2">
        <!-- :disabled="page <= 1"：第 1 页时禁用"上一页"按钮 -->
        <!-- @click="page--; fetchReviews()"：点击后页码减 1 并重新请求 -->
        <button :disabled="page <= 1" @click="page--; fetchReviews()" class="btn btn-outline btn-sm">上一页</button>
        <!-- 当前页码 / 总页数 -->
        <span class="page-info">{{ page }} / {{ totalPages }}</span>
        <!-- :disabled="page >= totalPages"：最后一页时禁用"下一页"按钮 -->
        <!-- @click="page++; fetchReviews()"：点击后页码加 1 并重新请求 -->
        <button :disabled="page >= totalPages" @click="page++; fetchReviews()" class="btn btn-outline btn-sm">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '../api/client'

/**
 * Review 接口定义：评价数据的数据结构
 */
interface Review {
  reviewId: number      // 评价 ID（唯一标识）
  productId?: string    // 产品 ID
  productName?: string  // 产品名称
  rating: number        // 评分（1-5 星）
  content: string       // 评价内容
  reply?: string        // 商家回复（可选）
  createTime: string    // 创建时间
}

// reviews：评价列表数组，类型为 Review 接口
const reviews = ref<Review[]>([])
// page：当前页码
const page = ref(1)
// pageSize：每页评价数量
const pageSize = 10
// total：评价总条数
const total = ref(0)
// loading：页面加载状态
const loading = ref(false)

// totalPages：计算属性，根据 total 和 pageSize 计算总页数（至少 1 页）
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

/**
 * 格式化日期字符串
 * 将 ISO 日期字符串转换为中文格式（如 2024/1/15）
 */
function formatDate(s: string) {
  if (!s) return ''
  return new Date(s).toLocaleDateString('zh-CN')
}

/**
 * 获取评价列表
 * 调用 GET /reviews 接口，传递分页参数
 * try 成功分支：
 *   - 兼容多种后端返回格式：records / list / reviews
 *   - 提取 total 字段（兼容 total / totalElements）
 * catch 失败分支：
 *   - 清空 reviews 数组
 * finally：
 *   - 将 loading 设为 false
 */
async function fetchReviews() {
  loading.value = true
  try {
    // 调用评价列表 API：GET /reviews
    const res = await api.get('/reviews', { params: { pageNum: page.value, pageSize } })
    const data = res.data || res
    // 兼容多种返回字段名
    reviews.value = (data.records || data.list || data.reviews || []) as Review[]
    // 兼容多种 total 字段名
    total.value = (data.total || data.totalElements || 0) as number
  } catch (e: any) { reviews.value = [] }
  finally { loading.value = false }
}

/**
 * 删除评价
 * 先弹出 confirm 确认框，确认后调用 DELETE /reviews/{reviewId} 接口
 * try 成功分支：
 *   - 从本地 reviews 数组中过滤掉被删除的评价（前端乐观更新，无需重新请求）
 * catch 失败分支：
 *   - 弹出错误信息提示
 */
async function handleDelete(reviewId: number) {
  if (!confirm('确定删除该评价？')) return
  try {
    // 调用删除评价 API：DELETE /reviews/{reviewId}
    await api.delete(`/reviews/${reviewId}`)
    // 从前端列表中移除已删除的评价
    reviews.value = reviews.value.filter(r => r.reviewId !== reviewId)
  } catch (e: any) { alert(e.message) }
}

onMounted(fetchReviews)
</script>

<style scoped>
/* 评价头部：flex 水平布局，对齐居中，间距 10px，支持换行 */
.review-head { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; flex-wrap: wrap; }
/* 星级：黄色 (#ffb700) + 字母间距，模拟星星效果 */
.stars { color: #ffb700; font-size: 14px; letter-spacing: 2px; }
/* 商家回复框：青色半透明背景 + 边框 + 圆角 */
.reply { background: rgba(0,240,255,.05); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 10px 14px; font-size: 13px; color: var(--text-secondary); margin-top: 8px; }
/* 分页控件：flex 水平居中布局，间距 10px */
.pagination { display: flex; align-items: center; justify-content: center; gap: 10px; }
/* 页码文字：等宽灰色小字 */
.page-info { color: var(--text-dim); font-family: var(--font-mono); font-size: 12px; }
/* 次要文字 */
.text-dim { color: var(--text-dim); }
</style>
