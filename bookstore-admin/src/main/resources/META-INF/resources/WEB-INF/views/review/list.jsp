<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>评价管理 - BookVerse Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #f5f5f5; color: #333; margin: 0; }

        .admin-header { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; padding: 15px 30px; display: flex; align-items: center; justify-content: space-between; }
        .admin-header h1 { margin: 0; font-size: 20px; }
        .admin-header a { color: #fff; text-decoration: none; padding: 8px 16px; border-radius: 5px; background: rgba(255,255,255,0.2); }
        .admin-header a:hover { background: rgba(255,255,255,0.3); }

        .admin-layout { display: flex; min-height: calc(100vh - 50px); }

        .admin-content { flex: 1; padding: 24px; }

        .card { background: #fff; border-radius: 12px; box-shadow: 0 1px 6px rgba(0,0,0,0.08); margin-bottom: 20px; }
        .card-header { padding: 20px 24px; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: space-between; }
        .card-header h2 { margin: 0; font-size: 18px; }

        .toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; padding: 16px 24px; border-bottom: 1px solid #eee; }
        .toolbar input[type="text"] { padding: 8px 14px; border: 2px solid #e5e7eb; border-radius: 8px; font-size: 14px; outline: none; width: 200px; }
        .toolbar input[type="text"]:focus { border-color: #667eea; }
        .toolbar select { padding: 8px 14px; border: 2px solid #e5e7eb; border-radius: 8px; font-size: 14px; outline: none; }
        .toolbar select:focus { border-color: #667eea; }
        .btn { padding: 8px 18px; border-radius: 8px; font-size: 14px; font-weight: 600; border: none; cursor: pointer; text-decoration: none; transition: all 0.2s; }
        .btn-primary { background: #667eea; color: #fff; }
        .btn-primary:hover { background: #5a6fd6; }
        .btn-success { background: #10b981; color: #fff; }
        .btn-success:hover { background: #059669; }
        .btn-danger { background: #ef4444; color: #fff; }
        .btn-danger:hover { background: #dc2626; }
        .btn-warning { background: #f59e0b; color: #fff; }
        .btn-warning:hover { background: #d97706; }
        .btn-sm { padding: 5px 12px; font-size: 12px; border-radius: 6px; }

        .table { width: 100%; border-collapse: collapse; }
        .table th { padding: 12px 16px; font-size: 13px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid #e5e7eb; text-align: left; }
        .table td { padding: 14px 16px; border-bottom: 1px solid #f3f4f6; vertical-align: middle; font-size: 14px; }
        .table tbody tr:hover { background: #f9fafb; }

        .status-badge { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
        .status-normal { background: #d1fae5; color: #065f46; }
        .status-blocked { background: #fee2e2; color: #991b1b; }

        .stars { color: #f59e0b; }

        .review-img-thumb { width: 50px; height: 50px; border-radius: 6px; object-fit: cover; cursor: pointer; }

        .action-btns { display: flex; gap: 6px; flex-wrap: wrap; }

        .empty-state { text-align: center; padding: 60px; color: #9ca3af; }

        .modal-img { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.8); display: flex; align-items: center; justify-content: center; z-index: 9999; }
        .modal-img img { max-width: 90%; max-height: 90%; border-radius: 8px; }
    </style>
</head>
<body>

<div class="admin-header">
    <h1>📚 BookVerse 后台管理</h1>
    <div>
        <a href="${pageContext.request.contextPath}/">🌐 查看商城</a>
        <a href="${pageContext.request.contextPath}/admin/logout">🚪 退出</a>
    </div>
</div>

<div class="admin-layout">
    <jsp:include page="../../common/admin_sidebar.jsp" />

    <div class="admin-content">
        <div class="card">
            <div class="card-header">
                <h2>⭐ 评价管理</h2>
            </div>
            <div class="toolbar">
                <input type="text" id="searchKeyword" placeholder="搜索评价内容..." value="${keyword}" />
                <select id="statusFilter">
                    <option value="" ${empty statusFilter ? 'selected' : ''}>全部状态</option>
                    <option value="normal" ${statusFilter == 'normal' ? 'selected' : ''}>正常</option>
                    <option value="blocked" ${statusFilter == 'blocked' ? 'selected' : ''}>已屏蔽</option>
                </select>
                <button class="btn btn-primary" onclick="doSearch()"> 搜索</button>
                <span style="color: #9ca3af; font-size: 13px;">共 ${fn:length(reviewList)} 条评价</span>
            </div>
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户ID</th>
                        <th>商品ID</th>
                        <th>评分</th>
                        <th>评价内容</th>
                        <th>晒图</th>
                        <th>状态</th>
                        <th>时间</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${reviewList}" var="r">
                        <tr>
                            <td>${r.id}</td>
                            <td>${r.userId}</td>
                            <td>${r.productId}</td>
                            <td>
                                <span class="stars">
                                    <c:forEach begin="1" end="${r.rating}" var="s">★</c:forEach>
                                    <c:forEach begin="${r.rating + 1}" end="5" var="s">☆</c:forEach>
                                </span>
                            </td>
                            <td style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${r.content}">${r.content}</td>
                            <td>
                                <c:if test="${not empty r.image}">
                                    <img src="${pageContext.request.contextPath}/img/books/${r.image}" alt="" class="review-img-thumb" onclick="showImage('${pageContext.request.contextPath}/img/books/${r.image}')">
                                </c:if>
                                <c:if test="${empty r.image}">
                                    <span style="color: #ccc;">-</span>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${r.blocked == 1}">
                                        <span class="status-badge status-blocked">已屏蔽</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge status-normal">正常</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><fmt:formatDate value="${r.createTime}" pattern="yyyy-MM-dd"/></td>
                            <td>
                                <div class="action-btns">
                                    <c:choose>
                                        <c:when test="${r.blocked == 1}">
                                            <button class="btn btn-sm btn-success" onclick="unblockReview(${r.id}, this)">解封</button>
                                        </c:when>
                                        <c:otherwise>
                                            <button class="btn btn-sm btn-warning" onclick="blockReview(${r.id}, this)">屏蔽</button>
                                        </c:otherwise>
                                    </c:choose>
                                    <button class="btn btn-sm btn-danger" onclick="deleteReview(${r.id}, this)">删除</button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <c:if test="${empty reviewList}">
                <div class="empty-state">暂无评价数据</div>
            </c:if>
        </div>
    </div>
</div>

<script>
    function doSearch() {
        var kw = $('#searchKeyword').val();
        var st = $('#statusFilter').val();
        var url = '${pageContext.request.contextPath}/admin/review?';
        if (kw) url += 'keyword=' + encodeURIComponent(kw) + '&';
        if (st) url += 'status=' + st;
        window.location.href = url;
    }

    function blockReview(id, btn) {
        if (!confirm('确定屏蔽这条评价吗？屏蔽后用户将无法看到该评价。')) return;
        $.post('${pageContext.request.contextPath}/admin/review/block', { id: id }, function(data) {
            if (data.success) {
                alert('屏蔽成功');
                location.reload();
            } else {
                alert(data.message || '操作失败');
            }
        }, 'json');
    }

    function unblockReview(id, btn) {
        if (!confirm('确定解封这条评价吗？')) return;
        $.post('${pageContext.request.contextPath}/admin/review/unblock', { id: id }, function(data) {
            if (data.success) {
                alert('解封成功');
                location.reload();
            } else {
                alert(data.message || '操作失败');
            }
        }, 'json');
    }

    function deleteReview(id, btn) {
        if (!confirm('确定删除这条评价吗？此操作不可恢复！')) return;
        $.get('${pageContext.request.contextPath}/admin/review/delete', { id: id }, function(data) {
            if (data.success) {
                alert('删除成功');
                location.reload();
            } else {
                alert(data.message || '操作失败');
            }
        }, 'json');
    }

    function showImage(src) {
        var modal = document.createElement('div');
        modal.className = 'modal-img';
        modal.innerHTML = '<img src="' + src + '" onclick="this.parentElement.remove()" />';
        modal.onclick = function() { this.remove(); };
        document.body.appendChild(modal);
    }
</script>

</body>
</html>
