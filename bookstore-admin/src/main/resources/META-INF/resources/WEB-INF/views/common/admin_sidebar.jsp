<%-- ============================================================
     管理后台侧边栏 (admin_sidebar.jsp)
     功能：为管理后台各页面提供统一的左侧导航菜单
     特点：
       - 通过 ${currentPage} 变量判断当前页面，自动高亮对应菜单项
       - 包含所有管理功能入口：数据大屏、图书、订单、用户、评价等
       - 底部提供"查看商城"和"退出登录"操作
     使用方式：通过 <jsp:include> 在其他页面中引入
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 获取当前页面的 URI 路径，用于判断哪个菜单项应该高亮 --%>
<c:set var="currentPage" value="${pageContext.request.requestURI}"/>
<%-- 侧边栏容器，深色背景，固定在左侧 --%>
<div class="col-md-2 sidebar" style="background: #1a1a2e; min-height: calc(100vh - 50px); padding: 20px 0; color: #fff;">
    <div style="padding: 0 15px;">
        <%-- 每个链接通过 contains() 判断当前路径是否匹配，匹配则添加 active 样式高亮 --%>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="${currentPage.contains('/admin/dashboard') ? 'active' : ''}">📊 数据大屏</a>
        <a href="${pageContext.request.contextPath}/admin/index" class="${currentPage.contains('/admin/index') && !currentPage.contains('/admin/index/dashboard') ? 'active' : ''}">🏠 首页概览</a>
        <a href="${pageContext.request.contextPath}/admin/product" class="${currentPage.contains('/admin/product') && !currentPage.contains('/admin/product/bestseller') && !currentPage.contains('/admin/product/stock') ? 'active' : ''}">📦 图书管理</a>
        <a href="${pageContext.request.contextPath}/admin/product/bestseller" class="${currentPage.contains('/admin/product/bestseller') ? 'active' : ''}">🔥 热销排行</a>
        <a href="${pageContext.request.contextPath}/admin/product/stock" class="${currentPage.contains('/admin/product/stock') ? 'active' : ''}">📦 库存管理</a>
        <a href="${pageContext.request.contextPath}/admin/order" class="${currentPage.contains('/admin/order') ? 'active' : ''}">📋 订单管理</a>
        <a href="${pageContext.request.contextPath}/admin/user" class="${currentPage.contains('/admin/user') ? 'active' : ''}">👥 用户管理</a>
        <a href="${pageContext.request.contextPath}/admin/review" class="${currentPage.contains('/admin/review') ? 'active' : ''}">⭐ 评价管理</a>
        <a href="${pageContext.request.contextPath}/admin/coupon" class="${currentPage.contains('/admin/coupon') ? 'active' : ''}">🎫 优惠券管理</a>
        <a href="${pageContext.request.contextPath}/admin/message" class="${currentPage.contains('/admin/message') ? 'active' : ''}">💬 消息中心</a>
        <a href="${pageContext.request.contextPath}/admin/announcement" class="${currentPage.contains('/admin/announcement') ? 'active' : ''}">📢 公告管理</a>
        <a href="${pageContext.request.contextPath}/admin/log" class="${currentPage.contains('/admin/log') ? 'active' : ''}">📝 操作日志</a>
        <%-- 分割线 --%>
        <hr style="border-color: #333;">
        <%-- 底部操作链接 --%>
        <a href="${pageContext.request.contextPath}/" target="_blank">🌐 查看商城</a>
        <a href="${pageContext.request.contextPath}/admin/logout">🚪 退出登录</a>
    </div>
</div>
<%-- 侧边栏样式定义 --%>
<style>
    /* 侧边栏链接基础样式 */
    .sidebar a {
        display: block;
        padding: 10px 15px;
        color: #a0a0a0;              /* 默认灰色文字 */
        text-decoration: none;
        border-radius: 5px;
        margin-bottom: 5px;
        transition: all 0.2s;        /* 平滑过渡动画 */
    }
    /* 链接悬停效果：背景变亮，文字变白 */
    .sidebar a:hover {
        background: rgba(255,255,255,0.1);
        color: #fff;
    }
    /* 当前激活的菜单项：紫色渐变背景，白色文字 */
    .sidebar a.active {
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: #fff;
    }
</style>
