<%-- 个人中心页面 user/profile.jsp --%>
<%-- 功能：用户个人信息管理页面，包括查看/修改个人资料、头像、密码、收货地址等 --%>
<%-- 用户可在此页面管理自己的账户信息 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>个人中心 - 在线书店</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/css/bootstrap.min.css">
    <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
    <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/js/bootstrap.min.js"></script>
    <style>
        :root {
            --primary: #4f46e5;
            --primary-dark: #3730a3;
            --primary-light: #818cf8;
            --accent: #f59e0b;
            --success: #10b981;
            --danger: #ef4444;
            --gray-50: #f9fafb;
            --gray-100: #f3f4f6;
            --gray-200: #e5e7eb;
            --gray-300: #d1d5db;
            --gray-400: #9ca3af;
            --gray-500: #6b7280;
            --gray-600: #4b5563;
            --gray-700: #374151;
            --gray-800: #1f2937;
            --white: #ffffff;
            --shadow: 0 1px 6px rgba(0,0,0,0.08);
            --shadow-md: 0 4px 6px rgba(0,0,0,0.1);
            --shadow-lg: 0 10px 25px rgba(0,0,0,0.12);
            --radius: 8px;
            --radius-lg: 12px;
            --radius-xl: 16px;
        }
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: var(--gray-50); color: var(--gray-800); }

        .navbar-custom { background: linear-gradient(135deg, var(--primary), var(--primary-dark)); border: none; border-radius: 0; box-shadow: 0 2px 12px rgba(79,70,229,0.3); }
        .navbar-custom .navbar-brand { color: #fff !important; font-weight: 800; font-size: 20px; }
        .navbar-custom .nav > li > a { color: rgba(255,255,255,0.9) !important; font-weight: 600; transition: all 0.3s; }
        .navbar-custom .nav > li > a:hover { color: #fff !important; background: rgba(255,255,255,0.1) !important; border-radius: 6px; }

        .page-header { background: var(--white); padding: 20px 0; box-shadow: var(--shadow); margin-bottom: 24px; }
        .page-header .container { max-width: 1100px; }
        .page-header h2 { font-size: 22px; font-weight: 700; margin: 0; color: var(--gray-800); }
        .page-header p { color: var(--gray-500); font-size: 14px; margin: 4px 0 0; }

        .container { max-width: 1100px; }

        .profile-layout { display: flex; gap: 24px; align-items: flex-start; }
        .profile-sidebar { width: 300px; flex-shrink: 0; }
        .profile-main { flex: 1; min-width: 0; }

        .card { background: var(--white); border-radius: var(--radius-lg); box-shadow: var(--shadow); overflow: hidden; }
        .card + .card { margin-top: 20px; }
        .card-header { padding: 16px 24px; border-bottom: 1px solid var(--gray-100); }
        .card-header h3 { font-size: 16px; font-weight: 700; margin: 0; color: var(--gray-800); }
        .card-body { padding: 24px; }

        .user-info-card .avatar-area { text-align: center; padding: 40px 24px 24px; background: linear-gradient(135deg, var(--primary) 0%, #7c3aed 50%, #ec4899 100%); color: #fff; position: relative; overflow: hidden; }
        .user-info-card .avatar-area::before { content: ''; position: absolute; top: -50%; right: -30%; width: 300px; height: 300px; background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%); border-radius: 50%; }
        .user-info-card .avatar-area::after { content: ''; position: absolute; bottom: -30%; left: -20%; width: 200px; height: 200px; background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%); border-radius: 50%; }
        .avatar-wrapper { position: relative; display: inline-block; margin-bottom: 16px; z-index: 1; }
        .user-avatar-lg { width: 100px; height: 100px; border-radius: 50%; background: rgba(255,255,255,0.15); display: inline-flex; align-items: center; justify-content: center; font-size: 42px; border: 4px solid rgba(255,255,255,0.3); overflow: hidden; cursor: pointer; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); position: relative; backdrop-filter: blur(10px); }
        .user-avatar-lg:hover { border-color: rgba(255,255,255,0.8); transform: scale(1.08); box-shadow: 0 8px 32px rgba(0,0,0,0.2); }
        .user-avatar-lg img { width: 100%; height: 100%; object-fit: cover; }
        .user-avatar-lg .avatar-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(135deg, rgba(99,102,241,0.8), rgba(236,72,153,0.8)); display: flex; flex-direction: column; align-items: center; justify-content: center; opacity: 0; transition: all 0.3s; border-radius: 50%; gap: 4px; }
        .user-avatar-lg:hover .avatar-overlay { opacity: 1; }
        .user-avatar-lg .avatar-overlay i { font-size: 22px; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2)); }
        .user-avatar-lg .avatar-overlay span { font-size: 11px; font-weight: 600; letter-spacing: 0.5px; }
        .avatar-badge { position: absolute; bottom: 4px; right: 4px; width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, #10b981, #059669); border: 3px solid #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; box-shadow: 0 4px 12px rgba(0,0,0,0.2); cursor: pointer; transition: all 0.3s; z-index: 2; }
        .avatar-badge:hover { transform: scale(1.15); background: linear-gradient(135deg, #059669, #047857); }
        .avatar-upload-input { position: absolute; width: 0; height: 0; opacity: 0; overflow: hidden; z-index: -1; }
        .avatar-upload-tip { font-size: 12px; opacity: 0.85; margin-top: 8px; position: relative; z-index: 1; letter-spacing: 0.5px; }
        .user-info-card .user-name { font-size: 18px; font-weight: 700; }
        .user-info-card .user-id { font-size: 13px; opacity: 0.8; margin-top: 4px; }
        .user-info-card .info-list { padding: 16px 24px; }
        .user-info-card .info-item { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--gray-100); font-size: 14px; }
        .user-info-card .info-item:last-child { border-bottom: none; }
        .user-info-card .info-item .info-icon { width: 32px; font-size: 16px; text-align: center; margin-right: 12px; flex-shrink: 0; }
        .user-info-card .info-item .info-label { color: var(--gray-500); width: 70px; flex-shrink: 0; }
        .user-info-card .info-item .info-value { color: var(--gray-800); font-weight: 500; word-break: break-all; }

        .status-badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 12px; font-weight: 600; }
        .status-active { background: #d1fae5; color: #065f46; }
        .status-disabled { background: #fee2e2; color: #991b1b; }
        .role-badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 12px; font-weight: 600; background: #ede9fe; color: #5b21b6; }

        .nav-tabs-custom { border-bottom: 2px solid var(--gray-100); margin-bottom: 0; }
        .nav-tabs-custom > li > a { color: var(--gray-500); font-weight: 600; font-size: 14px; padding: 12px 20px; border: none; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.3s; }
        .nav-tabs-custom > li > a:hover { color: var(--primary); background: transparent; border-bottom-color: var(--gray-300); }
        .nav-tabs-custom > li.active > a, .nav-tabs-custom > li.active > a:hover, .nav-tabs-custom > li.active > a:focus { color: var(--primary); border: none; border-bottom: 2px solid var(--primary); background: transparent; }
        .tab-content { padding: 0; }

        .form-group label { font-weight: 600; color: var(--gray-700); font-size: 13px; margin-bottom: 6px; }
        .form-control { border-radius: var(--radius); border: 1px solid var(--gray-300); padding: 10px 14px; font-size: 14px; transition: all 0.3s; height: auto; }
        .form-control:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(79,70,229,0.15); }
        .form-control[readonly] { background: var(--gray-100); color: var(--gray-500); }

        .section-title { font-size: 14px; font-weight: 700; color: var(--gray-600); margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px dashed var(--gray-200); }
        .section-title .section-icon { margin-right: 6px; }

        .btn-save { background: linear-gradient(135deg, var(--primary), var(--primary-dark)); color: #fff; border: none; padding: 12px 40px; border-radius: var(--radius); font-size: 15px; font-weight: 700; cursor: pointer; transition: all 0.3s; box-shadow: 0 4px 12px rgba(79,70,229,0.3); }
        .btn-save:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(79,70,229,0.4); color: #fff; }
        .btn-save:active { transform: translateY(0); }

        .btn-change-pwd { background: var(--accent); color: #fff; border: none; padding: 10px 28px; border-radius: var(--radius); font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.3s; }
        .btn-change-pwd:hover { background: #d97706; color: #fff; }

        .password-strength { height: 4px; border-radius: 2px; margin-top: 6px; transition: all 0.3s; background: var(--gray-200); }
        .password-strength .bar { height: 100%; border-radius: 2px; transition: all 0.3s; width: 0; }

        .toast-msg { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); background: var(--gray-800); color: #fff; padding: 12px 24px; border-radius: 30px; z-index: 9999; font-size: 14px; box-shadow: 0 4px 15px rgba(0,0,0,0.3); opacity: 0; transition: opacity 0.3s; }
        .toast-msg.show { opacity: 1; }

        .security-item { display: flex; align-items: center; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid var(--gray-100); }
        .security-item:last-child { border-bottom: none; }
        .security-item .security-left { display: flex; align-items: center; gap: 12px; }
        .security-item .security-icon { width: 40px; height: 40px; border-radius: var(--radius); display: flex; align-items: center; justify-content: center; font-size: 20px; }
        .security-item .security-title { font-weight: 600; font-size: 14px; color: var(--gray-800); }
        .security-item .security-desc { font-size: 12px; color: var(--gray-500); margin-top: 2px; }
        .security-item .security-status { font-size: 13px; font-weight: 600; }
        .security-status.verified { color: var(--success); }
        .security-status.unverified { color: var(--gray-400); }

        .footer-custom { background: var(--gray-800); color: var(--gray-300); padding: 30px 0 20px; margin-top: 60px; }
        .footer-bottom { border-top: 1px solid var(--gray-700); margin-top: 20px; padding-top: 15px; text-align: center; color: var(--gray-500); font-size: 13px; }

        .msg-toast { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); background: var(--gray-800); color: #fff; padding: 12px 28px; border-radius: 30px; z-index: 9999; font-size: 14px; box-shadow: 0 4px 15px rgba(0,0,0,0.3); animation: fadeInOut 2.5s ease forwards; }
        @keyframes fadeInOut { 0%{opacity:0;transform:translateX(-50%) translateY(10px);} 15%{opacity:1;transform:translateX(-50%) translateY(0);} 80%{opacity:1;} 100%{opacity:0;transform:translateX(-50%) translateY(-10px);} }

        .btn-back-home { background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important; position: relative; overflow: hidden; }
        .btn-back-home::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent); transition: left 0.5s; }
        .btn-back-home:hover { transform: translateY(-2px) !important; box-shadow: 0 8px 25px rgba(79,70,229,0.4) !important; color: #fff !important; text-decoration: none !important; }
        .btn-back-home:hover::before { left: 100%; }
        .btn-back-home:active { transform: translateY(0) !important; }

        @media (max-width: 768px) {
            .profile-layout { flex-direction: column; }
            .profile-sidebar { width: 100%; }
        }
    </style>
</head>
<body>

<nav class="navbar navbar-default navbar-custom navbar-fixed-top">
    <div class="container" style="max-width:1100px;">
        <div class="navbar-header">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">📚 BookVerse</a>
        </div>
        <ul class="nav navbar-nav navbar-right">
            <li><a href="${pageContext.request.contextPath}/">返回首页</a></li>
            <li><a href="${pageContext.request.contextPath}/orders">我的订单</a></li>
            <li><a href="${pageContext.request.contextPath}/message">消息中心</a></li>
            <c:if test="${sessionScope.user.role == 'admin'}">
                <li><a href="${pageContext.request.contextPath}/admin/login" style="color:#ef4444;">🔑 管理后台</a></li>
            </c:if>
        </ul>
    </div>
</nav>

<div style="height:70px;"></div>

<div class="page-header">
    <div class="container">
        <div style="display:flex;align-items:center;justify-content:space-between;">
            <div>
                <h2>👤 个人中心</h2>
                <p>管理您的个人信息和账户安全设置</p>
            </div>
            <a href="${pageContext.request.contextPath}/" class="btn-back-home" style="display:inline-flex;align-items:center;gap:8px;padding:12px 28px;background:linear-gradient(135deg,var(--primary),var(--primary-dark));color:#fff;border:none;border-radius:50px;font-size:14px;font-weight:600;cursor:pointer;text-decoration:none;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);box-shadow:0 4px 15px rgba(79,70,229,0.3);position:relative;overflow:hidden;">
                <span style="position:relative;z-index:1;display:flex;align-items:center;gap:8px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/></svg>
                    返回首页
                </span>
            </a>
        </div>
    </div>
</div>

<div class="container">
    <div class="profile-layout">

        <div class="profile-sidebar">
            <div class="card user-info-card">
                <div class="avatar-area">
                    <div class="avatar-wrapper">
                        <div class="user-avatar-lg" onclick="document.getElementById('avatarInput').click()">
                            <img src="${pageContext.request.contextPath}${not empty sessionScope.user.avatar ? sessionScope.user.avatar : '/img/default-book.svg'}" alt="头像">
                            <div class="avatar-overlay">
                                <i class="fas fa-camera"></i>
                                <span>更换头像</span>
                            </div>
                        </div>
                        <div class="avatar-badge" onclick="document.getElementById('avatarInput').click()" title="上传头像">
                            <i class="fas fa-pen" style="font-size:12px;color:#fff;"></i>
                        </div>
                    </div>
                    <input type="file" id="avatarInput" class="avatar-upload-input" accept="image/*" onchange="uploadAvatar(this)">
                    <div class="user-name">${sessionScope.user.firstname} ${sessionScope.user.lastname}</div>
                    <div class="user-id">@${sessionScope.user.userid}</div>
                </div>
                <div class="info-list">
                    <div class="info-item">
                        <span class="info-icon">📧</span>
                        <span class="info-label">邮箱</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${not empty sessionScope.user.email}">${sessionScope.user.email}</c:when>
                                <c:otherwise><span style="color:var(--gray-400);">未设置</span></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="info-item">
                        <span class="info-icon">📱</span>
                        <span class="info-label">电话</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${not empty sessionScope.user.phone}">${sessionScope.user.phone}</c:when>
                                <c:otherwise><span style="color:var(--gray-400);">未设置</span></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="info-item">
                        <span class="info-icon">🏷️</span>
                        <span class="info-label">角色</span>
                        <span class="info-value">
                            <span class="role-badge">${sessionScope.user.role}</span>
                        </span>
                    </div>
                    <div class="info-item">
                        <span class="info-icon">🔒</span>
                        <span class="info-label">状态</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${sessionScope.user.status == 1}">
                                    <span class="status-badge status-active">正常</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge status-disabled">已禁用</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <div class="profile-main">
            <div class="card">
                <div style="padding: 0 24px;">
                    <ul class="nav nav-tabs nav-tabs-custom" id="profileTabs">
                        <li class="active"><a href="#tab-info" data-toggle="tab">📝 个人信息</a></li>
                        <li><a href="#tab-password" data-toggle="tab">🔑 修改密码</a></li>
                        <li><a href="#tab-security" data-toggle="tab">🛡️ 账户安全</a></li>
                    </ul>
                </div>
                <div class="tab-content" style="padding: 0;">

                    <div class="tab-pane active" id="tab-info">
                        <div class="card-body">
                            <c:if test="${not empty profileSuccess}">
                                <div class="alert alert-success" style="border-radius:var(--radius);font-size:14px;">
                                    ✅ ${profileSuccess}
                                </div>
                            </c:if>
                            <c:if test="${not empty profileError}">
                                <div class="alert alert-danger" style="border-radius:var(--radius);font-size:14px;">
                                    ❌ ${profileError}
                                </div>
                            </c:if>
                            <form id="profileForm" action="${pageContext.request.contextPath}/user/profile" method="post">
                                <div class="section-title"><span class="section-icon">📋</span>基本信息</div>
                                <div class="row">
                                    <div class="col-sm-6">
                                        <div class="form-group">
                                            <label for="userid">用户名</label>
                                            <input type="text" class="form-control" id="userid" value="${sessionScope.user.userid}" readonly>
                                            <p class="help-block" style="font-size:12px;">用户名不可修改</p>
                                        </div>
                                    </div>
                                    <div class="col-sm-6">
                                        <div class="form-group">
                                            <label for="email">邮箱地址</label>
                                            <input type="email" class="form-control" id="email" name="email" value="${sessionScope.user.email}" placeholder="请输入邮箱地址">
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-sm-6">
                                        <div class="form-group">
                                            <label for="firstname">姓</label>
                                            <input type="text" class="form-control" id="firstname" name="firstname" value="${sessionScope.user.firstname}" placeholder="请输入姓">
                                        </div>
                                    </div>
                                    <div class="col-sm-6">
                                        <div class="form-group">
                                            <label for="lastname">名</label>
                                            <input type="text" class="form-control" id="lastname" name="lastname" value="${sessionScope.user.lastname}" placeholder="请输入名">
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-sm-6">
                                        <div class="form-group">
                                            <label for="phone">手机号码</label>
                                            <input type="text" class="form-control" id="phone" name="phone" value="${sessionScope.user.phone}" placeholder="请输入手机号码">
                                        </div>
                                    </div>
                                </div>

                                <div class="section-title" style="margin-top:28px;"><span class="section-icon">🏠</span>收货地址</div>
                                <div class="row">
                                    <div class="col-sm-12">
                                        <div class="form-group">
                                            <label for="addr1">详细地址</label>
                                            <input type="text" class="form-control" id="addr1" name="addr1" value="${sessionScope.user.addr1}" placeholder="请输入详细地址">
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-sm-4">
                                        <div class="form-group">
                                            <label for="provinceSelect">省份</label>
                                            <select class="form-control" id="provinceSelect" name="state">
                                                <option value="">请选择省份</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="col-sm-4">
                                        <div class="form-group">
                                            <label for="citySelect">城市</label>
                                            <select class="form-control" id="citySelect" name="city">
                                                <option value="">请先选择省份</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="col-sm-4">
                                        <div class="form-group">
                                            <label for="country">国家</label>
                                            <input type="text" class="form-control" id="country" name="country" value="${sessionScope.user.country}" placeholder="国家">
                                        </div>
                                    </div>
                                </div>

                                <div style="text-align:right;margin-top:20px;">
                                    <button type="submit" class="btn btn-save">💾 保存个人信息</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="tab-pane" id="tab-password">
                        <div class="card-body">
                            <c:if test="${not empty passwordSuccess}">
                                <div class="alert alert-success" style="border-radius:var(--radius);font-size:14px;">
                                    ✅ ${passwordSuccess}
                                </div>
                            </c:if>
                            <c:if test="${not empty passwordError}">
                                <div class="alert alert-danger" style="border-radius:var(--radius);font-size:14px;">
                                    ❌ ${passwordError}
                                </div>
                            </c:if>
                            <form id="passwordForm" action="${pageContext.request.contextPath}/user/password" method="post">
                                <div class="section-title"><span class="section-icon">🔐</span>修改密码</div>
                                <div class="row">
                                    <div class="col-sm-8">
                                        <div class="form-group">
                                            <label for="currentPassword">当前密码</label>
                                            <input type="password" class="form-control" id="currentPassword" name="currentPassword" placeholder="请输入当前密码" required>
                                        </div>
                                        <div class="form-group">
                                            <label for="newPassword">新密码</label>
                                            <input type="password" class="form-control" id="newPassword" name="newPassword" placeholder="请输入新密码（至少6位）" required>
                                            <div class="password-strength" id="pwdStrength"><div class="bar"></div></div>
                                            <p class="help-block" id="pwdHint" style="font-size:12px;margin-top:4px;"></p>
                                        </div>
                                        <div class="form-group">
                                            <label for="confirmPassword">确认新密码</label>
                                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" placeholder="请再次输入新密码" required>
                                        </div>
                                    </div>
                                </div>
                                <div style="text-align:right;margin-top:20px;">
                                    <button type="submit" class="btn btn-change-pwd" id="btnChangePwd">🔑 修改密码</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="tab-pane" id="tab-security">
                        <div class="card-body">
                            <div class="section-title"><span class="section-icon">🛡️</span>安全状态</div>

                            <div class="security-item">
                                <div class="security-left">
                                    <div class="security-icon" style="background:#d1fae5;">📧</div>
                                    <div>
                                        <div class="security-title">邮箱验证</div>
                                        <div class="security-desc">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.user.email}">
                                                    已绑定：${sessionScope.user.email}
                                                </c:when>
                                                <c:otherwise>未绑定邮箱，建议绑定以保障账户安全</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                                <div>
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.user.email}">
                                            <span class="security-status verified">✓ 已验证</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="security-status unverified">未绑定</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <div class="security-item">
                                <div class="security-left">
                                    <div class="security-icon" style="background:#ede9fe;">📱</div>
                                    <div>
                                        <div class="security-title">手机绑定</div>
                                        <div class="security-desc">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.user.phone}">
                                                    已绑定：${sessionScope.user.phone}
                                                </c:when>
                                                <c:otherwise>未绑定手机号码</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                                <div>
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.user.phone}">
                                            <span class="security-status verified">✓ 已绑定</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="security-status unverified">未绑定</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <div class="security-item">
                                <div class="security-left">
                                    <div class="security-icon" style="background:#fef3c7;">🔑</div>
                                    <div>
                                        <div class="security-title">登录密码</div>
                                        <div class="security-desc">定期修改密码可以保护账户安全</div>
                                    </div>
                                </div>
                                <div>
                                    <a href="#tab-password" onclick="switchToPasswordTab()" style="color:var(--primary);font-weight:600;font-size:13px;text-decoration:none;">修改密码 →</a>
                                </div>
                            </div>

                            <div class="security-item">
                                <div class="security-left">
                                    <div class="security-icon" style="background:#fee2e2;">⚠️</div>
                                    <div>
                                        <div class="security-title">账户状态</div>
                                        <div class="security-desc">您的账户当前状态</div>
                                    </div>
                                </div>
                                <div>
                                    <c:choose>
                                        <c:when test="${sessionScope.user.status == 1}">
                                            <span class="status-badge status-active">正常</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-badge status-disabled">已禁用</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                        </div>
                    </div>

                </div>
            </div>
        </div>

    </div>
</div>

<footer class="footer-custom">
    <div class="container" style="max-width:1100px;">
        <div class="footer-bottom">
            © 2026 在线书店 Online Bookstore. All rights reserved.
        </div>
    </div>
</footer>

<script>
    var provinceCityData = {
        "北京市": ["东城区","西城区","朝阳区","丰台区","石景山区","海淀区","门头沟区","房山区","通州区","顺义区","昌平区","大兴区"],
        "上海市": ["黄浦区","徐汇区","长宁区","静安区","普陀区","虹口区","杨浦区","闵行区","宝山区","嘉定区","浦东新区","松江区","青浦区"],
        "天津市": ["和平区","河东区","河西区","南开区","河北区","红桥区","东丽区","西青区","津南区","北辰区","武清区","宝坻区","滨海新区"],
        "重庆市": ["万州区","涪陵区","渝中区","大渡口区","江北区","沙坪坝区","九龙坡区","南岸区","北碚区","渝北区","巴南区","长寿区","江津区","合川区","永川区"],
        "河北省": ["石家庄市","唐山市","秦皇岛市","邯郸市","邢台市","保定市","张家口市","承德市","沧州市","廊坊市","衡水市"],
        "山西省": ["太原市","大同市","阳泉市","长治市","晋城市","朔州市","晋中市","运城市","忻州市","临汾市","吕梁市"],
        "内蒙古自治区": ["呼和浩特市","包头市","乌海市","赤峰市","通辽市","鄂尔多斯市","呼伦贝尔市","巴彦淖尔市","乌兰察布市"],
        "辽宁省": ["沈阳市","大连市","鞍山市","抚顺市","本溪市","丹东市","锦州市","营口市","阜新市","辽阳市","盘锦市","铁岭市","朝阳市","葫芦岛市"],
        "吉林省": ["长春市","吉林市","四平市","辽源市","通化市","白山市","松原市","白城市"],
        "黑龙江省": ["哈尔滨市","齐齐哈尔市","鸡西市","鹤岗市","双鸭山市","大庆市","伊春市","佳木斯市","七台河市","牡丹江市","黑河市","绥化市"],
        "江苏省": ["南京市","无锡市","徐州市","常州市","苏州市","南通市","连云港市","淮安市","盐城市","扬州市","镇江市","泰州市","宿迁市"],
        "浙江省": ["杭州市","宁波市","温州市","嘉兴市","湖州市","绍兴市","金华市","衢州市","舟山市","台州市","丽水市"],
        "安徽省": ["合肥市","芜湖市","蚌埠市","淮南市","马鞍山市","淮北市","铜陵市","安庆市","黄山市","滁州市","阜阳市","宿州市","六安市","亳州市","池州市","宣城市"],
        "福建省": ["福州市","厦门市","莆田市","三明市","泉州市","漳州市","南平市","龙岩市","宁德市"],
        "江西省": ["南昌市","景德镇市","萍乡市","九江市","新余市","鹰潭市","赣州市","吉安市","宜春市","抚州市","上饶市"],
        "山东省": ["济南市","青岛市","淄博市","枣庄市","东营市","烟台市","潍坊市","济宁市","泰安市","威海市","日照市","临沂市","德州市","聊城市","滨州市","菏泽市"],
        "河南省": ["郑州市","开封市","洛阳市","平顶山市","安阳市","鹤壁市","新乡市","焦作市","濮阳市","许昌市","漯河市","三门峡市","南阳市","商丘市","信阳市","周口市","驻马店市"],
        "湖北省": ["武汉市","黄石市","十堰市","宜昌市","襄阳市","鄂州市","荆门市","孝感市","荆州市","黄冈市","咸宁市","随州市","恩施土家族苗族自治州"],
        "湖南省": ["长沙市","株洲市","湘潭市","衡阳市","邵阳市","岳阳市","常德市","张家界市","益阳市","郴州市","永州市","怀化市","娄底市","湘西土家族苗族自治州"],
        "广东省": ["广州市","韶关市","深圳市","珠海市","汕头市","佛山市","江门市","湛江市","茂名市","肇庆市","惠州市","梅州市","汕尾市","河源市","阳江市","清远市","东莞市","中山市","潮州市","揭阳市","云浮市"],
        "广西壮族自治区": ["南宁市","柳州市","桂林市","梧州市","北海市","防城港市","钦州市","贵港市","玉林市","百色市","贺州市","河池市","来宾市","崇左市"],
        "海南省": ["海口市","三亚市","三沙市","儋州市"],
        "四川省": ["成都市","自贡市","攀枝花市","泸州市","德阳市","绵阳市","广元市","遂宁市","内江市","乐山市","南充市","眉山市","宜宾市","广安市","达州市","雅安市","巴中市","资阳市","阿坝藏族羌族自治州","甘孜藏族自治州","凉山彝族自治州"],
        "贵州省": ["贵阳市","六盘水市","遵义市","安顺市","毕节市","铜仁市","黔西南布依族苗族自治州","黔东南苗族侗族自治州","黔南布依族苗族自治州"],
        "云南省": ["昆明市","曲靖市","玉溪市","保山市","昭通市","丽江市","普洱市","临沧市","楚雄彝族自治州","红河哈尼族彝族自治州","文山壮族苗族自治州","西双版纳傣族自治州","大理白族自治州","德宏傣族景颇族自治州","怒江傈僳族自治州","迪庆藏族自治州"],
        "西藏自治区": ["拉萨市","日喀则市","昌都市","林芝市","山南市","那曲市"],
        "陕西省": ["西安市","铜川市","宝鸡市","咸阳市","渭南市","延安市","汉中市","榆林市","安康市","商洛市"],
        "甘肃省": ["兰州市","嘉峪关市","金昌市","白银市","天水市","武威市","张掖市","平凉市","酒泉市","庆阳市","定西市","陇南市","临夏回族自治州","甘南藏族自治州"],
        "青海省": ["西宁市","海东市","海北藏族自治州","黄南藏族自治州","海南藏族自治州","果洛藏族自治州","玉树藏族自治州","海西蒙古族藏族自治州"],
        "宁夏回族自治区": ["银川市","石嘴山市","吴忠市","固原市","中卫市"],
        "新疆维吾尔自治区": ["乌鲁木齐市","克拉玛依市","吐鲁番市","哈密市","昌吉回族自治州","博尔塔拉蒙古自治州","巴音郭楞蒙古自治州","阿克苏地区","克孜勒苏柯尔克孜自治州","喀什地区","和田地区","伊犁哈萨克自治州","塔城地区","阿勒泰地区"],
        "台湾省": ["台北市","高雄市","台中市","台南市","新北市","桃园市"],
        "香港特别行政区": ["中西区","湾仔区","东区","南区","油尖旺区","深水埗区","九龙城区","黄大仙区","观塘区","荃湾区","屯门区","元朗区","北区","大埔区","沙田区","西贡区","葵青区","离岛区"],
        "澳门特别行政区": ["花地玛堂区","花王堂区","望德堂区","大堂区","风顺堂区"]
    };

    var $province = $('#provinceSelect');
    var $city = $('#citySelect');
    var savedState = '${sessionScope.user.state}';
    var savedCity = '${sessionScope.user.city}';

    $.each(provinceCityData, function(province) {
        var selected = (province === savedState) ? ' selected' : '';
        $province.append('<option value="' + province + '"' + selected + '>' + province + '</option>');
    });

    if (savedState && provinceCityData[savedState]) {
        $city.empty().append('<option value="">请选择城市</option>');
        $.each(provinceCityData[savedState], function(i, city) {
            var selected = (city === savedCity) ? ' selected' : '';
            $city.append('<option value="' + city + '"' + selected + '>' + city + '</option>');
        });
    }

    $province.on('change', function() {
        var province = $(this).val();
        $city.empty().append('<option value="">请选择城市</option>');
        if (province && provinceCityData[province]) {
            $.each(provinceCityData[province], function(i, city) {
                $city.append('<option value="' + city + '">' + city + '</option>');
            });
        }
    });

    function switchToPasswordTab() {
        $('a[href="#tab-password"]').tab('show');
    }

    function showToast(msg) {
        var t = $('<div class="msg-toast"></div>');
        t.text(msg);
        $('body').append(t);
        setTimeout(function() { t.remove(); }, 2500);
    }

    $(document).ready(function() {
        var newPwdInput = $('#newPassword');
        var confirmInput = $('#confirmPassword');

        newPwdInput.on('input', function() {
            var pwd = $(this).val();
            var bar = $('#pwdStrength .bar');
            var hint = $('#pwdHint');
            if (pwd.length === 0) {
                bar.css('width', '0');
                hint.text('');
                return;
            }
            var score = 0;
            if (pwd.length >= 6) score++;
            if (pwd.length >= 10) score++;
            if (/[A-Z]/.test(pwd)) score++;
            if (/[0-9]/.test(pwd)) score++;
            if (/[^A-Za-z0-9]/.test(pwd)) score++;

            if (score <= 2) {
                bar.css({ 'width': '33%', 'background': '#ef4444' });
                hint.text('弱 - 建议使用更复杂的密码').css('color', '#ef4444');
            } else if (score <= 3) {
                bar.css({ 'width': '66%', 'background': '#f59e0b' });
                hint.text('中等 - 还可以更好').css('color', '#f59e0b');
            } else {
                bar.css({ 'width': '100%', 'background': '#10b981' });
                hint.text('强 - 密码安全性良好').css('color', '#10b981');
            }
        });

        $('#passwordForm').on('submit', function(e) {
            var current = $('#currentPassword').val();
            var newPwd = newPwdInput.val();
            var confirm = confirmInput.val();
            if (!current) {
                e.preventDefault();
                showToast('请输入当前密码');
                return;
            }
            if (newPwd.length < 6) {
                e.preventDefault();
                showToast('新密码至少需要6位');
                return;
            }
            if (newPwd !== confirm) {
                e.preventDefault();
                showToast('两次输入的密码不一致');
                return;
            }
        });

        $('#profileForm').on('submit', function() {
            var btn = $(this).find('.btn-save');
            btn.text('保存中...').prop('disabled', true);
        });

        var hash = window.location.hash;
        if (hash === '#password') {
            $('a[href="#tab-password"]').tab('show');
        } else if (hash === '#security') {
            $('a[href="#tab-security"]').tab('show');
        }
    });

    function uploadAvatar(input) {
        if (input.files && input.files[0]) {
            var file = input.files[0];
            if (file.size > 5 * 1024 * 1024) {
                showToast('⚠️ 图片大小不能超过5MB');
                input.value = '';
                return;
            }
            if (!file.type.startsWith('image/')) {
                showToast('⚠️ 只支持上传图片文件');
                input.value = '';
                return;
            }

            var avatarDiv = $('.user-avatar-lg');
            var badge = $('.avatar-badge');
            avatarDiv.css('opacity', '0.7');
            badge.html('<i class="fas fa-spinner fa-spin" style="font-size:12px;color:#fff;"></i>');

            var formData = new FormData();
            formData.append('avatar', file);

            $.ajax({
                url: '${pageContext.request.contextPath}/user/avatar',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    if (response.success) {
                        avatarDiv.html('<img src="${pageContext.request.contextPath}' + response.avatarUrl + '" alt="头像" style="animation:avatarFadeIn 0.5s ease;"><div class="avatar-overlay"><i class="fas fa-camera"></i><span>更换头像</span></div>');
                        avatarDiv.css('opacity', '1');
                        badge.html('<i class="fas fa-pen" style="font-size:12px;color:#fff;"></i>');
                        $('.avatar-upload-tip').html('✨ 已设置自定义头像');
                        showToast('🎉 头像上传成功！');
                    } else {
                        avatarDiv.css('opacity', '1');
                        badge.html('<i class="fas fa-pen" style="font-size:12px;color:#fff;"></i>');
                        showToast('❌ ' + (response.message || '上传失败'));
                    }
                },
                error: function() {
                    avatarDiv.css('opacity', '1');
                    badge.html('<i class="fas fa-pen" style="font-size:12px;color:#fff;"></i>');
                    showToast('❌ 网络错误，请稍后重试');
                }
            });
            input.value = '';
        }
    }

    var style = document.createElement('style');
    style.textContent = '@keyframes avatarFadeIn{from{opacity:0;transform:scale(0.8)}to{opacity:1;transform:scale(1)}}';
    document.head.appendChild(style);
</script>
</body>
</html>
