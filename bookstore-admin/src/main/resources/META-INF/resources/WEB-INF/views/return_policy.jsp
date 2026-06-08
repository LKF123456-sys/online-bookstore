<%-- ============================================================
     return_policy.jsp — 退换货政策页面
     功能：向用户说明 BookVerse 的退换货政策，包括退货条件、
           换货流程、退款方式和时间等信息。
     说明：纯展示型页面，帮助用户了解退换货相关规则。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>退换货政策 - BookVerse</title>
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
        .policy-card {
            background: #fff;
            border-radius: 16px;
            padding: 32px;
            margin-bottom: 24px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.06);
            transition: all 0.3s;
            border: 1px solid rgba(0,0,0,0.05);
        }
        .policy-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 40px rgba(0,0,0,0.12);
        }
        .policy-card .icon-wrapper {
            width: 64px;
            height: 64px;
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            margin-bottom: 20px;
        }
        .policy-card h3 {
            font-size: 20px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 12px;
        }
        .policy-card p {
            color: var(--gray-600);
            line-height: 1.8;
            margin-bottom: 16px;
        }
        .policy-card .policy-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        .policy-card .policy-list li {
            padding: 12px 0;
            border-bottom: 1px solid var(--gray-100);
            display: flex;
            align-items: flex-start;
            gap: 12px;
        }
        .policy-card .policy-list li:last-child {
            border-bottom: none;
        }
        .policy-card .policy-list li .policy-item {
            font-weight: 600;
            color: var(--gray-700);
            min-width: 80px;
        }
        .policy-card .policy-list li .policy-detail {
            color: var(--gray-600);
        }
        .icon-blue { background: linear-gradient(135deg, #dbeafe, #93c5fd); color: #2563eb; }
        .icon-green { background: linear-gradient(135deg, #d1fae5, #6ee7b7); color: #059669; }
        .icon-purple { background: linear-gradient(135deg, #ede9fe, #c4b5fd); color: #7c3aed; }
        .icon-orange { background: linear-gradient(135deg, #fef3c7, #fcd34d); color: #d97706; }
        .icon-pink { background: linear-gradient(135deg, #fce7f3, #f9a8d4); color: #db2777; }
        .icon-red { background: linear-gradient(135deg, #fee2e2, #fca5a5); color: #dc2626; }
        .contact-box {
            background: linear-gradient(135deg, var(--primary), #7c3aed);
            border-radius: 16px;
            padding: 40px;
            color: #fff;
            text-align: center;
            margin-top: 40px;
        }
        .contact-box h3 {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 16px;
        }
        .contact-box p {
            opacity: 0.9;
            margin-bottom: 24px;
        }
        .contact-box .contact-info {
            display: flex;
            justify-content: center;
            gap: 40px;
            flex-wrap: wrap;
        }
        .contact-box .contact-item {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 16px;
        }
        .contact-box .contact-item i {
            font-size: 20px;
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
        <h1>📋 退换货政策</h1>
        <p>了解我们的退换货政策，保障您的购物权益</p>
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
                <div class="policy-card">
                    <div class="icon-wrapper icon-blue">
                        <i class="fas fa-check-circle"></i>
                    </div>
                    <h3>退换货条件</h3>
                    <p>我们提供7天无理由退换货服务，让您的购物更放心。</p>
                    <ul class="policy-list">
                        <li>
                            <span class="policy-item">时间限制</span>
                            <span class="policy-detail">自收到商品之日起7天内可申请退换货。</span>
                        </li>
                        <li>
                            <span class="policy-item">商品状态</span>
                            <span class="policy-detail">商品需保持原样，未拆封、未使用，包装完好。</span>
                        </li>
                        <li>
                            <span class="policy-item">凭证要求</span>
                            <span class="policy-detail">需提供订单号、商品照片等有效凭证。</span>
                        </li>
                        <li>
                            <span class="policy-item">质量问题</span>
                            <span class="policy-detail">如商品存在质量问题，可随时申请退换货。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="policy-card">
                    <div class="icon-wrapper icon-green">
                        <i class="fas fa-tasks"></i>
                    </div>
                    <h3>退换货流程</h3>
                    <p>简单四步完成退换货申请，快速处理您的需求。</p>
                    <ul class="policy-list">
                        <li>
                            <span class="policy-item">第一步</span>
                            <span class="policy-detail">登录账户，进入"我的订单"页面，找到需要退换货的订单。</span>
                        </li>
                        <li>
                            <span class="policy-item">第二步</span>
                            <span class="policy-detail">点击"申请退换货"，填写退换货原因和说明。</span>
                        </li>
                        <li>
                            <span class="policy-item">第三步</span>
                            <span class="policy-detail">等待客服审核（1-2个工作日），审核通过后按指引寄回商品。</span>
                        </li>
                        <li>
                            <span class="policy-item">第四步</span>
                            <span class="policy-detail">收到商品后，我们将尽快处理退款或换货。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="policy-card">
                    <div class="icon-wrapper icon-purple">
                        <i class="fas fa-money-bill-wave"></i>
                    </div>
                    <h3>退款说明</h3>
                    <p>了解退款方式和到账时间，让您心中有数。</p>
                    <ul class="policy-list">
                        <li>
                            <span class="policy-item">退款方式</span>
                            <span class="policy-detail">退款将原路返回至您的支付账户（微信、支付宝、银行卡等）。</span>
                        </li>
                        <li>
                            <span class="policy-item">退款时间</span>
                            <span class="policy-detail">审核通过后，退款将在3-7个工作日内到账。</span>
                        </li>
                        <li>
                            <span class="policy-item">退款金额</span>
                            <span class="policy-detail">退款金额为商品实际支付金额，优惠券部分不予退还。</span>
                        </li>
                        <li>
                            <span class="policy-item">运费说明</span>
                            <span class="policy-detail">质量问题退换货由商家承担运费，个人原因退换货需自行承担运费。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="policy-card">
                    <div class="icon-wrapper icon-orange">
                        <i class="fas fa-exclamation-triangle"></i>
                    </div>
                    <h3>不支持退换货的情况</h3>
                    <p>以下情况不在退换货范围内，请您知悉。</p>
                    <ul class="policy-list">
                        <li>
                            <span class="policy-item">超时申请</span>
                            <span class="policy-detail">超过7天无理由退换货期限的商品。</span>
                        </li>
                        <li>
                            <span class="policy-item">商品损坏</span>
                            <span class="policy-detail">因个人原因导致商品损坏、污染或影响二次销售。</span>
                        </li>
                        <li>
                            <span class="policy-item">定制商品</span>
                            <span class="policy-detail">个性化定制商品、签名版图书等特殊商品。</span>
                        </li>
                        <li>
                            <span class="policy-item">数字商品</span>
                            <span class="policy-detail">已下载的电子书、音频课程等数字商品。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-12">
                <div class="policy-card">
                    <div class="icon-wrapper icon-pink">
                        <i class="fas fa-headset"></i>
                    </div>
                    <h3>联系客服</h3>
                    <p>如有任何疑问，欢迎随时联系我们的客服团队。</p>
                    <ul class="policy-list">
                        <li>
                            <span class="policy-item">客服热线</span>
                            <span class="policy-detail">400-888-8888（工作时间：9:00-21:00）</span>
                        </li>
                        <li>
                            <span class="policy-item">在线客服</span>
                            <span class="policy-detail">登录账户后，点击页面右下角的在线客服图标。</span>
                        </li>
                        <li>
                            <span class="policy-item">邮箱支持</span>
                            <span class="policy-detail">support@bookverse.com（24小时内回复）</span>
                        </li>
                        <li>
                            <span class="policy-item">消息中心</span>
                            <span class="policy-detail">通过"消息中心"联系管理员，获取一对一帮助。</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <div class="contact-box">
            <h3>📞 还有疑问？联系我们</h3>
            <p>我们的客服团队随时准备为您提供帮助</p>
            <div class="contact-info">
                <div class="contact-item">
                    <i class="fas fa-phone"></i>
                    <span>400-888-8888</span>
                </div>
                <div class="contact-item">
                    <i class="fas fa-envelope"></i>
                    <span>support@bookverse.com</span>
                </div>
                <div class="contact-item">
                    <i class="fas fa-clock"></i>
                    <span>工作时间: 9:00-21:00</span>
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