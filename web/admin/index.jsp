<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${empty sessionScope.admin}">
    <c:redirect url="${pageContext.request.contextPath}/admin/login.jsp"/>
</c:if>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>管理后台 - 在线图书销售平台</title>
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
        .main-content {
            padding: 30px;
        }
        .stat-card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .stat-card h3 {
            margin: 0;
            color: #2c3e50;
        }
        .stat-card p {
            margin: 10px 0 0;
            color: #7f8c8d;
        }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <!-- 侧边栏 -->
        <div class="col-md-2 sidebar">
            <h3>📚 管理后台</h3>
            <a href="${pageContext.request.contextPath}/admin/index.jsp" class="active">🏠 首页概览</a>
            <a href="${pageContext.request.contextPath}/admin/product">📦 图书管理</a>
            <a href="${pageContext.request.contextPath}/admin/order">📋 订单管理</a>
            <a href="${pageContext.request.contextPath}/admin/user">👥 用户管理</a>
            <a href="${pageContext.request.contextPath}/admin/announcement">📢 公告管理</a>
            <hr style="border-color: #34495e;">
            <a href="${pageContext.request.contextPath}/index.jsp" target="_blank">🌐 查看商城</a>
            <a href="${pageContext.request.contextPath}/admin/logout" style="color: #e74c3c;">🚪 退出登录</a>
        </div>

        <!-- 主内容区 -->
        <div class="col-md-10 main-content">
            <h2>欢迎回来，管理员！</h2>
            <hr>

            <div class="row">
                <div class="col-md-3">
                    <div class="stat-card">
                        <h3>📦 图书管理</h3>
                        <p>新增、编辑、删除图书<br>管理上下架状态</p>
                        <a href="${pageContext.request.contextPath}/admin/product" class="btn btn-primary btn-sm">进入管理</a>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <h3>📋 订单管理</h3>
                        <p>查看所有订单<br>修改发货状态</p>
                        <a href="${pageContext.request.contextPath}/admin/order" class="btn btn-success btn-sm">进入管理</a>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <h3>👥 用户管理</h3>
                        <p>查看注册用户<br>管理用户账号</p>
                        <a href="${pageContext.request.contextPath}/admin/user" class="btn btn-info btn-sm">进入管理</a>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <h3>📢 公告管理</h3>
                        <p>发布系统公告<br>编辑公告内容</p>
                        <a href="${pageContext.request.contextPath}/admin/announcement" class="btn btn-warning btn-sm">进入管理</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
