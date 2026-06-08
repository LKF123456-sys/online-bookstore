<%-- ============================================================
     评价管理页面 (review/list.jsp)
     功能：管理用户对商品的评价内容
     主要功能：
       - 按关键词搜索评价内容
       - 按状态筛选（全部/正常/已屏蔽）
       - 查看评价列表（用户ID、商品ID、评分星级、评价内容、晒图、状态、时间）
       - 屏蔽/解封评价（AJAX无刷新操作）
       - 删除评价（AJAX无刷新操作，需二次确认）
       - 点击晒图缩略图可弹出大图预览
     使用了自定义卡片样式，不依赖 Bootstrap 面板
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- fn 标签库：用于调用字符串函数，如 fn:length() 计算集合长度 --%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>评价管理 - BookVerse Admin</title>
    <%-- 引入 Bootstrap 样式和 jQuery --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        /* ==================== 基础重置 ==================== */
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #f5f5f5; color: #333; margin: 0; }

        /* ==================== 顶部导航栏 ==================== */
        .admin-header { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; padding: 15px 30px; display: flex; align-items: center; justify-content: space-between; }
        .admin-header h1 { margin: 0; font-size: 20px; }
        .admin-header a { color: #fff; text-decoration: none; padding: 8px 16px; border-radius: 5px; background: rgba(255,255,255,0.2); }
        .admin-header a:hover { background: rgba(255,255,255,0.3); }

        /* ==================== 页面布局 ==================== */
        .admin-layout { display: flex; min-height: calc(100vh - 50px); }
        .admin-content { flex: 1; padding: 24px; }

        /* ==================== 卡片容器 ==================== */
        .card { background: #fff; border-radius: 12px; box-shadow: 0 1px 6px rgba(0,0,0,0.08); margin-bottom: 20px; }
        .card-header { padding: 20px 24px; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: space-between; }
        .card-header h2 { margin: 0; font-size: 18px; }

        /* ==================== 搜索工具栏 ==================== */
        .toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; padding: 16px 24px; border-bottom: 1px solid #eee; }
        .toolbar input[type="text"] { padding: 8px 14px; border: 2px solid #e5e7eb; border-radius: 8px; font-size: 14px; outline: none; width: 200px; }
        .toolbar input[type="text"]:focus { border-color: #667eea; }
        .toolbar select { padding: 8px 14px; border: 2px solid #e5e7eb; border-radius: 8px; font-size: 14px; outline: none; }
        .toolbar select:focus { border-color: #667eea; }

        /* ==================== 按钮样式 ==================== */
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

        /* ==================== 表格样式 ==================== */
        .table { width: 100%; border-collapse: collapse; }
        .table th { padding: 12px 16px; font-size: 13px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid #e5e7eb; text-align: left; }
        .table td { padding: 14px 16px; border-bottom: 1px solid #f3f4f6; vertical-align: middle; font-size: 14px; }
        .table tbody tr:hover { background: #f9fafb; }

        /* ==================== 状态标签 ==================== */
        .status-badge { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
        .status-normal { background: #d1fae5; color: #065f46; }   /* 正常=绿色 */
        .status-blocked { background: #fee2e2; color: #991b1b; }   /* 已屏蔽=红色 */

        /* ==================== 星级评分样式 ==================== */
        .stars { color: #f59e0b; }

        /* ==================== 评价图片缩略图 ==================== */
        .review-img-thumb { width: 50px; height: 50px; border-radius: 6px; object-fit: cover; cursor: pointer; }

        /* ==================== 操作按钮组 ==================== */
        .action-btns { display: flex; gap: 6px; flex-wrap: wrap; }

        /* ==================== 空状态提示 ==================== */
        .empty-state { text-align: center; padding: 60px; color: #9ca3af; }

        /* ==================== 图片大图预览弹窗 ==================== */
        /* 全屏遮罩层 + 居中显示大图，点击任意位置关闭 */
        .modal-img { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.8); display: flex; align-items: center; justify-content: center; z-index: 9999; }
        .modal-img img { max-width: 90%; max-height: 90%; border-radius: 8px; }
    </style>
</head>
<body>

<%-- ==================== 顶部导航栏 ==================== --%>
<div class="admin-header">
    <h1>📚 BookVerse 后台管理</h1>
    <div>
        <a href="${pageContext.request.contextPath}/">🌐 查看商城</a>
        <a href="${pageContext.request.contextPath}/admin/logout">🚪 退出</a>
    </div>
</div>

<%-- ==================== 页面主体布局 ==================== --%>
<div class="admin-layout">
    <%-- 引入侧边栏 --%>
    <jsp:include page="../../common/admin_sidebar.jsp" />

    <%-- 右侧内容区 --%>
    <div class="admin-content">
        <div class="card">
            <%-- 卡片标题 --%>
            <div class="card-header">
                <h2>⭐ 评价管理</h2>
            </div>
            <%-- ==================== 搜索和筛选工具栏 ==================== --%>
            <div class="toolbar">
                <%-- 关键词搜索输入框 --%>
                <input type="text" id="searchKeyword" placeholder="搜索评价内容..." value="${keyword}" />
                <%-- 状态下拉筛选 --%>
                <select id="statusFilter">
                    <option value="" ${empty statusFilter ? 'selected' : ''}>全部状态</option>
                    <option value="normal" ${statusFilter == 'normal' ? 'selected' : ''}>正常</option>
                    <option value="blocked" ${statusFilter == 'blocked' ? 'selected' : ''}>已屏蔽</option>
                </select>
                <button class="btn btn-primary" onclick="doSearch()"> 搜索</button>
                <%-- 显示评价总数 --%>
                <span style="color: #9ca3af; font-size: 13px;">共 ${fn:length(reviewList)} 条评价</span>
            </div>
            <%-- ==================== 评价数据表格 ==================== --%>
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
                    <%-- 循环遍历评价列表 --%>
                    <c:forEach items="${reviewList}" var="r">
                        <tr>
                            <td>${r.id}</td>
                            <td>${r.userId}</td>
                            <td>${r.productId}</td>
                            <td>
                                <%-- 根据评分值显示实心星★和空心星☆ --%>
                                <span class="stars">
                                    <c:forEach begin="1" end="${r.rating}" var="s">★</c:forEach>
                                    <c:forEach begin="${r.rating + 1}" end="5" var="s">☆</c:forEach>
                                </span>
                            </td>
                            <%-- 评价内容超长时截断显示，鼠标悬停显示完整内容 --%>
                            <td style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${r.content}">${r.content}</td>
                            <td>
                                <%-- 如果有晒图，显示缩略图，点击可弹出大图 --%>
                                <c:if test="${not empty r.image}">
                                    <img src="${pageContext.request.contextPath}/img/books/${r.image}" alt="" class="review-img-thumb" onclick="showImage('${pageContext.request.contextPath}/img/books/${r.image}')">
                                </c:if>
                                <c:if test="${empty r.image}">
                                    <span style="color: #ccc;">-</span>
                                </c:if>
                            </td>
                            <td>
                                <%-- 根据 blocked 字段显示状态标签 --%>
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
                                    <%-- 已屏蔽的显示"解封"按钮，正常的显示"屏蔽"按钮 --%>
                                    <c:choose>
                                        <c:when test="${r.blocked == 1}">
                                            <button class="btn btn-sm btn-success" onclick="unblockReview(${r.id}, this)">解封</button>
                                        </c:when>
                                        <c:otherwise>
                                            <button class="btn btn-sm btn-warning" onclick="blockReview(${r.id}, this)">屏蔽</button>
                                        </c:otherwise>
                                    </c:choose>
                                    <%-- 删除按钮 --%>
                                    <button class="btn btn-sm btn-danger" onclick="deleteReview(${r.id}, this)">删除</button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <%-- 评价列表为空时的提示 --%>
            <c:if test="${empty reviewList}">
                <div class="empty-state">暂无评价数据</div>
            </c:if>
        </div>
    </div>
</div>

<%-- ==================== JavaScript 部分 ==================== --%>
<script>
    /**
     * 执行搜索：拼接关键词和状态筛选参数，跳转到新URL
     */
    function doSearch() {
        var kw = $('#searchKeyword').val();       // 获取搜索关键词
        var st = $('#statusFilter').val();         // 获取状态筛选值
        var url = '${pageContext.request.contextPath}/admin/review?';
        if (kw) url += 'keyword=' + encodeURIComponent(kw) + '&';  // URL编码中文关键词
        if (st) url += 'status=' + st;
        window.location.href = url;               // 跳转到筛选后的URL
    }

    /**
     * 屏蔽评价
     * @param id - 评价ID
     * @param btn - 触发按钮的DOM元素
     */
    function blockReview(id, btn) {
        if (!confirm('确定屏蔽这条评价吗？屏蔽后用户将无法看到该评价。')) return;
        // 发送POST请求到后端屏蔽接口
        $.post('${pageContext.request.contextPath}/admin/review/block', { id: id }, function(data) {
            if (data.success) {
                alert('屏蔽成功');
                location.reload();       // 成功后刷新页面
            } else {
                alert(data.message || '操作失败');
            }
        }, 'json');
    }

    /**
     * 解封评价
     * @param id - 评价ID
     * @param btn - 触发按钮的DOM元素
     */
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

    /**
     * 删除评价
     * @param id - 评价ID
     * @param btn - 触发按钮的DOM元素
     */
    function deleteReview(id, btn) {
        if (!confirm('确定删除这条评价吗？此操作不可恢复！')) return;
        // 发送GET请求到后端删除接口
        $.get('${pageContext.request.contextPath}/admin/review/delete', { id: id }, function(data) {
            if (data.success) {
                alert('删除成功');
                location.reload();
            } else {
                alert(data.message || '操作失败');
            }
        }, 'json');
    }

    /**
     * 显示图片大图预览
     * 创建一个全屏遮罩层，在其中居中显示大图
     * @param src - 图片URL
     */
    function showImage(src) {
        var modal = document.createElement('div');  // 创建遮罩层
        modal.className = 'modal-img';
        modal.innerHTML = '<img src="' + src + '" onclick="this.parentElement.remove()" />';
        modal.onclick = function() { this.remove(); };  // 点击遮罩层关闭
        document.body.appendChild(modal);           // 添加到页面
    }
</script>

</body>
</html>