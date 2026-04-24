<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>我的购物车</title>
    <style>
        table {border-collapse: collapse; width: 90%; margin: 20px auto;}
        th, td {border: 1px solid #ccc; padding: 10px; text-align: center;}
        th {background: #f5f5f5;}
        .total {text-align: right; padding-right: 20px;}
    </style>
</head>
<body>
<h2 style="text-align: center;">我的购物车</h2>

<c:if test="${empty cart.items}">
    <h3 style="text-align: center; color: red;">购物车是空的，快去选购图书吧！</h3>
</c:if>

<c:if test="${not empty cart.items}">
    <table>
        <tr>
            <th>图书名称</th>
            <th>单价</th>
            <th>数量</th>
            <th>小计</th>
            <th>操作</th>
        </tr>
        <c:forEach items="${cart.items}" var="item">
            <tr>
                <td>${item.product.name}</td>
                <td>￥${item.product.price}</td>
                <td>${item.quantity}</td>
                <td>￥${item.subTotal}</td>
                <td>
                    <a href="cart?method=remove&productId=${item.product.productid}">删除</a>
                </td>
            </tr>
        </c:forEach>
        <tr>
            <td colspan="5" class="total">
                总金额：<strong>￥${cart.total}</strong>
                <a href="${pageContext.request.contextPath}/order" class="btn btn-primary">去结算</a>
            </td>
        </tr>
    </table>
</c:if>
</body>
</html>