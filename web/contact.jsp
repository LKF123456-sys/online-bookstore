<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <meta charset="UTF-8">
  <title>联系我们 - 在线图书销售平台</title>
  <link rel="stylesheet" href="css/bootstrap.css" />
</head>
<body>
<div class="container">
  <h1 class="text-center" style="margin-top: 50px;">联系我们</h1>
  <div class="row" style="margin-top: 30px;">
    <div class="col-md-6 col-md-offset-3">
      <h3>客服信息</h3>
      <p><strong>客服热线：</strong>400-888-8888</p>
      <p><strong>客服邮箱：</strong>service@bookstore.com</p>
      <p><strong>工作时间：</strong>周一至周五 9:00-18:00</p>

      <h3>在线留言</h3>
      <form>
        <div class="form-group">
          <label>姓名</label>
          <input type="text" class="form-control" placeholder="请输入您的姓名">
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input type="email" class="form-control" placeholder="请输入您的邮箱">
        </div>
        <div class="form-group">
          <label>留言内容</label>
          <textarea class="form-control" rows="5" placeholder="请输入您的留言"></textarea>
        </div>
        <button type="submit" class="btn btn-primary">提交留言</button>
      </form>

      <br>
      <a href="index.jsp" class="btn btn-default">返回首页</a>
    </div>
  </div>
</div>
</body>
</html>
