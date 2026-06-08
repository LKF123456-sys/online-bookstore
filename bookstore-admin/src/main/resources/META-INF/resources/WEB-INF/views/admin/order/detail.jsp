<%-- ============================================================
     订单详情页面 (order/detail.jsp)
     功能：查看和管理单个订单的详细信息
     主要功能：
       - 顶部返回按钮和订单状态标签
       - 左侧：订单基本信息（订单号、用户、日期、金额、优惠券、支付方式、物流）
       - 右侧：收货地址信息（收货人、地址、城市、省份、邮编）
       - 订单商品明细表格（商品ID、名称、数量、单价、小计、用户评价）
       - 显示每个商品的评价内容（星级评分、评价文字、管理员回复）
       - 表尾显示优惠券减免和订单总额
       - 底部操作区：修改订单状态按钮 + 更新物流单号
     使用了 Bootstrap 3 面板 + 自定义样式
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>订单详情 - 后台管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        /* ==================== CSS变量定义 ==================== */
        :root {
            --primary: #4361ee;
            --primary-600: #3a56d4;
            --success: #2ecc71;
            --warning: #f39c12;
            --danger: #e74c3c;
            --gray-500: #95a5a6;
            --star-color: #f5a623;
        }

        body { background: #f0f2f5; }

        /* ==================== 详情区块面板 ==================== */
        .detail-section { margin-bottom: 20px; }
        /* 面板标题使用渐变背景 */
        .detail-section .panel-heading { background: linear-gradient(135deg, var(--primary), var(--primary-600)); color: #fff; }

        /* ==================== 信息表格 ==================== */
        /* 左列为标签（加粗灰底），右列为数据值 */
        .info-table td { padding: 8px 15px; }
        .info-table td:first-child { font-weight: bold; width: 120px; background: #f8f9fa; }

        /* ==================== 状态标签 ==================== */
        .status-badge { font-size: 16px; padding: 5px 15px; }

        /* ==================== 商品明细表格 ==================== */
        .items-table { margin-bottom: 0; }
        .items-table > thead > tr > th {
            background: #fafbfc;
            border-bottom: 2px solid #e8e8e8;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #7f8c8d;
            padding: 12px;
        }
        .items-table > tbody > tr > td {
            vertical-align: middle;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 13px;
        }
        .items-table > tbody > tr:last-child > td { border-bottom: none; }
        .items-table > tbody > tr:hover { background: #f8f9ff; }

        /* ==================== 评价星级样式 ==================== */
        .review-stars {
            display: inline-flex;
            gap: 1px;
            color: var(--star-color);
            font-size: 14px;
        }
        .review-stars .star-off { color: #ddd; }  /* 未点亮的星=灰色 */

        /* ==================== 评价内容卡片 ==================== */
        .review-box {
            background: #fdf6e3;       /* 浅黄色背景 */
            border-radius: 10px;
            padding: 12px 16px;
            margin-top: 8px;
            font-size: 13px;
            border-left: 3px solid var(--star-color);  /* 左侧金色边框 */
        }
        .review-box .review-user {
            font-weight: 600;
            color: #555;
            margin-bottom: 4px;
        }
        .review-box .review-text {
            color: #666;
            line-height: 1.5;
        }
        .review-box .review-time {
            font-size: 11px;
            color: var(--gray-500);
            margin-top: 4px;
        }

        /* ==================== 无评价提示 ==================== */
        .no-review {
            color: var(--gray-500);
            font-size: 12px;
            font-style: italic;
        }
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
            <%-- ==================== 顶部操作栏（返回按钮 + 状态标签） ==================== --%>
            <div class="row">
                <div class="col-md-12">
                    <div class="pull-left">
                        <a href="${pageContext.request.contextPath}/admin/order" class="btn btn-default">
                            <span class="glyphicon glyphicon-arrow-left"></span> 返回列表
                        </a>
                    </div>
                    <div class="pull-right">
                        <%-- 根据订单状态显示不同颜色的大号标签 --%>
                        <span class="status-badge">
                            <c:choose>
                                <c:when test="${order.status == '待支付'}">
                                    <span class="label label-warning label-lg">待支付</span>
                                </c:when>
                                <c:when test="${order.status == '已支付'}">
                                    <span class="label label-info label-lg">已支付</span>
                                </c:when>
                                <c:when test="${order.status == '已发货'}">
                                    <span class="label label-success label-lg">已发货</span>
                                </c:when>
                                <c:when test="${order.status == '已完成'}">
                                    <span class="label label-primary label-lg">已完成</span>
                                </c:when>
                                <c:when test="${order.status == '已取消'}">
                                    <span class="label label-danger label-lg">已取消</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="label label-default label-lg">${order.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="clearfix"></div>
                </div>
            </div>

            <%-- ==================== 订单信息 + 收货地址（两栏布局） ==================== --%>
            <div class="row" style="margin-top: 20px;">
                <%-- 左栏：订单基本信息 --%>
                <div class="col-md-6">
                    <div class="panel panel-default detail-section">
                        <div class="panel-heading">
                            <h4 class="panel-title">订单信息</h4>
                        </div>
                        <table class="table info-table">
                            <tr>
                                <td>订单号</td>
                                <td>${order.orderid}</td>
                            </tr>
                            <tr>
                                <td>用户名</td>
                                <td>${order.userid}</td>
                            </tr>
                            <tr>
                                <td>订单日期</td>
                                <td><fmt:formatDate value="${order.orderdate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            </tr>
                            <tr>
                                <td>订单金额</td>
                                <td class="text-danger" style="font-size: 18px; font-weight: bold;">
                                    <%-- 显示实付金额 --%>
                                    ¥<fmt:formatNumber value="${order.totalprice}" pattern="#,##0.00"/>
                                    <%-- 如果有优惠，显示原价（删除线）和优惠金额 --%>
                                    <c:if test="${not empty order.originalprice && not empty order.discountamount}">
                                        <br/><span style="font-size:12px;color:#999;text-decoration:line-through;">原价 ¥<fmt:formatNumber value="${order.originalprice}" pattern="#0.00"/></span>
                                        <span style="font-size:12px;color:#10b981;margin-left:6px;">优惠 -¥<fmt:formatNumber value="${order.discountamount}" pattern="#0.00"/></span>
                                    </c:if>
                                </td>
                            </tr>
                            <%-- 如果使用了优惠券，显示优惠券名称 --%>
                            <c:if test="${not empty order.couponname}">
                            <tr>
                                <td>优惠券</td>
                                <td><span style="color:#92400e;background:#fef3c7;padding:2px 10px;border-radius:10px;font-size:13px;">🎫 ${order.couponname}</span></td>
                            </tr>
                            </c:if>
                            <tr>
                                <td>支付方式</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${order.cardtype == 'wechat'}">微信支付</c:when>
                                        <c:when test="${order.cardtype == 'alipay'}">支付宝</c:when>
                                        <c:when test="${order.cardtype == 'card'}">银行卡</c:when>
                                        <c:otherwise>${order.cardtype}</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <tr>
                                <td>物流单号</td>
                                <td>${order.courier != null ? order.courier : '未发货'}</td>
                            </tr>
                        </table>
                    </div>
                </div>

                <%-- 右栏：收货地址信息 --%>
                <div class="col-md-6">
                    <div class="panel panel-default detail-section">
                        <div class="panel-heading">
                            <h4 class="panel-title">收货地址</h4>
                        </div>
                        <table class="table info-table">
                            <tr>
                                <td>收货人</td>
                                <td>${order.billtofirstname} ${order.billtolastname}</td>
                            </tr>
                            <tr>
                                <td>地址</td>
                                <td>${order.shipaddr1}</td>
                            </tr>
                            <tr>
                                <td>城市</td>
                                <td>${order.shipcity}</td>
                            </tr>
                            <tr>
                                <td>省份</td>
                                <td>${order.shipstate}</td>
                            </tr>
                            <tr>
                                <td>邮编</td>
                                <td>${order.shipzip}</td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>

            <%-- ==================== 订单商品明细 ==================== --%>
            <div class="row">
                <div class="col-md-12">
                    <div class="panel panel-default detail-section">
                        <div class="panel-heading">
                            <h4 class="panel-title">📦 订单商品明细</h4>
                        </div>
                        <div class="panel-body" style="padding: 0;">
                            <c:choose>
                                <c:when test="${not empty orderItems}">
                                    <table class="table table-bordered items-table">
                                        <thead>
                                            <tr>
                                                <th style="width:80px;">商品ID</th>
                                                <th>商品名称</th>
                                                <th style="width:80px;text-align:center;">数量</th>
                                                <th style="width:120px;text-align:right;">单价</th>
                                                <th style="width:120px;text-align:right;">小计</th>
                                                <th style="width:200px;">用户评价</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <%-- 遍历订单中的每个商品 --%>
                                            <c:forEach items="${orderItems}" var="item" varStatus="vs">
                                                <tr>
                                                    <td><code>${item.productId}</code></td>
                                                    <td><strong>${item.productName}</strong></td>
                                                    <td style="text-align:center;">${item.quantity}</td>
                                                    <td style="text-align:right;color:var(--danger);">
                                                        ¥<fmt:formatNumber value="${item.price}" pattern="#,##0.00"/>
                                                    </td>
                                                    <%-- 小计 = 单价 × 数量 --%>
                                                    <td style="text-align:right;font-weight:700;color:var(--danger);">
                                                        ¥<fmt:formatNumber value="${item.price * item.quantity}" pattern="#,##0.00"/>
                                                    </td>
                                                    <td>
                                                        <%-- 遍历评价列表，查找当前商品的评价 --%>
                                                        <c:set var="hasReview" value="false"/>
                                                        <c:if test="${not empty reviews}">
                                                            <c:forEach items="${reviews}" var="review">
                                                                <c:if test="${review.productId == item.productId}">
                                                                    <c:set var="hasReview" value="true"/>
                                                                    <div class="review-box">
                                                                        <div class="review-user">
                                                                            ${review.userId}
                                                                            <%-- 显示星级评分（实心★和空心★） --%>
                                                                            <span class="review-stars">
                                                                                <c:forEach begin="1" end="5" var="star">
                                                                                    <c:choose>
                                                                                        <c:when test="${star <= review.rating}">★</c:when>
                                                                                        <c:otherwise><span class="star-off">★</span></c:otherwise>
                                                                                    </c:choose>
                                                                                </c:forEach>
                                                                            </span>
                                                                        </div>
                                                                        <div class="review-text">${review.content}</div>
                                                                        <%-- 如果管理员有回复，显示回复内容 --%>
                                                                        <c:if test="${not empty review.reply}">
                                                                            <div style="color:var(--primary);margin-top:6px;font-size:12px;">
                                                                                ↳ 回复：${review.reply}
                                                                            </div>
                                                                        </c:if>
                                                                        <div class="review-time">
                                                                            <fmt:formatDate value="${review.createTime}" pattern="yyyy-MM-dd HH:mm"/>
                                                                        </div>
                                                                    </div>
                                                                </c:if>
                                                            </c:forEach>
                                                        </c:if>
                                                        <%-- 该商品没有评价时显示提示 --%>
                                                        <c:if test="${not hasReview}">
                                                            <span class="no-review">暂无评价</span>
                                                        </c:if>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                        <tfoot>
                                            <%-- 如果有优惠券减免，显示减免行 --%>
                                            <c:if test="${not empty order.originalprice && not empty order.discountamount}">
                                            <tr style="background:#f0fdf4;">
                                                <td colspan="4" style="text-align:right;font-weight:600;font-size:13px;color:#10b981;">
                                                    优惠券减免 (${order.couponname})：
                                                </td>
                                                <td style="text-align:right;font-weight:700;font-size:14px;color:#10b981;">
                                                    -¥<fmt:formatNumber value="${order.discountamount}" pattern="#,##0.00"/>
                                                </td>
                                                <td></td>
                                            </tr>
                                            </c:if>
                                            <%-- 订单总额行 --%>
                                            <tr style="background:#fafbfc;">
                                                <td colspan="4" style="text-align:right;font-weight:600;font-size:14px;">
                                                    订单总额：
                                                </td>
                                                <td style="text-align:right;font-weight:700;font-size:16px;color:var(--danger);">
                                                    ¥<fmt:formatNumber value="${order.totalprice}" pattern="#,##0.00"/>
                                                </td>
                                                <td></td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <div style="padding:20px;text-align:center;color:var(--gray-500);">
                                        暂无商品明细数据
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ==================== 修改订单状态操作区 ==================== --%>
            <div class="row">
                <div class="col-md-12">
                    <div class="panel panel-default detail-section">
                        <div class="panel-heading">
                            <h4 class="panel-title">修改订单状态</h4>
                        </div>
                        <div class="panel-body">
                            <%-- 状态修改按钮组 --%>
                            <div class="btn-group" role="group">
                                <button class="btn btn-info" onclick="updateStatus('已支付')">已支付</button>
                                <button class="btn btn-success" onclick="updateStatus('已发货')">已发货</button>
                                <button class="btn btn-primary" onclick="updateStatus('已完成')">已完成</button>
                                <button class="btn btn-danger" onclick="updateStatus('已取消')">已取消</button>
                            </div>
                            <%-- 物流单号输入和更新 --%>
                            <div class="form-inline" style="margin-top: 15px;">
                                <label>物流单号：</label>
                                <input type="text" id="courierInput" class="form-control" placeholder="请输入物流单号" value="${order.courier != null ? order.courier : ''}"/>
                                <button class="btn btn-warning" onclick="updateCourier()">更新物流</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<%-- 引入 jQuery 和 Bootstrap JS --%>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script type="text/javascript">
    // 当前订单ID，从后端数据中获取
    var orderId = '${order.orderid}';

    /**
     * 修改订单状态
     * @param status - 新状态值
     */
    function updateStatus(status) {
        if (!confirm('确定要将订单状态修改为"' + status + '"吗？')) return;  // 二次确认
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/order/updateStatus',
            type: 'POST',
            data: { orderId: orderId, status: status },
            success: function(data) {
                if (data.success) {
                    location.reload();     // 成功后刷新页面
                } else {
                    alert(data.message);
                }
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }

    /**
     * 更新物流单号
     * 从输入框读取物流单号，发送POST请求更新
     */
    function updateCourier() {
        var courier = $('#courierInput').val();    // 获取输入框中的物流单号
        if (!courier) { alert('请输入物流单号'); return; }  // 验证非空
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/order',
            type: 'POST',
            data: { orderId: orderId, courier: courier },
            success: function() {
                alert('物流单号更新成功');
                location.reload();     // 成功后刷新页面
            },
            error: function() { alert('更新失败，请重试'); }
        });
    }
</script>
</body>
</html>