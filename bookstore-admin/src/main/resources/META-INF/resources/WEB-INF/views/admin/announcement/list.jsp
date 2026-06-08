<%-- ============================================================
     公告管理页面 (list.jsp)
     功能：管理员查看、发布和删除系统公告
     技术要点：
       - 使用 Bootstrap 模态框（modal）实现发布公告的弹窗表单
       - 公告数据通过 ${announcementList} 从后端获取
       - 删除操作通过 confirm() 确认后执行
       - 页面顶部有登录状态检查，未登录则跳转到登录页
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 引入格式化标签库，用于日期格式化 --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- ==================== 登录状态检查 ==================== --%>
<%-- 如果 session 中没有 admin 对象（未登录），则重定向到登录页面 --%>
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
        <%-- ==================== 左侧导航栏 ==================== --%>
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

        <%-- ==================== 右侧主内容区 ==================== --%>
        <div class="col-md-10" style="padding: 40px;">
            <h2 class="section-title">📢 公告管理</h2>

            <%-- 发布公告按钮，点击后弹出模态框 --%>
            <button class="btn btn-gradient" data-toggle="modal" data-target="#addModal">➕ 发布公告</button>
            <br><br>

            <%-- ==================== 公告列表表格 ==================== --%>
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
                    <%-- 遍历公告列表，每行显示一条公告 --%>
                    <c:forEach items="${announcementList}" var="item">
                        <tr>
                            <td>${item.id}</td>
                            <td><strong>${item.title}</strong></td>
                            <td>${item.content}</td>
                            <%-- 格式化显示发布时间 --%>
                            <td><fmt:formatDate value="${item.created_at}" pattern="yyyy-MM-dd HH:mm"/></td>
                            <td>
                                <%-- 根据状态值显示不同标签：1=启用，其他=禁用 --%>
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
                                <%-- 删除按钮，onclick 中用 confirm() 弹出确认对话框 --%>
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

<%-- ==================== 发布公告模态框（弹窗） ==================== --%>
<%-- Bootstrap 模态框组件，点击"发布公告"按钮后弹出 --%>
<div class="modal fade" id="addModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <%-- 表单提交到 /admin/announcement?action=add --%>
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
