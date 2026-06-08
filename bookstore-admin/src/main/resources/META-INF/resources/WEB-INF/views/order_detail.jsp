<%-- 订单详情页 order_detail.jsp --%>
<%-- 功能：展示单个订单的详细信息，包括订单状态、商品列表、收货地址、支付信息等 --%>
<%-- 用户可在此页面进行支付、确认收货、申请退款等操作 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>订单详情 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        :root {
            --primary: #4f46e5;
            --primary-dark: #3730a3;
            --accent: #f59e0b;
            --danger: #ef4444;
            --success: #10b981;
            --warning: #f59e0b;
            --gray-50: #f9fafb;
            --gray-100: #f3f4f6;
            --gray-200: #e5e7eb;
            --gray-300: #d1d5db;
            --gray-400: #9ca3af;
            --gray-500: #6b7280;
            --gray-600: #4b5563;
            --gray-700: #374151;
            --gray-800: #1f2937;
            --gray-900: #111827;
            --white: #ffffff;
            --shadow: 0 1px 6px rgba(0,0,0,0.08);
            --shadow-lg: 0 8px 30px rgba(0,0,0,0.12);
            --shadow-glass: 0 8px 32px rgba(31, 38, 135, 0.12);
            --radius: 8px;
            --radius-lg: 12px;
            --radius-xl: 16px;
        }
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 30%, #f0f4ff 70%, #faf5ff 100%); min-height: 100vh; color: var(--gray-800); }

        .navbar-custom { background: linear-gradient(135deg, var(--primary), var(--primary-dark)); border: none; border-radius: 0; box-shadow: 0 2px 12px rgba(79,70,229,0.3); }
        .navbar-custom .navbar-brand { color: #fff !important; font-weight: 800; font-size: 20px; }
        .navbar-custom .nav > li > a { color: rgba(255,255,255,0.9) !important; font-weight: 600; transition: all 0.3s; }
        .navbar-custom .nav > li > a:hover { color: #fff !important; background: rgba(255,255,255,0.1) !important; border-radius: 6px; }
        .cart-badge { display: inline-block; background: var(--danger); color: #fff; border-radius: 50%; min-width: 20px; height: 20px; line-height: 20px; text-align: center; font-size: 11px; font-weight: 700; padding: 0 5px; margin-left: 4px; }

        .page-hero {
            background: linear-gradient(135deg, var(--primary) 0%, #6366f1 50%, #8b5cf6 100%);
            border-radius: var(--radius-xl);
            padding: 32px 40px;
            margin-bottom: 28px;
            position: relative;
            overflow: hidden;
            box-shadow: 0 10px 40px rgba(79,70,229,0.25);
            animation: heroSlideIn 0.6s ease-out;
        }
        @keyframes heroSlideIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
        .page-hero::before {
            content: '';
            position: absolute;
            top: -50%; right: -20%;
            width: 400px; height: 400px;
            background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
            border-radius: 50%;
        }
        .page-hero::after {
            content: '';
            position: absolute;
            bottom: -30%; left: -10%;
            width: 300px; height: 300px;
            background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
            border-radius: 50%;
        }
        .page-hero-content { position: relative; z-index: 1; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; }
        .page-hero-title { font-size: 28px; font-weight: 800; color: #fff; margin: 0; display: flex; align-items: center; gap: 12px; text-shadow: 0 2px 8px rgba(0,0,0,0.15); }
        .page-hero-title .hero-icon { font-size: 32px; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.15)); }
        .page-hero-subtitle { color: rgba(255,255,255,0.85); font-size: 15px; margin-top: 8px; font-weight: 500; }

        .btn-back {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 28px;
            border-radius: 50px;
            font-size: 14px;
            font-weight: 700;
            background: rgba(255,255,255,0.2);
            backdrop-filter: blur(10px);
            border: 1.5px solid rgba(255,255,255,0.35);
            color: #fff;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }
        .btn-back::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent); transition: left 0.5s; }
        .btn-back:hover { background: rgba(255,255,255,0.35); color: #fff; text-decoration: none; transform: translateY(-2px); box-shadow: 0 8px 25px rgba(0,0,0,0.2); border-color: rgba(255,255,255,0.5); }
        .btn-back:hover::before { left: 100%; }

        .card-glass {
            background: rgba(255, 255, 255, 0.65);
            backdrop-filter: blur(20px) saturate(180%);
            -webkit-backdrop-filter: blur(20px) saturate(180%);
            border: 1px solid rgba(255, 255, 255, 0.5);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-glass);
            padding: 28px;
            margin-bottom: 20px;
            position: relative;
            overflow: hidden;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            animation: cardFadeIn 0.5s ease-out both;
        }
        .card-glass::before {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0;
            height: 1px;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.8), transparent);
        }
        .card-glass:hover {
            box-shadow: 0 12px 40px rgba(31, 38, 135, 0.15);
            background: rgba(255, 255, 255, 0.8);
            border-color: rgba(255, 255, 255, 0.7);
        }
        @keyframes cardFadeIn { from { opacity: 0; transform: translateY(20px) scale(0.98); } to { opacity: 1; transform: translateY(0) scale(1); } }
        .card-glass:nth-child(2) { animation-delay: 0.1s; }
        .card-glass:nth-child(3) { animation-delay: 0.15s; }
        .card-glass:nth-child(4) { animation-delay: 0.2s; }

        .card-glass h3 { font-size: 18px; font-weight: 700; color: var(--gray-800); margin: 0 0 20px; padding-bottom: 14px; border-bottom: 2px solid rgba(229, 231, 235, 0.5); }

        .status-badge-lg {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 22px;
            border-radius: 20px;
            font-size: 15px;
            font-weight: 700;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .status-badge-lg:hover { transform: scale(1.05); }
        .status-pending { background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); color: #92400e; box-shadow: 0 3px 12px rgba(245,158,11,0.25); }
        .status-paid { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); color: #1e40af; box-shadow: 0 3px 12px rgba(59,130,246,0.25); }
        .status-shipped { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); color: #065f46; box-shadow: 0 3px 12px rgba(16,185,129,0.25); }
        .status-completed { background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%); color: #5b21b6; box-shadow: 0 3px 12px rgba(139,92,246,0.25); }
        .status-cancelled { background: linear-gradient(135deg, var(--gray-200) 0%, var(--gray-300) 100%); color: var(--gray-500); box-shadow: 0 3px 12px rgba(156,163,175,0.2); }

        .status-badge-lg .badge-icon { font-size: 16px; }
        .status-pending .badge-icon { animation: iconPulse 2s ease-in-out infinite; }
        .status-shipped .badge-icon { animation: iconBounce 1.5s ease-in-out infinite; }
        .status-paid .badge-icon { animation: iconShine 2.5s ease-in-out infinite; }
        @keyframes iconPulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.25); } }
        @keyframes iconBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }
        @keyframes iconShine { 0%, 100% { opacity: 1; } 50% { opacity: 0.6; } }

        .timeline { position: relative; padding: 24px 0 24px 36px; }
        .timeline::before {
            content: '';
            position: absolute;
            left: 17px; top: 36px; bottom: 36px;
            width: 3px;
            background: linear-gradient(to bottom, var(--success), var(--gray-200));
            border-radius: 2px;
        }
        .timeline-node { position: relative; padding: 0 0 32px 36px; animation: nodeSlideIn 0.5s ease-out both; }
        .timeline-node:last-child { padding-bottom: 0; }
        @keyframes nodeSlideIn { from { opacity: 0; transform: translateX(-15px); } to { opacity: 1; transform: translateX(0); } }
        .timeline-node:nth-child(1) { animation-delay: 0.1s; }
        .timeline-node:nth-child(2) { animation-delay: 0.2s; }
        .timeline-node:nth-child(3) { animation-delay: 0.3s; }
        .timeline-node:nth-child(4) { animation-delay: 0.4s; }
        .timeline-node:nth-child(5) { animation-delay: 0.5s; }
        .timeline-dot {
            position: absolute;
            left: -28px;
            width: 32px; height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 13px;
            font-weight: 700;
            z-index: 1;
            transition: all 0.3s;
        }
        .timeline-dot.done { background: linear-gradient(135deg, var(--success), #059669); color: #fff; box-shadow: 0 0 0 6px rgba(16,185,129,0.15), 0 4px 12px rgba(16,185,129,0.25); }
        .timeline-dot.active { background: linear-gradient(135deg, var(--primary), #6366f1); color: #fff; box-shadow: 0 0 0 6px rgba(79,70,229,0.15), 0 4px 12px rgba(79,70,229,0.25); animation: pulse 2s infinite; }
        .timeline-dot.pending { background: var(--gray-200); color: var(--gray-400); box-shadow: 0 0 0 6px rgba(229,231,235,0.3); }
        @keyframes pulse { 0%, 100% { box-shadow: 0 0 0 6px rgba(79,70,229,0.15), 0 4px 12px rgba(79,70,229,0.25); } 50% { box-shadow: 0 0 0 12px rgba(79,70,229,0.05), 0 4px 12px rgba(79,70,229,0.25); } }
        .timeline-title { font-size: 15px; font-weight: 700; color: var(--gray-800); margin-bottom: 2px; }
        .timeline-time { font-size: 12px; color: var(--gray-400); font-family: 'SF Mono', 'Consolas', monospace; }
        .timeline-desc { font-size: 13px; color: var(--gray-500); margin-top: 4px; }

        .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
        .info-item { padding: 14px 16px; background: rgba(249,250,251,0.5); border-radius: var(--radius-lg); border: 1px solid rgba(229,231,235,0.3); transition: all 0.3s; }
        .info-item:hover { background: rgba(255,255,255,0.7); border-color: rgba(79,70,229,0.15); box-shadow: 0 2px 12px rgba(79,70,229,0.06); }
        .info-item .info-label { font-size: 12px; font-weight: 600; color: var(--gray-400); text-transform: uppercase; margin-bottom: 6px; letter-spacing: 0.5px; }
        .info-item .info-value { font-size: 14px; font-weight: 600; color: var(--gray-700); }

        .order-table { width: 100%; border-collapse: separate; border-spacing: 0; }
        .order-table thead th { padding: 14px 16px; font-size: 12px; font-weight: 700; color: var(--gray-500); text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid rgba(229,231,235,0.5); text-align: left; background: rgba(249,250,251,0.4); }
        .order-table thead th:first-child { border-radius: var(--radius-lg) 0 0 0; }
        .order-table thead th:last-child { border-radius: 0 var(--radius-lg) 0 0; }
        .order-table tbody td { padding: 16px; border-bottom: 1px solid rgba(229,231,235,0.3); vertical-align: middle; transition: background 0.2s; }
        .order-table tbody tr:hover td { background: rgba(79,70,229,0.03); }
        .order-table tbody tr:last-child td { border-bottom: none; }
        .order-item-img { width: 56px; height: 72px; object-fit: cover; border-radius: 8px; box-shadow: 0 3px 10px rgba(0,0,0,0.1); transition: transform 0.3s; }
        .order-table tbody tr:hover .order-item-img { transform: scale(1.05); }
        .order-item-name { font-weight: 700; color: var(--gray-800); font-size: 14px; }
        .order-item-subtotal { font-weight: 700; color: var(--danger); font-size: 15px; }

        .btn-primary-gradient {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 14px 36px;
            border-radius: 50px;
            font-size: 15px;
            font-weight: 700;
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 4px 15px rgba(79,70,229,0.3);
            position: relative;
            overflow: hidden;
        }
        .btn-primary-gradient::before {
            content: '';
            position: absolute;
            top: 0; left: -100%;
            width: 100%; height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
            transition: left 0.5s;
        }
        .btn-primary-gradient:hover { transform: translateY(-3px); box-shadow: 0 10px 30px rgba(79,70,229,0.4); color: #fff; text-decoration: none; }
        .btn-primary-gradient:hover::before { left: 100%; }

        .btn-outline-danger {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 14px 32px;
            border-radius: 50px;
            font-size: 15px;
            font-weight: 600;
            background: transparent;
            color: var(--danger);
            border: 2px solid var(--danger);
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .btn-outline-danger:hover { background: var(--danger); color: #fff; text-decoration: none; transform: translateY(-3px); box-shadow: 0 8px 24px rgba(239,68,68,0.3); }

        .btn-review {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 8px 18px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 700;
            background: var(--accent);
            color: #fff;
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .btn-review:hover { background: #d97706; color: #fff; text-decoration: none; transform: translateY(-2px); box-shadow: 0 6px 18px rgba(245,158,11,0.35); }

        .action-bar { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; margin-top: 24px; padding: 24px; background: rgba(255,255,255,0.55); backdrop-filter: blur(20px); border-radius: var(--radius-xl); border: 1px solid rgba(255,255,255,0.5); box-shadow: var(--shadow-glass); }

        .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

        @media (max-width: 768px) {
            .two-col { grid-template-columns: 1fr; }
            .info-grid { grid-template-columns: 1fr; }
            .page-hero { padding: 24px 20px; }
            .page-hero-title { font-size: 22px; }
        }
    </style>
</head>
<body>
<nav class="navbar navbar-default navbar-custom navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">📚 BookVerse</a>
        </div>
        <ul class="nav navbar-nav navbar-right" style="margin-right: 20px;">
            <li><a href="${pageContext.request.contextPath}/">首页</a></li>
            <c:if test="${not empty sessionScope.user}">
                <li><a href="${pageContext.request.contextPath}/review/manage">我的评价</a></li>
                <li><a href="${pageContext.request.contextPath}/cart"><span class="glyphicon glyphicon-shopping-cart"></span> 购物车 <span class="cart-badge">${cartSize}</span></a></li>
            </c:if>
        </ul>
    </div>
</nav>
<div style="height: 80px;"></div>

<div class="container">
    <div class="page-hero">
        <div class="page-hero-content">
            <div>
                <h1 class="page-hero-title"><span class="hero-icon">📋</span> 订单详情</h1>
                <p class="page-hero-subtitle">查看订单 ${order.orderid} 的详细信息</p>
            </div>
            <a href="${pageContext.request.contextPath}/order/history" class="btn-back">← 返回订单列表</a>
        </div>
    </div>

    <div class="card-glass">
        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;">
            <span style="font-size: 15px; color: var(--gray-500);">订单号：<strong style="color: var(--gray-800);">${order.orderid}</strong></span>
            <c:choose>
                <c:when test="${order.status == '待支付'}">
                    <span class="status-badge-lg status-pending"><span class="badge-icon">⏳</span> 待支付</span>
                </c:when>
                <c:when test="${order.status == '已支付'}">
                    <span class="status-badge-lg status-paid"><span class="badge-icon">💰</span> 已支付</span>
                </c:when>
                <c:when test="${order.status == '已发货'}">
                    <span class="status-badge-lg status-shipped"><span class="badge-icon">📦</span> 已发货</span>
                </c:when>
                <c:when test="${order.status == '已完成'}">
                    <span class="status-badge-lg status-completed"><span class="badge-icon">✅</span> 已完成</span>
                </c:when>
                <c:when test="${order.status == '已取消'}">
                    <span class="status-badge-lg status-cancelled"><span class="badge-icon">❌</span> 已取消</span>
                </c:when>
                <c:otherwise>
                    <span class="status-badge-lg status-pending"><c:out value="${order.status}"/></span>
                </c:otherwise>
            </c:choose>
        </div>

        <h3>🚚 物流跟踪</h3>
        <div class="timeline">
            <c:set var="timelineStep" value="1" />

            <c:if test="${order.status == '已取消'}">
                <div class="timeline-node">
                    <div class="timeline-dot done">✓</div>
                    <div class="timeline-title">已下单</div>
                    <div class="timeline-time"><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm:ss"/></div>
                    <div class="timeline-desc">订单已生成</div>
                </div>
                <div class="timeline-node">
                    <div class="timeline-dot pending">✕</div>
                    <div class="timeline-title">订单已取消</div>
                    <div class="timeline-time">--</div>
                    <div class="timeline-desc">该订单已被取消</div>
                </div>
            </c:if>

            <c:if test="${order.status != '已取消'}">
                <div class="timeline-node">
                    <div class="timeline-dot done">✓</div>
                    <div class="timeline-title">已下单</div>
                    <div class="timeline-time"><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm:ss"/></div>
                    <div class="timeline-desc">订单已生成，等待支付</div>
                </div>

                <c:if test="${order.status == '已支付' || order.status == '已发货' || order.status == '已完成'}">
                    <c:set var="timelineStep" value="2" />
                </c:if>
                <div class="timeline-node">
                    <c:choose>
                        <c:when test="${order.status == '待支付'}">
                            <div class="timeline-dot active">○</div>
                        </c:when>
                        <c:otherwise>
                            <div class="timeline-dot done">✓</div>
                        </c:otherwise>
                    </c:choose>
                    <div class="timeline-title">已支付</div>
                    <div class="timeline-time">
                        <c:choose>
                            <c:when test="${order.status != '待支付'}"><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm:ss"/></c:when>
                            <c:otherwise>待支付</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="timeline-desc">支付方式：<c:choose>
                        <c:when test="${order.cardtype == 'wechat'}">微信支付</c:when>
                        <c:when test="${order.cardtype == 'alipay'}">支付宝</c:when>
                        <c:when test="${order.cardtype == 'card'}">银行卡</c:when>
                        <c:otherwise>${order.cardtype != null ? order.cardtype : '待支付'}</c:otherwise>
                    </c:choose></div>
                </div>

                <c:if test="${order.status == '已发货' || order.status == '已完成'}">
                    <c:set var="timelineStep" value="3" />
                </c:if>
                <div class="timeline-node">
                    <c:choose>
                        <c:when test="${order.status == '待支付' || order.status == '已支付'}">
                            <div class="timeline-dot pending">○</div>
                        </c:when>
                        <c:otherwise>
                            <div class="timeline-dot done">✓</div>
                        </c:otherwise>
                    </c:choose>
                    <div class="timeline-title">已发货</div>
                    <div class="timeline-time">
                        <c:choose>
                            <c:when test="${order.status == '已发货' || order.status == '已完成'}"><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm:ss"/></c:when>
                            <c:otherwise>等待发货</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="timeline-desc">
                        <c:choose>
                            <c:when test="${order.courier != null}">物流单号：${order.courier}</c:when>
                            <c:otherwise>商品正在打包中</c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <c:if test="${order.status == '已完成'}">
                    <c:set var="timelineStep" value="4" />
                </c:if>
                <div class="timeline-node">
                    <c:choose>
                        <c:when test="${order.status == '已完成'}">
                            <div class="timeline-dot done">✓</div>
                        </c:when>
                        <c:otherwise>
                            <div class="timeline-dot pending">○</div>
                        </c:otherwise>
                    </c:choose>
                    <div class="timeline-title">已完成</div>
                    <div class="timeline-time">
                        <c:choose>
                            <c:when test="${order.status == '已完成'}"><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm:ss"/></c:when>
                            <c:otherwise>等待收货</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="timeline-desc">订单已完成，感谢您的购买！</div>
                </div>
            </c:if>
        </div>
    </div>

    <div class="two-col">
        <div class="card-glass">
            <h3>📋 订单信息</h3>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">订单号</div>
                    <div class="info-value">${order.orderid}</div>
                </div>
                <div class="info-item">
                    <div class="info-label">下单日期</div>
                    <div class="info-value"><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm:ss"/></div>
                </div>
                <div class="info-item">
                    <div class="info-label">订单金额</div>
                    <div class="info-value" style="font-size: 20px; color: var(--danger);">¥<fmt:formatNumber value="${order.totalprice}" pattern="#,##0.00"/></div>
                    <c:if test="${not empty order.originalprice && not empty order.discountamount}">
                        <div style="margin-top:4px;">
                            <span style="font-size:12px;color:var(--gray-400);text-decoration:line-through;">原价 ¥<fmt:formatNumber value="${order.originalprice}" pattern="#0.00"/></span>
                            <span style="font-size:12px;color:#10b981;font-weight:600;margin-left:8px;">🎫 已优惠 ¥<fmt:formatNumber value="${order.discountamount}" pattern="#0.00"/></span>
                        </div>
                    </c:if>
                </div>
                <c:if test="${not empty order.couponname}">
                    <div class="info-item">
                        <div class="info-label">使用优惠券</div>
                        <div class="info-value" style="color:#92400e;background:#fef3c7;display:inline-block;padding:2px 10px;border-radius:10px;font-size:13px;">🎫 ${order.couponname}</div>
                    </div>
                </c:if>
                <div class="info-item">
                    <div class="info-label">支付方式</div>
                    <div class="info-value">
                        <c:choose>
                            <c:when test="${order.cardtype == 'wechat'}">微信支付</c:when>
                            <c:when test="${order.cardtype == 'alipay'}">支付宝</c:when>
                            <c:when test="${order.cardtype == 'card'}">银行卡</c:when>
                            <c:otherwise>${order.cardtype != null ? order.cardtype : '未支付'}</c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="info-item">
                    <div class="info-label">物流单号</div>
                    <div class="info-value">${order.courier != null ? order.courier : '暂无'}</div>
                </div>
            </div>
        </div>

        <div class="card-glass">
            <h3>📍 收货地址</h3>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">收货人</div>
                    <div class="info-value"><c:out value="${order.billtofirstname}"/> <c:out value="${order.billtolastname}"/></div>
                </div>
                <div class="info-item">
                    <div class="info-label">地址</div>
                    <div class="info-value"><c:out value="${order.shipaddr1}"/></div>
                </div>
                <div class="info-item">
                    <div class="info-label">城市</div>
                    <div class="info-value"><c:out value="${order.shipcity}"/></div>
                </div>
                <div class="info-item">
                    <div class="info-label">省份</div>
                    <div class="info-value"><c:out value="${order.shipstate}"/></div>
                </div>
                <div class="info-item">
                    <div class="info-label">邮编</div>
                    <div class="info-value"><c:out value="${order.shipzip}"/></div>
                </div>
            </div>
        </div>
    </div>

    <div class="card-glass">
        <h3>📦 商品明细</h3>
        <c:if test="${not empty orderItems}">
            <table class="order-table">
                <thead>
                    <tr>
                        <th style="width: 70px;">图片</th>
                        <th>图书名称</th>
                        <th style="width: 90px;">单价</th>
                        <th style="width: 70px;">数量</th>
                        <th style="width: 90px;">小计</th>
                        <th style="width: 80px;">操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${orderItems}" var="oi">
                        <tr>
                            <td>
                                <img src="${pageContext.request.contextPath}/img/books/${oi.productId}.jpg"
                                     alt="${oi.productName}" class="order-item-img"
                                     onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2252%22 height=%2268%22><rect fill=%22%23e5e7eb%22 width=%2252%22 height=%2268%22/><text fill=%22%239ca3af%22 font-size=%2210%22 text-anchor=%22middle%22 x=%2226%22 y=%2238%22>Book</text></svg>'" />
                            </td>
                            <td><span class="order-item-name">${oi.productName}</span></td>
                            <td><span style="color: var(--gray-500);">¥<fmt:formatNumber value="${oi.price}" pattern="#0.00"/></span></td>
                            <td><span style="color: var(--gray-500);">×${oi.quantity}</span></td>
                            <td><span class="order-item-subtotal">¥<fmt:formatNumber value="${oi.price * oi.quantity}" pattern="#0.00"/></span></td>
                            <td>
                                <c:if test="${order.status == '已完成'}">
                                    <c:choose>
                                        <c:when test="${orderLevelReviewed || reviewedProducts.contains(oi.productId)}">
                                            <span style="color: var(--success); font-size: 13px; font-weight: 700;">✅ 已评价</span>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/review?productId=${oi.productId}&orderId=${order.orderid}" class="btn-review">⭐ 评价</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${empty orderItems}">
            <p style="text-align: center; color: var(--gray-400); padding: 20px;">暂无商品明细</p>
        </c:if>
    </div>

    <c:if test="${order.status == '待支付'}">
        <div class="action-bar">
            <a href="${pageContext.request.contextPath}/payment?orderId=${order.orderid}" class="btn-primary-gradient">💳 立即支付</a>
            <form action="${pageContext.request.contextPath}/order/cancel" method="post" style="display: inline;">
                <input type="hidden" name="orderId" value="${order.orderid}"/>
                <button type="submit" class="btn-outline-danger" onclick="return confirm('确定取消该订单吗？')">❌ 取消订单</button>
            </form>
        </div>
    </c:if>
</div>

</body>
</html>
