<%-- ============================================================
     消息管理页面 (message/list.jsp)
     功能：管理员查看和管理系统内部消息
     主要功能：
       - 三个选项卡：收件箱、发件箱、写信
       - 收件箱：显示收到的消息，未读消息高亮，点击标记已读
       - 发件箱：显示已发送的消息
       - 写信：向指定用户发送新消息
       - 删除消息（需二次确认）
       - 未读消息数量实时更新
     使用了 Fetch API（现代浏览器API）替代 jQuery AJAX
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    // 构建项目根路径，用于资源引用
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>消息中心 - 在线书店管理后台</title>
    <style>
        /* ==================== CSS变量定义 ==================== */
        :root {
            --primary: #4361ee;
            --primary-dark: #3a56d4;
            --success: #06d6a0;
            --warning: #ffd166;
            --danger: #ef476f;
            --bg: #f8fafc;
            --card-bg: #ffffff;
            --text: #1e293b;
            --text-light: #64748b;
            --border: #e2e8f0;
            --shadow: 0 2px 12px rgba(0,0,0,0.08);
        }
        /* ==================== 基础重置 ==================== */
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: var(--bg); color: var(--text); }

        /* ==================== 顶部导航栏 ==================== */
        .top-bar {
            background: var(--card-bg);
            border-bottom: 1px solid var(--border);
            padding: 16px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .top-bar h1 { font-size: 20px; font-weight: 700; }
        .top-bar a {
            color: var(--primary);
            text-decoration: none;
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
        }
        .top-bar a:hover { background: rgba(67,97,238,0.1); }

        /* ==================== 页面容器 ==================== */
        .container { max-width: 1000px; margin: 24px auto; padding: 0 16px; }

        /* ==================== 选项卡样式 ==================== */
        .tabs { display: flex; gap: 4px; margin-bottom: 20px; }
        .tab-btn {
            padding: 10px 24px;
            border: none;
            background: var(--card-bg);
            color: var(--text-light);
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            border-radius: 10px 10px 0 0;
            position: relative;
        }
        /* 选中的选项卡高亮 */
        .tab-btn.active { background: var(--card-bg); color: var(--primary); border-bottom: 3px solid var(--primary); }
        /* 未读消息数量徽章 */
        .tab-btn .badge {
            background: var(--danger);
            color: #fff;
            font-size: 11px;
            padding: 2px 6px;
            border-radius: 10px;
            margin-left: 6px;
        }

        /* 选项卡面板切换 */
        .tab-panel { display: none; }
        .tab-panel.active { display: block; }

        /* ==================== 卡片容器 ==================== */
        .card {
            background: var(--card-bg);
            border-radius: 12px;
            box-shadow: var(--shadow);
            overflow: hidden;
        }

        /* ==================== 消息列表项 ==================== */
        .msg-item {
            display: flex;
            align-items: flex-start;
            padding: 16px 20px;
            border-bottom: 1px solid var(--border);
            transition: background 0.2s;
        }
        .msg-item:last-child { border-bottom: none; }
        .msg-item:hover { background: #f8fafc; }
        .msg-item.unread { background: #f0f4ff; }  /* 未读消息蓝色背景高亮 */
        /* 消息头像 */
        .msg-avatar {
            width: 40px; height: 40px;
            border-radius: 50%;
            display: flex; align-items: center; justify-content: center;
            font-size: 18px;
            flex-shrink: 0;
            margin-right: 12px;
        }
        .msg-avatar.admin { background: #fef3c7; }  /* 管理员头像=黄色 */
        .msg-avatar.user { background: #dbeafe; }    /* 用户头像=蓝色 */
        /* 消息内容区 */
        .msg-body { flex: 1; min-width: 0; }
        .msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
        .msg-sender { font-weight: 600; font-size: 14px; }
        .msg-time { font-size: 12px; color: var(--text-light); }
        .msg-content { font-size: 14px; color: var(--text); line-height: 1.5; }
        /* 消息操作按钮 */
        .msg-actions { display: flex; gap: 8px; margin-left: 12px; }
        .msg-actions button {
            border: none;
            background: none;
            cursor: pointer;
            font-size: 16px;
            padding: 4px;
            border-radius: 6px;
            transition: background 0.2s;
        }
        .msg-actions button:hover { background: #f1f5f9; }

        /* ==================== 写信区域 ==================== */
        .compose-box {
            padding: 20px;
            border-bottom: 1px solid var(--border);
        }
        .compose-box h3 { font-size: 15px; margin-bottom: 12px; color: var(--text-light); }
        .compose-row { display: flex; gap: 12px; }
        .compose-row input, .compose-row textarea {
            flex: 1;
            padding: 10px 14px;
            border: 1px solid var(--border);
            border-radius: 8px;
            font-size: 14px;
            outline: none;
        }
        .compose-row textarea { height: 80px; resize: vertical; font-family: inherit; }
        .compose-row input:focus, .compose-row textarea:focus { border-color: var(--primary); }
        .btn-send {
            padding: 10px 24px;
            background: var(--primary);
            color: #fff;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            white-space: nowrap;
        }
        .btn-send:hover { background: var(--primary-dark); }

        /* ==================== 空状态提示 ==================== */
        .empty-msg {
            text-align: center;
            padding: 60px 20px;
            color: var(--text-light);
            font-size: 15px;
        }
    </style>
</head>
<body>

<%-- ==================== 顶部导航栏 ==================== --%>
<div class="top-bar">
    <h1>💬 消息中心</h1>
    <a href="${pageContext.request.contextPath}/admin/dashboard">← 返回仪表盘</a>
</div>

<%-- ==================== 主内容容器 ==================== --%>
<div class="container">
    <%-- ==================== 选项卡切换栏 ==================== --%>
    <div class="tabs">
        <%-- 收件箱选项卡，显示未读消息数量徽章 --%>
        <button class="tab-btn active" onclick="switchTab('received', this)">
            收件箱
            <c:if test="${unreadCount > 0}">
                <span class="badge">${unreadCount}</span>
            </c:if>
        </button>
        <%-- 发件箱选项卡 --%>
        <button class="tab-btn" onclick="switchTab('sent', this)">发件箱</button>
        <%-- 写信选项卡 --%>
        <button class="tab-btn" onclick="switchTab('compose', this)">写信</button>
    </div>

    <%-- ==================== 收件箱面板 ==================== --%>
    <div id="tab-received" class="tab-panel active">
        <div class="card">
            <%-- 无消息时显示空状态 --%>
            <c:if test="${empty messageList}">
                <div class="empty-msg">📭 暂无消息</div>
            </c:if>
            <%-- 遍历收件箱消息列表 --%>
            <c:forEach var="msg" items="${messageList}">
                <%-- 未读消息添加 unread 样式类和点击事件 --%>
                <div class="msg-item ${msg.readStatus == 0 ? 'unread' : ''}" id="msg-${msg.id}" data-id="${msg.id}" data-read="${msg.readStatus}" style="${msg.readStatus == 0 ? 'cursor:pointer;' : ''}">
                    <div class="msg-avatar user">👤</div>
                    <div class="msg-body">
                        <div class="msg-header">
                            <span class="msg-sender"><c:out value="${msg.senderId}"/> <c:if test="${msg.readStatus == 0}"><span style="color:var(--danger);font-size:12px;" id="badge-${msg.id}">● 未读</span></c:if></span>
                            <span class="msg-time"><fmt:formatDate value="${msg.createTime}" pattern="yyyy-MM-dd HH:mm"/></span>
                        </div>
                        <div class="msg-content"><c:out value="${msg.content}"/></div>
                    </div>
                    <%-- 删除按钮，event.stopPropagation() 阻止事件冒泡触发标记已读 --%>
                    <div class="msg-actions">
                        <button onclick="event.stopPropagation();deleteMsg(${msg.id})" title="删除">🗑️</button>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <%-- ==================== 发件箱面板 ==================== --%>
    <div id="tab-sent" class="tab-panel">
        <div class="card">
            <c:if test="${empty sentMessages}">
                <div class="empty-msg">📤 暂无发送记录</div>
            </c:if>
            <%-- 遍历已发送的消息 --%>
            <c:forEach var="msg" items="${sentMessages}">
                <div class="msg-item">
                    <div class="msg-avatar admin">👤</div>
                    <div class="msg-body">
                        <div class="msg-header">
                            <span class="msg-sender">发给 ${msg.receiverId}</span>
                            <span class="msg-time"><fmt:formatDate value="${msg.createTime}" pattern="yyyy-MM-dd HH:mm"/></span>
                        </div>
                        <div class="msg-content"><c:out value="${msg.content}"/></div>
                    </div>
                    <div class="msg-actions">
                        <button onclick="deleteMsg(${msg.id})" title="删除">🗑️</button>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <%-- ==================== 写信面板 ==================== --%>
    <div id="tab-compose" class="tab-panel">
        <div class="card">
            <div class="compose-box">
                <h3> 发送消息给用户</h3>
                <%-- 收件人ID输入框 --%>
                <div class="compose-row" style="margin-bottom:12px;">
                    <input type="text" id="receiverId" placeholder="输入用户ID（如 admin）" />
                </div>
                <%-- 消息内容输入框 + 发送按钮 --%>
                <div class="compose-row">
                    <textarea id="msgContent" placeholder="输入消息内容..."></textarea>
                    <button class="btn-send" onclick="sendMsg()">发送</button>
                </div>
            </div>
        </div>
    </div>
</div>

<%-- ==================== JavaScript 部分 ==================== --%>
<script>
/**
 * 切换选项卡面板
 * @param tab - 选项卡名称（received/sent/compose）
 * @param el - 被点击的按钮元素
 */
function switchTab(tab, el) {
    // 移除所有选项卡的active状态
    document.querySelectorAll('.tab-btn').forEach((btn, i) => btn.classList.remove('active'));
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    // 为当前选项卡和对应面板添加active状态
    el.classList.add('active');
    document.getElementById('tab-' + tab).classList.add('active');
}

/**
 * 发送消息
 * 使用 Fetch API 发送 POST 请求（比 jQuery AJAX 更现代的方式）
 */
function sendMsg() {
    var receiverId = document.getElementById('receiverId').value.trim();  // 获取收件人ID
    var content = document.getElementById('msgContent').value.trim();     // 获取消息内容
    if (!receiverId || !content) { alert('请填写完整信息'); return; }     // 验证非空
    // 构建表单数据
    var formData = new FormData();
    formData.append('receiverId', receiverId);
    formData.append('content', content);
    // 使用 Fetch API 发送请求
    fetch('${pageContext.request.contextPath}/admin/message/send', {
        method: 'POST',
        body: formData
    }).then(r => r.json()).then(d => {          // 解析JSON响应
        alert(d.message);
        if (d.success) {
            document.getElementById('msgContent').value = '';  // 清空输入框
            location.reload();                                 // 刷新页面
        }
    });
}

/**
 * 页面加载完成后，为未读消息绑定点击事件（点击标记为已读）
 */
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.msg-item[data-read="0"]').forEach(function(item) {
        item.addEventListener('click', function() {
            markRead(this.getAttribute('data-id'));  // 点击未读消息触发标记已读
        });
    });
});

/**
 * 标记消息为已读
 * @param id - 消息ID
 */
function markRead(id) {
    // 发送GET请求到后端标记已读
    fetch('${pageContext.request.contextPath}/message/read?id=' + id, { method: 'GET' })
        .then(r => r.json()).then(d => {
            if (d.success) {
                // 更新UI：移除未读样式
                var item = document.getElementById('msg-' + id);
                if (item) {
                    item.classList.remove('unread');       // 移除蓝色背景
                    item.style.cursor = 'default';         // 恢复默认鼠标样式
                    item.removeAttribute('data-read');      // 移除未读标记
                }
                // 隐藏"未读"文字标记
                var badge = document.getElementById('badge-' + id);
                if (badge) badge.style.display = 'none';
                // 更新选项卡上的未读数量徽章
                var countEl = document.querySelector('.tab-btn .badge');
                if (countEl) {
                    var count = parseInt(countEl.textContent) - 1;  // 数量减1
                    if (count > 0) {
                        countEl.textContent = count;      // 更新数字
                    } else {
                        countEl.remove();                  // 归零则移除徽章
                    }
                }
            }
        });
}

/**
 * 删除消息
 * @param id - 消息ID
 */
function deleteMsg(id) {
    if (!confirm('确定删除此消息？')) return;  // 二次确认
    var formData = new FormData();
    formData.append('id', id);
    // 使用 Fetch API 发送删除请求
    fetch('${pageContext.request.contextPath}/admin/message/delete', {
        method: 'POST',
        body: formData
    }).then(r => r.json()).then(d => {
        if (d.success) {
            document.getElementById('msg-' + id).remove();  // 成功则从DOM移除消息元素
        } else {
            alert(d.message);
        }
    });
}
</script>
</body>
</html>