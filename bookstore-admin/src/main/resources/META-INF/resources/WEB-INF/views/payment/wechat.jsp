<%-- ============================================================
     payment/wechat.jsp — 微信支付页面
     功能：展示微信支付界面，引导用户完成微信付款。
     说明：生成微信支付二维码供用户扫码支付。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>微信支付 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <style>
        body {
            background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
            min-height: 100vh;
        }
        .wechat-pay-container {
            max-width: 460px;
            margin: 60px auto;
            padding: 0 20px;
        }
        .wechat-pay-card {
            background: #fff;
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-lg);
            padding: 40px 30px;
            text-align: center;
            border-top: 5px solid #07c160;
        }
        .wechat-brand {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 72px;
            height: 72px;
            background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
            border-radius: 50%;
            margin-bottom: 15px;
            box-shadow: 0 4px 15px rgba(7,193,96,0.3);
        }
        .wechat-brand .brand-icon {
            font-size: 38px;
            color: #fff;
        }
        .wechat-pay-card h2 {
            font-size: 22px;
            font-weight: 800;
            color: var(--gray-800);
            margin-bottom: 6px;
        }
        .wechat-pay-card .order-info {
            color: var(--gray-500);
            font-size: 14px;
            margin-bottom: 25px;
        }
        .order-detail-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
            border-radius: var(--radius);
            padding: 14px 18px;
            margin-bottom: 25px;
        }
        .order-detail-row .detail-label {
            color: var(--gray-500);
            font-size: 13px;
        }
        .order-detail-row .detail-value {
            font-weight: 700;
            color: var(--gray-800);
        }
        .order-detail-row .detail-value.amount {
            color: var(--danger);
            font-size: 22px;
        }
        .order-detail-row .detail-value.amount::before {
            content: '¥';
            font-size: 14px;
        }
        .qr-placeholder {
            width: 200px;
            height: 200px;
            margin: 0 auto 20px;
            background: #e5e7eb;
            border-radius: var(--radius-lg);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: var(--gray-400);
            font-size: 14px;
            position: relative;
            overflow: hidden;
        }
        .qr-placeholder::before,
        .qr-placeholder::after {
            content: '';
            position: absolute;
            background: #d1d5db;
        }
        .qr-placeholder::before {
            width: 60px;
            height: 60px;
            top: 20px;
            left: 20px;
            border-radius: 4px;
        }
        .qr-placeholder::after {
            width: 60px;
            height: 60px;
            top: 20px;
            right: 20px;
            border-radius: 4px;
        }
        .qr-inner-square {
            position: absolute;
            width: 60px;
            height: 60px;
            bottom: 20px;
            left: 20px;
            background: #d1d5db;
            border-radius: 4px;
        }
        .qr-inner-square2 {
            position: absolute;
            width: 60px;
            height: 60px;
            bottom: 20px;
            right: 20px;
            background: #d1d5db;
            border-radius: 4px;
        }
        .qr-icon-center {
            position: absolute;
            width: 30px;
            height: 30px;
            background: #9ca3af;
            border-radius: 4px;
            z-index: 1;
        }
        .qr-label {
            position: relative;
            z-index: 2;
            font-weight: 600;
            color: var(--gray-500);
            margin-top: 30px;
        }
        .qr-sub-label {
            font-size: 12px;
            color: var(--gray-400);
            margin-top: 4px;
        }
        .instruction-box {
            background: #fffbeb;
            border: 1px solid #fef3c7;
            border-radius: var(--radius);
            padding: 16px;
            margin-bottom: 25px;
            text-align: left;
            color: #92400e;
            font-size: 13px;
            line-height: 1.8;
        }
        .instruction-box p {
            margin: 0;
        }
        .instruction-box p::before {
            content: '• ';
            margin-right: 4px;
        }
        .timer-bar {
            height: 4px;
            background: #e5e7eb;
            border-radius: 2px;
            margin-bottom: 8px;
            overflow: hidden;
        }
        .timer-bar-fill {
            height: 100%;
            background: linear-gradient(90deg, #07c160, #10b981);
            border-radius: 2px;
            transition: width 1s linear;
            width: 100%;
        }
        .timer-text {
            font-size: 13px;
            color: var(--gray-500);
            margin-bottom: 20px;
        }
        .timer-text .countdown {
            color: #07c160;
            font-weight: 700;
        }
        .btn-row {
            display: flex;
            gap: 12px;
            margin-top: 20px;
        }
        .btn-pay-success {
            flex: 1;
            padding: 13px;
            border-radius: var(--radius);
            border: none;
            cursor: pointer;
            font-weight: 700;
            font-size: 15px;
            background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
            color: #fff;
            transition: all 0.3s;
        }
        .btn-pay-success:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(16,185,129,0.4);
        }
        .btn-pay-fail {
            flex: 1;
            padding: 13px;
            border-radius: var(--radius);
            border: 2px solid #e5e7eb;
            cursor: pointer;
            font-weight: 600;
            font-size: 15px;
            background: #fff;
            color: var(--gray-600);
            transition: all 0.3s;
            text-decoration: none;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .btn-pay-fail:hover {
            border-color: #ef4444;
            color: var(--danger);
            background: #fef2f2;
        }
        .back-link {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: var(--gray-400);
            font-size: 14px;
            text-decoration: none;
        }
        .back-link:hover {
            color: var(--gray-600);
        }
    </style>
</head>
<body>
<div class="wechat-pay-container">
    <div class="wechat-pay-card">
        <div class="wechat-brand">
            <span class="brand-icon">💚</span>
        </div>
        <h2>微信支付</h2>
        <p class="order-info">请使用微信扫描二维码完成支付</p>

        <div class="order-detail-row">
            <div>
                <div class="detail-label">订单号</div>
                <div class="detail-value">${orderId}</div>
            </div>
            <div style="text-align: right;">
                <div class="detail-label">支付金额</div>
                <div class="detail-value amount">${orderAmount}</div>
            </div>
        </div>

        <div class="qr-placeholder">
            <div class="qr-icon-center"></div>
            <div class="qr-inner-square"></div>
            <div class="qr-inner-square2"></div>
            <span class="qr-label">微信扫码支付</span>
            <span class="qr-sub-label">二维码占位</span>
        </div>

        <div class="timer-bar">
            <div class="timer-bar-fill" id="timerBar"></div>
        </div>
        <div class="timer-text">
            剩余支付时间 <span class="countdown" id="countdown">05:00</span>
        </div>

        <div class="instruction-box">
            <p>打开手机微信，点击「扫一扫」</p>
            <p>扫描屏幕上的二维码</p>
            <p>确认支付金额，输入密码完成支付</p>
        </div>

        <div class="btn-row">
            <a href="${pageContext.request.contextPath}/paymentCallback?orderId=${orderId}&status=success&paymentMethod=微信支付&userId=${param.userId}" class="btn-pay-success">模拟支付成功</a>
            <a href="${pageContext.request.contextPath}/paymentCallback?orderId=${orderId}&status=fail&paymentMethod=微信支付&userId=${param.userId}" class="btn-pay-fail">支付失败</a>
        </div>
    </div>
    <a href="${pageContext.request.contextPath}/payment?orderId=${orderId}" class="back-link">← 返回选择支付方式</a>
</div>
<script>
    var totalSeconds = 5 * 60;
    var initialSeconds = totalSeconds;
    var countdownInterval = setInterval(function() {
        totalSeconds--;
        if (totalSeconds <= 0) {
            clearInterval(countdownInterval);
            window.location.href = '${pageContext.request.contextPath}/paymentCallback?orderId=${orderId}&status=timeout&paymentMethod=微信支付&userId=${param.userId}';
            return;
        }
        var minutes = Math.floor(totalSeconds / 60);
        var seconds = totalSeconds % 60;
        document.getElementById('countdown').textContent =
            String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
        var pct = (totalSeconds / initialSeconds) * 100;
        document.getElementById('timerBar').style.width = pct + '%';
        if (pct < 20) {
            document.getElementById('timerBar').style.background = 'linear-gradient(90deg, #ef4444, #dc2626)';
            document.getElementById('countdown').style.color = '#ef4444';
        }
    }, 1000);
</script>
</body>
</html>
