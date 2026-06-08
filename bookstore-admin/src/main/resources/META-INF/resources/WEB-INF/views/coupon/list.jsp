<%-- ============================================================
     coupon/list.jsp — 前台优惠券列表页面
     功能：展示用户可领取或已拥有的优惠券列表。
     说明：从后端获取优惠券数据，支持领取和查看详情。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>优惠券管理 - BookVerse 管理后台</title>
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

        .stat-mini-row {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            margin-bottom: 20px;
        }

        @media (max-width: 768px) {
            .stat-mini-row { grid-template-columns: 1fr; }
        }

        .stat-mini-card {
            background: #fff;
            border-radius: 14px;
            padding: 20px 24px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .stat-mini-icon {
            width: 50px;
            height: 50px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
        }

        .stat-mini-icon.coupon-total { background: rgba(67,97,238,0.12); color: var(--primary); }
        .stat-mini-icon.coupon-active { background: rgba(46,204,113,0.12); color: var(--success); }
        .stat-mini-icon.coupon-issued { background: rgba(243,156,18,0.12); color: var(--warning); }

        .stat-mini-info .stat-mini-value {
            font-size: 26px;
            font-weight: 700;
            color: #2c3e50;
            line-height: 1.2;
        }

        .stat-mini-info .stat-mini-label {
            font-size: 12px;
            color: var(--gray-500);
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
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

        .coupon-type-tag {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
        }

        .coupon-type-tag.reduction { background: #e3f2fd; color: var(--primary); }
        .coupon-type-tag.discount { background: #fce4ec; color: #e91e63; }

        .coupon-status-tag {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
        }

        .coupon-status-tag.active { background: #e8f5e9; color: var(--success); }
        .coupon-status-tag.expired { background: #f5f5f5; color: var(--gray-500); }
        .coupon-status-tag.disabled { background: #fdecea; color: var(--danger); }

        .usage-bar {
            height: 6px;
            border-radius: 3px;
            background: #eee;
            overflow: hidden;
            min-width: 60px;
            margin-top: 4px;
        }

        .usage-bar-fill {
            height: 100%;
            border-radius: 3px;
            background: var(--primary);
            transition: width 0.4s ease;
        }

        .usage-text {
            font-size: 11px;
            color: var(--gray-500);
        }

        .action-icons {
            display: flex;
            gap: 6px;
        }

        .modal-custom .modal-content {
            border-radius: 14px;
        }

        .modal-custom .modal-header {
            background: linear-gradient(135deg, var(--primary), #764ba2);
            color: #fff;
            border-radius: 14px 14px 0 0;
            border: none;
        }

        .modal-custom .modal-header .close { color: #fff; opacity: 0.8; }

        .form-label {
            font-weight: 600;
            font-size: 13px;
            color: #555;
            margin-bottom: 4px;
        }

        .form-control {
            border-radius: 8px;
            border: 1px solid #ddd;
            padding: 8px 12px;
            font-size: 13px;
        }

        .form-control:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 2px rgba(67,97,238,0.15);
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/admin_header.jsp"/>
<div class="container-fluid">
    <div class="row">
        <jsp:include page="/WEB-INF/views/common/admin_sidebar.jsp"/>
        <div class="col-md-10 page-wrapper">
            <h2 class="section-title">🎫 优惠券管理</h2>

            <div class="stat-mini-row">
                <div class="stat-mini-card">
                    <div class="stat-mini-icon coupon-total">🎫</div>
                    <div class="stat-mini-info">
                        <div class="stat-mini-value">${totalCoupons}</div>
                        <div class="stat-mini-label">总优惠券数</div>
                    </div>
                </div>
                <div class="stat-mini-card">
                    <div class="stat-mini-icon coupon-active">✅</div>
                    <div class="stat-mini-info">
                        <div class="stat-mini-value">${activeCoupons}</div>
                        <div class="stat-mini-label">有效优惠券</div>
                    </div>
                </div>
                <div class="stat-mini-card">
                    <div class="stat-mini-icon coupon-issued">📤</div>
                    <div class="stat-mini-info">
                        <div class="stat-mini-value">${totalIssued}</div>
                        <div class="stat-mini-label">已发放数</div>
                    </div>
                </div>
            </div>

            <div class="toolbar">
                <div></div>
                <button class="btn btn-sm btn-primary" onclick="openAddModal()" style="border-radius:8px;font-weight:600;">
                    ➕ 新增优惠券
                </button>
            </div>

            <div class="table-card">
                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th style="width:50px;">ID</th>
                            <th>名称</th>
                            <th style="width:80px;">类型</th>
                            <th style="width:90px;">门槛</th>
                            <th style="width:90px;">优惠金额</th>
                            <th style="width:100px;">使用量/总量</th>
                            <th style="width:180px;">有效期</th>
                            <th style="width:80px;">状态</th>
                            <th style="width:170px;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${couponList}" var="coupon">
                            <tr>
                                <td><strong>#${coupon.id}</strong></td>
                                <td><strong>${coupon.name}</strong></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${coupon.type == 'fixed'}">
                                            <span class="coupon-type-tag reduction">满减</span>
                                        </c:when>
                                        <c:when test="${coupon.type == 'percent'}">
                                            <span class="coupon-type-tag discount">折扣</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="coupon-type-tag reduction">${coupon.type}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>¥<fmt:formatNumber value="${coupon.threshold}" pattern="#,##0.00"/></td>
                                <td style="color:var(--danger);font-weight:700;">
                                    <c:choose>
                                        <c:when test="${coupon.type == 'percent'}">
                                            <fmt:formatNumber value="${coupon.discount}" pattern="#.#"/>折
                                        </c:when>
                                        <c:otherwise>
                                            ¥<fmt:formatNumber value="${coupon.discount}" pattern="#,##0.00"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span style="font-weight:600;color:var(--primary);">${coupon.usedCount}</span>
                                    /
                                    <span style="color:var(--gray-500);">${coupon.totalCount}</span>
                                    <div class="usage-bar">
                                        <c:set var="usagePercent" value="0"/>
                                        <c:if test="${coupon.totalCount > 0}">
                                            <c:set var="usagePercent" value="${coupon.usedCount * 100 / coupon.totalCount}"/>
                                        </c:if>
                                        <div class="usage-bar-fill" style="width:${usagePercent}%;"></div>
                                    </div>
                                    <span class="usage-text">使用率 <fmt:formatNumber value="${usagePercent}" pattern="#.#"/>%</span>
                                </td>
                                <td>
                                    <div>
                                        <fmt:formatDate value="${coupon.startTime}" pattern="MM-dd"/>

                                        <fmt:formatDate value="${coupon.endTime}" pattern="MM-dd"/>
                                    </div>
                                    <small style="color:var(--gray-500);">
                                        <fmt:formatDate value="${coupon.startTime}" pattern="yyyy-MM-dd"/>
                                    </small>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${coupon.status == 1}">
                                            <span class="coupon-status-tag active">有效</span>
                                        </c:when>
                                        <c:when test="${coupon.status == 0}">
                                            <span class="coupon-status-tag disabled">已禁用</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="coupon-status-tag expired">已过期</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="action-icons">
                                        <button class="btn btn-xs btn-info" onclick="openEditModal(${coupon.id})">编辑</button>
                                        <button class="btn btn-xs btn-warning" onclick="grantToUser(${coupon.id})">发放</button>
                                        <button class="btn btn-xs btn-danger" onclick="deleteCoupon(${coupon.id})">删除</button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty couponList}">
                            <tr>
                                <td colspan="9" class="text-center" style="padding:40px;color:var(--gray-500);">
                                    暂无优惠券数据
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div class="modal fade modal-custom" id="couponModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title" id="couponModalTitle">新增优惠券</h4>
            </div>
            <div class="modal-body">
                <form id="couponForm">
                    <input type="hidden" id="couponId" name="id" value="">
                    <div class="form-group">
                        <label class="form-label">优惠券名称</label>
                        <input type="text" class="form-control" id="couponName" name="name" placeholder="例如：满100减20" required>
                    </div>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="form-label">类型</label>
                                <select class="form-control" id="couponType" name="type" required>
                                    <option value="fixed">满减</option>
                                    <option value="percent">折扣</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="form-label">优惠金额/折扣</label>
                                <input type="number" class="form-control" id="couponDiscount" name="discount"
                                       step="0.01" min="0" placeholder="金额或折扣值" required>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="form-label">使用门槛（元）</label>
                                <input type="number" class="form-control" id="couponThreshold" name="threshold"
                                       step="0.01" min="0" placeholder="0表示无门槛" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="form-label">发放总量</label>
                                <input type="number" class="form-control" id="couponTotalCount" name="totalCount"
                                       min="1" placeholder="可领取数量" required>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="form-label">开始时间</label>
                                <input type="datetime-local" class="form-control" id="couponStartTime" name="startTime" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="form-label">结束时间</label>
                                <input type="datetime-local" class="form-control" id="couponEndTime" name="endTime" required>
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">状态</label>
                        <select class="form-control" id="couponStatus" name="status">
                            <option value="1">启用</option>
                            <option value="0">禁用</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" onclick="saveCoupon()">保存</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade modal-custom" id="grantModal" tabindex="-1">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">发放优惠券</h4>
            </div>
            <div class="modal-body">
                <input type="hidden" id="grantCouponId" value="">
                <div class="form-group">
                    <label class="form-label">用户ID</label>
                    <input type="text" class="form-control" id="grantUserId" placeholder="请输入用户ID" required>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-success" onclick="confirmGrant()">确认发放</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script>
    function openAddModal() {
        $('#couponModalTitle').text('新增优惠券');
        $('#couponId').val('');
        $('#couponForm')[0].reset();
        $('#couponType').val('满减');
        $('#couponStatus').val('1');
        $('#couponModal').modal('show');
    }

    function openEditModal(id) {
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/coupon/get',
            type: 'GET',
            data: { id: id },
            success: function(data) {
                if (data.coupon) {
                    var c = data.coupon;
                    $('#couponModalTitle').text('编辑优惠券');
                    $('#couponId').val(c.id);
                    $('#couponName').val(c.name);
                    $('#couponType').val(c.type);
                    $('#couponDiscount').val(c.discount);
                    $('#couponThreshold').val(c.threshold);
                    $('#couponTotalCount').val(c.totalCount);
                    $('#couponStatus').val(c.status);
                    if (c.startTime) {
                        $('#couponStartTime').val(formatDateTime(c.startTime));
                    }
                    if (c.endTime) {
                        $('#couponEndTime').val(formatDateTime(c.endTime));
                    }
                    $('#couponModal').modal('show');
                } else {
                    alert('获取优惠券信息失败');
                }
            },
            error: function() { alert('请求失败，请重试'); }
        });
    }

    function saveCoupon() {
        var formData = $('#couponForm').serialize();
        var url = $('#couponId').val()
            ? '${pageContext.request.contextPath}/admin/coupon/update'
            : '${pageContext.request.contextPath}/admin/coupon/add';
        $.ajax({
            url: url,
            type: 'POST',
            data: formData,
            success: function(data) {
                if (data.success) {
                    $('#couponModal').modal('hide');
                    location.reload();
                } else {
                    alert(data.message || '操作失败');
                }
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }

    function grantToUser(id) {
        $('#grantCouponId').val(id);
        $('#grantUserId').val('');
        $('#grantModal').modal('show');
    }

    function confirmGrant() {
        var couponId = $('#grantCouponId').val();
        var userId = $('#grantUserId').val().trim();
        if (!userId) {
            alert('请输入用户ID');
            return;
        }
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/coupon/grant',
            type: 'POST',
            data: { couponId: couponId, userId: userId },
            success: function(data) {
                if (data.success) {
                    $('#grantModal').modal('hide');
                    alert('发放成功');
                    location.reload();
                } else {
                    alert(data.message || '发放失败');
                }
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }

    function deleteCoupon(id) {
        if (!confirm('确定要删除该优惠券吗？此操作不可恢复！')) return;
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/coupon/delete',
            type: 'GET',
            data: { id: id },
            success: function(data) {
                if (data.success) {
                    location.reload();
                } else {
                    alert(data.message || '删除失败');
                }
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }

    function formatDateTime(timestamp) {
        if (!timestamp) return '';
        var d = new Date(timestamp);
        var month = String(d.getMonth() + 1).padStart(2, '0');
        var day = String(d.getDate()).padStart(2, '0');
        var hours = String(d.getHours()).padStart(2, '0');
        var minutes = String(d.getMinutes()).padStart(2, '0');
        return d.getFullYear() + '-' + month + '-' + day + 'T' + hours + ':' + minutes;
    }
</script>
</body>
</html>
