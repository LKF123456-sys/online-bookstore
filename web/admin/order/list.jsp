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
    <title>订单管理 - 管理后台</title>
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
            <a href="${pageContext.request.contextPath}/admin/order" class="active">📋 订单管理</a>
            <a href="${pageContext.request.contextPath}/admin/user">👥 用户管理</a>
            <a href="${pageContext.request.contextPath}/admin/announcement">📢 公告管理</a>
            <hr style="border-color: #34495e;">
            <a href="${pageContext.request.contextPath}/index.jsp" target="_blank">🌐 查看商城</a>
            <a href="${pageContext.request.contextPath}/admin/logout" style="color: #e74c3c;">🚪 退出登录</a>
        </div>

        <div class="col-md-10 main-content">
            <h2>📋 订单管理</h2>
            <hr>

            <table class="table table-bordered table-hover bg-white">
                <thead>
                <tr>
                    <th>订单号</th>
                    <th>用户</th>
                    <th>订单时间</th>
                    <th>收货人</th>
                    <th>收货地址</th>
                    <th>总金额</th>
                    <th>物流信息</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${orderList}" var="order">
                    <tr>
                        <td>${order.orderid}</td>
                        <td>${order.userid}</td>
                        <td><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm"/></td>
                        <td>${order.billtofirstname} ${order.billtolastname}</td>
                        <td>${order.shipcity} ${order.shipaddr1}</td>
                        <td>¥${order.totalprice}</td>
                        <td>${order.courier != null ? order.courier : '<span class="text-muted">未发货</span>'}</td>
                        <td>
                            <button class="btn btn-sm btn-primary"
                                    data-toggle="modal"
                                    data-target="#shipModal${order.orderid}">
                                    ${order.courier != null ? '修改物流' : '发货'}
                            </button>
                        </td>
                    </tr>

                    <!-- 发货模态框 -->
                    <div class="modal fade" id="shipModal${order.orderid}">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <form action="${pageContext.request.contextPath}/admin/order" method="post">
                                    <div class="modal-header">
                                        <h4>发货 - 订单 ${order.orderid}</h4>
                                    </div>
                                    <div class="modal-body">
                                        <input type="hidden" name="orderId" value="${order.orderid}">
                                        <div class="form-group">
                                            <label>物流公司/快递单号</label>
                                            <input type="text" name="courier" class="form-control"
                                                   value="${order.courier}" placeholder="例如：顺丰 SF1234567890">
                                        </div>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                                        <button type="submit" class="btn btn-success">保存</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
