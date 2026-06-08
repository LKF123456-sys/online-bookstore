<%-- ============================================================
     分类管理页面 (list.jsp)
     功能：管理员查看图书分类列表
     技术要点：
       - 自带侧边栏导航（非公共组件引入方式）
       - 分类数据通过 ${categoryList} 从后端获取
       - 当前页面通过 class="active" 高亮侧边栏对应菜单
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 引入 JSTL 核心标签库 --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>分类管理 - BookVerse 管理后台</title>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        /* 页面背景色 */
        body { background: #f0f2f5; }
        /* 整体布局容器，使用 flex 布局 */
        .wrapper { display: flex; min-height: 100vh; }
        /* 左侧侧边栏样式 */
        .sidebar { width: 240px; background: #2c3e50; color: #ecf0f1; position: fixed; height: 100vh; overflow-y: auto; }
        .sidebar .logo { padding: 20px; text-align: center; background: #1a252f; }
        .sidebar .logo h3 { color: #3498db; margin: 0; }
        .sidebar ul { list-style: none; padding: 0; margin: 0; }
        .sidebar ul li a { display: block; padding: 12px 20px; color: #bdc3c7; text-decoration: none; transition: all 0.3s; }
        /* 菜单项悬停和激活样式 */
        .sidebar ul li a:hover, .sidebar ul li a.active { background: #34495e; color: #fff; }
        /* 右侧主内容区域 */
        .main-content { margin-left: 240px; padding: 20px; flex: 1; }
        .page-header { margin-bottom: 20px; }
        .page-header h2 { font-size: 22px; color: #2c3e50; font-weight: 600; }
    </style>
</head>
<body>
<div class="wrapper">
    <%-- ==================== 左侧导航栏 ==================== --%>
    <div class="sidebar">
        <div class="logo"><h3>BookVerse</h3><p>管理后台</p></div>
        <ul>
            <li><a href="/admin/index">首页</a></li>
            <li><a href="/admin/dashboard">数据大屏</a></li>
            <li><a href="/admin/product">商品管理</a></li>
            <li><a href="/admin/categories" class="active">分类管理</a></li>
            <li><a href="/admin/order">订单管理</a></li>
            <li><a href="/admin/user">用户管理</a></li>
            <li><a href="/admin/coupon">优惠券管理</a></li>
            <li><a href="/admin/review">评价管理</a></li>
            <li><a href="/admin/message">消息管理</a></li>
            <li><a href="/admin/announcement">公告管理</a></li>
            <li><a href="/admin/log/list">操作日志</a></li>
            <li><a href="/">返回前台</a></li>
        </ul>
    </div>
    <%-- ==================== 右侧主内容区 ==================== --%>
    <div class="main-content">
        <div class="page-header">
            <h2>分类管理</h2>
        </div>
        <div class="panel panel-default">
            <div class="panel-heading">分类列表</div>
            <div class="panel-body">
                <%-- ==================== 分类数据表格 ==================== --%>
                <table class="table table-striped table-hover">
                    <thead>
                        <tr>
                            <th>分类ID</th>
                            <th>分类名称</th>
                            <th>分类描述</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%-- 遍历分类列表，显示每个分类的信息 --%>
                        <c:forEach items="${categoryList}" var="cat">
                        <tr>
                            <td>${cat.categoryid}</td>
                            <td>${cat.categoryname}</td>
                            <td>${cat.categorydesc}</td>
                        </tr>
                        </c:forEach>
                        <%-- 如果分类列表为空，显示提示信息 --%>
                        <c:if test="${empty categoryList}">
                        <tr><td colspan="3" class="text-center text-muted">暂无分类数据</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
