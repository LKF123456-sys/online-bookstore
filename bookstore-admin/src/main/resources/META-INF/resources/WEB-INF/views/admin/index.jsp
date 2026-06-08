<%-- ============================================================
     管理后台首页 (index.jsp)
     功能：管理员登录后的默认着陆页，展示书店运营概况
     主要功能：
       - 欢迎横幅：显示管理员名称、系统状态、实时时钟
       - 统计卡片：商品总数、订单数、用户数、累计收入（带数字滚动动画）
       - 快捷功能入口：12个功能模块的快速跳转卡片（带未读角标）
       - 系统状态指示：数据库、应用服务、安全状态
       - 最新动态面板：最近的操作日志
       - 待处理事项面板：待支付订单、未读消息、低库存预警
       - 热销图书TOP5面板
       - 最新公告面板
     整体风格：赛博朋克深色主题，毛玻璃卡片效果
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>管理后台首页 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-theme.css">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* ==================== CSS变量定义（赛博朋克深色主题色板） ==================== */
        :root {
            --cyber-bg: #0a0e1a;                         /* 页面深色背景 */
            --neon-blue: #00d4ff;                         /* 霓虹蓝（主色） */
            --neon-purple: #a78bfa;                       /* 霓虹紫 */
            --neon-pink: #f472b6;                         /* 霓虹粉 */
            --neon-green: #34d399;                        /* 霓虹绿 */
            --neon-yellow: #fbbf24;                       /* 霓虹黄 */
            --neon-red: #f87171;                          /* 霓虹红 */
            --card-bg: rgba(15, 23, 42, 0.65);           /* 卡片半透明背景 */
            --card-border: rgba(0, 212, 255, 0.12);      /* 卡片边框色（淡蓝） */
            --text-primary: #e2e8f0;                      /* 主文字颜色 */
            --text-muted: #94a3b8;                        /* 辅助文字颜色 */
            --text-dim: #64748b;                          /* 更淡的文字颜色 */
            --sidebar-bg: rgba(10, 14, 26, 0.95);        /* 侧边栏背景 */
            --sidebar-width: 260px;                       /* 侧边栏宽度 */
        }

        /* ==================== 全局重置 ==================== */
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
            background: var(--cyber-bg);
            min-height: 100vh;
            color: var(--text-primary);
            overflow-x: hidden;
        }

        /* ==================== 背景装饰效果 ==================== */
        /* 网格叠加层：模拟科幻界面的网格线 */
        body::before {
            content: '';
            position: fixed;
            inset: 0;
            background-image:
                linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
                linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
            background-size: 60px 60px;
            pointer-events: none;
            z-index: 0;
        }

        /* 扫描线效果：模拟老式显示器的水平线条 */
        body::after {
            content: '';
            position: fixed;
            inset: 0;
            background: repeating-linear-gradient(
                0deg,
                transparent,
                transparent 2px,
                rgba(0, 0, 0, 0.04) 2px,
                rgba(0, 0, 0, 0.04) 4px
            );
            pointer-events: none;
            z-index: 9999;
        }

        /* ==================== 页面整体布局（左侧边栏 + 右主内容） ==================== */
        .admin-wrapper {
            display: flex;
            min-height: 100vh;
            position: relative;
            z-index: 1;
        }

        /* ==================== 侧边栏样式 ==================== */
        .sidebar {
            width: var(--sidebar-width);
            background: var(--sidebar-bg);
            border-right: 1px solid var(--card-border);
            position: fixed;
            top: 0;
            left: 0;
            bottom: 0;
            z-index: 100;
            display: flex;
            flex-direction: column;
            backdrop-filter: blur(20px);        /* 毛玻璃效果 */
            -webkit-backdrop-filter: blur(20px);
            overflow-y: auto;
        }

        /* 侧边栏顶部品牌区域 */
        .sidebar-brand {
            padding: 28px 24px 24px;
            border-bottom: 1px solid var(--card-border);
            text-align: center;
        }

        /* 品牌Logo文字（带霓虹发光效果） */
        .sidebar-brand .brand-logo {
            font-size: 22px;
            font-weight: 800;
            color: var(--neon-blue);
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            text-shadow: 0 0 20px rgba(0, 212, 255, 0.5), 0 0 40px rgba(0, 212, 255, 0.2);
            letter-spacing: 1px;
            transition: text-shadow 0.3s;
        }

        /* Logo悬浮时增强发光 */
        .sidebar-brand .brand-logo:hover {
            text-shadow: 0 0 30px rgba(0, 212, 255, 0.8), 0 0 60px rgba(0, 212, 255, 0.4);
        }

        /* Logo图标脉冲动画 */
        .sidebar-brand .brand-logo i {
            font-size: 26px;
            animation: logoPulse 3s ease-in-out infinite;
        }

        /* Logo脉冲动画关键帧 */
        @keyframes logoPulse {
            0%, 100% { filter: drop-shadow(0 0 6px rgba(0, 212, 255, 0.6)); }
            50% { filter: drop-shadow(0 0 14px rgba(0, 212, 255, 0.9)); }
        }

        /* 副标题"Admin Console" */
        .sidebar-brand .brand-sub {
            display: block;
            font-size: 11px;
            color: var(--text-dim);
            margin-top: 6px;
            letter-spacing: 3px;
            text-transform: uppercase;
        }

        /* 侧边栏导航区域 */
        .sidebar-nav {
            flex: 1;
            padding: 16px 12px;
        }

        /* 导航分组标题（如"概览"、"商品"、"交易"） */
        .nav-section-title {
            font-size: 10px;
            font-weight: 700;
            color: var(--text-dim);
            text-transform: uppercase;
            letter-spacing: 2px;
            padding: 12px 14px 8px;
        }

        /* 导航链接基础样式 */
        .nav-link {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 11px 14px;
            color: var(--text-muted);
            text-decoration: none;
            border-radius: 10px;
            margin-bottom: 2px;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.25s;
            position: relative;
        }

        .nav-link i {
            width: 20px;
            text-align: center;
            font-size: 15px;
            transition: color 0.25s;
        }

        /* 导航链接悬浮效果 */
        .nav-link:hover {
            background: rgba(0, 212, 255, 0.06);
            color: var(--text-primary);
        }

        .nav-link:hover i {
            color: var(--neon-blue);
        }

        /* 当前活跃的导航链接（高亮） */
        .nav-link.active {
            background: linear-gradient(135deg, rgba(0, 212, 255, 0.12), rgba(167, 139, 250, 0.12));
            color: var(--neon-blue);
            border: 1px solid rgba(0, 212, 255, 0.2);
        }

        .nav-link.active i {
            color: var(--neon-blue);
            text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
        }

        /* 活跃链接左侧发光指示条 */
        .nav-link.active::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 3px;
            height: 20px;
            background: var(--neon-blue);
            border-radius: 0 3px 3px 0;
            box-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
        }

        /* 侧边栏底部（查看商城 + 退出登录） */
        .sidebar-footer {
            padding: 16px 12px;
            border-top: 1px solid var(--card-border);
        }

        .logout-form {
            margin: 0;
        }

        /* 退出登录按钮 */
        .logout-btn {
            display: flex;
            align-items: center;
            gap: 12px;
            width: 100%;
            padding: 11px 14px;
            background: rgba(248, 113, 113, 0.08);
            border: 1px solid rgba(248, 113, 113, 0.15);
            color: var(--neon-red);
            border-radius: 10px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.25s;
        }

        .logout-btn:hover {
            background: rgba(248, 113, 113, 0.15);
            border-color: rgba(248, 113, 113, 0.3);
            box-shadow: 0 0 20px rgba(248, 113, 113, 0.1);
        }

        .logout-btn i {
            width: 20px;
            text-align: center;
        }

        /* ==================== 主内容区域 ==================== */
        .main-content {
            flex: 1;
            margin-left: var(--sidebar-width);    /* 给侧边栏留空间 */
            padding: 28px 32px;
            min-height: 100vh;
        }

        /* ==================== 欢迎横幅样式 ==================== */
        .welcome-banner {
            background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%);
            border-radius: 20px;
            padding: 36px 40px;
            position: relative;
            overflow: hidden;
            margin-bottom: 28px;
            border: 1px solid var(--card-border);
            box-shadow: 0 0 40px rgba(0, 212, 255, 0.05), inset 0 1px 0 rgba(0, 212, 255, 0.1);
        }

        /* 横幅右上角装饰光晕 */
        .welcome-banner::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -10%;
            width: 400px;
            height: 400px;
            background: radial-gradient(circle, rgba(0, 212, 255, 0.08) 0%, transparent 70%);
            border-radius: 50%;
            animation: bannerFloat 8s ease-in-out infinite;
        }

        /* 横幅左下角装饰光晕 */
        .welcome-banner::after {
            content: '';
            position: absolute;
            bottom: -40%;
            left: 20%;
            width: 300px;
            height: 300px;