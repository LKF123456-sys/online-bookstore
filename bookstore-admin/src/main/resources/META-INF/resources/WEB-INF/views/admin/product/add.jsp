<%-- ============================================================
     新增图书页面 (add.jsp)
     功能：管理员填写表单添加新图书到数据库
     技术要点：
       - 表单使用 multipart/form-data 支持图片上传
       - 分类列表从后端 ${categories} 动态获取
       - 提交到 /admin/product/add 接口
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 引入 JSTL 核心标签库，用于遍历分类列表（c:forEach） --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>新增图书 - BookVerse</title>
    <%-- 引入 Bootstrap 样式和项目自定义样式 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <%-- 引入 jQuery 库和 Bootstrap JS 组件 --%>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
</head>
<body>
<%-- 引入管理后台公共头部导航栏 --%>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid" style="margin-top: 20px;">
    <div class="row">
        <%-- 引入管理后台侧边栏 --%>
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <%-- 主内容区域（占10列宽度） --%>
        <div class="col-md-10">
            <div class="panel panel-default">
                <%-- 面板标题 --%>
                <div class="panel-heading">
                    <h3 class="panel-title">➕ 新增图书</h3>
                </div>
                <div class="panel-body">
                    <div style="max-width: 600px;">
                        <%-- ==================== 新增图书表单 ==================== --%>
                        <%-- enctype="multipart/form-data" 表示表单支持文件上传 --%>
                        <form action="${pageContext.request.contextPath}/admin/product/add" method="post" enctype="multipart/form-data" class="form-horizontal">
                            <%-- 图书ID输入框（必填），如：FI-CR-01 --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">图书 ID <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="text" name="productid" class="form-control" required placeholder="如：FI-CR-01"/>
                                </div>
                            </div>
                            <%-- 书名输入框（必填） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">书名 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="text" name="name" class="form-control" required placeholder="请输入书名"/>
                                </div>
                            </div>
                            <%-- 分类下拉框（必填），选项从后端 categories 列表动态生成 --%>
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
                            <%-- 价格输入框（必填），step=0.01 允许输入小数 --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">价格 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="number" name="price" step="0.01" min="0" class="form-control" required placeholder="请输入价格"/>
                                </div>
                            </div>
                            <%-- 图书描述文本域（选填） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">描述</label>
                                <div class="col-sm-9">
                                    <textarea name="descn" class="form-control" rows="3" placeholder="请输入图书描述"></textarea>
                                </div>
                            </div>
                            <%-- 图片上传控件（选填） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">图片上传</label>
                                <div class="col-sm-9">
                                    <input type="file" name="imageFile" class="form-control" accept="image/*"/>
                                    <p class="help-block">支持 JPG、PNG、GIF 格式，建议尺寸 200x200 像素</p>
                                </div>
                            </div>
                            <%-- 提交和取消按钮 --%>
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
