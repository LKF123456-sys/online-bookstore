<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${empty sessionScope.admin}">
  <c:redirect url="${pageContext.request.contextPath}/admin/login.jsp"/>
</c:if>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>图书管理 - 管理后台</title>
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
    .book-image { width: 60px; height: 80px; object-fit: cover; }
  </style>
</head>
<body>
<div class="container-fluid">
  <div class="row">
    <div class="col-md-2 sidebar">
      <h3>📚 管理后台</h3>
      <a href="${pageContext.request.contextPath}/admin/index.jsp">🏠 首页概览</a>
      <a href="${pageContext.request.contextPath}/admin/product" class="active">📦 图书管理</a>
      <a href="${pageContext.request.contextPath}/admin/order">📋 订单管理</a>
      <a href="${pageContext.request.contextPath}/admin/user">👥 用户管理</a>
      <a href="${pageContext.request.contextPath}/admin/announcement">📢 公告管理</a>
      <hr style="border-color: #34495e;">
      <a href="${pageContext.request.contextPath}/index.jsp" target="_blank">🌐 查看商城</a>
      <a href="${pageContext.request.contextPath}/admin/logout" style="color: #e74c3c;">🚪 退出登录</a>
    </div>

    <div class="col-md-10 main-content">
      <h2>📦 图书管理</h2>
      <hr>

      <button class="btn btn-success" data-toggle="modal" data-target="#addModal">➕ 新增图书</button>
      <br><br>

      <table class="table table-bordered table-hover bg-white">
        <thead>
        <tr>
          <th>ID</th>
          <th>图片</th>
          <th>书名</th>
          <th>分类</th>
          <th>价格</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${productList}" var="book">
          <tr>
            <td>${book.productid}</td>
            <td>
              <img src="${pageContext.request.contextPath}/${book.image}"
                   alt="${book.name}" class="book-image"
                   onerror="this.style.display='none'">
            </td>
            <td>${book.name}</td>
            <td>${book.category}</td>
            <td>¥${book.price}</td>
            <td>
              <c:choose>
                <c:when test="${book.status == 1}">
                  <span class="label label-success">上架</span>
                </c:when>
                <c:otherwise>
                  <span class="label label-danger">下架</span>
                </c:otherwise>
              </c:choose>
            </td>
            <td>
              <button class="btn btn-sm btn-info"
                      onclick="editBook('${book.productid}', '${book.name}', '${book.category}', '${book.descn}', '${book.price}', '${book.status}')">
                编辑
              </button>
              <a href="${pageContext.request.contextPath}/admin/product?action=toggleStatus&productId=${book.productid}&status=${book.status == 1 ? 0 : 1}"
                 class="btn btn-sm btn-warning">
                  ${book.status == 1 ? '下架' : '上架'}
              </a>
              <a href="${pageContext.request.contextPath}/admin/product?action=delete&productId=${book.productid}"
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

<!-- 新增图书模态框 -->
<div class="modal fade" id="addModal">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4>新增图书</h4>
      </div>
      <form action="${pageContext.request.contextPath}/admin/product?action=add" method="post" enctype="multipart/form-data">
        <div class="modal-body">
          <div class="form-group">
            <label>图书ID</label>
            <input type="text" name="productid" class="form-control" required>
          </div>
          <div class="form-group">
            <label>书名</label>
            <input type="text" name="name" class="form-control" required>
          </div>
          <div class="form-group">
            <label>分类</label>
            <input type="text" name="category" class="form-control" required>
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea name="descn" class="form-control" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>价格</label>
            <input type="number" step="0.01" name="price" class="form-control" required>
          </div>
          <div class="form-group">
            <label>封面图片</label>
            <input type="file" name="image" class="form-control">
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

<script>
  function editBook(id, name, category, descn, price, status) {
    // 这里可以添加编辑功能的模态框
    alert('编辑功能开发中：' + name);
  }
</script>
</body>
</html>
