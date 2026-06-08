<%-- ============================================================
     payment/fail.jsp — 支付失败页面
     功能：支付失败时展示给用户的提示页面，说明失败原因和后续操作。
     说明：支付过程中出现错误时跳转到此页面。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>支付失败 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <style>
        body {
            background: linear-gradient(135deg, #fef2f2 0%, #fff5f5 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .fail-container {
            max-width: 500px;
            width: 100%;
            margin: 40px 20px;
        }
        .fail-card {
            background: #fff;
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-xl);
            padding: 50px 30px;
            text-align: center;
        }
        .fail-icon-wrapper {
            margin-bottom: 25px;
        }
        .fail-circle {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto;
            animation: shakeIn 0.6s ease;
            box-shadow: 0 8px 30px rgba(239,68,68,0.3);
        }
        @keyframes shakeIn {
            0% { transform: scale(0) rotate(-10deg); opacity: 0; }
            50% { transform: scale(1.1) rotate(3deg); }
            70% { transform: scale(0.95) rotate(-2deg); }
            100% { transform: scale(1) rotate(0); opacity: 1; }
        }
        .fail-svg {
            width: 45px;
            height: 45px;
        }
        .fail-svg line {
            stroke: #fff;
            stroke-width: 3;
            stroke-linecap: round;
            animation: drawX 0.3s ease 0.3s both;
        }
        .fail-svg line.line1 {
            stroke-dasharray: 40;
            stroke-dashoffset: 40;
            animation: drawX 0.3s ease 0.3s both;
        }
        .fail-svg line.line2 {
            stroke-dasharray: 40;
            stroke-dashoffset: 40;
            animation: drawX 0.3s ease 0.5s both;
        }
        @keyframes drawX {
            100% { stroke-dashoffset: 0; }
        }
        .fail-card h2 {
            font-size: 26px;
            font-weight: 800;
            color: var(--gray-800);
            margin-bottom: 8px;
        }
        .fail-card .fail-msg {
            color: var(--gray-500);
            font-size: 15px;
            margin-bottom: 12px;
        }
        .fail-card .fail-reason {
            display: inline-block;
            background: #fef2f2;
            color: var(--danger);
            padding: 6px 16px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 30px;
        }
        .fail-actions {
            display: flex;
            flex-direction: column;
            gap: 12px;
            margin-bottom: 20px;
        }
        .btn-retry {
            width: 100%;
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
        .btn-retry:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(79,70,229,0.4);
            color: #fff;
            text-decoration: none;
        }
        .btn-home {
            width: 100%;
            padding: 14px;
            border-radius: var(--radius);
            border: 2px solid var(--gray-200);
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
        .btn-home:hover {
            border-color: var(--gray-400);
            color: var(--gray-800);
            text-decoration: none;
        }
        .fail-tip {
            font-size: 13px;
            color: var(--gray-400);
        }
    </style>
</head>
<body>
<div class="fail-container">
    <div class="fail-card">
        <div class="fail-icon-wrapper">
            <div class="fail-circle">
                <svg class="fail-svg" viewBox="0 0 24 24">
                    <line x1="6" y1="6" x2="18" y2="18" class="line1"/>
                    <line x1="18" y1="6" x2="6" y2="18" class="line2"/>
                </svg>
            </div>
        </div>
        <h2>支付失败</h2>
        <p class="fail-msg">很抱歉，您的支付未能完成</p>
        <span class="fail-reason">订单已取消</span>

        <div class="fail-actions">
            <a href="${pageContext.request.contextPath}/payment?orderId=${orderId}" class="btn-retry">🔄 重新支付</a>
            <a href="${pageContext.request.contextPath}/" class="btn-home">🏠 返回首页</a>
        </div>

        <p class="fail-tip">如需帮助，请联系客服 support@bookverse.com</p>
    </div>
</div>
</body>
</html>
