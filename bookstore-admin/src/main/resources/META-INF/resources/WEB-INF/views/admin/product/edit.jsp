<%-- ============================================================
     编辑图书页面 (edit.jsp)
     功能：管理员修改已有图书的信息（书名、分类、价格、描述、图片等）
     技术要点：
       - 通过 ${product} 对象回显现有图书数据
       - 使用 c:choose 判断当前分类并设置 selected 属性
       - 表单使用 multipart/form-data 支持图片上传
       - 提交到 /admin/product/edit 接口
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 引入格式化标签库，用于数字和日期格式化 --%>
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
<%-- 引入管理后台公共头部和侧边栏 --%>
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
                        <%-- ==================== 编辑图书表单 ==================== --%>
                        <form action="${pageContext.request.contextPath}/admin/product/edit" method="post" enctype="multipart/form-data" class="form-horizontal">
                            <%-- 隐藏字段：传递图书ID（不可修改） --%>
                            <input type="hidden" name="productid" value="${product.productid}"/>
                            <%-- 图书ID只读显示 --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">图书 ID</label>
                                <div class="col-sm-9">
                                    <p class="form-control-static">${product.productid}</p>
                                </div>
                            </div>
                            <%-- 书名输入框，通过 value="${product.name}" 回显现有值 --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">书名 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="text" name="name" class="form-control" required value="${product.name}"/>
                                </div>
                            </div>
                            <%-- 分类下拉框：遍历所有分类，当前分类自动选中 --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">分类 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <select name="category" class="form-control" required>
                                        <c:forEach items="${categories}" var="cat">
                                            <%-- ${cat.id == product.category ? 'selected' : ''} 判断是否为当前分类 --%>
                                            <option value="${cat.id}" ${cat.id == product.category ? 'selected' : ''}>${cat.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <%-- 价格输入框 --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">价格 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="number" name="price" step="0.01" min="0" class="form-control" required value="${product.price}"/>
                                </div>
                            </div>
                            <%-- 描述文本域 --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">描述</label>
                                <div class="col-sm-9">
                                    <textarea name="descn" class="form-control" rows="3">${product.descn}</textarea>
                                </div>
                            </div>
                            <%-- 当前图片预览 --%>
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
                            <%-- 上传新图片（留空则保持原图） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">上传新图片</label>
                                <div class="col-sm-9">
                                    <input type="file" name="imageFile" class="form-control" accept="image/*"/>
                                    <p class="help-block">支持 JPG、PNG、GIF 格式，留空则保持当前图片</p>
                                </div>
                            </div>
                            <%-- 当前状态显示（上架/下架） --%>
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
                            <%-- 保存和取消按钮 --%>
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
