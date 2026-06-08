<%-- ============================================================
     user/add.jsp — 前台用户添加页面
     功能：管理员添加新用户的表单页面。
     说明：包含用户名、密码、邮箱等字段，提交到后端创建用户。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>新增用户 - BookVerse</title>
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
                    <h3 class="panel-title">➕ 新增用户</h3>
                </div>
                <div class="panel-body">
                    <div style="max-width: 600px;">
                        <form action="${pageContext.request.contextPath}/admin/user/add" method="post" class="form-horizontal" onsubmit="return validateForm()">
                            <div class="form-group">
                                <label class="col-sm-3 control-label">用户名 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="text" name="userid" class="form-control" required placeholder="请输入用户名"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">密码 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="password" name="password" class="form-control" required placeholder="请输入密码"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">邮箱 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="email" name="email" class="form-control" required placeholder="请输入邮箱"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">姓名 <span class="text-danger">*</span></label>
                                <div class="col-sm-4">
                                    <input type="text" name="firstname" class="form-control" required placeholder="姓"/>
                                </div>
                                <div class="col-sm-5">
                                    <input type="text" name="lastname" class="form-control" required placeholder="名"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">电话</label>
                                <div class="col-sm-9">
                                    <input type="text" name="phone" class="form-control" placeholder="请输入电话"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">角色 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <select name="role" class="form-control" required>
                                        <option value="user">普通用户</option>
                                        <option value="admin">管理员</option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-9">
                                    <button type="submit" class="btn btn-gradient">提交</button>
                                    <a href="${pageContext.request.contextPath}/admin/user" class="btn btn-default">取消</a>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function validateForm() {
        var userid = document.querySelector('[name=userid]').value.trim();
        if (!userid) { alert('请输入用户名'); return false; }
        return true;
    }
</script>
</body>
</html>
