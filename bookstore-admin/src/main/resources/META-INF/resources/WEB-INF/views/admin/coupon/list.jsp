<%-- ============================================================
     优惠券管理页面 (coupon/list.jsp)
     功能：管理所有优惠券的创建、编辑、发放和删除
     主要功能：
       - 顶部统计卡片（总优惠券数、有效优惠券、已发放数）
       - 优惠券列表表格（ID、名称、类型、门槛、优惠金额、使用率、有效期、状态）
       - 新增优惠券（弹出模态框填写表单）
       - 编辑优惠券（弹出模态框，回填已有数据）
       - 发放优惠券给指定用户（弹出模态框输入用户ID）
       - 删除优惠券（需二次确认）
       - 使用率进度条展示已用/总量比例
     使用了 Bootstrap 3 模态框 + 自定义统计卡片样式
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
        /* ==================== CSS变量定义 ==================== */
        :root {
            --primary: #4361ee;
            --success: #2ecc71;
            --warning: #f39c12;
            --danger: #e74c3c;
            --gray-500: #95a5a6;
        }
        body { background: #f0f2f5; }

        /* ==================== 页面容器和标题 ==================== */
        .page-wrapper { padding: 20px; }
        .section-title {
            font-size: 22px;
            font-weight: 700;
            color: #2c3e50;
            margin: 0 0 20px 0;
        }

        /* ==================== 统计卡片行 ==================== */
        .stat-mini-row {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            margin-bottom: 20px;
        }
        @media (max-width: 768px) { .stat-mini-row { grid-template-columns: 1fr; } }
        .stat-mini-card {
            background: #fff;
            border-radius: 14px;
            padding: 20px 24px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            display: flex;
            align-items: center;
            gap: 16px;
        }
        /* 统计图标（不同颜色的圆角方形） */
        .stat-mini-icon {
            width: 50px; height: 50px;
            border-radius: 12px;
            display: flex; align-items: center; justify-content: center;
            font-size: 24px;
        }
        .stat-mini-icon.coupon-total { background: rgba(67,97,238,0.12); color: var(--primary); }
        .stat-mini-icon.coupon-active { background: rgba(46,204,113,0.12); color: var(--success); }
        .stat-mini-icon.coupon-issued { background: rgba(243,156,18,0.12); color: var(--warning); }
        .stat-mini-info .stat-mini-value {
            font-size: 26px; font-weight: 700; color: #2c3e50; line-height: 1.2;
        }
        .stat-mini-info .stat-mini-label {
            font-size: 12px; color: var(--gray-500); text-transform: uppercase; letter-spacing: 0.5px;
        }

        /* ==================== 工具栏 ==================== */
        .toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }

        /* ==================== 表格卡片容器 ==================== */
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

        /* ==================== 优惠券类型标签 ==================== */
        .coupon-type-tag {
            display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 600;
        }
        .coupon-type-tag.reduction { background: #e3f2fd; color: var(--primary); }   /* 满减=蓝色 */
        .coupon-type-tag.discount { background: #fce4ec; color: #e91e63; }           /* 折扣=粉色 */

        /* ==================== 优惠券状态标签 ==================== */
        .coupon-status-tag {
            display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 600;
        }
        .coupon-status-tag.active { background: #e8f5e9; color: var(--success); }    /* 有效=绿色 */
        .coupon-status-tag.expired { background: #f5f5f5; color: var(--gray-500); }   /* 已过期=灰色 */
        .coupon-status-tag.disabled { background: #fdecea; color: var(--danger); }    /* 已禁用=红色 */

        /* ==================== 使用率进度条 ==================== */
        .usage-bar {
            height: 6px; border-radius: 3px; background: #eee; overflow: hidden; min-width: 60px; margin-top: 4px;
        }
        .usage-bar-fill {
            height: 100%; border-radius: 3px; background: var(--primary); transition: width 0.4s ease;
        }
        .usage-text { font-size: 11px; color: var(--gray-500); }

        /* ==================== 操作图标按钮组 ==================== */
        .action-icons { display: flex; gap: 6px; }

        /* ==================== 模态框自定义样式 ==================== */
        .modal-custom .modal-content { border-radius: 14px; }
        .modal-custom .modal-header {
            background: linear-gradient(135deg, var(--primary), #764ba2);
            color: #fff;
            border-radius: 14px 14px 0 0;
            border: none;
        }
        .modal-custom .modal-header .close { color: #fff; opacity: 0.8; }

        /* ==================== 表单样式 ==================== */
        .form-label { font-weight: 600; font-size: 13px; color: #555; margin-bottom: 4px; }
        .form-control {
            border-radius: 8px; border: 1px solid #ddd; padding: 8px 12px; font-size: 13px;
        }
        .form-control:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 2px rgba(67,97,238,0.15);
        }
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
            <h2 class="section-title">🎫 优惠券管理</h2>

            <%-- ==================== 统计卡片区域 ==================== --%>
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

            <%-- ==================== 工具栏（新增按钮） ==================== --%>
            <div class="toolbar">
                <div></div>
                <button class="btn btn-sm btn-primary" onclick="openAddModal()" style="border-radius:8px;font-weight:600;">
                    ➕ 新增优惠券
                </button>
            </div>

            <%-- ==================== 优惠券数据表格 ==================== --%>
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
                                    <%-- 优惠券类型：fixed=满减，percent=折扣 --%>
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
                                <%-- 使用门槛金额 --%>
                                <td>¥<fmt:formatNumber value="${coupon.threshold}" pattern="#,##0.00"/></td>
                                <td style="color:var(--danger);font-weight:700;">
                                    <%-- 折扣类型显示"x折"，满减类型显示"¥xxx" --%>
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
                                    <%-- 已使用量 / 总发放量 + 使用率进度条 --%>
                                    <span style="font-weight:600;color:var(--primary);">${coupon.usedCount}</span>
                                    /
                                    <span style="color:var(--gray-500);">${coupon.totalCount}</span>
                                    <div class="usage-bar">
                                        <%-- 计算使用率百分比 --%>
                                        <c:set var="usagePercent" value="0"/>
                                        <c:if test="${coupon.totalCount > 0}">
                                            <c:set var="usagePercent" value="${coupon.usedCount * 100 / coupon.totalCount}"/>
                                        </c:if>
                                        <div class="usage-bar-fill" style="width:${usagePercent}%;"></div>
                                    </div>
                                    <span class="usage-text">使用率 <fmt:formatNumber value="${usagePercent}" pattern="#.#"/>%</span>
                                </td>
                                <td>
                                    <%-- 有效期：短格式（月-日）+ 详细日期 --%>
                                    <div>
                                        <fmt:formatDate value="${coupon.startTime}" pattern="MM-dd"/> - <fmt:formatDate value="${coupon.endTime}" pattern="MM-dd"/>
                                    </div>
                                    <small style="color:var(--gray-500);">
                                        <fmt:formatDate value="${coupon.startTime}" pattern="yyyy-MM-dd"/>
                                    </small>
                                </td>
                                <td>
                                    <%-- 优惠券状态标签 --%>
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
                                    <%-- 操作按钮：编辑、发放、删除 --%>
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

<%-- ==================== 新增/编辑优惠券模态框 ==================== --%>
<div class="modal fade modal-custom" id="couponModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title" id="couponModalTitle">新增优惠券</h4>
            </div>
            <div class="modal-body">
                <form id="couponForm">
                    <%-- 隐藏字段：编辑时存储优惠券ID，新增时为空 --%>
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

<%-- ==================== 发放优惠券模态框 ==================== --%>
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

<%-- 引入 jQuery 和 Bootstrap JS --%>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script>
    /**
     * 打开新增优惠券模态框
     * 重置表单，设置标题为"新增"
     */
    function openAddModal() {
        $('#couponModalTitle').text('新增优惠券');
        $('#couponId').val('');               // 清空ID（表示新增）
        $('#couponForm')[0].reset();          // 重置所有表单字段
        $('#couponType').val('满减');
        $('#couponStatus').val('1');           // 默认启用
        $('#couponModal').modal('show');       // 显示模态框
    }

    /**
     * 打开编辑优惠券模态框
     * 先从后端获取优惠券数据，回填到表单中
     * @param id - 优惠券ID
     */
    function openEditModal(id) {
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/coupon/get',
            type: 'GET',
            data: { id: id },
            success: function(data) {
                if (data.coupon) {
                    var c = data.coupon;
                    $('#couponModalTitle').text('编辑优惠券');
                    // 回填所有表单字段
                    $('#couponId').val(c.id);
                    $('#couponName').val(c.name);
                    $('#couponType').val(c.type);
                    $('#couponDiscount').val(c.discount);
                    $('#couponThreshold').val(c.threshold);
                    $('#couponTotalCount').val(c.totalCount);
                    $('#couponStatus').val(c.status);
                    // 日期时间需要转换格式为 datetime-local 格式
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

    /**
     * 保存优惠券（新增或编辑）
     * 根据是否有ID来决定调用新增还是更新接口
     */
    function saveCoupon() {
        var formData = $('#couponForm').serialize();  // 序列化表单数据
        // 有ID则更新，无ID则新增
        var url = $('#couponId').val()
            ? '${pageContext.request.contextPath}/admin/coupon/update'
            : '${pageContext.request.contextPath}/admin/coupon/add';
        $.ajax({
            url: url,
            type: 'POST',
            data: formData,
            success: function(data) {
                if (data.success) {
                    $('#couponModal').modal('hide');   // 关闭模态框
                    location.reload();                 // 刷新页面
                } else {
                    alert(data.message || '操作失败');
                }
            },
            error: function() { alert('操作失败，请重试'); }
        });
    }

    /**
     * 打开发放优惠券模态框
     * @param id - 要发放的优惠券ID
     */
    function grantToUser(id) {
        $('#grantCouponId').val(id);          // 存储优惠券ID
        $('#grantUserId').val('');            // 清空用户ID输入
        $('#grantModal').modal('show');
    }

    /**
     * 确认发放优惠券给指定用户
     */
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

    /**
     * 删除优惠券
     * @param id - 优惠券ID
     */
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

    /**
     * 将时间戳转换为 datetime-local 输入框所需的格式
     * 格式：YYYY-MM-DDTHH:mm
     * @param timestamp - 时间戳（毫秒）
     * @returns {string} 格式化后的日期时间字符串
     */
    function formatDateTime(timestamp) {
        if (!timestamp) return '';
        var d = new Date(timestamp);
        var month = String(d.getMonth() + 1).padStart(2, '0');   // 月份补零
        var day = String(d.getDate()).padStart(2, '0');            // 日期补零
        var hours = String(d.getHours()).padStart(2, '0');         // 小时补零
        var minutes = String(d.getMinutes()).padStart(2, '0');     // 分钟补零
        return d.getFullYear() + '-' + month + '-' + day + 'T' + hours + ':' + minutes;
    }
</script>
</body>
</html>