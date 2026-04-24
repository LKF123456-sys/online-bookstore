<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.bookstore.utils.DBUtil" %>
<c:if test="${empty sessionScope.admin}">
    <c:redirect url="${pageContext.request.contextPath}/admin/login.jsp"/>
</c:if>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户管理 - 管理后台</title>
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
            <a href="${pageContext.request.contextPath}/admin/user" class="active">👥 用户管理</a>
            <a href="${pageContext.request.contextPath}/admin/announcement">📢 公告管理</a>
            <hr style="border-color: #34495e;">
            <a href="${pageContext.request.contextPath}/index.jsp" target="_blank">🌐 查看商城</a>
            <a href="${pageContext.request.contextPath}/admin/logout" style="color: #e74c3c;">🚪 退出登录</a>
        </div>

        <div class="col-md-10 main-content">
            <h2>👥 用户管理</h2>
            <hr>

            <table class="table table-bordered table-hover bg-white">
                <thead>
                <tr>
                    <th>用户名</th>
                    <th>姓名</th>
                    <th>邮箱</th>
                    <th>电话</th>
                    <th>城市</th>
                    <th>地址</th>
                    <th>状态</th>
                </tr>
                </thead>
                <tbody>
                <%
                    Connection conn = null;
                    PreparedStatement pstmt = null;
                    ResultSet rs = null;
                    try {
                        conn = DBUtil.getConnection();
                        String sql = "SELECT * FROM account ORDER BY userid";
                        pstmt = conn.prepareStatement(sql);
                        rs = pstmt.executeQuery();
                        while (rs.next()) {
                %>
                <tr>
                    <td><%= rs.getString("userid") %></td>
                    <td><%= rs.getString("firstname") %> <%= rs.getString("lastname") %></td>
                    <td><%= rs.getString("email") %></td>
                    <td><%= rs.getString("phone") %></td>
                    <td><%= rs.getString("city") %></td>
                    <td><%= rs.getString("addr1") %></td>
                    <td>
                        <% if (rs.getInt("status") == 1) { %>
                        <span class="label label-success">正常</span>
                        <% } else { %>
                        <span class="label label-danger">禁用</span>
                        <% } %>
                    </td>
                </tr>
                <%
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (rs != null) rs.close();
                        if (pstmt != null) pstmt.close();
                        if (conn != null) conn.close();
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
