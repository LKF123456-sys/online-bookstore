<%-- ============================================================
     操作日志页面 (log/list.jsp)
     功能：记录和展示管理员在后台的所有操作日志
     主要功能：
       - 顶部显示总日志数统计
       - 按操作类型筛选（全部/新增/编辑/删除/登录/登出/导出）
       - 按管理员或详情关键词搜索
       - 查看日志列表（时间、管理员、操作类型、操作对象、详情、IP地址）
       - 操作类型用不同颜色标签区分
       - 分页导航
     使用了 Bootstrap 3 + 自定义卡片样式
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>操作日志 - BookVerse 管理后台</title>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        /* ==================== CSS变量定义 ==================== */
        :root {
            --primary: #4361ee;
            --success: #2ecc71;
            --warning: #f39c12;
            --danger: #e74c3c;
            --gray-500: #95a5a6;
        }

        body { background: #f0f2f5; }

        /* ==================== 页面容器 ==================== */
        .page-wrapper { padding: 20px; }

        /* ==================== 页面标题 ==================== */
        .section-title {
            font-size: 22px;
            font-weight: 700;
            color: #2c3e50;
            margin: 0 0 20px 0;
        }

        /* ==================== 工具栏（筛选+搜索） ==================== */
        .toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
            flex-wrap: wrap;
            gap: 10px;
        }
        .filter-group {
            display: flex;
            gap: 10px;
            align-items: center;
            flex-wrap: wrap;
        }
        .filter-group select,
        .filter-group input {
            border-radius: 8px;
            border: 1px solid #ddd;
            padding: 6px 12px;
            font-size: 13px;
        }
        .filter-group input {
            width: 200px;
        }

        /* ==================== 卡片式表格容器 ==================== */
        .table-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            overflow: hidden;
        }
        .table-card > table { margin-bottom: 0; }
        /* 表头样式 */
        .table-card > table > thead > tr > th {
            background: #fafbfc;
            border-bottom: 2px solid #e8e8e8;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #7f8c8d;
            padding: 14px 12px;
            font-weight: 600;
        }
        /* 表格单元格样式 */
        .table-card > table > tbody > tr > td {
            vertical-align: middle;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 13px;
        }
        /* 表格行悬停效果 */
        .table-card > table > tbody > tr:hover { background: #f8f9ff; }

        /* ==================== 操作类型标签 ==================== */
        /* 不同操作类型用不同颜色区分 */
        .op-tag {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
            letter-spacing: 0.3px;
        }
        .op-tag.add { background: #e8f5e9; color: var(--success); }       /* 新增=绿色 */
        .op-tag.edit { background: #e3f2fd; color: var(--primary); }      /* 编辑=蓝色 */
        .op-tag.delete { background: #fdecea; color: var(--danger); }     /* 删除=红色 */
        .op-tag.login { background: #f3e5f5; color: #9b59b6; }           /* 登录=紫色 */
        .op-tag.logout { background: #fff3e0; color: var(--warning); }    /* 登出=橙色 */
        .op-tag.export { background: #e0f7fa; color: #00bcd4; }          /* 导出=青色 */
        .op-tag.other { background: #f5f5f5; color: #777; }              /* 其他=灰色 */

        /* ==================== IP地址徽章 ==================== */
        .ip-badge {
            font-family: 'Courier New', monospace;
            font-size: 12px;
            background: #f5f5f5;
            padding: 2px 8px;
            border-radius: 4px;
            color: #666;
        }

        /* ==================== 详情文本截断 ==================== */
        .detail-text {
            max-width: 220px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            display: inline-block;
        }

        /* ==================== 统计迷你卡片 ==================== */
        .stat-mini-row {
            display: flex;
            gap: 12px;
            margin-bottom: 16px;
        }
        .stat-mini {
            padding: 10px 16px;
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 1px 6px rgba(0,0,0,0.04);
            font-size: 13px;
            color: #555;
        }
        .stat-mini strong {
            color: var(--primary);
            font-size: 18px;
        }

        /* ==================== 分页区域 ==================== */
        .pagination-wrap {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 15px;
            background: #fff;
            padding: 12px 16px;
            border-radius: 10px;
            box-shadow: 0 1px 6px rgba(0,0,0,0.04);
        }
        .pagination-wrap .info-text {
            font-size: 13px;
            color: var(--gray-500);
        }
        .pagination-wrap .pagination { margin: 0; }
    </style>
</head>
<body>
<%-- 引入管理后台公共头部 --%>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid">
    <div class="row">
        <%-- 引入管理后台侧边栏 --%>
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10 page-wrapper">
            <%-- 页面标题 --%>
            <h2 class="section-title">📝 操作日志</h2>

            <%-- ==================== 统计信息 ==================== --%>
            <div class="stat-mini-row">
                <div class="stat-mini">
                    总日志数 <strong>${totalLogs}</strong>
                </div>
            </div>

            <%-- ==================== 筛选和搜索工具栏 ==================== --%>
            <div class="toolbar">
                <form action="${pageContext.request.contextPath}/admin/log/list" method="get" class="form-inline">
                    <div class="filter-group">
                        <%-- 操作类型下拉筛选 --%>
                        <select name="operation" class="form-control input-sm">
                            <option value="">全部操作类型</option>
                            <option value="新增" <c:out value="${operationFilter == '新增' ? 'selected' : ''}"/>>新增</option>
                            <option value="编辑" <c:out value="${operationFilter == '编辑' ? 'selected' : ''}"/>>编辑</option>
                            <option value="删除" <c:out value="${operationFilter == '删除' ? 'selected' : ''}"/>>删除</option>
                            <option value="登录" <c:out value="${operationFilter == '登录' ? 'selected' : ''}"/>>登录</option>
                            <option value="登出" <c:out value="${operationFilter == '登出' ? 'selected' : ''}"/>>登出</option>
                            <option value="导出" <c:out value="${operationFilter == '导出' ? 'selected' : ''}"/>>导出</option>
                        </select>
                        <%-- 关键词搜索输入框 --%>
                        <input type="text" name="keyword" class="form-control input-sm"
                               placeholder="搜索管理员或详情..." value="<c:out value="${keyword}"/>">
                        <button type="submit" class="btn btn-sm btn-primary">🔍 搜索</button>
                        <%-- 有筛选条件时显示"清除筛选"按钮 --%>
                        <c:if test="${not empty operationFilter or not empty keyword}">
                            <a href="${pageContext.request.contextPath}/admin/log/list" class="btn btn-sm btn-default">清除筛选</a>
                        </c:if>
                    </div>
                </form>
            </div>

            <%-- ==================== 日志数据表格 ==================== --%>
            <div class="table-card">
                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th style="width:140px;">时间</th>
                            <th style="width:90px;">管理员</th>
                            <th style="width:80px;">操作类型</th>
                            <th style="width:110px;">操作对象</th>
                            <th>详情</th>
                            <th style="width:140px;">IP地址</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${logList}" var="log">
                            <tr>
                                <%-- 格式化显示操作时间 --%>
                                <td><fmt:formatDate value="${log.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                <td><strong>${log.adminName}</strong></td>
                                <td>
                                    <%-- 根据操作类型动态设置CSS类名，实现不同颜色标签 --%>
                                    <c:set var="opClass" value="other"/>
                                    <c:choose>
                                        <c:when test="${log.operation == '新增' || log.operation == '添加'}">
                                            <c:set var="opClass" value="add"/>
                                        </c:when>
                                        <c:when test="${log.operation == '编辑' || log.operation == '修改' || log.operation == '更新'}">
                                            <c:set var="opClass" value="edit"/>
                                        </c:when>
                                        <c:when test="${log.operation == '删除'}">
                                            <c:set var="opClass" value="delete"/>
                                        </c:when>
                                        <c:when test="${log.operation == '登录'}">
                                            <c:set var="opClass" value="login"/>
                                        </c:when>
                                        <c:when test="${log.operation == '登出'}">
                                            <c:set var="opClass" value="logout"/>
                                        </c:when>
                                        <c:when test="${log.operation == '导出'}">
                                            <c:set var="opClass" value="export"/>
                                        </c:when>
                                    </c:choose>
                                    <span class="op-tag ${opClass}">${log.operation}</span>
                                </td>
                                <td>${log.target}</td>
                                <%-- 详情超长时截断，鼠标悬停显示完整内容 --%>
                                <td>
                                    <span class="detail-text" title="${log.detail}">${log.detail}</span>
                                </td>
                                <%-- IP地址用等宽字体显示 --%>
                                <td><span class="ip-badge">${log.ip}</span></td>
                            </tr>
                        </c:forEach>
                        <%-- 日志列表为空时显示提示 --%>
                        <c:if test="${empty logList}">
                            <tr>
                                <td colspan="6" class="text-center" style="padding:40px;color:var(--gray-500);">
                                    暂无操作日志
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <%-- ==================== 分页控件 ==================== --%>
            <c:if test="${pageInfo != null && pageInfo.pages > 1}">
                <div class="pagination-wrap">
                    <div class="info-text">
                        共 ${pageInfo.total} 条记录，第 ${pageInfo.pageNum}/${pageInfo.pages} 页，每页 ${pageInfo.pageSize} 条
                    </div>
                    <ul class="pagination pagination-sm">
                        <%-- 上一页按钮 --%>
                        <li class="${pageInfo.hasPreviousPage ? '' : 'disabled'}">
                            <a href="${pageContext.request.contextPath}/admin/log/list?pageNum=${pageInfo.pageNum - 1}&pageSize=${pageInfo.pageSize}${not empty operationFilter ? '&operation=' : ''}${operationFilter}${not empty keyword ? '&keyword=' : ''}${keyword}">&laquo;</a>
                        </li>
                        <%-- 页码数字，显示首尾页和当前页附近页码 --%>
                        <c:forEach var="i" begin="1" end="${pageInfo.pages}">
                            <c:choose>
                                <c:when test="${i == pageInfo.pageNum}">
                                    <li class="active"><a href="#">${i}</a></li>
                                </c:when>
                                <c:when test="${i <= 2 or i >= pageInfo.pages - 1 or i == pageInfo.pageNum}">
                                    <li><a href="${pageContext.request.contextPath}/admin/log/list?pageNum=${i}&pageSize=${pageInfo.pageSize}${not empty operationFilter ? '&operation=' : ''}${operationFilter}${not empty keyword ? '&keyword=' : ''}${keyword}">${i}</a></li>
                                </c:when>
                                <c:when test="${i == 3 or i == pageInfo.pages - 2}">
                                    <li class="disabled"><a href="#">...</a></li>
                                </c:when>
                            </c:choose>
                        </c:forEach>
                        <%-- 下一页按钮 --%>
                        <li class="${pageInfo.hasNextPage ? '' : 'disabled'}">
                            <a href="${pageContext.request.contextPath}/admin/log/list?pageNum=${pageInfo.pageNum + 1}&pageSize=${pageInfo.pageSize}${not empty operationFilter ? '&operation=' : ''}${operationFilter}${not empty keyword ? '&keyword=' : ''}${keyword}">&raquo;</a>
                        </li>
                    </ul>
                </div>
            </c:if>
        </div>
    </div>
</div>
<%-- 引入 jQuery 和 Bootstrap JS --%>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
</body>
</html>