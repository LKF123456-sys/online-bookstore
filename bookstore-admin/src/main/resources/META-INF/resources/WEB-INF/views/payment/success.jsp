<%-- ============================================================
     payment/success.jsp — 支付成功页面
     功能：支付成功后展示给用户的确认页面，显示订单号和支付金额。
     说明：用户完成支付后自动跳转到此页面。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>支付成功 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <style>
        body {
            background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .success-container {
            max-width: 500px;
            width: 100%;
            margin: 40px 20px;
        }
        .success-card {
            background: #fff;
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-xl);
            padding: 50px 30px;
            text-align: center;
        }
        .checkmark-wrapper {
            margin-bottom: 25px;
        }
        .checkmark-circle {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto;
            animation: scaleIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
            box-shadow: 0 8px 30px rgba(16,185,129,0.3);
        }
        @keyframes scaleIn {
            0% { transform: scale(0); opacity: 0; }
            70% { transform: scale(1.15); }
            100% { transform: scale(1); opacity: 1; }
        }
        .checkmark-svg {
            width: 45px;
            height: 45px;
            animation: drawCheck 0.5s ease 0.3s both;
        }
        @keyframes drawCheck {
            0% { stroke-dashoffset: 60; }
            100% { stroke-dashoffset: 0; }
        }
        .checkmark-svg circle {
            fill: none;
            stroke: #fff;
            stroke-width: 3;
            stroke-dasharray: 60;
            stroke-dashoffset: 60;
            animation: drawCheck 0.4s ease 0.3s both;
        }
        .checkmark-svg path {
            fill: none;
            stroke: #fff;
            stroke-width: 3;
            stroke-linecap: round;
            stroke-linejoin: round;
            stroke-dasharray: 30;
            stroke-dashoffset: 30;
            animation: drawCheck 0.4s ease 0.5s both;
        }
        .success-card h2 {
            font-size: 26px;
            font-weight: 800;
            color: var(--gray-800);
            margin-bottom: 8px;
        }
        .success-card .success-msg {
            color: var(--gray-500);
            font-size: 15px;
            margin-bottom: 30px;
        }
        .order-info-box {
            background: var(--gray-50);
            border-radius: var(--radius-lg);
            padding: 20px 25px;
            margin-bottom: 30px;
            border: 1px solid var(--gray-100);
        }
        .order-info-box .info-label {
            color: var(--gray-500);
            font-size: 13px;
            margin-bottom: 4px;
        }
        .order-info-box .info-value {
            color: var(--primary);
            font-size: 20px;
            font-weight: 800;
        }
        .order-info-box .info-divider {
            height: 1px;
            background: var(--gray-200);
            margin: 15px 0;
        }
        .order-info-box .amount-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .order-info-box .amount-row .info-label {
            margin-bottom: 0;
        }
        .order-info-box .amount-row .amount-value {
            font-size: 24px;
            font-weight: 800;
            color: var(--success);
        }
        .order-info-box .amount-row .amount-value::before {
            content: '¥';
            font-size: 16px;
        }
        .btn-row {
            display: flex;
            gap: 12px;
            margin-bottom: 20px;
        }
        .btn-home {
            flex: 1;
            padding: 14px;
            border-radius: var(--radius);
            border: none;
            cursor: pointer;
            font-weight: 700;
            font-size: 15px;
            background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
            color: #fff;
            transition: all 0.3s;
            text-decoration: none;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .btn-home:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(79,70,229,0.4);
            color: #fff;
            text-decoration: none;
        }
        .btn-orders {
            flex: 1;
            padding: 14px;
            border-radius: var(--radius);
            border: 2px solid var(--gray-200);
            cursor: pointer;
            font-weight: 600;
            font-size: 15px;
            background: #fff;
            color: var(--gray-700);
            transition: all 0.3s;
            text-decoration: none;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .btn-orders:hover {
            border-color: var(--primary);
            color: var(--primary);
            text-decoration: none;
        }
        .celebration-dots {
            position: relative;
            height: 0;
        }
        .celebration-dot {
            position: absolute;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            animation: floatUp 2s ease-out infinite;
        }
        @keyframes floatUp {
            0% { transform: translateY(0) scale(0); opacity: 1; }
            100% { transform: translateY(-120px) scale(1); opacity: 0; }
        }
        .dot-1 { background: #10b981; left: 20%; animation-delay: 0s; }
        .dot-2 { background: #818cf8; left: 40%; animation-delay: 0.3s; }
        .dot-3 { background: #f59e0b; left: 55%; animation-delay: 0.6s; }
        .dot-4 { background: #ef4444; left: 70%; animation-delay: 0.9s; }
        .dot-5 { background: #0ea5e9; left: 85%; animation-delay: 1.2s; }
        .dot-6 { background: #10b981; left: 10%; animation-delay: 0.15s; }
        .dot-7 { background: #f59e0b; left: 50%; animation-delay: 0.45s; }
        .dot-8 { background: #818cf8; left: 30%; animation-delay: 0.75s; }
    </style>
</head>
<body>
<div class="success-container">
    <div class="success-card">
        <div class="celebration-dots">
            <div class="celebration-dot dot-1"></div>
            <div class="celebration-dot dot-2"></div>
            <div class="celebration-dot dot-3"></div>
            <div class="celebration-dot dot-4"></div>
            <div class="celebration-dot dot-5"></div>
            <div class="celebration-dot dot-6"></div>
            <div class="celebration-dot dot-7"></div>
            <div class="celebration-dot dot-8"></div>
        </div>
        <div class="checkmark-wrapper">
            <div class="checkmark-circle">
                <svg class="checkmark-svg" viewBox="0 0 24 24">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M7 12l3.5 3.5L17 9"/>
                </svg>
            </div>
        </div>
        <h2>支付成功</h2>
        <p class="success-msg">感谢您的购买，订单已成功提交</p>

        <div class="order-info-box">
            <div class="info-label">订单编号</div>
            <div class="info-value">${orderId}</div>
            <div class="info-divider"></div>
            <div class="amount-row">
                <span class="info-label">支付金额</span>
                <span class="amount-value">${orderAmount}</span>
            </div>
        </div>

        <div class="btn-row">
            <a href="${pageContext.request.contextPath}/" class="btn-home">🏠 返回首页</a>
            <a href="${pageContext.request.contextPath}/order/history" class="btn-orders">📋 查看订单</a>
        </div>
    </div>
</div>
</body>
</html>
