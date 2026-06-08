<%-- ============================================================
     数据大屏页面 (dashboard.jsp)
     功能：以可视化图表形式展示书店运营数据概览
     主要功能：
       - 顶部统计卡片：商品总数、活跃商品、订单数、总收入、未读消息、优惠券数
       - 订单状态分布：SVG圆环图展示待支付/已支付/已发货/已完成/已取消的占比
       - 热销图书TOP10：水平柱状图，前三名金银铜渐变色
       - 低库存预警面板：红色/橙色标签提示库存不足的商品
       - 最新操作日志：展示最近的管理员操作记录
       - 实时时钟：页面右上角显示当前时间，每秒更新
     后端传递的数据：
       totalProducts, activeProducts, totalOrders, totalRevenue,
       adminUnreadMsgCount, totalCoupons, pendingCount, paidCount,
       shippingCount, completedCount, cancelledCount, bestsellers,
       lowStockItems, recentLogs
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>数据大屏 - BookVerse 管理后台</title>
    <!-- 引入Bootstrap 3.4.1 样式库 -->
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        /* ==================== CSS变量定义（统一管理颜色主题） ==================== */
        :root {
            --primary: #4361ee;          /* 主色调-蓝色 */
            --primary-light: #6c7dfe;    /* 主色调-浅蓝色 */
            --success: #2ecc71;          /* 成功色-绿色 */
            --warning: #f39c12;          /* 警告色-橙色 */
            --danger: #e74c3c;           /* 危险色-红色 */
            --gray-500: #95a5a6;         /* 灰色-用于辅助文字 */
            --bg-dark: #1a1a2e;          /* 深色背景 */
            --bg-card: #ffffff;          /* 卡片背景白色 */
            --text-muted: #7f8c8d;       /* 弱化文字颜色 */
        }

        /* ==================== 全局重置与基础样式 ==================== */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background: #f0f2f5;       /* 页面浅灰色背景 */
            font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
        }

        /* ==================== 页面容器与头部 ==================== */
        .dashboard-container {
            padding: 20px;
        }

        /* 页面头部：标题和时间左右分布 */
        .page-header-custom {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }

        .page-header-custom h2 {
            font-size: 24px;
            font-weight: 700;
            color: #2c3e50;
            margin: 0;
        }

        /* 右上角实时时间显示 */
        .page-header-custom .time-display {
            color: var(--gray-500);
            font-size: 14px;
        }

        /* ==================== 顶部统计卡片行（4列网格布局） ==================== */
        .stat-cards-row {
            display: grid;
            grid-template-columns: repeat(4, 1fr);  /* 4列等宽 */
            gap: 18px;
            margin-bottom: 25px;
        }

        /* 单个统计卡片样式 */
        .stat-card-dash {
            background: #fff;
            border-radius: 14px;
            padding: 22px 24px;
            position: relative;
            overflow: hidden;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            transition: all 0.3s ease;    /* 悬浮动画过渡 */
            cursor: default;
        }

        /* 卡片悬浮效果：轻微上移 + 加深阴影 */
        .stat-card-dash:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.12);
        }

        /* 卡片右侧图标区域 */
        .stat-card-dash .stat-icon {
            position: absolute;
            right: 20px;
            top: 50%;
            transform: translateY(-50%);
            width: 56px;
            height: 56px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 26px;
        }

        /* 卡片标签文字（如"总商品数"） */
        .stat-card-dash .stat-label {
            font-size: 13px;
            color: var(--gray-500);
            margin-bottom: 6px;
            text-transform: uppercase;   /* 英文大写 */
            letter-spacing: 0.5px;
        }

        /* 卡片数值（大号加粗数字） */
        .stat-card-dash .stat-value {
            font-size: 32px;
            font-weight: 700;
            color: #2c3e50;
            line-height: 1.2;
        }

        /* 卡片趋势文字（如"▲ 管理商品"） */
        .stat-card-dash .stat-trend {
            font-size: 12px;
            margin-top: 4px;
        }

        .stat-card-dash .stat-trend.up { color: var(--success); }    /* 上升趋势绿色 */
        .stat-card-dash .stat-trend.down { color: var(--danger); }   /* 下降趋势红色 */

        /* 各卡片图标背景色 */
        .stat-icon.products { background: rgba(67,97,238,0.12); color: var(--primary); }
        .stat-icon.active { background: rgba(46,204,113,0.12); color: var(--success); }
        .stat-icon.orders { background: rgba(243,156,18,0.12); color: var(--warning); }
        .stat-icon.revenue { background: rgba(231,76,60,0.12); color: var(--danger); }

        /* 卡片左侧彩色边条（4px宽） */
        .stat-card-dash::after {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 4px;
            height: 100%;
            border-radius: 4px 0 0 4px;
        }

        /* 不同卡片的左侧边条颜色 */
        .stat-card-dash.card-products::after { background: var(--primary); }
        .stat-card-dash.card-active::after { background: var(--success); }
        .stat-card-dash.card-orders::after { background: var(--warning); }
        .stat-card-dash.card-revenue::after { background: var(--danger); }

        /* ==================== 中部图表行（2列布局） ==================== */
        .charts-row {
            display: grid;
            grid-template-columns: 1fr 1fr;   /* 左右各占一半 */
            gap: 18px;
            margin-bottom: 25px;
        }

        /* 自定义面板（圆角白色卡片） */
        .panel-custom {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            overflow: hidden;
        }

        /* 面板头部（标题栏） */
        .panel-custom .panel-header {
            padding: 18px 22px;
            border-bottom: 1px solid #f0f0f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .panel-custom .panel-header h4 {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
            color: #2c3e50;
        }

        /* 面板内容区域 */
        .panel-custom .panel-body {
            padding: 20px 22px;
        }

        /* ==================== 圆环图（订单状态分布）样式 ==================== */
        .donut-chart {
            display: flex;
            align-items: center;
            gap: 30px;
            justify-content: center;
            padding: 20px 0;
        }

        /* SVG圆环图：旋转-90度让起始点从顶部开始 */
        .donut-svg {
            width: 160px;
            height: 160px;
            transform: rotate(-90deg);
        }

        /* 图例容器 */
        .donut-legend {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        /* 单个图例项 */
        .donut-legend-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            color: #555;
        }

        /* 图例色块圆点 */
        .donut-legend-dot {
            width: 12px;
            height: 12px;
            border-radius: 3px;
            flex-shrink: 0;
        }

        /* 各状态对应的颜色 */
        .donut-legend-dot.dot-pending { background: var(--warning); }    /* 待支付-橙色 */
        .donut-legend-dot.dot-paid { background: #4facfe; }              /* 已支付-浅蓝 */
        .donut-legend-dot.dot-shipping { background: var(--primary); }    /* 已发货-蓝色 */
        .donut-legend-dot.dot-completed { background: var(--success); }   /* 已完成-绿色 */
        .donut-legend-dot.dot-cancelled { background: var(--danger); }    /* 已取消-红色 */

        /* ==================== 柱状图列表（热销图书TOP10）样式 ==================== */
        .bar-chart-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        /* 每一行柱状图项 */
        .bar-chart-item {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 10px;
        }

        /* 排名圆形数字标签 */
        .bar-chart-item .bar-rank {
            width: 26px;
            height: 26px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            font-weight: 700;
            color: #fff;
            flex-shrink: 0;
        }

        /* 前三名渐变色：第1名金橙、第2名蓝紫、第3名红橙 */
        .bar-chart-item .bar-rank.rank-1 { background: linear-gradient(135deg, #f6d365, #fda085); }
        .bar-chart-item .bar-rank.rank-2 { background: linear-gradient(135deg, #a8c0ff, #8f94fb); }
        .bar-chart-item .bar-rank.rank-3 { background: linear-gradient(135deg, #f5af19, #f12711); }
        .bar-chart-item .bar-rank.rank-other { background: var(--gray-500); }   /* 其他名次灰色 */

        /* 书名文字（固定宽度，超长省略） */
        .bar-chart-item .bar-name {
            width: 110px;
            font-size: 12px;
            color: #555;
            overflow: hidden;
            text-overflow: ellipsis;     /* 溢出显示省略号 */
            white-space: nowrap;
            flex-shrink: 0;
        }

        /* 柱状图轨道（灰色背景条） */
        .bar-chart-item .bar-track {
            flex: 1;
            height: 22px;
            background: #f0f0f0;
            border-radius: 11px;
            overflow: hidden;
            position: relative;
        }

        /* 柱状图填充条（蓝色渐变） */
        .bar-chart-item .bar-fill {
            height: 100%;
            border-radius: 11px;
            background: linear-gradient(90deg, var(--primary), var(--primary-light));
            transition: width 0.6s ease;   /* 宽度动画 */
            display: flex;
            align-items: center;
            padding-left: 8px;
            font-size: 11px;
            color: #fff;
            font-weight: 600;
            min-width: 40px;               /* 最小宽度确保数字可见 */
        }

        /* 前三名柱状条特殊渐变色 */
        .bar-fill.color-1 { background: linear-gradient(90deg, #f6d365, #fda085); }
        .bar-fill.color-2 { background: linear-gradient(90deg, #a8c0ff, #8f94fb); }
        .bar-fill.color-3 { background: linear-gradient(90deg, #f5af19, #f12711); }

        /* ==================== 底部行（2列布局：预警 + 日志） ==================== */
        .bottom-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 18px;
        }

        /* ==================== 低库存预警列表样式 ==================== */
        .alert-list {
            list-style: none;
            padding: 0;
            margin: 0;
            max-height: 280px;
            overflow-y: auto;      /* 超出时显示滚动条 */
        }

        .alert-list li {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid #f5f5f5;
            font-size: 13px;
        }

        .alert-list li:last-child { border-bottom: none; }

        /* 库存数量徽章 */
        .alert-stock-badge {
            padding: 3px 10px;
            border-radius: 10px;
            font-size: 11px;
            font-weight: 600;
        }

        /* 库存徽章：危险(红色) 和 警告(橙色) */
        .alert-stock-badge.danger {
            background: #fdecea;
            color: var(--danger);
        }

        .alert-stock-badge.warning {
            background: #fef3e0;
            color: var(--warning);
        }

        /* ==================== 操作日志列表样式 ==================== */
        .log-list {
            list-style: none;
            padding: 0;
            margin: 0;
            max-height: 280px;
            overflow-y: auto;
        }

        .log-list li {
            display: flex;
            align-items: flex-start;
            gap: 12px;
            padding: 10px 0;
            border-bottom: 1px solid #f5f5f5;
            font-size: 12px;
        }

        .log-list li:last-child { border-bottom: none; }

        /* 日志时间戳 */
        .log-time {
            color: var(--gray-500);
            white-space: nowrap;
            flex-shrink: 0;
        }

        /* 操作类型标签（不同操作用不同颜色区分） */
        .log-op-tag {
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 600;
            flex-shrink: 0;
        }

        /* 操作类型颜色：新增=绿、编辑=蓝、删除=红、登录=紫、其他=灰 */
        .log-op-tag.tag-add { background: #e8f5e9; color: var(--success); }
        .log-op-tag.tag-edit { background: #e3f2fd; color: var(--primary); }
        .log-op-tag.tag-delete { background: #fdecea; color: var(--danger); }
        .log-op-tag.tag-login { background: #f3e5f5; color: #9b59b6; }
        .log-op-tag.tag-other { background: #f5f5f5; color: #777; }

        /* 日志详情文字（超长省略） */
        .log-detail {
            color: #666;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        /* ==================== 数字加载动画 ==================== */
        @keyframes countUp {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .stat-value {
            animation: countUp 0.6s ease-out;   /* 数字从下方淡入 */
        }

        /* ==================== 响应式布局适配 ==================== */
        /* 中等屏幕：卡片2列，图表和底部1列 */
        @media (max-width: 1200px) {
            .stat-cards-row { grid-template-columns: repeat(2, 1fr); }
            .charts-row { grid-template-columns: 1fr; }
            .bottom-row { grid-template-columns: 1fr; }
        }

        /* 小屏幕：卡片1列 */
        @media (max-width: 768px) {
            .stat-cards-row { grid-template-columns: 1fr; }
        }

        /* ==================== 空状态提示 ==================== */
        .empty-state {
            text-align: center;
            padding: 30px;
            color: var(--gray-500);
        }
    </style>
</head>
<body>
<%-- ==================== 引入公共头部导航栏 ==================== --%>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid">
    <div class="row">
        <%-- ==================== 引入左侧管理菜单 ==================== --%>
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10 dashboard-container">
            <%-- ==================== 页面标题与实时时钟 ==================== --%>
            <div class="page-header-custom">
                <h2>📊 数据大屏</h2>
                <%-- 实时时钟显示区域，由JS每秒更新 --%>
                <span class="time-display" id="liveTime"></span>
            </div>

            <%-- ==================== 第一行统计卡片（商品、订单、收入） ==================== --%>
            <div class="stat-cards-row">
                <%-- 总商品数卡片 --%>
                <div class="stat-card-dash card-products">
                    <div class="stat-label">总商品数</div>
                    <div class="stat-value">${totalProducts}</div>
                    <div class="stat-trend up">▲ 管理商品</div>
                    <div class="stat-icon products">📦</div>
                </div>
                <%-- 活跃商品（在售）卡片 --%>
                <div class="stat-card-dash card-active">
                    <div class="stat-label">活跃商品</div>
                    <div class="stat-value">${activeProducts}</div>
                    <div class="stat-trend up">▲ 在售商品</div>
                    <div class="stat-icon active">✅</div>
                </div>
                <%-- 总订单数卡片 --%>
                <div class="stat-card-dash card-orders">
                    <div class="stat-label">总订单数</div>
                    <div class="stat-value">${totalOrders}</div>
                    <div class="stat-trend up">▲ 全部订单</div>
                    <div class="stat-icon orders">📋</div>
                </div>
                <%-- 总收入卡片（使用fmt:formatNumber格式化金额） --%>
                <div class="stat-card-dash card-revenue">
                    <div class="stat-label">总收入</div>
                    <div class="stat-value">¥<fmt:formatNumber value="${totalRevenue}" pattern="#,##0.00"/></div>
                    <div class="stat-trend up">▲ 累计收入</div>
                    <div class="stat-icon revenue">💰</div>
                </div>
            </div>

            <%-- ==================== 第二行统计卡片（消息、优惠券） ==================== --%>
            <div class="stat-cards-row" style="grid-template-columns: repeat(2, 1fr);">
                <%-- 未读消息卡片（可点击跳转到消息管理页） --%>
                <div class="stat-card-dash" style="cursor:pointer;" onclick="location.href='${pageContext.request.contextPath}/admin/message'">
                    <div class="stat-label">未读消息</div>
                    <div class="stat-value" id="adminUnreadMsg">${adminUnreadMsgCount != null ? adminUnreadMsgCount : 0}</div>
                    <div class="stat-trend up">▲ 点击查看</div>
                    <div class="stat-icon" style="background:rgba(67,97,238,0.12);color:var(--primary);">💬</div>
                </div>
                <%-- 优惠券管理卡片（可点击跳转到优惠券管理页） --%>
                <div class="stat-card-dash" style="cursor:pointer;" onclick="location.href='${pageContext.request.contextPath}/admin/coupon'">
                    <div class="stat-label">优惠券管理</div>
                    <div class="stat-value">${totalCoupons != null ? totalCoupons : 0}</div>
                    <div class="stat-trend up">▲ 发放优惠券</div>
                    <div class="stat-icon" style="background:rgba(243,156,18,0.12);color:var(--warning);">🎫</div>
                </div>
            </div>

            <%-- ==================== 中部图表区域（订单分布 + 热销排行） ==================== --%>
            <div class="charts-row">
                <%-- 左侧：订单状态分布圆环图 --%>
                <div class="panel-custom">
                    <div class="panel-header">
                        <h4>📈 订单状态分布</h4>
                        <span style="font-size:12px;color:var(--gray-500);">实时统计</span>
                    </div>
                    <div class="panel-body">
                        <%-- 计算订单总数（防除以0） --%>
                        <c:set var="total" value="${pendingCount + paidCount + shippingCount + completedCount + cancelledCount}"/>
                        <c:set var="total" value="${total > 0 ? total : 1}"/>
                        <div class="donut-chart">
                            <%-- SVG圆环图：每个circle代表一种订单状态的弧段 --%>
                            <svg class="donut-svg" viewBox="0 0 36 36">
                                <%-- 圆的周长 = 2πr = 2×3.14159×16 ≈ 100.53 --%>
                                <c:set var="circumference" value="100.53"/>
                                <%-- 计算每种状态占用的弧段长度 --%>
                                <c:set var="pendingDash" value="${pendingCount * circumference / total}"/>
                                <c:set var="paidDash" value="${paidCount * circumference / total}"/>
                                <c:set var="shippingDash" value="${shippingCount * circumference / total}"/>
                                <c:set var="completedDash" value="${completedCount * circumference / total}"/>
                                <c:set var="cancelledDash" value="${cancelledCount * circumference / total}"/>
                                <%-- 计算每种状态的偏移量（累积偏移，使弧段依次排列） --%>
                                <c:set var="pendingOffset" value="0"/>
                                <c:set var="paidOffset" value="${-pendingDash}"/>
                                <c:set var="shippingOffset" value="${-pendingDash - paidDash}"/>
                                <c:set var="completedOffset" value="${-pendingDash - paidDash - shippingDash}"/>
                                <c:set var="cancelledOffset" value="${-pendingDash - paidDash - shippingDash - completedDash}"/>
                                <%-- 中心白色圆（空心效果） --%>
                                <circle class="donut-hole" cx="18" cy="18" r="11" fill="#fff"/>
                                <%-- 底层灰色轨道圆 --%>
                                <circle class="donut-ring" cx="18" cy="18" r="14" fill="none" stroke="#f0f0f0" stroke-width="4"/>
                                <%-- 待支付弧段（橙色） --%>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="var(--warning)" stroke-width="4"
                                        stroke-dasharray="${pendingDash} ${circumference - pendingDash}"
                                        stroke-dashoffset="${-pendingOffset}" stroke-linecap="round"/>
                                <%-- 已支付弧段（浅蓝） --%>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="#4facfe" stroke-width="4"
                                        stroke-dasharray="${paidDash} ${circumference - paidDash}"
                                        stroke-dashoffset="${paidOffset}" stroke-linecap="round"/>
                                <%-- 已发货弧段（蓝色） --%>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="var(--primary)" stroke-width="4"
                                        stroke-dasharray="${shippingDash} ${circumference - shippingDash}"
                                        stroke-dashoffset="${shippingOffset}" stroke-linecap="round"/>
                                <%-- 已完成弧段（绿色） --%>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="var(--success)" stroke-width="4"
                                        stroke-dasharray="${completedDash} ${circumference - completedDash}"
                                        stroke-dashoffset="${completedOffset}" stroke-linecap="round"/>
                                <%-- 已取消弧段（红色） --%>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="var(--danger)" stroke-width="4"
                                        stroke-dasharray="${cancelledDash} ${circumference - cancelledDash}"
                                        stroke-dashoffset="${cancelledOffset}" stroke-linecap="round"/>
                                <%-- 中心显示的总订单数字和文字 --%>
                                <text x="18" y="17" text-anchor="middle" font-size="5" fill="#2c3e50" font-weight="bold" transform="rotate(90,18,18)">${totalOrders}</text>
                                <text x="18" y="22" text-anchor="middle" font-size="2.5" fill="var(--gray-500)" transform="rotate(90,18,18)">总订单</text>
                            </svg>
                            <%-- 圆环图右侧图例 --%>
                            <div class="donut-legend">
                                <div class="donut-legend-item">
                                    <span class="donut-legend-dot dot-pending"></span>
                                    <span>待支付</span>
                                    <strong>${pendingCount}</strong>
                                </div>
                                <div class="donut-legend-item">
                                    <span class="donut-legend-dot dot-paid"></span>
                                    <span>已支付</span>
                                    <strong>${paidCount}</strong>
                                </div>
                                <div class="donut-legend-item">
                                    <span class="donut-legend-dot dot-shipping"></span>
                                    <span>已发货</span>
                                    <strong>${shippingCount}</strong>
                                </div>
                                <div class="donut-legend-item">
                                    <span class="donut-legend-dot dot-completed"></span>
                                    <span>已完成</span>
                                    <strong>${completedCount}</strong>
                                </div>
                                <div class="donut-legend-item">
                                    <span class="donut-legend-dot dot-cancelled"></span>
                                    <span>已取消</span>
                                    <strong>${cancelledCount}</strong>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- 右侧：热销图书TOP10柱状图 --%>
                <div class="panel-custom">
                    <div class="panel-header">
                        <h4>🔥 热销图书 TOP10</h4>
                        <span style="font-size:12px;color:var(--gray-500);">按销量排行</span>
                    </div>
                    <div class="panel-body">
                        <c:choose>
                            <c:when test="${not empty bestsellers}">
                                <ul class="bar-chart-list">
                                    <%-- 获取最大销量（用于计算柱状条宽度百分比） --%>
                                    <c:set var="maxSales" value="1"/>
                                    <c:forEach items="${bestsellers}" var="book" begin="0" end="0">
                                        <c:set var="maxSales" value="${book.sales > 0 ? book.sales : 1}"/>
                                    </c:forEach>
                                    <%-- 遍历热销图书列表，生成柱状图条目 --%>
                                    <c:forEach items="${bestsellers}" var="book" varStatus="vs">
                                        <c:set var="barWidth" value="${book.sales * 100 / maxSales}"/>
                                        <li class="bar-chart-item">
                                            <%-- 排名标签：前3名特殊颜色，其余灰色 --%>
                                            <span class="bar-rank ${vs.index == 0 ? 'rank-1' : (vs.index == 1 ? 'rank-2' : (vs.index == 2 ? 'rank-3' : 'rank-other'))}">${vs.index + 1}</span>
                                            <%-- 书名（鼠标悬停显示完整书名） --%>
                                            <span class="bar-name" title="${book.name}">${book.name}</span>
                                            <%-- 柱状条（宽度按销量比例计算） --%>
                                            <span class="bar-track">
                                                <span class="bar-fill ${vs.index == 0 ? 'color-1' : (vs.index == 1 ? 'color-2' : (vs.index == 2 ? 'color-3' : ''))}"
                                                      style="width: ${barWidth}%;">${book.sales} 本</span>
                                            </span>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">暂无销售数据</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <%-- ==================== 底部区域（低库存预警 + 操作日志） ==================== --%>
            <div class="bottom-row">
                <%-- 左侧：低库存预警面板 --%>
                <div class="panel-custom">
                    <div class="panel-header">
                        <h4>⚠️ 低库存预警</h4>
                        <a href="${pageContext.request.contextPath}/admin/product/stock" style="font-size:12px;color:var(--primary);">查看全部 →</a>
                    </div>
                    <div class="panel-body" style="padding: 10px 22px;">
                        <c:choose>
                            <c:when test="${not empty lowStockItems}">
                                <ul class="alert-list">
                                    <%-- 遍历低库存商品列表 --%>
                                    <c:forEach items="${lowStockItems}" var="item">
                                        <li>
                                            <span style="flex:1;">📘 ${item.name}</span>
                                            <%-- 库存低于5本显示红色(danger)，5-10本显示橙色(warning) --%>
                                            <span class="alert-stock-badge ${item.stock < 5 ? 'danger' : 'warning'}">
                                                库存: ${item.stock}
                                            </span>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">✅ 所有商品库存充足</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <%-- 右侧：最新操作日志面板 --%>
                <div class="panel-custom">
                    <div class="panel-header">
                        <h4>📝 最新操作日志</h4>
                        <a href="${pageContext.request.contextPath}/admin/log/list" style="font-size:12px;color:var(--primary);">查看全部 →</a>
                    </div>
                    <div class="panel-body" style="padding: 10px 22px;">
                        <c:choose>
                            <c:when test="${not empty recentLogs}">
                                <ul class="log-list">
                                    <c:forEach items="${recentLogs}" var="log">
                                        <li>
                                            <%-- 操作时间（格式：月-日 时:分） --%>
                                            <span class="log-time"><fmt:formatDate value="${log.createTime}" pattern="MM-dd HH:mm"/></span>
                                            <%-- 根据操作类型设置标签颜色 --%>
                                            <c:set var="opClass" value="tag-other"/>
                                            <c:choose>
                                                <c:when test="${log.operation == '新增' || log.operation == '添加'}">
                                                    <c:set var="opClass" value="tag-add"/>
                                                </c:when>
                                                <c:when test="${log.operation == '编辑' || log.operation == '修改' || log.operation == '更新'}">
                                                    <c:set var="opClass" value="tag-edit"/>
                                                </c:when>
                                                <c:when test="${log.operation == '删除'}">
                                                    <c:set var="opClass" value="tag-delete"/>
                                                </c:when>
                                                <c:when test="${log.operation == '登录' || log.operation == '登出'}">
                                                    <c:set var="opClass" value="tag-login"/>
                                                </c:when>
                                            </c:choose>
                                            <%-- 操作类型标签 --%>
                                            <span class="log-op-tag ${opClass}">${log.operation}</span>
                                            <%-- 操作详情：管理员名称 + 操作描述 --%>
                                            <span class="log-detail">${log.adminName} · ${log.detail}</span>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">暂无操作日志</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<%-- ==================== JavaScript部分 ==================== --%>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script>
    /**
     * 更新页面右上角的实时时钟
     * 格式：YYYY-MM-DD HH:MM:SS
     */
    function updateTime() {
        var now = new Date();
        // 拼接日期时间字符串，月份和日期补零
        var str = now.getFullYear() + '-' +
            String(now.getMonth() + 1).padStart(2, '0') + '-' +   // 月份从0开始，需+1
            String(now.getDate()).padStart(2, '0') + ' ' +
            String(now.getHours()).padStart(2, '0') + ':' +
            String(now.getMinutes()).padStart(2, '0') + ':' +
            String(now.getSeconds()).padStart(2, '0');
        $('#liveTime').text(str);    // 更新页面上的时间显示
    }
    updateTime();                           // 页面加载时立即执行一次
    setInterval(updateTime, 1000);          // 每隔1秒更新一次（1000毫秒）
</script>
</body>
</html>
