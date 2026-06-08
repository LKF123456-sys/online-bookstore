<%-- ============================================================
     dashboard.jsp — 用户仪表盘页面
     功能：展示用户的个人数据概览，如订单数量、收藏商品、
           优惠券等信息，是用户登录后的个人中心首页。
     说明：需要用户登录，数据从后端动态获取。
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
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        :root {
            --primary: #4361ee;
            --primary-light: #6c7dfe;
            --success: #2ecc71;
            --warning: #f39c12;
            --danger: #e74c3c;
            --gray-500: #95a5a6;
            --bg-dark: #1a1a2e;
            --bg-card: #ffffff;
            --text-muted: #7f8c8d;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background: #f0f2f5;
            font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
        }

        .dashboard-container {
            padding: 20px;
        }

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

        .page-header-custom .time-display {
            color: var(--gray-500);
            font-size: 14px;
        }

        .stat-cards-row {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 18px;
            margin-bottom: 25px;
        }

        .stat-card-dash {
            background: #fff;
            border-radius: 14px;
            padding: 22px 24px;
            position: relative;
            overflow: hidden;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            transition: all 0.3s ease;
            cursor: default;
        }

        .stat-card-dash:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.12);
        }

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

        .stat-card-dash .stat-label {
            font-size: 13px;
            color: var(--gray-500);
            margin-bottom: 6px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .stat-card-dash .stat-value {
            font-size: 32px;
            font-weight: 700;
            color: #2c3e50;
            line-height: 1.2;
        }

        .stat-card-dash .stat-trend {
            font-size: 12px;
            margin-top: 4px;
        }

        .stat-card-dash .stat-trend.up { color: var(--success); }
        .stat-card-dash .stat-trend.down { color: var(--danger); }

        .stat-icon.products { background: rgba(67,97,238,0.12); color: var(--primary); }
        .stat-icon.active { background: rgba(46,204,113,0.12); color: var(--success); }
        .stat-icon.orders { background: rgba(243,156,18,0.12); color: var(--warning); }
        .stat-icon.revenue { background: rgba(231,76,60,0.12); color: var(--danger); }

        .stat-card-dash::after {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 4px;
            height: 100%;
            border-radius: 4px 0 0 4px;
        }

        .stat-card-dash.card-products::after { background: var(--primary); }
        .stat-card-dash.card-active::after { background: var(--success); }
        .stat-card-dash.card-orders::after { background: var(--warning); }
        .stat-card-dash.card-revenue::after { background: var(--danger); }

        .charts-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 18px;
            margin-bottom: 25px;
        }

        .panel-custom {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            overflow: hidden;
        }

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

        .panel-custom .panel-body {
            padding: 20px 22px;
        }

        .donut-chart {
            display: flex;
            align-items: center;
            gap: 30px;
            justify-content: center;
            padding: 20px 0;
        }

        .donut-svg {
            width: 160px;
            height: 160px;
            transform: rotate(-90deg);
        }

        .donut-legend {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .donut-legend-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            color: #555;
        }

        .donut-legend-dot {
            width: 12px;
            height: 12px;
            border-radius: 3px;
            flex-shrink: 0;
        }

        .donut-legend-dot.dot-pending { background: var(--warning); }
        .donut-legend-dot.dot-paid { background: #4facfe; }
        .donut-legend-dot.dot-shipping { background: var(--primary); }
        .donut-legend-dot.dot-completed { background: var(--success); }
        .donut-legend-dot.dot-cancelled { background: var(--danger); }

        .bar-chart-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .bar-chart-item {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 10px;
        }

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

        .bar-chart-item .bar-rank.rank-1 { background: linear-gradient(135deg, #f6d365, #fda085); }
        .bar-chart-item .bar-rank.rank-2 { background: linear-gradient(135deg, #a8c0ff, #8f94fb); }
        .bar-chart-item .bar-rank.rank-3 { background: linear-gradient(135deg, #f5af19, #f12711); }
        .bar-chart-item .bar-rank.rank-other { background: var(--gray-500); }

        .bar-chart-item .bar-name {
            width: 110px;
            font-size: 12px;
            color: #555;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex-shrink: 0;
        }

        .bar-chart-item .bar-track {
            flex: 1;
            height: 22px;
            background: #f0f0f0;
            border-radius: 11px;
            overflow: hidden;
            position: relative;
        }

        .bar-chart-item .bar-fill {
            height: 100%;
            border-radius: 11px;
            background: linear-gradient(90deg, var(--primary), var(--primary-light));
            transition: width 0.6s ease;
            display: flex;
            align-items: center;
            padding-left: 8px;
            font-size: 11px;
            color: #fff;
            font-weight: 600;
            min-width: 40px;
        }

        .bar-fill.color-1 { background: linear-gradient(90deg, #f6d365, #fda085); }
        .bar-fill.color-2 { background: linear-gradient(90deg, #a8c0ff, #8f94fb); }
        .bar-fill.color-3 { background: linear-gradient(90deg, #f5af19, #f12711); }

        .bottom-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 18px;
        }

        .alert-list {
            list-style: none;
            padding: 0;
            margin: 0;
            max-height: 280px;
            overflow-y: auto;
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

        .alert-stock-badge {
            padding: 3px 10px;
            border-radius: 10px;
            font-size: 11px;
            font-weight: 600;
        }

        .alert-stock-badge.danger {
            background: #fdecea;
            color: var(--danger);
        }

        .alert-stock-badge.warning {
            background: #fef3e0;
            color: var(--warning);
        }

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

        .log-time {
            color: var(--gray-500);
            white-space: nowrap;
            flex-shrink: 0;
        }

        .log-op-tag {
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 600;
            flex-shrink: 0;
        }

        .log-op-tag.tag-add { background: #e8f5e9; color: var(--success); }
        .log-op-tag.tag-edit { background: #e3f2fd; color: var(--primary); }
        .log-op-tag.tag-delete { background: #fdecea; color: var(--danger); }
        .log-op-tag.tag-login { background: #f3e5f5; color: #9b59b6; }
        .log-op-tag.tag-other { background: #f5f5f5; color: #777; }

        .log-detail {
            color: #666;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        @keyframes countUp {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .stat-value {
            animation: countUp 0.6s ease-out;
        }

        @media (max-width: 1200px) {
            .stat-cards-row { grid-template-columns: repeat(2, 1fr); }
            .charts-row { grid-template-columns: 1fr; }
            .bottom-row { grid-template-columns: 1fr; }
        }

        @media (max-width: 768px) {
            .stat-cards-row { grid-template-columns: 1fr; }
        }

        .empty-state {
            text-align: center;
            padding: 30px;
            color: var(--gray-500);
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid">
    <div class="row">
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10 dashboard-container">
            <div class="page-header-custom">
                <h2>📊 数据大屏</h2>
                <span class="time-display" id="liveTime"></span>
            </div>

            <div class="stat-cards-row">
                <div class="stat-card-dash card-products">
                    <div class="stat-label">总商品数</div>
                    <div class="stat-value">${totalProducts}</div>
                    <div class="stat-trend up">▲ 管理商品</div>
                    <div class="stat-icon products">📦</div>
                </div>
                <div class="stat-card-dash card-active">
                    <div class="stat-label">活跃商品</div>
                    <div class="stat-value">${activeProducts}</div>
                    <div class="stat-trend up">▲ 在售商品</div>
                    <div class="stat-icon active">✅</div>
                </div>
                <div class="stat-card-dash card-orders">
                    <div class="stat-label">总订单数</div>
                    <div class="stat-value">${totalOrders}</div>
                    <div class="stat-trend up">▲ 全部订单</div>
                    <div class="stat-icon orders">📋</div>
                </div>
                <div class="stat-card-dash card-revenue">
                    <div class="stat-label">总收入</div>
                    <div class="stat-value">¥<fmt:formatNumber value="${totalRevenue}" pattern="#,##0.00"/></div>
                    <div class="stat-trend up">▲ 累计收入</div>
                    <div class="stat-icon revenue">💰</div>
                </div>
            </div>

            <div class="stat-cards-row" style="grid-template-columns: repeat(2, 1fr);">
                <div class="stat-card-dash" style="cursor:pointer;" onclick="location.href='${pageContext.request.contextPath}/admin/message'">
                    <div class="stat-label">未读消息</div>
                    <div class="stat-value" id="adminUnreadMsg">${adminUnreadMsgCount != null ? adminUnreadMsgCount : 0}</div>
                    <div class="stat-trend up">▲ 点击查看</div>
                    <div class="stat-icon" style="background:rgba(67,97,238,0.12);color:var(--primary);">💬</div>
                </div>
                <div class="stat-card-dash" style="cursor:pointer;" onclick="location.href='${pageContext.request.contextPath}/admin/coupon'">
                    <div class="stat-label">优惠券管理</div>
                    <div class="stat-value">${totalCoupons != null ? totalCoupons : 0}</div>
                    <div class="stat-trend up">▲ 发放优惠券</div>
                    <div class="stat-icon" style="background:rgba(243,156,18,0.12);color:var(--warning);">🎫</div>
                </div>
            </div>

            <div class="charts-row">
                <div class="panel-custom">
                    <div class="panel-header">
                        <h4>📈 订单状态分布</h4>
                        <span style="font-size:12px;color:var(--gray-500);">实时统计</span>
                    </div>
                    <div class="panel-body">
                        <c:set var="total" value="${pendingCount + paidCount + shippingCount + completedCount + cancelledCount}"/>
                        <c:set var="total" value="${total > 0 ? total : 1}"/>
                        <div class="donut-chart">
                            <svg class="donut-svg" viewBox="0 0 36 36">
                                <c:set var="circumference" value="100.53"/>
                                <c:set var="pendingDash" value="${pendingCount * circumference / total}"/>
                                <c:set var="paidDash" value="${paidCount * circumference / total}"/>
                                <c:set var="shippingDash" value="${shippingCount * circumference / total}"/>
                                <c:set var="completedDash" value="${completedCount * circumference / total}"/>
                                <c:set var="cancelledDash" value="${cancelledCount * circumference / total}"/>
                                <c:set var="pendingOffset" value="0"/>
                                <c:set var="paidOffset" value="${-pendingDash}"/>
                                <c:set var="shippingOffset" value="${-pendingDash - paidDash}"/>
                                <c:set var="completedOffset" value="${-pendingDash - paidDash - shippingDash}"/>
                                <c:set var="cancelledOffset" value="${-pendingDash - paidDash - shippingDash - completedDash}"/>
                                <circle class="donut-hole" cx="18" cy="18" r="11" fill="#fff"/>
                                <circle class="donut-ring" cx="18" cy="18" r="14" fill="none" stroke="#f0f0f0" stroke-width="4"/>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="var(--warning)" stroke-width="4"
                                        stroke-dasharray="${pendingDash} ${circumference - pendingDash}"
                                        stroke-dashoffset="${-pendingOffset}" stroke-linecap="round"/>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="#4facfe" stroke-width="4"
                                        stroke-dasharray="${paidDash} ${circumference - paidDash}"
                                        stroke-dashoffset="${paidOffset}" stroke-linecap="round"/>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="var(--primary)" stroke-width="4"
                                        stroke-dasharray="${shippingDash} ${circumference - shippingDash}"
                                        stroke-dashoffset="${shippingOffset}" stroke-linecap="round"/>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="var(--success)" stroke-width="4"
                                        stroke-dasharray="${completedDash} ${circumference - completedDash}"
                                        stroke-dashoffset="${completedOffset}" stroke-linecap="round"/>
                                <circle cx="18" cy="18" r="14" fill="none" stroke="var(--danger)" stroke-width="4"
                                        stroke-dasharray="${cancelledDash} ${circumference - cancelledDash}"
                                        stroke-dashoffset="${cancelledOffset}" stroke-linecap="round"/>
                                <text x="18" y="17" text-anchor="middle" font-size="5" fill="#2c3e50" font-weight="bold" transform="rotate(90,18,18)">${totalOrders}</text>
                                <text x="18" y="22" text-anchor="middle" font-size="2.5" fill="var(--gray-500)" transform="rotate(90,18,18)">总订单</text>
                            </svg>
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

                <div class="panel-custom">
                    <div class="panel-header">
                        <h4>🔥 热销图书 TOP10</h4>
                        <span style="font-size:12px;color:var(--gray-500);">按销量排行</span>
                    </div>
                    <div class="panel-body">
                        <c:choose>
                            <c:when test="${not empty bestsellers}">
                                <ul class="bar-chart-list">
                                    <c:set var="maxSales" value="1"/>
                                    <c:forEach items="${bestsellers}" var="book" begin="0" end="0">
                                        <c:set var="maxSales" value="${book.sales > 0 ? book.sales : 1}"/>
                                    </c:forEach>
                                    <c:forEach items="${bestsellers}" var="book" varStatus="vs">
                                        <c:set var="barWidth" value="${book.sales * 100 / maxSales}"/>
                                        <li class="bar-chart-item">
                                            <span class="bar-rank ${vs.index == 0 ? 'rank-1' : (vs.index == 1 ? 'rank-2' : (vs.index == 2 ? 'rank-3' : 'rank-other'))}">${vs.index + 1}</span>
                                            <span class="bar-name" title="${book.name}">${book.name}</span>
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

            <div class="bottom-row">
                <div class="panel-custom">
                    <div class="panel-header">
                        <h4>⚠️ 低库存预警</h4>
                        <a href="${pageContext.request.contextPath}/admin/product/stock" style="font-size:12px;color:var(--primary);">查看全部 →</a>
                    </div>
                    <div class="panel-body" style="padding: 10px 22px;">
                        <c:choose>
                            <c:when test="${not empty lowStockItems}">
                                <ul class="alert-list">
                                    <c:forEach items="${lowStockItems}" var="item">
                                        <li>
                                            <span style="flex:1;">📘 ${item.name}</span>
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
                                            <span class="log-time"><fmt:formatDate value="${log.createTime}" pattern="MM-dd HH:mm"/></span>
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
                                            <span class="log-op-tag ${opClass}">${log.operation}</span>
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
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script>
    function updateTime() {
        var now = new Date();
        var str = now.getFullYear() + '-' +
            String(now.getMonth() + 1).padStart(2, '0') + '-' +
            String(now.getDate()).padStart(2, '0') + ' ' +
            String(now.getHours()).padStart(2, '0') + ':' +
            String(now.getMinutes()).padStart(2, '0') + ':' +
            String(now.getSeconds()).padStart(2, '0');
        $('#liveTime').text(str);
    }
    updateTime();
    setInterval(updateTime, 1000);
</script>
</body>
</html>
