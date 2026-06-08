<%-- ============================================================
     管理后台顶部导航栏 (admin_header.jsp)
     功能：为管理后台各页面提供统一的顶部导航条
     包含：品牌Logo、查看商城链接、消息入口、退出登录
     使用方式：通过 <jsp:include> 在其他页面中引入
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Bootstrap 导航栏组件，深色渐变背景 --%>
<nav class="navbar navbar-default navbar-custom" style="background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); border: none; margin-bottom: 0;">
    <div class="container-fluid">
        <%-- 导航栏左侧：品牌Logo，点击跳转到管理后台首页 --%>
        <div class="navbar-header">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/admin/index" style="color: #fff; font-size: 18px; font-weight: bold;">📚 BookVerse 管理后台</a>
        </div>
        <%-- 导航栏右侧：功能链接 --%>
        <ul class="nav navbar-nav navbar-right">
            <%-- 在新窗口中打开前台商城页面 --%>
            <li><a href="${pageContext.request.contextPath}/" target="_blank" style="color: #a0a0a0;">🌐 查看商城</a></li>
            <%-- 消息中心入口 --%>
            <li><a href="${pageContext.request.contextPath}/admin/message" style="color: #a0a0a0;">💬 消息</a></li>
            <%-- 退出登录，提交到 /admin/logout 接口 --%>
            <li><a href="${pageContext.request.contextPath}/admin/logout" style="color: #a0a0a0;">🚪 退出</a></li>
        </ul>
    </div>
</nav>
