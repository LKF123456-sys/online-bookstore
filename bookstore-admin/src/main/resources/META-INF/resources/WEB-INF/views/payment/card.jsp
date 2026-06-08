<%-- ============================================================
     payment/card.jsp — 银行卡支付页面
     功能：展示银行卡支付表单，引导用户输入银行卡信息完成付款。
     说明：包含银行卡号、有效期、CVV等字段的表单。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>银行卡支付 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <style>
        body {
            background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
            min-height: 100vh;
        }
        .card-pay-container {
            max-width: 500px;
            margin: 60px auto;
            padding: 0 20px;
        }
        .card-pay-card {
            background: #fff;
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-lg);
            padding: 40px 30px;
            border-top: 5px solid #f97316;
        }
        .card-brand {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            margin-bottom: 25px;
        }
        .card-brand-icon {
            width: 60px;
            height: 60px;
            background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 30px;
            box-shadow: 0 4px 15px rgba(249,115,22,0.3);
        }
        .card-brand h2 {
            font-size: 22px;
            font-weight: 800;
            color: var(--gray-800);
            margin: 0;
        }
        .card-pay-card .order-info {
            color: var(--gray-500);
            font-size: 14px;
            text-align: center;
            margin-bottom: 25px;
        }
        .order-detail-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #fff7ed;
            border: 1px solid #fed7aa;
            border-radius: var(--radius);
            padding: 14px 18px;
            margin-bottom: 28px;
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
        .card-visual {
            background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
            border-radius: var(--radius-lg);
            padding: 20px 24px;
            margin-bottom: 28px;
            color: #fff;
            position: relative;
            overflow: hidden;
            min-height: 180px;
        }
        .card-visual::before {
            content: '';
            position: absolute;
            top: -30px;
            right: -30px;
            width: 150px;
            height: 150px;
            border-radius: 50%;
            background: rgba(255,255,255,0.08);
        }
        .card-visual::after {
            content: '';
            position: absolute;
            bottom: -40px;
            left: -20px;
            width: 180px;
            height: 180px;
            border-radius: 50%;
            background: rgba(255,255,255,0.05);
        }
        .card-visual .card-chip {
            width: 40px;
            height: 30px;
            background: rgba(255,255,255,0.25);
            border-radius: 5px;
            margin-bottom: 20px;
            position: relative;
            z-index: 1;
        }
        .card-visual .card-number-preview {
            font-size: 20px;
            font-weight: 700;
            letter-spacing: 4px;
            margin-bottom: 15px;
            position: relative;
            z-index: 1;
        }
        .card-visual .card-footer-visual {
            display: flex;
            justify-content: space-between;
            font-size: 12px;
            opacity: 0.85;
            position: relative;
            z-index: 1;
        }
        .form-group {
            margin-bottom: 18px;
        }
        .form-group label {
            font-weight: 600;
            color: var(--gray-700);
            margin-bottom: 6px;
            font-size: 14px;
            display: block;
        }
        .form-control {
            border: 2px solid #e5e7eb;
            border-radius: 10px;
            padding: 12px 16px;
            font-size: 15px;
            transition: all 0.3s;
            width: 100%;
        }
        .form-control:focus {
            border-color: #f97316;
            box-shadow: 0 0 0 4px rgba(249,115,22,0.1);
            outline: none;
        }
        .card-row {
            display: flex;
            gap: 12px;
        }
        .card-row .form-group {
            flex: 1;
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
            background: linear-gradient(90deg, #f97316, #ea580c);
            border-radius: 2px;
            transition: width 1s linear;
            width: 100%;
        }
        .timer-text {
            font-size: 13px;
            color: var(--gray-500);
            margin-bottom: 22px;
            text-align: center;
        }
        .timer-text .countdown {
            color: #f97316;
            font-weight: 700;
        }
        .btn-row {
            display: flex;
            gap: 12px;
        }
        .btn-pay-success {
            flex: 1;
            padding: 14px;
            border-radius: var(--radius);
            border: none;
            cursor: pointer;
            font-weight: 700;
            font-size: 15px;
            background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
            color: #fff;
            transition: all 0.3s;
        }
        .btn-pay-success:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(249,115,22,0.4);
        }
        .btn-pay-fail {
            flex: 1;
            padding: 14px;
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
            text-decoration: none;
        }
        .secure-tip {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            color: var(--gray-400);
            margin-top: 16px;
            text-align: center;
            justify-content: center;
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
        @media (max-width: 500px) {
            .card-visual .card-number-preview {
                font-size: 16px;
                letter-spacing: 2px;
            }
            .card-row {
                flex-direction: column;
                gap: 0;
            }
        }
    </style>
</head>
<body>
<div class="card-pay-container">
    <div class="card-pay-card">
        <div class="card-brand">
            <div class="card-brand-icon">💳</div>
            <h2>银行卡支付</h2>
        </div>
        <p class="order-info">请填写银行卡信息完成支付</p>

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

        <div class="card-visual">
            <div class="card-chip"></div>
            <div class="card-number-preview" id="cardPreview">•••• •••• •••• ••••</div>
            <div class="card-footer-visual">
                <span id="cardHolderPreview">持卡人姓名</span>
                <span id="cardExpiryPreview">MM/YY</span>
            </div>
        </div>

        <div class="form-group">
            <label>卡号</label>
            <input type="text" class="form-control" id="cardNumber" placeholder="请输入银行卡号" maxlength="19" oninput="formatCardNumber(this); updateCardPreview();">
        </div>
        <div class="form-group">
            <label>持卡人姓名</label>
            <input type="text" class="form-control" id="cardHolder" placeholder="请输入持卡人姓名" oninput="updateCardPreview();">
        </div>
        <div class="card-row">
            <div class="form-group">
                <label>有效期 (MM/YY)</label>
                <input type="text" class="form-control" id="cardExpiry" placeholder="MM/YY" maxlength="5" oninput="formatExpiry(this); updateCardPreview();">
            </div>
            <div class="form-group">
                <label>CVV</label>
                <input type="password" class="form-control" id="cardCvv" placeholder="CVV" maxlength="4">
            </div>
        </div>

        <div class="timer-bar">
            <div class="timer-bar-fill" id="timerBar"></div>
        </div>
        <div class="timer-text">
            剩余支付时间 <span class="countdown" id="countdown">05:00</span>
        </div>

        <div class="btn-row">
            <a href="${pageContext.request.contextPath}/paymentCallback?orderId=${orderId}&status=success&paymentMethod=银行卡支付" class="btn-pay-success">确认支付</a>
            <a href="${pageContext.request.contextPath}/paymentCallback?orderId=${orderId}&status=fail&paymentMethod=银行卡支付" class="btn-pay-fail">取消支付</a>
        </div>

        <div class="secure-tip">
            🔒 您的银行卡信息经过加密传输，安全可靠
        </div>
    </div>
    <a href="${pageContext.request.contextPath}/payment?orderId=${orderId}" class="back-link">← 返回选择支付方式</a>
</div>
<script>
    function formatCardNumber(input) {
        var value = input.value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
        var parts = [];
        for (var i = 0; i < value.length; i += 4) {
            parts.push(value.substring(i, i + 4));
        }
        input.value = parts.join(' ');
    }

    function formatExpiry(input) {
        var value = input.value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
        if (value.length > 2) {
            value = value.substring(0, 2) + '/' + value.substring(2, 4);
        }
        input.value = value;
    }

    function updateCardPreview() {
        var cardNum = document.getElementById('cardNumber').value.replace(/\s+/g, '');
        var preview = '';
        if (cardNum.length > 0) {
            for (var i = 0; i < cardNum.length && i < 16; i += 4) {
                preview += cardNum.substring(i, Math.min(i + 4, cardNum.length));
                if (i + 4 < cardNum.length && i + 4 < 16) preview += ' ';
            }
            var remaining = 19 - preview.length;
            for (var j = 0; j < remaining; j++) {
                preview += '•';
            }
        } else {
            preview = '•••• •••• •••• ••••';
        }
        document.getElementById('cardPreview').textContent = preview;

        var holder = document.getElementById('cardHolder').value;
        document.getElementById('cardHolderPreview').textContent = holder || '持卡人姓名';

        var expiry = document.getElementById('cardExpiry').value;
        document.getElementById('cardExpiryPreview').textContent = expiry || 'MM/YY';
    }

    var totalSeconds = 5 * 60;
    var initialSeconds = totalSeconds;
    var countdownInterval = setInterval(function() {
        totalSeconds--;
        if (totalSeconds <= 0) {
            clearInterval(countdownInterval);
            window.location.href = '${pageContext.request.contextPath}/paymentCallback?orderId=${orderId}&status=timeout&paymentMethod=银行卡支付';
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
