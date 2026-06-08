<%-- 首页 index.jsp --%>
<%-- 功能：BookVerse在线书店首页，包含导航栏、Hero区域、分类、热销排行、推荐、排行榜、特性展示、页脚 --%>
<%-- 这是用户访问网站时看到的第一个页面，是整个网站的入口 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 引入JSTL核心标签库（用于条件判断和循环） --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- 引入JSTL格式化标签库（用于数字、日期格式化） --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%-- 引入JSTL函数标签库（用于字符串处理） --%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BookVerse - 探索知识的无限宇宙</title>
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/favicon.svg">
    <%-- 引入Font Awesome图标库 --%>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <%-- 引入赛博朋克主题CSS --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cyber-theme.css">
    <style>
        /* 首页特殊样式 */
        .hero-particles {
            position: absolute;
            inset: 0;
            overflow: hidden;
            z-index: 0;
        }
        .hero-particle {
            position: absolute;
            width: 3px;
            height: 3px;
            background: rgba(0, 212, 255, 0.4);
            border-radius: 50%;
            animation: heroFloat linear infinite;
        }
        .hero-particle:nth-child(1) { left: 10%; top: 20%; animation-duration: 15s; animation-delay: 0s; }
        .hero-particle:nth-child(2) { left: 25%; top: 60%; width: 4px; height: 4px; animation-duration: 18s; animation-delay: -3s; background: rgba(167, 139, 250, 0.3); }
        .hero-particle:nth-child(3) { left: 45%; top: 30%; animation-duration: 12s; animation-delay: -5s; }
        .hero-particle:nth-child(4) { left: 65%; top: 70%; width: 5px; height: 5px; animation-duration: 20s; animation-delay: -2s; background: rgba(99, 102, 241, 0.25); }
        .hero-particle:nth-child(5) { left: 80%; top: 40%; animation-duration: 16s; animation-delay: -4s; }
        .hero-particle:nth-child(6) { left: 15%; top: 80%; width: 4px; height: 4px; animation-duration: 22s; animation-delay: -6s; }
        .hero-particle:nth-child(7) { left: 55%; top: 15%; animation-duration: 14s; animation-delay: -1s; }
        .hero-particle:nth-child(8) { left: 75%; top: 55%; width: 4px; height: 4px; animation-duration: 19s; animation-delay: -7s; background: rgba(0, 212, 255, 0.3); }
        @keyframes heroFloat {
            0% { transform: translateY(0) translateX(0); opacity: 0; }
            10% { opacity: 1; }
            90% { opacity: 1; }
            100% { transform: translateY(-100vh) translateX(30px); opacity: 0; }
        }
        .hero-glow {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 600px;
            height: 600px;
            background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, transparent 70%);
            pointer-events: none;
            animation: glowPulse 4s ease-in-out infinite;
        }
        @keyframes glowPulse {
            0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(1); }
            50% { opacity: 1; transform: translate(-50%, -50%) scale(1.1); }
        }
        .scanline {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 2px;
            background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.3), transparent);
            animation: scanMove 8s linear infinite;
            pointer-events: none;
        }
        @keyframes scanMove {
            0% { top: 0; }
            100% { top: 100%; }
        }
        .toast-cyber {
            position: fixed;
            top: 80px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(17, 24, 39, 0.95);
            backdrop-filter: blur(20px);
            border: 1px solid var(--cyber-border);
            color: var(--text-primary);
            padding: 14px 32px;
            border-radius: 50px;
            z-index: 9999;
            font-size: 14px;
            box-shadow: var(--shadow-neon);
            display: none;
        }
    </style>
</head>
<body>
<%-- ========== 导航栏区域 ========== --%>
<%-- 固定在页面顶部的导航栏，包含Logo、导航链接、搜索框、购物车、用户菜单 --%>
<nav class="navbar-cyber">
    <div class="container">
        <%-- 网站Logo，点击跳转首页 --%>
        <a href="${pageContext.request.contextPath}/" class="nav-brand">
            <div class="brand-icon"><i class="fas fa-book-open"></i></div>
            BookVerse<span>ONLINE BOOKSTORE</span>
        </a>
        <%-- 主导航链接 --%>
        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/" class="active">首页</a>
            <a href="${pageContext.request.contextPath}/order/history">我的订单</a>
            <a href="${pageContext.request.contextPath}/coupon/my">优惠券</a>
            <a href="${pageContext.request.contextPath}/help">帮助</a>
        </div>
        <%-- 右侧功能区：搜索框、购物车、用户信息 --%>
        <div class="nav-right">
            <%-- 搜索表单 --%>
            <form class="nav-search" action="${pageContext.request.contextPath}/search" method="get">
                <input type="text" name="keyword" placeholder="搜索书名、作者..." value="<c:out value="${keyword}"/>">
                <button type="submit"><i class="fas fa-search"></i></button>
            </form>
            <%-- 购物车图标，显示商品数量 --%>
            <a href="${pageContext.request.contextPath}/cart" class="nav-cart">
                <i class="fas fa-shopping-cart"></i>
                <span class="badge" id="cartCount">${cartSize > 0 ? cartSize : 0}</span>
            </a>
            <%-- 已登录用户：显示用户头像和下拉菜单 --%>
            <c:if test="${not empty sessionScope.user}">
                <div class="nav-user">
                    <div class="nav-user-btn">
                        <div class="avatar">
                            <img src="${pageContext.request.contextPath}${not empty sessionScope.user.avatar ? sessionScope.user.avatar : '/img/default-book.svg'}" alt="">
                        </div>
                        <span><c:out value="${sessionScope.user.userid}"/></span>
                        <i class="fas fa-chevron-down" style="font-size:10px;"></i>
                    </div>
                    <%-- 用户下拉菜单 --%>
                    <div class="nav-dropdown">
                        <a href="${pageContext.request.contextPath}/user/profile"><i class="fas fa-user"></i> 个人中心</a>
                        <a href="${pageContext.request.contextPath}/order/history"><i class="fas fa-list"></i> 我的订单</a>
                        <a href="${pageContext.request.contextPath}/message"><i class="fas fa-envelope"></i> 消息 <span id="msgCount" style="display:none;color:var(--neon-blue);font-weight:700;"></span></a>
                        <a href="${pageContext.request.contextPath}/coupon/my"><i class="fas fa-ticket"></i> 优惠券</a>
                        <div class="sep"></div>
                        <%-- 管理员用户显示管理后台入口 --%>
                        <c:if test="${sessionScope.user.role == 'admin'}">
                            <a href="${pageContext.request.contextPath}/admin/login" style="color:var(--neon-blue);"><i class="fas fa-cog"></i> 管理后台</a>
                        </c:if>
                        <a href="javascript:void(0)" onclick="document.getElementById('logoutForm').submit()"><i class="fas fa-sign-out-alt"></i> 退出登录</a>
                    </div>
                </div>
            </c:if>
            <%-- 未登录用户：显示登录/注册按钮 --%>
            <c:if test="${empty sessionScope.user}">
                <div class="nav-links">
                    <a href="${pageContext.request.contextPath}/login">登录</a>
                    <a href="${pageContext.request.contextPath}/register" style="background:linear-gradient(135deg,var(--primary),var(--neon-blue));color:#fff;padding:8px 20px;border-radius:50px;">注册</a>
                </div>
            </c:if>
        </div>
    </div>
</nav>
<%-- 隐藏的退出登录表单 --%>
<form id="logoutForm" action="${pageContext.request.contextPath}/logout" method="post" style="display:none;"></form>

<%-- ========== Hero区域（首屏大图区域） ========== --%>
<%-- 包含粒子动画背景、中央搜索框、热门搜索标签 --%>
<section class="hero-cyber">
    <%-- 背景浮动粒子动画 --%>
    <div class="hero-particles">
        <div class="hero-particle"></div><div class="hero-particle"></div><div class="hero-particle"></div>
        <div class="hero-particle"></div><div class="hero-particle"></div><div class="hero-particle"></div>
        <div class="hero-particle"></div><div class="hero-particle"></div>
    </div>
    <div class="hero-glow"></div>  <%-- 中央光晕效果 --%>
    <div class="scanline"></div>  <%-- 扫描线动画 --%>
    <div class="container">
        <h1 class="hero-title">探索知识的无限宇宙</h1>  <%-- 主标题 --%>
        <p class="hero-subtitle">海量精选图书，从经典名著到前沿科技，开启你的下一段阅读旅程</p>  <%-- 副标题 --%>
        <%-- Hero区域搜索框 --%>
        <form class="hero-search" action="${pageContext.request.contextPath}/search" method="get">
            <input type="text" name="keyword" placeholder="搜索书名、作者、ISBN...">
            <button type="submit"><i class="fas fa-search"></i> 搜索</button>
        </form>
        <%-- 热门搜索标签（快速搜索链接） --%>
        <div class="hero-tags">
            <a href="${pageContext.request.contextPath}/?keyword=Java">Java编程</a>
            <a href="${pageContext.request.contextPath}/?keyword=Python">Python入门</a>
            <a href="${pageContext.request.contextPath}/?keyword=小说">热门小说</a>
            <a href="${pageContext.request.contextPath}/?keyword=人工智能">人工智能</a>
            <a href="${pageContext.request.contextPath}/?keyword=科幻">科幻经典</a>
        </div>
    </div>
</section>

<%-- ========== 图书分类区域 ========== --%>
<%-- 展示所有图书分类，用户点击可跳转到对应分类页面 --%>
<section class="section">
    <div class="container">
        <div class="section-header fade-in">
            <h2 class="section-title">图书分类</h2>
            <p class="section-subtitle">按兴趣探索，发现你的专属书单</p>
            <div class="section-divider"></div>
        </div>
        <%-- 分类网格，使用JSTL循环遍历分类列表 --%>
        <div class="cat-grid">
            <c:forEach items="${categories}" var="cat" varStatus="vs">
                <a href="${pageContext.request.contextPath}/?category=${cat.categoryid}" class="cat-card fade-in" style="transition-delay:${vs.index * 80}ms">
                    <%-- 根据分类索引显示不同图标 --%>
                    <div class="icon"><i class="fas ${vs.index == 0 ? 'fa-pen-fancy' : vs.index == 1 ? 'fa-landmark' : vs.index == 2 ? 'fa-laptop-code' : vs.index == 3 ? 'fa-chart-line' : vs.index == 4 ? 'fa-graduation-cap' : vs.index == 5 ? 'fa-child' : vs.index == 6 ? 'fa-palette' : 'fa-leaf'}"></i></div>
                    <div class="name"><c:out value="${cat.name}"/></div>
                </a>
            </c:forEach>
        </div>
    </div>
</section>

<%-- ========== 热销排行区域 ========== --%>
<%-- 展示销量最高的前10本图书 --%>
<c:if test="${not empty bestsellers}">
<section class="section" style="background:rgba(15,23,42,0.5);">
    <div class="container">
        <div class="section-header fade-in">
            <h2 class="section-title">热销排行</h2>
            <p class="section-subtitle">万千读者的共同选择</p>
            <div class="section-divider"></div>
        </div>
        <%-- 图书网格展示区域 --%>
        <div class="book-grid">
            <c:forEach items="${bestsellers}" var="book" begin="0" end="9" varStatus="vs">
                <div class="book-card fade-in" style="transition-delay:${vs.index * 60}ms">
                    <div class="img-box">
                        <%-- 前3名显示TOP标签 --%>
                        <c:if test="${vs.index < 3}"><span class="tag tag-hot">TOP${vs.index + 1}</span></c:if>
                        <%-- 图书封面图片，加载失败时显示默认图标 --%>
                        <img src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg" alt="${book.name}"
                             onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                        <div style="display:none;position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);font-size:48px;opacity:0.3;">📖</div>
                    </div>
                    <div class="body">
                        <%-- 图书名称链接 --%>
                        <a href="${pageContext.request.contextPath}/product/detail?id=${book.productid}" class="title"><c:out value="${book.name}"/></a>
                        <div class="price-row">
                            <span class="price"><span class="yen">¥</span><fmt:formatNumber value="${book.price}" pattern="#,##0.00"/></span>  <%-- 价格（格式化为两位小数） --%>
                            <span class="sales">已售 ${book.sales}</span>  <%-- 销量 --%>
                        </div>
                        <div class="actions">
                            <%-- 加入购物车按钮，调用addToCart函数 --%>
                            <button class="btn-cyber-cart" onclick="addToCart('${book.productid}', this)"><i class="fas fa-cart-plus"></i> 加入购物车</button>
                            <%-- 查看详情按钮 --%>
                            <a href="${pageContext.request.contextPath}/product/detail?id=${book.productid}" class="btn-cyber-detail"><i class="fas fa-eye"></i></a>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</section>
</c:if>

<%-- ========== 精选推荐区域 ========== --%>
<%-- 编辑精心挑选的好书推荐 --%>
<c:if test="${not empty recommended}">
<section class="section">
    <div class="container">
        <div class="section-header fade-in">
            <h2 class="section-title">精选推荐</h2>
            <p class="section-subtitle">编辑精心挑选的好书</p>
            <div class="section-divider"></div>
        </div>
        <div class="book-grid">
            <c:forEach items="${recommended}" var="book" begin="0" end="4" varStatus="vs">
                <div class="book-card fade-in" style="transition-delay:${vs.index * 60}ms">
                    <div class="img-box">
                        <span class="tag tag-rec">精选</span>
                        <img src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg" alt="${book.name}"
                             onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                        <div style="display:none;position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);font-size:48px;opacity:0.3;">📖</div>
                    </div>
                    <div class="body">
                        <a href="${pageContext.request.contextPath}/product/detail?id=${book.productid}" class="title"><c:out value="${book.name}"/></a>
                        <div class="price-row">
                            <span class="price"><span class="yen">¥</span><fmt:formatNumber value="${book.price}" pattern="#,##0.00"/></span>
                            <span class="sales">好评推荐</span>
                        </div>
                        <div class="actions">
                            <button class="btn-cyber-cart" onclick="addToCart('${book.productid}', this)"><i class="fas fa-cart-plus"></i> 加入购物车</button>
                            <a href="${pageContext.request.contextPath}/product/detail?id=${book.productid}" class="btn-cyber-detail"><i class="fas fa-eye"></i></a>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</section>
</c:if>

<%-- ========== 统计栏区域 ========== --%>
<%-- 展示平台核心数据：图书数量、用户数量、好评率、发货速度 --%>
<section class="stats-bar">
    <div class="container">
        <div class="stats-grid">
            <div class="stat-item fade-in"><div class="num">10,000+</div><div class="label">精选图书</div></div>
            <div class="stat-item fade-in" style="transition-delay:100ms"><div class="num">50,000+</div><div class="label">忠实读者</div></div>
            <div class="stat-item fade-in" style="transition-delay:200ms"><div class="num">99.8%</div><div class="label">好评率</div></div>
            <div class="stat-item fade-in" style="transition-delay:300ms"><div class="num">24h</div><div class="label">极速发货</div></div>
        </div>
    </div>
</section>

<%-- ========== 排行榜区域 ========== --%>
<%-- 包含销量榜和新书榜两个排行列表 --%>
<c:if test="${not empty bestsellers || not empty newArrivals}">
<section class="section">
    <div class="container">
        <div class="section-header fade-in">
            <h2 class="section-title">排行榜</h2>
            <p class="section-subtitle">实时热销与新书榜单</p>
            <div class="section-divider"></div>
        </div>
        <div class="rank-grid">
            <%-- 销量榜 --%>
            <c:if test="${not empty bestsellers}">
            <div class="rank-card fade-in">
                <div class="rank-header">
                    <div class="title"><i class="fas fa-fire"></i> 销量榜</div>
                </div>
                <ul class="rank-list">
                    <c:forEach items="${bestsellers}" var="book" begin="0" end="4" varStatus="vs">
                        <li class="rank-item">
                            <span class="rank-num n${vs.index + 1}">${vs.index + 1}</span>  <%-- 排名序号 --%>
                            <div class="rank-img">
                                <img src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg" alt="${book.name}"
                                     onerror="this.style.display='none'">
                            </div>
                            <div class="rank-info">
                                <a href="${pageContext.request.contextPath}/product/detail?id=${book.productid}" class="rank-name"><c:out value="${book.name}"/></a>
                                <div class="rank-meta">
                                    <span class="rank-price">¥<fmt:formatNumber value="${book.price}" pattern="#,##0.00"/></span>
                                    <span class="rank-sales">销量 ${book.sales}</span>
                                </div>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            </c:if>
            <%-- 新书榜 --%>
            <c:if test="${not empty newArrivals}">
            <div class="rank-card fade-in" style="transition-delay:100ms">
                <div class="rank-header">
                    <div class="title"><i class="fas fa-star"></i> 新书榜</div>
                </div>
                <ul class="rank-list">
                    <c:forEach items="${newArrivals}" var="book" begin="0" end="4" varStatus="vs">
                        <li class="rank-item">
                            <span class="rank-num n${vs.index + 1}">${vs.index + 1}</span>
                            <div class="rank-img">
                                <img src="${pageContext.request.contextPath}/img/books/${book.productid}.jpg" alt="${book.name}"
                                     onerror="this.style.display='none'">
                            </div>
                            <div class="rank-info">
                                <a href="${pageContext.request.contextPath}/product/detail?id=${book.productid}" class="rank-name"><c:out value="${book.name}"/></a>
                                <div class="rank-meta">
                                    <span class="rank-price">¥<fmt:formatNumber value="${book.price}" pattern="#,##0.00"/></span>
                                    <span class="rank-sales">新书</span>
                                </div>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            </c:if>
        </div>
    </div>
</section>
</c:if>

<%-- ========== 特性展示区域 ========== --%>
<%-- 展示平台的四大优势：正品保障、极速配送、7天退换、专属客服 --%>
<section class="section" style="background:rgba(15,23,42,0.5);">
    <div class="container">
        <div class="section-header fade-in">
            <h2 class="section-title">为什么选择 BookVerse</h2>
            <p class="section-subtitle">我们致力于提供最好的阅读体验</p>
            <div class="section-divider"></div>
        </div>
        <div class="features">
            <div class="feature-card fade-in">
                <div class="icon"><i class="fas fa-shield-alt"></i></div>
                <div class="title">正品保障</div>
                <div class="desc">所有图书均为正版授权，品质有保证</div>
            </div>
            <div class="feature-card fade-in" style="transition-delay:100ms">
                <div class="icon"><i class="fas fa-shipping-fast"></i></div>
                <div class="title">极速配送</div>
                <div class="desc">下单后24小时内发货，全国3日达</div>
            </div>
            <div class="feature-card fade-in" style="transition-delay:200ms">
                <div class="icon"><i class="fas fa-undo-alt"></i></div>
                <div class="title">7天退换</div>
                <div class="desc">不满意可无理由退换，购物无忧</div>
            </div>
            <div class="feature-card fade-in" style="transition-delay:300ms">
                <div class="icon"><i class="fas fa-headset"></i></div>
                <div class="title">专属客服</div>
                <div class="desc">7×24小时在线，随时为您解答</div>
            </div>
        </div>
    </div>
</section>

<%-- ========== 页脚区域 ========== --%>
<%-- 包含品牌信息、导航链接、联系方式、版权声明 --%>
<footer class="footer-cyber">
    <div class="container">
        <div class="footer-grid">
            <%-- 品牌信息列 --%>
            <div>
                <div class="footer-brand"><i class="fas fa-book-open"></i> BookVerse</div>
                <p class="footer-desc">BookVerse 是一个现代化的在线书店平台，致力于为读者提供优质的阅读体验和便捷的购书服务。</p>
                <%-- 社交媒体链接 --%>
                <div class="footer-social">
                    <a href="#"><i class="fab fa-weixin"></i></a>
                    <a href="#"><i class="fab fa-weibo"></i></a>
                    <a href="#"><i class="fab fa-tiktok"></i></a>
                    <a href="#"><i class="fas fa-rss"></i></a>
                </div>
            </div>
            <%-- 关于我们链接列 --%>
            <div class="footer-col">
                <h5>关于我们</h5>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/about">公司介绍</a></li>
                    <li><a href="${pageContext.request.contextPath}/history">发展历程</a></li>
                    <li><a href="${pageContext.request.contextPath}/careers">加入我们</a></li>
                    <li><a href="${pageContext.request.contextPath}/partners">合作伙伴</a></li>
                </ul>
            </div>
            <%-- 客户服务链接列 --%>
            <div class="footer-col">
                <h5>客户服务</h5>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/help">帮助中心</a></li>
                    <li><a href="${pageContext.request.contextPath}/return-policy">退换货政策</a></li>
                    <li><a href="${pageContext.request.contextPath}/shipping">配送说明</a></li>
                    <li><a href="${pageContext.request.contextPath}/complaint">投诉建议</a></li>
                </ul>
            </div>
            <%-- 联系我们信息列 --%>
            <div class="footer-col">
                <h5>联系我们</h5>
                <ul>
                    <li><i class="fas fa-phone" style="margin-right:8px;color:var(--neon-blue);"></i>400-888-8888</li>
                    <li><i class="fas fa-envelope" style="margin-right:8px;color:var(--neon-blue);"></i>support@bookverse.com</li>
                    <li><i class="fas fa-clock" style="margin-right:8px;color:var(--neon-blue);"></i>9:00-21:00</li>
                </ul>
            </div>
        </div>
        <%-- 页脚底部：版权信息 --%>
        <div class="footer-bottom">
            <span>&copy; 2026 BookVerse. All rights reserved.</span>
            <span>Made with <i class="fas fa-heart" style="color:var(--neon-pink);"></i> for readers</span>
        </div>
    </div>
</footer>

<%-- ========== JavaScript脚本区域 ========== --%>
<%-- 引入jQuery库 --%>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script>
    /**
     * 添加商品到购物车（通过AJAX异步请求）
     * @param {string} productId - 商品ID
     * @param {HTMLElement} btn - 点击的按钮元素
     */
    function addToCart(productId, btn) {
        var origHtml = btn.innerHTML;  // 保存按钮原始内容
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';  // 显示加载动画
        btn.disabled = true;  // 禁用按钮防止重复点击
        
        // 发送AJAX请求添加商品到购物车
        $.ajax({
            url: '${pageContext.request.contextPath}/cart/add/ajax',
            type: 'GET',
            data: { id: productId, quantity: 1 },  // 传递商品ID和数量
            dataType: 'json',
            success: function(data) {
                if (data.success) {
                    // 更新购物车数量显示
                    var count = parseInt($('#cartCount').text()) || 0;
                    $('#cartCount').text(count + 1);
                    showToast('已添加到购物车');  // 显示成功提示
                } else {
                    showToast(data.message || '添加失败');  // 显示失败提示
                }
            },
            error: function() { showToast('请先登录'); },  // 未登录提示
            complete: function() { btn.innerHTML = origHtml; btn.disabled = false; }  // 恢复按钮状态
        });
    }

    /**
     * 显示提示消息（Toast通知）
     * @param {string} msg - 提示消息内容
     */
    function showToast(msg) {
        var t = $('<div class="toast-cyber"></div>');  // 创建提示元素
        t.text(msg);
        $('body').append(t);  // 添加到页面
        t.fadeIn(200);  // 淡入显示
        setTimeout(function() { t.fadeOut(300, function() { t.remove(); }); }, 2000);  // 2秒后淡出并移除
    }

    <%-- 已登录用户：获取未读消息数量 --%>
    <c:if test="${not empty sessionScope.user}">
    $.ajax({
        url: '${pageContext.request.contextPath}/message/unread',
        dataType: 'json',
        success: function(data) {
            if (data.count > 0) {
                // 显示未读消息数量（超过99显示99+）
                $('#msgCount').text(data.count > 99 ? '99+' : data.count).show();
            }
        }
    });
    </c:if>

    <%-- 滚动淡入动画效果 --%>
    // 获取所有需要淡入的元素
    var fadeEls = document.querySelectorAll('.fade-in');
    
    /**
     * 检查元素是否进入视口，如果是则添加visible类触发淡入动画
     */
    function checkFade() {
        fadeEls.forEach(function(el) {
            // 当元素顶部距离视口顶部小于视口高度的92%时触发
            if (el.getBoundingClientRect().top < window.innerHeight * 0.92) {
                el.classList.add('visible');
            }
        });
    }
    // 监听滚动和窗口大小变化事件
    window.addEventListener('scroll', checkFade);
    window.addEventListener('resize', checkFade);
    checkFade();  // 页面加载时立即检查一次
</script>
</body>
</html>
