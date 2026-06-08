<%-- ============================================================
     user/list.jsp — 前台用户列表页面
     功能：管理员查看所有注册用户的信息列表。
     说明：管理员专用页面，展示用户的账号、昵称、注册时间等。
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
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp">
            <jsp:param name="page" value="user"/>
        </jsp:include>
        <div class="col-md-10">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">👥 用户管理</h3>
                </div>
                <div class="panel-body">
                    <div class="toolbar">
                        <div class="search-form">
                            <form action="${pageContext.request.contextPath}/admin/user" method="get" class="form-inline">
                                <input type="text" name="keyword" class="form-control input-sm" placeholder="搜索用户名/姓名/邮箱..." value="<c:out value="${keyword}"/>"/>
                                <button type="submit" class="btn btn-sm btn-primary">🔍 搜索</button>
                                <c:if test="${not empty keyword}">
                                    <a href="${pageContext.request.contextPath}/admin/user" class="btn btn-sm btn-default">清除</a>
                                </c:if>
                            </form>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/user/add" class="btn btn-sm btn-gradient">➕ 新增用户</a>
                    </div>
                    <c:if test="${param.success != null}">
                        <div class="alert alert-${param.success == 'true' ? 'success' : 'danger'} alert-dismissible">
                            <button type="button" class="close" data-dismiss="alert">&times;</button>
                            ${param.success == 'true' ? '操作成功' : '操作失败'}
                        </div>
                    </c:if>
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
                            <c:forEach items="${userList}" var="user">
                                <tr>
                                    <td><strong>${user.userid}</strong></td>
                                    <td>${user.firstname} ${user.lastname}</td>
                                    <td>${user.email}</td>
                                    <td>${user.phone}</td>
                                    <td>${user.city}</td>
                                    <td>
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
                                        <a href="${pageContext.request.contextPath}/admin/user/edit?userid=${user.userid}" class="btn btn-sm btn-info">编辑</a>
                                        <c:if test="${user.status == 1}">
                                            <button class="btn btn-sm btn-warning" onclick="toggleUserStatus('${user.userid}', 'disable')">禁用</button>
                                        </c:if>
                                        <c:if test="${user.status == 0}">
                                            <button class="btn btn-sm btn-success" onclick="toggleUserStatus('${user.userid}', 'enable')">启用</button>
                                        </c:if>
                                        <c:if test="${user.userid != 'admin'}">
                                            <button class="btn btn-sm btn-danger" onclick="deleteUser('${user.userid}')">删除</button>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
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
<script type="text/javascript">
    function toggleUserStatus(userid, action) {
        if (action === 'disable' && !confirm('确定要禁用该用户吗？')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/user/' + action,
            type: 'GET',
            data: { userid: userid },
            success: function(data) {
                if (data.success) location.reload();
                else alert(data.message);
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }
    function deleteUser(userid) {
        if (!confirm('确定要删除用户 "' + userid + '" 吗？此操作不可撤销！')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/user/delete',
            type: 'GET',
            data: { userid: userid },
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
