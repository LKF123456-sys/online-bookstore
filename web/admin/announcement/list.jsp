<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${empty sessionScope.admin}">
    <c:redirect url="${pageContext.request.contextPath}/admin/login.jsp"/>
</c:if>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>公告管理 - 管理后台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        body { background-color: #f5f5f5; }
        .sidebar {
            background: #2c3e50;
            min-height: 100vh;
            padding-top: 20px;
        }
        .sidebar a {
            color: #ecf0f1;
            padding: 15px 20px;
            display: block;
            text-decoration: none;
            border-left: 3px solid transparent;
        }
        .sidebar a:hover, .sidebar a.active {
            background: #34495e;
            border-left-color: #3498db;
        }
        .sidebar h3 {
            color: white;
            text-align: center;
            padding: 20px;
            border-bottom: 1px solid #34495e;
        }
        .main-content { padding: 30px; }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-2 sidebar">
            <h3>📚 管理后台</h3>
            <a href="${pageContext.request.contextPath}/admin/index.jsp">🏠 首页概览</a>
            <a href="${pageContext.request.contextPath}/admin/product">📦 图书管理</a>
            <a href="${pageContext.request.contextPath}/admin/order">📋 订单管理</a>
            <a href="${pageContext.request.contextPath}/admin/user">👥 用户管理</a>
            <a href="${pageContext.request.contextPath}/admin/announcement" class="active">📢 公告管理</a>
            <hr style="border-color: #34495e;">
            <a href="${pageContext.request.contextPath}/index.jsp" target="_blank">🌐 查看商城</a>
            <a href="${pageContext.request.contextPath}/admin/logout" style="color: #e74c3c;">🚪 退出登录</a>
        </div>

        <div class="col-md-10 main-content">
            <h2>📢 公告管理</h2>
            <hr>

            <button class="btn btn-success" data-toggle="modal" data-target="#addModal">➕ 发布公告</button>
            <br><br>

            <table class="table table-bordered table-hover bg-white">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>标题</th>
                    <th>内容</th>
                    <th>发布时间</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${announcementList}" var="item">
                    <tr>
                        <td>${item.id}</td>
                        <td>${item.title}</td>
                        <td>${item.content}</td>
                        <td><fmt:formatDate value="${item.create_time}" pattern="yyyy-MM-dd HH:mm"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${item.status == 1}">
                                    <span class="label label-success">启用</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="label label-danger">禁用</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <button class="btn btn-sm btn-info"
                                    onclick="alert('编辑功能开发中：${item.title}')">
                                编辑
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/announcement?action=delete&id=${item.id}"
                               class="btn btn-sm btn-danger"
                               onclick="return confirm('确定删除吗？')">
                                删除
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- 发布公告模态框 -->
<div class="modal fade" id="addModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/admin/announcement?action=add" method="post">
                <div class="modal-header">
                    <h4>发布公告</h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label>公告标题</label>
                        <input type="text" name="title" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>公告内容</label>
                        <textarea name="content" class="form-control" rows="5" required></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button type="submit" class="btn btn-success">发布</button>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
