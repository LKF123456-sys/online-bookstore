<%-- ============================================================
     user/edit.jsp — 前台用户编辑页面
     功能：管理员编辑用户信息的表单页面。
     说明：包含用户基本信息的修改表单，数据提交到后端更新。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>编辑用户 - BookVerse</title>
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
                    <h3 class="panel-title">️ 编辑用户 - ${user.userid}</h3>
                </div>
                <div class="panel-body">
                    <div style="max-width: 600px;">
                        <form action="${pageContext.request.contextPath}/admin/user/edit" method="post" class="form-horizontal">
                            <input type="hidden" name="userid" value="${user.userid}"/>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">用户名</label>
                                <div class="col-sm-9">
                                    <p class="form-control-static">${user.userid}</p>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">邮箱 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="email" name="email" class="form-control" required value="${user.email}"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">姓名 <span class="text-danger">*</span></label>
                                <div class="col-sm-4">
                                    <input type="text" name="firstname" class="form-control" required value="${user.firstname}" placeholder="姓"/>
                                </div>
                                <div class="col-sm-5">
                                    <input type="text" name="lastname" class="form-control" required value="${user.lastname}" placeholder="名"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">电话</label>
                                <div class="col-sm-9">
                                    <input type="text" name="phone" class="form-control" value="${user.phone}" placeholder="请输入电话"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">角色 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <select name="role" class="form-control" required>
                                        <option value="user" ${user.role == 'user' ? 'selected' : ''}>普通用户</option>
                                        <option value="admin" ${user.role == 'admin' ? 'selected' : ''}>管理员</option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">修改密码</label>
                                <div class="col-sm-9">
                                    <input type="password" name="newPassword" class="form-control" placeholder="留空则不修改密码"/>
                                    <span class="help-block" style="color: #999; font-size: 12px;">如需修改密码请输入新密码，否则留空</span>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">当前状态</label>
                                <div class="col-sm-9">
                                    <c:choose>
                                        <c:when test="${user.status == 1}">
                                            <span class="label label-success" style="font-size: 14px;">正常</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="label label-default" style="font-size: 14px;">禁用</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-9">
                                    <button type="submit" class="btn btn-gradient">保存</button>
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
</body>
</html>
