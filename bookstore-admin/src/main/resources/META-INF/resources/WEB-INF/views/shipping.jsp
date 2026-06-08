<%-- ============================================================
     shipping.jsp — 配送说明页面
     功能：向用户说明 BookVerse 的配送政策，包括配送范围、配送方式、
           配送时间、运费说明和配送查询方法。
     说明：纯展示型页面，采用卡片式布局展示各项配送信息。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>配送说明 - BookVerse</title>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        :root {
            --primary: #4f46e5;
            --primary-dark: #3730a3;
            --gray-50: #f9fafb;
            --gray-100: #f3f4f6;
            --gray-200: #e5e7eb;
            --gray-600: #4b5563;
            --gray-700: #374151;
            --gray-800: #1f2937;
        }
        body {
            font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
            background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
            min-height: 100vh;
        }
        .navbar-custom {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            border: none;
            box-shadow: 0 4px 20px rgba(79,70,229,0.3);
        }
        .navbar-custom .navbar-brand {
            color: #fff;
            font-weight: 700;
            font-size: 20px;
        }
        .navbar-custom .navbar-brand:hover {
            color: #fff;
        }
        .navbar-custom .navbar-nav > li > a {
            color: rgba(255,255,255,0.85);
            transition: all 0.3s;
        }
        .navbar-custom .navbar-nav > li > a:hover {
            color: #fff;
            background: rgba(255,255,255,0.15);
        }
        .page-hero {
            background: linear-gradient(135deg, var(--primary) 0%, #7c3aed 50%, #ec4899 100%);
            padding: 60px 0;
            color: #fff;
            text-align: center;
            position: relative;
            overflow: hidden;
        }
        .page-hero::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -20%;
            width: 400px;
            height: 400px;
            background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
            border-radius: 50%;
        }
        .page-hero h1 {
            font-size: 36px;
            font-weight: 700;
            margin-bottom: 16px;
            position: relative;
            z-index: 1;
        }
        .page-hero p {
            font-size: 18px;
            opacity: 0.9;
            position: relative;
            z-index: 1;
        }
        .content-section {
            padding: 60px 0;
        }
        .shipping-card {
            background: #fff;
            border-radius: 16px;
            padding: 32px;
            margin-bottom: 24px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.06);
            transition: all 0.3s;
            border: 1px solid rgba(0,0,0,0.05);
        }
        .shipping-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 40px rgba(0,0,0,0.12);
        }
        .shipping-card .icon-wrapper {
            width: 64px;
            height: 64px;
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            margin-bottom: 20px;
        }
        .shipping-card h3 {
            font-size: 20px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 12px;
        }
        .shipping-card p {
            color: var(--gray-600);
            line-height: 1.8;
            margin-bottom: 16px;
        }
        .shipping-card .info-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        .shipping-card .info-list li {
            padding: 12px 0;
            border-bottom: 1px solid var(--gray-100);
            display: flex;
            align-items: flex-start;
            gap: 12px;
        }
        .shipping-card .info-list li:last-child {
            border-bottom: none;
        }
        .shipping-card .info-list li i {
            color: var(--primary);
            font-size: 16px;
            margin-top: 2px;
        }
        .icon-blue { background: linear-gradient(135deg, #dbeafe, #93c5fd); color: #2563eb; }
        .icon-green { background: linear-gradient(135deg, #d1fae5, #6ee7b7); color: #059669; }
        .icon-purple { background: linear-gradient(135deg, #ede9fe, #c4b5fd); color: #7c3aed; }
        .icon-orange { background: linear-gradient(135deg, #fef3c7, #fcd34d); color: #d97706; }
        .icon-pink { background: linear-gradient(135deg, #fce7f3, #f9a8d4); color: #db2777; }
        .icon-red { background: linear-gradient(135deg, #fee2e2, #fca5a5); color: #dc2626; }
        .highlight-box {
            background: linear-gradient(135deg, #fef3c7, #fcd34d);
            border-radius: 12px;
            padding: 20px;
            margin-top: 20px;
            border-left: 4px solid #d97706;
        }
        .highlight-box p {
            color: #92400e;
            margin: 0;
            font-weight: 500;
        }
        .highlight-box i {
            color: #d97706;
            margin-right: 8px;
        }
        .btn-back-home {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 28px;
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            border: none;
            border-radius: 50px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s cubic-bezier(0.4,0,0.2,1);
            box-shadow: 0 4px 15px rgba(79,70,229,0.3);
            position: relative;
            overflow: hidden;
        }
        .btn-back-home::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
            transition: left 0.5s;
        }
        .btn-back-home:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(79,70,229,0.4);
            color: #fff;
            text-decoration: none;
        }
        .btn-back-home:hover::before {
            left: 100%;
        }
        .footer-custom {
            background: var(--gray-800);
            color: var(--gray-300);
            padding: 40px 0 20px;
        }
        .footer-custom h5 {
            color: #fff;
            font-weight: 700;
            margin-bottom: 20px;
        }
        .footer-custom a {
            color: var(--gray-400);
            text-decoration: none;
            transition: color 0.2s;
        }
        .footer-custom a:hover {
            color: #fff;
        }
        .footer-bottom {
            border-top: 1px solid var(--gray-700);
            margin-top: 30px;
            padding-top: 20px;
            text-align: center;
            color: var(--gray-500);
            font-size: 13px;
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
                <li><a href="${pageContext.request.contextPath}/cart">购物车</a></li>
                <li><a href="${pageContext.request.contextPath}/order/history">我的订单</a></li>
            </c:if>
            <c:if test="${empty sessionScope.user}">
                <li><a href="${pageContext.request.contextPath}/login">登录</a></li>
                <li><a href="${pageContext.request.contextPath}/register">注册</a></li>
            </c:if>
        </ul>
    </div>
</nav>

<div style="height: 70px;"></div>

<div class="page-hero">
    <div class="container">
        <h1>🚚 配送说明</h1>
        <p>了解我们的配送服务，让您的图书更快到达</p>
    </div>
</div>

<div class="content-section">
    <div class="container">
        <div style="margin-bottom: 30px;">
            <a href="${pageContext.request.contextPath}/" class="btn-back-home">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/></svg>
                返回首页
            </a>
        </div>

        <div class="row">
            <div class="col-md-6">
                <div class="shipping-card">
                    <div class="icon-wrapper icon-blue">
                        <i class="fas fa-globe-asia"></i>
                    </div>
                    <h3>配送范围</h3>
                    <p>我们提供全国范围内的配送服务，覆盖中国大陆所有省市自治区。</p>
                    <ul class="info-list">
                        <li>
                            <i class="fas fa-check-circle"></i>
                            <span>支持全国31个省、市、自治区配送</span>
                        </li>
                        <li>
                            <i class="fas fa-check-circle"></i>
                            <span>包含港澳台地区（需额外运费）</span>
                        </li>
                        <li>
                            <i class="fas fa-check-circle"></i>
                            <span>偏远地区可能需要额外配送时间</span>
                        </li>
                    </ul>
                    <div class="highlight-box">
                        <p><i class="fas fa-info-circle"></i> 部分特殊地区（如海岛、边境等）可能无法配送，下单前请咨询客服确认。</p>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="shipping-card">
                    <div class="icon-wrapper icon-green">
                        <i class="fas fa-truck"></i>
                    </div>
                    <h3>配送方式</h3>
                    <p>我们提供多种配送方式，满足您不同的需求。</p>
                    <ul class="info-list">
                        <li>
                            <i class="fas fa-shipping-fast"></i>
                            <div>
                                <strong>顺丰快递</strong>
                                <p class="text-muted">速度快，服务好，适合急需的订单</p>
                            </div>
                        </li>
                        <li>
                            <i class="fas fa-truck"></i>
                            <div>
                                <strong>普通快递</strong>
                                <p class="text-muted">经济实惠，适合不急的订单</p>
                            </div>
                        </li>
                    </ul>
                    <div class="highlight-box">
                        <p><i class="fas fa-star"></i> 会员用户可享受顺丰快递优先配送服务。</p>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="shipping-card">
                    <div class="icon-wrapper icon-purple">
                        <i class="fas fa-clock"></i>
                    </div>
                    <h3>配送时间</h3>
                    <p>不同配送方式的预计到达时间如下：</p>
                    <ul class="info-list">
                        <li>
                            <i class="fas fa-bolt"></i>
                            <div>
                                <strong>顺丰快递</strong>
                                <p class="text-muted">1-2个工作日到达（主要城市）</p>
                            </div>
                        </li>
                        <li>
                            <i class="fas fa-truck-loading"></i>
                            <div>
                                <strong>普通快递</strong>
                                <p class="text-muted">3-5个工作日到达（大部分地区）</p>
                            </div>
                        </li>
                        <li>
                            <i class="fas fa-map-marker-alt"></i>
                            <div>
                                <strong>偏远地区</strong>
                                <p class="text-muted">可能需要5-7个工作日</p>
                            </div>
                        </li>
                    </ul>
                    <div class="highlight-box">
                        <p><i class="fas fa-exclamation-triangle"></i> 以上时间为预估时间，实际到达时间可能因天气、交通等因素有所变化。</p>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="shipping-card">
                    <div class="icon-wrapper icon-orange">
                        <i class="fas fa-money-bill-wave"></i>
                    </div>
                    <h3>运费说明</h3>
                    <p>我们提供灵活的运费政策，让您购物更划算。</p>
                    <ul class="info-list">
                        <li>
                            <i class="fas fa-gift"></i>
                            <div>
                                <strong>满99元包邮</strong>
                                <p class="text-muted">订单满99元（普通快递）免运费</p>
                            </div>
                        </li>
                        <li>
                            <i class="fas fa-crown"></i>
                            <div>
                                <strong>会员专享</strong>
                                <p class="text-muted">VIP会员全场包邮</p>
                            </div>
                        </li>
                        <li>
                            <i class="fas fa-calculator"></i>
                            <div>
                                <strong>运费计算</strong>
                                <p class="text-muted">普通快递8元，顺丰15-20元</p>
                            </div>
                        </li>
                    </ul>
                    <div class="highlight-box">
                        <p><i class="fas fa-tag"></i> 特价商品、促销活动期间可能有特殊包邮政策，详见活动页面。</p>
                    </div>
                </div>
            </div>

            <div class="col-md-12">
                <div class="shipping-card">
                    <div class="icon-wrapper icon-pink">
                        <i class="fas fa-search"></i>
                    </div>
                    <h3>配送查询方法</h3>
                    <p>您可以通过以下方式查询订单的配送状态：</p>
                    <div class="row">
                        <div class="col-md-4">
                            <ul class="info-list">
                                <li>
                                    <i class="fas fa-user"></i>
                                    <div>
                                        <strong>个人中心查询</strong>
                                        <p class="text-muted">登录后进入"我的订单"查看物流信息</p>
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <div class="col-md-4">
                            <ul class="info-list">
                                <li>
                                    <i class="fas fa-sms"></i>
                                    <div>
                                        <strong>短信通知</strong>
                                        <p class="text-muted">发货后会收到包含物流单号的短信</p>
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <div class="col-md-4">
                            <ul class="info-list">
                                <li>
                                    <i class="fas fa-headset"></i>
                                    <div>
                                        <strong>客服查询</strong>
                                        <p class="text-muted">联系客服提供订单号查询物流</p>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <div class="highlight-box">
                        <p><i class="fas fa-lightbulb"></i> 建议您在订单发货后及时查看物流信息，如有异常请及时联系客服处理。</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<footer class="footer-custom">
    <div class="container">
        <div class="row">
            <div class="col-md-4">
                <h5>📚 BookVerse</h5>
                <p>您的一站式在线图书商城，致力于为读者提供优质图书和便捷的购物体验。</p>
            </div>
            <div class="col-md-2">
                <h5>关于我们</h5>
                <ul style="list-style:none;padding:0;">
                    <li><a href="${pageContext.request.contextPath}/about">公司介绍</a></li>
                    <li><a href="${pageContext.request.contextPath}/history">发展历程</a></li>
                    <li><a href="${pageContext.request.contextPath}/careers">加入我们</a></li>
                    <li><a href="${pageContext.request.contextPath}/partners">合作伙伴</a></li>
                </ul>
            </div>
            <div class="col-md-2">
                <h5>帮助中心</h5>
                <ul style="list-style:none;padding:0;">
                    <li><a href="${pageContext.request.contextPath}/help">帮助中心</a></li>
                    <li><a href="${pageContext.request.contextPath}/return-policy">退换货政策</a></li>
                    <li><a href="${pageContext.request.contextPath}/shipping">配送说明</a></li>
                    <li><a href="${pageContext.request.contextPath}/complaint">投诉建议</a></li>
                </ul>
            </div>
            <div class="col-md-4">
                <h5>联系方式</h5>
                <p><i class="fas fa-phone"></i> 客服电话: 400-888-8888</p>
                <p><i class="fas fa-envelope"></i> 邮箱: support@bookverse.com</p>
                <p><i class="fas fa-clock"></i> 工作时间: 9:00-21:00</p>
            </div>
        </div>
        <div class="footer-bottom">
            <p>© 2026 BookVerse. All rights reserved.</p>
        </div>
    </div>
</footer>

<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
</body>
</html>