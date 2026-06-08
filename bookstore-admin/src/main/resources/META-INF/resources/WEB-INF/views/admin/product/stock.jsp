<%-- ============================================================
     库存管理页面 (stock.jsp)
     功能：查看和管理所有商品的库存信息
     主要功能：
       - 左侧低库存预警面板：红色高亮库存不足的商品（<10本）
       - 右侧库存表格：展示所有商品的库存、销量、库存状态条
       - 库存进度条三色分级：绿色(充足>100)、橙色(适中10-100)、红色(紧张<10)
       - 点击库存数字可直接编辑（行内编辑，按Enter确认，按Esc取消）
       - 搜索商品（按书名或ID）
       - 分页导航
       - 与热销排行页面通过标签导航互相切换
     使用了 Bootstrap 3 + CSS Grid 两栏布局
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>库存管理 - BookVerse 管理后台</title>
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

        /* ==================== 两栏布局（左侧预警+右侧表格） ==================== */
        .content-grid {
            display: grid;
            grid-template-columns: 320px 1fr;  /* 左320px，右侧自适应 */
            gap: 18px;
            align-items: start;
        }
        @media (max-width: 992px) {
            .content-grid { grid-template-columns: 1fr; }  /* 小屏幕改为单列 */
        }

        /* ==================== 面板卡片 ==================== */
        .panel-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            overflow: hidden;
        }
        .panel-card .panel-head {
            padding: 16px 20px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 15px;
            font-weight: 600;
            color: #2c3e50;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        /* 低库存预警面板标题：红色左边框 */
        .panel-card .panel-head.danger-head {
            background: linear-gradient(135deg, #fff5f5, #fff);
            border-left: 4px solid var(--danger);
        }
        .panel-card .panel-body { padding: 12px 20px; }

        /* ==================== 低库存预警列表项 ==================== */
        .alert-inline-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10px 12px;
            margin-bottom: 6px;
            border-radius: 10px;
            background: #fdf2f2;
            border: 1px solid #fce4e4;
            transition: all 0.2s;
        }
        .alert-inline-item:hover { background: #fce8e8; border-color: #f5c6c6; }
        .alert-inline-item:last-child { margin-bottom: 0; }
        .alert-book-name {
            font-size: 13px;
            font-weight: 600;
            color: #2c3e50;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        /* 库存数字红色大号显示 */
        .alert-stock-num {
            font-size: 18px;
            font-weight: 700;
            color: var(--danger);
            padding: 2px 10px;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
            min-width: 36px;
            text-align: center;
        }

        /* ==================== 标签导航（热销排行/库存管理） ==================== */
        .tabs-nav {
            display: flex;
            gap: 5px;
            margin-bottom: 20px;
            background: #fff;
            padding: 5px;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
        }
        .tabs-nav a {
            padding: 8px 20px;
            border-radius: 8px;
            text-decoration: none;
            font-size: 13px;
            font-weight: 600;
            color: #7f8c8d;
            transition: all 0.3s;
        }
        .tabs-nav a.active { background: var(--primary); color: #fff; }
        .tabs-nav a:hover:not(.active) { background: #f0f0f0; color: #2c3e50; }

        /* ==================== 库存进度条 ==================== */
        .stock-bar {
            height: 8px;
            border-radius: 4px;
            background: #eee;
            overflow: hidden;
            min-width: 80px;
        }
        .stock-bar-fill {
            height: 100%;
            border-radius: 4px;
            transition: width 0.5s ease;
        }
        /* 三级颜色：充足=绿色、适中=橙色、紧张=红色 */
        .stock-bar-fill.high { background: var(--success); }
        .stock-bar-fill.medium { background: var(--warning); }
        .stock-bar-fill.low { background: var(--danger); }

        /* ==================== 可编辑库存数字 ==================== */
        /* 鼠标悬停时显示编辑图标，点击可进入编辑模式 */
        .stock-editable {
            cursor: pointer;
            padding: 3px 8px;
            border-radius: 6px;
            transition: all 0.2s;
            font-weight: 600;
            position: relative;
        }
        .stock-editable:hover { background: #eef0ff; color: var(--primary); }
        /* 悬停时显示编辑铅笔图标 */
        .stock-editable::after {
            content: ' ✎';
            font-size: 10px;
            color: var(--gray-500);
            opacity: 0;
            transition: opacity 0.2s;
        }
        .stock-editable:hover::after { opacity: 1; }

        /* 编辑模式下的输入框样式 */
        .stock-edit-input {
            width: 70px;
            height: 28px;
            padding: 2px 6px;
            border: 2px solid var(--primary);
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            text-align: center;
            outline: none;
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
        }
        .table-card > table > tbody > tr:hover { background: #f8f9ff; }
        /* 低库存行的特殊背景色 */
        .table-card > table > tbody > tr.row-danger { background: #fffafa; }
        .table-card > table > tbody > tr.row-danger:hover { background: #fff0f0; }

        /* ==================== 库存文字颜色 ==================== */
        .stock-text { font-weight: 600; }
        .stock-text.danger-text { color: var(--danger); }    /* 紧张=红色 */
        .stock-text.warning-text { color: var(--warning); }   /* 适中=橙色 */
        .stock-text.success-text { color: var(--success); }   /* 充足=绿色 */

        /* ==================== 搜索栏 ==================== */
        .search-box-row {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
            align-items: center;
        }
        .search-box-row input { width: 250px; border-radius: 8px; }
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
            <h2 class="section-title">📦 库存管理</h2>

            <%-- ==================== 标签导航（热销排行/库存管理） ==================== --%>
            <div class="tabs-nav">
                <a href="${pageContext.request.contextPath}/admin/product/bestseller">销量排行 TOP50</a>
                <a href="${pageContext.request.contextPath}/admin/product/stock" class="active">库存管理</a>
            </div>

            <%-- ==================== 两栏内容区域 ==================== --%>
            <div class="content-grid">
                <%-- 左栏：低库存预警面板 --%>
                <div>
                    <div class="panel-card">
                        <div class="panel-head danger-head">
                            ⚠️ 低库存预警
                            <span style="margin-left:auto;font-size:12px;color:var(--gray-500);">
                                ${lowStockCount} 个商品
                            </span>
                        </div>
                        <div class="panel-body">
                            <c:choose>
                                <c:when test="${not empty lowStockList}">
                                    <%-- 遍历低库存商品列表 --%>
                                    <c:forEach items="${lowStockList}" var="item">
                                        <div class="alert-inline-item">
                                            <span class="alert-book-name" title="${item.name}">${item.name}</span>
                                            <span class="alert-stock-num">${item.stock}</span>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <%-- 所有商品库存充足时的提示 --%>
                                    <div style="text-align:center;padding:30px;color:var(--success);">
                                        ✅ 所有商品库存充足
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <%-- 右栏：搜索 + 库存表格 + 分页 --%>
                <div>
                    <%-- ==================== 搜索栏 ==================== --%>
                    <div class="search-box-row">
                        <form action="${pageContext.request.contextPath}/admin/product/stock" method="get" class="form-inline">
                            <input type="text" name="keyword" class="form-control input-sm"
                                   placeholder="搜索书名或ID..." value="${keyword}">
                            <button type="submit" class="btn btn-sm btn-primary">搜索</button>
                            <c:if test="${not empty keyword}">
                                <a href="${pageContext.request.contextPath}/admin/product/stock" class="btn btn-sm btn-default">清除</a>
                            </c:if>
                        </form>
                    </div>

                    <%-- ==================== 库存数据表格 ==================== --%>
                    <div class="table-card">
                        <table class="table table-bordered">
                            <thead>
                                <tr>
                                    <th style="width:80px;">商品ID</th>
                                    <th>书名</th>
                                    <th style="width:100px;">当前库存</th>
                                    <th style="width:90px;">销量</th>
                                    <th style="width:130px;">库存状态</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${stockList}" var="book">
                                    <%-- 根据库存数量判断等级：low(<10)、medium(10-100)、high(>100) --%>
                                    <c:set var="stockLevel" value=""/>
                                    <c:choose>
                                        <c:when test="${book.stock < 10}">
                                            <c:set var="stockLevel" value="low"/>
                                        </c:when>
                                        <c:when test="${book.stock <= 100}">
                                            <c:set var="stockLevel" value="medium"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="stockLevel" value="high"/>
                                        </c:otherwise>
                                    </c:choose>

                                    <%-- 计算进度条百分比（以200为满库存基准） --%>
                                    <c:set var="stockPercent" value="100"/>
                                    <c:choose>
                                        <c:when test="${book.stock >= 200}">
                                            <c:set var="stockPercent" value="100"/>
                                        </c:when>
                                        <c:when test="${book.stock >= 0}">
                                            <c:set var="stockPercent" value="${book.stock * 100 / 200}"/>
                                        </c:when>
                                    </c:choose>

                                    <%-- 低库存行添加 row-danger 背景色 --%>
                                    <tr class="${stockLevel == 'low' ? 'row-danger' : ''}">
                                        <td><code>${book.productid}</code></td>
                                        <td><strong>${book.name}</strong></td>
                                        <td style="text-align:center;">
                                            <%-- 可点击编辑的库存数字，data属性存储商品ID和当前库存 --%>
                                            <span class="stock-editable stock-text ${stockLevel == 'low' ? 'danger-text' : (stockLevel == 'medium' ? 'warning-text' : 'success-text')}"
                                                  data-productid="${book.productid}"
                                                  data-stock="${book.stock}"
                                                  onclick="editStock(this)">
                                                ${book.stock}
                                            </span>
                                        </td>
                                        <td style="text-align:center;">
                                            <span style="color:var(--primary);font-weight:600;">${book.sales}</span>
                                        </td>
                                        <td>
                                            <%-- 库存进度条：根据等级填充不同颜色 --%>
                                            <div class="stock-bar">
                                                <div class="stock-bar-fill ${stockLevel}"
                                                     style="width: ${stockPercent}%;"></div>
                                            </div>
                                            <%-- 库存状态文字说明 --%>
                                            <span style="font-size:11px;color:var(--gray-500);margin-top:3px;display:block;">
                                                <c:choose>
                                                    <c:when test="${stockLevel == 'low'}">库存紧张</c:when>
                                                    <c:when test="${stockLevel == 'medium'}">库存适中</c:when>
                                                    <c:otherwise>库存充足</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty stockList}">
                                    <tr>
                                        <td colspan="5" class="text-center" style="padding:40px;color:var(--gray-500);">
                                            暂无商品数据
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <%-- ==================== 分页控件 ==================== --%>
                    <c:if test="${pageInfo != null && pageInfo.pages > 1}">
                        <div style="display:flex;justify-content:space-between;align-items:center;margin-top:15px;">
                            <div style="color:var(--gray-500);font-size:13px;">
                                共 ${pageInfo.total} 条，第 ${pageInfo.pageNum}/${pageInfo.pages} 页
                            </div>
                            <ul class="pagination pagination-sm" style="margin:0;">
                                <li class="${pageInfo.hasPreviousPage ? '' : 'disabled'}">
                                    <a href="${pageContext.request.contextPath}/admin/product/stock?pageNum=${pageInfo.pageNum - 1}&pageSize=${pageInfo.pageSize}${not empty keyword ? '&keyword=' : ''}${keyword}">&laquo;</a>
                                </li>
                                <c:forEach var="i" begin="1" end="${pageInfo.pages}">
                                    <c:choose>
                                        <c:when test="${i == pageInfo.pageNum}">
                                            <li class="active"><a href="#">${i}</a></li>
                                        </c:when>
                                        <c:when test="${i <= 2 or i >= pageInfo.pages - 1 or i == pageInfo.pageNum}">
                                            <li><a href="${pageContext.request.contextPath}/admin/product/stock?pageNum=${i}&pageSize=${pageInfo.pageSize}${not empty keyword ? '&keyword=' : ''}${keyword}">${i}</a></li>
                                        </c:when>
                                        <c:when test="${i == 3 or i == pageInfo.pages - 2}">
                                            <li class="disabled"><a href="#">...</a></li>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>
                                <li class="${pageInfo.hasNextPage ? '' : 'disabled'}">
                                    <a href="${pageContext.request.contextPath}/admin/product/stock?pageNum=${pageInfo.pageNum + 1}&pageSize=${pageInfo.pageSize}${not empty keyword ? '&keyword=' : ''}${keyword}">&raquo;</a>
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
<script>
    /**
     * 行内编辑库存
     * 将库存数字替换为输入框，修改后提交到后端
     * @param el - 被点击的库存数字元素
     */
    function editStock(el) {
        var $el = $(el);
        var productid = $el.data('productid');     // 从data属性获取商品ID
        var currentStock = $el.data('stock');       // 从data属性获取当前库存
        // 创建数字输入框，设置最小值为0
        var $input = $('<input type="number" class="stock-edit-input" min="0" value="' + currentStock + '">');

        // 用输入框替换原来的数字显示
        $el.replaceWith($input);
        $input.focus().select();                    // 自动聚焦并选中内容

        // 监听失焦和键盘事件
        $input.on('blur keydown', function(e) {
            // 失焦或按Enter键时提交修改
            if (e.type === 'blur' || e.which === 13) {
                var newStock = parseInt($input.val());
                // 验证输入值：必须是非负整数
                if (isNaN(newStock) || newStock < 0) {
                    $input.replaceWith($el);        // 无效输入则恢复原样
                    return;
                }
                // 值未改变则不提交
                if (newStock === currentStock) {
                    $input.replaceWith($el);
                    return;
                }

                var $p = $input;
                // 发送AJAX请求更新库存
                $.ajax({
                    url: '${pageContext.request.contextPath}/admin/product/stock/update',
                    type: 'POST',
                    data: { productid: productid, stock: newStock },
                    success: function(data) {
                        if (data.success) {
                            location.reload();      // 成功后刷新页面
                        } else {
                            alert(data.message || '更新失败');
                            location.reload();
                        }
                    },
                    error: function() {
                        alert('操作失败，请重试');
                        location.reload();
                    }
                });
            }
            // 按Esc键取消编辑，恢复原样
            if (e.which === 27) {
                $input.replaceWith($el);
            }
        });
    }
</script>
</body>
</html>