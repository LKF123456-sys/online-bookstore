<%-- 注册页面 register.jsp --%>
<%-- 功能：用户注册表单页面，包含账户信息、联系方式、个人信息三个表单区域 --%>
<%-- 设计风格：与login.jsp一致的赛博朋克风格 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 引入JSTL核心标签库 --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户注册 - BookVerse</title>
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/favicon.svg">
    <%-- 引入Bootstrap框架CSS --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css"/>
    <%-- 引入自定义样式表 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css"/>
    <%-- 引入Font Awesome图标库 --%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <style>
        <%-- CSS变量定义 - 统一颜色和主题管理 --%>
        :root {
            --cyber-bg: #0a0e1a;  /* 主背景色 */
            --neon-blue: #00d4ff;  /* 霓虹蓝 */
            --neon-purple: #a78bfa;  /* 霓虹紫 */
            --primary: #6366f1;  /* 主色调 */
            --cyber-card: rgba(15, 20, 40, 0.85);  /* 卡片背景 */
            --cyber-border: rgba(0, 212, 255, 0.15);  /* 边框色 */
            --cyber-glow: rgba(0, 212, 255, 0.4);  /* 光晕色 */
            --text-main: #e2e8f0;  /* 主文字色 */
            --text-dim: #64748b;  /* 次要文字色 */
            --input-bg: rgba(10, 14, 30, 0.9);  /* 输入框背景 */
            --input-border: rgba(100, 116, 139, 0.25);  /* 输入框边框 */
        }

        <%-- 全局重置样式 --%>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
            background: var(--cyber-bg);
            min-height: 100vh;
            overflow-x: hidden;
            color: var(--text-main);
        }

        <%-- 注册页面整体布局 - 左右两栏 --%>
        .register-page {
            display: flex;
            min-height: 100vh;
            animation: pageFadeIn 0.8s ease-out;  /* 页面淡入动画 */
        }

        @keyframes pageFadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        <%-- 左侧面板 - 品牌展示区域 --%>
        /* ===== LEFT PANEL ===== */
        .register-left {
            flex: 0 0 440px;  /* 固定宽度 */
            background: linear-gradient(160deg, #0a0e1a 0%, #0d1333 40%, #15104a 70%, #1a1050 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            overflow: hidden;
            padding: 60px 40px;
        }

        .register-left::before {
            content: '';
            position: absolute;
            top: -200px;
            right: -200px;
            width: 500px;
            height: 500px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(0, 212, 255, 0.08) 0%, transparent 70%);
            animation: floatOrb 8s ease-in-out infinite;
        }

        .register-left::after {
            content: '';
            position: absolute;
            bottom: -150px;
            left: -100px;
            width: 400px;
            height: 400px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(167, 139, 250, 0.07) 0%, transparent 70%);
            animation: floatOrb 10s ease-in-out infinite reverse;
        }

        @keyframes floatOrb {
            0%, 100% { transform: translate(0, 0) scale(1); }
            50% { transform: translate(30px, -30px) scale(1.1); }
        }

        /* Grid overlay */
        .register-left .grid-overlay {
            position: absolute;
            inset: 0;
            background-image:
                linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
                linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
            background-size: 40px 40px;
            z-index: 0;
        }

        .particles-container {
            position: absolute;
            inset: 0;
            overflow: hidden;
            z-index: 0;
        }

        .particle {
            position: absolute;
            border-radius: 50%;
            animation: particleFloat linear infinite;
        }

        .particle:nth-child(1) { left: 8%; top: 15%; width: 3px; height: 3px; background: rgba(0, 212, 255, 0.5); animation-duration: 12s; box-shadow: 0 0 6px rgba(0, 212, 255, 0.4); }
        .particle:nth-child(2) { left: 22%; top: 55%; width: 5px; height: 5px; background: rgba(167, 139, 250, 0.45); animation-duration: 15s; animation-delay: -2s; box-shadow: 0 0 8px rgba(167, 139, 250, 0.3); }
        .particle:nth-child(3) { left: 40%; top: 25%; width: 3px; height: 3px; background: rgba(0, 212, 255, 0.6); animation-duration: 10s; animation-delay: -4s; box-shadow: 0 0 6px rgba(0, 212, 255, 0.5); }
        .particle:nth-child(4) { left: 60%; top: 65%; width: 4px; height: 4px; background: rgba(99, 102, 241, 0.5); animation-duration: 18s; animation-delay: -1s; box-shadow: 0 0 8px rgba(99, 102, 241, 0.3); }
        .particle:nth-child(5) { left: 78%; top: 35%; width: 4px; height: 4px; background: rgba(0, 212, 255, 0.45); animation-duration: 14s; animation-delay: -3s; box-shadow: 0 0 6px rgba(0, 212, 255, 0.35); }
        .particle:nth-child(6) { left: 12%; top: 75%; width: 5px; height: 5px; background: rgba(167, 139, 250, 0.4); animation-duration: 16s; animation-delay: -5s; box-shadow: 0 0 10px rgba(167, 139, 250, 0.3); }
        .particle:nth-child(7) { left: 52%; top: 10%; width: 3px; height: 3px; background: rgba(0, 212, 255, 0.55); animation-duration: 11s; animation-delay: -2.5s; box-shadow: 0 0 6px rgba(0, 212, 255, 0.4); }
        .particle:nth-child(8) { left: 70%; top: 50%; width: 4px; height: 4px; background: rgba(99, 102, 241, 0.45); animation-duration: 13s; animation-delay: -6s; box-shadow: 0 0 8px rgba(99, 102, 241, 0.3); }
        .particle:nth-child(9) { left: 30%; top: 80%; width: 5px; height: 5px; background: rgba(0, 212, 255, 0.35); animation-duration: 17s; animation-delay: -1.5s; box-shadow: 0 0 10px rgba(0, 212, 255, 0.25); }
        .particle:nth-child(10) { left: 88%; top: 20%; width: 3px; height: 3px; background: rgba(167, 139, 250, 0.5); animation-duration: 9s; animation-delay: -4.5s; box-shadow: 0 0 6px rgba(167, 139, 250, 0.4); }

        @keyframes particleFloat {
            0% { transform: translateY(0) translateX(0) rotate(0deg); opacity: 0; }
            10% { opacity: 1; }
            90% { opacity: 1; }
            100% { transform: translateY(-100vh) translateX(50px) rotate(360deg); opacity: 0; }
        }

        .geo-shape {
            position: absolute;
            border: 1px solid;
            z-index: 0;
            animation: geoRotate 20s linear infinite;
        }

        .geo-shape:nth-child(15) {
            width: 70px; height: 70px; top: 10%; left: 15%;
            border-color: rgba(0, 212, 255, 0.12);
            border-radius: 4px;
            animation-duration: 25s;
            box-shadow: inset 0 0 15px rgba(0, 212, 255, 0.05);
        }
        .geo-shape:nth-child(16) {
            width: 45px; height: 45px; top: 70%; right: 12%;
            border-color: rgba(167, 139, 250, 0.12);
            border-radius: 50%;
            animation-duration: 18s;
            animation-direction: reverse;
            box-shadow: inset 0 0 10px rgba(167, 139, 250, 0.05);
        }
        .geo-shape:nth-child(17) {
            width: 100px; height: 100px; bottom: 10%; left: 50%;
            border-color: rgba(0, 212, 255, 0.08);
            border-radius: 6px;
            animation-duration: 30s;
            box-shadow: inset 0 0 20px rgba(0, 212, 255, 0.03);
        }
        .geo-shape:nth-child(18) {
            width: 55px; height: 55px; top: 40%; left: 76%;
            border-color: rgba(167, 139, 250, 0.1);
            border-radius: 50%;
            animation-duration: 22s;
            animation-direction: reverse;
            box-shadow: inset 0 0 12px rgba(167, 139, 250, 0.04);
        }

        @keyframes geoRotate {
            0% { transform: rotate(0deg) scale(1); }
            50% { transform: rotate(180deg) scale(1.1); }
            100% { transform: rotate(360deg) scale(1); }
        }

        .register-left-content {
            position: relative;
            z-index: 2;
            text-align: center;
            color: #fff;
            max-width: 340px;
        }

        .brand-icon {
            width: 76px;
            height: 76px;
            margin: 0 auto 22px;
            background: linear-gradient(135deg, var(--neon-blue), var(--primary));
            border-radius: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 34px;
            box-shadow: 0 0 30px rgba(0, 212, 255, 0.3), 0 0 60px rgba(0, 212, 255, 0.15);
            animation: iconPulse 3s ease-in-out infinite;
            position: relative;
        }

        .brand-icon::after {
            content: '';
            position: absolute;
            inset: -3px;
            border-radius: 20px;
            border: 1px solid rgba(0, 212, 255, 0.3);
            animation: borderPulse 2s ease-in-out infinite;
        }

        @keyframes iconPulse {
            0%, 100% { box-shadow: 0 0 30px rgba(0, 212, 255, 0.3), 0 0 60px rgba(0, 212, 255, 0.15); }
            50% { box-shadow: 0 0 40px rgba(0, 212, 255, 0.5), 0 0 80px rgba(0, 212, 255, 0.25); }
        }

        @keyframes borderPulse {
            0%, 100% { opacity: 0.5; }
            50% { opacity: 1; }
        }

        .register-left-content h1 {
            font-size: 34px;
            font-weight: 900;
            margin-bottom: 10px;
            letter-spacing: -0.5px;
        }

        .register-left-content h1 span {
            background: linear-gradient(135deg, var(--neon-blue), var(--neon-purple));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            filter: drop-shadow(0 0 8px rgba(0, 212, 255, 0.3));
        }

        .register-left-content .slogan {
            font-size: 15px;
            opacity: 0.5;
            line-height: 1.8;
            font-weight: 300;
            letter-spacing: 1px;
        }

        .terminal-line {
            margin-top: 8px;
            font-family: 'Courier New', monospace;
            font-size: 11px;
            color: var(--neon-blue);
            opacity: 0.5;
            letter-spacing: 2px;
        }

        .register-left-stats {
            margin-top: 40px;
            display: flex;
            flex-direction: column;
            gap: 14px;
        }

        .stat-item {
            display: flex;
            align-items: center;
            gap: 14px;
            padding: 12px 18px;
            background: rgba(0, 212, 255, 0.03);
            border: 1px solid rgba(0, 212, 255, 0.08);
            border-radius: 12px;
            backdrop-filter: blur(10px);
            transition: all 0.3s;
            animation: statSlideIn 0.6s ease-out backwards;
        }

        .stat-item:nth-child(1) { animation-delay: 0.3s; }
        .stat-item:nth-child(2) { animation-delay: 0.5s; }
        .stat-item:nth-child(3) { animation-delay: 0.7s; }

        .stat-item:hover {
            background: rgba(0, 212, 255, 0.06);
            border-color: rgba(0, 212, 255, 0.2);
            transform: translateX(6px);
            box-shadow: 0 0 20px rgba(0, 212, 255, 0.08);
        }

        @keyframes statSlideIn {
            from { opacity: 0; transform: translateX(-20px); }
            to { opacity: 1; transform: translateX(0); }
        }

        .stat-number {
            font-size: 24px;
            font-weight: 800;
            min-width: 60px;
            text-align: left;
            font-family: 'Courier New', monospace;
        }

        .stat-item:nth-child(1) .stat-number { color: var(--neon-blue); text-shadow: 0 0 10px rgba(0, 212, 255, 0.4); }
        .stat-item:nth-child(2) .stat-number { color: var(--neon-purple); text-shadow: 0 0 10px rgba(167, 139, 250, 0.4); }
        .stat-item:nth-child(3) .stat-number { color: #fbbf24; text-shadow: 0 0 10px rgba(251, 191, 36, 0.4); }

        .stat-label {
            font-size: 13px;
            opacity: 0.5;
            text-align: left;
        }

        /* ===== RIGHT PANEL ===== */
        .register-right {
            flex: 1;
            display: flex;
            align-items: flex-start;
            justify-content: center;
            padding: 40px 48px;
            background: var(--cyber-bg);
            overflow-y: auto;
            position: relative;
        }

        .register-right::before {
            content: '';
            position: absolute;
            top: -100px;
            right: -100px;
            width: 300px;
            height: 300px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(0, 212, 255, 0.04) 0%, transparent 70%);
        }

        .register-card {
            width: 100%;
            max-width: 540px;
            animation: cardSlideIn 0.6s ease-out 0.2s backwards;
            padding: 20px 0;
        }

        @keyframes cardSlideIn {
            from { opacity: 0; transform: translateY(30px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .glass-card {
            background: var(--cyber-card);
            border: 1px solid var(--cyber-border);
            border-radius: 20px;
            padding: 40px 36px;
            backdrop-filter: blur(20px);
            box-shadow: 0 0 40px rgba(0, 212, 255, 0.05), inset 0 1px 0 rgba(255, 255, 255, 0.03);
            position: relative;
            overflow: hidden;
        }

        .glass-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 1px;
            background: linear-gradient(90deg, transparent, var(--neon-blue), transparent);
            opacity: 0.4;
        }

        .register-card-header {
            margin-bottom: 28px;
        }

        .register-card-header h2 {
            font-size: 28px;
            font-weight: 800;
            color: var(--text-main);
            margin-bottom: 8px;
            letter-spacing: -0.5px;
        }

        .register-card-header h2 i {
            color: var(--neon-blue);
            margin-right: 10px;
            font-size: 24px;
            text-shadow: 0 0 12px rgba(0, 212, 255, 0.5);
        }

        .register-card-header p {
            color: var(--text-dim);
            font-size: 14px;
            font-weight: 400;
        }

        /* Cyber alert */
        .alert-danger {
            background: rgba(239, 68, 68, 0.1);
            border: 1px solid rgba(239, 68, 68, 0.3);
            border-radius: 12px;
            padding: 14px 18px;
            color: #f87171;
            font-size: 14px;
            margin-bottom: 24px;
            animation: alertShake 0.5s ease-in-out;
            display: flex;
            align-items: center;
            gap: 10px;
            box-shadow: 0 0 20px rgba(239, 68, 68, 0.08);
        }

        .alert-danger i {
            font-size: 16px;
            flex-shrink: 0;
        }

        @keyframes alertShake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-5px); }
            75% { transform: translateX(5px); }
        }

        <%-- 表单区域样式 - 将表单分为多个区域 --%>
        /* Form sections */
        .form-section {
            margin-bottom: 8px;
        }

        <%-- 表单区域标题样式 --%>
        .form-section-title {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid rgba(0, 212, 255, 0.08);  /* 底部分隔线 */
        }

        <%-- 表单区域图标样式 --%>
        .form-section-icon {
            width: 32px;
            height: 32px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            flex-shrink: 0;  /* 不缩小 */
            color: var(--neon-blue);
        }

        <%-- 不同表单区域的图标背景色（蓝、紫、靛） --%>
        .form-section:nth-child(1) .form-section-icon { background: rgba(0, 212, 255, 0.1); }
        .form-section:nth-child(2) .form-section-icon { background: rgba(167, 139, 250, 0.1); }
        .form-section:nth-child(3) .form-section-icon { background: rgba(99, 102, 241, 0.1); }

        .form-section-title span {
            font-size: 14px;
            font-weight: 700;
            color: var(--text-main);
            letter-spacing: 0.5px;
            text-transform: uppercase;  /* 大写字母 */
        }

        /* Cyber inputs */
        .input-group-custom {
            position: relative;
            margin-bottom: 22px;
        }

        .input-group-custom .input-wrapper {
            position: relative;
        }

        .input-group-custom input {
            width: 100%;
            padding: 20px 16px 6px 44px;
            border: 1px solid var(--input-border);
            background: var(--input-bg);
            border-radius: 10px;
            font-size: 14px;
            color: var(--text-main);
            transition: all 0.3s;
            outline: none;
            height: 52px;
        }

        .input-group-custom input:focus {
            border-color: var(--neon-blue);
            background: rgba(10, 14, 30, 1);
            box-shadow: 0 0 15px rgba(0, 212, 255, 0.15), inset 0 0 15px rgba(0, 212, 255, 0.03);
        }

        .input-group-custom input:-webkit-autofill,
        .input-group-custom input:-webkit-autofill:hover,
        .input-group-custom input:-webkit-autofill:focus {
            -webkit-box-shadow: 0 0 0 1000px #0a0e1a inset;
            -webkit-text-fill-color: var(--text-main);
            transition: background-color 5000s ease-in-out 0s;
        }

        .input-group-custom input:focus + .input-glow {
            opacity: 1;
            transform: scaleX(1);
        }

        .input-glow {
            position: absolute;
            bottom: 0;
            left: 10%;
            right: 10%;
            height: 1px;
            background: linear-gradient(90deg, transparent, var(--neon-blue), transparent);
            opacity: 0;
            transform: scaleX(0);
            transition: all 0.4s ease;
        }

        .input-group-custom .input-icon-left {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-dim);
            font-size: 15px;
            z-index: 2;
            transition: color 0.3s;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .input-group-custom input:focus ~ .input-icon-left {
            color: var(--neon-blue);
            text-shadow: 0 0 8px rgba(0, 212, 255, 0.5);
        }

        .floating-label {
            position: absolute;
            left: 44px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-dim);
            font-size: 13px;
            pointer-events: none;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            transform-origin: left center;
            background: transparent;
            z-index: 1;
        }

        .input-group-custom input:focus ~ .floating-label,
        .input-group-custom input:not(:placeholder-shown) ~ .floating-label {
            top: 6px;
            transform: translateY(0) scale(0.72);
            color: var(--neon-blue);
            font-weight: 600;
        }

        .input-group-custom input:-webkit-autofill ~ .floating-label {
            top: 6px;
            transform: translateY(0) scale(0.72);
            color: var(--neon-blue);
            font-weight: 600;
        }

        .input-row {
            display: flex;
            gap: 16px;
        }

        .input-row .input-group-custom {
            flex: 1;
        }

        .toggle-password {
            position: absolute;
            right: 14px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            color: var(--text-dim);
            cursor: pointer;
            padding: 4px;
            z-index: 2;
            transition: color 0.3s;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 26px;
            height: 26px;
            border-radius: 6px;
        }

        .toggle-password:hover {
            color: var(--neon-blue);
            background: rgba(0, 212, 255, 0.08);
        }

        <%-- 密码强度指示器样式 --%>
        /* Password strength */
        .password-strength {
            margin-top: 8px;
            display: none;  /* 默认隐藏 */
        }

        .password-strength.visible {
            display: block;  /* 显示状态 */
        }

        <%-- 强度条容器 - 4段横条 --%>
        .strength-bar {
            display: flex;
            gap: 4px;  /* 横条间距 */
            margin-bottom: 6px;
        }

        <%-- 单个强度条样式 --%>
        .strength-bar-item {
            flex: 1;  /* 等分宽度 */
            height: 3px;
            border-radius: 2px;
            background: rgba(100, 116, 139, 0.2);  /* 默认灰色 */
            transition: all 0.3s;
        }

        <%-- 强度条激活状态（弱=红、中=黄、强=蓝） --%>
        .strength-bar-item.active-weak { background: #ef4444; box-shadow: 0 0 8px rgba(239, 68, 68, 0.4); }
        .strength-bar-item.active-medium { background: #f59e0b; box-shadow: 0 0 8px rgba(245, 158, 11, 0.4); }
        .strength-bar-item.active-strong { background: var(--neon-blue); box-shadow: 0 0 8px rgba(0, 212, 255, 0.4); }

        <%-- 强度文字提示 --%>
        .strength-text {
            font-size: 12px;
            font-weight: 600;
            font-family: 'Courier New', monospace;
            transition: color 0.3s;
        }

        <%-- 不同强度等级的文字颜色 --%>
        .strength-text.weak { color: #ef4444; }  /* 弱：红色 */
        .strength-text.medium { color: #f59e0b; }  /* 中：黄色 */
        .strength-text.strong { color: var(--neon-blue); text-shadow: 0 0 6px rgba(0, 212, 255, 0.3); }  /* 强：蓝色带光晕 */

        <%-- 用户协议同意行样式 --%>
        /* Agreement */
        .agreement-row {
            margin-bottom: 24px;
            margin-top: 4px;
        }

        .agreement-check {
            display: flex;
            align-items: flex-start;  /* 顶部对齐 */
            gap: 10px;
            cursor: pointer;
            color: var(--text-dim);
            font-size: 13px;
            line-height: 1.6;
            user-select: none;  /* 禁止文字选中 */
        }

        .agreement-check input[type="checkbox"] {
            width: 18px;
            height: 18px;
            margin-top: 2px;  /* 微调对齐 */
            accent-color: var(--neon-blue);  /* 复选框颜色 */
            cursor: pointer;
            flex-shrink: 0;
            border-radius: 4px;
        }

        <%-- 协议链接样式 --%>
        .agreement-check a {
            color: var(--neon-blue);
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s;
        }

        .agreement-check a:hover {
            color: var(--neon-purple);  /* 悬停变紫 */
            text-shadow: 0 0 8px rgba(0, 212, 255, 0.3);
        }

        <%-- 注册按钮样式 --%>
        /* Cyber button */
        .btn-register {
            width: 100%;
            padding: 16px;
            border: none;
            border-radius: 14px;
            background: linear-gradient(135deg, var(--neon-blue), var(--primary));  /* 蓝紫渐变 */
            color: #fff;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.3s;
            letter-spacing: 2px;
            position: relative;
            overflow: hidden;
            text-transform: uppercase;
        }

        .btn-register::before {
            content: '';
            position: absolute;
            inset: 0;
            background: linear-gradient(135deg, var(--neon-purple), var(--neon-blue));  /* 反向渐变 */
            opacity: 0;
            transition: opacity 0.3s;
        }

        .btn-register:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 30px rgba(0, 212, 255, 0.4), 0 0 60px rgba(0, 212, 255, 0.15);
        }

        .btn-register:hover::before {
            opacity: 1;
        }

        .btn-register span {
            position: relative;
            z-index: 1;  /* 确保文字在覆盖层之上 */
        }

        .btn-register:active {
            transform: translateY(0);
        }

        <%-- 注册按钮禁用状态 --%>
        .btn-register:disabled {
            opacity: 0.5;
            cursor: not-allowed;  /* 禁止点击光标 */
            transform: none;
            box-shadow: none;
        }

        .btn-register:disabled:hover::before {
            opacity: 0;  /* 禁用时不显示渐变 */
        }

        /* Links */
        .login-link-row {
            text-align: center;
            margin-top: 24px;
            color: var(--text-dim);
            font-size: 14px;
        }

        .login-link-row a {
            color: var(--neon-blue);
            font-weight: 700;
            text-decoration: none;
            transition: all 0.3s;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        .login-link-row a:hover {
            color: var(--neon-purple);
            gap: 10px;
            text-shadow: 0 0 8px rgba(0, 212, 255, 0.4);
        }

        .back-home {
            display: block;
            text-align: center;
            margin-top: 16px;
            color: var(--text-dim);
            font-size: 13px;
            text-decoration: none;
            transition: color 0.3s;
        }

        .back-home:hover {
            color: var(--text-main);
        }

        /* Scanline effect on left panel */
        .scanline {
            position: absolute;
            inset: 0;
            background: repeating-linear-gradient(
                0deg,
                transparent,
                transparent 2px,
                rgba(0, 212, 255, 0.008) 2px,
                rgba(0, 212, 255, 0.008) 4px
            );
            z-index: 1;
            pointer-events: none;
        }

        @media (max-width: 1024px) {
            .register-page {
                flex-direction: column;
            }

            .register-left {
                flex: none;
                padding: 40px 32px;
            }

            .register-left-stats {
                flex-direction: row;
                flex-wrap: wrap;
                justify-content: center;
                gap: 10px;
                margin-top: 28px;
            }

            .stat-item {
                padding: 10px 14px;
                flex: 1;
                min-width: 120px;
            }

            .register-right {
                padding: 32px 24px;
            }
        }

        @media (max-width: 480px) {
            .register-left {
                padding: 28px 20px;
            }

            .brand-icon {
                width: 56px;
                height: 56px;
                font-size: 24px;
            }

            .register-left-content h1 {
                font-size: 24px;
            }

            .register-left-stats {
                display: none;
            }

            .register-right {
                padding: 24px 16px;
            }

            .glass-card {
                padding: 28px 20px;
            }

            .register-card-header h2 {
                font-size: 22px;
            }

            .input-row {
                flex-direction: column;
                gap: 0;
            }
        }
    </style>
</head>
<body>
<%-- ========== 注册页面整体布局 ========== --%>
<div class="register-page">
    <%-- ========== 左侧面板：品牌展示 ========== --%>
    <div class="register-left">
        <div class="grid-overlay"></div>  <%-- 网格背景 --%>
        <div class="scanline"></div>  <%-- 扫描线效果 --%>
        <%-- 浮动粒子装饰 --%>
        <div class="particles-container">
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="particle"></div>
            <div class="geo-shape"></div>
            <div class="geo-shape"></div>
            <div class="geo-shape"></div>
            <div class="geo-shape"></div>
        </div>
        <%-- 品牌内容 --%>
        <div class="register-left-content">
            <div class="brand-icon"><i class="fas fa-meteor"></i></div>  <%-- 流星图标 --%>
            <h1><span>BookVerse</span></h1>  <%-- 品牌名称 --%>
            <p class="slogan">开启你的阅读之旅<br>探索无限知识宇宙</p>  <%-- 标语 --%>
            <div class="terminal-line">&gt; SYSTEM_ONLINE_</div>  <%-- 终端风格装饰 --%>
            <%-- 统计数据 --%>
            <div class="register-left-stats">
                <div class="stat-item">
                    <div class="stat-number">10万+</div>
                    <div class="stat-label">正版图书</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">50万+</div>
                    <div class="stat-label">忠实读者</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">99.6%</div>
                    <div class="stat-label">好评率</div>
                </div>
            </div>
        </div>
    </div>

    <%-- ========== 右侧面板：注册表单 ========== --%>
    <div class="register-right">
        <div class="register-card">
            <div class="glass-card">
                <%-- 注册表单头部 --%>
                <div class="register-card-header">
                    <h2><i class="fas fa-user-plus"></i>创建账户</h2>
                    <p>填写以下信息注册 BookVerse 账户</p>
                </div>

                <%-- 错误提示 --%>
                <c:if test="${not empty error}">
                    <div class="alert-danger"><i class="fas fa-triangle-exclamation"></i><c:out value="${error}"/></div>
                </c:if>

                <%-- ========== 注册表单 ========== --%>
                <%-- 使用JavaScript的fetch API提交表单，而非传统表单提交 --%>
                <form onsubmit="return handleRegister(event)" action="javascript:void(0)" id="registerForm">
                    <%-- ===== 第一区域：账户信息 ===== --%>
                    <div class="form-section">
                        <div class="form-section-title">
                            <div class="form-section-icon"><i class="fas fa-id-card"></i></div>
                            <span>账户信息</span>
                        </div>
                        <%-- 用户名输入框 --%>
                        <div class="input-group-custom">
                            <div class="input-wrapper">
                                <input type="text" name="userid" id="userid" placeholder=" " required>
                                <div class="input-glow"></div>
                                <span class="input-icon-left"><i class="fas fa-user"></i></span>
                                <label class="floating-label">用户名</label>
                            </div>
                        </div>
                        <%-- 密码输入框 --%>
                        <div class="input-group-custom">
                            <div class="input-wrapper">
                                <input type="password" name="password" id="password" placeholder=" " required minlength="6">
                                <div class="input-glow"></div>
                                <span class="input-icon-left"><i class="fas fa-lock"></i></span>
                                <label class="floating-label">密码（至少6位）</label>
                                <button type="button" class="toggle-password" onclick="togglePassword('password', this)" aria-label="显示密码">
                                    <i class="fas fa-eye"></i>
                                    <i class="fas fa-eye-slash" style="display:none"></i>
                                </button>
                            </div>
                            <%-- 密码强度指示器 --%>
                            <div class="password-strength" id="passwordStrength">
                                <div class="strength-bar">
                                    <div class="strength-bar-item" id="str1"></div>  <%-- 强度条1 --%>
                                    <div class="strength-bar-item" id="str2"></div>  <%-- 强度条2 --%>
                                    <div class="strength-bar-item" id="str3"></div>  <%-- 强度条3 --%>
                                    <div class="strength-bar-item" id="str4"></div>  <%-- 强度条4 --%>
                                </div>
                                <span class="strength-text" id="strengthText"></span>  <%-- 强度文字提示 --%>
                            </div>
                        </div>
                        <%-- 确认密码输入框 --%>
                        <div class="input-group-custom">
                            <div class="input-wrapper">
                                <input type="password" name="confirmPassword" id="confirmPassword" placeholder=" " required minlength="6">
                                <div class="input-glow"></div>
                                <span class="input-icon-left"><i class="fas fa-key"></i></span>
                                <label class="floating-label">确认密码</label>
                                <button type="button" class="toggle-password" onclick="togglePassword('confirmPassword', this)" aria-label="显示密码">
                                    <i class="fas fa-eye"></i>
                                    <i class="fas fa-eye-slash" style="display:none"></i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <%-- ===== 第二区域：联系方式 ===== --%>
                    <div class="form-section">
                        <div class="form-section-title">
                            <div class="form-section-icon"><i class="fas fa-at"></i></div>
                            <span>联系方式</span>
                        </div>
                        <%-- 邮箱输入框 --%>
                        <div class="input-group-custom">
                            <div class="input-wrapper">
                                <input type="email" name="email" id="email" placeholder=" " required>
                                <div class="input-glow"></div>
                                <span class="input-icon-left"><i class="fas fa-envelope"></i></span>
                                <label class="floating-label">邮箱</label>
                            </div>
                        </div>
                        <%-- 手机号输入框 --%>
                        <div class="input-group-custom">
                            <div class="input-wrapper">
                                <input type="tel" name="phone" id="phone" placeholder=" " required>
                                <div class="input-glow"></div>
                                <span class="input-icon-left"><i class="fas fa-mobile-screen"></i></span>
                                <label class="floating-label">手机号</label>
                            </div>
                        </div>
                    </div>

                    <%-- ===== 第三区域：个人信息 ===== --%>
                    <div class="form-section">
                        <div class="form-section-title">
                            <div class="form-section-icon"><i class="fas fa-address-card"></i></div>
                            <span>个人信息</span>
                        </div>
                        <%-- 姓名输入框（一行两列布局） --%>
                        <div class="input-row">
                            <div class="input-group-custom">
                                <div class="input-wrapper">
                                    <input type="text" name="firstname" id="firstname" placeholder=" " required>
                                    <div class="input-glow"></div>
                                    <span class="input-icon-left"><i class="fas fa-user"></i></span>
                                    <label class="floating-label">姓</label>
                                </div>
                            </div>
                            <div class="input-group-custom">
                                <div class="input-wrapper">
                                    <input type="text" name="lastname" id="lastname" placeholder=" " required>
                                    <div class="input-glow"></div>
                                    <span class="input-icon-left"><i class="fas fa-user"></i></span>
                                    <label class="floating-label">名</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- 用户协议同意复选框 --%>
                    <div class="agreement-row">
                        <label class="agreement-check">
                            <input type="checkbox" id="agreeTerms" required>
                            <span>我已阅读并同意 <a href="javascript:void(0);" onclick="alert('用户协议页面暂未开放。');">《用户服务协议》</a> 和 <a href="javascript:void(0);" onclick="alert('隐私政策页面暂未开放。');">《隐私政策》</a></span>
                        </label>
                    </div>
                    <%-- 注册按钮 --%>
                    <button type="submit" class="btn-register" id="registerBtn"><span><i class="fas fa-rocket"></i> 注 册</span></button>
                </form>

                <%-- 登录链接 --%>
                <div class="login-link-row">
                    已有账号？<a href="${pageContext.request.contextPath}/login">立即登录 <i class="fas fa-arrow-right"></i></a>
                </div>
                <%-- 返回首页链接 --%>
                <a href="${pageContext.request.contextPath}/" class="back-home"><i class="fas fa-arrow-left"></i> 返回首页</a>
            </div>
        </div>
    </div>
</div>

<%-- ========== JavaScript脚本区域 ========== --%>
<script>
/**
 * 切换密码显示/隐藏状态
 * @param {string} inputId - 密码输入框的ID
 * @param {HTMLElement} btn - 切换按钮元素
 */
function togglePassword(inputId, btn) {
    var input = document.getElementById(inputId);  // 获取输入框
    var eyeOpen = btn.querySelector('.fa-eye');  // 睁眼图标
    var eyeClosed = btn.querySelector('.fa-eye-slash');  // 闭眼图标
    if (input.type === 'password') {
        input.type = 'text';  // 显示密码
        eyeOpen.style.display = 'none';
        eyeClosed.style.display = 'inline';
    } else {
        input.type = 'password';  // 隐藏密码
        eyeOpen.style.display = 'inline';
        eyeClosed.style.display = 'none';
    }
}

/**
 * 检查密码强度
 * @param {string} password - 密码字符串
 * @returns {number} 强度分数（0-5）
 */
function checkPasswordStrength(password) {
    var score = 0;
    if (password.length >= 6) score++;  // 长度>=6加分
    if (password.length >= 10) score++;  // 长度>=10加分
    if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++;  // 包含大小写字母加分
    if (/[0-9]/.test(password)) score++;  // 包含数字加分
    if (/[^A-Za-z0-9]/.test(password)) score++;  // 包含特殊字符加分
    return score;
}

// 密码强度实时检测
var passwordInput = document.getElementById('password');
if (passwordInput) {
    passwordInput.addEventListener('input', function() {
        var val = this.value;  // 获取当前输入的密码
        var strengthDiv = document.getElementById('passwordStrength');
        var str1 = document.getElementById('str1');  // 强度条1
        var str2 = document.getElementById('str2');  // 强度条2
        var str3 = document.getElementById('str3');  // 强度条3
        var str4 = document.getElementById('str4');  // 强度条4
        var text = document.getElementById('strengthText');  // 强度文字

        // 重置所有强度条样式
        str1.className = 'strength-bar-item';
        str2.className = 'strength-bar-item';
        str3.className = 'strength-bar-item';
        str4.className = 'strength-bar-item';
        text.className = 'strength-text';
        text.textContent = '';

        // 密码为空时隐藏强度指示器
        if (val.length === 0) {
            strengthDiv.classList.remove('visible');
            return;
        }

        strengthDiv.classList.add('visible');
        var score = checkPasswordStrength(val);  // 计算强度分数

        // 根据分数显示不同强度等级
        if (score <= 1) {
            str1.className = 'strength-bar-item active-weak';  // 弱：1条红色
            text.textContent = '弱 - 建议增加长度和复杂度';
            text.className = 'strength-text weak';
        } else if (score <= 2) {
            str1.className = 'strength-bar-item active-medium';
            str2.className = 'strength-bar-item active-medium';  // 中：2条黄色
            text.textContent = '中 - 可以更强';
            text.className = 'strength-text medium';
        } else if (score <= 3) {
            str1.className = 'strength-bar-item active-medium';
            str2.className = 'strength-bar-item active-medium';
            str3.className = 'strength-bar-item active-medium';  // 中：3条黄色
            text.textContent = '中 - 安全性良好';
            text.className = 'strength-text medium';
        } else {
            str1.className = 'strength-bar-item active-strong';
            str2.className = 'strength-bar-item active-strong';
            str3.className = 'strength-bar-item active-strong';
            str4.className = 'strength-bar-item active-strong';  // 强：4条蓝色
            text.textContent = '强 - 密码安全性很高';
            text.className = 'strength-text strong';
        }
    });
}

/**
 * 表单验证函数
 * @returns {boolean} 验证是否通过
 */
function validateForm() {
    var password = document.getElementById('password').value;
    var confirmPassword = document.getElementById('confirmPassword').value;
    // 检查两次密码是否一致
    if (password !== confirmPassword) {
        alert('两次输入的密码不一致，请重新输入。');
        document.getElementById('confirmPassword').focus();  // 聚焦到确认密码框
        return false;
    }
    // 检查是否同意用户协议
    var agree = document.getElementById('agreeTerms');
    if (!agree.checked) {
        alert('请先阅读并同意用户服务协议和隐私政策。');
        return false;
    }
    return true;
}

/**
 * 处理注册表单提交
 * 使用fetch API异步提交注册请求
 * @param {Event} event - 表单提交事件
 */
function handleRegister(event) {
    event.preventDefault();  // 阻止表单默认提交行为
    if (!validateForm()) return false;  // 验证表单

    // 收集表单数据
    var data = {
        username: document.getElementById('userid').value,
        password: document.getElementById('password').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value
    };

    // 发送注册请求
    fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },  // 设置请求头为JSON
        body: JSON.stringify(data)  // 将数据转换为JSON字符串
    })
    .then(function(res) { return res.json(); })  // 解析响应JSON
    .then(function(result) {
        if (result.code === 200) {
            alert('注册成功！即将跳转到登录页面。');
            window.location.href = '/login';  // 跳转到登录页
        } else {
            alert(result.message || '注册失败');  // 显示错误信息
        }
    })
    .catch(function(err) {
        alert('网络错误，请稍后重试');  // 网络错误处理
    });
    return false;
}
</script>
</body>
</html>