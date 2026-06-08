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
        :root {
            --cyber-bg: #0a0e1a;
            --neon-blue: #00d4ff;
            --neon-purple: #a78bfa;
            --neon-pink: #f472b6;
            --neon-green: #34d399;
            --neon-yellow: #fbbf24;
            --neon-red: #f87171;
            --card-bg: rgba(15, 23, 42, 0.65);
            --card-border: rgba(0, 212, 255, 0.12);
            --text-primary: #e2e8f0;
            --text-muted: #94a3b8;
            --text-dim: #64748b;
            --sidebar-bg: rgba(10, 14, 26, 0.95);
            --sidebar-width: 260px;
        }
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
            background: var(--cyber-bg);
            min-height: 100vh;
            color: var(--text-primary);
            overflow-x: hidden;
        }
        body::before {
            content: '';
            position: fixed; inset: 0;
            background-image:
                linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
                linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
            background-size: 60px 60px;
            pointer-events: none; z-index: 0;
        }
        body::after {
            content: '';
            position: fixed; inset: 0;
            background: repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0,0,0,0.04) 2px, rgba(0,0,0,0.04) 4px);
            pointer-events: none; z-index: 9999;
        }
        .admin-wrapper { display: flex; min-height: 100vh; position: relative; z-index: 1; }
        .sidebar {
            width: var(--sidebar-width);
            background: var(--sidebar-bg);
            border-right: 1px solid var(--card-border);
            position: fixed; top: 0; left: 0; bottom: 0;
            z-index: 100;
            display: flex; flex-direction: column;
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            overflow-y: auto;
        }
        .sidebar-brand { padding: 28px 24px 24px; border-bottom: 1px solid var(--card-border); text-align: center; }
        .sidebar-brand .brand-logo {
            font-size: 22px; font-weight: 800; color: var(--neon-blue);
            text-decoration: none;
            display: inline-flex; align-items: center; gap: 10px;
            text-shadow: 0 0 20px rgba(0, 212, 255, 0.5), 0 0 40px rgba(0, 212, 255, 0.2);
            letter-spacing: 1px; transition: text-shadow 0.3s;
        }
        .sidebar-brand .brand-logo:hover { text-shadow: 0 0 30px rgba(0, 212, 255, 0.8), 0 0 60px rgba(0, 212, 255, 0.4); }
        .sidebar-brand .brand-logo i { font-size: 26px; animation: logoPulse 3s ease-in-out infinite; }
        @keyframes logoPulse {
            0%, 100% { filter: drop-shadow(0 0 6px rgba(0, 212, 255, 0.6)); }
            50% { filter: drop-shadow(0 0 14px rgba(0, 212, 255, 0.9)); }
        }
        .sidebar-brand .brand-sub { display: block; font-size: 11px; color: var(--text-dim); margin-top: 6px; letter-spacing: 3px; text-transform: uppercase; }
        .sidebar-nav { flex: 1; padding: 16px 12px; }
        .nav-section-title {
            font-size: 10px; font-weight: 700; color: var(--text-dim);
            text-transform: uppercase; letter-spacing: 2px; padding: 12px 14px 8px;
        }
        .nav-link {
            display: flex; align-items: center; gap: 12px;
            padding: 11px 14px; color: var(--text-muted);
            text-decoration: none; border-radius: 10px; margin-bottom: 2px;
            font-size: 14px; font-weight: 500; transition: all 0.25s; position: relative;
        }
        .nav-link i { width: 20px; text-align: center; font-size: 15px; transition: color 0.25s; }
        .nav-link:hover { background: rgba(0, 212, 255, 0.06); color: var(--text-primary); }
        .nav-link:hover i { color: var(--neon-blue); }
        .nav-link.active {
            background: linear-gradient(135deg, rgba(0, 212, 255, 0.12), rgba(167, 139, 250, 0.12));
            color: var(--neon-blue); border: 1px solid rgba(0, 212, 255, 0.2);
        }
        .nav-link.active i { color: var(--neon-blue); text-shadow: 0 0 10px rgba(0, 212, 255, 0.5); }
        .nav-link.active::before {
            content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%);
            width: 3px; height: 20px; background: var(--neon-blue);
            border-radius: 0 3px 3px 0; box-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
        }
        .sidebar-footer { padding: 16px 12px; border-top: 1px solid var(--card-border); }
        .logout-form { margin: 0; }
        .logout-btn {
            display: flex; align-items: center; gap: 12px;
            width: 100%; padding: 11px 14px;
            background: rgba(248, 113, 113, 0.08);
            border: 1px solid rgba(248, 113, 113, 0.15);
            color: var(--neon-red); border-radius: 10px;
            font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.25s;
        }
        .logout-btn:hover {
            background: rgba(248, 113, 113, 0.15);
            border-color: rgba(248, 113, 113, 0.3);
            box-shadow: 0 0 20px rgba(248, 113, 113, 0.1);
        }
        .logout-btn i { width: 20px; text-align: center; }
        .main-content { flex: 1; margin-left: var(--sidebar-width); padding: 28px 32px; min-height: 100vh; }
        .welcome-banner {
            background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%);
            border-radius: 20px; padding: 36px 40px; position: relative; overflow: hidden;
            margin-bottom: 28px; border: 1px solid var(--card-border);
            box-shadow: 0 0 40px rgba(0, 212, 255, 0.05), inset 0 1px 0 rgba(0, 212, 255, 0.1);
        }
        .welcome-banner::before {
            content: ''; position: absolute; top: -50%; right: -10%;
            width: 400px; height: 400px;
            background: radial-gradient(circle, rgba(0, 212, 255, 0.08) 0%, transparent 70%);
            border-radius: 50%; animation: bannerFloat 8s ease-in-out infinite;
        }
        .welcome-banner::after {
            content: ''; position: absolute; bottom: -40%; left: 20%;
            width: 300px; height: 300px;
            background: radial-gradient(circle, rgba(167, 139, 250, 0.06) 0%, transparent 70%);
            border-radius: 50%; animation: bannerFloat 12s ease-in-out infinite reverse;
        }
        @keyframes bannerFloat { 0%,100%{transform:translate(0,0) scale(1)} 50%{transform:translate(-20px,-10px) scale(1.05)} }
        .welcome-banner h1 { font-size: 26px; font-weight: 700; color: var(--text-primary); position: relative; z-index: 1; margin-bottom: 8px; }
        .admin-badge {
            display: inline-block; background: linear-gradient(135deg, var(--neon-blue), var(--neon-purple));
            color: #fff; font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 20px;
            margin-left: 12px; vertical-align: middle;
        }
        .status-row { display: flex; gap: 24px; position: relative; z-index: 1; margin-top: 10px; }
        .status-row span { font-size: 13px; color: var(--text-muted); display: flex; align-items: center; gap: 6px; }
        .status-dot { width: 8px; height: 8px; border-radius: 50%; }
        .status-dot.online { background: var(--neon-green); box-shadow: 0 0 8px var(--neon-green); }
        #clock { font-family: 'Courier New', monospace; font-size: 14px; color: var(--neon-blue); }
        .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
        @media (max-width: 1100px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
        .stat-card {
            background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 14px;
            padding: 24px; display: flex; align-items: center; gap: 18px;
            backdrop-filter: blur(16px); transition: transform 0.3s, border-color 0.3s;
        }
        .stat-card:hover { transform: translateY(-3px); border-color: var(--neon-blue); }
        .stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; color: #fff; flex-shrink: 0; }
        .stat-icon.blue { background: linear-gradient(135deg, #0ea5e9, #6366f1); }
        .stat-icon.purple { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }
        .stat-icon.green { background: linear-gradient(135deg, #10b981, #34d399); }
        .stat-icon.orange { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
        .stat-info h3 { font-size: 22px; font-weight: 700; color: var(--text-primary); }
        .stat-info p { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
        .quick-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 14px; margin-bottom: 24px; }
        @media (max-width: 1400px) { .quick-grid { grid-template-columns: repeat(3, 1fr); } }
        .quick-card {
            background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 12px;
            padding: 20px 16px; text-align: center; text-decoration: none; color: var(--text-primary);
            transition: all 0.3s; position: relative; backdrop-filter: blur(12px);
        }
        .quick-card:hover { border-color: var(--neon-blue); transform: translateY(-3px); box-shadow: 0 8px 30px rgba(0,212,255,0.1); text-decoration: none; color: var(--neon-blue); }
        .quick-card i { font-size: 28px; display: block; margin-bottom: 10px; }
        .quick-label { font-size: 13px; font-weight: 500; }
        .quick-badge { position: absolute; top: 8px; right: 8px; background: var(--neon-red); color: #fff; font-size: 11px; font-weight: 700; min-width: 20px; height: 20px; line-height: 20px; border-radius: 10px; padding: 0 6px; text-align: center; }
        .status-bar { display: flex; gap: 24px; margin-bottom: 24px; padding: 14px 24px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 12px; flex-wrap: wrap; }
        .status-item { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--text-muted); }
        .panel-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
        @media (max-width: 1200px) { .panel-row { grid-template-columns: 1fr; } }
        .panel { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 14px; overflow: hidden; backdrop-filter: blur(12px); }
        .panel-header { padding: 16px 20px; border-bottom: 1px solid var(--card-border); font-weight: 600; font-size: 15px; display: flex; align-items: center; gap: 10px; }
        .panel-header i { color: var(--neon-blue); }
        .panel-body { padding: 16px 20px; }
        .info-list { list-style: none; }
        .info-list li { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid rgba(100,116,139,0.08); font-size: 13px; }
        .info-list li:last-child { border-bottom: none; }
        .info-time { color: var(--text-dim); font-size: 12px; }
        .info-badge { font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 500; }
        .info-badge.warn { background: rgba(251,191,36,0.15); color: var(--neon-yellow); }
        .info-badge.info { background: rgba(0,212,255,0.12); color: var(--neon-blue); }
        .info-badge.danger { background: rgba(248,113,113,0.12); color: var(--neon-red); }
        .info-badge.success { background: rgba(52,211,153,0.12); color: var(--neon-green); }
        .simple-table { width: 100%; border-collapse: collapse; }
        .simple-table th, .simple-table td { padding: 10px 12px; text-align: left; font-size: 13px; }
        .simple-table th { color: var(--text-dim); font-weight: 600; border-bottom: 1px solid var(--card-border); }
        .simple-table td { border-bottom: 1px solid rgba(100,116,139,0.06); color: var(--text-muted); }
        .simple-table tr:last-child td { border-bottom: none; }
        .simple-table .rank { font-weight: 700; color: var(--neon-blue); width: 30px; }
        .empty-tip { text-align: center; color: var(--text-dim); padding: 30px 0; font-size: 14px; }
        @media (max-width: 768px) {
            .sidebar { display: none; }
            .main-content { margin-left: 0; padding: 16px; }
            .stats-grid { grid-template-columns: 1fr; }
            .quick-grid { grid-template-columns: repeat(2, 1fr); }
        }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <aside class="sidebar">
        <div class="sidebar-brand">
            <a href="${pageContext.request.contextPath}/admin/index" class="brand-logo">
                <i class="fas fa-shield-halved"></i> BookVerse
            </a>
            <span class="brand-sub">Admin Console</span>
        </div>
        <nav class="sidebar-nav">
            <div class="nav-section-title">概 览</div>
            <a href="${pageContext.request.contextPath}/admin/index" class="nav-link active"><i class="fas fa-gauge-high"></i> 仪表盘</a>
            <div class="nav-section-title">商 品</div>
            <a href="${pageContext.request.contextPath}/admin/product" class="nav-link"><i class="fas fa-box"></i> 商品列表</a>
            <a href="${pageContext.request.contextPath}/admin/product/add" class="nav-link"><i class="fas fa-plus-circle"></i> 添加商品</a>
            <a href="${pageContext.request.contextPath}/admin/product/stock" class="nav-link"><i class="fas fa-warehouse"></i> 库存管理</a>
            <a href="${pageContext.request.contextPath}/admin/categories" class="nav-link"><i class="fas fa-tags"></i> 分类管理</a>
            <a href="${pageContext.request.contextPath}/admin/product/bestseller" class="nav-link"><i class="fas fa-trophy"></i> 热销排行</a>
            <div class="nav-section-title">交 易</div>
            <a href="${pageContext.request.contextPath}/admin/order" class="nav-link"><i class="fas fa-receipt"></i> 订单管理</a>
            <div class="nav-section-title">用 户</div>
            <a href="${pageContext.request.contextPath}/admin/user" class="nav-link"><i class="fas fa-users"></i> 用户列表</a>
            <div class="nav-section-title">营 销</div>
            <a href="${pageContext.request.contextPath}/admin/coupon" class="nav-link"><i class="fas fa-ticket"></i> 优惠券</a>
            <a href="${pageContext.request.contextPath}/admin/review" class="nav-link"><i class="fas fa-star-half-stroke"></i> 评价管理</a>
            <a href="${pageContext.request.contextPath}/admin/announcement" class="nav-link"><i class="fas fa-bullhorn"></i> 公告管理</a>
            <div class="nav-section-title">系 统</div>
            <a href="${pageContext.request.contextPath}/admin/message" class="nav-link"><i class="fas fa-envelope"></i> 消息管理</a>
            <a href="${pageContext.request.contextPath}/admin/log" class="nav-link"><i class="fas fa-history"></i> 操作日志</a>
        </nav>
        <div class="sidebar-footer">
            <form action="${pageContext.request.contextPath}/admin/logout" method="post" class="logout-form">
                <button type="submit" class="logout-btn"><i class="fas fa-right-from-bracket"></i> 退出登录</button>
            </form>
        </div>
    </aside>

    <main class="main-content">
        <div class="welcome-banner">
            <h1>
                欢迎回来，${sessionScope.admin.userid}
                <span class="admin-badge"><i class="fas fa-crown"></i> 管理员</span>
            </h1>
            <div class="status-row">
                <span><span class="status-dot online"></span> 系统运行中</span>
                <span><i class="fas fa-clock"></i> <span id="clock">--:--:--</span></span>
                <span><i class="fas fa-calendar-day"></i> <span id="dateStr"></span></span>
            </div>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon blue"><i class="fas fa-book"></i></div>
                <div class="stat-info"><h3>${productCount}</h3><p>商品总数</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon purple"><i class="fas fa-receipt"></i></div>
                <div class="stat-info"><h3>${totalOrders}</h3><p>订单总数</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon green"><i class="fas fa-user"></i></div>
                <div class="stat-info"><h3>${totalUsers}</h3><p>用户总数</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon orange"><i class="fas fa-yen-sign"></i></div>
                <div class="stat-info"><h3>&yen;${totalRevenue}</h3><p>累计收入</p></div>
            </div>
        </div>

        <div class="quick-grid">
            <a href="${pageContext.request.contextPath}/admin/product/add" class="quick-card">
                <i class="fas fa-plus-square" style="color:var(--neon-blue)"></i>
                <span class="quick-label">添加商品</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/order" class="quick-card">
                <i class="fas fa-clipboard-list" style="color:var(--neon-purple)"></i>
                <span class="quick-label">待处理订单</span>
                <c:if test="${pendingOrders > 0}"><span class="quick-badge">${pendingOrders}</span></c:if>
            </a>
            <a href="${pageContext.request.contextPath}/admin/user" class="quick-card">
                <i class="fas fa-user-plus" style="color:var(--neon-green)"></i>
                <span class="quick-label">用户管理</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/product/stock" class="quick-card">
                <i class="fas fa-warehouse" style="color:var(--neon-yellow)"></i>
                <span class="quick-label">库存预警</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/coupon" class="quick-card">
                <i class="fas fa-ticket" style="color:var(--neon-pink)"></i>
                <span class="quick-label">优惠券</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/log" class="quick-card">
                <i class="fas fa-chart-line" style="color:var(--neon-blue)"></i>
                <span class="quick-label">操作日志</span>
            </a>
        </div>

        <div class="status-bar">
            <span class="status-item"><span class="status-dot online"></span> 数据库</span>
            <span class="status-item"><span class="status-dot online"></span> 应用服务</span>
            <span class="status-item"><i class="fas fa-shield-halved" style="color:var(--neon-green);margin-right:4px;"></i>安全连接</span>
        </div>

        <div class="panel-row">
            <div class="panel">
                <div class="panel-header"><i class="fas fa-rss"></i> 最新动态</div>
                <div class="panel-body">
                    <c:choose>
                        <c:when test="${not empty recentLogs}">
                            <ul class="info-list">
                                <c:forEach items="${recentLogs}" var="log" begin="0" end="4">
                                    <li><span>${log.action}</span><span class="info-time">${log.createTime}</span></li>
                                </c:forEach>
                            </ul>
                        </c:when>
                        <c:otherwise><div class="empty-tip"><i class="fas fa-inbox"></i> 暂无操作记录</div></c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="panel">
                <div class="panel-header"><i class="fas fa-triangle-exclamation"></i> 待处理事项</div>
                <div class="panel-body">
                    <ul class="info-list">
                        <li><span>待支付订单</span><span class="info-badge warn">${pendingPay} 笔</span></li>
                        <li><span>待发货订单</span><span class="info-badge info">${pendingShip} 笔</span></li>
                        <li><span>未读消息</span><span class="info-badge danger">${adminUnreadMsg} 条</span></li>
                        <li><span>低库存商品</span>
                            <c:choose>
                                <c:when test="${not empty lowStockItems}"><span class="info-badge danger">${lowStockItems.size()} 件</span></c:when>
                                <c:otherwise><span class="info-badge success">0 件</span></c:otherwise>
                            </c:choose>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <div class="panel-row">
            <div class="panel">
                <div class="panel-header"><i class="fas fa-fire"></i> 热销图书 TOP5</div>
                <div class="panel-body">
                    <c:choose>
                        <c:when test="${not empty hotProducts}">
                            <table class="simple-table">
                                <thead><tr><th>#</th><th>书名</th><th>销量</th></tr></thead>
                                <tbody>
                                    <c:forEach items="${hotProducts}" var="p" varStatus="s">
                                        <tr><td class="rank">${s.index + 1}</td><td>${p.name}</td><td>${p.salesCount}</td></tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise><div class="empty-tip"><i class="fas fa-book-open"></i> 暂无销售数据</div></c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="panel">
                <div class="panel-header"><i class="fas fa-bullhorn"></i> 最新公告</div>
                <div class="panel-body">
                    <c:choose>
                        <c:when test="${not empty announcementList}">
                            <ul class="info-list">
                                <c:forEach items="${announcementList}" var="ann">
                                    <li><span>${ann.title}</span><span class="info-time">${ann.createTime}</span></li>
                                </c:forEach>
                            </ul>
                        </c:when>
                        <c:otherwise><div class="empty-tip"><i class="fas fa-bullhorn"></i> 暂无公告</div></c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </main>
</div>

<script>
    (function() {
        function updateClock() {
            var now = new Date();
            var h = String(now.getHours()).padStart(2, '0');
            var m = String(now.getMinutes()).padStart(2, '0');
            var s = String(now.getSeconds()).padStart(2, '0');
            var el = document.getElementById('clock');
            if (el) el.textContent = h + ':' + m + ':' + s;
            var de = document.getElementById('dateStr');
            if (de) {
                var days = ['周日','周一','周二','周三','周四','周五','周六'];
                de.textContent = now.getFullYear() + '-' + String(now.getMonth()+1).padStart(2,'0') + '-' +
                    String(now.getDate()).padStart(2,'0') + ' ' + days[now.getDay()];
            }
        }
        updateClock();
        setInterval(updateClock, 1000);
    })();
</script>
</body>
</html>
