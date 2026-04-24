<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>用户注册</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<h2 align="center">用户注册</h2>
<div align="center">
    <form action="${pageContext.request.contextPath}/registerServlet" method="post">
        <table>
            <tr>
                <td>用户名:</td>
                <td><input type="text" name="userid" required></td>
            </tr>
            <tr>
                <td>密码:</td>
                <td><input type="password" name="password" required></td>
            </tr>
            <tr>
                <td>邮箱:</td>
                <td><input type="email" name="email" required></td>
            </tr>
            <tr>
                <td colspan="2" align="center">
                    <input type="submit" value="注册">
                </td>
            </tr>
        </table>
    </form>
</div>
</body>
</html>