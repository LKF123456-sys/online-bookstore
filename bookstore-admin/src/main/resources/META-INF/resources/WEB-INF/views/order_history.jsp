<%-- 订单历史页 order_history.jsp --%>
<%-- 功能：展示用户的所有历史订单列表，支持按状态筛选、分页查看 --%>
<%-- 用户可在此页面查看订单状态、进入订单详情、进行支付等操作 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的订单 - BookVerse</title>
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
            padding: 36px 40px;
            margin-bottom: 28px;
            position: relative;
            overflow: hidden;
            box-shadow: 0 10px 40px rgba(79,70,229,0.25);
        }
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
        .page-hero-stats { display: flex; gap: 24px; }
        .hero-stat { text-align: center; }
        .hero-stat-value { font-size: 28px; font-weight: 800; color: #fff; text-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .hero-stat-label { font-size: 12px; color: rgba(255,255,255,0.75); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }

        .tabs-container {
            background: rgba(255, 255, 255, 0.55);
            backdrop-filter: blur(20px) saturate(180%);
            -webkit-backdrop-filter: blur(20px) saturate(180%);
            border: 1px solid rgba(255, 255, 255, 0.5);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-glass);
            padding: 8px;
            margin-bottom: 24px;
            overflow-x: auto;
            position: relative;
        }
        .tabs-container::before {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0;
            height: 1px;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.9), transparent);
        }
        .tabs-list { display: flex; gap: 4px; list-style: none; margin: 0; padding: 0; }
        .tabs-list li { flex-shrink: 0; }
        .tab-btn {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 10px 20px;
            border-radius: var(--radius-lg);
            font-size: 14px;
            font-weight: 700;
            color: var(--gray-500);
            text-decoration: none;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            border: none;
            background: none;
            white-space: nowrap;
        }
        .tab-btn:hover { color: var(--primary); background: rgba(79,70,229,0.06); }
        .tab-btn.active {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            box-shadow: 0 4px 14px rgba(79,70,229,0.35);
        }
        .tab-btn.active:hover { box-shadow: 0 6px 18px rgba(79,70,229,0.45); }
        .tab-count {
            display: inline-block;
            background: var(--gray-100);
            color: var(--gray-500);
            border-radius: 10px;
            padding: 2px 8px;
            font-size: 12px;
            font-weight: 700;
            margin-left: 2px;
            transition: all 0.3s;
        }
        .tab-btn.active .tab-count { background: rgba(255,255,255,0.25); color: #fff; }
        .tab-icon { font-size: 15px; line-height: 1; }

        .filter-no-result-card {
            display: none;
            text-align: center;
            padding: 60px 20px;
            background: rgba(255,255,255,0.55);
            backdrop-filter: blur(20px) saturate(180%);
            border: 1px solid rgba(255,255,255,0.5);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-glass);
            animation: cardFadeIn 0.4s ease-out;
        }
        .filter-no-result-card .fnr-icon { font-size: 56px; margin-bottom: 16px; display: block; animation: emptyFloat 3s ease-in-out infinite; }
        .filter-no-result-card h3 { font-size: 20px; color: var(--gray-700); margin: 0 0 8px; font-weight: 700; }
        .filter-no-result-card p { color: var(--gray-400); font-size: 14px; margin: 0; }

        .order-card {
            background: rgba(255, 255, 255, 0.55);
            backdrop-filter: blur(20px) saturate(180%);
            -webkit-backdrop-filter: blur(20px) saturate(180%);
            border: 1px solid rgba(255, 255, 255, 0.5);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-glass);
            margin-bottom: 16px;
            overflow: hidden;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            animation: cardFadeIn 0.5s ease-out both;
        }
        .order-card::before {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0;
            height: 1px;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.8), transparent);
            z-index: 1;
        }
        .order-card:hover {
            box-shadow: 0 12px 40px rgba(31, 38, 135, 0.18);
            transform: translateY(-4px);
            background: rgba(255, 255, 255, 0.8);
            border-color: rgba(255, 255, 255, 0.7);
        }
        @keyframes cardFadeIn {
            from { opacity: 0; transform: translateY(20px) scale(0.98); }
            to { opacity: 1; transform: translateY(0) scale(1); }
        }
        .order-card:nth-child(1) { animation-delay: 0.05s; }
        .order-card:nth-child(2) { animation-delay: 0.1s; }
        .order-card:nth-child(3) { animation-delay: 0.15s; }
        .order-card:nth-child(4) { animation-delay: 0.2s; }
        .order-card:nth-child(5) { animation-delay: 0.25s; }
        .order-card:nth-child(n+6) { animation-delay: 0.3s; }

        .order-card-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 18px 24px;
            background: rgba(249, 250, 251, 0.5);
            backdrop-filter: blur(10px);
            border-bottom: 1px solid rgba(229, 231, 235, 0.5);
            flex-wrap: wrap;
            gap: 10px;
        }
        .order-card-header .order-no { font-size: 14px; color: var(--gray-500); }
        .order-card-header .order-no strong { color: var(--gray-800); margin-left: 6px; font-family: 'SF Mono', 'Consolas', 'Courier New', monospace; }
        .order-card-header .order-date { font-size: 13px; color: var(--gray-400); white-space: nowrap; }

        .status-tag {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 16px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
            white-space: nowrap;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .status-tag:hover { transform: scale(1.08); }
        .status-待支付 { background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); color: #92400e; box-shadow: 0 2px 10px rgba(245, 158, 11, 0.25); }
        .status-已支付 { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); color: #1e40af; box-shadow: 0 2px 10px rgba(59, 130, 246, 0.25); }
        .status-已发货 { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); color: #065f46; box-shadow: 0 2px 10px rgba(16, 185, 129, 0.25); }
        .status-已完成 { background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%); color: #5b21b6; box-shadow: 0 2px 10px rgba(139, 92, 246, 0.25); }
        .status-已取消 { background: linear-gradient(135deg, var(--gray-200) 0%, var(--gray-300) 100%); color: var(--gray-500); box-shadow: 0 2px 10px rgba(156, 163, 175, 0.2); }
        .status-icon { font-size: 14px; }
        .status-待支付 .status-icon { animation: iconPulse 2s ease-in-out infinite; }
        .status-已发货 .status-icon { animation: iconBounce 1.5s ease-in-out infinite; }
        .status-已支付 .status-icon { animation: iconShine 2.5s ease-in-out infinite; }
        @keyframes iconPulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.25); } }
        @keyframes iconBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }
        @keyframes iconShine { 0%, 100% { opacity: 1; } 50% { opacity: 0.6; } }

        .order-card-body { padding: 20px 24px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; }
        .order-recipient { display: flex; flex-direction: column; gap: 4px; }
        .order-recipient .recipient-name { font-size: 15px; font-weight: 700; color: var(--gray-800); }
        .order-recipient .recipient-addr { font-size: 13px; color: var(--gray-400); }
        .order-amount { text-align: right; }
        .order-amount .amount-label { font-size: 12px; color: var(--gray-400); margin-bottom: 4px; }
        .order-amount .amount-value { font-size: 24px; font-weight: 800; color: var(--danger); }

        .order-card-actions {
            display: flex;
            gap: 8px;
            padding: 16px 24px;
            border-top: 1px solid rgba(229, 231, 235, 0.5);
            justify-content: flex-end;
            flex-wrap: wrap;
        }
        .btn-sm {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 18px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 700;
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }
        .btn-sm::after {
            content: '';
            position: absolute;
            inset: 0;
            background: radial-gradient(circle, rgba(255,255,255,0.3), transparent 70%);
            transform: scale(0);
            transition: transform 0.5s;
            border-radius: inherit;
        }
        .btn-sm:active::after { transform: scale(2.5); transition: transform 0s; }
        .btn-sm-detail {
            background: rgba(243, 244, 246, 0.7);
            color: var(--gray-600);
            backdrop-filter: blur(4px);
        }
        .btn-sm-detail:hover { background: var(--gray-200); color: var(--gray-800); text-decoration: none; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
        .btn-sm-primary { background: var(--primary); color: #fff; }
        .btn-sm-primary:hover { background: var(--primary-dark); color: #fff; text-decoration: none; transform: translateY(-2px); box-shadow: 0 4px 15px rgba(79,70,229,0.4); }
        .btn-sm-danger { background: transparent; color: var(--danger); border: 1.5px solid var(--danger); }
        .btn-sm-danger:hover { background: var(--danger); color: #fff; text-decoration: none; transform: translateY(-2px); box-shadow: 0 4px 15px rgba(239,68,68,0.3); }
        .btn-sm-success { background: var(--success); color: #fff; }
        .btn-sm-success:hover { background: #059669; color: #fff; text-decoration: none; transform: translateY(-2px); box-shadow: 0 4px 15px rgba(16,185,129,0.4); }
        .btn-sm-warning { background: var(--accent); color: #fff; }
        .btn-sm-warning:hover { background: #d97706; color: #fff; text-decoration: none; transform: translateY(-2px); box-shadow: 0 4px 15px rgba(245,158,11,0.4); }

        .empty-state {
            text-align: center;
            padding: 80px 20px;
            background: rgba(255,255,255,0.55);
            backdrop-filter: blur(20px) saturate(180%);
            -webkit-backdrop-filter: blur(20px) saturate(180%);
            border: 1px solid rgba(255,255,255,0.5);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-glass);
            position: relative;
            overflow: hidden;
        }
        .empty-state::before {
            content: '';
            position: absolute;
            top: -50%; left: -50%;
            width: 200%; height: 200%;
            background: radial-gradient(circle, rgba(79,70,229,0.06) 0%, transparent 60%);
            animation: emptyPulse 4s ease-in-out infinite;
        }
        .empty-state::after {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--primary), var(--accent), var(--success), var(--primary));
            background-size: 300% 100%;
            animation: gradientShift 3s linear infinite;
            border-radius: var(--radius-xl) var(--radius-xl) 0 0;
        }
        @keyframes gradientShift { 0% { background-position: 0% 50%; } 100% { background-position: 300% 50%; } }
        @keyframes emptyPulse { 0%, 100% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.1); opacity: 1; } }
        .empty-state .empty-icon {
            font-size: 80px;
            margin-bottom: 20px;
            position: relative;
            z-index: 1;
            animation: emptyFloat 3s ease-in-out infinite;
            filter: drop-shadow(0 8px 16px rgba(0,0,0,0.08));
        }
        @keyframes emptyFloat { 0%, 100% { transform: translateY(0) rotate(0deg); } 25% { transform: translateY(-10px) rotate(3deg); } 75% { transform: translateY(-4px) rotate(-3deg); } }
        .empty-state h2 { font-size: 24px; color: var(--gray-700); margin-bottom: 8px; position: relative; z-index: 1; font-weight: 800; }
        .empty-state p { color: var(--gray-400); margin-bottom: 24px; font-size: 15px; position: relative; z-index: 1; }
        .btn-gradient {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 14px 32px;
            border-radius: 50px;
            font-size: 16px;
            font-weight: 700;
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 4px 15px rgba(79,70,229,0.3);
            position: relative;
            z-index: 1;
            overflow: hidden;
        }
        .btn-gradient::before {
            content: '';
            position: absolute;
            top: 0; left: -100%;
            width: 100%; height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
            transition: left 0.5s;
        }
        .btn-gradient:hover { transform: translateY(-3px); box-shadow: 0 10px 30px rgba(79,70,229,0.4); color: #fff; text-decoration: none; }
        .btn-gradient:hover::before { left: 100%; }

        .no-result {
            text-align: center;
            padding: 60px 20px;
            color: var(--gray-400);
            font-size: 15px;
            background: rgba(255,255,255,0.4);
            backdrop-filter: blur(12px);
            border-radius: var(--radius-xl);
            border: 1px solid rgba(255,255,255,0.3);
            animation: cardFadeIn 0.4s ease-out;
        }
        .no-result-icon { font-size: 48px; margin-bottom: 12px; display: block; }

        .filter-no-result { display: none; text-align: center; padding: 60px 20px; color: var(--gray-400); }
        .filter-no-result-icon { font-size: 48px; margin-bottom: 12px; display: block; }

        .btn-back-home { background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important; position: relative; overflow: hidden; }
        .btn-back-home::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent); transition: left 0.5s; }
        .btn-back-home:hover { transform: translateY(-2px) !important; box-shadow: 0 8px 25px rgba(79,70,229,0.4) !important; color: #fff !important; text-decoration: none !important; }
        .btn-back-home:hover::before { left: 100%; }
        .btn-back-home:active { transform: translateY(0) !important; }
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
            <li><a href="${pageContext.request.contextPath}/cart"><span class="glyphicon glyphicon-shopping-cart"></span> 购物车</a></li>
            <c:if test="${not empty sessionScope.user}">
                <li><a href="${pageContext.request.contextPath}/review/manage">我的评价</a></li>
                <li><a href="${pageContext.request.contextPath}/message">消息中心</a></li>
                <li><a href="${pageContext.request.contextPath}/order/history">我的订单</a></li>
                <li><a href="${pageContext.request.contextPath}/logout">退出</a></li>
            </c:if>
            <c:if test="${empty sessionScope.user}">
                <li><a href="${pageContext.request.contextPath}/login">登录</a></li>
            </c:if>
        </ul>
    </div>
</nav>
<div style="height: 80px;"></div>

<div class="container">
    <div style="margin-bottom:20px;">
        <a href="${pageContext.request.contextPath}/" class="btn-back-home" style="display:inline-flex;align-items:center;gap:8px;padding:12px 28px;background:linear-gradient(135deg,var(--primary),var(--primary-dark));color:#fff;border:none;border-radius:50px;font-size:14px;font-weight:600;cursor:pointer;text-decoration:none;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);box-shadow:0 4px 15px rgba(79,70,229,0.3);position:relative;overflow:hidden;">
            <span style="position:relative;z-index:1;display:flex;align-items:center;gap:8px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/></svg>
                返回首页
            </span>
        </a>
    </div>
    <c:if test="${not empty success}">
        <div style="background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); color: #065f46; padding: 14px 20px; border-radius: 12px; margin-bottom: 16px; font-weight: 600; display: flex; align-items: center; gap: 8px; box-shadow: 0 4px 15px rgba(16,185,129,0.2);">
            <span style="font-size: 20px;">✅</span> ${success}
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div style="background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); color: #991b1b; padding: 14px 20px; border-radius: 12px; margin-bottom: 16px; font-weight: 600; display: flex; align-items: center; gap: 8px; box-shadow: 0 4px 15px rgba(239,68,68,0.2);">
            <span style="font-size: 20px;">❌</span> ${error}
        </div>
    </c:if>
    <div class="page-hero">
        <div class="page-hero-content">
            <div>
                <h1 class="page-hero-title"><span class="hero-icon">📋</span> 我的订单</h1>
                <p class="page-hero-subtitle">在这里查看和管理您的所有订单</p>
            </div>
            <c:if test="${not empty orderList}">
                <div class="page-hero-stats">
                    <div class="hero-stat">
                        <div class="hero-stat-value">${cntAll}</div>
                        <div class="hero-stat-label">总订单</div>
                    </div>
                    <div class="hero-stat">
                        <div class="hero-stat-value">${cntPending}</div>
                        <div class="hero-stat-label">待支付</div>
                    </div>
                </div>
            </c:if>
        </div>
    </div>

    <c:if test="${empty orderList}">
        <div class="empty-state">
            <div class="empty-icon">📭</div>
            <h2>还没有订单</h2>
            <p>快去挑选心仪的图书下第一单吧！</p>
            <a href="${pageContext.request.contextPath}/" class="btn-gradient">📚 去逛逛</a>
        </div>
    </c:if>

    <c:if test="${not empty orderList}">
        <c:set var="cntAll" value="0"/>
        <c:set var="cntPending" value="0"/>
        <c:set var="cntPaid" value="0"/>
        <c:set var="cntShipped" value="0"/>
        <c:set var="cntCompleted" value="0"/>
        <c:set var="cntCancelled" value="0"/>
        <c:forEach items="${orderList}" var="o">
            <c:set var="cntAll" value="${cntAll + 1}"/>
            <c:choose>
                <c:when test="${o.status == '待支付'}"><c:set var="cntPending" value="${cntPending + 1}"/></c:when>
                <c:when test="${o.status == '已支付'}"><c:set var="cntPaid" value="${cntPaid + 1}"/></c:when>
                <c:when test="${o.status == '已发货'}"><c:set var="cntShipped" value="${cntShipped + 1}"/></c:when>
                <c:when test="${o.status == '已完成'}"><c:set var="cntCompleted" value="${cntCompleted + 1}"/></c:when>
                <c:when test="${o.status == '已取消'}"><c:set var="cntCancelled" value="${cntCancelled + 1}"/></c:when>
            </c:choose>
        </c:forEach>

        <div class="tabs-container">
            <ul class="tabs-list" id="orderTabs">
                <li><a class="tab-btn active" data-filter="all"><span class="tab-icon">📦</span> 全部<span class="tab-count">${cntAll}</span></a></li>
                <li><a class="tab-btn" data-filter="待支付"><span class="tab-icon">⏳</span> 待支付<span class="tab-count">${cntPending}</span></a></li>
                <li><a class="tab-btn" data-filter="已支付"><span class="tab-icon">💰</span> 已支付<span class="tab-count">${cntPaid}</span></a></li>
                <li><a class="tab-btn" data-filter="已发货"><span class="tab-icon">🚚</span> 已发货<span class="tab-count">${cntShipped}</span></a></li>
                <li><a class="tab-btn" data-filter="已完成"><span class="tab-icon">✅</span> 已完成<span class="tab-count">${cntCompleted}</span></a></li>
                <li><a class="tab-btn" data-filter="已取消"><span class="tab-icon">❌</span> 已取消<span class="tab-count">${cntCancelled}</span></a></li>
            </ul>
        </div>

        <div id="orderListContainer">
            <c:forEach items="${orderList}" var="o">
                <div class="order-card" data-status="${o.status}">
                    <div class="order-card-header">
                        <div>
                            <span class="order-no">订单号:<strong>${o.orderid}</strong></span>
                        </div>
                        <div style="display: flex; align-items: center; gap: 12px;">
                            <span class="order-date"><fmt:formatDate value="${o.orderdate}" pattern="yyyy-MM-dd HH:mm"/></span>
                            <span class="status-tag status-${o.status}">
                                <c:choose>
                                    <c:when test="${o.status == '待支付'}">
                                        <span class="status-icon">⏳</span> 待支付
                                    </c:when>
                                    <c:when test="${o.status == '已支付'}">
                                        <span class="status-icon">💰</span> 已支付
                                    </c:when>
                                    <c:when test="${o.status == '已发货'}">
                                        <span class="status-icon">📦</span> 已发货
                                    </c:when>
                                    <c:when test="${o.status == '已完成'}">
                                        <span class="status-icon">✅</span> 已完成
                                    </c:when>
                                    <c:when test="${o.status == '已取消'}">
                                        <span class="status-icon">❌</span> 已取消
                                    </c:when>
                                    <c:otherwise>${o.status}</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                    <div class="order-card-body">
                        <div class="order-recipient">
                            <span class="recipient-name">👤 ${o.billtofirstname} ${o.billtolastname}</span>
                            <span class="recipient-addr">📍 ${o.shipaddr1} ${o.shipcity} ${o.shipstate} ${o.shipzip}</span>
                        </div>
                        <div class="order-amount">
                            <div class="amount-label">订单金额</div>
                            <c:if test="${not empty o.originalprice && not empty o.discountamount}">
                                <div style="font-size:12px;color:var(--gray-400);text-decoration:line-through;">原价 ¥<fmt:formatNumber value="${o.originalprice}" pattern="#0.00"/></div>
                                <div style="font-size:11px;color:#10b981;">🎫 优惠 -¥<fmt:formatNumber value="${o.discountamount}" pattern="#0.00"/></div>
                            </c:if>
                            <div class="amount-value">¥<fmt:formatNumber value="${o.totalprice}" pattern="#,##0.00"/></div>
                        </div>
                    </div>
                    <div class="order-card-actions">
                        <a href="${pageContext.request.contextPath}/order/detail?orderId=${o.orderid}" class="btn-sm btn-sm-detail">📄 查看详情</a>
                        <c:if test="${o.status == '待支付'}">
                            <a href="${pageContext.request.contextPath}/payment?orderId=${o.orderid}" class="btn-sm btn-sm-primary">💳 去支付</a>
                            <form action="${pageContext.request.contextPath}/order/cancel" method="post" style="display: inline;">
                                <input type="hidden" name="orderId" value="${o.orderid}"/>
                                <button type="submit" class="btn-sm btn-sm-danger" onclick="return confirm('确定取消该订单吗？')">取消</button>
                            </form>
                        </c:if>
                        <c:if test="${o.status == '已发货'}">
                            <button class="btn-sm btn-sm-success" onclick="confirmReceive('${o.orderid}')">📦 确认收货</button>
                        </c:if>
                        <c:if test="${o.status == '已完成'}">
                            <c:choose>
                                <c:when test="${reviewedOrderIds.contains(o.orderid)}">
                                    <span style="color: var(--success); font-size: 13px; font-weight: 700;">✅ 已评价</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/review?orderId=${o.orderid}" class="btn-sm btn-sm-warning">⭐ 去评价</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div class="filter-no-result-card" id="filterNoResult">
            <span class="fnr-icon">🔍</span>
            <h3>没有找到对应订单</h3>
            <p>该状态下暂无订单，换个分类看看吧</p>
        </div>
    </c:if>
</div>

<script>
    $(function() {
        $('#orderTabs .tab-btn').on('click', function(e) {
            e.preventDefault();
            $('#orderTabs .tab-btn').removeClass('active');
            $(this).addClass('active');
            var filter = $(this).data('filter');
            var visibleCount = 0;
            if (filter === 'all') {
                $('#orderListContainer .order-card').fadeIn(200);
                visibleCount = $('#orderListContainer .order-card').length;
            } else {
                $('#orderListContainer .order-card').each(function() {
                    if ($(this).data('status') === filter) {
                        $(this).fadeIn(200);
                        visibleCount++;
                    } else {
                        $(this).fadeOut(200);
                    }
                });
            }
            setTimeout(function() {
                if (visibleCount === 0) {
                    $('#filterNoResult').fadeIn(200);
                } else {
                    $('#filterNoResult').fadeOut(200);
                }
            }, 220);
        });
    });

    function confirmReceive(orderId) {
        if (!confirm('确认已收到商品吗？')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/order/confirmReceipt',
            type: 'POST',
            data: { orderId: orderId },
            dataType: 'json',
            success: function(data) {
                if (data.success) {
                    alert('已确认收货！');
                    location.reload();
                } else {
                    alert(data.message || '操作失败，请重试');
                }
            },
            error: function() {
                alert('操作失败，请重试');
            }
        });
    }
</script>

</body>
</html>
