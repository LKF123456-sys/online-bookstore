<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的优惠券 - 在线书店</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/css/bootstrap.min.css">
    <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
    <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/js/bootstrap.min.js"></script>
    <style>
        :root {
            --primary: #4f46e5;
            --primary-dark: #3730a3;
            --accent: #f59e0b;
            --success: #10b981;
            --danger: #ef4444;
            --gray-50: #f9fafb;
            --gray-100: #f3f4f6;
            --gray-200: #e5e7eb;
            --gray-300: #d1d5db;
            --gray-500: #6b7280;
            --gray-600: #4b5563;
            --gray-700: #374151;
            --gray-800: #1f2937;
            --white: #ffffff;
            --shadow: 0 1px 6px rgba(0,0,0,0.08);
            --radius: 8px;
            --radius-lg: 12px;
            --radius-xl: 16px;
        }
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: var(--gray-50); color: var(--gray-800); }

        .navbar-custom { background: linear-gradient(135deg, var(--primary), var(--primary-dark)); border: none; border-radius: 0; box-shadow: 0 2px 12px rgba(79,70,229,0.3); }
        .navbar-custom .navbar-brand { color: #fff !important; font-weight: 800; font-size: 20px; }
        .navbar-custom .nav > li > a { color: rgba(255,255,255,0.9) !important; font-weight: 600; transition: all 0.3s; }
        .navbar-custom .nav > li > a:hover { color: #fff !important; background: rgba(255,255,255,0.1) !important; border-radius: 6px; }

        .page-header { background: var(--white); padding: 20px 0; box-shadow: var(--shadow); margin-bottom: 24px; }
        .page-header .container { max-width: 1100px; }
        .page-header h2 { font-size: 22px; font-weight: 700; margin: 0; color: var(--gray-800); }
        .page-header p { color: var(--gray-500); font-size: 14px; margin: 4px 0 0; }

        .container { max-width: 1100px; }

        .tabs { display: flex; gap: 4px; margin-bottom: 20px; background: var(--white); padding: 8px; border-radius: var(--radius-lg); box-shadow: var(--shadow); }
        .tab-btn { padding: 10px 24px; border: none; background: transparent; color: var(--gray-500); font-size: 14px; font-weight: 600; cursor: pointer; border-radius: var(--radius); transition: all 0.2s; }
        .tab-btn:hover { background: var(--gray-100); }
        .tab-btn.active { background: var(--primary); color: #fff; }
        .tab-btn .badge { background: var(--danger); color: #fff; font-size: 11px; padding: 2px 6px; border-radius: 10px; margin-left: 6px; }
        .tab-btn.active .badge { background: rgba(255,255,255,0.3); }

        .tab-panel { display: none; }
        .tab-panel.active { display: block; }

        .coupon-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }

        .coupon-card { background: var(--white); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow); transition: all 0.3s; position: relative; }
        .coupon-card:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(0,0,0,0.1); }
        .coupon-card.used { opacity: 0.6; }
        .coupon-card.expired { opacity: 0.5; }

        .coupon-left { width: 100px; background: linear-gradient(135deg, var(--primary), var(--primary-dark)); color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px 10px; position: relative; }
        .coupon-left .discount { font-size: 24px; font-weight: 800; }
        .coupon-left .unit { font-size: 12px; opacity: 0.8; }
        .coupon-left .threshold { font-size: 11px; opacity: 0.7; margin-top: 4px; }
        .coupon-card.used .coupon-left { background: linear-gradient(135deg, var(--gray-500), var(--gray-600)); }
        .coupon-card.expired .coupon-left { background: linear-gradient(135deg, var(--gray-400), var(--gray-500)); }

        .coupon-right { padding: 16px 20px; flex: 1; }
        .coupon-name { font-size: 16px; font-weight: 700; color: var(--gray-800); margin-bottom: 8px; }
        .coupon-desc { font-size: 13px; color: var(--gray-500); margin-bottom: 12px; }
        .coupon-time { font-size: 12px; color: var(--gray-400); }
        .coupon-status { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 11px; font-weight: 600; }
        .coupon-status.available { background: #d1fae5; color: #065f46; }
        .coupon-status.used { background: var(--gray-200); color: var(--gray-500); }
        .coupon-status.expired { background: #fee2e2; color: #991b1b; }

        .coupon-right::after { content: ''; position: absolute; right: 0; top: 0; bottom: 0; width: 2px; background: repeating-linear-gradient(to bottom, var(--gray-300) 0, var(--gray-300) 4px, transparent 4px, transparent 8px); }

        .empty-state { text-align: center; padding: 60px 20px; color: var(--gray-500); }
        .empty-state .icon { font-size: 48px; margin-bottom: 12px; }

        .footer-custom { background: var(--gray-800); color: var(--gray-300); padding: 30px 0 20px; margin-top: 60px; }
        .footer-bottom { border-top: 1px solid var(--gray-700); margin-top: 20px; padding-top: 15px; text-align: center; color: var(--gray-500); font-size: 13px; }

        .btn-back-home { background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important; position: relative; overflow: hidden; }
        .btn-back-home::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent); transition: left 0.5s; }
        .btn-back-home:hover { transform: translateY(-2px) !important; box-shadow: 0 8px 25px rgba(79,70,229,0.4) !important; color: #fff !important; text-decoration: none !important; }
        .btn-back-home:hover::before { left: 100%; }
        .btn-back-home:active { transform: translateY(0) !important; }

        @media (max-width: 768px) {
            .coupon-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>

<nav class="navbar navbar-default navbar-custom navbar-fixed-top">
    <div class="container" style="max-width:1100px;">
        <div class="navbar-header">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">📚 BookVerse</a>
        </div>
        <ul class="nav navbar-nav navbar-right">
            <li><a href="${pageContext.request.contextPath}/">返回首页</a></li>
            <li><a href="${pageContext.request.contextPath}/orders">我的订单</a></li>
            <li><a href="${pageContext.request.contextPath}/message">消息中心</a></li>
        </ul>
    </div>
</nav>

<div style="height:70px;"></div>

<div class="page-header">
    <div class="container">
        <div style="display:flex;align-items:center;justify-content:space-between;">
            <div>
                <h2>🎫 我的优惠券</h2>
                <p>查看和管理您所有的优惠券</p>
            </div>
            <a href="${pageContext.request.contextPath}/" class="btn-back-home" style="display:inline-flex;align-items:center;gap:8px;padding:12px 28px;background:linear-gradient(135deg,var(--primary),var(--primary-dark));color:#fff;border:none;border-radius:50px;font-size:14px;font-weight:600;cursor:pointer;text-decoration:none;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);box-shadow:0 4px 15px rgba(79,70,229,0.3);position:relative;overflow:hidden;">
                <span style="position:relative;z-index:1;display:flex;align-items:center;gap:8px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/></svg>
                    返回首页
                </span>
            </a>
        </div>
    </div>
</div>

<div class="container">
    <div class="tabs">
        <button class="tab-btn active" onclick="switchTab('available', this)">
            可使用
            <c:if test="${not empty availableCoupons}">
                <span class="badge">${fn:length(availableCoupons)}</span>
            </c:if>
        </button>
        <button class="tab-btn" onclick="switchTab('used', this)">
            已使用
            <c:if test="${not empty usedCoupons}">
                <span class="badge">${fn:length(usedCoupons)}</span>
            </c:if>
        </button>
        <button class="tab-btn" onclick="switchTab('expired', this)">
            已过期
            <c:if test="${not empty expiredCoupons}">
                <span class="badge">${fn:length(expiredCoupons)}</span>
            </c:if>
        </button>
    </div>

    <!-- 可使用 -->
    <div id="tab-available" class="tab-panel active">
        <div class="coupon-grid">
            <c:if test="${empty availableCoupons}">
                <div class="empty-state" style="grid-column: 1/-1;">
                    <div class="icon">🎫</div>
                    <p>暂无可用优惠券</p>
                </div>
            </c:if>
            <c:forEach var="c" items="${availableCoupons}">
                <div class="coupon-card">
                    <div style="display:flex;">
                        <div class="coupon-left">
                            <span class="unit">¥</span>
                            <span class="discount"><fmt:formatNumber value="${c.discount}" pattern="#0"/></span>
                            <span class="threshold">满<fmt:formatNumber value="${c.threshold}" pattern="#0"/>可用</span>
                        </div>
                        <div class="coupon-right" style="position:relative;">
                            <div class="coupon-name">${c.name}</div>
                            <c:choose>
                                <c:when test="${c.type == 'percent'}">
                                    <div class="coupon-desc">满<fmt:formatNumber value="${c.threshold}" pattern="#0"/>元享<fmt:formatNumber value="${c.discount}" pattern="#.#"/>折</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="coupon-desc">满<fmt:formatNumber value="${c.threshold}" pattern="#0"/>元减<fmt:formatNumber value="${c.discount}" pattern="#0"/>元</div>
                                </c:otherwise>
                            </c:choose>
                            <div class="coupon-time">有效期至: ${c.endTime}</div>
                            <div style="display:flex;align-items:center;gap:8px;margin-top:6px;">
                                <span class="coupon-status available">可使用</span>
                                <a href="${pageContext.request.contextPath}/products/affordable?minPrice=${c.threshold}" style="font-size:12px;color:var(--primary);text-decoration:none;font-weight:600;">去逛逛 →</a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- 已使用 -->
    <div id="tab-used" class="tab-panel">
        <div class="coupon-grid">
            <c:if test="${empty usedCoupons}">
                <div class="empty-state" style="grid-column: 1/-1;">
                    <div class="icon">✅</div>
                    <p>暂无已使用优惠券</p>
                </div>
            </c:if>
            <c:forEach var="c" items="${usedCoupons}">
                <div class="coupon-card used">
                    <div style="display:flex;">
                        <div class="coupon-left">
                            <span class="unit">¥</span>
                            <span class="discount"><fmt:formatNumber value="${c.discount}" pattern="#0"/></span>
                            <span class="threshold">满<fmt:formatNumber value="${c.threshold}" pattern="#0"/>可用</span>
                        </div>
                        <div class="coupon-right">
                            <div class="coupon-name">${c.name}</div>
                            <div class="coupon-desc">满<fmt:formatNumber value="${c.threshold}" pattern="#0"/>元减<fmt:formatNumber value="${c.discount}" pattern="#0"/>元</div>
                            <div class="coupon-time">有效期: ${c.startTime} ~ ${c.endTime}</div>
                            <span class="coupon-status used">已使用</span>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- 已过期 -->
    <div id="tab-expired" class="tab-panel">
        <div class="coupon-grid">
            <c:if test="${empty expiredCoupons}">
                <div class="empty-state" style="grid-column: 1/-1;">
                    <div class="icon">⏰</div>
                    <p>暂无已过期优惠券</p>
                </div>
            </c:if>
            <c:forEach var="c" items="${expiredCoupons}">
                <div class="coupon-card expired">
                    <div style="display:flex;">
                        <div class="coupon-left">
                            <span class="unit">¥</span>
                            <span class="discount"><fmt:formatNumber value="${c.discount}" pattern="#0"/></span>
                            <span class="threshold">满<fmt:formatNumber value="${c.threshold}" pattern="#0"/>可用</span>
                        </div>
                        <div class="coupon-right">
                            <div class="coupon-name">${c.name}</div>
                            <div class="coupon-desc">满<fmt:formatNumber value="${c.threshold}" pattern="#0"/>元减<fmt:formatNumber value="${c.discount}" pattern="#0"/>元</div>
                            <div class="coupon-time">已过期: ${c.endTime}</div>
                            <span class="coupon-status expired">已过期</span>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

<footer class="footer-custom">
    <div class="container" style="max-width:1100px;">
        <div class="footer-bottom">
            © 2026 在线书店 Online Bookstore. All rights reserved.
        </div>
    </div>
</footer>

<script>
    function switchTab(tab, el) {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
        el.classList.add('active');
        document.getElementById('tab-' + tab).classList.add('active');
    }
</script>
</body>
</html>