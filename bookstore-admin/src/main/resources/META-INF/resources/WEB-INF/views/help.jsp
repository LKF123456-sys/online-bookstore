<%-- ============================================================
     help.jsp — 帮助中心页面
     功能：为用户提供常见问题解答（FAQ），涵盖购物指南、配送查询、
           退换货说明、账户管理、优惠券使用和评价反馈六大板块。
     说明：纯展示型页面，采用卡片式布局，每个卡片对应一个帮助主题。
           底部有联系客服区域，提供电话和邮箱信息。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>帮助中心 - BookVerse</title>
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
        .help-card {
            background: #fff;
            border-radius: 16px;
            padding: 32px;
            margin-bottom: 24px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.06);
            transition: all 0.3s;
            border: 1px solid rgba(0,0,0,0.05);
        }
        .help-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 40px rgba(0,0,0,0.12);
        }
        .help-card .icon-wrapper {
            width: 64px;
            height: 64px;
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            margin-bottom: 20px;
        }
        .help-card h3 {
            font-size: 20px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 12px;
        }
        .help-card p {
            color: var(--gray-600);
            line-height: 1.8;
            margin-bottom: 16px;
        }
        .help-card .faq-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        .help-card .faq-list li {
            padding: 12px 0;
            border-bottom: 1px solid var(--gray-100);
            display: flex;
            align-items: flex-start;
            gap: 12px;
        }
        .help-card .faq-list li:last-child {
            border-bottom: none;
        }
        .help-card .faq-list li .faq-q {
            font-weight: 600;
            color: var(--gray-700);
            min-width: 80px;
        }
        .help-card .faq-list li .faq-a {
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
        <h1>📖 帮助中心</h1>
        <p>我们随时准备为您提供帮助，解答您的疑问</p>
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
                <div class="help-card">
                    <div class="icon-wrapper icon-blue">
                        <i class="fas fa-shopping-cart"></i>
                    </div>
                    <h3>购物指南</h3>
                    <p>如何在BookVerse购买图书？</p>
                    <ul class="faq-list">
                        <li>
                            <span class="faq-q">Q: 如何下单？</span>
                            <span class="faq-a">浏览商品，点击"加入购物车"，然后在购物车页面结算即可。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 如何支付？</span>
                            <span class="faq-a">支持微信支付、支付宝、银行卡等多种支付方式。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 可以修改订单吗？</span>
                            <span class="faq-a">未支付的订单可以取消重新下单，已支付订单请联系客服。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="help-card">
                    <div class="icon-wrapper icon-green">
                        <i class="fas fa-truck"></i>
                    </div>
                    <h3>配送查询</h3>
                    <p>了解订单配送状态</p>
                    <ul class="faq-list">
                        <li>
                            <span class="faq-q">Q: 如何查询物流？</span>
                            <span class="faq-a">登录后进入"我的订单"，点击订单详情查看物流信息。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 配送多久到达？</span>
                            <span class="faq-a">普通快递3-5天，顺丰快递1-2天，偏远地区可能稍有延迟。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 可以指定配送时间吗？</span>
                            <span class="faq-a">目前暂不支持指定配送时间，请保持电话畅通。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="help-card">
                    <div class="icon-wrapper icon-purple">
                        <i class="fas fa-undo"></i>
                    </div>
                    <h3>退换货说明</h3>
                    <p>了解退换货政策</p>
                    <ul class="faq-list">
                        <li>
                            <span class="faq-q">Q: 可以退换货吗？</span>
                            <span class="faq-a">支持7天无理由退换货，详情请查看退换货政策页面。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 退款多久到账？</span>
                            <span class="faq-a">审核通过后，退款将在3-7个工作日内原路返回。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 退货运费谁承担？</span>
                            <span class="faq-a">质量问题由商家承担运费，个人原因退换需自行承担。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="help-card">
                    <div class="icon-wrapper icon-orange">
                        <i class="fas fa-user"></i>
                    </div>
                    <h3>账户管理</h3>
                    <p>管理您的个人信息</p>
                    <ul class="faq-list">
                        <li>
                            <span class="faq-q">Q: 如何修改密码？</span>
                            <span class="faq-a">登录后进入"个人中心"，在账户安全中修改密码。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 忘记密码怎么办？</span>
                            <span class="faq-a">在登录页面点击"忘记密码"，通过邮箱验证重置密码。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 如何绑定手机号？</span>
                            <span class="faq-a">在个人中心的账户设置中绑定或更换手机号。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="help-card">
                    <div class="icon-wrapper icon-pink">
                        <i class="fas fa-ticket-alt"></i>
                    </div>
                    <h3>优惠券使用</h3>
                    <p>了解优惠券的使用规则</p>
                    <ul class="faq-list">
                        <li>
                            <span class="faq-q">Q: 如何获取优惠券？</span>
                            <span class="faq-a">参与活动、新用户注册、会员专享等方式获取优惠券。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 优惠券如何使用？</span>
                            <span class="faq-a">结算时在优惠券输入框输入优惠码，点击"兑换"即可。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 优惠券有有效期吗？</span>
                            <span class="faq-a">每张优惠券都有有效期，请在有效期内使用。</span>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="help-card">
                    <div class="icon-wrapper icon-red">
                        <i class="fas fa-star"></i>
                    </div>
                    <h3>评价与反馈</h3>
                    <p>分享您的购物体验</p>
                    <ul class="faq-list">
                        <li>
                            <span class="faq-q">Q: 如何评价商品？</span>
                            <span class="faq-a">订单完成后，在"我的订单"中点击"评价"按钮。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 评价有奖励吗？</span>
                            <span class="faq-a">优质评价可获得积分奖励，积分可兑换优惠券。</span>
                        </li>
                        <li>
                            <span class="faq-q">Q: 如何联系客服？</span>
                            <span class="faq-a">可通过消息中心联系管理员，或拨打客服热线。</span>
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