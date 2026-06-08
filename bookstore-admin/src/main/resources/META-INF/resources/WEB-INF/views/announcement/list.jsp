<%-- ============================================================
     announcement/list.jsp — 前台公告列表页面
     功能：展示系统公告和通知信息列表。
     说明：从后端获取公告数据，按时间倒序展示。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${empty sessionScope.admin}">
    <c:redirect url="${pageContext.request.contextPath}/admin/login"/>
</c:if>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>公告管理 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-2 sidebar">
            <h3>📚 BookVerse 管理后台</h3>
            <a href="${pageContext.request.contextPath}/admin/index">🏠 首页概览</a>
            <a href="${pageContext.request.contextPath}/admin/product">📦 图书管理</a>
            <a href="${pageContext.request.contextPath}/admin/order">📋 订单管理</a>
            <a href="${pageContext.request.contextPath}/admin/user">👥 用户管理</a>
            <a href="${pageContext.request.contextPath}/admin/announcement" class="active">📢 公告管理</a>
            <a href="${pageContext.request.contextPath}/" target="_blank">🌐 查看商城</a>
            <a href="${pageContext.request.contextPath}/admin/logout">🚪 退出登录</a>
        </div>

        <div class="col-md-10" style="padding: 40px;">
            <h2 class="section-title">📢 公告管理</h2>

            <button class="btn btn-gradient" data-toggle="modal" data-target="#addModal">➕ 发布公告</button>
            <br><br>

            <div class="order-card">
                <table class="table">
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
                            <td><strong>${item.title}</strong></td>
                            <td>${item.content}</td>
                            <td><fmt:formatDate value="${item.created_at}" pattern="yyyy-MM-dd HH:mm"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.status == 1}">
                                        <span class="label label-success">启用</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="label label-default">禁用</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/announcement/delete?id=${item.id}"
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
</div>

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
