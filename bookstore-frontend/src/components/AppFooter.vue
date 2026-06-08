<template>
  <footer class="footer">
    <div class="container footer-inner">
      <div class="footer-brand">
        <h4><span style="color:var(--text-dim)">&lt;</span><span style="color:var(--primary)">BookVerse</span><span style="color:var(--text-dim)">/&gt;</span></h4>
        <p>探索无限书海 · 科技赋能阅读</p>
      </div>
      <div class="footer-links">
        <strong>快速链接</strong>
        <router-link to="/">首页</router-link>
        <router-link to="/products">全部图书</router-link>
        <router-link to="/cart">购物车</router-link>
      </div>
      <div class="footer-links">
        <strong>用户中心</strong>
        <router-link to="/orders">我的订单</router-link>
        <router-link to="/reviews">我的评价</router-link>
        <router-link to="/profile">个人资料</router-link>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2026 BookVerse · 在线书城</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: rgba(6, 6, 18, 0.9);
  border-top: 1px solid var(--border);
  margin-top: 60px;
}
.footer-inner {
  display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 40px;
  padding: 40px 24px;
}
.footer-brand h4 {
  font-family: var(--font-mono); font-size: 18px; margin-bottom: 8px;
  text-shadow: 0 0 10px var(--primary-glow);
}
.footer-brand p { color: var(--text-dim); font-size: 13px; }
.footer-links { display: flex; flex-direction: column; gap: 10px; }
.footer-links strong { color: var(--text); font-size: 13px; margin-bottom: 4px; text-transform: uppercase; letter-spacing: 1px; }
.footer-links a { color: var(--text-secondary); font-size: 13px; }
.footer-links a:hover { color: var(--primary); }
.footer-bottom {
  text-align: center; padding: 16px; border-top: 1px solid rgba(0,240,255,0.06);
  font-size: 12px; color: var(--text-dim);
}
@media (max-width: 768px) {
  .footer-inner { grid-template-columns: 1fr; gap: 24px; }
}
</style>
