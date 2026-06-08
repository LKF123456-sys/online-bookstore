<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>图书管理 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        .toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-wrap: wrap; gap: 10px; }
        .search-form { display: flex; gap: 10px; }
        .search-form input { width: 200px; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid" style="margin-top: 20px;">
    <div class="row">
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">📦 图书管理</h3>
                </div>
                <div class="panel-body">
                    <div class="toolbar">
                        <div class="search-form">
                            <form action="${pageContext.request.contextPath}/admin/product" method="get" class="form-inline">
                                <input type="text" name="keyword" class="form-control input-sm" placeholder="搜索书名/描述..." value="<c:out value="${keyword}"/>"/>
                                <button type="submit" class="btn btn-sm btn-primary">🔍 搜索</button>
                                <c:if test="${not empty keyword}">
                                    <a href="${pageContext.request.contextPath}/admin/product" class="btn btn-sm btn-default">清除</a>
                                </c:if>
                            </form>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/product/add" class="btn btn-sm btn-gradient">➕ 新增图书</a>
                    </div>
                    <c:if test="${param.success != null}">
                        <div class="alert alert-${param.success == 'true' ? 'success' : 'danger'} alert-dismissible">
                            <button type="button" class="close" data-dismiss="alert">&times;</button>
                            ${param.success == 'true' ? '操作成功' : '操作失败（图书ID可能已存在）'}
                        </div>
                    </c:if>
                    <table class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>图片</th>
                                <th>书名</th>
                                <th>分类</th>
                                <th>描述</th>
                                <th>价格</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${productList}" var="book">
                                <tr>
                                    <td><strong>${book.productid}</strong></td>
                                    <td>
                                        <img src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg"
                                             alt="${book.name}" style="width: 60px; height: 80px; object-fit: cover; border-radius: 8px;"
                                             onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2260%22 height=%2280%22><rect fill=%22%23e5e7eb%22 width=%2260%22 height=%2280%22/><text fill=%22%239ca3af%22 font-size=%2210%22 text-anchor=%22middle%22 x=%2230%22 y=%2244%22>Book</text></svg>'">
                                    </td>
                                    <td><c:out value="${book.name}"/></td>
                                    <td>${book.category}</td>
                                    <td>${book.descn}</td>
                                    <td style="color: var(--danger); font-weight: 700;">¥<fmt:formatNumber value="${book.price}" pattern="#,##0.00"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${book.status == 1}">
                                                <span class="label label-success">上架</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="label label-default">下架</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/product/edit?productid=${book.productid}" class="btn btn-sm btn-info">编辑</a>
                                        <c:if test="${book.status == 1}">
                                            <button class="btn btn-sm btn-warning" onclick="toggleProductStatus('${book.productid}', 'offline')">下架</button>
                                        </c:if>
                                        <c:if test="${book.status == 0}">
                                            <button class="btn btn-sm btn-success" onclick="toggleProductStatus('${book.productid}', 'online')">上架</button>
                                        </c:if>
                                        <button class="btn btn-sm btn-danger" onclick="deleteProduct('${book.productid}')">删除</button>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty productList}">
                                <tr><td colspan="8" class="text-center" style="color: var(--gray-500);">暂无图书数据</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function toggleProductStatus(productid, action) {
        if (action === 'offline' && !confirm('确定要下架该图书吗？')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/product/' + action,
            type: 'GET',
            data: { productid: productid },
            success: function(data) {
                if (data.success) location.reload();
                else alert(data.message);
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }
    function deleteProduct(productid) {
        if (!confirm('确定要删除图书 "' + productid + '" 吗？此操作不可撤销！')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/product/delete',
            type: 'GET',
            data: { productid: productid },
            success: function(data) {
                if (data.success) location.reload();
                else alert(data.message);
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }
</script>
</body>
</html>
