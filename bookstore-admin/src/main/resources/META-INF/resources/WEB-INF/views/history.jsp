<%-- ============================================================
     history.jsp — 浏览历史页面
     功能：展示用户曾经浏览过的商品记录，方便用户快速找回感兴趣的商品。
     说明：需要用户登录才能查看，数据来自用户的浏览历史记录。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>发展历程 - BookVerse</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.4.1/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.4.0/css/all.min.css">
    <style>
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
        body {
            font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
            background: var(--gray-50);
            color: var(--gray-800);
            min-height: 100vh;
        }
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
        .timeline-section {
            position: relative;
            padding: 20px 0;
        }
        .timeline-section::before {
            content: '';
            position: absolute;
            left: 50%;
            top: 0;
            bottom: 0;
            width: 4px;
            background: linear-gradient(to bottom, var(--primary), var(--primary-light), #a855f7);
            transform: translateX(-50%);
            border-radius: 2px;
        }
        .timeline-item {
            position: relative;
            margin-bottom: 50px;
            display: flex;
            align-items: flex-start;
        }
        .timeline-item:last-child { margin-bottom: 0; }
        .timeline-item:nth-child(odd) { flex-direction: row; }
        .timeline-item:nth-child(even) { flex-direction: row-reverse; }
        .timeline-content {
            width: 45%;
            background: var(--white);
            border-radius: var(--radius-xl);
            padding: 30px;
            box-shadow: var(--shadow);
            transition: all 0.4s cubic-bezier(0.4,0,0.2,1);
            border: 1px solid rgba(0,0,0,0.04);
            position: relative;
        }
        .timeline-content:hover {
            transform: translateY(-6px);
            box-shadow: var(--shadow-xl);
        }
        .timeline-item:nth-child(odd) .timeline-content { margin-right: auto; }
        .timeline-item:nth-child(even) .timeline-content { margin-left: auto; }
        .timeline-dot {
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            width: 24px;
            height: 24px;
            background: linear-gradient(135deg, var(--primary), #7c3aed);
            border-radius: 50%;
            border: 4px solid var(--gray-50);
            box-shadow: 0 0 0 3px var(--primary), var(--shadow-md);
            z-index: 2;
            transition: all 0.3s;
        }
        .timeline-item:hover .timeline-dot {
            transform: translateX(-50%) scale(1.3);
            box-shadow: 0 0 0 4px var(--primary), 0 0 20px rgba(79,70,229,0.4);
        }
        .timeline-year {
            display: inline-block;
            padding: 6px 18px;
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            border-radius: 50px;
            font-size: 16px;
            font-weight: 700;
            margin-bottom: 16px;
            letter-spacing: 1px;
        }
        .timeline-content h4 {
            font-size: 18px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 10px;
        }
        .timeline-content p {
            color: var(--gray-600);
            font-size: 14px;
            line-height: 1.8;
            margin-bottom: 0;
        }
        .timeline-icon {
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            top: 46px;
            width: 48px;
            height: 48px;
            background: var(--white);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            color: var(--primary);
            box-shadow: var(--shadow-md);
            z-index: 3;
            border: 2px solid rgba(79,70,229,0.1);
        }
        .timeline-milestones {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-top: 14px;
        }
        .milestone-tag {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 12px;
            background: linear-gradient(135deg, #ede9fe, #e0e7ff);
            color: var(--primary);
            border-radius: 50px;
            font-size: 12px;
            font-weight: 600;
            border: 1px solid rgba(79,70,229,0.1);
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
            transition: all 0.3s;
        }
        .stat-box:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 30px rgba(79,70,229,0.3);
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
        .future-card {
            background: linear-gradient(135deg, var(--primary-dark), var(--primary), #7c3aed);
            border-radius: var(--radius-xl);
            padding: 40px;
            color: #fff;
            text-align: center;
            position: relative;
            overflow: hidden;
        }
        .future-card::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -30%;
            width: 400px;
            height: 400px;
            background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
            border-radius: 50%;
        }
        .future-card h3 {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 16px;
            position: relative;
            z-index: 1;
        }
        .future-card p {
            font-size: 15px;
            line-height: 1.9;
            opacity: 0.9;
            position: relative;
            z-index: 1;
            margin-bottom: 0;
        }
        .future-card .future-icon {
            font-size: 50px;
            margin-bottom: 20px;
            display: block;
            animation: float 3s ease-in-out infinite;
            position: relative;
            z-index: 1;
        }
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
        .fade-in {
            opacity: 0;
            transform: translateY(30px);
            transition: opacity 0.8s ease, transform 0.8s ease;
        }
        .fade-in.visible {
            opacity: 1;
            transform: translateY(0);
        }
        @media (max-width: 768px) {
            .page-hero { padding: 50px 0 40px; }
            .page-hero h1 { font-size: 28px; }
            .stats-row { grid-template-columns: repeat(2, 1fr); }
            .timeline-section::before { left: 20px; }
            .timeline-item,
            .timeline-item:nth-child(odd),
            .timeline-item:nth-child(even) {
                flex-direction: column;
                padding-left: 50px;
            }
            .timeline-content,
            .timeline-item:nth-child(odd) .timeline-content,
            .timeline-item:nth-child(even) .timeline-content {
                width: 100%;
                margin: 0;
            }
            .timeline-dot {
                left: 20px;
                top: 0;
            }
            .timeline-icon {
                left: 20px;
                top: 30px;
            }
        }
    </style>
</head>
<body>

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

<div class="page-hero">
    <div class="container">
        <span class="hero-icon"><i class="fas fa-road"></i></span>
        <h1>发展历程</h1>
        <p>见证BookVerse从创立到辉煌的每一步</p>
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
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <h3 style="text-align:center; font-size:26px; margin-bottom:10px;">
                        <i class="fas fa-flag" style="color:var(--primary); margin-right:8px;"></i>我们的故事
                    </h3>
                    <p style="text-align:center; max-width:700px; margin:0 auto;">从2020年一间小小的办公室到如今服务百万用户的在线图书平台，BookVerse始终怀揣着"让阅读触手可及"的梦想，一步一个脚印，稳步前行。</p>
                </div>
            </div>
        </div>

        <div class="timeline-section fade-in">
            <div class="timeline-item">
                <div class="timeline-content">
                    <span class="timeline-year">2020</span>
                    <h4><i class="fas fa-rocket" style="color:var(--primary); margin-right:8px;"></i>公司成立，团队组建</h4>
                    <p>BookVerse在北京正式注册成立，创始团队5人怀揣对阅读的热爱与互联网的信念，在中关村一间50平米的办公室开启了创业征程。完成商业模式验证，确立了"正版+优质服务"的核心战略方向。</p>
                    <div class="timeline-milestones">
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>公司注册</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>团队组建</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>模式验证</span>
                    </div>
                </div>
                <div class="timeline-dot"></div>
            </div>

            <div class="timeline-item">
                <div class="timeline-content">
                    <span class="timeline-year">2021</span>
                    <h4><i class="fas fa-globe" style="color:#2563eb; margin-right:8px;"></i>平台上线，首笔订单</h4>
                    <p>经过一年的精心筹备与技术开发，BookVerse在线平台正式上线运营。首月注册用户突破5000人，成功完成第一笔订单。与首批20家知名出版社达成战略合作，图书品类覆盖文学、科技、教育等主要领域。</p>
                    <div class="timeline-milestones">
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>平台上线</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>首笔订单</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>20家合作出版社</span>
                    </div>
                </div>
                <div class="timeline-dot"></div>
            </div>

            <div class="timeline-item">
                <div class="timeline-content">
                    <span class="timeline-year">2022</span>
                    <h4><i class="fas fa-chart-line" style="color:#059669; margin-right:8px;"></i>用户突破10万，获得融资</h4>
                    <p>平台注册用户正式突破10万大关，月活跃用户超过3万。成功获得A轮数千万融资，由知名投资机构领投。融资资金用于技术升级、品类拓展和仓储建设，为下一阶段的高速发展奠定坚实基础。</p>
                    <div class="timeline-milestones">
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>10万用户</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>A轮融资</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>技术升级</span>
                    </div>
                </div>
                <div class="timeline-dot"></div>
            </div>

            <div class="timeline-item">
                <div class="timeline-content">
                    <span class="timeline-year">2023</span>
                    <h4><i class="fas fa-warehouse" style="color:#d97706; margin-right:8px;"></i>拓展品类，建立仓储中心</h4>
                    <p>图书品类扩展至50,000种，涵盖教育、艺术、童书、外文原版等全品类。在北京、上海、广州三地建立智能仓储中心，实现核心城市"次日达"配送服务。引入AI智能推荐系统，用户购书转化率提升40%。</p>
                    <div class="timeline-milestones">
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>5万品类</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>三地仓储</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>AI推荐</span>
                    </div>
                </div>
                <div class="timeline-dot"></div>
            </div>

            <div class="timeline-item">
                <div class="timeline-content">
                    <span class="timeline-year">2024</span>
                    <h4><i class="fas fa-crown" style="color:#db2777; margin-right:8px;"></i>用户突破100万，推出VIP会员</h4>
                    <p>平台累计注册用户突破100万，年销售额同比增长200%。正式推出VIP会员体系，为忠实读者提供专属折扣、优先配送、生日礼遇等增值服务。与全国200余家出版社建立深度合作，成为国内领先的在线图书平台之一。</p>
                    <div class="timeline-milestones">
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>100万用户</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>VIP会员</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>200家合作出版社</span>
                    </div>
                </div>
                <div class="timeline-dot"></div>
            </div>

            <div class="timeline-item">
                <div class="timeline-content">
                    <span class="timeline-year">2025</span>
                    <h4><i class="fas fa-globe-asia" style="color:#7c3aed; margin-right:8px;"></i>全国布局，技术创新</h4>
                    <p>仓储中心扩展至全国8个城市，实现全国主要城市"当日达"覆盖。图书品类突破100,000种，用户规模持续增长。推出自主研发的智能搜索和语音购书功能，持续引领行业技术创新，致力于打造最优质的在线阅读生态。</p>
                    <div class="timeline-milestones">
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>8大仓储</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>10万品类</span>
                        <span class="milestone-tag"><i class="fas fa-check-circle"></i>语音购书</span>
                    </div>
                </div>
                <div class="timeline-dot"></div>
            </div>
        </div>

        <div class="stats-row fade-in">
            <div class="stat-box">
                <span class="stat-number">1,000,000+</span>
                <span class="stat-label">累计用户</span>
            </div>
            <div class="stat-box">
                <span class="stat-number">100,000+</span>
                <span class="stat-label">图书品类</span>
            </div>
            <div class="stat-box">
                <span class="stat-number">8</span>
                <span class="stat-label">仓储中心</span>
            </div>
            <div class="stat-box">
                <span class="stat-number">200+</span>
                <span class="stat-label">合作出版社</span>
            </div>
        </div>

        <div class="row fade-in">
            <div class="col-md-12">
                <div class="future-card">
                    <span class="future-icon"><i class="fas fa-paper-plane"></i></span>
                    <h3>展望未来</h3>
                    <p>未来，BookVerse将继续秉持"让阅读触手可及"的初心，持续深耕在线图书领域。我们计划在三年内完成全国县级以上城市的物流覆盖，推出数字阅读和有声书服务，构建线上线下融合的阅读社区，致力于成为中国最受读者信赖和喜爱的综合性阅读平台。</p>
                </div>
            </div>
        </div>

    </div>
</div>

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
                    <li><a href="${pageContext.request.contextPath}/history">发展历程</a></li>
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

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@3.4.1/dist/js/bootstrap.min.js"></script>
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