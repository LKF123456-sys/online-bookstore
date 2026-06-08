<%-- ============================================================
     partners.jsp — 合作伙伴页面
     功能：展示 BookVerse 的合作伙伴列表、合作模式和合作优势。
     说明：包含合作伙伴介绍、6家知名出版社列表、3种合作模式（供货/营销/技术）、
           合作优势说明，以及成为合作伙伴的申请流程和联系方式。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>合作伙伴 - BookVerse</title>
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
        .partner-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 24px;
            margin-top: 20px;
        }
        .partner-card {
            text-align: center;
            padding: 30px 20px;
            background: linear-gradient(135deg, #f8fafc, #eef2ff);
            border-radius: var(--radius-lg);
            transition: all 0.3s;
            border: 1px solid rgba(79,70,229,0.08);
        }
        .partner-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--shadow-md);
            border-color: rgba(79,70,229,0.2);
        }
        .partner-card .partner-icon {
            width: 72px;
            height: 72px;
            border-radius: 50%;
            margin: 0 auto 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 32px;
            background: linear-gradient(135deg, var(--primary), #7c3aed);
            color: #fff;
            box-shadow: 0 4px 15px rgba(79,70,229,0.3);
        }
        .partner-card:nth-child(2) .partner-icon { background: linear-gradient(135deg, #2563eb, #3b82f6); box-shadow: 0 4px 15px rgba(37,99,235,0.3); }
        .partner-card:nth-child(3) .partner-icon { background: linear-gradient(135deg, #059669, #10b981); box-shadow: 0 4px 15px rgba(5,150,105,0.3); }
        .partner-card:nth-child(4) .partner-icon { background: linear-gradient(135deg, #d97706, #f59e0b); box-shadow: 0 4px 15px rgba(217,119,6,0.3); }
        .partner-card:nth-child(5) .partner-icon { background: linear-gradient(135deg, #db2777, #ec4899); box-shadow: 0 4px 15px rgba(219,39,119,0.3); }
        .partner-card:nth-child(6) .partner-icon { background: linear-gradient(135deg, #dc2626, #ef4444); box-shadow: 0 4px 15px rgba(220,38,38,0.3); }
        .partner-card h4 {
            font-size: 17px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 8px;
        }
        .partner-card p {
            font-size: 13px;
            color: var(--gray-500);
            margin: 0;
            line-height: 1.7;
        }
        .cooperation-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 24px;
            margin-top: 20px;
        }
        .cooperation-card {
            text-align: center;
            padding: 30px 20px;
            background: var(--white);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow);
            transition: all 0.3s;
            border: 1px solid rgba(0,0,0,0.04);
        }
        .cooperation-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--shadow-lg);
        }
        .cooperation-card .coop-icon {
            width: 64px;
            height: 64px;
            border-radius: var(--radius-lg);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            margin: 0 auto 16px;
        }
        .cooperation-card h4 {
            font-size: 18px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 10px;
        }
        .cooperation-card p {
            font-size: 14px;
            color: var(--gray-500);
            margin: 0;
            line-height: 1.7;
        }
        .advantage-list {
            list-style: none;
            padding: 0;
            margin: 20px 0 0;
        }
        .advantage-list li {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 16px 0;
            border-bottom: 1px solid var(--gray-100);
        }
        .advantage-list li:last-child { border-bottom: none; }
        .advantage-list li .advantage-icon {
            width: 48px;
            height: 48px;
            border-radius: var(--radius);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            flex-shrink: 0;
        }
        .advantage-list li .advantage-content h4 {
            font-size: 15px;
            font-weight: 600;
            color: var(--gray-800);
            margin: 0 0 4px;
        }
        .advantage-list li .advantage-content p {
            font-size: 13px;
            color: var(--gray-500);
            margin: 0;
        }
        .apply-section {
            background: linear-gradient(135deg, var(--primary-dark), var(--primary));
            border-radius: var(--radius-xl);
            padding: 40px;
            color: #fff;
            text-align: center;
            margin-top: 10px;
            position: relative;
            overflow: hidden;
        }
        .apply-section::before {
            content: '';
            position: absolute;
            top: -30%;
            right: -10%;
            width: 300px;
            height: 300px;
            background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
            border-radius: 50%;
        }
        .apply-section h3 {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 16px;
            position: relative;
            z-index: 1;
        }
        .apply-section p {
            font-size: 15px;
            opacity: 0.9;
            line-height: 1.8;
            margin-bottom: 20px;
            position: relative;
            z-index: 1;
        }
        .apply-section .apply-steps {
            display: flex;
            justify-content: center;
            gap: 40px;
            margin: 24px 0;
            position: relative;
            z-index: 1;
        }
        .apply-section .apply-step {
            text-align: center;
        }
        .apply-section .apply-step .step-number {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            background: rgba(255,255,255,0.2);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            font-weight: 700;
            margin: 0 auto 12px;
            border: 2px solid rgba(255,255,255,0.3);
        }
        .apply-section .apply-step .step-label {
            font-size: 14px;
            opacity: 0.9;
        }
        .apply-section .apply-contact {
            margin-top: 24px;
            padding: 16px 28px;
            background: rgba(255,255,255,0.15);
            border-radius: 50px;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            font-size: 15px;
            font-weight: 600;
            position: relative;
            z-index: 1;
            border: 1px solid rgba(255,255,255,0.2);
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
            .partner-grid { grid-template-columns: 1fr; }
            .cooperation-grid { grid-template-columns: 1fr; }
            .apply-section .apply-steps { flex-direction: column; gap: 20px; }
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
        <span class="hero-icon"><i class="fas fa-handshake"></i></span>
        <h1>合作伙伴</h1>
        <p>携手共创，共赢未来 — 与BookVerse共同推动全民阅读事业</p>
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
                        <i class="fas fa-handshake"></i>
                    </div>
                    <h3>合作伙伴介绍</h3>
                    <p>BookVerse与多家知名出版社建立了长期稳定的合作关系，共同致力于为读者提供最优质的图书产品和服务。我们始终秉持"互利共赢、协同发展"的理念，通过资源整合与优势互补，不断拓展合作领域，提升合作层次。目前，我们已与国内数十家顶级出版社达成战略合作协议，涵盖文学、科技、教育、学术等多个领域，确保为读者提供丰富多样、品质卓越的图书选择。</p>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-blue">
                        <i class="fas fa-building"></i>
                    </div>
                    <h3>合作伙伴列表</h3>
                    <p style="margin-bottom: 24px;">我们精选行业内最具影响力的出版社作为核心合作伙伴，共同为广大读者奉上精品图书。</p>
                    <div class="partner-grid">
                        <div class="partner-card">
                            <div class="partner-icon"><i class="fas fa-landmark"></i></div>
                            <h4>人民文学出版社</h4>
                            <p>中国最大的文学出版社之一，以出版中外优秀文学作品著称，拥有丰富的经典文学资源。</p>
                        </div>
                        <div class="partner-card">
                            <div class="partner-icon"><i class="fas fa-rocket"></i></div>
                            <h4>中信出版社</h4>
                            <p>以财经、社科类图书见长，致力于引进全球前沿思想，打造具有国际视野的知识平台。</p>
                        </div>
                        <div class="partner-card">
                            <div class="partner-icon"><i class="fas fa-cogs"></i></div>
                            <h4>机械工业出版社</h4>
                            <p>国内领先的科技出版机构，在计算机、工程技术、经管等领域拥有强大的出版实力。</p>
                        </div>
                        <div class="partner-card">
                            <div class="partner-icon"><i class="fas fa-graduation-cap"></i></div>
                            <h4>清华大学出版社</h4>
                            <p>依托清华大学学术资源，出版高品质的学术著作和高校教材，是教育出版领域的标杆。</p>
                        </div>
                        <div class="partner-card">
                            <div class="partner-icon"><i class="fas fa-microchip"></i></div>
                            <h4>电子工业出版社</h4>
                            <p>专注于信息技术、电子科技类图书出版，为IT从业者和科技爱好者提供权威知识内容。</p>
                        </div>
                        <div class="partner-card">
                            <div class="partner-icon"><i class="fas fa-satellite-dish"></i></div>
                            <h4>人民邮电出版社</h4>
                            <p>在通信、计算机、电子信息等领域具有深厚积累，是科技类图书出版的重要力量。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-green">
                        <i class="fas fa-project-diagram"></i>
                    </div>
                    <h3>合作模式</h3>
                    <p style="margin-bottom: 24px;">BookVerse提供多元化的合作模式，满足不同类型合作伙伴的需求，实现互利共赢。</p>
                    <div class="cooperation-grid">
                        <div class="cooperation-card">
                            <div class="coop-icon icon-bg-blue">
                                <i class="fas fa-truck-loading"></i>
                            </div>
                            <h4>供货合作</h4>
                            <p>与出版社建立直接供货关系，确保图书品质与供应稳定性。通过高效的供应链管理，实现快速上架与及时补货，为读者提供最新的图书资源。</p>
                        </div>
                        <div class="cooperation-card">
                            <div class="coop-icon icon-bg-amber">
                                <i class="fas fa-bullhorn"></i>
                            </div>
                            <h4>营销合作</h4>
                            <p>联合开展线上线下营销活动，包括新书首发、作家签售、主题书展等。通过资源共享和渠道互通，扩大品牌影响力，提升图书销量。</p>
                        </div>
                        <div class="cooperation-card">
                            <div class="coop-icon icon-bg-pink">
                                <i class="fas fa-code"></i>
                            </div>
                            <h4>技术合作</h4>
                            <p>在数字化阅读、智能推荐、大数据分析等领域展开深度技术合作，共同探索图书行业的数字化转型之路，提升用户阅读体验。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-amber">
                        <i class="fas fa-trophy"></i>
                    </div>
                    <h3>合作优势</h3>
                    <ul class="advantage-list">
                        <li>
                            <div class="advantage-icon" style="background: linear-gradient(135deg, #ede9fe, #c4b5fd); color: var(--primary);">
                                <i class="fas fa-puzzle-piece"></i>
                            </div>
                            <div class="advantage-content">
                                <h4>资源互补</h4>
                                <p>BookVerse拥有庞大的用户基础和先进的技术平台，出版社拥有丰富的出版资源和作者网络。双方通过资源互补，能够实现1+1>2的协同效应，共同开拓更广阔的市场空间。我们的大数据分析能力可以帮助出版社精准定位目标读者，优化选题策划，提高出版效率。</p>
                            </div>
                        </li>
                        <li>
                            <div class="advantage-icon" style="background: linear-gradient(135deg, #d1fae5, #6ee7b7); color: #059669;">
                                <i class="fas fa-award"></i>
                            </div>
                            <div class="advantage-content">
                                <h4>品牌共赢</h4>
                                <p>通过与知名出版社的深度合作，BookVerse进一步提升了平台的权威性和可信度，而出版社也借助BookVerse的线上渠道触达了更广泛的读者群体。双方品牌相互赋能，共同构建了值得读者信赖的图书消费生态，实现了品牌价值的持续增长。</p>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="about-card fade-in">
                    <div class="card-icon icon-bg-red">
                        <i class="fas fa-envelope-open-text"></i>
                    </div>
                    <h3>成为合作伙伴</h3>
                    <div class="apply-section">
                        <h3>期待与您携手同行</h3>
                        <p>如果您是出版社、图书供应商或相关行业的企业，欢迎与我们建立合作关系。<br>BookVerse期待与更多优秀伙伴共同成长，一起推动全民阅读事业发展。</p>
                        <div class="apply-steps">
                            <div class="apply-step">
                                <div class="step-number">1</div>
                                <div class="step-label">提交合作申请</div>
                            </div>
                            <div class="apply-step">
                                <div class="step-number">2</div>
                                <div class="step-label">商务洽谈对接</div>
                            </div>
                            <div class="apply-step">
                                <div class="step-number">3</div>
                                <div class="step-label">签订合作协议</div>
                            </div>
                            <div class="apply-step">
                                <div class="step-number">4</div>
                                <div class="step-label">正式开启合作</div>
                            </div>
                        </div>
                        <div class="apply-contact">
                            <i class="fas fa-envelope"></i>
                            合作邮箱：partner@bookverse.com
                        </div>
                    </div>
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
                    <li><a href="#">发展历程</a></li>
                    <li><a href="#">加入我们</a></li>
                    <li><a href="${pageContext.request.contextPath}/partners">合作伙伴</a></li>
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