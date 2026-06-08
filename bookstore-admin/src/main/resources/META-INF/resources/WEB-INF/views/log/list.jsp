<%-- ============================================================
     log/list.jsp — 前台日志列表页面
     功能：展示系统操作日志记录列表（管理员可用）。
     说明：从后端获取日志数据，记录用户的操作行为。
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
        :root {
            --primary: #4361ee;
            --success: #2ecc71;
            --warning: #f39c12;
            --danger: #e74c3c;
            --gray-500: #95a5a6;
        }

        body { background: #f0f2f5; }

        .page-wrapper { padding: 20px; }

        .section-title {
            font-size: 22px;
            font-weight: 700;
            color: #2c3e50;
            margin: 0 0 20px 0;
        }

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

        .table-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            overflow: hidden;
        }

        .table-card > table { margin-bottom: 0; }

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

        .table-card > table > tbody > tr > td {
            vertical-align: middle;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 13px;
        }

        .table-card > table > tbody > tr:hover { background: #f8f9ff; }

        .op-tag {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
            letter-spacing: 0.3px;
        }

        .op-tag.add { background: #e8f5e9; color: var(--success); }
        .op-tag.edit { background: #e3f2fd; color: var(--primary); }
        .op-tag.delete { background: #fdecea; color: var(--danger); }
        .op-tag.login { background: #f3e5f5; color: #9b59b6; }
        .op-tag.logout { background: #fff3e0; color: var(--warning); }
        .op-tag.export { background: #e0f7fa; color: #00bcd4; }
        .op-tag.other { background: #f5f5f5; color: #777; }

        .ip-badge {
            font-family: 'Courier New', monospace;
            font-size: 12px;
            background: #f5f5f5;
            padding: 2px 8px;
            border-radius: 4px;
            color: #666;
        }

        .detail-text {
            max-width: 220px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            display: inline-block;
        }

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
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid">
    <div class="row">
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10 page-wrapper">
            <h2 class="section-title">📝 操作日志</h2>

            <div class="stat-mini-row">
                <div class="stat-mini">
                    总日志数 <strong>${totalLogs}</strong>
                </div>
            </div>

            <div class="toolbar">
                <form action="${pageContext.request.contextPath}/admin/log/list" method="get" class="form-inline">
                    <div class="filter-group">
                        <select name="operation" class="form-control input-sm">
                            <option value="">全部操作类型</option>
                            <option value="新增" <c:out value="${operationFilter == '新增' ? 'selected' : ''}"/>>新增</option>
                            <option value="编辑" <c:out value="${operationFilter == '编辑' ? 'selected' : ''}"/>>编辑</option>
                            <option value="删除" <c:out value="${operationFilter == '删除' ? 'selected' : ''}"/>>删除</option>
                            <option value="登录" <c:out value="${operationFilter == '登录' ? 'selected' : ''}"/>>登录</option>
                            <option value="登出" <c:out value="${operationFilter == '登出' ? 'selected' : ''}"/>>登出</option>
                            <option value="导出" <c:out value="${operationFilter == '导出' ? 'selected' : ''}"/>>导出</option>
                        </select>
                        <input type="text" name="keyword" class="form-control input-sm"
                               placeholder="搜索管理员或详情..." value="<c:out value="${keyword}"/>">
                        <button type="submit" class="btn btn-sm btn-primary">🔍 搜索</button>
                        <c:if test="${not empty operationFilter or not empty keyword}">
                            <a href="${pageContext.request.contextPath}/admin/log/list" class="btn btn-sm btn-default">清除筛选</a>
                        </c:if>
                    </div>
                </form>
            </div>

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
                                <td><fmt:formatDate value="${log.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                <td><strong>${log.adminName}</strong></td>
                                <td>
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
                                <td>
                                    <span class="detail-text" title="${log.detail}">${log.detail}</span>
                                </td>
                                <td><span class="ip-badge">${log.ip}</span></td>
                            </tr>
                        </c:forEach>
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

            <c:if test="${pageInfo != null && pageInfo.pages > 1}">
                <div class="pagination-wrap">
                    <div class="info-text">
                        共 ${pageInfo.total} 条记录，第 ${pageInfo.pageNum}/${pageInfo.pages} 页，每页 ${pageInfo.pageSize} 条
                    </div>
                    <ul class="pagination pagination-sm">
                        <li class="${pageInfo.hasPreviousPage ? '' : 'disabled'}">
                            <a href="${pageContext.request.contextPath}/admin/log/list?pageNum=${pageInfo.pageNum - 1}&pageSize=${pageInfo.pageSize}${not empty operationFilter ? '&operation=' : ''}${operationFilter}${not empty keyword ? '&keyword=' : ''}${keyword}">&laquo;</a>
                        </li>
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
                        <li class="${pageInfo.hasNextPage ? '' : 'disabled'}">
                            <a href="${pageContext.request.contextPath}/admin/log/list?pageNum=${pageInfo.pageNum + 1}&pageSize=${pageInfo.pageSize}${not empty operationFilter ? '&operation=' : ''}${operationFilter}${not empty keyword ? '&keyword=' : ''}${keyword}">&raquo;</a>
                        </li>
                    </ul>
                </div>
            </c:if>
        </div>
    </div>
</div>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
</body>
</html>
