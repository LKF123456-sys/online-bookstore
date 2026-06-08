<%-- ============================================================
     product_review.jsp — 商品评论页面
     功能：展示某个商品的用户评论列表，支持查看评分和评论内容。
     说明：从后端获取指定商品的评论数据，以列表形式展示。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>商品评价 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <style>
        :root {
            --primary: #4f46e5; --primary-dark: #3730a3; --accent: #f59e0b;
            --danger: #ef4444; --success: #10b981;
            --gray-50: #f9fafb; --gray-100: #f3f4f6; --gray-200: #e5e7eb;
            --gray-300: #d1d5db; --gray-400: #9ca3af; --gray-500: #6b7280;
            --gray-600: #4b5563; --gray-700: #374151; --gray-800: #1f2937;
            --gray-900: #111827; --white: #ffffff;
            --shadow: 0 1px 6px rgba(0,0,0,0.08);
            --shadow-lg: 0 8px 30px rgba(0,0,0,0.12);
            --radius: 8px; --radius-lg: 12px; --radius-xl: 16px;
        }
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: var(--gray-50); color: var(--gray-800); margin: 0; }

        .page-container { max-width: 800px; margin: 40px auto; padding: 0 20px; }
        .page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
        .page-header h1 { font-size: 24px; font-weight: 800; color: var(--gray-800); margin: 0; }
        .back-link { color: var(--primary); text-decoration: none; font-size: 14px; font-weight: 600; }
        .back-link:hover { text-decoration: underline; }

        .summary-card { background: var(--white); border-radius: var(--radius-xl); box-shadow: var(--shadow-lg); padding: 30px; margin-bottom: 24px; display: flex; align-items: center; gap: 30px; }
        .avg-rating { text-align: center; min-width: 120px; }
        .avg-rating .rating-number { font-size: 48px; font-weight: 800; color: var(--accent); }
        .avg-rating .rating-max { font-size: 16px; color: var(--gray-400); }
        .avg-rating .rating-stars { color: var(--accent); font-size: 20px; margin-top: 4px; }
        .avg-rating .rating-count { font-size: 13px; color: var(--gray-500); margin-top: 4px; }

        .review-list { display: flex; flex-direction: column; gap: 16px; }
        .review-item { background: var(--white); border-radius: var(--radius-lg); box-shadow: var(--shadow); padding: 24px; }
        .review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
        .reviewer-avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, var(--primary), var(--primary-dark)); display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 16px; }
        .reviewer-info { flex: 1; }
        .reviewer-name { font-weight: 700; color: var(--gray-800); font-size: 15px; }
        .review-date { font-size: 12px; color: var(--gray-400); margin-top: 2px; }
        .review-stars { color: var(--accent); font-size: 14px; }
        .review-content { color: var(--gray-700); font-size: 14px; line-height: 1.7; margin-top: 8px; }
        .review-image { max-width: 200px; border-radius: var(--radius); margin-top: 10px; }

        .empty-state { text-align: center; padding: 60px 20px; color: var(--gray-400); }
        .empty-state .empty-icon { font-size: 48px; margin-bottom: 16px; }
        .empty-state p { font-size: 15px; }
    </style>
</head>
<body>
<div class="page-container">
    <div class="page-header">
        <h1>商品评价</h1>
        <a href="javascript:history.back()" class="back-link">← 返回</a>
    </div>

    <div class="summary-card">
        <div class="avg-rating">
            <div class="rating-number"><fmt:formatNumber value="${avgRating}" pattern="#0.0"/></div>
            <div class="rating-max">/ 5</div>
            <div class="rating-stars">
                <c:forEach begin="1" end="5" var="i">
                    <c:choose>
                        <c:when test="${i <= avgRating}">★</c:when>
                        <c:otherwise>☆</c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
            <div class="rating-count">${reviewCount} 条评价</div>
        </div>
    </div>

    <div class="review-list">
        <c:choose>
            <c:when test="${not empty reviews}">
                <c:forEach items="${reviews}" var="review">
                    <div class="review-item">
                        <div class="review-header">
                            <div class="reviewer-avatar">
                                ${not empty review.username ? review.username.substring(0,1) : 'U'}
                            </div>
                            <div class="reviewer-info">
                                <div class="reviewer-name">${review.username}</div>
                                <div class="review-date"><fmt:formatDate value="${review.reviewDate}" pattern="yyyy-MM-dd HH:mm"/></div>
                            </div>
                            <div class="review-stars">
                                <c:forEach begin="1" end="5" var="i">
                                    <c:choose>
                                        <c:when test="${i <= review.rating}">★</c:when>
                                        <c:otherwise>☆</c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </div>
                        </div>
                        <div class="review-content"><c:out value="${review.content}"/></div>
                        <c:if test="${not empty review.imagePath}">
                            <img src="${pageContext.request.contextPath}${review.imagePath}" class="review-image" alt="评价图片"/>
                        </c:if>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <div class="empty-icon">📝</div>
                    <p>暂无评价，快来发表第一条评价吧！</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
