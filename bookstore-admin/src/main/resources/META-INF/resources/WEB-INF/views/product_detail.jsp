<%-- 商品详情页 product_detail.jsp --%>
<%-- 功能：展示单个商品的详细信息，包括图片放大镜、价格、描述、评价、加入购物车等 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${product.name}"/> - BookVerse</title>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cyber-theme.css">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <style>
        /* ========== 详情页专用样式 ========== */

        /* 面包屑 */
        .breadcrumb-cyber { display: flex; align-items: center; gap: 10px; padding: 20px 0; margin: 0; list-style: none; font-size: 13px; }
        .breadcrumb-cyber li a { color: var(--text-muted); transition: all 0.3s; }
        .breadcrumb-cyber li a:hover { color: var(--neon-blue); }
        .breadcrumb-cyber li.sep { color: var(--text-muted); }
        .breadcrumb-cyber li.active { color: var(--neon-blue); font-weight: 700; }

        /* 返回按钮 */
        .btn-back-cyber { display: inline-flex; align-items: center; gap: 8px; padding: 12px 28px; background: rgba(30,41,59,0.8); border: 1px solid var(--cyber-border); color: var(--text-secondary); border-radius: 50px; font-size: 14px; font-weight: 600; cursor: pointer; text-decoration: none; transition: all 0.3s; }
        .btn-back-cyber:hover { border-color: var(--neon-blue); color: var(--neon-blue); box-shadow: 0 0 20px rgba(0,212,255,0.2); transform: translateY(-2px); }

        /* 主产品卡片 */
        .product-main-card { background: var(--cyber-bg-card); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); border: 1px solid var(--cyber-border); border-radius: var(--radius-xl); padding: 36px; margin-bottom: 30px; position: relative; overflow: hidden; }
        .product-main-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 2px; background: linear-gradient(90deg, transparent, var(--neon-blue), var(--neon-purple), transparent); }

        /* 产品画廊 */
        .product-gallery { position: sticky; top: 90px; }
        .magnifier-container { position: relative; overflow: hidden; border-radius: var(--radius-lg); cursor: none; border: 1px solid var(--cyber-border); background: linear-gradient(135deg, var(--cyber-surface), var(--cyber-bg)); }
        .magnifier-container .product-main-img { width: 100%; border-radius: var(--radius-lg); display: block; transition: opacity 0.3s; }
        .magnifier-lens { display: none; position: absolute; width: 140px; height: 140px; border: 2px solid var(--neon-blue); border-radius: 50%; background-repeat: no-repeat; box-shadow: 0 0 20px rgba(0,212,255,0.3), inset 0 0 20px rgba(0,212,255,0.1); pointer-events: none; z-index: 10; }
        .magnifier-result { display: none; position: absolute; top: 0; left: calc(100% + 20px); width: 400px; height: 400px; border-radius: var(--radius-lg); border: 1px solid var(--cyber-border); background-repeat: no-repeat; box-shadow: var(--shadow-neon-lg); z-index: 100; background-color: var(--cyber-bg); }
        .magnifier-container:hover .magnifier-lens { display: block; }
        .magnifier-container:hover .magnifier-result { display: block; }

        /* 产品信息 */
        .product-info-section { padding-left: 30px; }
        .product-name { font-size: 32px; font-weight: 800; color: var(--text-primary); margin-bottom: 8px; line-height: 1.3; background: linear-gradient(135deg, var(--text-primary), var(--neon-blue)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
        .product-author { font-size: 16px; color: var(--text-secondary); margin-bottom: 16px; }

        /* 标签 */
        .product-meta-tags { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
        .product-meta-tags span { display: inline-block; padding: 5px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; backdrop-filter: blur(10px); }
        .tag-category { background: rgba(99,102,241,0.15); color: var(--neon-purple); border: 1px solid rgba(99,102,241,0.3); }
        .tag-hot { background: rgba(239,68,68,0.15); color: #f87171; border: 1px solid rgba(239,68,68,0.3); }

        /* 评分区域 */
        .rating-summary-wrap { display: flex; align-items: center; gap: 16px; padding: 18px 22px; background: rgba(245,158,11,0.08); border-radius: var(--radius-lg); margin-bottom: 20px; border: 1px solid rgba(245,158,11,0.2); backdrop-filter: blur(10px); }
        .rating-summary-wrap .avg-score { font-size: 42px; font-weight: 800; color: var(--accent); line-height: 1; text-shadow: 0 0 20px rgba(245,158,11,0.4); }
        .rating-summary-wrap .stars-display { font-size: 18px; color: var(--accent); letter-spacing: 2px; }
        .rating-summary-wrap .review-count { color: var(--text-muted); font-size: 14px; }

        /* 价格区域 - 霓虹发光 */
        .price-section { margin-bottom: 18px; padding: 22px; background: rgba(0,212,255,0.05); border-radius: var(--radius-lg); border: 1px solid rgba(0,212,255,0.15); position: relative; overflow: hidden; }
        .price-section::before { content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%; background: conic-gradient(transparent, rgba(0,212,255,0.05), transparent 30%); animation: priceRotate 6s linear infinite; pointer-events: none; }
        @keyframes priceRotate { 100% { transform: rotate(360deg); } }
        .price-label { display: inline-block; background: linear-gradient(135deg, var(--danger), #dc2626); color: #fff; font-size: 12px; font-weight: 700; padding: 3px 10px; border-radius: 4px; margin-right: 10px; vertical-align: middle; box-shadow: 0 0 15px rgba(239,68,68,0.4); }
        .product-price-lg { font-size: 44px; font-weight: 800; color: var(--neon-blue); line-height: 1; vertical-align: middle; text-shadow: 0 0 30px rgba(0,212,255,0.5), 0 0 60px rgba(0,212,255,0.2); position: relative; z-index: 1; }
        .product-price-lg::before { content: '\FFE5'; font-size: 26px; }
        .product-price-old { font-size: 18px; color: var(--text-muted); text-decoration: line-through; margin-left: 12px; vertical-align: middle; }
        .product-price-old::before { content: '\FFE5'; font-size: 14px; }
        .price-discount-badge { display: inline-block; background: rgba(245,158,11,0.15); color: var(--accent); font-size: 13px; font-weight: 700; padding: 4px 10px; border-radius: 20px; margin-left: 12px; vertical-align: middle; border: 1px solid rgba(245,158,11,0.3); }
        .price-savings { display: block; margin-top: 8px; font-size: 13px; color: var(--neon-green); font-weight: 600; }

        /* 库存 */
        .stock-badge { display: inline-block; padding: 8px 18px; border-radius: 20px; font-size: 13px; font-weight: 700; margin-bottom: 20px; backdrop-filter: blur(10px); }
        .stock-plenty { background: rgba(16,185,129,0.12); color: var(--neon-green); border: 1px solid rgba(16,185,129,0.3); }
        .stock-tight { background: rgba(245,158,11,0.12); color: var(--accent); border: 1px solid rgba(245,158,11,0.3); }
        .stock-scarce { background: rgba(239,68,68,0.12); color: #f87171; border: 1px solid rgba(239,68,68,0.3); }

        /* 数量选择器 */
        .qty-selector-wrap { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
        .qty-selector-label { font-size: 14px; font-weight: 700; color: var(--text-secondary); min-width: 50px; }
        .qty-selector { display: inline-flex; align-items: center; border: 1px solid var(--cyber-border); border-radius: 10px; overflow: hidden; background: rgba(30,41,59,0.6); }
        .qty-sel-btn { width: 40px; height: 40px; border: none; background: transparent; color: var(--text-secondary); font-size: 20px; font-weight: 700; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; justify-content: center; padding: 0; user-select: none; }
        .qty-sel-btn:hover { background: rgba(99,102,241,0.15); color: var(--neon-blue); }
        .qty-sel-btn:active { transform: scale(0.92); }
        .qty-sel-btn:disabled { opacity: 0.3; cursor: not-allowed; }
        .qty-sel-btn:disabled:hover { background: transparent; color: var(--text-secondary); }
        .qty-sel-input { width: 56px; height: 40px; border: none; border-left: 1px solid var(--cyber-border); border-right: 1px solid var(--cyber-border); text-align: center; font-size: 16px; font-weight: 700; color: var(--text-primary); outline: none; background: transparent; }
        .qty-sel-input:focus { background: rgba(0,212,255,0.05); }
        .qty-stock-hint { font-size: 13px; color: var(--text-muted); }

        /* 加购按钮 */
        .btn-add-cart-lg { display: inline-flex; align-items: center; gap: 8px; padding: 16px 40px; border-radius: 50px; font-size: 17px; font-weight: 700; border: none; cursor: pointer; transition: all 0.3s; margin-right: 12px; background: linear-gradient(135deg, var(--primary), var(--neon-blue)); color: #fff; box-shadow: 0 0 20px rgba(99,102,241,0.3); position: relative; overflow: hidden; }
        .btn-add-cart-lg:hover { transform: translateY(-3px); box-shadow: 0 0 35px rgba(99,102,241,0.5); }
        .btn-add-cart-lg:active { transform: translateY(0); }
        .btn-add-cart-lg .btn-ripple { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.4); transform: scale(0); animation: ripple-effect 0.6s ease-out; pointer-events: none; }
        @keyframes ripple-effect { to { transform: scale(4); opacity: 0; } }
        .btn-add-cart-lg.added { background: linear-gradient(135deg, var(--success), #059669); }
        .btn-buy-now { display: inline-flex; align-items: center; gap: 8px; padding: 16px 40px; border-radius: 50px; font-size: 17px; font-weight: 700; cursor: pointer; transition: all 0.3s; background: linear-gradient(135deg, var(--accent), var(--neon-orange)); color: #fff; border: none; box-shadow: 0 0 20px rgba(245,158,11,0.3); text-decoration: none; }
        .btn-buy-now:hover { transform: translateY(-3px); box-shadow: 0 0 35px rgba(245,158,11,0.5); color: #fff; text-decoration: none; }

        /* 飞入购物车 */
        .fly-dot { position: fixed; width: 20px; height: 20px; border-radius: 50%; background: var(--neon-blue); z-index: 99999; pointer-events: none; box-shadow: 0 0 20px rgba(0,212,255,0.6); }
        .fly-dot::after { content: ''; position: absolute; top: 50%; left: 50%; width: 8px; height: 8px; margin: -4px 0 0 -4px; background: #fff; border-radius: 50%; }

        /* 区块卡片 */
        .section-card { background: var(--cyber-bg-card); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); border: 1px solid var(--cyber-border); border-radius: var(--radius-xl); padding: 30px; margin-bottom: 30px; position: relative; overflow: hidden; transition: all 0.3s; }
        .section-card:hover { border-color: rgba(99,102,241,0.3); box-shadow: 0 0 20px rgba(99,102,241,0.1); }
        .section-card h3 { font-size: 20px; font-weight: 700; color: var(--text-primary); margin: 0 0 20px; padding-bottom: 15px; border-bottom: 1px solid var(--cyber-border); }
        .section-card h3 i { margin-right: 8px; color: var(--neon-blue); }

        /* 详细信息表格 */
        .detail-table { width: 100%; }
        .detail-table td { padding: 12px 15px; border-bottom: 1px solid rgba(99,102,241,0.08); font-size: 14px; }
        .detail-table td:first-child { font-weight: 700; color: var(--text-muted); width: 100px; }
        .detail-table td:last-child { color: var(--text-primary); }

        /* 描述 */
        .desc-content { font-size: 15px; color: var(--text-secondary); line-height: 2; }

        /* 评分分布 */
        .rating-bar-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; font-size: 13px; }
        .rating-bar-row .bar-label { width: 28px; text-align: right; color: var(--text-muted); }
        .rating-bar-row .bar-track { flex: 1; height: 6px; background: rgba(30,41,59,0.8); border-radius: 3px; overflow: hidden; }
        .rating-bar-row .bar-fill { height: 100%; background: linear-gradient(90deg, var(--accent), var(--neon-orange)); border-radius: 3px; transition: width 0.5s; box-shadow: 0 0 8px rgba(245,158,11,0.4); }

        /* 评分概览 */
        .rating-overview-wrap { margin-bottom: 20px; padding: 20px; background: rgba(245,158,11,0.06); border-radius: var(--radius-lg); display: flex; gap: 20px; align-items: center; border: 1px solid rgba(245,158,11,0.12); }

        /* 评价卡片 */
        .review-card { padding: 24px; background: rgba(17,24,39,0.6); backdrop-filter: blur(10px); border: 1px solid var(--cyber-border); border-radius: var(--radius-lg); margin-bottom: 16px; transition: all 0.3s; position: relative; overflow: hidden; }
        .review-card::before { content: ''; position: absolute; top: 0; left: 0; width: 3px; height: 100%; background: linear-gradient(180deg, var(--neon-blue), var(--neon-purple)); opacity: 0; transition: opacity 0.3s; }
        .review-card:hover { border-color: rgba(99,102,241,0.3); box-shadow: 0 0 25px rgba(99,102,241,0.15); transform: translateY(-2px); }
        .review-card:hover::before { opacity: 1; }
        .review-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
        .review-user-info { display: flex; align-items: center; gap: 12px; }
        .review-avatar { width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, var(--primary), var(--neon-blue)); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 18px; box-shadow: 0 0 15px rgba(99,102,241,0.3); }
        .review-user-name { font-weight: 700; color: var(--text-primary); font-size: 14px; }
        .review-time { color: var(--text-muted); font-size: 12px; margin-top: 2px; }
        .review-stars { color: var(--accent); font-size: 16px; letter-spacing: 2px; text-shadow: 0 0 10px rgba(245,158,11,0.3); }
        .review-content { color: var(--text-secondary); line-height: 1.8; margin-bottom: 8px; font-size: 14px; }
        .review-reply { margin-top: 12px; padding: 14px 18px; background: rgba(99,102,241,0.08); border-radius: 10px; border-left: 3px solid var(--neon-blue); color: var(--text-secondary); font-size: 13px; line-height: 1.7; }
        .review-reply strong { color: var(--neon-blue); }
        .review-images { display: flex; gap: 8px; margin-top: 8px; }
        .review-images img { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; cursor: pointer; transition: all 0.3s; border: 1px solid var(--cyber-border); }
        .review-images img:hover { transform: scale(1.08); box-shadow: 0 0 15px rgba(0,212,255,0.3); }
        .review-empty { text-align: center; padding: 40px; color: var(--text-muted); }
        .review-empty .empty-icon { font-size: 48px; margin-bottom: 12px; }

        /* 猜你喜欢 & 相关推荐 */
        .guess-you-like-section .section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid var(--cyber-border); }
        .guess-you-like-section .section-header h3 { margin: 0; padding: 0; border: none; }
        .guess-you-like-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
        .guess-card { background: var(--cyber-bg-card); backdrop-filter: blur(10px); border: 1px solid var(--cyber-border); border-radius: var(--radius-lg); overflow: hidden; transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); cursor: pointer; text-decoration: none; display: block; position: relative; }
        .guess-card:hover { transform: translateY(-8px) scale(1.02); border-color: var(--neon-blue); box-shadow: 0 0 25px rgba(99,102,241,0.2); }
        .guess-card .guess-img-wrap { position: relative; overflow: hidden; height: 200px; background: linear-gradient(135deg, var(--cyber-surface), var(--cyber-bg)); }
        .guess-card .guess-img-wrap img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
        .guess-card:hover .guess-img-wrap img { transform: scale(1.08); }
        .guess-card .guess-tag { position: absolute; top: 8px; left: 8px; background: linear-gradient(135deg, var(--primary), var(--neon-blue)); color: #fff; font-size: 11px; font-weight: 700; padding: 3px 10px; border-radius: 20px; box-shadow: 0 0 10px rgba(99,102,241,0.4); }
        .guess-card .guess-info { padding: 14px; }
        .guess-card .guess-title { font-size: 14px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; transition: color 0.3s; }
        .guess-card:hover .guess-title { color: var(--neon-blue); }
        .guess-card .guess-author { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; }
        .guess-card .guess-price-row { display: flex; align-items: center; justify-content: space-between; }
        .guess-card .guess-price { font-size: 18px; font-weight: 800; color: var(--neon-blue); text-shadow: 0 0 10px rgba(0,212,255,0.3); }
        .guess-card .guess-price::before { content: '\FFE5'; font-size: 13px; }
        .guess-card .guess-sales { font-size: 11px; color: var(--text-muted); }

        .related-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
        .related-card { background: var(--cyber-bg-card); backdrop-filter: blur(10px); border: 1px solid var(--cyber-border); border-radius: var(--radius-lg); overflow: hidden; transition: all 0.3s; cursor: pointer; text-decoration: none; display: block; }
        .related-card:hover { transform: translateY(-6px); border-color: var(--neon-blue); box-shadow: 0 0 20px rgba(99,102,241,0.2); }
        .related-card img { width: 100%; height: 200px; object-fit: cover; background: linear-gradient(135deg, var(--cyber-surface), var(--cyber-bg)); }
        .related-card .related-info { padding: 14px; }
        .related-card .related-title { font-size: 14px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; transition: color 0.3s; }
        .related-card:hover .related-title { color: var(--neon-blue); }
        .related-card .related-author { font-size: 12px; color: var(--text-muted); margin-bottom: 6px; }
        .related-card .related-price { font-size: 16px; font-weight: 700; color: var(--neon-blue); text-shadow: 0 0 8px rgba(0,212,255,0.3); }

        /* 动画 */
        @keyframes pulse-cart { 0% { transform: scale(1); } 50% { transform: scale(1.3); } 100% { transform: scale(1); } }
        .cart-badge-pulse { animation: pulse-cart 0.4s ease; }

        /* 响应式 */
        @media (max-width: 768px) {
            .product-info-section { padding-left: 0; margin-top: 20px; }
            .product-gallery { position: static; }
            .related-grid { grid-template-columns: repeat(2, 1fr); }
            .guess-you-like-grid { grid-template-columns: repeat(2, 1fr); }
            .magnifier-result { display: none !important; }
            .magnifier-lens { display: none !important; }
            .product-main-card { padding: 20px; }
            .section-card { padding: 20px; }
        }
    </style>
</head>
<body>

<!-- 导航栏 -->
<nav class="navbar-cyber">
    <div class="container">
        <a href="${pageContext.request.contextPath}/" class="nav-brand">
            <div class="brand-icon"><i class="fas fa-book-open"></i></div>
            BookVerse<span>ONLINE BOOKSTORE</span>
        </a>
        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/">首页</a>
            <a href="${pageContext.request.contextPath}/order/history">我的订单</a>
        </div>
        <div class="nav-right">
            <a href="${pageContext.request.contextPath}/cart" class="nav-cart">
                <i class="fas fa-shopping-cart"></i>
                <span class="badge" id="navCartBadge">${cartSize > 0 ? cartSize : 0}</span>
            </a>
            <c:if test="${not empty sessionScope.user}">
                <div class="nav-user">
                    <div class="nav-user-btn">
                        <div class="avatar">
                            <img src="${pageContext.request.contextPath}${not empty sessionScope.user.avatar ? sessionScope.user.avatar : '/img/default-book.svg'}" alt="">
                        </div>
                        <span><c:out value="${sessionScope.user.userid}"/></span>
                        <i class="fas fa-chevron-down" style="font-size:10px;"></i>
                    </div>
                    <div class="nav-dropdown">
                        <a href="${pageContext.request.contextPath}/user/profile"><i class="fas fa-user"></i> 个人中心</a>
                        <a href="${pageContext.request.contextPath}/order/history"><i class="fas fa-list"></i> 我的订单</a>
                        <a href="${pageContext.request.contextPath}/message"><i class="fas fa-envelope"></i> 消息</a>
                        <div class="sep"></div>
                        <c:if test="${sessionScope.user.role == 'admin'}">
                            <a href="${pageContext.request.contextPath}/admin/login" style="color:var(--neon-blue);"><i class="fas fa-cog"></i> 管理后台</a>
                        </c:if>
                        <a href="javascript:void(0)" onclick="document.getElementById('logoutForm').submit()"><i class="fas fa-sign-out-alt"></i> 退出登录</a>
                    </div>
                </div>
            </c:if>
            <c:if test="${empty sessionScope.user}">
                <div class="nav-links">
                    <a href="${pageContext.request.contextPath}/login">登录</a>
                    <a href="${pageContext.request.contextPath}/register" style="background:linear-gradient(135deg,var(--primary),var(--neon-blue));color:#fff;padding:8px 20px;border-radius:50px;">注册</a>
                </div>
            </c:if>
        </div>
    </div>
</nav>
<form id="logoutForm" action="${pageContext.request.contextPath}/logout" method="post" style="display:none;"></form>

<div style="height: 20px;"></div>

<div class="container">
    <div style="margin-bottom:20px;">
        <a href="${pageContext.request.contextPath}/" class="btn-back-cyber">
            <i class="fas fa-arrow-left"></i> 返回首页
        </a>
    </div>
    <ul class="breadcrumb-cyber">
        <li><a href="${pageContext.request.contextPath}/">首页</a></li>
        <li class="sep"><i class="fas fa-chevron-right" style="font-size:10px;"></i></li>
        <li><a href="${pageContext.request.contextPath}/">图书列表</a></li>
        <li class="sep"><i class="fas fa-chevron-right" style="font-size:10px;"></i></li>
        <li class="active"><c:out value="${product.name}"/></li>
    </ul>

    <div class="product-main-card">
        <div class="row">
            <div class="col-md-5 product-gallery">
                <div class="magnifier-container" id="magnifierContainer">
                    <img src="${pageContext.request.contextPath}/img/books/${product.productid}.jpg"
                         alt="${product.name}" class="product-main-img" id="mainImage"
                         onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22400%22 height=%22500%22><rect fill=%22%231e293b%22 width=%22400%22 height=%22500%22/><text fill=%22%2364748b%22 font-size=%2224%22 text-anchor=%22middle%22 x=%22200%22 y=%22260%22>No Image</text></svg>'" />
                    <div class="magnifier-lens" id="magnifierLens"></div>
                    <div class="magnifier-result" id="magnifierResult"></div>
                </div>
            </div>
            <div class="col-md-7 product-info-section">
                <div class="product-meta-tags">
                    <span class="tag-category"><i class="fas fa-folder"></i> <c:out value="${product.category}"/></span>
                    <c:if test="${product.sales > 100}">
                        <span class="tag-hot"><i class="fas fa-fire"></i> 热销</span>
                    </c:if>
                </div>

                <h1 class="product-name"><c:out value="${product.name}"/></h1>
                <div class="product-author"><i class="fas fa-pen-fancy" style="margin-right:6px;color:var(--text-muted);"></i>作者：<c:out value="${product.author}"/></div>

                <c:if test="${not empty avgRating}">
                    <c:set var="ar" value="${avgRating}" />
                    <div class="rating-summary-wrap">
                        <span class="avg-score"><fmt:formatNumber value="${ar}" pattern="#0.0"/></span>
                        <div>
                            <div class="stars-display">
                                <c:forEach begin="1" end="5" var="s">
                                    <c:choose>
                                        <c:when test="${s <= ar}"><i class="fas fa-star"></i></c:when>
                                        <c:when test="${s - 0.5 <= ar}"><i class="fas fa-star-half-alt"></i></c:when>
                                        <c:otherwise><i class="far fa-star"></i></c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </div>
                            <span class="review-count">${fn:length(reviews)} 条评价</span>
                        </div>
                    </div>
                </c:if>

                <div class="price-section">
                    <span class="price-label"><i class="fas fa-bolt"></i> 活动价</span>
                    <span class="product-price-lg" id="currentPrice"><fmt:formatNumber value="${product.price}" pattern="#0.00"/></span>
                    <span class="product-price-old" id="originalPrice"></span>
                    <span class="price-discount-badge" id="discountBadge"></span>
                    <span class="price-savings" id="savingsText"></span>
                </div>

                <c:choose>
                    <c:when test="${product.stock > 100}">
                        <span class="stock-badge stock-plenty"><i class="fas fa-check-circle"></i> 现货充足 (库存: ${product.stock})</span>
                    </c:when>
                    <c:when test="${product.stock >= 10}">
                        <span class="stock-badge stock-tight"><i class="fas fa-exclamation-circle"></i> 库存紧张 (库存: ${product.stock})</span>
                    </c:when>
                    <c:when test="${product.stock > 0}">
                        <span class="stock-badge stock-scarce"><i class="fas fa-fire-alt"></i> 即将售罄 (库存: ${product.stock})</span>
                    </c:when>
                    <c:otherwise>
                        <span class="stock-badge stock-scarce"><i class="fas fa-times-circle"></i> 暂时缺货</span>
                    </c:otherwise>
                </c:choose>

                <p class="desc-content" style="margin-bottom: 25px;"><c:out value="${product.descn}"/></p>

                <c:if test="${product.stock > 0}">
                <div class="qty-selector-wrap">
                    <span class="qty-selector-label">数量</span>
                    <div class="qty-selector">
                        <button type="button" class="qty-sel-btn" id="qtyMinus" disabled>−</button>
                        <input type="text" class="qty-sel-input" id="qtyInput" value="1" readonly />
                        <button type="button" class="qty-sel-btn" id="qtyPlus">+</button>
                    </div>
                    <span class="qty-stock-hint">库存 ${product.stock} 件</span>
                </div>
                </c:if>

                <div>
                    <c:if test="${product.stock > 0}">
                        <button onclick="addToCartDetail('<c:out value="${product.productid}"/>', '<c:out value="${product.name}" escapeXml="true"/>')" class="btn-add-cart-lg" id="btnAddCart"><i class="fas fa-cart-plus"></i> 加入购物车</button>
                        <a href="${pageContext.request.contextPath}/cart/add?id=${product.productid}" class="btn-buy-now"><i class="fas fa-bolt"></i> 立即购买</a>
                    </c:if>
                    <c:if test="${product.stock <= 0}">
                        <button class="btn-add-cart-lg" disabled style="opacity: 0.5; cursor: not-allowed;"><i class="fas fa-times"></i> 暂时缺货</button>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <div class="section-card">
        <h3><i class="fas fa-book"></i> 图书详细信息</h3>
        <table class="detail-table">
            <tr>
                <td>书名</td>
                <td><c:out value="${product.name}"/></td>
            </tr>
            <tr>
                <td>作者</td>
                <td><c:out value="${product.author}"/></td>
            </tr>
            <tr>
                <td>分类</td>
                <td><c:out value="${product.category}"/></td>
            </tr>
            <tr>
                <td>ISBN</td>
                <td>${product.productid}</td>
            </tr>
            <tr>
                <td>价格</td>
                <td style="font-weight: 700; color: var(--neon-blue); text-shadow: 0 0 10px rgba(0,212,255,0.3);">¥<fmt:formatNumber value="${product.price}" pattern="#0.00"/></td>
            </tr>
            <tr>
                <td>库存</td>
                <td>${product.stock} 件</td>
            </tr>
            <tr>
                <td>销量</td>
                <td>${product.sales} 件</td>
            </tr>
        </table>
    </div>

    <div class="section-card" id="reviewsSection">
        <h3><i class="fas fa-star"></i> 用户评价 <span style="font-size: 14px; color: var(--text-muted); font-weight: 400; margin-left: 4px;" id="reviewTotal"></span></h3>

        <c:if test="${not empty avgRating}">
            <div class="rating-overview-wrap">
                <div style="text-align: center;">
                    <div style="font-size: 48px; font-weight: 800; color: var(--accent); line-height: 1; text-shadow: 0 0 20px rgba(245,158,11,0.4);" id="avgRatingDisplay">
                        <fmt:formatNumber value="${avgRating}" pattern="#0.0"/>
                    </div>
                    <div style="font-size: 14px; color: var(--accent); margin-top: 4px;">综合评分</div>
                </div>
                <div style="flex: 1;" id="ratingBars">
                    <div class="rating-bar-row">
                        <span class="bar-label">5<i class="fas fa-star" style="font-size:10px;color:var(--accent);"></i></span>
                        <div class="bar-track"><div class="bar-fill" style="width: 0%;"></div></div>
                    </div>
                    <div class="rating-bar-row">
                        <span class="bar-label">4<i class="fas fa-star" style="font-size:10px;color:var(--accent);"></i></span>
                        <div class="bar-track"><div class="bar-fill" style="width: 0%;"></div></div>
                    </div>
                    <div class="rating-bar-row">
                        <span class="bar-label">3<i class="fas fa-star" style="font-size:10px;color:var(--accent);"></i></span>
                        <div class="bar-track"><div class="bar-fill" style="width: 0%;"></div></div>
                    </div>
                    <div class="rating-bar-row">
                        <span class="bar-label">2<i class="fas fa-star" style="font-size:10px;color:var(--accent);"></i></span>
                        <div class="bar-track"><div class="bar-fill" style="width: 0%;"></div></div>
                    </div>
                    <div class="rating-bar-row">
                        <span class="bar-label">1<i class="fas fa-star" style="font-size:10px;color:var(--accent);"></i></span>
                        <div class="bar-track"><div class="bar-fill" style="width: 0%;"></div></div>
                    </div>
                </div>
            </div>
        </c:if>

        <div id="reviewsContainer">
            <div class="review-empty">
                <div class="empty-icon"><i class="fas fa-spinner fa-spin"></i></div>
                <p>加载评价中...</p>
            </div>
        </div>
    </div>

    <c:if test="${not empty guessYouLike}">
    <div class="section-card guess-you-like-section">
        <div class="section-header">
            <h3><i class="fas fa-magic"></i> 猜你喜欢</h3>
            <a href="${pageContext.request.contextPath}/" style="font-size: 14px; color: var(--neon-blue); font-weight: 600; text-decoration: none;">查看更多 <i class="fas fa-chevron-right"></i></a>
        </div>
        <div class="guess-you-like-grid">
            <c:forEach items="${guessYouLike}" var="gl" begin="0" end="4">
                <a href="${pageContext.request.contextPath}/product/detail?id=${gl.productid}" class="guess-card">
                    <div class="guess-img-wrap">
                        <img src="${pageContext.request.contextPath}/img/books/${gl.productid}.jpg"
                             alt="${gl.name}"
                             onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22260%22><rect fill=%22%231e293b%22 width=%22200%22 height=%22260%22/><text fill=%22%2364748b%22 font-size=%2214%22 text-anchor=%22middle%22 x=%22100%22 y=%22135%22>Book</text></svg>'" />
                        <span class="guess-tag"><i class="fas fa-star" style="margin-right:3px;"></i>推荐</span>
                    </div>
                    <div class="guess-info">
                        <div class="guess-title">${gl.name}</div>
                        <div class="guess-author">${gl.author}</div>
                        <div class="guess-price-row">
                            <span class="guess-price"><fmt:formatNumber value="${gl.price}" pattern="#0.00"/></span>
                            <span class="guess-sales">已售 ${gl.sales}</span>
                        </div>
                    </div>
                </a>
            </c:forEach>
        </div>
    </div>
    </c:if>

    <c:if test="${not empty relatedBooks}">
    <div class="section-card related-section">
        <h3><i class="fas fa-link"></i> 相关推荐</h3>
        <div class="related-grid">
            <c:forEach items="${relatedBooks}" var="rb" begin="0" end="3">
                <a href="${pageContext.request.contextPath}/product/detail?id=${rb.productid}" class="related-card">
                    <img src="${pageContext.request.contextPath}/img/books/${rb.productid}.jpg"
                         alt="${rb.name}"
                         onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22260%22><rect fill=%22%231e293b%22 width=%22200%22 height=%22260%22/><text fill=%22%2364748b%22 font-size=%2214%22 text-anchor=%22middle%22 x=%22100%22 y=%22135%22>Book</text></svg>'" />
                    <div class="related-info">
                        <div class="related-title">${rb.name}</div>
                        <div class="related-author">${rb.author}</div>
                        <div class="related-price">¥<fmt:formatNumber value="${rb.price}" pattern="#0.00"/></div>
                    </div>
                </a>
            </c:forEach>
        </div>
    </div>
    </c:if>
</div>

<script>
    $(function() {
        var currentPrice = parseFloat('<c:out value="${product.price}"/>');
        var stock = parseInt('<c:out value="${product.stock}"/>');

        var originalPrice = (currentPrice * 1.3).toFixed(2);
        var discount = (currentPrice / (currentPrice * 1.3) * 10).toFixed(1);
        var savings = (currentPrice * 1.3 - currentPrice).toFixed(2);
        $('#originalPrice').text(originalPrice);
        $('#discountBadge').text(discount + '折');
        $('#savingsText').text('已省 ¥' + savings);

        var $qtyInput = $('#qtyInput');
        var $qtyMinus = $('#qtyMinus');
        var $qtyPlus = $('#qtyPlus');

        $qtyPlus.on('click', function() {
            var val = parseInt($qtyInput.val());
            if (val < stock) {
                $qtyInput.val(val + 1);
                $qtyMinus.prop('disabled', false);
                if (val + 1 >= stock) {
                    $qtyPlus.prop('disabled', true);
                }
            }
        });

        $qtyMinus.on('click', function() {
            var val = parseInt($qtyInput.val());
            if (val > 1) {
                $qtyInput.val(val - 1);
                $qtyPlus.prop('disabled', false);
                if (val - 1 <= 1) {
                    $qtyMinus.prop('disabled', true);
                }
            }
        });
    });

    (function() {
        var container = document.getElementById('magnifierContainer');
        var img = document.getElementById('mainImage');
        var lens = document.getElementById('magnifierLens');
        var result = document.getElementById('magnifierResult');
        var zoom = 2.5;

        function initMagnifier() {
            if (!img.complete || !img.naturalWidth) {
                setTimeout(initMagnifier, 100);
                return;
            }
            var bgWidth = img.width * zoom;
            var bgHeight = img.height * zoom;
            result.style.backgroundSize = bgWidth + 'px ' + bgHeight + 'px';
            lens.style.backgroundSize = bgWidth + 'px ' + bgHeight + 'px';
        }

        img.addEventListener('load', initMagnifier);
        if (img.complete) initMagnifier();

        container.addEventListener('mousemove', function(e) {
            var rect = container.getBoundingClientRect();
            var x = e.clientX - rect.left;
            var y = e.clientY - rect.top;

            var lensW = lens.offsetWidth / 2;
            var lensH = lens.offsetHeight / 2;

            var lx = Math.max(lensW, Math.min(x, rect.width - lensW));
            var ly = Math.max(lensH, Math.min(y, rect.height - lensH));

            lens.style.left = (lx - lensW) + 'px';
            lens.style.top = (ly - lensH) + 'px';

            var ratioX = lx / rect.width;
            var ratioY = ly / rect.height;

            var bgX = -(ratioX * img.width * zoom - result.offsetWidth / 2);
            var bgY = -(ratioY * img.height * zoom - result.offsetHeight / 2);
            result.style.backgroundImage = 'url(' + img.src + ')';
            result.style.backgroundPosition = bgX + 'px ' + bgY + 'px';

            lens.style.backgroundImage = 'url(' + img.src + ')';
            var lensBgX = -(lx * zoom - lensW);
            var lensBgY = -(ly * zoom - lensH);
            lens.style.backgroundPosition = lensBgX + 'px ' + lensBgY + 'px';
        });

        container.addEventListener('mouseleave', function() {
            lens.style.display = 'none';
            result.style.display = 'none';
        });

        container.addEventListener('mouseenter', function() {
            lens.style.display = 'block';
            if (window.innerWidth > 768) {
                result.style.display = 'block';
            }
        });
    })();

    function flyToCart(startEl) {
        var $start = $(startEl);
        var startOffset = $start.offset();
        var $target = $('#navCartBadge');
        if (!$target.length) {
            $target = $('#navCartLink');
        }
        if (!$target.length) return;
        var targetOffset = $target.offset();

        var $dot = $('<div class="fly-dot"></div>');
        $dot.css({
            left: startOffset.left + $start.outerWidth() / 2 - 10,
            top: startOffset.top + $start.outerHeight() / 2 - 10
        });
        $('body').append($dot);

        $dot.animate({
            left: targetOffset.left + $target.width() / 2 - 10,
            top: targetOffset.top + $target.height() / 2 - 10,
            width: 12,
            height: 12,
            opacity: 0.6
        }, 600, 'swing', function() {
            $dot.remove();
            $target.addClass('cart-badge-pulse');
            setTimeout(function() { $target.removeClass('cart-badge-pulse'); }, 400);
        });
    }

    function addToCartDetail(productId, productName) {
        var quantity = parseInt($('#qtyInput').val()) || 1;
        var btn = document.getElementById('btnAddCart');

        var ripple = document.createElement('span');
        ripple.className = 'btn-ripple';
        var rect = btn.getBoundingClientRect();
        ripple.style.width = ripple.style.height = Math.max(rect.width, rect.height) + 'px';
        ripple.style.left = '50%';
        ripple.style.top = '50%';
        ripple.style.marginLeft = -(Math.max(rect.width, rect.height) / 2) + 'px';
        ripple.style.marginTop = -(Math.max(rect.width, rect.height) / 2) + 'px';
        btn.appendChild(ripple);
        setTimeout(function() { ripple.remove(); }, 600);

        flyToCart(btn);

        $.get('${pageContext.request.contextPath}/cart/add/ajax', {productId: productId, quantity: quantity}, function(data) {
            if (data.success) {
                var $btn = $('#btnAddCart');
                $btn.addClass('added').html('<i class="fas fa-check"></i> 已加入购物车');
                setTimeout(function() {
                    $btn.removeClass('added').html('<i class="fas fa-cart-plus"></i> 加入购物车');
                }, 2000);

                var tip = $('<div style="position:fixed;top:80px;left:50%;transform:translateX(-50%);z-index:9999;background:rgba(17,24,39,0.95);backdrop-filter:blur(20px);border:1px solid rgba(99,102,241,0.3);color:var(--text-primary,#f1f5f9);padding:14px 28px;border-radius:50px;box-shadow:0 0 25px rgba(99,102,241,0.3);font-weight:600;font-size:14px;"><i class="fas fa-check-circle" style="color:var(--neon-green,#34d399);margin-right:8px;"></i>已将 "' + productName + '" x' + quantity + ' 加入购物车！<a href="${pageContext.request.contextPath}/cart" style="color:var(--neon-blue,#00d4ff);font-weight:700;margin-left:10px;text-decoration:underline;">去结算</a></div>');
                $('body').append(tip);
                setTimeout(function() { tip.fadeOut(400, function() { tip.remove(); }); }, 3000);

                var badge = $('#navCartBadge');
                if (badge.length) {
                    var current = parseInt(badge.text()) || 0;
                    badge.text(current + quantity);
                }
            } else {
                alert('❌ ' + data.message);
            }
        }).fail(function() { alert('❌ 加入购物车失败！'); });
    }

    $(function() {
        $.ajax({
            url: '${pageContext.request.contextPath}/api/review/list',
            data: { productid: '${product.productid}' },
            dataType: 'json',
            success: function(data) {
                var container = $('#reviewsContainer');
                if (!data || !data.reviews || data.reviews.length === 0) {
                    container.html('<div class="review-empty"><div class="empty-icon"><i class="fas fa-comment-slash"></i></div><p>暂无评价，成为第一个评价的人吧！</p></div>');
                    $('#reviewTotal').text('(0 条)');
                    return;
                }
                var reviews = data.reviews;
                container.empty();
                var count = data.reviewCount || reviews.length;
                $('#reviewTotal').text('(' + count + ' 条)');
                if (data.avgRating !== undefined && data.avgRating !== null) {
                    $('#avgRatingDisplay').text(Number(data.avgRating).toFixed(1));
                }

                var ratingDist = [0, 0, 0, 0, 0];
                $.each(reviews, function(i, r) {
                    if (r.rating >= 1 && r.rating <= 5) ratingDist[r.rating - 1]++;
                });
                var maxCount = Math.max.apply(null, ratingDist) || 1;
                for (var i = 4; i >= 0; i--) {
                    var pct = (ratingDist[i] / maxCount * 100).toFixed(0);
                    $('#ratingBars .rating-bar-row').eq(4 - i).find('.bar-fill').css('width', pct + '%');
                }

                $.each(data.reviews, function(i, r) {
                    var starsHtml = '';
                    for (var s = 1; s <= 5; s++) {
                        starsHtml += s <= r.rating ? '<i class="fas fa-star"></i>' : '<i class="far fa-star"></i>';
                    }
                    var timeStr = r.createTime || '';
                    var avatarColors = ['#6366f1', '#8b5cf6', '#a78bfa', '#00d4ff', '#22d3ee', '#34d399', '#f472b6'];
                    var colorIdx = (r.userId ? r.userId.charCodeAt(0) : 0) % avatarColors.length;
                    var card = $('<div class="review-card"></div>');
                    var header = $('<div class="review-header"></div>');
                    var userInfo = $('<div class="review-user-info"><div class="review-avatar" style="background:linear-gradient(135deg,' + avatarColors[colorIdx] + ',' + avatarColors[(colorIdx + 1) % avatarColors.length] + ')">' + (r.userId ? r.userId.charAt(0).toUpperCase() : 'U') + '</div><div><div class="review-user-name">' + (r.userId || '匿名用户') + '</div><div class="review-time">' + timeStr + '</div></div></div>');
                    header.append(userInfo).append('<div class="review-stars">' + starsHtml + '</div>');
                    card.append(header);
                    card.append('<div class="review-content">' + (r.content || '') + '</div>');
                    if (r.image) {
                        card.append('<div class="review-images"><img src="' + r.image + '" alt="review image" onerror="this.style.display=\'none\'" /></div>');
                    }
                    if (r.reply) {
                        card.append('<div class="review-reply"><strong><i class="fas fa-reply"></i> 商家回复：</strong>' + r.reply + '</div>');
                    }
                    container.append(card);
                });
            },
            error: function() {
                $('#reviewsContainer').html('<div class="review-empty"><div class="empty-icon"><i class="fas fa-exclamation-triangle"></i></div><p>评价加载失败，请稍后重试</p></div>');
            }
        });
    });
</script>

</body>
</html>
