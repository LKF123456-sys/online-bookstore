<%-- ============================================================
     message/list.jsp — 前台消息列表页面
     功能：展示系统消息和通知列表。
     说明：从后端获取消息数据，支持标记已读。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>消息中心 - 在线书店管理后台</title>
    <style>
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
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: var(--bg); color: var(--text); }

        /* 导航栏 */
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

        .container { max-width: 1000px; margin: 24px auto; padding: 0 16px; }

        /* 选项卡 */
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
        .tab-btn.active { background: var(--card-bg); color: var(--primary); border-bottom: 3px solid var(--primary); }
        .tab-btn .badge {
            background: var(--danger);
            color: #fff;
            font-size: 11px;
            padding: 2px 6px;
            border-radius: 10px;
            margin-left: 6px;
        }

        .tab-panel { display: none; }
        .tab-panel.active { display: block; }

        .card {
            background: var(--card-bg);
            border-radius: 12px;
            box-shadow: var(--shadow);
            overflow: hidden;
        }

        /* 消息列表 */
        .msg-item {
            display: flex;
            align-items: flex-start;
            padding: 16px 20px;
            border-bottom: 1px solid var(--border);
            transition: background 0.2s;
        }
        .msg-item:last-child { border-bottom: none; }
        .msg-item:hover { background: #f8fafc; }
        .msg-item.unread { background: #f0f4ff; }
        .msg-avatar {
            width: 40px; height: 40px;
            border-radius: 50%;
            display: flex; align-items: center; justify-content: center;
            font-size: 18px;
            flex-shrink: 0;
            margin-right: 12px;
        }
        .msg-avatar.admin { background: #fef3c7; }
        .msg-avatar.user { background: #dbeafe; }
        .msg-body { flex: 1; min-width: 0; }
        .msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
        .msg-sender { font-weight: 600; font-size: 14px; }
        .msg-time { font-size: 12px; color: var(--text-light); }
        .msg-content { font-size: 14px; color: var(--text); line-height: 1.5; }
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

        /* 发送框 */
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

        .empty-msg {
            text-align: center;
            padding: 60px 20px;
            color: var(--text-light);
            font-size: 15px;
        }
    </style>
</head>
<body>

<div class="top-bar">
    <h1>💬 消息中心</h1>
    <a href="${pageContext.request.contextPath}/admin/dashboard">← 返回仪表盘</a>
</div>

<div class="container">
    <!-- 选项卡 -->
    <div class="tabs">
        <button class="tab-btn active" onclick="switchTab('received', this)">
            收件箱
            <c:if test="${unreadCount > 0}">
                <span class="badge">${unreadCount}</span>
            </c:if>
        </button>
        <button class="tab-btn" onclick="switchTab('sent', this)">发件箱</button>
        <button class="tab-btn" onclick="switchTab('compose', this)">写信</button>
    </div>

    <!-- 收件箱 -->
    <div id="tab-received" class="tab-panel active">
        <div class="card">
            <c:if test="${empty messageList}">
                <div class="empty-msg">📭 暂无消息</div>
            </c:if>
            <c:forEach var="msg" items="${messageList}">
                <div class="msg-item ${msg.readStatus == 0 ? 'unread' : ''}" id="msg-${msg.id}" data-id="${msg.id}" data-read="${msg.readStatus}" style="${msg.readStatus == 0 ? 'cursor:pointer;' : ''}">
                    <div class="msg-avatar user">👤</div>
                    <div class="msg-body">
                        <div class="msg-header">
                            <span class="msg-sender"><c:out value="${msg.senderId}"/> <c:if test="${msg.readStatus == 0}"><span style="color:var(--danger);font-size:12px;" id="badge-${msg.id}">● 未读</span></c:if></span>
                            <span class="msg-time"><fmt:formatDate value="${msg.createTime}" pattern="yyyy-MM-dd HH:mm"/></span>
                        </div>
                        <div class="msg-content"><c:out value="${msg.content}"/></div>
                    </div>
                    <div class="msg-actions">
                        <button onclick="event.stopPropagation();deleteMsg(${msg.id})" title="删除">🗑️</button>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- 发件箱 -->
    <div id="tab-sent" class="tab-panel">
        <div class="card">
            <c:if test="${empty sentMessages}">
                <div class="empty-msg">📤 暂无发送记录</div>
            </c:if>
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

    <!-- 写信 -->
    <div id="tab-compose" class="tab-panel">
        <div class="card">
            <div class="compose-box">
                <h3> 发送消息给用户</h3>
                <div class="compose-row" style="margin-bottom:12px;">
                    <input type="text" id="receiverId" placeholder="输入用户ID（如 admin）" />
                </div>
                <div class="compose-row">
                    <textarea id="msgContent" placeholder="输入消息内容..."></textarea>
                    <button class="btn-send" onclick="sendMsg()">发送</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
function switchTab(tab, el) {
    document.querySelectorAll('.tab-btn').forEach((btn, i) => btn.classList.remove('active'));
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    el.classList.add('active');
    document.getElementById('tab-' + tab).classList.add('active');
}

function sendMsg() {
    var receiverId = document.getElementById('receiverId').value.trim();
    var content = document.getElementById('msgContent').value.trim();
    if (!receiverId || !content) { alert('请填写完整信息'); return; }
    var formData = new FormData();
    formData.append('receiverId', receiverId);
    formData.append('content', content);
    fetch('${pageContext.request.contextPath}/admin/message/send', {
        method: 'POST',
        body: formData
    }).then(r => r.json()).then(d => {
        alert(d.message);
        if (d.success) {
            document.getElementById('msgContent').value = '';
            location.reload();
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.msg-item[data-read="0"]').forEach(function(item) {
        item.addEventListener('click', function() {
            markRead(this.getAttribute('data-id'));
        });
    });
});

function markRead(id) {
    fetch('${pageContext.request.contextPath}/message/read?id=' + id, { method: 'GET' })
        .then(r => r.json()).then(d => {
            if (d.success) {
                var item = document.getElementById('msg-' + id);
                if (item) {
                    item.classList.remove('unread');
                    item.style.cursor = 'default';
                    item.removeAttribute('data-read');
                }
                var badge = document.getElementById('badge-' + id);
                if (badge) badge.style.display = 'none';
                var countEl = document.querySelector('.tab-btn .badge');
                if (countEl) {
                    var count = parseInt(countEl.textContent) - 1;
                    if (count > 0) {
                        countEl.textContent = count;
                    } else {
                        countEl.remove();
                    }
                }
            }
        });
}

function deleteMsg(id) {
    if (!confirm('确定删除此消息？')) return;
    var formData = new FormData();
    formData.append('id', id);
    fetch('${pageContext.request.contextPath}/admin/message/delete', {
        method: 'POST',
        body: formData
    }).then(r => r.json()).then(d => {
        if (d.success) {
            document.getElementById('msg-' + id).remove();
        } else {
            alert(d.message);
        }
    });
}
</script>
</body>
</html>
