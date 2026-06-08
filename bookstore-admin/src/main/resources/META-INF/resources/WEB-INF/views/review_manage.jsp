<%-- 评价管理页面 review_manage.jsp --%>
<%-- 功能：用户管理自己发表的所有评价，包括查看、编辑、删除评价 --%>
<%-- 展示用户的历史评价列表 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的评价 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        :root {
            --primary: #4f46e5; --primary-dark: #3730a3; --accent: #f59e0b;
            --danger: #ef4444; --success: #10b981; --warning: #f59e0b;
            --gray-50: #f9fafb; --gray-100: #f3f4f6; --gray-200: #e5e7eb;
            --gray-300: #d1d5db; --gray-400: #9ca3af; --gray-500: #6b7280;
            --gray-600: #4b5563; --gray-700: #374151; --gray-800: #1f2937;
            --gray-900: #111827; --white: #ffffff; --shadow: 0 1px 6px rgba(0,0,0,0.08);
            --shadow-lg: 0 8px 30px rgba(0,0,0,0.12); --radius: 8px;
            --radius-lg: 12px; --radius-xl: 16px;
        }
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: var(--gray-50); color: var(--gray-800); }

        .navbar-custom { background: linear-gradient(135deg, var(--primary), var(--primary-dark)); border: none; border-radius: 0; box-shadow: 0 2px 12px rgba(79,70,229,0.3); }
        .navbar-custom .navbar-brand { color: #fff !important; font-weight: 800; font-size: 20px; }
        .navbar-custom .nav > li > a { color: rgba(255,255,255,0.9) !important; font-weight: 600; transition: all 0.3s; }
        .navbar-custom .nav > li > a:hover { color: #fff !important; background: rgba(255,255,255,0.1) !important; border-radius: 6px; }

        .page-header-wrap { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
        .page-title { font-size: 26px; font-weight: 800; color: var(--gray-900); margin: 0; }

        .review-card { background: var(--white); border-radius: var(--radius-xl); box-shadow: var(--shadow); margin-bottom: 16px; overflow: hidden; }
        .review-card-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; background: var(--gray-50); border-bottom: 1px solid var(--gray-100); }
        .review-product { display: flex; align-items: center; gap: 12px; }
        .review-product img { width: 48px; height: 62px; border-radius: var(--radius); object-fit: cover; }
        .review-product-name { font-weight: 700; color: var(--gray-800); font-size: 15px; }
        .review-stars { color: var(--accent); font-size: 18px; }
        .review-card-body { padding: 20px 24px; }
        .review-content { font-size: 14px; color: var(--gray-600); line-height: 1.6; margin-bottom: 12px; }
        .review-image { max-width: 200px; border-radius: var(--radius); margin-top: 10px; }
        .review-meta { display: flex; align-items: center; justify-content: space-between; font-size: 12px; color: var(--gray-400); }
        .review-status { padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
        .status-normal { background: #d1fae5; color: #065f46; }
        .status-blocked { background: #fee2e2; color: #991b1b; }

        .btn-sm { display: inline-block; padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 700; border: none; cursor: pointer; text-decoration: none; transition: all 0.3s; }
        .btn-sm-danger { background: transparent; color: var(--danger); border: 1.5px solid var(--danger); }
        .btn-sm-danger:hover { background: var(--danger); color: #fff; text-decoration: none; }

        .empty-state { text-align: center; padding: 80px 20px; background: var(--white); border-radius: var(--radius-xl); box-shadow: var(--shadow); }
        .empty-state .empty-icon { font-size: 80px; margin-bottom: 20px; }
        .empty-state h2 { font-size: 22px; color: var(--gray-600); margin-bottom: 8px; }
        .empty-state p { color: var(--gray-400); margin-bottom: 24px; }
        .btn-gradient { display: inline-block; padding: 14px 32px; border-radius: 50px; font-size: 16px; font-weight: 700; background: linear-gradient(135deg, var(--primary), var(--primary-dark)); color: #fff; border: none; cursor: pointer; text-decoration: none; }

        .btn-back-home { background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important; position: relative; overflow: hidden; }
        .btn-back-home::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent); transition: left 0.5s; }
        .btn-back-home:hover { transform: translateY(-2px) !important; box-shadow: 0 8px 25px rgba(79,70,229,0.4) !important; color: #fff !important; text-decoration: none !important; }
        .btn-back-home:hover::before { left: 100%; }
        .btn-back-home:active { transform: translateY(0) !important; }
    </style>
</head>
<body>
<nav class="navbar navbar-default navbar-custom navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">BookVerse</a>
        </div>
        <ul class="nav navbar-nav navbar-right" style="margin-right: 20px;">
            <li><a href="${pageContext.request.contextPath}/">首页</a></li>
            <li><a href="${pageContext.request.contextPath}/order/history">我的订单</a></li>
            <li><a href="${pageContext.request.contextPath}/logout">退出</a></li>
        </ul>
    </div>
</nav>
<div style="height: 80px;"></div>

<div class="container">
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px;">
        <a href="${pageContext.request.contextPath}/" class="btn-back-home" style="display:inline-flex;align-items:center;gap:8px;padding:12px 28px;background:linear-gradient(135deg,var(--primary),var(--primary-dark));color:#fff;border:none;border-radius:50px;font-size:14px;font-weight:600;cursor:pointer;text-decoration:none;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);box-shadow:0 4px 15px rgba(79,70,229,0.3);position:relative;overflow:hidden;">
            <span style="position:relative;z-index:1;display:flex;align-items:center;gap:8px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/></svg>
                返回首页
            </span>
        </a>
        <a href="${pageContext.request.contextPath}/order/history" class="btn-back-home" style="display:inline-flex;align-items:center;gap:8px;padding:12px 28px;background:linear-gradient(135deg,var(--primary),var(--primary-dark));color:#fff;border:none;border-radius:50px;font-size:14px;font-weight:600;cursor:pointer;text-decoration:none;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);box-shadow:0 4px 15px rgba(79,70,229,0.3);position:relative;overflow:hidden;">
            <span style="position:relative;z-index:1;display:flex;align-items:center;gap:8px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/></svg>
                返回订单
            </span>
        </a>
    </div>
    <div class="page-header-wrap">
        <h1 class="page-title">我的评价</h1>
    </div>

    <c:if test="${empty reviews}">
        <div class="empty-state">
            <div class="empty-icon">📝</div>
            <h2>还没有评价</h2>
            <p>完成订单后，来分享您的购物体验吧！</p>
            <a href="${pageContext.request.contextPath}/order/history" class="btn-gradient">查看订单</a>
        </div>
    </c:if>

    <c:if test="${not empty reviews}">
        <c:forEach items="${reviews}" var="r">
            <div class="review-card">
                <div class="review-card-header">
                    <div class="review-product">
                        <c:choose>
                            <c:when test="${not empty r.productId}">
                                <img src="${pageContext.request.contextPath}/img/books/${r.productId}.jpg" alt="" onerror="this.style.background='var(--gray-200)';this.src='';">
                                <span class="review-product-name">${r.productId}</span>
                            </c:when>
                            <c:otherwise>
                                <img src="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2248%22 height=%2262%22><rect fill=%22%23e5e7eb%22 width=%2248%22 height=%2262%22 rx=%228%22/><text fill=%22%239ca3af%22 font-size=%2216%22 text-anchor=%22middle%22 x=%2224%22 y=%2237%22>📦</text></svg>" alt="">
                                <span class="review-product-name">订单评价</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="review-stars">
                        <c:forEach begin="1" end="${r.rating}" var="s">★</c:forEach>
                        <c:forEach begin="${r.rating + 1}" end="5" var="s">☆</c:forEach>
                    </div>
                </div>
                <div class="review-card-body">
                    <div class="review-content">${r.content}</div>
                    <c:if test="${not empty r.image}">
                        <img src="${pageContext.request.contextPath}/img/books/${r.image}" alt="晒图" class="review-image" onclick="window.open(this.src)">
                    </c:if>
                    <div class="review-meta">
                        <span><fmt:formatDate value="${r.createTime}" pattern="yyyy-MM-dd HH:mm"/></span>
                        <c:choose>
                            <c:when test="${r.blocked == 1}">
                                <span class="review-status status-blocked">已屏蔽</span>
                            </c:when>
                            <c:otherwise>
                                <span class="review-status status-normal">正常显示</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div style="padding: 12px 24px; border-top: 1px solid var(--gray-100); text-align: right;">
                    <form action="${pageContext.request.contextPath}/review/delete" method="post" style="display: inline;">
                        <input type="hidden" name="reviewId" value="${r.id}"/>
                        <button type="submit" class="btn-sm btn-sm-danger" onclick="return confirm('确定删除这条评价吗？')">删除</button>
                    </form>
                </div>
            </div>
        </c:forEach>
    </c:if>
</div>

</body>
</html>
