<%-- ============================================================
     product/add.jsp — 前台商品添加页面
     功能：管理员添加新商品的表单页面，包含商品名称、价格、描述、图片等字段。
     说明：表单数据提交到后端创建新商品记录。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>新增图书 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid" style="margin-top: 20px;">
    <div class="row">
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">➕ 新增图书</h3>
                </div>
                <div class="panel-body">
                    <div style="max-width: 600px;">
                        <form action="${pageContext.request.contextPath}/admin/product/add" method="post" enctype="multipart/form-data" class="form-horizontal">
                            <div class="form-group">
                                <label class="col-sm-3 control-label">图书 ID <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="text" name="productid" class="form-control" required placeholder="如：FI-CR-01"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">书名 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="text" name="name" class="form-control" required placeholder="请输入书名"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">分类 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <select name="category" class="form-control" required>
                                        <c:forEach items="${categories}" var="cat">
                                            <option value="${cat.id}">${cat.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">价格 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="number" name="price" step="0.01" min="0" class="form-control" required placeholder="请输入价格"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">描述</label>
                                <div class="col-sm-9">
                                    <textarea name="descn" class="form-control" rows="3" placeholder="请输入图书描述"></textarea>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">图片上传</label>
                                <div class="col-sm-9">
                                    <input type="file" name="imageFile" class="form-control" accept="image/*"/>
                                    <p class="help-block">支持 JPG、PNG、GIF 格式，建议尺寸 200x200 像素</p>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-9">
                                    <button type="submit" class="btn btn-gradient">提交</button>
                                    <a href="${pageContext.request.contextPath}/admin/product" class="btn btn-default">取消</a>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
