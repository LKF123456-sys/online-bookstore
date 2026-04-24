<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>订单确认</title>
    <style>
        .container {width: 1200px; margin: 0 auto;}
        table {width: 100%; border-collapse: collapse; margin-top: 20px;}
        th,td {padding: 10px; text-align: center; border: 1px solid #ccc;}
        th {background: #f5f5f5;}
        .total {text-align: right; font-size: 18px; font-weight: bold;}
        .btn {background: #000; color: #fff; padding: 8px 15px; text-decoration: none;}
        .btn:hover {background: #333;}
    </style>
</head>
<body>
<div class="container">
    <h2>订单确认</h2>
    <table>
        <tr>
            <th>图书名称</th>
            <th>单价</th>
            <th>数量</th>
            <th>小计</th>
        </tr>
        <c:forEach items="${cart.items}" var="item">
            <tr>
                <td>${item.product.name}</td>
                <td>¥${item.product.price}</td>
                <td>${item.quantity}</td>
                <td>¥${item.product.price * item.quantity}</td>
            </tr>
        </c:forEach>
        <tr>
            <td colspan="4" class="total">总金额：¥${cart.total}</td>
        </tr>
    </table>
    <div style="text-align: right; margin-top: 20px;">
        <a href="${pageContext.request.contextPath}/index.jsp" class="btn">继续购物</a>
        <a href="${pageContext.request.contextPath}/submitOrder" class="btn">提交订单</a>
    </div>
</div>
</body>
</html>
