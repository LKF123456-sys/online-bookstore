<%-- ============================================================
     订单管理列表页面 (order/list.jsp)
     功能：展示和管理所有订单信息
     主要功能：
       - 顶部统计卡片（总订单、待支付、已支付、已发货、已完成、总收入）
       - 按订单状态筛选（全部/待支付/已支付/已发货/已完成/已取消）
       - 查看订单列表（订单号、用户、日期、金额、支付方式、物流、状态）
       - 修改订单状态（通过下拉菜单）
       - 已支付订单可发货（需输入物流单号）
       - 待支付/已取消订单可删除
       - 分页导航
     使用了 Bootstrap 3 + 自定义统计卡片样式
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>订单管理 - 后台管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        /* ==================== 统计卡片区域 ==================== */
        /* 横向排列的渐变色统计卡片，展示订单各状态数量 */
        .stat-cards { display: flex; gap: 15px; margin-bottom: 20px; flex-wrap: wrap; }
        .stat-card { flex: 1; min-width: 150px; padding: 15px; border-radius: 8px; color: #fff; text-align: center; }
        .stat-card h3 { margin: 0; font-size: 28px; font-weight: bold; }
        .stat-card p { margin: 5px 0 0; font-size: 13px; opacity: 0.9; }
        /* 各统计卡片的渐变背景色 */
        .stat-total { background: linear-gradient(135deg, #667eea, #764ba2); }      /* 总订单=紫色 */
        .stat-pending { background: linear-gradient(135deg, #f093fb, #f5576c); }    /* 待支付=粉色 */
        .stat-paid { background: linear-gradient(135deg, #4facfe, #00f2fe); }       /* 已支付=蓝色 */
        .stat-shipping { background: linear-gradient(135deg, #43e97b, #38f9d7); }   /* 已发货=绿色 */
        .stat-completed { background: linear-gradient(135deg, #fa709a, #fee140); }  /* 已完成=橙黄 */
        .stat-revenue { background: linear-gradient(135deg, #a18cd1, #fbc2eb); }    /* 总收入=紫色渐变 */

        /* ==================== 状态筛选按钮栏 ==================== */
        .status-filter { margin-bottom: 15px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
        .status-filter .btn { margin: 0; }

        /* ==================== 操作按钮组 ==================== */
        .order-actions .btn { margin-right: 5px; margin-bottom: 3px; }

        /* ==================== 分页区域 ==================== */
        .pagination-container { display: flex; justify-content: space-between; align-items: center; margin-top: 15px; flex-wrap: wrap; }
        .pagination-info { color: #666; font-size: 14px; }
        .pagination { margin: 0; }
    </style>
</head>
<body>
<%-- 引入管理后台公共头部 --%>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid" style="margin-top: 20px;">
    <div class="row">
        <%-- 引入管理后台侧边栏 --%>
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">订单管理</h3>
                </div>
                <div class="panel-body">
                    <%-- ==================== 订单统计卡片 ==================== --%>
                    <div class="stat-cards">
                        <div class="stat-card stat-total">
                            <h3>${totalOrders}</h3>
                            <p>总订单数</p>
                        </div>
                        <div class="stat-card stat-pending">
                            <h3>${pendingCount}</h3>
                            <p>待支付</p>
                        </div>
                        <div class="stat-card stat-paid">
                            <h3>${paidCount}</h3>
                            <p>已支付</p>
                        </div>
                        <div class="stat-card stat-shipping">
                            <h3>${shippingCount}</h3>
                            <p>已发货</p>
                        </div>
                        <div class="stat-card stat-completed">
                            <h3>${completedCount}</h3>
                            <p>已完成</p>
                        </div>
                        <div class="stat-card stat-revenue">
                            <h3>¥<fmt:formatNumber value="${totalRevenue}" pattern="#,##0.00"/></h3>
                            <p>总收入</p>
                        </div>
                    </div>
                    <%-- ==================== 状态筛选栏 ==================== --%>
                    <%-- 每个按钮是一个链接，点击后带status参数重新加载页面 --%>
                    <%-- 当前选中的状态按钮高亮显示 --%>
                    <div class="status-filter">
                        <span>状态筛选：</span>
                        <a href="${pageContext.request.contextPath}/admin/order" class="btn btn-sm ${empty selectedStatus ? 'btn-primary' : 'btn-default'}">全部</a>
                        <a href="${pageContext.request.contextPath}/admin/order?status=待支付" class="btn btn-sm ${selectedStatus == '待支付' ? 'btn-warning' : 'btn-default'}">待支付</a>
                        <a href="${pageContext.request.contextPath}/admin/order?status=已支付" class="btn btn-sm ${selectedStatus == '已支付' ? 'btn-info' : 'btn-default'}">已支付</a>
                        <a href="${pageContext.request.contextPath}/admin/order?status=已发货" class="btn btn-sm ${selectedStatus == '已发货' ? 'btn-success' : 'btn-default'}">已发货</a>
                        <a href="${pageContext.request.contextPath}/admin/order?status=已完成" class="btn btn-sm ${selectedStatus == '已完成' ? 'btn-primary' : 'btn-default'}">已完成</a>
                        <a href="${pageContext.request.contextPath}/admin/order?status=已取消" class="btn btn-sm ${selectedStatus == '已取消' ? 'btn-danger' : 'btn-default'}">已取消</a>
                    </div>
                    <%-- ==================== 订单数据表格 ==================== --%>
                    <table class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>订单号</th>
                                <th>用户名</th>
                                <th>订单日期</th>
                                <th>总金额</th>
                                <th>支付方式</th>
                                <th>物流单号</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${orderList}" var="order">
                                <tr>
                                    <%-- 订单号可点击跳转到详情页 --%>
                                    <td><a href="${pageContext.request.contextPath}/admin/order/detail?orderId=${order.orderid}">${order.orderid}</a></td>
                                    <td>${order.userid}</td>
                                    <td><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                    <td>
                                    <%-- 如果有优惠信息，先显示原价（删除线）和优惠金额，再显示实付金额 --%>
                                    <c:if test="${not empty order.originalprice && not empty order.discountamount}">
                                        <span style="text-decoration:line-through;color:#999;font-size:12px;">¥<fmt:formatNumber value="${order.originalprice}" pattern="#0.00"/></span><br/>
                                        <span style="color:#10b981;font-size:11px;">🎫-¥<fmt:formatNumber value="${order.discountamount}" pattern="#0.00"/></span>
                                    </c:if>
                                    <span class="text-danger">¥<fmt:formatNumber value="${order.totalprice}" pattern="#,##0.00"/></span>
                                </td>
                                    <td>
                                        <%-- 支付方式：将英文标识转为中文显示 --%>
                                        <c:choose>
                                            <c:when test="${order.cardtype == 'wechat'}">微信</c:when>
                                            <c:when test="${order.cardtype == 'alipay'}">支付宝</c:when>
                                            <c:when test="${order.cardtype == 'card'}">银行卡</c:when>
                                            <c:otherwise>${order.cardtype}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <%-- 物流单号，没有则显示横杠 --%>
                                    <td>${order.courier != null ? order.courier : '-'}</td>
                                    <td>
                                        <%-- 根据订单状态显示不同颜色的标签 --%>
                                        <c:choose>
                                            <c:when test="${order.status == '待支付'}">
                                                <span class="label label-warning">待支付</span>
                                            </c:when>
                                            <c:when test="${order.status == '已支付'}">
                                                <span class="label label-info">已支付</span>
                                            </c:when>
                                            <c:when test="${order.status == '已发货'}">
                                                <span class="label label-success">已发货</span>
                                            </c:when>
                                            <c:when test="${order.status == '已完成'}">
                                                <span class="label label-primary">已完成</span>
                                            </c:when>
                                            <c:when test="${order.status == '已取消'}">
                                                <span class="label label-danger">已取消</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="label label-default">${order.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="order-actions">
                                        <%-- 详情按钮 --%>
                                        <a href="${pageContext.request.contextPath}/admin/order/detail?orderId=${order.orderid}" class="btn btn-sm btn-info">详情</a>
                                        <%-- 修改状态下拉菜单（Bootstrap dropdown） --%>
                                        <div class="btn-group">
                                            <button class="btn btn-sm btn-default dropdown-toggle" data-toggle="dropdown">
                                                修改状态 <span class="caret"></span>
                                            </button>
                                            <ul class="dropdown-menu">
                                                <li><a href="javascript:void(0)" onclick="updateStatus('${order.orderid}', '已支付')">已支付</a></li>
                                                <li><a href="javascript:void(0)" onclick="updateStatus('${order.orderid}', '已发货')">已发货</a></li>
                                                <li><a href="javascript:void(0)" onclick="updateStatus('${order.orderid}', '已完成')">已完成</a></li>
                                                <li><a href="javascript:void(0)" onclick="updateStatus('${order.orderid}', '已取消')">已取消</a></li>
                                            </ul>
                                        </div>
                                        <%-- 已支付状态显示"发货"按钮 --%>
                                        <c:if test="${order.status == '已支付'}">
                                            <button class="btn btn-sm btn-success" onclick="shipOrder('${order.orderid}')">发货</button>
                                        </c:if>
                                        <%-- 待支付或已取消状态显示"删除"按钮 --%>
                                        <c:if test="${order.status == '待支付' || order.status == '已取消'}">
                                            <button class="btn btn-sm btn-danger" onclick="deleteOrder('${order.orderid}')">删除</button>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <%-- 订单列表为空时显示提示 --%>
                            <c:if test="${empty orderList}">
                                <tr><td colspan="8" class="text-center" style="color: var(--gray-500);">暂无订单数据</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                    
                    <%-- ==================== 分页控件 ==================== --%>
                    <%-- 仅在总页数大于1时显示分页 --%>
                    <c:if test="${pageInfo.pages > 1}">
                        <div class="pagination-container">
                            <%-- 分页信息：总记录数、当前页/总页数、每页条数 --%>
                            <div class="pagination-info">
                                共 ${pageInfo.total} 条记录，第 ${pageInfo.pageNum}/${pageInfo.pages} 页，
                                每页 ${pageInfo.pageSize} 条
                            </div>
                            <%-- 分页页码按钮 --%>
                            <ul class="pagination pagination-sm">
                                <%-- 上一页按钮，无上一页时禁用 --%>
                                <li class="${pageInfo.hasPreviousPage ? '' : 'disabled'}">
                                    <a href="${pageContext.request.contextPath}/admin/order?pageNum=${pageInfo.pageNum - 1}&pageSize=${pageInfo.pageSize}${selectedStatus != null ? '&status=' : ''}${selectedStatus}">&laquo;</a>
                                </li>
                                <%-- 页码数字：显示前2页、后2页和当前页，中间用省略号 --%>
                                <c:forEach var="i" begin="1" end="${pageInfo.pages}">
                                    <c:choose>
                                        <c:when test="${i == pageInfo.pageNum}">
                                            <li class="active"><a href="#">${i}</a></li>
                                        </c:when>
                                        <c:when test="${i <= 2 or i >= pageInfo.pages - 1 or i == pageInfo.pageNum}">
                                            <li><a href="${pageContext.request.contextPath}/admin/order?pageNum=${i}&pageSize=${pageInfo.pageSize}${selectedStatus != null ? '&status=' : ''}${selectedStatus}">${i}</a></li>
                                        </c:when>
                                        <c:when test="${i == 3 or i == pageInfo.pages - 2}">
                                            <li class="disabled"><a href="#">...</a></li>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>
                                <%-- 下一页按钮，无下一页时禁用 --%>
                                <li class="${pageInfo.hasNextPage ? '' : 'disabled'}">
                                    <a href="${pageContext.request.contextPath}/admin/order?pageNum=${pageInfo.pageNum + 1}&pageSize=${pageInfo.pageSize}${selectedStatus != null ? '&status=' : ''}${selectedStatus}">&raquo;</a>
                                </li>
                            </ul>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>
<%-- 引入 jQuery 和 Bootstrap JS --%>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script type="text/javascript">
    /**
     * 修改订单状态
     * @param orderId - 订单ID
     * @param status - 新状态值（如"已支付"、"已发货"等）
     */
    function updateStatus(orderId, status) {
        if (!confirm('确定要将订单状态修改为"' + status + '"吗？')) return;  // 二次确认
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/order/updateStatus',
            type: 'POST',
            data: { orderId: orderId, status: status },
            success: function(data) {
                if (data.success) {
                    location.reload();     // 成功后刷新页面
                } else {
                    alert(data.message);   // 失败则显示错误信息
                }
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }

    /**
     * 订单发货：先弹出输入框让管理员输入物流单号，然后更新状态
     * @param orderId - 订单ID
     */
    function shipOrder(orderId) {
        var courier = prompt('请输入物流单号：');   // 弹出输入框
        if (!courier) return;                       // 用户取消则不操作
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/order',
            type: 'POST',
            data: { orderId: orderId, courier: courier },
            success: function() {
                updateStatus(orderId, '已发货');   // 保存物流单号后，再更新状态为已发货
            }
        });
    }

    /**
     * 删除订单（仅待支付和已取消的订单可删除）
     * @param orderId - 订单ID
     */
    function deleteOrder(orderId) {
        if (!confirm('确定要删除该订单吗？此操作不可恢复！')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/order/delete',
            type: 'GET',
            data: { orderId: orderId },
            success: function(data) {
                if (data.success) {
                    alert('删除成功');
                    location.reload();
                } else {
                    alert(data.message);
                }
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }
</script>
</body>
</html>