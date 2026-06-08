<%-- ============================================================
     热销排行页面 (bestseller.jsp)
     功能：展示商品销量排行榜，支持推荐开关
     主要功能：
       - 以表格形式展示按销量排序的TOP50商品
       - 前三名显示金银铜奖牌（👑🥈🥉），其余显示数字排名
       - 每行显示：排名、封面、书名、分类、价格、销量、推荐状态
       - 推荐开关（Toggle Switch）：点击切换商品的推荐状态（AJAX无刷新）
       - 与库存管理页面通过顶部标签导航互相切换
     使用了 Bootstrap 3 + 自定义表格和开关样式
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>热销排行榜 - BookVerse 管理后台</title>
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

        /* ==================== 排名奖牌样式 ==================== */
        /* 金银铜奖牌：渐变背景 + 发光阴影 */
        .rank-medal {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 34px;
            height: 34px;
            border-radius: 50%;
            font-weight: 700;
            font-size: 14px;
            color: #fff;
            flex-shrink: 0;
        }
        /* 第1名=金色渐变 */
        .rank-medal.gold {
            background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
            box-shadow: 0 3px 10px rgba(246,211,101,0.4);
        }
        /* 第2名=银蓝渐变 */
        .rank-medal.silver {
            background: linear-gradient(135deg, #a8c0ff 0%, #8f94fb 100%);
            box-shadow: 0 3px 10px rgba(143,148,251,0.4);
        }
        /* 第3名=铜橙渐变 */
        .rank-medal.bronze {
            background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
            box-shadow: 0 3px 10px rgba(241,39,17,0.4);
        }
        /* 第4名之后=灰色普通样式 */
        .rank-medal.normal {
            background: var(--gray-500);
            font-size: 12px;
        }

        /* ==================== 图书封面缩略图 ==================== */
        .book-cover {
            width: 50px;
            height: 68px;
            object-fit: cover;
            border-radius: 6px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        /* ==================== 书名单元格（超长截断） ==================== */
        .book-name-cell {
            font-weight: 600;
            color: #2c3e50;
            max-width: 200px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        /* ==================== 销量数字 ==================== */
        .sales-number {
            font-weight: 700;
            font-size: 15px;
            color: var(--primary);
        }

        /* ==================== 推荐开关（Toggle Switch） ==================== */
        /* CSS实现的滑动开关，无需图片 */
        .toggle-switch {
            position: relative;
            display: inline-block;
            width: 44px;
            height: 24px;
        }
        /* 隐藏原生checkbox */
        .toggle-switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }
        /* 开关滑轨 */
        .toggle-slider {
            position: absolute;
            cursor: pointer;
            top: 0; left: 0; right: 0; bottom: 0;
            background-color: #ccc;        /* 未选中=灰色 */
            transition: 0.3s;
            border-radius: 24px;
        }
        /* 开关圆形滑块 */
        .toggle-slider:before {
            position: absolute;
            content: "";
            height: 18px;
            width: 18px;
            left: 3px;
            bottom: 3px;
            background-color: #fff;
            transition: 0.3s;
            border-radius: 50%;
        }
        /* 选中时：滑轨变为渐变色 */
        .toggle-switch input:checked + .toggle-slider {
            background: linear-gradient(135deg, var(--primary), #764ba2);
        }
        /* 选中时：滑块向右移动 */
        .toggle-switch input:checked + .toggle-slider:before {
            transform: translateX(20px);
        }

        /* ==================== 推荐状态标签 ==================== */
        .recommend-badge {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 600;
        }
        .recommend-badge.on { background: #e8f5e9; color: var(--success); }   /* 已推荐=绿色 */
        .recommend-badge.off { background: #f5f5f5; color: var(--gray-500); } /* 未推荐=灰色 */

        /* ==================== 自定义表格样式 ==================== */
        .table-custom {
            background: #fff;
            border-radius: 14px;
            overflow: hidden;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
        }
        .table-custom > table { margin-bottom: 0; }
        .table-custom > table > thead > tr > th {
            background: #fafbfc;
            border-bottom: 2px solid #e8e8e8;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #7f8c8d;
            padding: 14px 12px;
            font-weight: 600;
        }
        .table-custom > table > tbody > tr > td {
            vertical-align: middle;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
        }
        .table-custom > table > tbody > tr:hover { background: #f8f9ff; }
        /* 前3名行的特殊背景（浅金色渐变） */
        .table-custom > table > tbody > tr.top-row {
            background: linear-gradient(90deg, #fffdf5, #ffffff);
        }
        .table-custom > table > tbody > tr.top-row:hover {
            background: linear-gradient(90deg, #fff9e0, #fdf8ff);
        }

        /* ==================== 顶部标签导航 ==================== */
        /* 在热销排行和库存管理之间切换的标签栏 */
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
        /* 当前选中的标签 */
        .tabs-nav a.active {
            background: var(--primary);
            color: #fff;
        }
        .tabs-nav a:hover:not(.active) {
            background: #f0f0f0;
            color: #2c3e50;
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
            <h2 class="section-title">🔥 热销排行榜</h2>

            <%-- ==================== 标签导航（热销排行/库存管理） ==================== --%>
            <div class="tabs-nav">
                <a href="${pageContext.request.contextPath}/admin/product/bestseller" class="active">销量排行 TOP50</a>
                <a href="${pageContext.request.contextPath}/admin/product/stock">库存管理</a>
            </div>

            <%-- ==================== 热销排行数据表格 ==================== --%>
            <div class="table-custom">
                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th style="width:60px;">排名</th>
                            <th style="width:70px;">封面</th>
                            <th>书名</th>
                            <th style="width:100px;">分类</th>
                            <th style="width:100px;">价格</th>
                            <th style="width:90px;">销量</th>
                            <th style="width:100px;">推荐状态</th>
                            <th style="width:90px;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%-- 使用 varStatus 获取循环索引，用于判断排名 --%>
                        <c:forEach items="${bestsellers}" var="book" varStatus="vs">
                            <%-- 前3名添加 top-row 高亮行样式 --%>
                            <tr class="${vs.index < 3 ? 'top-row' : ''}">
                                <td style="text-align:center;">
                                    <%-- 根据排名显示不同奖牌：第1名👑、第2名🥈、第3名🥉、其余显示数字 --%>
                                    <c:choose>
                                        <c:when test="${vs.index == 0}">
                                            <span class="rank-medal gold">👑</span>
                                        </c:when>
                                        <c:when test="${vs.index == 1}">
                                            <span class="rank-medal silver">🥈</span>
                                        </c:when>
                                        <c:when test="${vs.index == 2}">
                                            <span class="rank-medal bronze">🥉</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rank-medal normal">${vs.index + 1}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align:center;">
                                    <%-- 图书封面图片，加载失败时显示默认图 --%>
                                    <img class="book-cover"
                                         src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg"
                                         alt="${book.name}"
                                         onerror="this.src='${pageContext.request.contextPath}/img/default-book.svg'">
                                </td>
                                <td>
                                    <%-- 书名超长截断，鼠标悬停显示完整名称 --%>
                                    <span class="book-name-cell" title="${book.name}">${book.name}</span>
                                </td>
                                <td><span class="label label-default">${book.category}</span></td>
                                <td style="color:var(--danger);font-weight:700;">
                                    ¥<fmt:formatNumber value="${book.price}" pattern="#,##0.00"/>
                                </td>
                                <td>
                                    <span class="sales-number">${book.sales}</span>
                                    <span style="font-size:11px;color:var(--gray-500);">本</span>
                                </td>
                                <td>
                                    <%-- 推荐状态标签 --%>
                                    <c:choose>
                                        <c:when test="${book.isRecommend == 1}">
                                            <span class="recommend-badge on">已推荐</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="recommend-badge off">未推荐</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align:center;">
                                    <%-- 推荐开关：CSS实现的滑动开关，onchange 触发AJAX切换 --%>
                                    <label class="toggle-switch" title="${book.isRecommend == 1 ? '取消推荐' : '设为推荐'}">
                                        <input type="checkbox"
                                               ${book.isRecommend == 1 ? 'checked' : ''}
                                               onchange="toggleRecommend('${book.productid}', this.checked)">
                                        <span class="toggle-slider"></span>
                                    </label>
                                </td>
                            </tr>
                        </c:forEach>
                        <%-- 列表为空时显示提示 --%>
                        <c:if test="${empty bestsellers}">
                            <tr>
                                <td colspan="8" class="text-center" style="padding:40px;color:var(--gray-500);">
                                    暂无销售数据
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<%-- 引入 jQuery 和 Bootstrap JS --%>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script src="https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script>
    /**
     * 切换商品推荐状态
     * @param productid - 商品ID
     * @param checked - 开关状态：true=推荐，false=取消推荐
     */
    function toggleRecommend(productid, checked) {
        var isRecommend = checked ? 1 : 0;  // 将布尔值转为数字（1=推荐，0=取消）
        $.ajax({
            url: '${pageContext.request.contextPath}/admin/product/recommend',
            type: 'POST',
            data: { productid: productid, isRecommend: isRecommend },
            success: function(data) {
                if (data.success) {
                    location.reload();       // 成功后刷新页面更新状态
                } else {
                    alert(data.message || '操作失败');
                    location.reload();       // 失败也刷新，恢复开关原始状态
                }
            },
            error: function() {
                alert('操作失败，请重试');
                location.reload();           // 请求错误也刷新恢复
            }
        });
    }
</script>
</body>
</html>