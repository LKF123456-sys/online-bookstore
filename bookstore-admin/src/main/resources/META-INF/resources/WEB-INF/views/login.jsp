<%-- 登录页面 login.jsp --%>
<%-- 功能：用户登录表单页面，包含用户名/密码输入、记住我选项、登录按钮 --%>
<%-- 设计风格：赛博朋克风格，深色主题，霓虹灯光效果 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 引入JSTL核心标签库，用于条件判断和数据展示 --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <%-- 设置页面字符编码为UTF-8，支持中文显示 --%>
    <meta charset="UTF-8">
    <%-- 响应式视口设置，适配移动端设备 --%>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <%-- 页面标题，显示在浏览器标签页上 --%>
    <title>用户登录 - BookVerse</title>
    <%-- 网站图标（favicon） --%>
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/favicon.svg">
    <%-- 引入Font Awesome图标库，提供丰富的图标资源 --%>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        <%-- CSS变量定义区域 - 统一管理颜色和主题 --%>
        :root {
            --cyber-bg: #0a0e1a;  /* 主背景色：深蓝黑色 */
            --neon-blue: #00d4ff;  /* 霓虹蓝色：用于高亮和强调 */
            --neon-purple: #a78bfa;  /* 霓虹紫色：用于渐变和装饰 */
            --primary: #6366f1;  /* 主色调：靛蓝色 */
            --cyber-card: rgba(15, 20, 40, 0.85);  /* 卡片背景色：半透明深色 */
            --cyber-border: rgba(0, 212, 255, 0.15);  /* 边框色：淡蓝色 */
            --text-main: #e2e8f0;  /* 主要文字颜色：浅灰白色 */
            --text-dim: #64748b;  /* 次要文字颜色：暗灰色 */
            --input-bg: rgba(10, 14, 30, 0.9);  /* 输入框背景色 */
            --input-border: rgba(100, 116, 139, 0.25);  /* 输入框边框色 */
        }

        <%-- 全局重置样式 - 清除浏览器默认间距和边框 --%>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        <%-- 页面主体样式 --%>
        body {
            font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;  /* 字体设置：优先使用思源黑体，兼容各平台 */
            background: var(--cyber-bg);  /* 使用CSS变量设置背景色 */
            min-height: 100vh;  /* 最小高度为视口高度 */
            overflow-x: hidden;  /* 隐藏水平溢出内容 */
            color: var(--text-main);  /* 文字颜色 */
        }

        <%-- 页面布局 - 左右两栏布局 --%>
        /* ===== Page Layout ===== */
        .login-page {
            display: flex;  /* 弹性布局，左右排列 */
            min-height: 100vh;  /* 占满整个视口高度 */
            animation: pageFadeIn 0.8s ease-out;  /* 页面加载时的淡入动画 */
        }

        /* 页面淡入动画效果 */
        @keyframes pageFadeIn {
            from { opacity: 0; }  /* 初始状态：完全透明 */
            to { opacity: 1; }  /* 结束状态：完全可见 */
        }

        <%-- 左侧面板样式 - 品牌展示区域 --%>
        /* ===== LEFT PANEL ===== */
        .login-left {
            flex: 0 0 440px;  /* 固定宽度440像素 */
            background: linear-gradient(160deg, #0a0e1a 0%, #0d1333 40%, #15104a 70%, #1a1050 100%);  /* 渐变背景 */
            display: flex;
            align-items: center;  /* 垂直居中 */
            justify-content: center;  /* 水平居中 */
            position: relative;  /* 相对定位，用于子元素绝对定位 */
            overflow: hidden;  /* 隐藏溢出内容 */
            padding: 60px 40px;  /* 内边距 */
        }

        <%-- 左侧面板装饰性光晕效果 --%>
        .login-left::before {
            content: '';  /* 伪元素必须有content属性 */
            position: absolute;  /* 绝对定位 */
            top: -200px;
            right: -200px;
            width: 500px;
            height: 500px;
            border-radius: 50%;  /* 圆形 */
            background: radial-gradient(circle, rgba(0, 212, 255, 0.08) 0%, transparent 70%);  /* 径向渐变，蓝色光晕 */
            animation: floatOrb 8s ease-in-out infinite;  /* 浮动动画 */
        }

        .login-left::after {
            content: '';
            position: absolute;
            bottom: -150px;
            left: -100px;
            width: 400px;
            height: 400px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(167, 139, 250, 0.07) 0%, transparent 70%);  /* 紫色光晕 */
            animation: floatOrb 10s ease-in-out infinite reverse;  /* 反向浮动动画 */
        }

        /* 光晕浮动动画 */
        @keyframes floatOrb {
            0%, 100% { transform: translate(0, 0) scale(1); }  /* 原始位置 */
            50% { transform: translate(30px, -30px) scale(1.1); }  /* 移动并放大 */
        }

        <%-- 网格覆盖层 - 营造科技感背景 --%>
        /* Grid overlay */
        .grid-overlay {
            position: absolute;
            inset: 0;  /* 填满父元素 */
            background-image:
                linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),  /* 水平网格线 */
                linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);  /* 垂直网格线 */
            background-size: 40px 40px;  /* 网格大小 */
            z-index: 0;  /* 层级：最底层 */
        }

        <%-- 扫描线效果 - 模拟CRT显示器效果 --%>
        /* CRT scanline effect */
        .scanline {
            position: absolute;
            inset: 0;
            background: repeating-linear-gradient(
                0deg,
                transparent,
                transparent 2px,
                rgba(0, 212, 255, 0.008) 2px,
                rgba(0, 212, 255, 0.008) 4px
            );  /* 重复的水平线条，模拟老式显示器 */
            z-index: 1;
            pointer-events: none;  /* 不响应鼠标事件 */
        }

        <%-- 浮动粒子容器 - 装饰性背景效果 --%>
        /* Floating particles with neon glow */
        .particles-container {
            position: absolute;
            inset: 0;
            overflow: hidden;
            z-index: 0;
        }

        <%-- 单个粒子样式 --%>
        .particle {
            position: absolute;
            border-radius: 50%;  /* 圆形粒子 */
            animation: particleFloat linear infinite;  /* 向上漂浮动画 */
        }

        <%-- 10个粒子的定位和颜色设置（每个粒子位置、大小、颜色、动画时长不同） --%>
        .particle:nth-child(1)  { left: 8%;  top: 15%; width: 3px; height: 3px; background: rgba(0, 212, 255, 0.5);   box-shadow: 0 0 6px rgba(0, 212, 255, 0.4);   animation-duration: 12s; }
        .particle:nth-child(2)  { left: 22%; top: 55%; width: 5px; height: 5px; background: rgba(167, 139, 250, 0.45); box-shadow: 0 0 8px rgba(167, 139, 250, 0.3);  animation-duration: 15s; animation-delay: -2s; }
        .particle:nth-child(3)  { left: 40%; top: 25%; width: 3px; height: 3px; background: rgba(0, 212, 255, 0.6);   box-shadow: 0 0 6px rgba(0, 212, 255, 0.5);   animation-duration: 10s; animation-delay: -4s; }
        .particle:nth-child(4)  { left: 60%; top: 65%; width: 4px; height: 4px; background: rgba(99, 102, 241, 0.5);   box-shadow: 0 0 8px rgba(99, 102, 241, 0.3);   animation-duration: 18s; animation-delay: -1s; }
        .particle:nth-child(5)  { left: 78%; top: 35%; width: 4px; height: 4px; background: rgba(0, 212, 255, 0.45);  box-shadow: 0 0 6px rgba(0, 212, 255, 0.35);  animation-duration: 14s; animation-delay: -3s; }
        .particle:nth-child(6)  { left: 12%; top: 75%; width: 5px; height: 5px; background: rgba(167, 139, 250, 0.4);  box-shadow: 0 0 10px rgba(167, 139, 250, 0.3); animation-duration: 16s; animation-delay: -5s; }
        .particle:nth-child(7)  { left: 52%; top: 10%; width: 3px; height: 3px; background: rgba(0, 212, 255, 0.55);  box-shadow: 0 0 6px rgba(0, 212, 255, 0.4);   animation-duration: 11s; animation-delay: -2.5s; }
        .particle:nth-child(8)  { left: 70%; top: 50%; width: 4px; height: 4px; background: rgba(99, 102, 241, 0.45);  box-shadow: 0 0 8px rgba(99, 102, 241, 0.3);  animation-duration: 13s; animation-delay: -6s; }
        .particle:nth-child(9)  { left: 30%; top: 80%; width: 5px; height: 5px; background: rgba(0, 212, 255, 0.35);  box-shadow: 0 0 10px rgba(0, 212, 255, 0.25); animation-duration: 17s; animation-delay: -1.5s; }
        .particle:nth-child(10) { left: 88%; top: 20%; width: 3px; height: 3px; background: rgba(167, 139, 250, 0.5);  box-shadow: 0 0 6px rgba(167, 139, 250, 0.4); animation-duration: 9s;  animation-delay: -4.5s; }

        /* 粒子漂浮动画 */
        @keyframes particleFloat {
            0%   { transform: translateY(0) translateX(0) rotate(0deg); opacity: 0; }  /* 起始：透明，在原点 */
            10%  { opacity: 1; }  /* 10%时变为可见 */
            90%  { opacity: 1; }  /* 90%时保持可见 */
            100% { transform: translateY(-100vh) translateX(50px) rotate(360deg); opacity: 0; }  /* 结束：移出屏幕，旋转一周 */
        }

        <%-- 几何形状装饰元素 - 缓慢旋转的装饰图形 --%>
        /* Rotating geometric shapes with inner glow */
        .geo-shape {
            position: absolute;
            border: 1px solid;  /* 边框样式 */
            z-index: 0;
            animation: geoRotate 20s linear infinite;  /* 旋转动画 */
        }

        <%-- 第一个几何形状：方形，蓝色调 --%>
        .geo-shape:nth-child(15) {
            width: 70px; height: 70px; top: 10%; left: 15%;
            border-color: rgba(0, 212, 255, 0.12);
            border-radius: 4px;  /* 轻微圆角 */
            animation-duration: 25s;
            box-shadow: inset 0 0 15px rgba(0, 212, 255, 0.05);  /* 内发光效果 */
        }
        <%-- 第二个几何形状：圆形，紫色调 --%>
        .geo-shape:nth-child(16) {
            width: 45px; height: 45px; top: 70%; right: 12%;
            border-color: rgba(167, 139, 250, 0.12);
            border-radius: 50%;  /* 正圆形 */
            animation-duration: 18s;
            animation-direction: reverse;  /* 反向旋转 */
            box-shadow: inset 0 0 10px rgba(167, 139, 250, 0.05);
        }
        <%-- 第三个几何形状：大方形，蓝色调 --%>
        .geo-shape:nth-child(17) {
            width: 100px; height: 100px; bottom: 10%; left: 50%;
            border-color: rgba(0, 212, 255, 0.08);
            border-radius: 6px;
            animation-duration: 30s;
            box-shadow: inset 0 0 20px rgba(0, 212, 255, 0.03);
        }
        <%-- 第四个几何形状：中等圆形，紫色调 --%>
        .geo-shape:nth-child(18) {
            width: 55px; height: 55px; top: 40%; left: 76%;
            border-color: rgba(167, 139, 250, 0.1);
            border-radius: 50%;
            animation-duration: 22s;
            animation-direction: reverse;
            box-shadow: inset 0 0 12px rgba(167, 139, 250, 0.04);
        }

        /* 几何形状旋转动画 */
        @keyframes geoRotate {
            0%   { transform: rotate(0deg) scale(1); }  /* 初始状态 */
            50%  { transform: rotate(180deg) scale(1.1); }  /* 旋转半圈并放大 */
            100% { transform: rotate(360deg) scale(1); }  /* 旋转一圈回到原状 */
        }

        <%-- 左侧面板内容区域 - 品牌信息和统计数据 --%>
        /* Left panel content */
        .login-left-content {
            position: relative;
            z-index: 2;  /* 确保内容在装饰元素之上 */
            text-align: center;
            color: #fff;
            max-width: 340px;  /* 最大宽度限制 */
        }

        <%-- 品牌图标 - 带有霓虹光效的书本图标 --%>
        .brand-icon {
            width: 76px;
            height: 76px;
            margin: 0 auto 22px;  /* 水平居中，底部间距 */
            background: linear-gradient(135deg, var(--neon-blue), var(--primary));  /* 渐变背景 */
            border-radius: 18px;  /* 圆角 */
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 34px;  /* 图标大小 */
            box-shadow: 0 0 30px rgba(0, 212, 255, 0.3), 0 0 60px rgba(0, 212, 255, 0.15);  /* 多层阴影，霓虹光效 */
            animation: iconPulse 3s ease-in-out infinite;  /* 脉冲动画 */
            position: relative;
        }

        <%-- 品牌图标外边框 - 呼吸灯效果 --%>
        .brand-icon::after {
            content: '';
            position: absolute;
            inset: -3px;  /* 向外扩展3像素 */
            border-radius: 20px;
            border: 1px solid rgba(0, 212, 255, 0.3);
            animation: borderPulse 2s ease-in-out infinite;  /* 边框呼吸灯动画 */
        }

        /* 图标脉冲动画 */
        @keyframes iconPulse {
            0%, 100% { box-shadow: 0 0 30px rgba(0, 212, 255, 0.3), 0 0 60px rgba(0, 212, 255, 0.15); }  /* 正常状态 */
            50%      { box-shadow: 0 0 40px rgba(0, 212, 255, 0.5), 0 0 80px rgba(0, 212, 255, 0.25); }  /* 增强光效 */
        }

        /* 边框呼吸灯动画 */
        @keyframes borderPulse {
            0%, 100% { opacity: 0.5; }  /* 半透明 */
            50%      { opacity: 1; }  /* 完全不透明 */
        }

        <%-- 品牌标题样式 --%>
        .login-left-content h1 {
            font-size: 34px;
            font-weight: 900;  /* 极粗字体 */
            margin-bottom: 10px;
            letter-spacing: -0.5px;  /* 字间距微调 */
        }

        <%-- 品牌名称渐变文字效果 --%>
        .login-left-content h1 span {
            background: linear-gradient(135deg, var(--neon-blue), var(--neon-purple));  /* 蓝紫渐变 */
            -webkit-background-clip: text;  /* 背景裁剪到文字 */
            -webkit-text-fill-color: transparent;  /* 文字颜色透明，显示背景 */
            background-clip: text;
            filter: drop-shadow(0 0 8px rgba(0, 212, 255, 0.3));  /* 文字阴影 */
        }

        <%-- 标语文字样式 --%>
        .login-left-content .slogan {
            font-size: 15px;
            opacity: 0.5;  /* 半透明效果 */
            line-height: 1.8;  /* 行高 */
            font-weight: 300;  /* 细体字 */
            letter-spacing: 1px;
        }

        <%-- 终端风格文字 - 装饰性元素 --%>
        .terminal-line {
            margin-top: 8px;
            font-family: 'Courier New', monospace;  /* 等宽字体 */
            font-size: 11px;
            color: var(--neon-blue);
            opacity: 0.5;
            letter-spacing: 2px;
        }

        <%-- 统计数据区域 --%>
        .login-left-stats {
            margin-top: 40px;
            display: flex;
            flex-direction: column;  /* 垂直排列 */
            gap: 14px;  /* 子元素间距 */
        }

        <%-- 单个统计项样式 --%>
        .stat-item {
            display: flex;
            align-items: center;  /* 垂直居中 */
            gap: 14px;
            padding: 12px 18px;
            background: rgba(0, 212, 255, 0.03);  /* 淡蓝色背景 */
            border: 1px solid rgba(0, 212, 255, 0.08);
            border-radius: 12px;  /* 圆角 */
            backdrop-filter: blur(10px);  /* 毛玻璃效果 */
            transition: all 0.3s;  /* 过渡动画 */
            animation: statSlideIn 0.6s ease-out backwards;  /* 滑入动画 */
        }

        <%-- 统计项入场动画延迟（依次出现） --%>
        .stat-item:nth-child(1) { animation-delay: 0.3s; }
        .stat-item:nth-child(2) { animation-delay: 0.5s; }
        .stat-item:nth-child(3) { animation-delay: 0.7s; }

        <%-- 统计项鼠标悬停效果 --%>
        .stat-item:hover {
            background: rgba(0, 212, 255, 0.06);  /* 背景变亮 */
            border-color: rgba(0, 212, 255, 0.2);  /* 边框变亮 */
            transform: translateX(6px);  /* 向右微移 */
            box-shadow: 0 0 20px rgba(0, 212, 255, 0.08);  /* 添加光晕 */
        }

        /* 统计项滑入动画 */
        @keyframes statSlideIn {
            from { opacity: 0; transform: translateX(-20px); }  /* 从左侧滑入 */
            to   { opacity: 1; transform: translateX(0); }  /* 到达原位 */
        }

        <%-- 统计数字样式 --%>
        .stat-number {
            font-size: 24px;
            font-weight: 800;
            min-width: 60px;
            text-align: left;
            font-family: 'Courier New', monospace;  /* 等宽字体，数字对齐 */
        }

        <%-- 不同统计项的数字颜色（蓝、紫、黄） --%>
        .stat-item:nth-child(1) .stat-number { color: var(--neon-blue);   text-shadow: 0 0 10px rgba(0, 212, 255, 0.4); }
        .stat-item:nth-child(2) .stat-number { color: var(--neon-purple); text-shadow: 0 0 10px rgba(167, 139, 250, 0.4); }
        .stat-item:nth-child(3) .stat-number { color: #fbbf24;            text-shadow: 0 0 10px rgba(251, 191, 36, 0.4); }

        <%-- 统计标签文字样式 --%>
        .stat-label {
            font-size: 13px;
            opacity: 0.5;
            text-align: left;
        }

        <%-- 右侧面板样式 - 登录表单区域 --%>
        /* ===== RIGHT PANEL ===== */
        .login-right {
            flex: 1;  /* 占据剩余空间 */
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 48px;
            background: var(--cyber-bg);
            position: relative;
        }

        <%-- 右侧面板装饰性光晕 --%>
        .login-right::before {
            content: '';
            position: absolute;
            top: -100px;
            right: -100px;
            width: 300px;
            height: 300px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(0, 212, 255, 0.04) 0%, transparent 70%);  /* 淡蓝色光晕 */
        }

        <%-- 登录卡片容器 --%>
        .login-card {
            width: 100%;
            max-width: 440px;  /* 最大宽度限制 */
            animation: cardSlideIn 0.6s ease-out 0.2s backwards;  /* 卡片滑入动画，延迟0.2秒 */
        }

        /* 卡片滑入动画 */
        @keyframes cardSlideIn {
            from { opacity: 0; transform: translateY(30px); }  /* 从下方滑入 */
            to   { opacity: 1; transform: translateY(0); }  /* 到达原位 */
        }

        <%-- 毛玻璃卡片样式 - 半透明深色卡片 --%>
        /* Dark glass card with neon top border */
        .glass-card {
            background: var(--cyber-card);  /* 半透明深色背景 */
            border: 1px solid var(--cyber-border);  /* 淡蓝色边框 */
            border-radius: 20px;  /* 大圆角 */
            padding: 40px 36px;
            backdrop-filter: blur(20px);  /* 毛玻璃模糊效果 */
            box-shadow: 0 0 40px rgba(0, 212, 255, 0.05), inset 0 1px 0 rgba(255, 255, 255, 0.03);  /* 外阴影和内高光 */
            position: relative;
            overflow: hidden;
        }

        <%-- 卡片顶部霓虹渐变边框 --%>
        .glass-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 2px;  /* 2像素高度的渐变条 */
            background: linear-gradient(90deg, transparent, var(--neon-blue), var(--neon-purple), transparent);  /* 蓝紫渐变 */
            opacity: 0.6;
        }

        <%-- 登录卡片头部样式 --%>
        /* Card header */
        .login-card-header {
            margin-bottom: 32px;
            text-align: center;
        }

        .login-card-header h2 {
            font-size: 28px;
            font-weight: 800;
            color: var(--text-main);
            margin-bottom: 8px;
            letter-spacing: -0.5px;
        }

        .login-card-header h2 i {
            color: var(--neon-blue);  /* 图标颜色 */
            margin-right: 10px;
            font-size: 24px;
            text-shadow: 0 0 12px rgba(0, 212, 255, 0.5);  /* 图标光晕 */
        }

        .login-card-header p {
            color: var(--text-dim);  /* 副标题颜色 */
            font-size: 14px;
            font-weight: 400;
        }

        <%-- 错误提示样式 - 红色警告框 --%>
        /* Cyber alert for errors */
        .alert-danger {
            background: rgba(239, 68, 68, 0.1);  /* 淡红色背景 */
            border: 1px solid rgba(239, 68, 68, 0.3);  /* 红色边框 */
            border-radius: 12px;
            padding: 14px 18px;
            color: #f87171;  /* 红色文字 */
            font-size: 14px;
            margin-bottom: 24px;
            animation: alertShake 0.5s ease-in-out;  /* 抖动动画 */
            display: flex;
            align-items: center;
            gap: 10px;
            box-shadow: 0 0 20px rgba(239, 68, 68, 0.08);  /* 红色光晕 */
        }

        .alert-danger i {
            font-size: 16px;
            flex-shrink: 0;  /* 图标不缩小 */
        }

        /* 警告框抖动动画 */
        @keyframes alertShake {
            0%, 100% { transform: translateX(0); }  /* 原位 */
            25%      { transform: translateX(-5px); }  /* 向左偏移 */
            75%      { transform: translateX(5px); }  /* 向右偏移 */
        }

        <%-- 成功提示样式 - 绿色提示框 --%>
        /* Success alert */
        .alert-success {
            background: rgba(34, 197, 94, 0.1);  /* 淡绿色背景 */
            border: 1px solid rgba(34, 197, 94, 0.3);  /* 绿色边框 */
            border-radius: 12px;
            padding: 14px 18px;
            color: #4ade80;  /* 绿色文字 */
            font-size: 14px;
            margin-bottom: 24px;
            display: flex;
            align-items: center;
            gap: 10px;
            box-shadow: 0 0 20px rgba(34, 197, 94, 0.08);  /* 绿色光晕 */
        }

        .alert-success i {
            font-size: 16px;
            flex-shrink: 0;
        }

        <%-- 输入框组样式 - 包含输入框、图标、标签 --%>
        /* Cyber inputs */
        .input-group-custom {
            position: relative;
            margin-bottom: 22px;  /* 输入框之间的间距 */
        }

        .input-group-custom .input-wrapper {
            position: relative;  /* 为内部绝对定位元素提供参考 */
        }

        <%-- 输入框基础样式 --%>
        .input-group-custom input {
            width: 100%;
            padding: 16px 16px 8px 44px;  /* 上右下左内边距，左侧留出图标空间 */
            border: 1px solid var(--input-border);
            background: var(--input-bg);
            border-radius: 10px;
            font-size: 14px;
            color: var(--text-main);
            transition: all 0.3s;  /* 过渡动画 */
            outline: none;  /* 移除默认聚焦轮廓 */
            height: 52px;
        }

        <%-- 输入框聚焦效果 - 霓虹蓝边框和光晕 --%>
        .input-group-custom input:focus {
            border-color: var(--neon-blue);  /* 聚焦时边框变蓝 */
            background: rgba(10, 14, 30, 1);  /* 背景变深 */
            box-shadow: 0 0 15px rgba(0, 212, 255, 0.15), inset 0 0 15px rgba(0, 212, 255, 0.03);  /* 外发光和内发光 */
        }

        <%-- 输入框自动填充样式覆盖（浏览器自动填充时的样式） --%>
        .input-group-custom input:-webkit-autofill,
        .input-group-custom input:-webkit-autofill:hover,
        .input-group-custom input:-webkit-autofill:focus {
            -webkit-box-shadow: 0 0 0 1000px #0a0e1a inset;  /* 覆盖自动填充的默认背景色 */
            -webkit-text-fill-color: var(--text-main);  /* 文字颜色 */
            transition: background-color 5000s ease-in-out 0s;  /* 延迟背景色变化 */
        }

        <%-- 输入框底部发光线条 - 聚焦时显示 --%>
        .input-glow {
            position: absolute;
            bottom: 0;
            left: 10%;
            right: 10%;
            height: 1px;
            background: linear-gradient(90deg, transparent, var(--neon-blue), transparent);  /* 渐变发光线条 */
            opacity: 0;  /* 默认隐藏 */
            transform: scaleX(0);  /* 初始宽度为0 */
            transition: all 0.4s ease;
        }

        <%-- 输入框聚焦时显示发光线条 --%>
        .input-group-custom input:focus ~ .input-glow {
            opacity: 1;  /* 显示 */
            transform: scaleX(1);  /* 展开到全宽 */
        }

        <%-- 输入框左侧图标样式 --%>
        .input-icon-left {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);  /* 垂直居中 */
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

        <%-- 输入框聚焦时图标变色 --%>
        .input-group-custom input:focus ~ .input-icon-left {
            color: var(--neon-blue);  /* 变为霓虹蓝 */
            text-shadow: 0 0 8px rgba(0, 212, 255, 0.5);  /* 添加光晕 */
        }

        <%-- 浮动标签样式 - 输入内容时标签上浮 --%>
        .floating-label {
            position: absolute;
            left: 44px;
            top: 50%;
            transform: translateY(-50%);  /* 垂直居中 */
            color: var(--text-dim);
            font-size: 13px;
            pointer-events: none;  /* 不响应鼠标事件 */
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);  /* 平滑过渡 */
            transform-origin: left center;  /* 变换原点：左侧居中 */
            background: transparent;
            z-index: 1;
        }

        <%-- 输入框聚焦或有内容时，标签上浮并缩小 --%>
        .input-group-custom input:focus ~ .floating-label,
        .input-group-custom input:not(:placeholder-shown) ~ .floating-label {
            top: 6px;  /* 移动到顶部 */
            transform: translateY(0) scale(0.72);  /* 缩小 */
            color: var(--neon-blue);  /* 变为霓虹蓝 */
            font-weight: 600;
        }

        <%-- 自动填充时标签也上浮 --%>
        .input-group-custom input:-webkit-autofill ~ .floating-label {
            top: 6px;
            transform: translateY(0) scale(0.72);
            color: var(--neon-blue);
            font-weight: 600;
        }

        <%-- 密码显示/隐藏切换按钮 --%>
        .toggle-password {
            position: absolute;
            right: 14px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            color: var(--text-dim);
            cursor: pointer;  /* 鼠标指针变为手型 */
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

        <%-- 密码切换按钮悬停效果 --%>
        .toggle-password:hover {
            color: var(--neon-blue);
            background: rgba(0, 212, 255, 0.08);  /* 淡蓝色背景 */
        }

        <%-- 记住我行样式 --%>
        /* Remember me row */
        .remember-row {
            display: flex;
            align-items: center;
            justify-content: space-between;  /* 两端对齐 */
            margin-bottom: 24px;
            margin-top: 4px;
        }

        <%-- 记住我复选框样式 --%>
        .remember-check {
            display: flex;
            align-items: center;
            gap: 8px;
            cursor: pointer;
            color: var(--text-dim);
            font-size: 13px;
            user-select: none;  /* 禁止文字选中 */
        }

        .remember-check input[type="checkbox"] {
            width: 16px;
            height: 16px;
            accent-color: var(--neon-blue);  /* 复选框颜色 */
            cursor: pointer;
            border-radius: 4px;
        }

        <%-- 登录按钮样式 - 渐变背景，霓虹光效 --%>
        /* Cyber submit button with glow sweep */
        .btn-login {
            width: 100%;
            padding: 16px;
            border: none;
            border-radius: 14px;
            background: linear-gradient(135deg, var(--neon-blue), var(--primary));  /* 蓝紫渐变背景 */
            color: #fff;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.3s;
            letter-spacing: 2px;  /* 字间距 */
            position: relative;
            overflow: hidden;  /* 隐藏溢出的光效 */
            text-transform: uppercase;  /* 文字大写 */
        }

        <%-- 按钮悬停时的渐变覆盖层 --%>
        .btn-login::before {
            content: '';
            position: absolute;
            inset: 0;
            background: linear-gradient(135deg, var(--neon-purple), var(--neon-blue));  /* 反向渐变 */
            opacity: 0;  /* 默认隐藏 */
            transition: opacity 0.3s;
        }

        <%-- 按钮光效扫过动画 --%>
        .btn-login::after {
            content: '';
            position: absolute;
            top: -50%;
            left: -60%;
            width: 40%;
            height: 200%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);  /* 白色光带 */
            transform: skewX(-20deg);  /* 倾斜 */
            animation: glowSweep 4s ease-in-out infinite;  /* 光效扫过动画 */
        }

        /* 光效扫过动画 */
        @keyframes glowSweep {
            0%   { left: -60%; }  /* 从左侧开始 */
            30%  { left: 120%; }  /* 扫到右侧 */
            100% { left: 120%; }  /* 保持在右侧 */
        }

        <%-- 登录按钮悬停效果 --%>
        .btn-login:hover {
            transform: translateY(-2px);  /* 向上微移 */
            box-shadow: 0 0 30px rgba(0, 212, 255, 0.4), 0 0 60px rgba(0, 212, 255, 0.15);  /* 霓虹光晕 */
        }

        .btn-login:hover::before {
            opacity: 1;  /* 显示渐变覆盖层 */
        }

        .btn-login span {
            position: relative;
            z-index: 1;  /* 确保文字在覆盖层之上 */
        }

        .btn-login:active {
            transform: translateY(0);  /* 按下时回到原位 */
        }

        <%-- 分隔线样式 --%>
        /* Divider */
        .divider {
            display: flex;
            align-items: center;
            gap: 14px;
            margin: 24px 0;
            color: var(--text-dim);
            font-size: 12px;
            letter-spacing: 1px;
        }

        <%-- 分隔线两侧的渐变线条 --%>
        .divider::before,
        .divider::after {
            content: '';
            flex: 1;  /* 占据剩余空间 */
            height: 1px;
            background: linear-gradient(90deg, transparent, rgba(100, 116, 139, 0.3), transparent);  /* 渐变线条 */
        }

        <%-- 注册链接行样式 --%>
        /* Links */
        .register-link-row {
            text-align: center;
            margin-top: 24px;
            color: var(--text-dim);
            font-size: 14px;
        }

        .register-link-row a {
            color: var(--neon-blue);
            font-weight: 700;
            text-decoration: none;  /* 移除下划线 */
            transition: all 0.3s;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        <%-- 注册链接悬停效果 --%>
        .register-link-row a:hover {
            color: var(--neon-purple);  /* 变为紫色 */
            gap: 10px;  /* 间距增大，箭头右移 */
            text-shadow: 0 0 8px rgba(0, 212, 255, 0.4);  /* 光晕效果 */
        }

        <%-- 返回首页链接样式 --%>
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
            color: var(--text-main);  /* 悬停时文字变亮 */
        }

        <%-- 响应式布局 - 平板设备（1024px以下） --%>
        /* Responsive */
        @media (max-width: 1024px) {
            .login-page {
                flex-direction: column;  /* 改为垂直布局 */
            }

            .login-left {
                flex: none;  /* 取消固定宽度 */
                padding: 40px 32px;
            }

            .login-left-stats {
                flex-direction: row;  /* 统计项改为水平排列 */
                flex-wrap: wrap;  /* 允许换行 */
                justify-content: center;
                gap: 10px;
                margin-top: 28px;
            }

            .stat-item {
                padding: 10px 14px;
                flex: 1;  /* 等分宽度 */
                min-width: 120px;  /* 最小宽度 */
            }

            .login-right {
                padding: 32px 24px;
            }
        }

        <%-- 响应式布局 - 手机设备（480px以下） --%>
        @media (max-width: 480px) {
            .login-left {
                padding: 28px 20px;
            }

            .brand-icon {
                width: 56px;
                height: 56px;
                font-size: 24px;
            }

            .login-left-content h1 {
                font-size: 24px;
            }

            .login-left-stats {
                display: none;  /* 隐藏统计数据 */
            }

            .login-right {
                padding: 24px 16px;
            }

            .glass-card {
                padding: 28px 20px;
            }

            .login-card-header h2 {
                font-size: 22px;
            }
        }
    </style>
</head>
<%-- 页面主体内容 --%>
<body>
<%-- ========== 登录页面整体布局 ========== --%>
<%-- 采用左右两栏布局：左侧品牌展示，右侧登录表单 --%>
<div class="login-page">
    <%-- ========== 左侧面板：品牌展示区域 ========== --%>
    <%-- 包含品牌Logo、标语、统计数据等，用于营造品牌氛围 --%>
    <div class="login-left">
        <%-- 装饰性背景元素 --%>
        <div class="grid-overlay"></div>  <%-- 网格覆盖层 --%>
        <div class="scanline"></div>  <%-- 扫描线效果 --%>
        <%-- 浮动粒子容器 - 10个霓虹色小圆点 --%>
        <div class="particles-container">
            <div class="particle"></div>  <%-- 粒子1 --%>
            <div class="particle"></div>  <%-- 粒子2 --%>
            <div class="particle"></div>  <%-- 粒子3 --%>
            <div class="particle"></div>  <%-- 粒子4 --%>
            <div class="particle"></div>  <%-- 粒子5 --%>
            <div class="particle"></div>  <%-- 粒子6 --%>
            <div class="particle"></div>  <%-- 粒子7 --%>
            <div class="particle"></div>  <%-- 粒子8 --%>
            <div class="particle"></div>  <%-- 粒子9 --%>
            <div class="particle"></div>  <%-- 粒子10 --%>
            <%-- 旋转几何形状装饰 --%>
            <div class="geo-shape"></div>  <%-- 方形1 --%>
            <div class="geo-shape"></div>  <%-- 圆形1 --%>
            <div class="geo-shape"></div>  <%-- 方形2 --%>
            <div class="geo-shape"></div>  <%-- 圆形2 --%>
        </div>
        <%-- 品牌核心内容区域 --%>
        <div class="login-left-content">
            <%-- 品牌图标 - 书本图标 --%>
            <div class="brand-icon"><i class="fas fa-book-open"></i></div>
            <%-- 品牌名称 --%>
            <h1><span>BookVerse</span></h1>
            <%-- 品牌标语 --%>
            <p class="slogan">欢迎回来<br>继续你的阅读之旅</p>
            <%-- 终端风格装饰文字 --%>
            <div class="terminal-line">&gt; ACCESS_GRANTED_</div>
            <%-- 统计数据展示 - 图书数量、用户数量、好评率 --%>
            <div class="login-left-stats">
                <div class="stat-item">
                    <div class="stat-number">10万+</div>  <%-- 图书数量 --%>
                    <div class="stat-label">正版图书</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">50万+</div>  <%-- 用户数量 --%>
                    <div class="stat-label">忠实读者</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">99.6%</div>  <%-- 好评率 --%>
                    <div class="stat-label">好评率</div>
                </div>
            </div>
        </div>
    </div>

    <%-- ========== 右侧面板：登录表单区域 ========== --%>
    <div class="login-right">
        <div class="login-card">
            <%-- 毛玻璃效果的登录卡片 --%>
            <div class="glass-card">
                <%-- 卡片头部：标题和副标题 --%>
                <div class="login-card-header">
                    <h2><i class="fas fa-right-to-bracket"></i>欢迎登录</h2>  <%-- 登录标题 --%>
                    <p>登录你的 BookVerse 账户</p>  <%-- 副标题 --%>
                </div>

                <%-- 错误提示信息（使用JSTL条件判断） --%>
                <c:if test="${not empty error}">
                    <div class="alert-danger"><i class="fas fa-triangle-exclamation"></i><c:out value="${error}"/></div>
                </c:if>

                <%-- 成功提示信息（使用JSTL条件判断） --%>
                <c:if test="${not empty msg}">
                    <div class="alert-success"><i class="fas fa-circle-check"></i><c:out value="${msg}"/></div>
                </c:if>

                <%-- ========== 登录表单 ========== --%>
                <%-- 表单提交方式：POST，提交到/login路径 --%>
                <form id="loginForm" action="${pageContext.request.contextPath}/login" method="post">
                    <%-- 错误提示（备用显示方式） --%>
                    <c:if test="${not empty error}">
                        <div style="color:#ff4757;background:rgba(255,71,87,0.1);border:1px solid rgba(255,71,87,0.3);border-radius:8px;padding:10px 15px;margin-bottom:15px;font-size:14px;">
                            <i class="fas fa-exclamation-circle"></i> ${error}
                        </div>
                    </c:if>

                    <%-- 用户名输入框 --%>
                    <div class="input-group-custom">
                        <div class="input-wrapper">
                            <input type="text" name="userid" id="userid" placeholder=" " required>  <%-- 用户名输入框，必填 --%>
                            <div class="input-glow"></div>  <%-- 聚焦时的发光效果 --%>
                            <span class="input-icon-left"><i class="fas fa-user"></i></span>  <%-- 用户图标 --%>
                            <label class="floating-label">用户名</label>  <%-- 浮动标签 --%>
                        </div>
                    </div>

                    <%-- 密码输入框 --%>
                    <div class="input-group-custom">
                        <div class="input-wrapper">
                            <input type="password" name="password" id="password" placeholder=" " required>  <%-- 密码输入框，必填 --%>
                            <div class="input-glow"></div>  <%-- 聚焦时的发光效果 --%>
                            <span class="input-icon-left"><i class="fas fa-lock"></i></span>  <%-- 锁图标 --%>
                            <label class="floating-label">密码</label>  <%-- 浮动标签 --%>
                            <%-- 密码显示/隐藏切换按钮 --%>
                            <button type="button" class="toggle-password" onclick="togglePassword('password', this)" aria-label="显示密码">
                                <i class="fas fa-eye"></i>  <%-- 眼睛图标（显示密码） --%>
                                <i class="fas fa-eye-slash" style="display:none"></i>  <%-- 闭眼图标（隐藏密码） --%>
                            </button>
                        </div>
                    </div>

                    <%-- 记住我选项 --%>
                    <div class="remember-row">
                        <label class="remember-check">
                            <input type="checkbox" name="remember" value="true">  <%-- 记住我复选框 --%>
                            <span>记住我</span>
                        </label>
                    </div>

                    <%-- 登录按钮 --%>
                    <button type="submit" class="btn-login"><span><i class="fas fa-right-to-bracket"></i> 登 录</span></button>
                </form>

                <%-- 分隔线 --%>
                <div class="divider">OR</div>

                <%-- 注册链接 --%>
                <div class="register-link-row">
                    还没有账号？<a href="${pageContext.request.contextPath}/register">立即注册 <i class="fas fa-arrow-right"></i></a>
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
    var input = document.getElementById(inputId);  // 获取密码输入框
    var eyeOpen = btn.querySelector('.fa-eye');  // 获取睁眼图标
    var eyeClosed = btn.querySelector('.fa-eye-slash');  // 获取闭眼图标
    
    // 切换输入框类型
    if (input.type === 'password') {
        input.type = 'text';  // 显示密码：将类型改为文本
        eyeOpen.style.display = 'none';  // 隐藏睁眼图标
        eyeClosed.style.display = 'inline';  // 显示闭眼图标
    } else {
        input.type = 'password';  // 隐藏密码：将类型改回密码
        eyeOpen.style.display = 'inline';  // 显示睁眼图标
        eyeClosed.style.display = 'none';  // 隐藏闭眼图标
    }
}
</script>
</body>
</html>
