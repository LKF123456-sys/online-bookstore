<%-- ============================================================
     complaint.jsp — 投诉建议页面
     功能：为用户提供投诉和建议的提交渠道，展示联系方式和反馈流程。
     说明：纯展示型页面，帮助用户了解如何提交投诉或建议。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>投诉建议 - BookVerse</title>
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
        .page-hero::after {
            content: '';
            position: absolute;
            bottom: -30%;
            left: -10%;
            width: 300px;
            height: 300px;
            background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
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
        .complaint-card {
            background: #fff;
            border-radius: 16px;
            padding: 32px;
            margin-bottom: 24px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.06);
            transition: all 0.3s;
            border: 1px solid rgba(0,0,0,0.05);
        }
        .complaint-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 40px rgba(0,0,0,0.12);
        }
        .complaint-card .icon-wrapper {
            width: 64px;
            height: 64px;
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            margin-bottom: 20px;
        }
        .complaint-card h3 {
            font-size: 20px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 12px;
        }
        .complaint-card p {
            color: var(--gray-600);
            line-height: 1.8;
            margin-bottom: 16px;
        }
        .complaint-card .channel-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        .complaint-card .channel-list li {
            padding: 14px 0;
            border-bottom: 1px solid var(--gray-100);
            display: flex;
            align-items: flex-start;
            gap: 14px;
        }
        .complaint-card .channel-list li:last-child {
            border-bottom: none;
        }
        .complaint-card .channel-list li .channel-icon {
            width: 40px;
            height: 40px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            flex-shrink: 0;
        }
        .complaint-card .channel-list li .channel-info h4 {
            font-size: 15px;
            font-weight: 600;
            color: var(--gray-700);
            margin: 0 0 4px;
        }
        .complaint-card .channel-list li .channel-info p {
            font-size: 13px;
            color: var(--gray-600);
            margin: 0;
        }
        .process-steps {
            list-style: none;
            padding: 0;
            margin: 0;
            position: relative;
        }
        .process-steps::before {
            content: '';
            position: absolute;
            left: 23px;
            top: 30px;
            bottom: 30px;
            width: 2px;
            background: linear-gradient(180deg, var(--primary), #a78bfa);
        }
        .process-steps li {
            padding: 12px 0 12px 60px;
            position: relative;
        }
        .process-steps li .step-num {
            position: absolute;
            left: 8px;
            top: 12px;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--primary), #7c3aed);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            font-weight: 700;
            z-index: 1;
        }
        .process-steps li .step-content h4 {
            font-size: 15px;
            font-weight: 700;
            color: var(--gray-700);
            margin: 0 0 4px;
        }
        .process-steps li .step-content p {
            font-size: 13px;
            color: var(--gray-600);
            margin: 0;
        }
        .faq-item {
            padding: 16px 0;
            border-bottom: 1px solid var(--gray-100);
        }
        .faq-item:last-child {
            border-bottom: none;
        }
        .faq-item .faq-q {
            display: flex;
            align-items: center;
            gap: 10px;
            font-weight: 600;
            color: var(--gray-700);
            margin-bottom: 8px;
            font-size: 15px;
        }
        .faq-item .faq-q i {
            color: var(--primary);
            font-size: 16px;
        }
        .faq-item .faq-a {
            color: var(--gray-600);
            line-height: 1.8;
            padding-left: 26px;
            font-size: 14px;
        }
        .survey-section {
            background: linear-gradient(135deg, #f8fafc, #eef2ff);
            border-radius: 16px;
            padding: 36px;
            margin-bottom: 24px;
            border: 1px solid rgba(79,70,229,0.1);
        }
        .survey-section h3 {
            font-size: 20px;
            font-weight: 700;
            color: var(--gray-800);
            margin-bottom: 16px;
        }
        .survey-section p {
            color: var(--gray-600);
            line-height: 1.8;
            margin-bottom: 24px;
        }
        .survey-stars {
            display: flex;
            gap: 16px;
            margin-bottom: 24px;
            flex-wrap: wrap;
        }
        .survey-star {
            width: 64px;
            height: 64px;
            border-radius: 16px;
            background: #fff;
            border: 2px solid var(--gray-200);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 4px;
            cursor: pointer;
            transition: all 0.3s;
        }
        .survey-star:hover {
            border-color: var(--primary);
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(79,70,229,0.15);
        }
        .survey-star i {
            font-size: 20px;
            color: #f59e0b;
        }
        .survey-star span {
            font-size: 11px;
            font-weight: 600;
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
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">&#128218; BookVerse</a>
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
        <h1>&#128221; 投诉建议</h1>
        <p>您的每一条反馈都是我们进步的动力</p>
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
                <div class="complaint-card">
                    <div class="icon-wrapper icon-blue">
                        <i class="fas fa-headset"></i>
                    </div>
                    <h3>投诉渠道</h3>
                    <p>我们提供多种投诉渠道，方便您随时联系我们：</p>
                    <ul class="channel-list">
                        <li>
                            <div class="channel-icon icon-purple">
                                <i class="fas fa-laptop"></i>
                            </div>
                            <div class="channel-info">
                                <h4>在线投诉</h4>
                                <p>登录账户后，在消息中心提交投诉工单，我们将在24小时内回复处理。</p>
                            </div>
                        </li>
                        <li>
                            <div class="channel-icon icon-green">
                                <i class="fas fa-phone"></i>
                            </div>
                            <div class="channel-info">
                                <h4>电话投诉</h4>
                                <p>拨打客服热线 400-888-8888，工作时间 9:00-21:00，全年无休。</p>
                            </div>
                        </li>
                        <li>
                            <div class="channel-icon icon-orange">
                                <i class="fas fa-envelope"></i>
                            </div>
                            <div class="channel-info">
                                <h4>邮箱投诉</h4>
                                <p>发送邮件至 complaint@bookverse.com，请注明订单号及问题描述。</p>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="complaint-card">
                    <div class="icon-wrapper icon-green">
                        <i class="fas fa-tasks"></i>
                    </div>
                    <h3>投诉处理流程</h3>
                    <p>我们承诺规范化处理每一条投诉：</p>
                    <ul class="process-steps">
                        <li>
                            <span class="step-num">1</span>
                            <div class="step-content">
                                <h4>提交投诉</h4>
                                <p>通过任意渠道提交您的投诉内容，请尽量提供详细的订单信息和问题描述。</p>
                            </div>
                        </li>
                        <li>
                            <span class="step-num">2</span>
                            <div class="step-content">
                                <h4>受理确认</h4>
                                <p>客服人员将在24小时内确认受理，并分配专属处理人员与您对接。</p>
                            </div>
                        </li>
                        <li>
                            <span class="step-num">3</span>
                            <div class="step-content">
                                <h4>调查核实</h4>
                                <p>对投诉内容进行调查核实，必要时与相关部门沟通协调。</p>
                            </div>
                        </li>
                        <li>
                            <span class="step-num">4</span>
                            <div class="step-content">
                                <h4>解决反馈</h4>
                                <p>在3个工作日内给出处理方案，并通过您选择的方式反馈结果。</p>
                            </div>
                        </li>
                        <li>
                            <span class="step-num">5</span>
                            <div class="step-content">
                                <h4>满意度回访</h4>
                                <p>处理完成后进行满意度回访，确保问题得到妥善解决。</p>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="col-md-6">
                <div class="complaint-card">
                    <div class="icon-wrapper icon-orange">
                        <i class="fas fa-question-circle"></i>
                    </div>
                    <h3>常见问题解答</h3>
                    <p>以下是用户反馈中最常见的问题：</p>
                    <div class="faq-item">
                        <div class="faq-q">
                            <i class="fas fa-question-circle"></i>
                            <span>收到的图书有质量问题怎么办？</span>
                        </div>
                        <div class="faq-a">如收到的图书存在印刷质量、装订损坏等问题，请在签收后7天内联系客服，我们提供免费换货服务。</div>
                    </div>
                    <div class="faq-item">
                        <div class="faq-q">
                            <i class="fas fa-question-circle"></i>
                            <span>快递长时间未到怎么处理？</span>
                        </div>
                        <div class="faq-a">普通快递3-5天到达，如超过7天仍未收到，请通过"我的订单"查询物流状态或联系客服协助查询。</div>
                    </div>
                    <div class="faq-item">
                        <div class="faq-q">
                            <i class="fas fa-question-circle"></i>
                            <span>退款什么时候能到账？</span>
                        </div>
                        <div class="faq-a">退款审核通过后，微信和支付宝退款1-3个工作日到账，银行卡退款3-7个工作日到账。</div>
                    </div>
                    <div class="faq-item">
                        <div class="faq-q">
                            <i class="fas fa-question-circle"></i>
                            <span>如何修改已提交的订单？</span>
                        </div>
                        <div class="faq-a">未支付的订单可取消后重新下单；已支付但未发货的订单可联系客服修改；已发货的订单暂不支持修改。</div>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="complaint-card">
                    <div class="icon-wrapper icon-pink">
                        <i class="fas fa-poll"></i>
                    </div>
                    <h3>满意度调查</h3>
                    <p>您对我们的服务满意吗？请为我们打分：</p>
                    <div class="survey-stars">
                        <div class="survey-star">
                            <i class="fas fa-star"></i>
                            <span>非常差</span>
                        </div>
                        <div class="survey-star">
                            <i class="fas fa-star"></i>
                            <span>较差</span>
                        </div>
                        <div class="survey-star">
                            <i class="fas fa-star"></i>
                            <span>一般</span>
                        </div>
                        <div class="survey-star">
                            <i class="fas fa-star"></i>
                            <span>满意</span>
                        </div>
                        <div class="survey-star">
                            <i class="fas fa-star"></i>
                            <span>非常满意</span>
                        </div>
                    </div>
                    <p style="font-size: 13px; color: var(--gray-600);">您的评价将帮助我们不断改进服务质量。点击评分后，可详细描述您的体验。</p>
                </div>

                <div class="complaint-card">
                    <div class="icon-wrapper icon-red">
                        <i class="fas fa-shield-alt"></i>
                    </div>
                    <h3>我们的承诺</h3>
                    <p>BookVerse郑重承诺：</p>
                    <ul class="channel-list">
                        <li>
                            <div class="channel-icon icon-green">
                                <i class="fas fa-clock"></i>
                            </div>
                            <div class="channel-info">
                                <h4>24小时响应</h4>
                                <p>所有投诉工单将在24小时内得到首次响应。</p>
                            </div>
                        </li>
                        <li>
                            <div class="channel-icon icon-blue">
                                <i class="fas fa-handshake"></i>
                            </div>
                            <div class="channel-info">
                                <h4>公平公正</h4>
                                <p>每一条投诉都将得到认真对待和公平处理。</p>
                            </div>
                        </li>
                        <li>
                            <div class="channel-icon icon-purple">
                                <i class="fas fa-lock"></i>
                            </div>
                            <div class="channel-info">
                                <h4>隐私保护</h4>
                                <p>严格保护投诉人的个人信息和隐私安全。</p>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <div class="contact-box">
            <h3>&#128222; 联系方式</h3>
            <p>如有任何问题或建议，欢迎随时联系我们</p>
            <div class="contact-info">
                <div class="contact-item">
                    <i class="fas fa-phone"></i>
                    <span>400-888-8888</span>
                </div>
                <div class="contact-item">
                    <i class="fas fa-envelope"></i>
                    <span>complaint@bookverse.com</span>
                </div>
                <div class="contact-item">
                    <i class="fas fa-clock"></i>
                    <span>工作时间: 9:00-21:00</span>
                </div>
                <div class="contact-item">
                    <i class="fas fa-map-marker-alt"></i>
                    <span>北京市海淀区中关村大街1号</span>
                </div>
            </div>
        </div>
    </div>
</div>

<footer class="footer-custom">
    <div class="container">
        <div class="row">
            <div class="col-md-4">
                <h5>&#128218; BookVerse</h5>
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
            <p>&copy; 2026 BookVerse. All rights reserved.</p>
        </div>
    </div>
</footer>

<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
</body>
</html>
