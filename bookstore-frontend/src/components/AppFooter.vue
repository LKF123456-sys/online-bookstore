<template>
  <!-- ==================== 页脚区域 ==================== -->
  <!--
    页脚布局分为两部分：
    1. footer-inner：三栏信息区域（品牌、快速链接、用户中心），使用 CSS Grid 布局
    2. footer-bottom：底部版权信息栏
  -->
  <footer class="footer">
    <!-- 页脚主要内容区：CSS Grid 三栏布局 -->
    <div class="container footer-inner">

      <!-- 第一栏：品牌信息 -->
      <div class="footer-brand">
        <h4><span style="color:var(--text-dim)">&lt;</span><span style="color:var(--primary)">BookVerse</span><span style="color:var(--text-dim)">/&gt;</span></h4>
        <p>探索无限书海 · 科技赋能阅读</p>
      </div>

      <!-- 第二栏：快速链接 -->
      <div class="footer-links">
        <strong>快速链接</strong>
        <!-- router-link to="/" 跳转到首页 -->
        <router-link to="/">首页</router-link>
        <!-- router-link to="/products" 跳转到全部图书页 -->
        <router-link to="/products">全部图书</router-link>
        <!-- router-link to="/cart" 跳转到购物车页 -->
        <router-link to="/cart">购物车</router-link>
      </div>

      <!-- 第三栏：用户中心 -->
      <div class="footer-links">
        <strong>用户中心</strong>
        <!-- router-link to="/orders" 跳转到我的订单页 -->
        <router-link to="/orders">我的订单</router-link>
        <!-- router-link to="/reviews" 跳转到我的评价页 -->
        <router-link to="/reviews">我的评价</router-link>
        <!-- router-link to="/profile" 跳转到个人资料页 -->
        <router-link to="/profile">个人资料</router-link>
      </div>
    </div>

    <!-- 页脚底部：版权信息 -->
    <div class="footer-bottom">
      <p>&copy; 2026 BookVerse · 在线书城</p>
    </div>
  </footer>
</template>

<style scoped>
/* 页脚整体容器：半透明背景 + 顶部边框 + 顶部外边距 */
.footer {
  background: rgba(6, 6, 18, 0.9);
  border-top: 1px solid var(--border);
  margin-top: 60px;
}
/*
 * CSS Grid 三栏布局说明：
 * - grid-template-columns: 2fr 1fr 1fr
 *   品牌信息占 2 份宽度，快速链接和用户中心各占 1 份宽度
 * - gap: 40px 设置栏间距
 * - 移动端（<=768px）降级为单栏布局
 */
.footer-inner {
  display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 40px;
  padding: 40px 24px;
}
/* 品牌栏：等宽字体标题 + 发光阴影 */
.footer-brand h4 {
  font-family: var(--font-mono); font-size: 18px; margin-bottom: 8px;
  text-shadow: 0 0 10px var(--primary-glow);
}
.footer-brand p { color: var(--text-dim); font-size: 13px; }
/* 链接栏：flex 垂直排列，间距 10px */
.footer-links { display: flex; flex-direction: column; gap: 10px; }
.footer-links strong { color: var(--text); font-size: 13px; margin-bottom: 4px; text-transform: uppercase; letter-spacing: 1px; }
.footer-links a { color: var(--text-secondary); font-size: 13px; }
.footer-links a:hover { color: var(--primary); }
/* 底部版权栏：居中对齐 + 顶部分隔线 */
.footer-bottom {
  text-align: center; padding: 16px; border-top: 1px solid rgba(0,240,255,0.06);
  font-size: 12px; color: var(--text-dim);
}
/* 移动端适配：Grid 降级为单列布局 */
@media (max-width: 768px) {
  .footer-inner { grid-template-columns: 1fr; gap: 24px; }
}
</style>
