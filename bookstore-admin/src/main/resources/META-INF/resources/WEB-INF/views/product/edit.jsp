<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>编辑图书 - BookVerse</title>
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
                    <h3 class="panel-title">✏️ 编辑图书 - ${product.productid}</h3>
                </div>
                <div class="panel-body">
                    <div style="max-width: 600px;">
                        <form action="${pageContext.request.contextPath}/admin/product/edit" method="post" enctype="multipart/form-data" class="form-horizontal">
                            <input type="hidden" name="productid" value="${product.productid}"/>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">图书 ID</label>
                                <div class="col-sm-9">
                                    <p class="form-control-static">${product.productid}</p>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">书名 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="text" name="name" class="form-control" required value="${product.name}"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">分类 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <select name="category" class="form-control" required>
                                        <c:forEach items="${categories}" var="cat">
                                            <option value="${cat.id}" ${cat.id == product.category ? 'selected' : ''}>${cat.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">价格 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="number" name="price" step="0.01" min="0" class="form-control" required value="${product.price}"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">描述</label>
                                <div class="col-sm-9">
                                    <textarea name="descn" class="form-control" rows="3">${product.descn}</textarea>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">当前图片</label>
                                <div class="col-sm-9">
                                    <c:choose>
                                        <c:when test="${not empty product.image}">
                                            <img src="${pageContext.request.contextPath}/img/books/${product.image}" alt="图书图片" style="max-width: 200px; max-height: 200px; border: 1px solid #ddd; padding: 5px;"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">暂无图片</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">上传新图片</label>
                                <div class="col-sm-9">
                                    <input type="file" name="imageFile" class="form-control" accept="image/*"/>
                                    <p class="help-block">支持 JPG、PNG、GIF 格式，留空则保持当前图片</p>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">当前状态</label>
                                <div class="col-sm-9">
                                    <c:choose>
                                        <c:when test="${product.status == 1}">
                                            <span class="label label-success" style="font-size: 14px;">上架</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="label label-default" style="font-size: 14px;">下架</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-9">
                                    <button type="submit" class="btn btn-gradient">保存</button>
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
