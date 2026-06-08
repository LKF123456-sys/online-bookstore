<%-- 支付页面 payment.jsp --%>
<%-- 功能：订单支付页面，展示订单金额和支付方式选择 --%>
<%-- 支持支付宝、微信支付、银行卡等多种支付方式 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>选择支付方式 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <style>
        :root {
            --primary: #4f46e5;
            --primary-dark: #3730a3;
            --accent: #f59e0b;
            --danger: #ef4444;
            --success: #10b981;
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
            --shadow-glass: 0 8px 32px rgba(31, 38, 135, 0.12);
            --radius: 8px;
            --radius-lg: 12px;
            --radius-xl: 16px;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 30%, #f0f4ff 70%, #faf5ff 100%);
            min-height: 100vh;
            color: var(--gray-800);
        }
        .payment-container {
            max-width: 680px;
            margin: 40px auto;
            padding: 0 20px;
        }
        .payment-card {
            background: rgba(255, 255, 255, 0.65);
            backdrop-filter: blur(20px) saturate(180%);
            -webkit-backdrop-filter: blur(20px) saturate(180%);
            border: 1px solid rgba(255, 255, 255, 0.5);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-glass);
            padding: 40px;
            position: relative;
            overflow: hidden;
            animation: cardSlideIn 0.6s ease-out;
        }
        @keyframes cardSlideIn { from { opacity: 0; transform: translateY(30px) scale(0.97); } to { opacity: 1; transform: translateY(0) scale(1); } }
        .payment-card::before {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0;
            height: 1px;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.9), transparent);
        }
        .payment-header {
            text-align: center;
            margin-bottom: 30px;
        }
        .payment-header .payment-icon {
            font-size: 56px;
            margin-bottom: 16px;
            display: block;
            animation: iconFloat 3s ease-in-out infinite;
            filter: drop-shadow(0 4px 8px rgba(0,0,0,0.1));
        }
        @keyframes iconFloat { 0%, 100% { transform: translateY(0) rotate(0deg); } 50% { transform: translateY(-8px) rotate(3deg); } }
        .payment-header h2 {
            font-size: 28px;
            font-weight: 800;
            color: var(--gray-800);
            margin-bottom: 6px;
        }
        .payment-header p {
            color: var(--gray-500);
            font-size: 15px;
        }
        .order-amount-box {
            background: linear-gradient(135deg, rgba(79,70,229,0.06) 0%, rgba(139,92,246,0.06) 100%);
            border: 1.5px solid rgba(79,70,229,0.15);
            border-radius: var(--radius-lg);
            padding: 22px 26px;
            margin-bottom: 28px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            transition: all 0.3s;
        }
        .order-amount-box:hover { border-color: rgba(79,70,229,0.3); box-shadow: 0 4px 20px rgba(79,70,229,0.08); }
        .order-amount-box .order-info-label {
            color: var(--gray-500);
            font-size: 13px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .order-amount-box .order-id {
            color: var(--gray-700);
            font-weight: 700;
            font-size: 15px;
            font-family: 'SF Mono', 'Consolas', monospace;
            margin-top: 4px;
        }
        .order-amount-box .amount-label {
            color: var(--gray-500);
            font-size: 13px;
            font-weight: 600;
            text-align: right;
        }
        .order-amount-box .amount-value {
            font-size: 34px;
            font-weight: 800;
            color: var(--danger);
            text-align: right;
        }
        .order-amount-box .amount-value::before {
            content: '¥';
            font-size: 20px;
        }
        .timer-section {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            margin-bottom: 28px;
            padding: 14px 18px;
            background: linear-gradient(135deg, #fef2f2 0%, #fff1f2 100%);
            border: 1px solid #fecaca;
            border-radius: var(--radius-lg);
            color: var(--danger);
            font-size: 14px;
            font-weight: 600;
            animation: timerPulse 2s ease-in-out infinite;
        }
        @keyframes timerPulse { 0%, 100% { box-shadow: 0 0 0 0 rgba(239,68,68,0); } 50% { box-shadow: 0 0 0 4px rgba(239,68,68,0.08); } }
        .timer-section .timer-icon {
            font-size: 20px;
            animation: iconPulse 1.5s ease-in-out infinite;
        }
        @keyframes iconPulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.15); } }
        .timer-section .countdown {
            font-weight: 800;
            font-size: 22px;
            font-family: 'SF Mono', 'Consolas', monospace;
            letter-spacing: 1px;
        }
        .method-list {
            display: flex;
            flex-direction: column;
            gap: 14px;
        }
        .method-card {
            display: flex;
            align-items: center;
            padding: 22px 26px;
            border-radius: var(--radius-lg);
            color: #fff;
            text-decoration: none;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            position: relative;
            overflow: hidden;
            animation: methodSlideIn 0.5s ease-out both;
        }
        .method-card:nth-child(1) { animation-delay: 0.1s; }
        .method-card:nth-child(2) { animation-delay: 0.2s; }
        .method-card:nth-child(3) { animation-delay: 0.3s; }
        @keyframes methodSlideIn { from { opacity: 0; transform: translateX(-20px); } to { opacity: 1; transform: translateX(0); } }
        .method-card::after {
            content: '';
            position: absolute;
            top: -50%; right: -30%;
            width: 200px; height: 200px;
            background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
            border-radius: 50%;
            transition: all 0.4s;
        }
        .method-card:hover::after { top: -30%; right: -10%; }
        .method-card:hover {
            transform: translateY(-4px) scale(1.01);
            box-shadow: 0 12px 35px rgba(0,0,0,0.2);
            color: #fff;
            text-decoration: none;
        }
        .method-card.wechat {
            background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
        }
        .method-card.alipay {
            background: linear-gradient(135deg, #1677ff 0%, #096dd9 100%);
        }
        .method-card.bankcard {
            background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
        }
        .method-card .method-icon {
            font-size: 44px;
            margin-right: 18px;
            flex-shrink: 0;
            filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
            transition: transform 0.3s;
        }
        .method-card:hover .method-icon { transform: scale(1.1); }
        .method-card .method-info {
            flex: 1;
        }
        .method-card .method-info h3 {
            margin: 0;
            font-size: 18px;
            font-weight: 700;
            color: #fff;
        }
        .method-card .method-info p {
            margin: 4px 0 0;
            opacity: 0.9;
            font-size: 13px;
        }
        .method-card .method-arrow {
            font-size: 24px;
            opacity: 0.7;
            transition: all 0.3s;
        }
        .method-card:hover .method-arrow { opacity: 1; transform: translateX(4px); }
        .method-card .method-badge {
            position: absolute;
            top: 12px;
            right: 12px;
            background: rgba(255,255,255,0.25);
            backdrop-filter: blur(4px);
            padding: 3px 12px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
        }
        .security-tip {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-top: 28px;
            padding: 16px 20px;
            background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
            border: 1px solid #bbf7d0;
            border-radius: var(--radius-lg);
            color: #166534;
            font-size: 13px;
            font-weight: 500;
        }
        .security-tip .secure-icon {
            font-size: 22px;
        }
        .back-link {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-top: 20px;
            padding: 14px 28px;
            color: var(--gray-500);
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            border-radius: 50px;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            background: rgba(255,255,255,0.5);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255,255,255,0.5);
            position: relative;
            overflow: hidden;
        }
        .back-link::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent); transition: left 0.5s; }
        .back-link:hover {
            color: var(--primary);
            background: rgba(255,255,255,0.8);
            text-decoration: none;
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.1);
        }
        .back-link:hover::before { left: 100%; }
        @media (max-width: 768px) {
            .payment-card {
                padding: 25px 20px;
            }
            .order-amount-box {
                flex-direction: column;
                gap: 12px;
                text-align: center;
            }
            .order-amount-box .amount-label,
            .order-amount-box .amount-value {
                text-align: center;
            }
        }
    </style>
</head>
<body>
<div class="payment-container">
    <div class="payment-card">
        <div class="payment-header">
            <span class="payment-icon">💳</span>
            <h2>选择支付方式</h2>
            <p>请选择您偏好的支付方式完成订单</p>
        </div>

        <div class="order-amount-box">
            <div>
                <div class="order-info-label">订单编号</div>
                <div class="order-id">${orderId}</div>
                <c:if test="${not empty order.couponname}">
                    <div style="margin-top:6px;font-size:12px;color:#92400e;background:#fef3c7;display:inline-block;padding:2px 8px;border-radius:10px;">🎫 ${order.couponname}</div>
                </c:if>
            </div>
            <div style="text-align: right;">
                <c:if test="${not empty order.originalprice && not empty order.discountamount}">
                    <div style="font-size:13px;color:var(--gray-400);text-decoration:line-through;">原价 ¥<fmt:formatNumber value="${order.originalprice}" pattern="#0.00"/></div>
                    <div style="font-size:13px;color:#10b981;font-weight:600;">优惠 -¥<fmt:formatNumber value="${order.discountamount}" pattern="#0.00"/></div>
                </c:if>
                <div class="amount-label">应付金额</div>
                <div class="amount-value">${orderAmount}</div>
            </div>
        </div>

        <div class="timer-section">
            <span class="timer-icon">⏳</span>
            请在 <span class="countdown" id="timerDisplay">15:00</span> 内完成支付，超时订单将自动取消
        </div>

        <div class="method-list">
            <a href="${pageContext.request.contextPath}/payment/wechat?orderId=${orderId}&userId=${userId}" class="method-card wechat glow-hover">
                <span class="method-icon">💚</span>
                <div class="method-info">
                    <h3>微信支付</h3>
                    <p>推荐使用微信扫码支付，安全便捷</p>
                </div>
                <span class="method-badge">推荐</span>
                <span class="method-arrow">→</span>
            </a>
            <a href="${pageContext.request.contextPath}/payment/alipay?orderId=${orderId}&userId=${userId}" class="method-card alipay glow-hover">
                <span class="method-icon">💙</span>
                <div class="method-info">
                    <h3>支付宝</h3>
                    <p>使用支付宝扫码支付，支持花呗分期</p>
                </div>
                <span class="method-arrow">→</span>
            </a>
            <a href="${pageContext.request.contextPath}/payment/card?orderId=${orderId}&userId=${userId}" class="method-card bankcard glow-hover">
                <span class="method-icon">💳</span>
                <div class="method-info">
                    <h3>银行卡支付</h3>
                    <p>支持各大银行借记卡和信用卡</p>
                </div>
                <span class="method-arrow">→</span>
            </a>
        </div>

        <div class="security-tip">
            <span class="secure-icon">🔒</span>
            支付信息经过加密处理，我们不会存储您的银行卡信息。请放心支付。
        </div>
    </div>
    <a href="${pageContext.request.contextPath}/" class="back-link">← 返回首页</a>
</div>
<script>
    var totalSeconds = 15 * 60;
    var timerInterval = setInterval(function() {
        totalSeconds--;
        if (totalSeconds <= 0) {
            clearInterval(timerInterval);
            alert('支付超时，订单已自动取消。');
            window.location.href = '${pageContext.request.contextPath}/paymentCallback?orderId=${orderId}&status=timeout&paymentMethod=通用&userId=${userId}';
            return;
        }
        var minutes = Math.floor(totalSeconds / 60);
        var seconds = totalSeconds % 60;
        document.getElementById('timerDisplay').textContent =
            String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
    }, 1000);
</script>
</body>
</html>
