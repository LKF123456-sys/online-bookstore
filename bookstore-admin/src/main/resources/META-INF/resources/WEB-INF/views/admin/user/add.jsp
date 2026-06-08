<%-- ============================================================
     新增用户页面 (add.jsp)
     功能：管理员创建新的用户账号
     技术要点：
       - 表单提交前通过 onsubmit 调用 validateForm() 进行前端验证
       - 角色选择：普通用户(user) 或 管理员(admin)
       - 提交到 /admin/user/add 接口
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
<%-- 引入管理后台公共头部和侧边栏 --%>
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
                        <%-- ==================== 新增用户表单 ==================== --%>
                        <%-- onsubmit="return validateForm()" 提交前执行前端验证 --%>
                        <form action="${pageContext.request.contextPath}/admin/user/add" method="post" class="form-horizontal" onsubmit="return validateForm()">
                            <%-- 用户名输入框（必填） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">用户名 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="text" name="userid" class="form-control" required placeholder="请输入用户名"/>
                                </div>
                            </div>
                            <%-- 密码输入框（必填） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">密码 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="password" name="password" class="form-control" required placeholder="请输入密码"/>
                                </div>
                            </div>
                            <%-- 邮箱输入框（必填） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">邮箱 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <input type="email" name="email" class="form-control" required placeholder="请输入邮箱"/>
                                </div>
                            </div>
                            <%-- 姓名输入框（姓和名分开） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">姓名 <span class="text-danger">*</span></label>
                                <div class="col-sm-4">
                                    <input type="text" name="firstname" class="form-control" required placeholder="姓"/>
                                </div>
                                <div class="col-sm-5">
                                    <input type="text" name="lastname" class="form-control" required placeholder="名"/>
                                </div>
                            </div>
                            <%-- 电话输入框（选填） --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">电话</label>
                                <div class="col-sm-9">
                                    <input type="text" name="phone" class="form-control" placeholder="请输入电话"/>
                                </div>
                            </div>
                            <%-- 角色选择下拉框 --%>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">角色 <span class="text-danger">*</span></label>
                                <div class="col-sm-9">
                                    <select name="role" class="form-control" required>
                                        <option value="user">普通用户</option>
                                        <option value="admin">管理员</option>
                                    </select>
                                </div>
                            </div>
                            <%-- 提交和取消按钮 --%>
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
<%-- ==================== 前端表单验证脚本 ==================== --%>
<script type="text/javascript">
    // 表单提交前的验证函数
    function validateForm() {
        // 获取用户名输入值并去除首尾空格
        var userid = document.querySelector('[name=userid]').value.trim();
        // 如果用户名为空，弹出提示并阻止表单提交
        if (!userid) { alert('请输入用户名'); return false; }
        // 验证通过，允许提交
        return true;
    }
</script>
</body>
</html>
