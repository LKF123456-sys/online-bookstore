<%-- ============================================================
     product/stock.jsp — 前台库存查看页面
     功能：展示商品的库存信息，帮助用户了解商品是否有货。
     说明：库存数据从后端实时获取。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>库存管理 - BookVerse 管理后台</title>
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

        .page-wrapper { padding: 20px; }

        .section-title {
            font-size: 22px;
            font-weight: 700;
            color: #2c3e50;
            margin: 0 0 20px 0;
        }

        .content-grid {
            display: grid;
            grid-template-columns: 320px 1fr;
            gap: 18px;
            align-items: start;
        }

        @media (max-width: 992px) {
            .content-grid { grid-template-columns: 1fr; }
        }

        .panel-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            overflow: hidden;
        }

        .panel-card .panel-head {
            padding: 16px 20px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 15px;
            font-weight: 600;
            color: #2c3e50;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .panel-card .panel-head.danger-head {
            background: linear-gradient(135deg, #fff5f5, #fff);
            border-left: 4px solid var(--danger);
        }

        .panel-card .panel-body { padding: 12px 20px; }

        .alert-inline-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10px 12px;
            margin-bottom: 6px;
            border-radius: 10px;
            background: #fdf2f2;
            border: 1px solid #fce4e4;
            transition: all 0.2s;
        }

        .alert-inline-item:hover {
            background: #fce8e8;
            border-color: #f5c6c6;
        }

        .alert-inline-item:last-child { margin-bottom: 0; }

        .alert-book-name {
            font-size: 13px;
            font-weight: 600;
            color: #2c3e50;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .alert-stock-num {
            font-size: 18px;
            font-weight: 700;
            color: var(--danger);
            padding: 2px 10px;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
            min-width: 36px;
            text-align: center;
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

        .stock-bar {
            height: 8px;
            border-radius: 4px;
            background: #eee;
            overflow: hidden;
            min-width: 80px;
        }

        .stock-bar-fill {
            height: 100%;
            border-radius: 4px;
            transition: width 0.5s ease;
        }

        .stock-bar-fill.high { background: var(--success); }
        .stock-bar-fill.medium { background: var(--warning); }
        .stock-bar-fill.low { background: var(--danger); }

        .stock-editable {
            cursor: pointer;
            padding: 3px 8px;
            border-radius: 6px;
            transition: all 0.2s;
            font-weight: 600;
            position: relative;
        }

        .stock-editable:hover {
            background: #eef0ff;
            color: var(--primary);
        }

        .stock-editable::after {
            content: ' ✎';
            font-size: 10px;
            color: var(--gray-500);
            opacity: 0;
            transition: opacity 0.2s;
        }

        .stock-editable:hover::after { opacity: 1; }

        .stock-edit-input {
            width: 70px;
            height: 28px;
            padding: 2px 6px;
            border: 2px solid var(--primary);
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            text-align: center;
            outline: none;
        }

        .table-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            overflow: hidden;
        }

        .table-card > table { margin-bottom: 0; }

        .table-card > table > thead > tr > th {
            background: #fafbfc;
            border-bottom: 2px solid #e8e8e8;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #7f8c8d;
            padding: 14px 12px;
            font-weight: 600;
        }

        .table-card > table > tbody > tr > td {
            vertical-align: middle;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
        }

        .table-card > table > tbody > tr:hover { background: #f8f9ff; }

        .table-card > table > tbody > tr.row-danger {
            background: #fffafa;
        }

        .table-card > table > tbody > tr.row-danger:hover {
            background: #fff0f0;
        }

        .stock-text { font-weight: 600; }
        .stock-text.danger-text { color: var(--danger); }
        .stock-text.warning-text { color: var(--warning); }
        .stock-text.success-text { color: var(--success); }

        .search-box-row {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
            align-items: center;
        }

        .search-box-row input {
            width: 250px;
            border-radius: 8px;
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid">
    <div class="row">
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10 page-wrapper">
            <h2 class="section-title">📦 库存管理</h2>

            <div class="tabs-nav">
                <a href="${pageContext.request.contextPath}/admin/product/bestseller">销量排行 TOP50</a>
                <a href="${pageContext.request.contextPath}/admin/product/stock" class="active">库存管理</a>
            </div>

            <div class="content-grid">
                <div>
                    <div class="panel-card">
                        <div class="panel-head danger-head">
                            ⚠️ 低库存预警
                            <span style="margin-left:auto;font-size:12px;color:var(--gray-500);">
                                ${lowStockCount} 个商品
                            </span>
                        </div>
                        <div class="panel-body">
                            <c:choose>
                                <c:when test="${not empty lowStockList}">
                                    <c:forEach items="${lowStockList}" var="item">
                                        <div class="alert-inline-item">
                                            <span class="alert-book-name" title="${item.name}">${item.name}</span>
                                            <span class="alert-stock-num">${item.stock}</span>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div style="text-align:center;padding:30px;color:var(--success);">
                                        ✅ 所有商品库存充足
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <div>
                    <div class="search-box-row">
                        <form action="${pageContext.request.contextPath}/admin/product/stock" method="get" class="form-inline">
                            <input type="text" name="keyword" class="form-control input-sm"
                                   placeholder="搜索书名或ID..." value="${keyword}">
                            <button type="submit" class="btn btn-sm btn-primary">搜索</button>
                            <c:if test="${not empty keyword}">
                                <a href="${pageContext.request.contextPath}/admin/product/stock" class="btn btn-sm btn-default">清除</a>
                            </c:if>
                        </form>
                    </div>

                    <div class="table-card">
                        <table class="table table-bordered">
                            <thead>
                                <tr>
                                    <th style="width:80px;">商品ID</th>
                                    <th>书名</th>
                                    <th style="width:100px;">当前库存</th>
                                    <th style="width:90px;">销量</th>
                                    <th style="width:130px;">库存状态</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${stockList}" var="book">
                                    <c:set var="stockLevel" value=""/>
                                    <c:choose>
                                        <c:when test="${book.stock < 10}">
                                            <c:set var="stockLevel" value="low"/>
                                        </c:when>
                                        <c:when test="${book.stock <= 100}">
                                            <c:set var="stockLevel" value="medium"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="stockLevel" value="high"/>
                                        </c:otherwise>
                                    </c:choose>

                                    <c:set var="stockPercent" value="100"/>
                                    <c:choose>
                                        <c:when test="${book.stock >= 200}">
                                            <c:set var="stockPercent" value="100"/>
                                        </c:when>
                                        <c:when test="${book.stock >= 0}">
                                            <c:set var="stockPercent" value="${book.stock * 100 / 200}"/>
                                        </c:when>
                                    </c:choose>

                                    <tr class="${stockLevel == 'low' ? 'row-danger' : ''}">
                                        <td><code>${book.productid}</code></td>
                                        <td>
                                            <strong>${book.name}</strong>
                                        </td>
                                        <td style="text-align:center;">
                                            <span class="stock-editable stock-text ${stockLevel == 'low' ? 'danger-text' : (stockLevel == 'medium' ? 'warning-text' : 'success-text')}"
                                                  data-productid="${book.productid}"
                                                  data-stock="${book.stock}"
                                                  onclick="editStock(this)">
                                                ${book.stock}
                                            </span>
                                        </td>
                                        <td style="text-align:center;">
                                            <span style="color:var(--primary);font-weight:600;">${book.sales}</span>
                                        </td>
                                        <td>
                                            <div class="stock-bar">
                                                <div class="stock-bar-fill ${stockLevel}"
                                                     style="width: ${stockPercent}%;">
                                                </div>
                                            </div>
                                            <span style="font-size:11px;color:var(--gray-500);margin-top:3px;display:block;">
                                                <c:choose>
                                                    <c:when test="${stockLevel == 'low'}">库存紧张</c:when>
                                                    <c:when test="${stockLevel == 'medium'}">库存适中</c:when>
                                                    <c:otherwise>库存充足</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty stockList}">
                                    <tr>
                                        <td colspan="5" class="text-center" style="padding:40px;color:var(--gray-500);">
                                            暂无商品数据
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${pageInfo != null && pageInfo.pages > 1}">
                        <div style="display:flex;justify-content:space-between;align-items:center;margin-top:15px;">
                            <div style="color:var(--gray-500);font-size:13px;">
                                共 ${pageInfo.total} 条，第 ${pageInfo.pageNum}/${pageInfo.pages} 页
                            </div>
                            <ul class="pagination pagination-sm" style="margin:0;">
                                <li class="${pageInfo.hasPreviousPage ? '' : 'disabled'}">
                                    <a href="${pageContext.request.contextPath}/admin/product/stock?pageNum=${pageInfo.pageNum - 1}&pageSize=${pageInfo.pageSize}${not empty keyword ? '&keyword=' : ''}${keyword}">&laquo;</a>
                                </li>
                                <c:forEach var="i" begin="1" end="${pageInfo.pages}">
                                    <c:choose>
                                        <c:when test="${i == pageInfo.pageNum}">
                                            <li class="active"><a href="#">${i}</a></li>
                                        </c:when>
                                        <c:when test="${i <= 2 or i >= pageInfo.pages - 1 or i == pageInfo.pageNum}">
                                            <li><a href="${pageContext.request.contextPath}/admin/product/stock?pageNum=${i}&pageSize=${pageInfo.pageSize}${not empty keyword ? '&keyword=' : ''}${keyword}">${i}</a></li>
                                        </c:when>
                                        <c:when test="${i == 3 or i == pageInfo.pages - 2}">
                                            <li class="disabled"><a href="#">...</a></li>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>
                                <li class="${pageInfo.hasNextPage ? '' : 'disabled'}">
                                    <a href="${pageContext.request.contextPath}/admin/product/stock?pageNum=${pageInfo.pageNum + 1}&pageSize=${pageInfo.pageSize}${not empty keyword ? '&keyword=' : ''}${keyword}">&raquo;</a>
                                </li>
                            </ul>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script>
    function editStock(el) {
        var $el = $(el);
        var productid = $el.data('productid');
        var currentStock = $el.data('stock');
        var $input = $('<input type="number" class="stock-edit-input" min="0" value="' + currentStock + '">');

        $el.replaceWith($input);
        $input.focus().select();

        $input.on('blur keydown', function(e) {
            if (e.type === 'blur' || e.which === 13) {
                var newStock = parseInt($input.val());
                if (isNaN(newStock) || newStock < 0) {
                    $input.replaceWith($el);
                    return;
                }
                if (newStock === currentStock) {
                    $input.replaceWith($el);
                    return;
                }

                var $p = $input;
                $.ajax({
                    url: '${pageContext.request.contextPath}/admin/product/stock/update',
                    type: 'POST',
                    data: { productid: productid, stock: newStock },
                    success: function(data) {
                        if (data.success) {
                            location.reload();
                        } else {
                            alert(data.message || '更新失败');
                            location.reload();
                        }
                    },
                    error: function() {
                        alert('操作失败，请重试');
                        location.reload();
                    }
                });
            }
            if (e.which === 27) {
                $input.replaceWith($el);
            }
        });
    }
</script>
</body>
</html>
