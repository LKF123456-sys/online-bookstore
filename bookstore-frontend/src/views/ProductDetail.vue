<template>
  <div class="container">
    <!-- ===== 加载状态 ===== -->
    <!-- v-if="loading"：加载中时显示旋转动画 -->
    <div v-if="loading" class="loading-spinner"></div>

    <!-- ===== 产品不存在状态 ===== -->
    <!-- v-else-if="!product"：加载完成但产品为 null 时显示终端风格提示 -->
    <div v-else-if="!product" class="text-center mt-3">
      <p class="text-dim" style="font-size:20px;">&gt; PRODUCT_NOT_FOUND</p>
    </div>

    <!-- ===== 产品详情：左右两栏布局 ===== -->
    <!-- v-else：产品数据存在时展示详情 -->
    <div v-else class="detail-grid">
      <!-- ===== 左侧：产品图片区域 ===== -->
      <div class="detail-img">
        <!-- v-if="imgSrc"：图片 URL 存在时渲染 img 标签 -->
        <!-- :src="imgSrc"：动态绑定图片地址（由 computed 属性计算得出） -->
        <!-- @error="imgFail = true"：图片加载失败时设置 imgFail 为 true，显示占位图 -->
        <!-- v-show="!imgFail"：图片加载成功时可见，失败时隐藏 -->
        <img v-if="imgSrc"
          :src="imgSrc"
          :alt="product.title"
          @error="imgFail = true"
          v-show="!imgFail" />
        <!-- v-if="!imgSrc || imgFail"：图片 URL 不存在或加载失败时显示占位图 -->
        <div v-if="!imgSrc || imgFail" class="img-placeholder">
          <!-- 书本 emoji 作为占位图标 -->
          <span class="ph-icon">&#x1F4D6;</span>
          <!-- 显示书名前 16 个字符 -->
          <span class="ph-title">{{ (product.title || product.name || 'Book').slice(0, 16) }}</span>
        </div>
      </div>

      <!-- ===== 右侧：产品信息区域 ===== -->
      <div class="detail-info">
        <!-- 终端风格徽章 -->
        <div class="info-badge">&gt; product_spec</div>
        <!-- 产品标题 -->
        <h1>{{ product.title || product.name }}</h1>
        <!-- 作者/出版社信息 -->
        <p class="author">{{ product.author || product.publisher }}</p>
        <!-- 价格行：当前价格 + 原价（如有折扣） -->
        <div class="price-row">
          <!-- 当前售价，显示为折扣价或原价 -->
          <span class="current-price">&#165;{{ (product.price || product.discountprice || 0).toFixed(2) }}</span>
          <!-- v-if="product.originalprice && product.originalprice > product.price"：仅当有原价且高于现价时显示划线原价 -->
          <span v-if="product.originalprice && product.originalprice > product.price" class="original-price">&#165;{{ product.originalprice.toFixed(2) }}</span>
        </div>
        <!-- 库存信息：有库存显示绿色数量，无库存显示"缺货" -->
        <p class="stock">{{ product.stock > 0 ? `库存 ${product.stock} 件` : '缺货' }}</p>
        <!-- 操作按钮 -->
        <div class="actions mt-2">
          <!-- @click="addToCart"：加入购物车 -->
          <!-- :disabled="product.stock <= 0"：库存为 0 时禁用按钮 -->
          <button class="btn btn-primary" @click="addToCart" :disabled="product.stock <= 0">加入购物车</button>
          <!-- @click="buyNow"：立即购买（先加购物车再跳转结算页） -->
          <!-- 绿色渐变按钮，带绿色发光阴影 -->
          <button class="btn" style="background:linear-gradient(135deg,#00ff88,#00c853);color:#000;box-shadow:0 4px 20px rgba(0,255,136,0.3);" @click="buyNow" :disabled="product.stock <= 0">立即购买</button>
        </div>
        <!-- 产品描述：将换行符替换为 <br> 后使用 v-html 渲染 -->
        <div class="desc mt-3" v-html="description"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/client'

// route：当前路由对象，用于从 URL 路径参数中提取产品 ID（route.params.id）
const route = useRoute()
// router：Vue Router 实例，用于加入购物车后跳转到购物车页
const router = useRouter()
// product：产品详情对象，存储 /products/{id} 接口返回的产品数据
const product = ref<any>(null)
// loading：页面加载状态
const loading = ref(true)
// imgFail：图片加载失败标志，true 时显示占位图
const imgFail = ref(false)

/**
 * imgSrc 计算属性：从产品对象中提取图片 URL
 * 兼容多种后端返回字段名：image / image_url / img / cover
 * 返回 null 时模板显示占位图
 */
const imgSrc = computed(() => {
  if (!product.value) return null
  return product.value.image || product.value.image_url || product.value.img || product.value.cover || null
})

/**
 * description 计算属性：从产品对象中提取描述文本
 * 兼容多种字段名：description / detail
 * 将 \n 替换为 <br> 标签以支持 HTML 渲染
 */
const description = computed(() => {
  if (!product.value) return ''
  return (product.value.description || product.value.detail || '').replace(/\n/g, '<br>')
})

/**
 * 加入购物车函数
 * 调用 POST /orders/cart 接口，传递产品 ID 和数量 1
 * try 成功分支：
 *   - 弹出"已加入购物车"提示
 *   - router.push('/cart') 跳转到购物车页
 * catch 失败分支：
 *   - 弹出错误信息（如"请先登录"）
 */
async function addToCart() {
  try {
    // 调用加入购物车 API：POST /orders/cart
    await api.post('/orders/cart', { productId: product.value.id || product.value.productid, quantity: 1 })
    alert('已加入购物车')
    router.push('/cart')
  } catch (e: any) { alert(e.message) }
}

/**
 * 立即购买函数：复用 addToCart 逻辑，先加入购物车再跳转结算页
 */
function buyNow() { addToCart() }

/**
 * onMounted 生命周期钩子：组件挂载后根据路由参数请求产品详情
 * 调用 GET /products/{id} 接口
 * try 成功分支：
 *   - 将返回的产品数据赋值给 product
 * catch 失败分支：
 *   - 静默处理，product 保持 null，页面显示 PRODUCT_NOT_FOUND
 * finally：
 *   - 将 loading 设为 false
 */
onMounted(async () => {
  try {
    // 从路由参数中获取产品 ID
    const id = route.params.id
    // 调用产品详情 API：GET /products/{id}
    const res = await api.get(`/products/${id}`)
    product.value = res.data
  } catch (e) { /* 请求失败静默处理 */ }
  finally { loading.value = false }
})
</script>

<style scoped>
/* 详情页双栏网格布局：左右各一列，间距 48px */
.detail-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 48px;
}
/* 图片容器：渐变背景（青→紫）+ 边框 + 圆角，flex 居中内容，最小高度 400px */
.detail-img {
  background: linear-gradient(135deg, rgba(0,240,255,0.04), rgba(157,78,221,0.04));
  border: 1px solid var(--border); border-radius: var(--radius);
  overflow: hidden; display: flex; align-items: center; justify-content: center;
  min-height: 400px;
}
/* 产品图片：宽度撑满，高度自适应 */
.detail-img img { width: 100%; height: auto; }
/* 图片占位符：纵向 flex 布局居中，间距 12px */
.img-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  color: var(--text-dim); padding: 60px;
}
/* 占位图标：大号 emoji + 半透明 */
.ph-icon { font-size: 64px; opacity: .4; }
/* 占位书名：等宽字体 + 半透明 */
.ph-title { font-size: 16px; opacity: .5; font-family: var(--font-mono); }
/* 终端风格信息徽章 */
.info-badge {
  display: inline-block; font-family: var(--font-mono); font-size: 10px;
  color: var(--text-dim); background: rgba(255,255,255,0.03);
  padding: 2px 10px; border: 1px solid var(--border); border-radius: 4px;
  margin-bottom: 12px; letter-spacing: 1px;
}
/* 产品标题：大字白色 */
.detail-info h1 { font-size: 30px; margin-bottom: 6px; color: #fff; }
/* 作者/出版社：等宽灰色小字 */
.author { color: var(--text-dim); font-size: 14px; font-family: var(--font-mono); margin-bottom: 20px; }
/* 价格行：flex 基线对齐，间距 14px */
.price-row { display: flex; align-items: baseline; gap: 14px; }
/* 当前价格：大号渐变文字（青→紫），粗体 */
.current-price {
  font-size: 36px; font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
/* 原价：灰色 + 删除线 */
.original-price { font-size: 16px; color: var(--text-dim); text-decoration: line-through; }
/* 库存信息：绿色等宽小字 */
.stock { margin-top: 10px; font-size: 13px; color: var(--success); font-family: var(--font-mono); }
/* 操作按钮：flex 水平排列，间距 14px */
.actions { display: flex; gap: 14px; }
/* 产品描述容器：半透明背景 + 边框 + 舒适行高 */
.desc {
  padding: 24px; background: rgba(255,255,255,0.03); border: 1px solid var(--border);
  border-radius: var(--radius); color: var(--text-secondary); line-height: 1.9; font-size: 14px;
}
/* 移动端响应式：详情页变为单列布局 */
@media(max-width:768px){ .detail-grid{ grid-template-columns:1fr; } }
</style>
