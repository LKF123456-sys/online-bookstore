<%-- ============================================================
     product/bestseller.jsp — 前台热销排行页面
     功能：展示销量最高的商品排行榜，帮助用户发现热门图书。
     说明：数据按销量排序，从后端动态获取。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>热销排行榜 - BookVerse 管理后台</title>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        :root {
            --primary: #4361ee;
            --success: #2ecc71;
            --warning: #f39c12;
            --danger: #e74c3c;
            --gray-500: #95a5a6;
        }

        body { background: #f0f2f5; }

        .page-wrapper {
            padding: 20px;
        }

        .section-title {
            font-size: 22px;
            font-weight: 700;
            color: #2c3e50;
            margin: 0 0 20px 0;
        }

        .rank-medal {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 34px;
            height: 34px;
            border-radius: 50%;
            font-weight: 700;
            font-size: 14px;
            color: #fff;
            flex-shrink: 0;
        }

        .rank-medal.gold {
            background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
            box-shadow: 0 3px 10px rgba(246,211,101,0.4);
        }

        .rank-medal.silver {
            background: linear-gradient(135deg, #a8c0ff 0%, #8f94fb 100%);
            box-shadow: 0 3px 10px rgba(143,148,251,0.4);
        }

        .rank-medal.bronze {
            background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
            box-shadow: 0 3px 10px rgba(241,39,17,0.4);
        }

        .rank-medal.normal {
            background: var(--gray-500);
            font-size: 12px;
        }

        .book-cover {
            width: 50px;
            height: 68px;
            object-fit: cover;
            border-radius: 6px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .book-name-cell {
            font-weight: 600;
            color: #2c3e50;
            max-width: 200px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .sales-number {
            font-weight: 700;
            font-size: 15px;
            color: var(--primary);
        }

        .toggle-switch {
            position: relative;
            display: inline-block;
            width: 44px;
            height: 24px;
        }

        .toggle-switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }

        .toggle-slider {
            position: absolute;
            cursor: pointer;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: #ccc;
            transition: 0.3s;
            border-radius: 24px;
        }

        .toggle-slider:before {
            position: absolute;
            content: "";
            height: 18px;
            width: 18px;
            left: 3px;
            bottom: 3px;
            background-color: #fff;
            transition: 0.3s;
            border-radius: 50%;
        }

        .toggle-switch input:checked + .toggle-slider {
            background: linear-gradient(135deg, var(--primary), #764ba2);
        }

        .toggle-switch input:checked + .toggle-slider:before {
            transform: translateX(20px);
        }

        .recommend-badge {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 600;
        }

        .recommend-badge.on {
            background: #e8f5e9;
            color: var(--success);
        }

        .recommend-badge.off {
            background: #f5f5f5;
            color: var(--gray-500);
        }

        .table-custom {
            background: #fff;
            border-radius: 14px;
            overflow: hidden;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
        }

        .table-custom > table {
            margin-bottom: 0;
        }

        .table-custom > table > thead > tr > th {
            background: #fafbfc;
            border-bottom: 2px solid #e8e8e8;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #7f8c8d;
            padding: 14px 12px;
            font-weight: 600;
        }

        .table-custom > table > tbody > tr > td {
            vertical-align: middle;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
        }

        .table-custom > table > tbody > tr:hover {
            background: #f8f9ff;
        }

        .table-custom > table > tbody > tr.top-row {
            background: linear-gradient(90deg, #fffdf5, #ffffff);
        }

        .table-custom > table > tbody > tr.top-row:hover {
            background: linear-gradient(90deg, #fff9e0, #fdf8ff);
        }

        .tabs-nav {
            display: flex;
            gap: 5px;
            margin-bottom: 20px;
            background: #fff;
            padding: 5px;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
        }

        .tabs-nav a {
            padding: 8px 20px;
            border-radius: 8px;
            text-decoration: none;
            font-size: 13px;
            font-weight: 600;
            color: #7f8c8d;
            transition: all 0.3s;
        }

        .tabs-nav a.active {
            background: var(--primary);
            color: #fff;
        }

        .tabs-nav a:hover:not(.active) {
            background: #f0f0f0;
            color: #2c3e50;
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid">
    <div class="row">
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10 page-wrapper">
            <h2 class="section-title">🔥 热销排行榜</h2>

            <div class="tabs-nav">
                <a href="${pageContext.request.contextPath}/admin/product/bestseller" class="active">销量排行 TOP50</a>
                <a href="${pageContext.request.contextPath}/admin/product/stock">库存管理</a>
            </div>

            <div class="table-custom">
                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th style="width:60px;">排名</th>
                            <th style="width:70px;">封面</th>
                            <th>书名</th>
                            <th style="width:100px;">分类</th>
                            <th style="width:100px;">价格</th>
                            <th style="width:90px;">销量</th>
                            <th style="width:100px;">推荐状态</th>
                            <th style="width:90px;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${bestsellers}" var="book" varStatus="vs">
                            <tr class="${vs.index < 3 ? 'top-row' : ''}">
                                <td style="text-align:center;">
                                    <c:choose>
                                        <c:when test="${vs.index == 0}">
                                            <span class="rank-medal gold">👑</span>
                                        </c:when>
                                        <c:when test="${vs.index == 1}">
                                            <span class="rank-medal silver">🥈</span>
                                        </c:when>
                                        <c:when test="${vs.index == 2}">
                                            <span class="rank-medal bronze">🥉</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rank-medal normal">${vs.index + 1}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align:center;">
                                    <img class="book-cover"
                                         src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg"
                                         alt="${book.name}"
                                         onerror="this.src='${pageContext.request.contextPath}/img/default-book.svg'">
                                </td>
                                <td>
                                    <span class="book-name-cell" title="${book.name}">${book.name}</span>
                                </td>
                                <td><span class="label label-default">${book.category}</span></td>
                                <td style="color:var(--danger);font-weight:700;">
                                    ¥<fmt:formatNumber value="${book.price}" pattern="#,##0.00"/>
                                </td>
                                <td>
                                    <span class="sales-number">${book.sales}</span>
                                    <span style="font-size:11px;color:var(--gray-500);">本</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${book.isRecommend == 1}">
                                            <span class="recommend-badge on">已推荐</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="recommend-badge off">未推荐</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align:center;">
                                    <label class="toggle-switch" title="${book.isRecommend == 1 ? '取消推荐' : '设为推荐'}">
                                        <input type="checkbox"
                                               ${book.isRecommend == 1 ? 'checked' : ''}
                                               onchange="toggleRecommend('${book.productid}', this.checked)">
                                        <span class="toggle-slider"></span>
                                    </label>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty bestsellers}">
                            <tr>
                                <td colspan="8" class="text-center" style="padding:40px;color:var(--gray-500);">
                                    暂无销售数据
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script>
    function toggleRecommend(productid, checked) {
        var isRecommend = checked ? 1 : 0;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/product/recommend',
            type: 'POST',
            data: { productid: productid, isRecommend: isRecommend },
            success: function(data) {
                if (data.success) {
                    location.reload();
                } else {
                    alert(data.message || '操作失败');
                    location.reload();
                }
            },
            error: function() {
                alert('操作失败，请重试');
                location.reload();
            }
        });
    }
</script>
</body>
</html>
