<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
	// 如果 productList 为空，加载所有图书作为默认展示
	if (request.getAttribute("productList") == null) {
		com.bookstore.dao.ProductDao dao = new com.bookstore.dao.ProductDao();
		request.setAttribute("productList", dao.findAll());
	}
%>
<html>
<head>
	<!-- 双重编码保证，彻底解决乱码 -->
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<!--BootStrap设计的页面支持响应式的 -->
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>在线图书销售平台</title>
	<!--引入BootStrap的CSS-->
	<link rel="stylesheet" href="css/bootstrap.css" type="text/css" />
	<!--引入JQuery的JS文件：JQuery的JS文件要在BootStrap的js的文件的前面引入-->
	<script type="text/javascript" src="js/jquery-3.3.1.min.js"></script>
	<!--引入BootStrap的JS的文件-->
	<script type="text/javascript" src="js/bootstrap.js"></script>
	<style type="text/css">
		#logo ul li {
			list-style: none;
			float: left;
			padding: 5px 10px;
			line-height: 60px;
		}
		#logo ul li a {
			color: #333;
			text-decoration: none;
		}
		#logo ul li a:hover {
			color: #007bff;
		}
		.book-image {
			width: 80px;
			height: 100px;
			object-fit: cover;
			border-radius: 4px;
		}
	</style>
</head>
<body>
<div class="container">
	<!--logo-->
	<div class="row">
		<div class="col-md-4">
			<h3>在线图书销售平台</h3>
		</div>
		<div class="col-md-4">
			<img src="img/header.jpg" />
		</div>
		<div class="col-md-4" id="logo">
			<ul>
				<%
					// 从session获取登录用户
					Object account = session.getAttribute("account");
					if (account != null) {
				%>
				<!-- 已登录：显示 用户名 + 购物车 -->
				<li>欢迎，<%= ((com.bookstore.entity.Account)account).getUserid() %></li>
				<li><a href="cart.jsp">购物车</a></li>
				<li><a href="logout">退出登录</a></li>
				<li><a href="${pageContext.request.contextPath}/admin/login.jsp" style="color: #e74c3c;">管理员入口</a></li>
				<% } else { %>
				<!-- 未登录：显示 登录 / 注册 -->
				<li><a href="login">登陆</a></li>
				<li><a href="register.jsp">注册</a></li>
				<li><a href="cart.jsp">购物车</a></li>
				<li><a href="${pageContext.request.contextPath}/admin/login.jsp" style="color: #e74c3c;">管理员入口</a></li>
				<% } %>
			</ul>
		</div>
	</div>
	<!--导航-->
	<div id="">
		<nav class="navbar navbar-inverse" role="navigation">
			<!-- Brand and toggle get grouped for better mobile display -->
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
						data-target=".navbar-ex1-collapse">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="index.jsp">首页</a>
			</div>
			<!-- Collect the nav links, forms, and other content for toggling -->
			<div class="collapse navbar-collapse navbar-ex1-collapse">
				<ul class="nav navbar-nav">
					<li class="active"><a href="category?categoryId=C001">科学类</a></li>
					<li><a href="category?categoryId=C002">文学类</a></li>
					<li><a href="category?categoryId=C003">动漫类</a></li>
					<li><a href="category?categoryId=C004">计算机类</a></li>
					<li class="dropdown"><a href="#" class="dropdown-toggle"
											data-toggle="dropdown">其他 <b class="caret"></b></a>
						<ul class="dropdown-menu">
							<li><a href="category?categoryId=C005">数据库</a></li>
							<li><a href="category?categoryId=C006">Java</a></li>
							<li><a href="category?categoryId=C007">概率论</a></li>
							<li class="divider"></li>
							<li><a href="category?categoryId=C008">新概念</a></li>
							<li class="divider"></li>
							<li><a href="category?categoryId=C009">大数据</a></li>
						</ul></li>
				</ul>
				<form class="navbar-form navbar-right" role="search" action="search" method="get">
					<div class="form-group">
						<input type="text" class="form-control" placeholder="Search" name="keyword">
					</div>
					<button type="submit" class="btn btn-default">Submit</button>
				</form>
			</div>
			<!-- /.navbar-collapse -->
		</nav>
	</div>
	<!--页面主题-->
	<div class="row">
		<!--类别列表-->
		<div class="col-md-4">
			<ul class="nav nav-pills nav-stacked">
				<li><a href="index.jsp">当前流行</a></li>
				<li><a href="category?categoryId=C001">科学类</a></li>
				<li><a href="category?categoryId=C002">文学类</a></li>
				<li><a href="category?categoryId=C003">动漫类</a></li>
				<li><a href="category?categoryId=C004">计算机类</a></li>
			</ul>
		</div>
		<!--轮播图-->
		<div class="col-md-8">
			<div id="">
				<div id="carousel-example-generic" class="carousel slide" data-ride="carousel">
					<!-- Indicators -->
					<ol class="carousel-indicators">
						<li data-target="#carousel-example-generic" data-slide-to="0"
							class="active"></li>
						<li data-target="#carousel-example-generic" data-slide-to="1"></li>
						<li data-target="#carousel-example-generic" data-slide-to="2"></li>
					</ol>
					<!-- Wrapper for slides -->
					<div class="carousel-inner">
						<div class="item active">
							<img src="img/12.jpg" alt="...">
							<div class="carousel-caption">第一张图片</div>
						</div>
						<div class="item ">
							<img src="img/12.jpg" alt="...">
							<div class="carousel-caption">第二张图片</div>
						</div>
						<div class="item">
							<img src="img/12.jpg" alt="...">
							<div class="carousel-caption">第三张图片</div>
						</div>
					</div>
					<!-- Controls -->
					<a class="left carousel-control" href="#carousel-example-generic"
					   data-slide="prev"> <span
							class="glyphicon glyphicon-chevron-left"></span>
					</a> <a class="right carousel-control"
							href="#carousel-example-generic" data-slide="next"> <span
						class="glyphicon glyphicon-chevron-right"></span>
				</a>
				</div>
			</div>
		</div>
	</div>
	<!--热门图书展示区-->
	<!-- 搜索结果/热门图书展示区 -->
	<div class="row" style="margin-top: 30px;">
		<div class="col-md-12">
			<h3>
				<c:choose>
					<c:when test="${not empty searchKeyword}">
						搜索「${searchKeyword}」的结果
					</c:when>
					<c:otherwise>
						热门图书
					</c:otherwise>
				</c:choose>
			</h3>

			<c:if test="${empty productList}">
				<p style="color: red;">暂无相关图书数据！</p>
			</c:if>

			<c:if test="${not empty productList}">
				<table class="table table-bordered">
					<tr>
						<th>图书ID</th>
						<th>图书图片</th>
						<th>图书名称</th>
						<th>价格</th>
						<th>操作</th>
					</tr>
					<c:forEach items="${productList}" var="book">
						<tr>
							<td>${book.productid}</td>
							<td>
								<img src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg"
									 alt="${book.name}"
									 class="book-image"
									 onerror="this.style.display='none'">
							</td>
							<td>${book.name}</td>
							<td>¥${book.price}</td>
							<td>
								<!-- 🔥 改这里：不跳转，只异步添加 -->
								<a href="javascript:addToCart('${book.productid}')" class="btn btn-primary">加入购物车</a>
							</td>
						</tr>
					</c:forEach>
				</table>
			</c:if>
		</div>
	</div>
	<!--版权部分-->
	<div>
		<div align="center" style="margin-top: 20px;">
			<img src="img/footer.jpg" width="100%">
		</div>
		<div style="background-color: #2c2c2c; color: #ccc; padding: 30px 0; margin-top: 0;">
			<div style="max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between;">
				<div style="flex: 1;">
					<h3 style="color: #fff; margin-bottom: 15px; border-bottom: 1px solid #555; padding-bottom: 10px;">关于我们</h3>
					<p style="margin: 10px 0;"><a href="about.jsp" style="color: #ccc; text-decoration: none;">公司简介</a></p>
					<p style="margin: 10px 0;"><a href="contact.jsp" style="color: #ccc; text-decoration: none;">联系我们</a></p>
					<p style="margin: 10px 0;"><a href="service.jsp" style="color: #ccc; text-decoration: none;">服务条款</a></p>
				</div>
				<div style="flex: 1;">
					<h3 style="color: #fff; margin-bottom: 15px; border-bottom: 1px solid #555; padding-bottom: 10px;">客户服务</h3>
					<p style="margin: 10px 0;"><a href="delivery.jsp" style="color: #ccc; text-decoration: none;">配送方式</a></p>
					<p style="margin: 10px 0;"><a href="payment.jsp" style="color: #ccc; text-decoration: none;">支付方式</a></p>
					<p style="margin: 10px 0;"><a href="service.jsp" style="color: #ccc; text-decoration: none;">常见问题</a></p>
				</div>
				<div style="flex: 1;">
					<h3 style="color: #fff; margin-bottom: 15px; border-bottom: 1px solid #555; padding-bottom: 10px;">快速链接</h3>
					<p style="margin: 10px 0;"><a href="index.jsp" style="color: #ccc; text-decoration: none;">首页</a></p>
					<p style="margin: 10px 0;"><a href="bookList.jsp" style="color: #ccc; text-decoration: none;">全部书籍</a></p>
					<p style="margin: 10px 0;"><a href="cart.jsp" style="color: #ccc; text-decoration: none;">购物车</a></p>
				</div>
			</div>
			<div style="text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #555; color: #999;">
				@ 2021 版权所有 Copyright
			</div>
		</div>
	</div>

</div>

<script>
	function addToCart(productId) {
		$.get("cart?method=add&productId=" + productId, function(){
			alert("加入购物车成功！");
		});
	}
</script>

</body>
</html>
