<template>
  <n-layout-footer bordered class="app-footer">
    <div class="footer-content container">
      <div class="footer-section">
        <div class="footer-brand">
          <div class="footer-logo-icon">
            <n-icon size="20">
              <BookOutline />
            </n-icon>
          </div>
          <span>BookVerse</span>
        </div>
        <p class="footer-desc">您身边的在线书城，海量图书任您挑选</p>
        <div class="footer-social">
          <a href="#"><n-icon><LogoGithub /></n-icon></a>
          <a href="#"><n-icon><LogoTwitter /></n-icon></a>
          <a href="#"><n-icon><MailOutline /></n-icon></a>
        </div>
      </div>
      <div class="footer-section">
        <h4>快速链接</h4>
        <router-link to="/products">浏览图书</router-link>
        <router-link to="/coupons">优惠券</router-link>
        <router-link to="/cart">购物车</router-link>
      </div>
      <div class="footer-section">
        <h4>我的账户</h4>
        <router-link to="/orders">我的订单</router-link>
        <router-link to="/profile">个人中心</router-link>
        <router-link to="/messages">消息中心</router-link>
      </div>
      <div class="footer-section">
        <h4>联系我们</h4>
        <p><n-icon size="14"><MailOutline /></n-icon> support@bookverse.com</p>
        <p><n-icon size="14"><CallOutline /></n-icon> 400-888-8888</p>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; {{ new Date().getFullYear() }} BookVerse. 版权所有。</p>
    </div>
  </n-layout-footer>
</template>

<script setup lang="ts">
import { BookOutline, MailOutline, CallOutline, LogoGithub, LogoTwitter } from '@vicons/ionicons5'
</script>

<style scoped>
.app-footer {
  background: rgba(10, 14, 26, 0.95);
  border-top: 1px solid rgba(99, 102, 241, 0.2);
  margin-top: auto;
  padding: 64px 0 32px;
  position: relative;
  z-index: 1;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 48px;
}

.footer-brand {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #f1f5f9, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1, #00d4ff);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
}

.footer-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.8;
  margin-bottom: 24px;
}

.footer-social {
  display: flex;
  gap: 12px;
}

.footer-social a {
  width: 40px;
  height: 40px;
  background: #1e293b;
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: all 0.3s ease;
}

.footer-social a:hover {
  background: linear-gradient(135deg, #6366f1, #00d4ff);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.4);
  transform: translateY(-3px);
}

.footer-section h4 {
  margin-bottom: 24px;
  color: #f1f5f9;
  font-size: 15px;
  font-weight: 600;
}

.footer-section p {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-section a {
  display: block;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 12px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.footer-section a:hover {
  color: #00d4ff;
  padding-left: 5px;
}

.footer-bottom {
  border-top: 1px solid rgba(99, 102, 241, 0.2);
  padding-top: 28px;
  margin-top: 48px;
  text-align: center;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }
}
</style>
