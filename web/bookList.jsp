<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>在线书城</title>
    <style>
        table {border-collapse: collapse; width: 90%; margin: 20px auto;}
        th, td {border: 1px solid #ccc; padding: 10px; text-align: center;}
        th {background: #f5f5f5;}
        .book-image {width: 100px; height: 130px; object-fit: cover; border-radius: 4px;}
    </style>
</head>
<body>
<h2>在线书城</h2>
<h3>全部图书</h3>

<c:if test="${empty bookList}">
    <p style="color:red;">暂无图书数据</p>
</c:if>

<c:if test="${not empty bookList}">
    <table>
        <tr>
            <th>图书ID</th>
            <th>图书图片</th>
            <th>图书名称</th>
            <th>分类</th>
            <th>描述</th>
            <th>价格</th>
            <th>操作</th>
        </tr>
        <c:forEach items="${bookList}" var="book">
            <tr>
                <td>${book.productid}</td>
                <td>
                    <img src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg"
                         alt="${book.name}"
                         class="book-image"
                         onerror="this.style.display='none'">
                </td>
                <td>${book.name}</td>
                <td>${book.category}</td>
                <td>${book.descn}</td>
                <td>¥${book.price}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/cart?method=add&productId=${book.productid}">加入购物车</a>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>
</body>
</html>
