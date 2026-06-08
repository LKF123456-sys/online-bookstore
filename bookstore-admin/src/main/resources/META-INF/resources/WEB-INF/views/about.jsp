<%-- ============================================================
     about.jsp — 关于我们（公司介绍）页面
     功能：展示 BookVerse 的公司简介、企业愿景、核心价值观、
           团队介绍、企业荣誉以及关键统计数据。
     说明：这是一个纯展示型页面，不涉及后端数据交互，
           所有内容为静态文本，样式采用内联CSS。
     关键技术点：
       - 使用 CSS 自定义属性（CSS变量）统一管理颜色和阴影
       - 使用 CSS Grid 布局实现响应式网格
       - 使用 fadeIn 动画实现滚动渐显效果
       - 使用 JSTL 标签库实现导航栏的登录/未登录状态切换
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 引入 JSTL 核心标签库（c:if、c:forEach等）和格式化标签库（fmt:formatDate等） --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%-- ========== 页面基本配置 ========== --%>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司介绍 - BookVerse</title>
    <%-- 引入 Bootstrap 3.4.1 样式框架和 FontAwesome 6.4.0 图标库 --%>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.4.1/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.4.0/css/all.min.css">
    <%-- ========== 页面内联样式定义 ========== --%>
    <style>
        <%-- CSS自定义属性（变量）：统一管理整个页面的颜色、阴影、圆角等设计参数 --%>
        :root {
            --primary: #4f46e5;
            --primary-dark: #3730a3;
            --primary-light: #818cf8;
            --accent: #f59e0b;
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
            --shadow: 0 1px 3px rgba(0,0,0,0.1);
            --shadow-md: 0 4px 6px rgba(0,0,0,0.1);
            --shadow-lg: 0 10px 25px rgba(0,0,0,0.12);
            --shadow-xl: 0 20px 50px rgba(0,0,0,0.15);
            --radius: 8px;
            --radius-lg: 12px;
            --radius-xl: 16px;
        }
        * { box-sizing: border-box; }
        <%-- 页面主体样式：设置字体、背景色和文字颜色 --%>
        body {
            font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
            background: var(--gray-50);
            color: var(--gray-800);
            min-height: 100vh;
        }
        <%-- 自定义导航栏样式：渐变紫色背景，固定在页面顶部 --%>
        .navbar-custom {
            background: linear-gradient(135deg, var(--primary-dark), var(--primary));
            border: none;
            border-radius: 0;
            margin-bottom: 0;
            box-shadow: var(--shadow-md);
            position: sticky;
            top: 0;
            z-index: 1030;
        }
        .navbar-custom .navbar-brand {
            color: #fff !important;
            font-weight: 700;
            font-size: 22px;
            letter-spacing: 1px;
        }
        .navbar-custom .navbar-brand:hover { color: #fff !important; }
        .navbar-custom .navbar-brand .logo-icon {
            display: inline-block;
            width: 32px;
            height: 32px;
            background: rgba(255,255,255,0.2);
            border-radius: var(--radius);
            text-align: center;
            line-height: 32px;
            margin-right: 8px;
        }
        .navbar-custom .nav > li > a {
            color: rgba(255,255,255,0.9) !important;
            transition: all 0.3s;
        }
        .navbar-custom .nav > li > a:hover,
        .navbar-custom .nav > li.active > a {
            color: #fff !important;
            background: rgba(255,255,255,0.1) !important;
        }
        .navbar-custom .navbar-toggle .icon-bar { background: #fff; }
        <%-- 页面顶部横幅（Hero）区域：渐变紫色背景，带装饰性圆形光晕 --%>
        .page-hero {
            background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary) 40%, #7c3aed 70%, #a855f7 100%);
            padding: 80px 0 60px;
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
            width: 500px;
            height: 500px;
            background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
            border-radius: 50%;
        }
        .page-hero::after {
            content: '';
            position: absolute;
            bottom: -40%;
            left: -15%;
            width: 350px;
            height: 350px;
            background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 70%);
            border-radius: 50%;
        }
        .page-hero h1 {
            font-size: 42px;
            font-weight: 800;
            margin-bottom: 16px;
            position: relative;
            z-index: 1;
            text-shadow: 0 2px 10px rgba(0,0,0,0.2);
        }
        .page-hero p {
            font-size: 18px;
            opacity: 0.9;
            position: relative;
            z-index: 1;
        }
        .page-hero .hero-icon {
            font-size: 60px;
            margin-bottom: 20px;
            display: block;
            animation: float 3s ease-in-out infinite;
        }
        @keyframes float {
            0%,100% { transform: translateY(0); }
            50% { transform: translateY(-15px); }
        }
        .content-section { padding: 60px 0; }
        .about-card {
            background: var(--white);
            border-radius: var(--radius-xl);
            padding: 36px;
            margin-bottom: 30px;
            box-shadow: var(--shadow);
            transition: all 0.3s;
            border: 1px solid rgba(0,0,0,0.04);
            position: relative;
            overflow: hidden;
        }
        .about-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--primary), var(--primary-light));
            transform: scaleX(0);
            transition: transform 0.3s;
            transform-origin: left;
        }
        .about-card:hover {
            transform: translateY(-4px);
            box-shadow: var(--shadow-lg);
        }
        .about-card:hover::before { transform: scaleX(1); }
        .about-card .card-icon {
            width: 64px;
            height: 64px;
            border-radius: var(--radius-lg);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            margin-bottom: 20px;
        }
        .icon-bg-purple {
            background: linear-gradient(135deg, #ede9fe, #c4b5fd);
            color: var(--primary);
        }
        .icon-bg-blue {
            background: linear-gradient(135deg, #dbeafe, #93c5fd);
            color: #2563eb;
        }
        .icon-bg-amber {
            background: linear-gradient(135deg, #fef3c7, #fcd34d);
            color: #d97706;
        }
        .icon-bg-green {
            background: linear-gradient(135deg, #d1fae5, #6ee7b7);
            color: #059669;
        }
        .icon-bg-pink {
            background: linear-gradient(135deg, #fce7f3, #f9a8d4);
            color: #db2777;
        }
        .icon-bg-red {
            background: linear-gradient(135deg, #fee2e2, #fca5a5);
            color: #dc2626;
        }
        .about-card h3 {
            font-size: 22px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 16px;
        }
        .about-card p {
            color: var(--gray-600);
            line-height: 1.9;
            font-size: 15px;
            margin-bottom: 0;
        }
        .values-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 24px;
            margin-top: 20px;
        }
        .value-card {
            text-align: center;
            padding: 30px 20px;
            background: linear-gradient(135deg, #f8fafc, #eef2ff);
            border-radius: var(--radius-lg);
            transition: all 0.3s;
            border: 1px solid rgba(79,70,229,0.08);
        }
        .value-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--shadow-md);
            border-color: rgba(79,70,229,0.2);
        }
        .value-card .value-icon {
            font-size: 42px;
            margin-bottom: 15px;
            display: block;
            animation: float 3s ease-in-out infinite;
        }
        .value-card:nth-child(2) .value-icon { animation-delay: 0.5s; }
        .value-card:nth-child(3) .value-icon { animation-delay: 1s; }
        .value-card h4 {
            font-size: 18px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 10px;
        }
        .value-card p {
            font-size: 14px;
            color: var(--gray-500);
            margin: 0;
            line-height: 1.7;
        }
        .team-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 24px;
            margin-top: 20px;
        }
        .team-card {
            text-align: center;
            padding: 30px 20px;
            background: var(--white);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow);
            transition: all 0.3s;
        }
        .team-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--shadow-lg);
        }
        .team-card .team-avatar {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            margin: 0 auto 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 36px;
            background: linear-gradient(135deg, var(--primary), #7c3aed);
            color: #fff;
            box-shadow: 0 4px 15px rgba(79,70,229,0.3);
        }
        .team-card h4 {
            font-size: 16px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 6px;
        }
        .team-card .team-role {
            font-size: 13px;
            color: var(--primary);
            font-weight: 600;
            margin-bottom: 10px;
        }
        .team-card p {
            font-size: 13px;
            color: var(--gray-500);
            margin: 0;
            line-height: 1.6;
        }
        .honors-list {
            list-style: none;
            padding: 0;
            margin: 20px 0 0;
        }
        .honors-list li {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 16px 0;
            border-bottom: 1px solid var(--gray-100);
        }
        .honors-list li:last-child { border-bottom: none; }
        .honors-list li .honor-icon {
            width: 48px;
            height: 48px;
            border-radius: var(--radius);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            background: linear-gradient(135deg, #fef3c7, #fcd34d);
            color: #d97706;
            flex-shrink: 0;
        }
        .honors-list li .honor-content h4 {
            font-size: 15px;
            font-weight: 600;
            color: var(--gray-800);
            margin: 0 0 4px;
        }
        .honors-list li .honor-content p {
            font-size: 13px;
            color: var(--gray-500);
            margin: 0;
        }
        .stats-row {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin: 40px 0;
        }
        .stat-box {
            text-align: center;
            padding: 30px 20px;
            background: linear-gradient(135deg, var(--primary-dark), var(--primary));
            border-radius: var(--radius-lg);
            color: #fff;
        }
        .stat-box .stat-number {
            font-size: 36px;
            font-weight: 800;
            display: block;
            margin-bottom: 6px;
            background: linear-gradient(135deg, #fff, #e0e7ff);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        .stat-box .stat-label {
            font-size: 14px;
            opacity: 0.9;
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
        .btn-back-home:hover::before { left: 100%; }
        .btn-back-home:active { transform: translateY(0); }
        .footer-custom {
            background: linear-gradient(135deg, #0f172a, #1e293b, #111827);
            color: var(--gray-300);
            padding: 50px 0 25px;
            margin-top: 60px;
            position: relative;
            overflow: hidden;
        }
        .footer-custom::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 3px;
            background: linear-gradient(90deg, var(--primary), var(--accent), var(--primary));
        }
        .footer-custom h5 {
            color: #fff;
            font-weight: 700;
            margin-bottom: 20px;
            font-size: 16px;
        }
        .footer-custom ul { list-style: none; padding: 0; }
        .footer-custom ul li { margin-bottom: 10px; }
        .footer-custom ul li a {
            color: var(--gray-400);
            text-decoration: none;
            transition: color 0.3s;
            font-size: 13px;
        }
        .footer-custom ul li a:hover { color: var(--accent); }
        .footer-contact li { display: flex; align-items: center; gap: 10px; }
        .footer-social { display: flex; gap: 15px; margin-top: 15px; }
        .footer-social a {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: rgba(255,255,255,0.1);
            color: var(--gray-300);
            font-size: 18px;
            transition: all 0.3s;
            text-decoration: none;
        }
        .footer-social a:hover {
            background: var(--primary);
            color: #fff;
            transform: translateY(-3px);
        }
        .footer-bottom {
            border-top: 1px solid rgba(255,255,255,0.1);
            margin-top: 35px;
            padding-top: 20px;
            text-align: center;
            color: var(--gray-500);
            font-size: 13px;
        }
        <%-- 淡入动画：元素初始不可见，滚动到视口时渐显 --%>
        .fade-in {
            opacity: 0;
            transform: translateY(30px);
            transition: opacity 0.8s ease, transform 0.8s ease;
        }
        .fade-in.visible {
            opacity: 1;
            transform: translateY(0);
        }
<%-- 响应式适配：在小屏幕上调整网格列数和间距 --%>
        @media (max-width: 768px) {
            .page-hero { padding: 50px 0 40px; }
            .page-hero h1 { font-size: 28px; }
            .values-grid { grid-template-columns: 1fr; }
            .team-grid { grid-template-columns: repeat(2, 1fr); }
            .stats-row { grid-template-columns: repeat(2, 1fr); }
        }
    </style>
</head>
<%-- ========== 页面主体开始 ========== --%>
<body>

<%-- ========== 顶部导航栏 ========== --%>
<%-- 自定义导航栏：包含网站Logo、首页链接，以及根据登录状态显示的不同菜单项 --%>
<nav class="navbar navbar-custom">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#mainNav">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">
                <span class="logo-icon"><i class="fas fa-book-open"></i></span>BookVerse
            </a>
        </div>
        <div class="collapse navbar-collapse" id="mainNav">
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
    </div>
</nav>

<%-- ========== 页面横幅（Hero）区域 ========== --%>
<%-- 大标题 + 图标 + 副标题，用于视觉吸引和页面定位 --%>
<div class="page-hero">
    <div class="container">
        <span class="hero-icon"><i class="fas fa-building"></i></span>
        <h1>公司介绍</h1>
        <p>了解BookVerse的故事、愿景与使命</p>
    </div>
</div>

<%-- ========== 主要内容区域 ========== --%>
<%-- 包含"返回首页"按钮，以及公司简介、愿景、核心价值观、团队介绍、荣誉等卡片模块 --%>
<div class="content-section">
    <div class="container">

        <div style="margin-bottom: 30px;">
            <a href="${pageContext.request.contextPath}/" class="btn-back-home">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/></svg>
                返回首页
            </a>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-purple">
                        <i class="fas fa-info-circle"></i>
                    </div>
                    <h3>公司简介</h3>
                    <p>BookVerse成立于2020年，是一家专注于在线图书销售的互联网企业。自成立以来，我们始终秉持"让阅读触手可及"的理念，致力于为广大读者提供丰富、优质、便捷的在线购书体验。通过不断的技术创新和服务优化，我们已发展成为拥有超过10万种图书品类、服务百万读者的综合性在线书店。我们与国内外数百家知名出版社建立了长期合作关系，确保每一本书都是正版授权，让读者买得放心、读得安心。</p>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-blue">
                        <i class="fas fa-eye"></i>
                    </div>
                    <h3>企业愿景</h3>
                    <p>我们的愿景是成为最受欢迎的在线书店，打造中国领先的数字阅读生态平台。我们希望通过科技的力量，打破传统购书的局限，让每一位读者都能轻松找到心仪的好书。未来五年，我们计划覆盖全国所有县级以上城市，建立完善的物流配送体系，实现"当日达"和"次日达"服务，让好书更快地到达读者手中。同时，我们将持续拓展数字阅读领域，构建线上线下融合的阅读社区，推动全民阅读事业发展。</p>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-amber">
                        <i class="fas fa-heart"></i>
                    </div>
                    <h3>核心价值观</h3>
                    <div class="values-grid">
                        <div class="value-card">
                            <span class="value-icon"><i class="fas fa-users"></i></span>
                            <h4>客户至上</h4>
                            <p>始终将客户需求放在首位，倾听用户声音，持续改进服务体验，努力超越每一位读者的期望。</p>
                        </div>
                        <div class="value-card">
                            <span class="value-icon"><i class="fas fa-shield-alt"></i></span>
                            <h4>品质保证</h4>
                            <p>严选每一本图书，确保正版品质；精心打造每一个服务环节，为读者提供最可靠的购书保障。</p>
                        </div>
                        <div class="value-card">
                            <span class="value-icon"><i class="fas fa-lightbulb"></i></span>
                            <h4>创新驱动</h4>
                            <p>拥抱变化，勇于创新。运用前沿技术提升用户体验，引领在线图书销售行业的发展方向。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-green">
                        <i class="fas fa-user-friends"></i>
                    </div>
                    <h3>团队介绍</h3>
                    <p style="margin-bottom: 24px;">BookVerse拥有一支充满激情与创造力的团队，团队成员来自互联网、出版、物流等多个领域，平均从业经验超过8年。我们相信，优秀的人才是企业发展的核心动力。</p>
                    <div class="team-grid">
                        <div class="team-card">
                            <div class="team-avatar"><i class="fas fa-user-tie"></i></div>
                            <h4>张明远</h4>
                            <div class="team-role">创始人 & CEO</div>
                            <p>15年互联网行业经验，曾任某知名电商平台高管，对图书零售行业有深刻理解。</p>
                        </div>
                        <div class="team-card">
                            <div class="team-avatar"><i class="fas fa-laptop-code"></i></div>
                            <h4>李思琪</h4>
                            <div class="team-role">技术总监 CTO</div>
                            <p>资深全栈工程师，精通分布式系统架构，致力于用技术驱动业务创新。</p>
                        </div>
                        <div class="team-card">
                            <div class="team-avatar"><i class="fas fa-palette"></i></div>
                            <h4>王雨萱</h4>
                            <div class="team-role">运营总监 COO</div>
                            <p>多年电商运营经验，擅长用户增长和品牌建设，带领团队持续提升用户满意度。</p>
                        </div>
                        <div class="team-card">
                            <div class="team-avatar"><i class="fas fa-book-reader"></i></div>
                            <h4>陈学文</h4>
                            <div class="team-role">内容总监</div>
                            <p>资深出版人，拥有丰富的图书选品经验，为读者精选每一本值得阅读的好书。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-red">
                        <i class="fas fa-trophy"></i>
                    </div>
                    <h3>企业荣誉</h3>
                    <ul class="honors-list">
                        <li>
                            <div class="honor-icon"><i class="fas fa-award"></i></div>
                            <div class="honor-content">
                                <h4>2024年度最佳在线书店</h4>
                                <p>由全国电子商务协会评选，表彰我们在图书电商领域的卓越表现。</p>
                            </div>
                        </li>
                        <li>
                            <div class="honor-icon"><i class="fas fa-star"></i></div>
                            <div class="honor-content">
                                <h4>消费者最信赖品牌</h4>
                                <p>连续三年获得消费者满意度调查第一名，深受广大读者好评。</p>
                            </div>
                        </li>
                        <li>
                            <div class="honor-icon"><i class="fas fa-shield-alt"></i></div>
                            <div class="honor-content">
                                <h4>正版保护示范企业</h4>
                                <p>国家版权局授予的正版保护示范企业，始终坚持正版经营。</p>
                            </div>
                        </li>
                        <li>
                            <div class="honor-icon"><i class="fas fa-medal"></i></div>
                            <div class="honor-content">
                                <h4>互联网创新50强</h4>
                                <p>入选年度互联网创新企业50强，技术创新能力获得行业认可。</p>
                            </div>
                        </li>
                        <li>
                            <div class="honor-icon"><i class="fas fa-hand-holding-heart"></i></div>
                            <div class="honor-content">
                                <h4>公益阅读推广单位</h4>
                                <p>积极参与全民阅读推广活动，向偏远地区学校捐赠图书超过10万册。</p>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <div class="stats-row fade-in">
            <div class="stat-box">
                <span class="stat-number">100,000+</span>
                <span class="stat-label">图书品类</span>
            </div>
            <div class="stat-box">
                <span class="stat-number">500,000+</span>
                <span class="stat-label">满意读者</span>
            </div>
            <div class="stat-box">
                <span class="stat-number">99.6%</span>
                <span class="stat-label">好评率</span>
            </div>
            <div class="stat-box">
                <span class="stat-number">24h</span>
                <span class="stat-label">极速发货</span>
            </div>
        </div>

    </div>
</div>

<%-- ========== 页脚区域 ========== --%>
<%-- 包含网站简介、导航链接、联系方式和社交媒体图标 --%>
<footer class="footer-custom">
    <div class="container">
        <div class="row" style="display:flex; flex-wrap:wrap;">
            <div class="col-md-3 col-sm-6" style="flex:1; min-width:200px;">
                <h5><i class="fas fa-book-open"></i> BookVerse</h5>
                <p style="color:var(--gray-400);font-size:13px;line-height:1.8;">您的一站式在线图书商城，致力于为读者提供优质图书和便捷的购物体验。让阅读触手可及，让知识无界传播。</p>
            </div>
            <div class="col-md-2 col-sm-6" style="flex:1; min-width:150px;">
                <h5>关于我们</h5>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/about">公司介绍</a></li>
                    <li><a href="#">发展历程</a></li>
                    <li><a href="#">加入我们</a></li>
                    <li><a href="#">合作伙伴</a></li>
                </ul>
            </div>
            <div class="col-md-2 col-sm-6" style="flex:1; min-width:150px;">
                <h5>快速链接</h5>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/">首页</a></li>
                    <li><a href="${pageContext.request.contextPath}/cart">购物车</a></li>
                    <li><a href="${pageContext.request.contextPath}/order/history">我的订单</a></li>
                    <li><a href="${pageContext.request.contextPath}/login">登录/注册</a></li>
                </ul>
            </div>
            <div class="col-md-2 col-sm-6" style="flex:1; min-width:150px;">
                <h5>客户服务</h5>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/help">帮助中心</a></li>
                    <li><a href="#">退换货政策</a></li>
                    <li><a href="#">配送说明</a></li>
                    <li><a href="#">投诉建议</a></li>
                </ul>
            </div>
            <div class="col-md-3 col-sm-6" style="flex:1; min-width:200px;">
                <h5>联系我们</h5>
                <ul class="footer-contact">
                    <li><i class="fas fa-envelope"></i> support@bookverse.com</li>
                    <li><i class="fas fa-phone"></i> 400-888-8888</li>
                    <li><i class="fas fa-clock"></i> 工作时间: 9:00-21:00</li>
                    <li><i class="fas fa-map-marker-alt"></i> 北京市海淀区中关村大街1号</li>
                </ul>
                <h5 style="margin-top:20px;">关注我们</h5>
                <div class="footer-social">
                    <a href="#" title="微信"><i class="fab fa-weixin"></i></a>
                    <a href="#" title="微博"><i class="fab fa-weibo"></i></a>
                    <a href="#" title="抖音"><i class="fab fa-tiktok"></i></a>
                    <a href="#" title="小红书"><i class="fas fa-book"></i></a>
                </div>
            </div>
        </div>
        <div class="footer-bottom">
            <p>&copy; 2026 BookVerse. All rights reserved. | 京ICP备XXXXXXXX号-1</p>
        </div>
    </div>
</footer>

<%-- ========== JavaScript 脚本区域 ========== --%>
<%-- 引入 jQuery 和 Bootstrap JS 库 --%>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@3.4.1/dist/js/bootstrap.min.js"></script>
<%-- 滚动渐显动画：当页面元素滚动到视口85%位置时，添加visible类触发淡入效果 --%>
<script>
    function handleScrollAnimations() {
        var fadeElements = document.querySelectorAll('.fade-in');
        function checkVisibility() {
            fadeElements.forEach(function(el) {
                var rect = el.getBoundingClientRect();
                if (rect.top < window.innerHeight * 0.85) {
                    el.classList.add('visible');
                }
            });
        }
        window.addEventListener('scroll', checkVisibility);
        checkVisibility();
    }
    handleScrollAnimations();
</script>
</body>
</html>