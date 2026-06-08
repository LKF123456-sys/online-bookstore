<%-- ============================================================
     careers.jsp — 招聘页面（加入我们）
     功能：展示公司文化、福利待遇和当前招聘职位列表。
     说明：包含"为什么选择BookVerse"、"福利待遇"（6项福利卡片）、
           "招聘职位"（Java开发、前端、产品经理、运营、客服等），
           以及投递简历的邮箱联系方式。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>加入我们 - BookVerse</title>
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
        .benefits-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 24px;
            margin-top: 20px;
        }
        .benefit-card {
            text-align: center;
            padding: 30px 20px;
            background: var(--white);
            border-radius: var(--radius-lg);
            transition: all 0.3s;
            border: 1px solid var(--gray-200);
        }
        .benefit-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--shadow-md);
            border-color: var(--primary-light);
        }
        .benefit-card .benefit-icon {
            font-size: 42px;
            margin-bottom: 15px;
            display: block;
        }
        .benefit-card h4 {
            font-size: 18px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 10px;
        }
        .benefit-card p {
            font-size: 14px;
            color: var(--gray-500);
            margin: 0;
            line-height: 1.7;
        }
        .job-card {
            background: var(--white);
            border-radius: var(--radius-lg);
            padding: 30px;
            margin-bottom: 20px;
            box-shadow: var(--shadow);
            transition: all 0.3s;
            border-left: 4px solid var(--primary);
        }
        .job-card:hover {
            transform: translateX(5px);
            box-shadow: var(--shadow-lg);
        }
        .job-card h4 {
            font-size: 20px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 8px;
        }
        .job-card .job-meta {
            display: flex;
            gap: 20px;
            margin-bottom: 16px;
            flex-wrap: wrap;
        }
        .job-card .job-meta span {
            font-size: 14px;
            color: var(--gray-500);
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .job-card .job-meta span i {
            color: var(--primary);
        }
        .job-card h5 {
            font-size: 15px;
            font-weight: 600;
            color: var(--gray-700);
            margin-bottom: 10px;
            margin-top: 16px;
        }
        .job-card ul {
            padding-left: 20px;
            margin-bottom: 0;
        }
        .job-card ul li {
            font-size: 14px;
            color: var(--gray-600);
            line-height: 1.8;
        }
        .apply-section {
            background: linear-gradient(135deg, var(--primary-dark), var(--primary));
            border-radius: var(--radius-xl);
            padding: 50px;
            text-align: center;
            color: #fff;
            margin-top: 20px;
            position: relative;
            overflow: hidden;
        }
        .apply-section::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -20%;
            width: 400px;
            height: 400px;
            background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
            border-radius: 50%;
        }
        .apply-section h3 {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 20px;
            position: relative;
            z-index: 1;
        }
        .apply-section p {
            font-size: 16px;
            opacity: 0.9;
            margin-bottom: 30px;
            position: relative;
            z-index: 1;
        }
        .apply-section .email-link {
            display: inline-flex;
            align-items: center;
            gap: 10px;
            background: rgba(255,255,255,0.2);
            padding: 16px 32px;
            border-radius: 50px;
            color: #fff;
            font-size: 18px;
            font-weight: 600;
            transition: all 0.3s;
            position: relative;
            z-index: 1;
            text-decoration: none;
        }
        .apply-section .email-link:hover {
            background: rgba(255,255,255,0.3);
            transform: translateY(-2px);
            color: #fff;
            text-decoration: none;
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
            .values-grid { grid-template-columns: 1fr; }
            .benefits-grid { grid-template-columns: 1fr; }
            .apply-section { padding: 30px 20px; }
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
        <span class="hero-icon"><i class="fas fa-hands-helping"></i></span>
        <h1>加入我们</h1>
        <p>与BookVerse一起，让阅读触手可及</p>
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
                    <div class="card-icon icon-bg-purple">
                        <i class="fas fa-rocket"></i>
                    </div>
                    <h3>为什么选择BookVerse</h3>
                    <p style="margin-bottom: 24px;">BookVerse是一家快速成长的互联网图书销售平台，我们致力于用科技改变阅读方式。在这里，你将与一群充满激情和创造力的伙伴共事，共同打造中国最优秀的在线书店。</p>
                    <div class="values-grid">
                        <div class="value-card">
                            <span class="value-icon"><i class="fas fa-seedling"></i></span>
                            <h4>企业文化</h4>
                            <p>我们崇尚开放、创新、协作的工作氛围，鼓励每位员工发挥创造力，让好想法能够快速落地实现。</p>
                        </div>
                        <div class="value-card">
                            <span class="value-icon"><i class="fas fa-chart-line"></i></span>
                            <h4>发展前景</h4>
                            <p>公司处于高速发展期，业务持续增长，为员工提供广阔的发展空间和快速晋升通道。</p>
                        </div>
                        <div class="value-card">
                            <span class="value-icon"><i class="fas fa-users"></i></span>
                            <h4>团队氛围</h4>
                            <p>扁平化管理，团队年轻有活力，定期举办团建活动，让工作与生活完美平衡。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-green">
                        <i class="fas fa-gift"></i>
                    </div>
                    <h3>福利待遇</h3>
                    <p style="margin-bottom: 24px;">我们为员工提供具有竞争力的薪酬福利体系，关注每一位伙伴的身心健康与职业发展。</p>
                    <div class="benefits-grid">
                        <div class="benefit-card">
                            <span class="benefit-icon" style="color: #2563eb;"><i class="fas fa-heartbeat"></i></span>
                            <h4>五险一金</h4>
                            <p>入职即缴纳五险一金，额外补充商业保险，全方位保障员工权益。</p>
                        </div>
                        <div class="benefit-card">
                            <span class="benefit-icon" style="color: #059669;"><i class="fas fa-umbrella-beach"></i></span>
                            <h4>带薪年假</h4>
                            <p>享受带薪年假、病假、婚假等多种假期，鼓励劳逸结合，保持最佳工作状态。</p>
                        </div>
                        <div class="benefit-card">
                            <span class="benefit-icon" style="color: #d97706;"><i class="fas fa-graduation-cap"></i></span>
                            <h4>培训发展</h4>
                            <p>定期组织专业技能培训、管理能力提升课程，支持员工持续学习和成长。</p>
                        </div>
                        <div class="benefit-card">
                            <span class="benefit-icon" style="color: #dc2626;"><i class="fas fa-coffee"></i></span>
                            <h4>节日福利</h4>
                            <p>逢年过节发放礼品礼金，生日惊喜祝福，让每位员工感受家的温暖。</p>
                        </div>
                        <div class="benefit-card">
                            <span class="benefit-icon" style="color: #7c3aed;"><i class="fas fa-book"></i></span>
                            <h4>员工购书优惠</h4>
                            <p>享受专属员工购书折扣，每月赠送图书额度，让阅读成为生活的一部分。</p>
                        </div>
                        <div class="benefit-card">
                            <span class="benefit-icon" style="color: #0891b2;"><i class="fas fa-dumbbell"></i></span>
                            <h4>健康关怀</h4>
                            <p>年度体检、健身补贴、心理咨询等福利，关注员工身心健康。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-blue">
                        <i class="fas fa-briefcase"></i>
                    </div>
                    <h3>招聘职位</h3>
                    <p style="margin-bottom: 24px;">我们正在寻找志同道合的伙伴加入BookVerse，一起创造更美好的阅读体验。</p>

                    <div class="job-card">
                        <h4><i class="fas fa-code" style="color: var(--primary); margin-right: 10px;"></i>Java开发工程师</h4>
                        <div class="job-meta">
                            <span><i class="fas fa-map-marker-alt"></i> 北京</span>
                            <span><i class="fas fa-clock"></i> 全职</span>
                            <span><i class="fas fa-money-bill-wave"></i> 20K-35K</span>
                        </div>
                        <h5>岗位职责</h5>
                        <ul>
                            <li>负责公司核心业务系统的后端开发与维护</li>
                            <li>参与系统架构设计，优化系统性能和稳定性</li>
                            <li>编写高质量代码，参与代码评审</li>
                            <li>与产品、前端团队紧密协作，按时交付项目</li>
                        </ul>
                        <h5>任职要求</h5>
                        <ul>
                            <li>本科及以上学历，计算机相关专业</li>
                            <li>3年以上Java开发经验，熟悉Spring Boot框架</li>
                            <li>熟悉MySQL、Redis等数据库技术</li>
                            <li>具备良好的编码习惯和团队协作能力</li>
                        </ul>
                    </div>

                    <div class="job-card">
                        <h4><i class="fas fa-palette" style="color: #7c3aed; margin-right: 10px;"></i>前端开发工程师</h4>
                        <div class="job-meta">
                            <span><i class="fas fa-map-marker-alt"></i> 北京</span>
                            <span><i class="fas fa-clock"></i> 全职</span>
                            <span><i class="fas fa-money-bill-wave"></i> 18K-30K</span>
                        </div>
                        <h5>岗位职责</h5>
                        <ul>
                            <li>负责电商平台前端页面的开发与优化</li>
                            <li>实现高保真UI设计稿，确保良好的用户体验</li>
                            <li>优化前端性能，提升页面加载速度</li>
                            <li>参与前端技术选型和架构设计</li>
                        </ul>
                        <h5>任职要求</h5>
                        <ul>
                            <li>本科及以上学历，计算机相关专业</li>
                            <li>2年以上前端开发经验，精通HTML/CSS/JavaScript</li>
                            <li>熟悉Vue.js或React等主流前端框架</li>
                            <li>有移动端H5开发经验者优先</li>
                        </ul>
                    </div>

                    <div class="job-card">
                        <h4><i class="fas fa-lightbulb" style="color: #d97706; margin-right: 10px;"></i>产品经理</h4>
                        <div class="job-meta">
                            <span><i class="fas fa-map-marker-alt"></i> 北京</span>
                            <span><i class="fas fa-clock"></i> 全职</span>
                            <span><i class="fas fa-money-bill-wave"></i> 25K-40K</span>
                        </div>
                        <h5>岗位职责</h5>
                        <ul>
                            <li>负责图书电商产品的规划、设计与迭代</li>
                            <li>深入分析用户需求，制定产品路线图</li>
                            <li>撰写产品需求文档，协调研发团队推进项目</li>
                            <li>跟踪产品数据，持续优化用户体验</li>
                        </ul>
                        <h5>任职要求</h5>
                        <ul>
                            <li>本科及以上学历，3年以上产品经理经验</li>
                            <li>有电商或内容平台产品经验者优先</li>
                            <li>具备优秀的需求分析和产品设计能力</li>
                            <li>良好的沟通协调能力和项目管理能力</li>
                        </ul>
                    </div>

                    <div class="job-card">
                        <h4><i class="fas fa-bullhorn" style="color: #059669; margin-right: 10px;"></i>运营专员</h4>
                        <div class="job-meta">
                            <span><i class="fas fa-map-marker-alt"></i> 北京</span>
                            <span><i class="fas fa-clock"></i> 全职</span>
                            <span><i class="fas fa-money-bill-wave"></i> 12K-20K</span>
                        </div>
                        <h5>岗位职责</h5>
                        <ul>
                            <li>负责平台日常运营活动的策划与执行</li>
                            <li>管理图书商品上架、促销活动配置</li>
                            <li>分析运营数据，制定优化策略</li>
                            <li>维护用户社群，提升用户活跃度和留存率</li>
                        </ul>
                        <h5>任职要求</h5>
                        <ul>
                            <li>本科及以上学历，市场营销或相关专业</li>
                            <li>1年以上电商运营经验</li>
                            <li>具备良好的数据分析能力和文案写作能力</li>
                            <li>热爱阅读，对图书市场有一定了解</li>
                        </ul>
                    </div>

                    <div class="job-card">
                        <h4><i class="fas fa-headset" style="color: #db2777; margin-right: 10px;"></i>客服专员</h4>
                        <div class="job-meta">
                            <span><i class="fas fa-map-marker-alt"></i> 北京</span>
                            <span><i class="fas fa-clock"></i> 全职</span>
                            <span><i class="fas fa-money-bill-wave"></i> 8K-15K</span>
                        </div>
                        <h5>岗位职责</h5>
                        <ul>
                            <li>通过在线聊天、电话等方式为用户提供咨询服务</li>
                            <li>处理用户订单问题、退换货申请等售后事务</li>
                            <li>收集用户反馈，协助优化服务流程</li>
                            <li>维护良好的客户关系，提升用户满意度</li>
                        </ul>
                        <h5>任职要求</h5>
                        <ul>
                            <li>大专及以上学历，不限专业</li>
                            <li>具备良好的沟通能力和服务意识</li>
                            <li>打字速度快，能熟练使用办公软件</li>
                            <li>有客服经验者优先，热爱阅读者优先</li>
                        </ul>
                    </div>

                </div>
            </div>
        </div>

        <div class="apply-section fade-in">
            <h3><i class="fas fa-paper-plane" style="margin-right: 12px;"></i>投递方式</h3>
            <p>如果您对以上职位感兴趣，请将个人简历发送至以下邮箱，我们将在3个工作日内回复您。</p>
            <a href="mailto:hr@bookverse.com" class="email-link">
                <i class="fas fa-envelope"></i> hr@bookverse.com
            </a>
            <p style="margin-top: 20px; margin-bottom: 0; font-size: 14px; opacity: 0.8;">
                邮件标题格式：姓名-应聘职位-联系电话
            </p>
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
                    <li><a href="#">发展历程</a></li>
                    <li><a href="${pageContext.request.contextPath}/careers">加入我们</a></li>
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