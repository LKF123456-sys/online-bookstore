<template>
  <!-- ==================== 图书产品卡片 ==================== -->
  <!--
    点击整个卡片跳转到图书详情页 /product/:id
    兼容 product.id 和 product.productid 两种字段名
  -->
  <div class="product-card card" @click="$router.push(`/product/${product.id || product.productid}`)">

    <!-- 卡片图片区域 -->
    <div class="card-img">
      <!--
        图片展示：
        - v-if="imgSrc"：仅当 imgSrc 计算属性有值时才渲染 <img>
        - v-show="!imgFail"：图片加载失败后隐藏 <img>，显示占位区
        - @error="imgFail = true"：图片加载失败时触发，设置 imgFail 为 true
      -->
      <img v-if="imgSrc"
        :src="imgSrc"
        :alt="product.title || product.name"
        @error="imgFail = true"
        v-show="!imgFail" />

      <!--
        占位区域：
        - 当 imgSrc 为空 或 图片加载失败（imgFail=true）时显示
        - 展示书本 emoji 图标和书名前 8 个字符
      -->
      <div v-if="!imgSrc || imgFail" class="img-placeholder">
        <span class="ph-icon">&#x1F4D6;</span>
        <span class="ph-text">{{ (product.title || product.name || 'Book').slice(0, 8) }}</span>
      </div>

      <!-- 卡片底部发光线条（hover 时显示） -->
      <div class="card-glow"></div>
    </div>

    <!-- 卡片信息区域 -->
    <div class="card-body">
      <!-- 书名：兼容 title 和 name 字段 -->
      <h4 class="card-title">{{ product.title || product.name }}</h4>
      <!-- 作者/出版社：兼容 author 和 publisher 字段 -->
      <p class="card-author">{{ product.author || product.publisher }}</p>

      <!-- 价格与库存状态 -->
      <div class="card-footer">
        <!-- 价格显示：兼容 price 和 discountprice 字段，保留两位小数 -->
        <span class="price">&#165;{{ (product.price || product.discountprice || 0).toFixed(2) }}</span>
        <!-- 库存为 0 时显示"缺货"标签 -->
        <span v-if="product.stock <= 0" class="badge badge-cancel">缺货</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

/**
 * props 定义：
 * - 接收父组件传入的 product 对象（Record<string, any> 类型）
 * - 兼容后端返回的不同字段名（id/productid, title/name, price/discountprice 等）
 */
const props = defineProps<{ product: Record<string, any> }>()

/**
 * imgFail：标记图片是否加载失败
 * - 初始值为 false，表示图片正常
 * - 当 <img> 触发 @error 事件时设为 true，触发占位图显示
 */
const imgFail = ref(false)

/**
 * imgSrc 计算属性：多字段兼容获取图片地址
 * - 按优先级依次尝试 image → image_url → img → cover 字段
 * - 若所有字段都不存在，返回 null，触发占位区显示
 */
const imgSrc = computed(() => {
  if (props.product.image) return props.product.image
  if (props.product.image_url) return props.product.image_url
  if (props.product.img) return props.product.img
  if (props.product.cover) return props.product.cover
  return null
})
</script>

<style scoped>
/*
 * 卡片整体样式：
 * - overflow: hidden 确保内容不超出圆角
 * - transition: all .35s 实现 hover 动画的平滑过渡
 */
.product-card {
  cursor: pointer; overflow: hidden;
  padding: 0; transition: all .35s;
  background: var(--bg-card);
}
/*
 * hover 动画效果说明：
 * - transform: translateY(-6px) 卡片上移 6px，模拟"浮起"效果
 * - border-color 变为主题色
 * - box-shadow 添加主题色发光阴影，增强立体感
 */
.product-card:hover {
  transform: translateY(-6px);
  border-color: var(--primary);
  box-shadow: 0 8px 40px var(--primary-glow);
}
/* 图片容器：固定高度 240px，渐变背景，中心对齐 */
.card-img {
  height: 240px; position: relative;
  background: linear-gradient(135deg, rgba(0,240,255,0.04), rgba(157,78,221,0.04));
  overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
/* 图片基础样式：填充容器，覆盖显示 */
.card-img img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform .5s;
}
/* hover 时图片放大 1.05 倍，增加动态感 */
.product-card:hover .card-img img { transform: scale(1.05); }
/*
 * 发光线条：位于图片底部
 * - 默认透明度为 0（不可见）
 * - hover 时透明度变为 1，显示出来
 */
.card-glow {
  position: absolute; bottom: 0; left: 0; right: 0; height: 2px;
  background: linear-gradient(90deg, transparent, var(--primary), transparent);
  opacity: 0; transition: opacity .35s;
}
.product-card:hover .card-glow { opacity: 1; }
/* 图片占位区：垂直居中的 flex 布局 */
.img-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  color: var(--text-dim);
}
.ph-icon { font-size: 42px; opacity: .4; }
.ph-text { font-size: 12px; opacity: .5; font-family: var(--font-mono); }
/* 卡片正文区域 */
.card-body { padding: 16px 20px 20px; }
/*
 * 标题样式：
 * - 使用 -webkit-line-clamp: 2 限制最多显示两行
 * - 超出部分以省略号隐藏
 */
.card-title {
  font-size: 15px; margin-bottom: 6px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; color: var(--text);
}
.card-author {
  font-size: 12px; color: var(--text-dim);
  margin-bottom: 12px; font-family: var(--font-mono);
}
/* 卡片底部：flex 水平排列，两端对齐 */
.card-footer { display: flex; align-items: center; gap: 8px; justify-content: space-between; }
/*
 * 价格渐变样式说明：
 * - background: linear-gradient(135deg, primary, secondary) 从主题色到辅色渐变
 * - -webkit-background-clip: text 将渐变色裁剪到文字形状
 * - -webkit-text-fill-color: transparent 使文字透明，露出渐变背景色
 * - background-clip: text 为标准属性（Firefox 等支持）
 */
.price {
  font-size: 20px; font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
