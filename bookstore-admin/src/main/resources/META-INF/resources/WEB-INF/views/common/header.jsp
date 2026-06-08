<%-- ============================================================
     管理后台公共页面模板 (header.jsp)
     功能：提供完整的管理后台页面框架（侧边栏 + 顶部栏 + 内容区）
     特点：
       - 赛博朋克风格的深色主题
       - 左侧固定侧边栏，按功能模块分组导航
       - 顶部粘性顶栏，显示页面标题和快捷操作
       - 使用 Font Awesome 图标库
       - 通过 ${param.title} 接收页面标题参数
     使用方式：在子页面中通过 <%@ include file="..." %> 引入
     注意：此文件包含 <html> 和 <body> 标签，是完整的页面骨架
     ============================================================ --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- 页面标题，通过 param.title 参数传入 --%>
    <title>${param.title} - BookVerse 管理后台</title>
    <%-- 引入 Font Awesome 图标库 --%>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <%-- 引入赛博朋克主题样式表 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cyber-theme.css">
    <style>
        /* ==================== 整体布局 ==================== */
        /* 使用 flex 布局实现左侧边栏 + 右侧内容区的两栏结构 */
        .admin-layout { display: flex; min-height: 100vh; }

        /* ==================== 左侧侧边栏 ==================== */
        .admin-sidebar {
            width: 260px;
            background: rgba(10, 14, 26, 0.98);  /* 深色半透明背景 */
            border-right: 1px solid var(--cyber-border);
            position: fixed;           /* 固定定位，不随页面滚动 */
            top: 0;
            left: 0;
            height: 100vh;             /* 占满整个视口高度 */
            overflow-y: auto;          /* 内容超出时显示滚动条 */
            z-index: 100;
            display: flex;
            flex-direction: column;    /* 垂直排列子元素 */
        }

        /* 侧边栏顶部品牌区域 */
        .sidebar-brand {
            padding: 24px 20px;
            border-bottom: 1px solid var(--cyber-border);
            display: flex;
            align-items: center;
            gap: 12px;
        }
        /* 品牌图标（渐变背景的方形图标） */
        .sidebar-brand .brand-icon {
            width: 40px;
            height: 40px;
            background: linear-gradient(135deg, var(--primary), var(--neon-blue));
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            color: #fff;
            box-shadow: 0 0 20px rgba(99, 102, 241, 0.4);
        }
        /* 品牌名称 */
        .sidebar-brand .brand-text {
            font-size: 18px;
            font-weight: 800;
            color: var(--text-primary);
        }
        /* 品牌副标题 */
        .sidebar-brand .brand-sub {
            font-size: 10px;
            color: var(--text-muted);
            letter-spacing: 2px;
            text-transform: uppercase;
        }

        /* ==================== 侧边栏导航 ==================== */
        .sidebar-nav {
            flex: 1;                   /* 占据剩余空间 */
            padding: 16px 12px;
        }
        /* 导航分组 */
        .sidebar-section {
            margin-bottom: 24px;
        }
        /* 分组标题（如"概览"、"商品管理"） */
        .sidebar-section-title {
            font-size: 11px;
            font-weight: 600;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 1.5px;
            padding: 0 12px;
            margin-bottom: 8px;
        }
        /* 导航链接基础样式 */
        .sidebar-link {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 16px;
            color: var(--text-secondary);
            border-radius: var(--radius);
            font-size: 14px;
            font-weight: 500;
            transition: all 0.3s ease;
            margin-bottom: 2px;
        }
        /* 链接悬停效果 */
        .sidebar-link:hover {
            color: var(--neon-blue);
            background: rgba(0, 212, 255, 0.08);
        }
        /* 当前激活的链接样式（蓝色高亮 + 左侧指示条） */
        .sidebar-link.active {
            color: var(--neon-blue);
            background: rgba(0, 212, 255, 0.1);
            border-left: 3px solid var(--neon-blue);
        }
        /* 链接图标固定宽度 */
        .sidebar-link i {
            width: 20px;
            text-align: center;
            font-size: 15px;
        }

        /* ==================== 侧边栏底部用户信息 ==================== */
        .sidebar-footer {
            padding: 16px 20px;
            border-top: 1px solid var(--cyber-border);
        }
        .sidebar-user {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        /* 用户头像 */
        .sidebar-user .avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--primary), var(--neon-purple));
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-weight: 700;
        }
        .sidebar-user .info { flex: 1; }
        .sidebar-user .name {
            font-size: 13px;
            font-weight: 600;
            color: var(--text-primary);
        }
        .sidebar-user .role {
            font-size: 11px;
            color: var(--text-muted);
        }

        /* ==================== 右侧主内容区 ==================== */
        .admin-main {
            flex: 1;
            margin-left: 260px;        /* 左侧留出侧边栏宽度 */
            background: var(--cyber-bg);
            min-height: 100vh;
        }

        /* ==================== 顶部工具栏 ==================== */
        .admin-topbar {
            height: 64px;
            background: rgba(10, 14, 26, 0.95);
            backdrop-filter: blur(20px);  /* 毛玻璃效果 */
            border-bottom: 1px solid var(--cyber-border);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 32px;
            position: sticky;          /* 粘性定位，滚动时固定在顶部 */
            top: 0;
            z-index: 50;
        }
        /* 顶栏标题 */
        .topbar-title {
            font-size: 18px;
            font-weight: 700;
            color: var(--text-primary);
        }
        /* 顶栏右侧操作区 */
        .topbar-actions {
            display: flex;
            align-items: center;
            gap: 16px;
        }
        /* 顶栏链接样式 */
        .topbar-link {
            color: var(--text-secondary);
            font-size: 14px;
            padding: 8px 16px;
            border-radius: var(--radius);
            transition: all 0.3s ease;
        }
        .topbar-link:hover {
            color: var(--neon-blue);
            background: rgba(0, 212, 255, 0.08);
        }

        /* 主内容区内边距 */
        .admin-content {
            padding: 32px;
        }

        /* ==================== 响应式适配 ==================== */
        /* 小屏幕时隐藏侧边栏 */
        @media (max-width: 768px) {
            .admin-sidebar { transform: translateX(-100%); }
            .admin-main { margin-left: 0; }
        }
    </style>
</head>
<body>
<div class="admin-layout">
    <%-- ==================== 左侧侧边栏 ==================== --%>
    <aside class="admin-sidebar">
        <%-- 品牌标识区域 --%>
        <div class="sidebar-brand">
            <div class="brand-icon"><i class="fas fa-book-open"></i></div>
            <div>
                <div class="brand-text">BookVerse</div>
                <div class="brand-sub">Admin Panel</div>
            </div>
        </div>
        <%-- 导航菜单 --%>
        <nav class="sidebar-nav">
            <%-- 概览模块 --%>
            <div class="sidebar-section">
                <div class="sidebar-section-title">概览</div>
                <a href="${pageContext.request.contextPath}/admin/" class="sidebar-link">
                    <i class="fas fa-tachometer-alt"></i> 数据大屏
                </a>
            </div>
            <%-- 商品管理模块 --%>
            <div class="sidebar-section">
                <div class="sidebar-section-title">商品管理</div>
                <a href="${pageContext.request.contextPath}/admin/product/list" class="sidebar-link">
                    <i class="fas fa-box"></i> 商品列表
                </a>
                <a href="${pageContext.request.contextPath}/admin/product/add" class="sidebar-link">
                    <i class="fas fa-plus-circle"></i> 添加商品
                </a>
                <a href="${pageContext.request.contextPath}/admin/categories" class="sidebar-link">
                    <i class="fas fa-tags"></i> 分类管理
                </a>
            </div>
            <%-- 订单管理模块 --%>
            <div class="sidebar-section">
                <div class="sidebar-section-title">订单管理</div>
                <a href="${pageContext.request.contextPath}/admin/order/list" class="sidebar-link">
                    <i class="fas fa-shopping-bag"></i> 订单列表
                </a>
            </div>
            <%-- 用户管理模块 --%>
            <div class="sidebar-section">
                <div class="sidebar-section-title">用户管理</div>
                <a href="${pageContext.request.contextPath}/admin/user/list" class="sidebar-link">
                    <i class="fas fa-users"></i> 用户列表
                </a>
            </div>
            <%-- 营销模块 --%>
            <div class="sidebar-section">
                <div class="sidebar-section-title">营销</div>
                <a href="${pageContext.request.contextPath}/admin/coupon/list" class="sidebar-link">
                    <i class="fas fa-ticket-alt"></i> 优惠券
                </a>
                <a href="${pageContext.request.contextPath}/admin/announcement/list" class="sidebar-link">
                    <i class="fas fa-bullhorn"></i> 公告管理
                </a>
            </div>
            <%-- 内容模块 --%>
            <div class="sidebar-section">
                <div class="sidebar-section-title">内容</div>
                <a href="${pageContext.request.contextPath}/admin/review/list" class="sidebar-link">
                    <i class="fas fa-star"></i> 评价管理
                </a>
                <a href="${pageContext.request.contextPath}/admin/message/list" class="sidebar-link">
                    <i class="fas fa-envelope"></i> 消息管理
                </a>
            </div>
            <%-- 系统模块 --%>
            <div class="sidebar-section">
                <div class="sidebar-section-title">系统</div>
                <a href="${pageContext.request.contextPath}/admin/log/list" class="sidebar-link">
                    <i class="fas fa-history"></i> 操作日志
                </a>
            </div>
        </nav>
        <%-- 底部用户信息 --%>
        <div class="sidebar-footer">
            <div class="sidebar-user">
                <div class="avatar"><i class="fas fa-user-shield"></i></div>
                <div class="info">
                    <div class="name"><c:out value="${sessionScope.admin.userid}"/></div>
                    <div class="role">管理员</div>
                </div>
            </div>
        </div>
    </aside>
    <%-- ==================== 右侧主内容区 ==================== --%>
    <main class="admin-main">
        <%-- 顶部工具栏，显示页面标题和快捷操作 --%>
        <div class="admin-topbar">
            <div class="topbar-title">${param.title}</div>
            <div class="topbar-actions">
                <a href="${pageContext.request.contextPath}/" class="topbar-link"><i class="fas fa-external-link-alt"></i> 访问前台</a>
                <%-- 退出登录：点击后提交隐藏的表单 --%>
                <a href="javascript:void(0)" onclick="document.getElementById('adminLogoutForm').submit()" class="topbar-link"><i class="fas fa-sign-out-alt"></i> 退出</a>
            </div>
        </div>
        <%-- 内容区域，子页面内容将插入此处 --%>
        <div class="admin-content">
        <%-- 隐藏的退出登录表单：点击"退出"链接时通过JS提交此表单 --%>
        <form id="adminLogoutForm" action="${pageContext.request.contextPath}/admin/logout" method="post" style="display:none;"></form>
