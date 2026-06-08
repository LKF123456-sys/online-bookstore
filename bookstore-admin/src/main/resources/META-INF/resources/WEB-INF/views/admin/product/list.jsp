<%-- ============================================================
     图书管理列表页面 (product/list.jsp)
     功能：展示所有图书信息的管理列表
     主要功能：
       - 按书名/描述搜索图书
       - 查看图书列表（ID、图片、书名、分类、描述、价格、状态）
       - 上架/下架图书（AJAX无刷新操作）
       - 删除图书（AJAX无刷新操作，需二次确认）
       - 跳转到新增/编辑图书页面
     使用了 Bootstrap 3 的面板、表格、按钮组件
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>图书管理 - BookVerse</title>
    <%-- 引入 Bootstrap 样式和自定义样式 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <%-- 引入 jQuery 和 Bootstrap JS（用于AJAX操作） --%>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        /* ==================== 工具栏样式 ==================== */
        /* 工具栏：搜索表单 + 新增按钮 横向排列 */
        .toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-wrap: wrap; gap: 10px; }
        /* 搜索表单内部元素横向排列 */
        .search-form { display: flex; gap: 10px; }
        .search-form input { width: 200px; }
    </style>
</head>
<body>
<%-- 引入管理后台公共头部（导航栏） --%>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<%-- ==================== 页面主体内容 ==================== --%>
<div class="container-fluid" style="margin-top: 20px;">
    <div class="row">
        <%-- 引入管理后台侧边栏 --%>
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <%-- 右侧主内容区（占10列） --%>
        <div class="col-md-10">
            <div class="panel panel-default">
                <%-- 面板标题 --%>
                <div class="panel-heading">
                    <h3 class="panel-title">📦 图书管理</h3>
                </div>
                <div class="panel-body">
                    <%-- ==================== 工具栏区域 ==================== --%>
                    <div class="toolbar">
                        <%-- 左侧：搜索表单 --%>
                        <div class="search-form">
                            <form action="${pageContext.request.contextPath}/admin/product" method="get" class="form-inline">
                                <%-- 搜索关键词输入框，保留上次搜索的值 --%>
                                <input type="text" name="keyword" class="form-control input-sm" placeholder="搜索书名/描述..." value="<c:out value="${keyword}"/>"/>
                                <button type="submit" class="btn btn-sm btn-primary">🔍 搜索</button>
                                <%-- 有搜索关键词时显示"清除"按钮，点击回到完整列表 --%>
                                <c:if test="${not empty keyword}">
                                    <a href="${pageContext.request.contextPath}/admin/product" class="btn btn-sm btn-default">清除</a>
                                </c:if>
                            </form>
                        </div>
                        <%-- 右侧：新增图书按钮 --%>
                        <a href="${pageContext.request.contextPath}/admin/product/add" class="btn btn-sm btn-gradient">➕ 新增图书</a>
                    </div>
                    <%-- ==================== 操作结果提示 ==================== --%>
                    <%-- 根据URL参数 success 显示成功或失败的提示信息 --%>
                    <c:if test="${param.success != null}">
                        <div class="alert alert-${param.success == 'true' ? 'success' : 'danger'} alert-dismissible">
                            <button type="button" class="close" data-dismiss="alert">&times;</button>
                            ${param.success == 'true' ? '操作成功' : '操作失败（图书ID可能已存在）'}
                        </div>
                    </c:if>
                    <%-- ==================== 图书数据表格 ==================== --%>
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
                            <%-- 使用 JSTL forEach 循环遍历图书列表，每本书生成一行 --%>
                            <c:forEach items="${productList}" var="book">
                                <tr>
                                    <td><strong>${book.productid}</strong></td>
                                    <td>
                                        <%-- 图书封面图片，加载失败时显示默认占位图 --%>
                                        <img src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg"
                                             alt="${book.name}" style="width: 60px; height: 80px; object-fit: cover; border-radius: 8px;"
                                             onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2260%22 height=%2280%22><rect fill=%22%23e5e7eb%22 width=%2260%22 height=%2280%22/><text fill=%22%239ca3af%22 font-size=%2210%22 text-anchor=%22middle%22 x=%2230%22 y=%2244%22>Book</text></svg>'">
                                    </td>
                                    <td><c:out value="${book.name}"/></td>
                                    <td>${book.category}</td>
                                    <td>${book.descn}</td>
                                    <%-- 价格显示为红色加粗，保留两位小数 --%>
                                    <td style="color: var(--danger); font-weight: 700;">¥<fmt:formatNumber value="${book.price}" pattern="#,##0.00"/></td>
                                    <td>
                                        <%-- 根据状态值显示不同的标签样式 --%>
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
                                        <%-- 操作按钮：编辑、上架/下架切换、删除 --%>
                                        <a href="${pageContext.request.contextPath}/admin/product/edit?productid=${book.productid}" class="btn btn-sm btn-info">编辑</a>
                                        <%-- 已上架的显示"下架"按钮 --%>
                                        <c:if test="${book.status == 1}">
                                            <button class="btn btn-sm btn-warning" onclick="toggleProductStatus('${book.productid}', 'offline')">下架</button>
                                        </c:if>
                                        <%-- 已下架的显示"上架"按钮 --%>
                                        <c:if test="${book.status == 0}">
                                            <button class="btn btn-sm btn-success" onclick="toggleProductStatus('${book.productid}', 'online')">上架</button>
                                        </c:if>
                                        <button class="btn btn-sm btn-danger" onclick="deleteProduct('${book.productid}')">删除</button>
                                    </td>
                                </tr>
                            </c:forEach>
                            <%-- 图书列表为空时显示提示 --%>
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
<%-- ==================== JavaScript 部分 ==================== --%>
<script type="text/javascript">
    /**
     * 切换图书上架/下架状态
     * @param productid - 图书ID
     * @param action - 操作类型：'online'(上架) 或 'offline'(下架)
     */
    function toggleProductStatus(productid, action) {
        // 下架操作需要二次确认
        if (action === 'offline' && !confirm('确定要下架该图书吗？')) return;
        // 发送AJAX请求到后端切换状态
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/product/' + action,
            type: 'GET',
            data: { productid: productid },
            success: function(data) {
                if (data.success) location.reload();  // 成功则刷新页面
                else alert(data.message);               // 失败则显示错误信息
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }

    /**
     * 删除图书
     * @param productid - 要删除的图书ID
     */
    function deleteProduct(productid) {
        // 删除操作需要二次确认，提示不可撤销
        if (!confirm('确定要删除图书 "' + productid + '" 吗？此操作不可撤销！')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/product/delete',
            type: 'GET',
            data: { productid: productid },
            success: function(data) {
                if (data.success) location.reload();  // 成功则刷新页面
                else alert(data.message);               // 失败则显示错误信息
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }
</script>
</body>
</html>