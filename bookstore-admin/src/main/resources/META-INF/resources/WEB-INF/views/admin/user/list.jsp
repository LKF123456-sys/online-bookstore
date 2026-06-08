<%-- ============================================================
     用户管理列表页面 (user/list.jsp)
     功能：展示所有注册用户的管理列表
     主要功能：
       - 按用户名/姓名/邮箱搜索用户
       - 查看用户列表（用户名、姓名、邮箱、电话、城市、角色、状态）
       - 启用/禁用用户（AJAX无刷新操作）
       - 删除用户（AJAX无刷新操作，需二次确认，admin账号不可删除）
       - 跳转到新增/编辑用户页面
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
    <title>用户管理 - BookVerse</title>
    <%-- 引入 Bootstrap 样式和自定义样式 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <%-- 引入 jQuery 和 Bootstrap JS（用于AJAX操作） --%>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        /* ==================== 工具栏样式 ==================== */
        .toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-wrap: wrap; gap: 10px; }
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
        <%-- 引入管理后台侧边栏，传入page参数标识当前页面 --%>
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp">
            <jsp:param name="page" value="user"/>
        </jsp:include>
        <%-- 右侧主内容区（占10列） --%>
        <div class="col-md-10">
            <div class="panel panel-default">
                <%-- 面板标题 --%>
                <div class="panel-heading">
                    <h3 class="panel-title">👥 用户管理</h3>
                </div>
                <div class="panel-body">
                    <%-- ==================== 工具栏区域 ==================== --%>
                    <div class="toolbar">
                        <%-- 左侧：搜索表单 --%>
                        <div class="search-form">
                            <form action="${pageContext.request.contextPath}/admin/user" method="get" class="form-inline">
                                <%-- 搜索关键词输入框 --%>
                                <input type="text" name="keyword" class="form-control input-sm" placeholder="搜索用户名/姓名/邮箱..." value="<c:out value="${keyword}"/>"/>
                                <button type="submit" class="btn btn-sm btn-primary">🔍 搜索</button>
                                <%-- 有搜索关键词时显示"清除"按钮 --%>
                                <c:if test="${not empty keyword}">
                                    <a href="${pageContext.request.contextPath}/admin/user" class="btn btn-sm btn-default">清除</a>
                                </c:if>
                            </form>
                        </div>
                        <%-- 右侧：新增用户按钮 --%>
                        <a href="${pageContext.request.contextPath}/admin/user/add" class="btn btn-sm btn-gradient">➕ 新增用户</a>
                    </div>
                    <%-- ==================== 操作结果提示 ==================== --%>
                    <c:if test="${param.success != null}">
                        <div class="alert alert-${param.success == 'true' ? 'success' : 'danger'} alert-dismissible">
                            <button type="button" class="close" data-dismiss="alert">&times;</button>
                            ${param.success == 'true' ? '操作成功' : '操作失败'}
                        </div>
                    </c:if>
                    <%-- ==================== 用户数据表格 ==================== --%>
                    <table class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>用户名</th>
                                <th>姓名</th>
                                <th>邮箱</th>
                                <th>电话</th>
                                <th>城市</th>
                                <th>角色</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%-- 使用 JSTL forEach 循环遍历用户列表 --%>
                            <c:forEach items="${userList}" var="user">
                                <tr>
                                    <td><strong>${user.userid}</strong></td>
                                    <td>${user.firstname} ${user.lastname}</td>
                                    <td>${user.email}</td>
                                    <td>${user.phone}</td>
                                    <td>${user.city}</td>
                                    <td>
                                        <%-- 根据角色值显示不同标签：admin=管理员(红色)，其他=普通用户(蓝色) --%>
                                        <c:choose>
                                            <c:when test="${user.role == 'admin'}">
                                                <span class="label label-danger">管理员</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="label label-info">普通用户</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <%-- 根据状态值显示不同标签：1=正常(绿色)，0=禁用(灰色) --%>
                                        <c:choose>
                                            <c:when test="${user.status == 1}">
                                                <span class="label label-success">正常</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="label label-default">禁用</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <%-- 操作按钮：编辑、启用/禁用切换、删除 --%>
                                        <a href="${pageContext.request.contextPath}/admin/user/edit?userid=${user.userid}" class="btn btn-sm btn-info">编辑</a>
                                        <%-- 正常状态显示"禁用"按钮 --%>
                                        <c:if test="${user.status == 1}">
                                            <button class="btn btn-sm btn-warning" onclick="toggleUserStatus('${user.userid}', 'disable')">禁用</button>
                                        </c:if>
                                        <%-- 禁用状态显示"启用"按钮 --%>
                                        <c:if test="${user.status == 0}">
                                            <button class="btn btn-sm btn-success" onclick="toggleUserStatus('${user.userid}', 'enable')">启用</button>
                                        </c:if>
                                        <%-- admin 账号不允许删除（安全保护） --%>
                                        <c:if test="${user.userid != 'admin'}">
                                            <button class="btn btn-sm btn-danger" onclick="deleteUser('${user.userid}')">删除</button>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <%-- 用户列表为空时显示提示 --%>
                            <c:if test="${empty userList}">
                                <tr><td colspan="8" class="text-center" style="color: var(--gray-500);">暂无用户数据</td></tr>
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
     * 切换用户启用/禁用状态
     * @param userid - 用户ID
     * @param action - 操作类型：'enable'(启用) 或 'disable'(禁用)
     */
    function toggleUserStatus(userid, action) {
        // 禁用操作需要二次确认
        if (action === 'disable' && !confirm('确定要禁用该用户吗？')) return;
        // 发送AJAX请求到后端切换状态
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/user/' + action,
            type: 'GET',
            data: { userid: userid },
            success: function(data) {
                if (data.success) location.reload();  // 成功则刷新页面
                else alert(data.message);               // 失败则显示错误信息
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }

    /**
     * 删除用户
     * @param userid - 要删除的用户ID
     */
    function deleteUser(userid) {
        // 删除操作需要二次确认，提示不可撤销
        if (!confirm('确定要删除用户 "' + userid + '" 吗？此操作不可撤销！')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/user/delete',
            type: 'GET',
            data: { userid: userid },
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