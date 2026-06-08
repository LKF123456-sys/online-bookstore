<%-- ============================================================
     管理员登录页面 (login.jsp)
     功能：提供管理员账号密码登录入口，验证身份后进入管理后台
     技术要点：
       - 使用 JSTL 标签库（c:if）进行条件判断
       - 使用 CSS 变量统一管理主题颜色
       - 表单通过 POST 方式提交到 /admin/login 接口
       - 后端验证失败时，通过 EL 表达式 ${error} 显示错误信息
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 引入 JSTL 核心标签库，用于条件判断（c:if）等 --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%-- viewport 设置，确保页面在移动设备上正确缩放 --%>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>管理员登录 - BookVerse</title>
    <%-- 引入 Font Awesome 图标库 --%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>
    <style>
        /* ==================== CSS 变量定义 ==================== */
        /* 使用 CSS 变量（:root）统一管理颜色，方便后续修改主题 */
        :root {
            --cyber-bg: #0a0e1a;           /* 页面深色背景 */
            --neon-blue: #00d4ff;           /* 霓虹蓝色，用于高亮和强调 */
            --neon-purple: #a78bfa;         /* 霓虹紫色，用于渐变和装饰 */
            --primary: #6366f1;             /* 主色调，按钮和品牌色 */
            --card-bg: rgba(15, 23, 42, 0.65); /* 卡片半透明背景 */
            --border-glow: rgba(0, 212, 255, 0.15); /* 边框发光效果 */
            --input-bg: rgba(10, 14, 26, 0.8);  /* 输入框深色背景 */
            --text-primary: #e2e8f0;        /* 主要文字颜色（浅灰白） */
            --text-muted: #94a3b8;          /* 次要文字颜色（灰色） */
        }

        /* 全局重置样式，消除浏览器默认间距 */
        * { margin: 0; padding: 0; box-sizing: border-box; }

        /* ==================== 页面主体样式 ==================== */
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: var(--cyber-bg);       /* 使用深色背景变量 */
            min-height: 100vh;                  /* 最小高度占满整个视口 */
            display: flex;
            align-items: center;                /* 垂直居中 */
            justify-content: center;            /* 水平居中 */
            overflow: hidden;                   /* 隐藏溢出内容（粒子动画用） */
            color: var(--text-primary);
        }

        /* 网格背景覆盖层，用 ::before 伪元素实现装饰效果 */
        body::before {
            content: '';
            position: fixed;
            inset: 0;
            background-image:
                linear-gradient(rgba(0, 212, 255, 0.04) 1px, transparent 1px),
                linear-gradient(90deg, rgba(0, 212, 255, 0.04) 1px, transparent 1px);
            background-size: 60px 60px;
            pointer-events: none;  /* 允许鼠标穿透此层 */
            z-index: 0;
        }

        /* CRT 扫描线效果，模拟老式显示器的横纹（纯装饰） */
        body::after {
            content: '';
            position: fixed;
            inset: 0;
            background: repeating-linear-gradient(
                0deg,
                transparent,
                transparent 2px,
                rgba(0, 0, 0, 0.06) 2px,
                rgba(0, 0, 0, 0.06) 4px
            );
            pointer-events: none;
            z-index: 999;
        }

        /* ==================== 浮动粒子动画 ==================== */
        /* 粒子容器，固定定位覆盖整个页面 */
        .particles {
            position: fixed;
            inset: 0;
            pointer-events: none;
            z-index: 1;
        }

        /* 单个粒子的基本样式 */
        .particle {
            position: absolute;
            border-radius: 50%;
            animation: float linear infinite;  /* 使用 float 关键帧动画 */
            opacity: 0;
        }

        /* 每个粒子设置不同的大小、颜色、位置和动画时长，营造随机感 */
        .particle:nth-child(1) { width: 4px; height: 4px; background: var(--neon-blue); left: 10%; animation-duration: 18s; animation-delay: 0s; }
        .particle:nth-child(2) { width: 6px; height: 6px; background: var(--neon-purple); left: 25%; animation-duration: 22s; animation-delay: 2s; }
        .particle:nth-child(3) { width: 3px; height: 3px; background: var(--neon-blue); left: 40%; animation-duration: 16s; animation-delay: 4s; }
        .particle:nth-child(4) { width: 5px; height: 5px; background: var(--primary); left: 55%; animation-duration: 20s; animation-delay: 1s; }
        .particle:nth-child(5) { width: 4px; height: 4px; background: var(--neon-purple); left: 70%; animation-duration: 24s; animation-delay: 3s; }
        .particle:nth-child(6) { width: 3px; height: 3px; background: var(--neon-blue); left: 85%; animation-duration: 19s; animation-delay: 5s; }
        .particle:nth-child(7) { width: 5px; height: 5px; background: var(--primary); left: 15%; animation-duration: 21s; animation-delay: 6s; }
        .particle:nth-child(8) { width: 4px; height: 4px; background: var(--neon-purple); left: 60%; animation-duration: 17s; animation-delay: 1.5s; }

        /* 粒子上升动画：从底部飘到顶部，同时逐渐显现再消失 */
        @keyframes float {
            0% { transform: translateY(100vh) scale(0); opacity: 0; }
            10% { opacity: 0.8; }
            90% { opacity: 0.8; }
            100% { transform: translateY(-10vh) scale(1); opacity: 0; }
        }

        /* ==================== 背景几何装饰图形 ==================== */
        .geo {
            position: fixed;
            border: 1px solid rgba(0, 212, 255, 0.08);
            pointer-events: none;
            z-index: 0;
        }

        /* 右上角大圆 */
        .geo-1 {
            width: 300px; height: 300px;
            top: -80px; right: -80px;
            border-radius: 50%;
            border-color: rgba(167, 139, 250, 0.08);
            animation: spin 40s linear infinite;
        }

        /* 左下角小方块 */
        .geo-2 {
            width: 200px; height: 200px;
            bottom: -50px; left: -50px;
            animation: spin 30s linear infinite reverse;
        }

        /* 左侧菱形 */
        .geo-3 {
            width: 150px; height: 150px;
            top: 40%; left: 10%;
            transform: rotate(45deg);
            animation: spin 50s linear infinite;
        }

        /* 旋转动画 */
        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        /* ==================== 登录卡片（毛玻璃效果） ==================== */
        .login-card {
            position: relative;
            z-index: 10;               /* 确保卡片在背景元素之上 */
            width: 420px;
            max-width: 92vw;           /* 小屏幕时不超过视口宽度的92% */
            padding: 48px 40px 40px;
            background: var(--card-bg); /* 半透明背景 */
            border: 1px solid var(--border-glow);
            border-radius: 20px;
            backdrop-filter: blur(24px);      /* 毛玻璃模糊效果 */
            -webkit-backdrop-filter: blur(24px); /* Safari 兼容 */
            box-shadow:
                0 0 40px rgba(0, 212, 255, 0.06),
                0 24px 60px rgba(0, 0, 0, 0.4);
        }

        /* ==================== 品牌图标（带脉冲发光动画） ==================== */
        .brand-icon {
            width: 72px; height: 72px;
            margin: 0 auto 20px;       /* 水平居中 */
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--primary), var(--neon-purple));
            font-size: 32px;
            color: #fff;
            animation: pulse-glow 3s ease-in-out infinite;
            box-shadow: 0 0 30px rgba(99, 102, 241, 0.3);
        }

        /* 品牌图标脉冲发光动画 */
        @keyframes pulse-glow {
            0%, 100% { box-shadow: 0 0 20px rgba(99, 102, 241, 0.3); }
            50% { box-shadow: 0 0 40px rgba(0, 212, 255, 0.5), 0 0 80px rgba(99, 102, 241, 0.2); }
        }

        /* ==================== 标题样式（渐变文字） ==================== */
        .login-title {
            text-align: center;
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 6px;
            background: linear-gradient(90deg, var(--neon-blue), var(--neon-purple));
            -webkit-background-clip: text;       /* 用背景裁剪实现渐变文字 */
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        /* 副标题 */
        .login-subtitle {
            text-align: center;
            color: var(--text-muted);
            font-size: 14px;
            margin-bottom: 32px;
        }

        /* ==================== 错误提示框 ==================== */
        .cyber-alert {
            background: rgba(239, 68, 68, 0.1);
            border: 1px solid rgba(239, 68, 68, 0.3);
            border-left: 3px solid #ef4444;  /* 左侧红色边框强调 */
            border-radius: 8px;
            padding: 12px 16px;
            margin-bottom: 20px;
            color: #fca5a5;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .cyber-alert i { color: #ef4444; font-size: 16px; }

        /* ==================== 表单输入框样式 ==================== */
        .form-group {
            margin-bottom: 20px;
            position: relative;
        }

        /* 表单标签 */
        .form-group label {
            display: block;
            font-size: 13px;
            font-weight: 500;
            color: var(--text-muted);
            margin-bottom: 8px;
            letter-spacing: 0.3px;
        }

        /* 输入框包裹容器（用于定位图标） */
        .input-wrap {
            position: relative;
        }

        /* 输入框左侧图标 */
        .input-wrap i {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);  /* 垂直居中 */
            color: var(--text-muted);
            font-size: 15px;
            transition: color 0.3s;
        }

        /* 输入框主体 */
        .input-wrap input {
            width: 100%;
            padding: 12px 14px 12px 42px;  /* 左侧留出图标空间 */
            background: var(--input-bg);
            border: 1px solid rgba(100, 116, 139, 0.2);
            border-radius: 10px;
            color: var(--text-primary);
            font-size: 15px;
            outline: none;
            transition: border-color 0.3s, box-shadow 0.3s;
        }

        /* 输入框占位符文字样式 */
        .input-wrap input::placeholder {
            color: rgba(148, 163, 184, 0.5);
        }

        /* 输入框获得焦点时的高亮效果 */
        .input-wrap input:focus {
            border-color: var(--neon-blue);
            box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.12), 0 0 20px rgba(0, 212, 255, 0.08);
        }

        /* 输入框获得焦点时，左侧图标也变色 */
        .input-wrap input:focus + i,
        .input-wrap input:focus ~ i {
            color: var(--neon-blue);
        }

        /* ==================== 登录按钮 ==================== */
        .btn-login {
            width: 100%;
            padding: 13px 0;
            margin-top: 8px;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 600;
            color: #fff;
            cursor: pointer;
            background: linear-gradient(135deg, var(--primary), var(--neon-purple));
            position: relative;
            overflow: hidden;          /* 隐藏光扫过效果的溢出 */
            transition: box-shadow 0.3s, transform 0.2s;
            letter-spacing: 4px;       /* 字间距，让"登录"两字更舒展 */
        }

        /* 按钮光扫过效果（用 ::before 伪元素实现） */
        .btn-login::before {
            content: '';
            position: absolute;
            top: 0; left: -75%;
            width: 50%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
            transform: skewX(-20deg);   /* 倾斜效果 */
            animation: glow-sweep 3s ease-in-out infinite;
        }

        /* 光扫过动画 */
        @keyframes glow-sweep {
            0% { left: -75%; }
            50% { left: 125%; }
            100% { left: 125%; }
        }

        /* 按钮悬停效果 */
        .btn-login:hover {
            box-shadow: 0 0 24px rgba(99, 102, 241, 0.4), 0 0 60px rgba(167, 139, 250, 0.15);
            transform: translateY(-1px);  /* 轻微上移 */
        }

        /* 按钮点击效果 */
        .btn-login:active {
            transform: translateY(0);
        }

        /* ==================== 返回首页链接 ==================== */
        .back-link {
            display: block;
            text-align: center;
            margin-top: 24px;
            color: var(--text-muted);
            font-size: 14px;
            text-decoration: none;
            transition: color 0.3s;
        }

        .back-link:hover {
            color: var(--neon-blue);
        }

        .back-link i { margin-right: 4px; }
    </style>
</head>
<body>

    <%-- ==================== 浮动粒子背景 ==================== --%>
    <%-- 8个小圆点粒子，通过 CSS 动画从底部飘到顶部 --%>
    <div class="particles">
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
    </div>

    <%-- ==================== 背景几何装饰图形 ==================== --%>
    <%-- 三个旋转的几何形状，增加页面视觉层次 --%>
    <div class="geo geo-1"></div>
    <div class="geo geo-2"></div>
    <div class="geo geo-3"></div>

    <%-- ==================== 登录卡片 ==================== --%>
    <%-- 核心登录区域，包含品牌图标、标题、表单和返回链接 --%>
    <div class="login-card">
        <%-- 品牌盾牌图标 --%>
        <div class="brand-icon">
            <i class="fas fa-shield-halved"></i>
        </div>
        <%-- 页面标题 --%>
        <h1 class="login-title">管理后台</h1>
        <p class="login-subtitle">BookVerse Administration</p>

        <%-- 错误提示：当后端返回 error 信息时显示 --%>
        <%-- ${not empty error} 表示 error 变量不为空时条件成立 --%>
        <c:if test="${not empty error}">
            <div class="cyber-alert">
                <i class="fas fa-circle-exclamation"></i>
                <span>${error}</span>
            </div>
        </c:if>

        <%-- ==================== 登录表单 ==================== --%>
        <%-- 表单提交地址：${pageContext.request.contextPath} 获取项目根路径 --%>
        <%-- method="post" 表示用 POST 方式提交（密码不会显示在 URL 中） --%>
        <form action="${pageContext.request.contextPath}/admin/login" method="post">
            <%-- 管理员账号输入框 --%>
            <div class="form-group">
                <label>管理员账号</label>
                <div class="input-wrap">
                    <input type="text" name="username" placeholder="请输入管理员账号" required>
                    <i class="fas fa-user-shield"></i>
                </div>
            </div>
            <%-- 密码输入框 --%>
            <div class="form-group">
                <label>密码</label>
                <div class="input-wrap">
                    <input type="password" name="password" placeholder="请输入密码" required>
                    <i class="fas fa-lock"></i>
                </div>
            </div>
            <%-- 登录按钮，点击后触发表单提交 --%>
            <button type="submit" class="btn-login">登 录</button>
        </form>

        <%-- 返回商城首页链接 --%>
        <a href="${pageContext.request.contextPath}/" class="back-link">
            <i class="fas fa-arrow-left"></i> 返回商城首页
        </a>
    </div>

</body>
</html>
