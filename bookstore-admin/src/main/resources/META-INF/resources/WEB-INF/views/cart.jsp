<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>购物车 - BookVerse</title>
    <link rel="stylesheet" href="https://cdn.bootcdn.net/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cyber-theme.css">
    <style>
        /* ========== 购物车页面特殊样式 ========== */
        .cart-page-wrap {
            padding: 32px 0 140px;
            position: relative;
            z-index: 1;
        }

        /* 返回首页 */
        .back-home-link {
            display: inline-flex;
            align-items: center;
            gap: 10px;
            padding: 12px 28px;
            background: rgba(30, 41, 59, 0.8);
            border: 1px solid var(--cyber-border);
            border-radius: 50px;
            color: var(--text-secondary);
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
            text-decoration: none;
            margin-bottom: 28px;
        }
        .back-home-link:hover {
            color: var(--neon-blue);
            border-color: var(--neon-blue);
            background: rgba(0, 212, 255, 0.08);
            box-shadow: 0 0 20px rgba(0, 212, 255, 0.15);
            transform: translateX(-4px);
        }
        .back-home-link i {
            transition: transform 0.3s ease;
        }
        .back-home-link:hover i {
            transform: translateX(-4px);
        }

        /* 页面标题 */
        .page-header-wrap {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 28px;
        }
        .page-title {
            font-size: 28px;
            font-weight: 800;
            background: linear-gradient(135deg, #fff, var(--neon-blue));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .page-title i {
            font-size: 24px;
            -webkit-text-fill-color: var(--neon-blue);
            filter: drop-shadow(0 0 8px rgba(0, 212, 255, 0.5));
        }
        .page-title-count {
            display: inline-block;
            background: linear-gradient(135deg, var(--primary), var(--neon-blue));
            color: #fff;
            font-size: 13px;
            font-weight: 700;
            padding: 4px 14px;
            border-radius: 50px;
            -webkit-text-fill-color: #fff;
            box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
        }

        /* 清空购物车按钮 */
        .btn-clear-cart {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 24px;
            border-radius: 50px;
            font-size: 13px;
            font-weight: 600;
            background: transparent;
            color: var(--danger);
            border: 1px solid rgba(239, 68, 68, 0.3);
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s ease;
        }
        .btn-clear-cart:hover {
            background: rgba(239, 68, 68, 0.1);
            border-color: var(--danger);
            box-shadow: 0 0 20px rgba(239, 68, 68, 0.2);
            color: var(--danger);
            text-decoration: none;
        }

        /* 布局 */
        .cart-layout {
            display: flex;
            gap: 28px;
        }
        .cart-main {
            flex: 1;
            min-width: 0;
        }
        .cart-sidebar {
            width: 320px;
            flex-shrink: 0;
        }

        /* ========== 空购物车状态 ========== */
        .empty-state {
            text-align: center;
            padding: 100px 20px;
            background: var(--cyber-bg-card);
            backdrop-filter: blur(10px);
            border: 1px solid var(--cyber-border);
            border-radius: var(--radius-xl);
            position: relative;
            overflow: hidden;
        }
        .empty-state::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle at 30% 30%, rgba(0, 212, 255, 0.05) 0%, transparent 50%),
                        radial-gradient(circle at 70% 70%, rgba(99, 102, 241, 0.05) 0%, transparent 50%);
            animation: emptyBgRotate 20s linear infinite;
        }
        @keyframes emptyBgRotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .empty-state .empty-icon-wrap {
            position: relative;
            display: inline-block;
            margin-bottom: 32px;
        }
        .empty-state .empty-icon {
            font-size: 80px;
            color: var(--neon-blue);
            filter: drop-shadow(0 0 30px rgba(0, 212, 255, 0.4));
            animation: float 3s ease-in-out infinite;
            position: relative;
            z-index: 1;
        }
        @keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }
        .empty-state .empty-ring {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 140px;
            height: 140px;
            border: 2px dashed var(--cyber-border);
            border-radius: 50%;
            animation: ringRotate 12s linear infinite;
        }
        @keyframes ringRotate { from { transform: translate(-50%, -50%) rotate(0deg); } to { transform: translate(-50%, -50%) rotate(360deg); } }
        .empty-state .empty-orbit-dot {
            position: absolute;
            width: 8px;
            height: 8px;
            background: var(--neon-blue);
            border-radius: 50%;
            box-shadow: 0 0 12px rgba(0, 212, 255, 0.6);
        }
        .empty-state .empty-orbit-dot:nth-child(2) { top: -4px; left: 50%; margin-left: -4px; }
        .empty-state .empty-orbit-dot:nth-child(3) { bottom: -4px; left: 50%; margin-left: -4px; }
        .empty-state .empty-orbit-dot:nth-child(4) { top: 50%; left: -4px; margin-top: -4px; }
        .empty-state .empty-decor {
            position: absolute;
            font-size: 28px;
            opacity: 0.12;
            color: var(--neon-blue);
        }
        .empty-state .empty-decor:nth-child(5) { top: 15%; left: 10%; animation: float 4s ease-in-out infinite 0.5s; }
        .empty-state .empty-decor:nth-child(6) { top: 25%; right: 12%; animation: float 3.5s ease-in-out infinite 1s; }
        .empty-state .empty-decor:nth-child(7) { bottom: 20%; left: 18%; animation: float 4.5s ease-in-out infinite 1.5s; }
        .empty-state .empty-decor:nth-child(8) { bottom: 22%; right: 18%; animation: float 3.8s ease-in-out infinite 0.8s; }
        .empty-state h2 {
            font-size: 26px;
            font-weight: 800;
            background: linear-gradient(135deg, var(--text-primary), var(--neon-blue));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 12px;
            position: relative;
        }
        .empty-state p {
            color: var(--text-muted);
            margin-bottom: 36px;
            font-size: 15px;
            position: relative;
        }
        .empty-state .empty-suggest {
            display: flex;
            gap: 14px;
            justify-content: center;
            position: relative;
        }

        /* ========== 购物车商品卡片 ========== */
        .cart-item-card {
            background: var(--cyber-bg-card);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid var(--cyber-border);
            border-radius: var(--radius-lg);
            padding: 20px;
            margin-bottom: 16px;
            display: flex;
            align-items: center;
            gap: 20px;
            transition: all 0.4s ease;
            position: relative;
            overflow: hidden;
        }
        .cart-item-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 0;
            height: 100%;
            background: linear-gradient(135deg, rgba(0, 212, 255, 0.03), rgba(99, 102, 241, 0.06));
            transition: width 0.4s ease;
            z-index: 0;
        }
        .cart-item-card:hover {
            border-color: var(--neon-blue);
            box-shadow: var(--shadow-neon);
            transform: translateY(-2px);
        }
        .cart-item-card:hover::before {
            width: 100%;
        }
        .cart-item-card > * {
            position: relative;
            z-index: 1;
        }
        .cart-item-card.selected {
            border-left: 3px solid var(--neon-blue);
            box-shadow: inset 0 0 30px rgba(0, 212, 255, 0.03);
        }

        /* 复选框 */
        .cart-item-checkbox {
            flex-shrink: 0;
        }
        .cart-checkbox {
            width: 20px;
            height: 20px;
            cursor: pointer;
            accent-color: var(--neon-blue);
        }

        /* 商品图片 */
        .cart-item-img-wrap {
            flex-shrink: 0;
            width: 90px;
            height: 120px;
            border-radius: var(--radius);
            overflow: hidden;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
            position: relative;
            border: 1px solid var(--cyber-border);
        }
        .cart-item-img-wrap img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.4s ease;
        }
        .cart-item-card:hover .cart-item-img-wrap img {
            transform: scale(1.08);
        }
        .cart-item-img-wrap::after {
            content: '';
            position: absolute;
            inset: 0;
            background: linear-gradient(180deg, transparent 60%, rgba(0, 0, 0, 0.2));
            pointer-events: none;
        }

        /* 商品信息 */
        .cart-item-info {
            flex: 1;
            min-width: 0;
            padding: 0 12px;
        }
        .cart-item-name {
            font-weight: 700;
            color: var(--text-primary);
            font-size: 16px;
            margin-bottom: 8px;
            line-height: 1.4;
        }
        .cart-item-name a {
            color: var(--text-primary);
            text-decoration: none;
            transition: all 0.2s ease;
        }
        .cart-item-name a:hover {
            color: var(--neon-blue);
            text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
        }
        .cart-item-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 14px;
        }
        .cart-item-price {
            font-size: 20px;
            font-weight: 800;
            color: var(--neon-blue);
            text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
        }
        .cart-item-price::before {
            content: '\FFE5';
            font-size: 13px;
        }
        .cart-item-price-old {
            font-size: 13px;
            color: var(--text-muted);
            text-decoration: line-through;
        }
        .cart-item-price-old::before {
            content: '\FFE5';
            font-size: 11px;
        }

        /* 数量控制器 */
        .qty-control {
            display: inline-flex;
            align-items: center;
            border: 1px solid var(--cyber-border);
            border-radius: var(--radius);
            overflow: hidden;
            background: rgba(30, 41, 59, 0.6);
            transition: all 0.3s ease;
        }
        .qty-control:focus-within {
            border-color: var(--neon-blue);
            box-shadow: 0 0 15px rgba(0, 212, 255, 0.2), inset 0 0 15px rgba(0, 212, 255, 0.05);
        }
        .qty-btn {
            width: 36px;
            height: 36px;
            border: none;
            background: transparent;
            color: var(--text-secondary);
            font-size: 18px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 0;
            user-select: none;
        }
        .qty-btn:hover {
            background: rgba(0, 212, 255, 0.1);
            color: var(--neon-blue);
        }
        .qty-btn:active {
            transform: scale(0.9);
        }
        .qty-btn:disabled {
            opacity: 0.3;
            cursor: not-allowed;
        }
        .qty-btn:disabled:hover {
            background: transparent;
            color: var(--text-secondary);
        }
        .qty-input {
            width: 50px;
            height: 36px;
            border: none;
            border-left: 1px solid var(--cyber-border);
            border-right: 1px solid var(--cyber-border);
            text-align: center;
            font-size: 15px;
            font-weight: 700;
            color: var(--text-primary);
            outline: none;
            background: transparent;
        }

        /* 小计 */
        .cart-item-subtotal-wrap {
            flex-shrink: 0;
            text-align: right;
            min-width: 100px;
        }
        .cart-subtotal {
            font-size: 22px;
            font-weight: 800;
            color: var(--neon-blue);
            text-shadow: 0 0 12px rgba(0, 212, 255, 0.4);
        }
        .cart-subtotal::before {
            content: '\FFE5';
            font-size: 14px;
        }
        .cart-item-subtotal-label {
            font-size: 12px;
            color: var(--text-muted);
            margin-bottom: 4px;
        }

        /* 删除按钮 */
        .cart-remove-btn {
            background: none;
            border: none;
            color: var(--text-muted);
            cursor: pointer;
            font-size: 22px;
            padding: 8px;
            transition: all 0.3s ease;
            border-radius: 50%;
            flex-shrink: 0;
        }
        .cart-remove-btn:hover {
            color: var(--danger);
            background: rgba(239, 68, 68, 0.1);
            box-shadow: 0 0 15px rgba(239, 68, 68, 0.2);
        }

        /* 继续购物 */
        .btn-continue {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 28px;
            border-radius: 50px;
            font-size: 14px;
            font-weight: 600;
            background: transparent;
            color: var(--text-accent);
            border: 1px solid var(--cyber-border);
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s ease;
        }
        .btn-continue:hover {
            color: var(--neon-blue);
            border-color: var(--neon-blue);
            background: rgba(0, 212, 255, 0.05);
            box-shadow: 0 0 15px rgba(0, 212, 255, 0.1);
            text-decoration: none;
        }

        /* ========== 底部结算栏 ========== */
        .settlement-bar {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            background: rgba(10, 14, 26, 0.95);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border-top: 1px solid var(--cyber-border);
            box-shadow: 0 -4px 30px rgba(0, 0, 0, 0.5);
            z-index: 1000;
            padding: 16px 0;
        }
        .settlement-inner {
            display: flex;
            align-items: center;
            justify-content: space-between;
            max-width: 1280px;
            margin: 0 auto;
            padding: 0 24px;
        }
        .settlement-select-all {
            display: flex;
            align-items: center;
            gap: 10px;
            cursor: pointer;
            font-size: 14px;
            color: var(--text-secondary);
            font-weight: 600;
        }
        .settlement-info {
            display: flex;
            align-items: center;
            gap: 24px;
        }
        .settlement-count {
            font-size: 14px;
            color: var(--text-muted);
        }
        .settlement-count strong {
            color: var(--text-primary);
        }
        .settlement-total-label {
            font-size: 14px;
            color: var(--text-muted);
        }
        .settlement-total-amount {
            font-size: 30px;
            font-weight: 800;
            color: var(--neon-blue);
            text-shadow: 0 0 20px rgba(0, 212, 255, 0.4);
        }
        .settlement-total-amount::before {
            content: '\FFE5';
            font-size: 18px;
        }
        .btn-settle {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            padding: 16px 50px;
            border-radius: 50px;
            font-size: 18px;
            font-weight: 700;
            background: linear-gradient(135deg, var(--primary), var(--neon-blue));
            color: #fff;
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.3s ease;
            box-shadow: 0 0 25px rgba(99, 102, 241, 0.4);
            position: relative;
            overflow: hidden;
        }
        .btn-settle:hover {
            transform: translateY(-3px);
            box-shadow: 0 0 40px rgba(99, 102, 241, 0.6);
            color: #fff;
            text-decoration: none;
        }
        .btn-settle::after {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
            transition: left 0.5s ease;
        }
        .btn-settle:hover::after {
            left: 100%;
        }
        .btn-settle:disabled {
            opacity: 0.4;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }

        /* ========== 侧边栏卡片 ========== */
        .sidebar-card {
            background: var(--cyber-bg-card);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid var(--cyber-border);
            border-radius: var(--radius-lg);
            padding: 24px;
            margin-bottom: 20px;
        }
        .sidebar-card h4 {
            font-size: 16px;
            font-weight: 700;
            color: var(--text-primary);
            margin: 0 0 18px;
            padding-bottom: 14px;
            border-bottom: 1px solid var(--cyber-border);
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .sidebar-card h4 i {
            color: var(--neon-blue);
        }

        /* 推荐商品 */
        .sidebar-reco-item {
            display: flex;
            gap: 12px;
            margin-bottom: 14px;
            padding-bottom: 14px;
            border-bottom: 1px solid rgba(99, 102, 241, 0.08);
            text-decoration: none;
            transition: all 0.3s ease;
        }
        .sidebar-reco-item:last-child {
            border-bottom: none;
            margin-bottom: 0;
            padding-bottom: 0;
        }
        .sidebar-reco-item:hover {
            opacity: 0.85;
            transform: translateX(4px);
        }
        .sidebar-reco-item img {
            width: 52px;
            height: 68px;
            object-fit: cover;
            border-radius: var(--radius);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
            border: 1px solid var(--cyber-border);
        }
        .sidebar-reco-info {
            flex: 1;
            min-width: 0;
        }
        .sidebar-reco-name {
            font-size: 13px;
            font-weight: 700;
            color: var(--text-primary);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            margin-bottom: 4px;
        }
        .sidebar-reco-author {
            font-size: 11px;
            color: var(--text-muted);
            margin-bottom: 4px;
        }
        .sidebar-reco-price {
            font-size: 15px;
            font-weight: 700;
            color: var(--neon-blue);
            text-shadow: 0 0 8px rgba(0, 212, 255, 0.3);
        }

        /* ========== 优惠券卡片 ========== */
        .coupon-card {
            background: var(--cyber-bg-card);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid var(--cyber-border);
            border-radius: var(--radius-lg);
            padding: 24px;
            margin-bottom: 20px;
        }
        .coupon-card h4 {
            font-size: 16px;
            font-weight: 700;
            color: var(--text-primary);
            margin: 0 0 18px;
            padding-bottom: 14px;
            border-bottom: 1px solid var(--cyber-border);
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .coupon-card h4 i {
            color: var(--neon-blue);
        }
        .coupon-input-group {
            display: flex;
            gap: 8px;
            margin-bottom: 12px;
        }
        .coupon-input {
            flex: 1;
            padding: 10px 14px;
            border: 1px solid var(--cyber-border);
            border-radius: var(--radius);
            font-size: 13px;
            outline: none;
            transition: all 0.3s ease;
            background: rgba(30, 41, 59, 0.6);
            color: var(--text-primary);
        }
        .coupon-input::placeholder {
            color: var(--text-muted);
        }
        .coupon-input:focus {
            border-color: var(--neon-blue);
            box-shadow: 0 0 15px rgba(0, 212, 255, 0.15);
        }
        .coupon-apply-btn {
            padding: 10px 20px;
            border: none;
            border-radius: var(--radius);
            background: linear-gradient(135deg, var(--primary), var(--neon-blue));
            color: #fff;
            font-weight: 700;
            font-size: 13px;
            cursor: pointer;
            transition: all 0.3s ease;
            white-space: nowrap;
        }
        .coupon-apply-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 20px rgba(99, 102, 241, 0.4);
        }
        .coupon-apply-btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            transform: none;
        }
        .coupon-msg {
            font-size: 13px;
            margin-top: 8px;
            min-height: 20px;
        }
        .coupon-msg.success {
            color: var(--neon-green);
        }
        .coupon-msg.error {
            color: var(--danger);
        }
        .coupon-list {
            max-height: 200px;
            overflow-y: auto;
        }
        .coupon-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            margin-bottom: 8px;
            border-radius: var(--radius);
            border: 1px dashed var(--cyber-border);
            background: rgba(30, 41, 59, 0.4);
            cursor: pointer;
            transition: all 0.3s ease;
        }
        .coupon-item:hover {
            border-color: var(--neon-blue);
            background: rgba(0, 212, 255, 0.05);
            transform: translateY(-1px);
        }
        .coupon-item.active {
            border-color: var(--neon-blue);
            border-style: solid;
            background: rgba(0, 212, 255, 0.08);
            box-shadow: 0 0 15px rgba(0, 212, 255, 0.1);
        }
        .coupon-item-icon {
            width: 44px;
            height: 44px;
            border-radius: var(--radius);
            background: linear-gradient(135deg, var(--danger), #dc2626);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 800;
            font-size: 12px;
            flex-shrink: 0;
            line-height: 1.2;
            text-align: center;
            box-shadow: 0 0 12px rgba(239, 68, 68, 0.3);
        }
        .coupon-item-info {
            flex: 1;
            min-width: 0;
        }
        .coupon-item-name {
            font-size: 13px;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 2px;
        }
        .coupon-item-desc {
            font-size: 11px;
            color: var(--text-muted);
        }
        .coupon-item-radio {
            width: 18px;
            height: 18px;
            accent-color: var(--neon-blue);
            flex-shrink: 0;
        }
        .coupon-loading {
            text-align: center;
            padding: 16px;
            color: var(--text-muted);
            font-size: 13px;
        }
        .coupon-empty {
            text-align: center;
            padding: 12px;
            color: var(--text-muted);
            font-size: 13px;
        }
        .coupon-discount-info {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 14px;
            background: rgba(16, 185, 129, 0.08);
            border: 1px solid rgba(16, 185, 129, 0.2);
            border-radius: var(--radius);
            margin-top: 12px;
        }
        .coupon-discount-label {
            font-size: 13px;
            color: var(--text-secondary);
        }
        .coupon-discount-amount {
            font-size: 16px;
            font-weight: 800;
            color: var(--neon-green);
            text-shadow: 0 0 10px rgba(52, 211, 153, 0.3);
        }

        /* ========== 订单摘要 ========== */
        .summary-card {
            background: var(--cyber-bg-card);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid var(--cyber-border);
            border-radius: var(--radius-lg);
            padding: 24px;
            margin-bottom: 20px;
        }
        .summary-card h4 {
            font-size: 16px;
            font-weight: 700;
            color: var(--text-primary);
            margin: 0 0 18px;
            padding-bottom: 14px;
            border-bottom: 1px solid var(--cyber-border);
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .summary-card h4 i {
            color: var(--neon-blue);
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 14px;
            font-size: 14px;
        }
        .summary-row .label {
            color: var(--text-muted);
        }
        .summary-row .value {
            font-weight: 700;
            color: var(--text-primary);
        }
        .summary-row .value.danger {
            color: var(--neon-blue);
            font-size: 16px;
        }
        .summary-row .value.success {
            color: var(--neon-green);
        }
        .summary-divider {
            height: 1px;
            background: var(--cyber-border);
            margin: 18px 0;
        }
        .summary-total {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
        }
        .summary-total .label {
            font-size: 16px;
            font-weight: 700;
            color: var(--text-secondary);
        }
        .summary-total .value {
            font-size: 28px;
            font-weight: 800;
            color: var(--neon-blue);
            text-shadow: 0 0 20px rgba(0, 212, 255, 0.4);
        }
        .summary-total .value::before {
            content: '\FFE5';
            font-size: 16px;
        }

        /* 滚动条 */
        .coupon-list::-webkit-scrollbar {
            width: 4px;
        }
        .coupon-list::-webkit-scrollbar-track {
            background: transparent;
        }
        .coupon-list::-webkit-scrollbar-thumb {
            background: var(--cyber-border);
            border-radius: 2px;
        }

        /* ========== 响应式 ========== */
        @media (max-width: 768px) {
            .cart-layout { flex-direction: column; }
            .cart-sidebar { width: 100%; order: -1; }
            .settlement-inner { flex-wrap: wrap; gap: 12px; }
            .settlement-info { gap: 12px; }
            .btn-settle { padding: 12px 30px; font-size: 16px; }
            .cart-item-card { flex-wrap: wrap; gap: 12px; }
            .cart-item-img-wrap { width: 70px; height: 90px; }
            .cart-item-info { padding: 0; }
            .page-header-wrap { flex-direction: column; align-items: flex-start; gap: 12px; }
        }
    </style>
</head>
<body>

<%-- ========== 导航栏 ========== --%>
<%-- 与其他页面相同的导航栏结构 --%>
<nav class="navbar-cyber">
    <div class="container">
        <a href="${pageContext.request.contextPath}/" class="nav-brand">
            <div class="brand-icon"><i class="fas fa-book-open"></i></div>
            BookVerse<span>ONLINE BOOKSTORE</span>
        </a>
        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/">首页</a>
            <a href="${pageContext.request.contextPath}/order/history">我的订单</a>
            <a href="${pageContext.request.contextPath}/coupon/my">优惠券</a>
            <a href="${pageContext.request.contextPath}/help">帮助</a>
        </div>
        <div class="nav-right">
            <form class="nav-search" action="${pageContext.request.contextPath}/search" method="get">
                <input type="text" name="keyword" placeholder="搜索书名、作者...">
                <button type="submit"><i class="fas fa-search"></i></button>
            </form>
            <%-- 购物车图标（当前页面高亮） --%>
            <a href="${pageContext.request.contextPath}/cart" class="nav-cart" style="color:var(--neon-blue);">
                <i class="fas fa-shopping-cart"></i>
                <span class="badge" id="cartCount">${cartSize > 0 ? cartSize : 0}</span>
            </a>
            <c:if test="${not empty sessionScope.user}">
                <div class="nav-user">
                    <div class="nav-user-btn">
                        <div class="avatar">
                            <img src="${pageContext.request.contextPath}${not empty sessionScope.user.avatar ? sessionScope.user.avatar : '/img/default-book.svg'}" alt="">
                        </div>
                        <span><c:out value="${sessionScope.user.userid}"/></span>
                        <i class="fas fa-chevron-down" style="font-size:10px;"></i>
                    </div>
                    <div class="nav-dropdown">
                        <a href="${pageContext.request.contextPath}/user/profile"><i class="fas fa-user"></i> 个人中心</a>
                        <a href="${pageContext.request.contextPath}/order/history"><i class="fas fa-list"></i> 我的订单</a>
                        <a href="${pageContext.request.contextPath}/message"><i class="fas fa-envelope"></i> 消息</a>
                        <a href="${pageContext.request.contextPath}/coupon/my"><i class="fas fa-ticket"></i> 优惠券</a>
                        <div class="sep"></div>
                        <c:if test="${sessionScope.user.role == 'admin'}">
                            <a href="${pageContext.request.contextPath}/admin/login" style="color:var(--neon-blue);"><i class="fas fa-cog"></i> 管理后台</a>
                        </c:if>
                        <a href="javascript:void(0)" onclick="document.getElementById('logoutForm').submit()"><i class="fas fa-sign-out-alt"></i> 退出登录</a>
                    </div>
                </div>
            </c:if>
            <c:if test="${empty sessionScope.user}">
                <div class="nav-links">
                    <a href="${pageContext.request.contextPath}/login">登录</a>
                    <a href="${pageContext.request.contextPath}/register" style="background:linear-gradient(135deg,var(--primary),var(--neon-blue));color:#fff;padding:8px 20px;border-radius:50px;">注册</a>
                </div>
            </c:if>
        </div>
    </div>
</nav>
<form id="logoutForm" action="${pageContext.request.contextPath}/logout" method="post" style="display:none;"></form>

<%-- ========== 购物车页面主体 ========== --%>
<div class="cart-page-wrap">
<div class="container">

    <%-- 返回首页链接 --%>
    <a href="${pageContext.request.contextPath}/" class="back-home-link">
        <i class="fas fa-arrow-left"></i>
        <span>返回首页</span>
    </a>

    <%-- 页面标题区域 --%>
    <div class="page-header-wrap">
        <h1 class="page-title">
            <i class="fas fa-shopping-cart"></i> 我的购物车
            <%-- 显示商品数量 --%>
            <c:if test="${not empty cart}">
                <span class="page-title-count">${fn:length(cart)} 件商品</span>
            </c:if>
        </h1>
        <%-- 清空购物车按钮（有商品时显示） --%>
        <c:if test="${not empty cart}">
            <a href="${pageContext.request.contextPath}/cart/clear" class="btn-clear-cart" onclick="return confirm('确定清空购物车吗？所有商品将被移除。')"><i class="fas fa-trash-alt"></i> 清空购物车</a>
        </c:if>
    </div>

    <%-- ========== 空购物车状态 ========== --%>
    <%-- 当购物车为空时显示此区域 --%>
    <c:if test="${empty cart}">
        <div class="empty-state">
            <i class="fas fa-meteor empty-decor"></i>
            <i class="fas fa-satellite empty-decor"></i>
            <i class="fas fa-rocket empty-decor"></i>
            <i class="fas fa-atom empty-decor"></i>
            <div class="empty-icon-wrap">
                <i class="fas fa-shopping-basket empty-icon"></i>
                <div class="empty-ring">
                    <span class="empty-orbit-dot"></span>
                    <span class="empty-orbit-dot"></span>
                    <span class="empty-orbit-dot"></span>
                </div>
            </div>
            <h2>购物车空空如也</h2>
            <p>还没有添加任何图书，快去探索知识的无限宇宙吧！</p>
            <div class="empty-suggest">
                <a href="${pageContext.request.contextPath}/" class="btn-neon"><i class="fas fa-rocket"></i> 去逛逛</a>
            </div>
        </div>
    </c:if>

    <%-- ========== 购物车内容（有商品时显示） ========== --%>
    <c:if test="${not empty cart}">
        <div class="cart-layout">
            <%-- 左侧：商品列表区域 --%>
            <div class="cart-main">
                <div id="cartItemsContainer">
                    <c:set var="totalAll" value="0"/>
                    <%-- 循环遍历购物车中的每个商品 --%>
                    <c:forEach items="${cart}" var="item" varStatus="loop">
                        <c:set var="totalAll" value="${totalAll + item.subtotal}"/>
                        <div class="cart-item-card" data-index="${loop.index}" data-subtotal="${item.subtotal}">
                            <div class="cart-item-checkbox">
                                <input type="checkbox" class="cart-checkbox item-checkbox" checked />
                            </div>
                            <div class="cart-item-img-wrap">
                                <a href="${pageContext.request.contextPath}/product/detail?id=${item.productId}">
                                    <img src="${pageContext.request.contextPath}/img/books/${item.productId}.jpg"
                                         alt="${item.name}"
                                         onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2290%22 height=%22120%22><rect fill=%22%231e293b%22 width=%2290%22 height=%22120%22/><text fill=%22%2300d4ff%22 font-size=%2214%22 text-anchor=%22middle%22 x=%2245%22 y=%2264%22>Book</text></svg>'" />
                                </a>
                            </div>
                            <div class="cart-item-info">
                                <div class="cart-item-name">
                                    <a href="${pageContext.request.contextPath}/product/detail?id=${item.productId}">${item.name}</a>
                                </div>
                                <div class="cart-item-meta">
                                    <span class="cart-item-price"><fmt:formatNumber value="${item.price}" pattern="#0.00"/></span>
                                    <span class="cart-item-price-old" data-original-price="${item.price}"></span>
                                </div>
                                <div class="qty-control">
                                    <button type="button" class="qty-btn qty-minus" data-pid="${item.productId}" <c:if test="${item.quantity <= 1}">disabled</c:if>>&minus;</button>
                                    <input type="text" class="qty-input" value="${item.quantity}" data-pid="${item.productId}" data-price="${item.price}" readonly />
                                    <button type="button" class="qty-btn qty-plus" data-pid="${item.productId}">+</button>
                                </div>
                            </div>
                            <div class="cart-item-subtotal-wrap">
                                <div class="cart-item-subtotal-label">小计</div>
                                <span class="cart-subtotal"><fmt:formatNumber value="${item.subtotal}" pattern="#0.00"/></span>
                            </div>
                            <a href="${pageContext.request.contextPath}/cart/remove?productId=${item.productId}" class="cart-remove-btn" title="删除" onclick="return confirm('确定删除该商品吗？')"><i class="fas fa-times"></i></a>
                        </div>
                    </c:forEach>
                </div>

                <div style="text-align: right; margin-top: 20px;">
                    <a href="${pageContext.request.contextPath}/" class="btn-continue"><i class="fas fa-arrow-left"></i> 继续购物</a>
                </div>
            </div>

            <div class="cart-sidebar">
                <c:if test="${not empty sessionScope.user}">
                <div class="coupon-card" id="couponCard">
                    <h4><i class="fas fa-ticket-alt"></i> 优惠券</h4>
                    <div class="coupon-input-group">
                        <input type="text" class="coupon-input" id="couponCodeInput" placeholder="输入优惠码" />
                        <button class="coupon-apply-btn" id="couponApplyBtn" onclick="applyCouponCode()">兑换</button>
                    </div>
                    <div class="coupon-msg" id="couponMsg"></div>
                    <div id="couponListWrap">
                        <div class="coupon-loading" id="couponLoading"><i class="fas fa-spinner fa-spin"></i> 加载优惠券中...</div>
                        <div class="coupon-list" id="couponList" style="display:none;"></div>
                        <div class="coupon-empty" id="couponEmpty" style="display:none;">暂无可用优惠券</div>
                    </div>
                    <div class="coupon-discount-info" id="couponDiscountInfo" style="display:none;">
                        <span class="coupon-discount-label"><i class="fas fa-tag"></i> 优惠金额</span>
                        <span class="coupon-discount-amount" id="couponDiscountAmount">-¥0.00</span>
                    </div>
                </div>
                </c:if>

                <div class="summary-card">
                    <h4><i class="fas fa-clipboard-list"></i> 订单摘要</h4>
                    <div class="summary-row">
                        <span class="label">商品件数</span>
                        <span class="value" id="summaryCount">${fn:length(cart)} 件</span>
                    </div>
                    <div class="summary-row">
                        <span class="label">商品金额</span>
                        <span class="value" id="summarySubtotal">¥<fmt:formatNumber value="${totalAll}" pattern="#0.00"/></span>
                    </div>
                    <div class="summary-row" id="summaryDiscountRow" style="display:none;">
                        <span class="label">优惠减免</span>
                        <span class="value success" id="summaryDiscount">-¥0.00</span>
                    </div>
                    <div class="summary-row">
                        <span class="label">运费</span>
                        <span class="value" style="color: var(--neon-green);">免运费</span>
                    </div>
                    <div class="summary-divider"></div>
                    <div class="summary-total">
                        <span class="label">应付金额</span>
                        <span class="value" id="summaryTotal">¥<fmt:formatNumber value="${totalAll}" pattern="#0.00"/></span>
                    </div>
                </div>

                <div class="sidebar-card">
                    <h4><i class="fas fa-lightbulb"></i> 你可能还喜欢</h4>
                    <c:if test="${not empty recommendedBooks}">
                        <c:forEach items="${recommendedBooks}" var="rb" begin="0" end="3">
                            <a href="${pageContext.request.contextPath}/product/detail?id=${rb.productid}" class="sidebar-reco-item">
                                <img src="${pageContext.request.contextPath}/img/books/${rb.productid}.jpg"
                                     alt="${rb.name}"
                                     onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2248%22 height=%2264%22><rect fill=%22%231e293b%22 width=%2248%22 height=%2264%22/><text fill=%22%2300d4ff%22 font-size=%2210%22 text-anchor=%22middle%22 x=%2224%22 y=%2236%22>Book</text></svg>'" />
                                <div class="sidebar-reco-info">
                                    <div class="sidebar-reco-name">${rb.name}</div>
                                    <div class="sidebar-reco-author">${rb.author}</div>
                                    <div class="sidebar-reco-price">¥<fmt:formatNumber value="${rb.price}" pattern="#0.00"/></div>
                                </div>
                            </a>
                        </c:forEach>
                    </c:if>
                    <c:if test="${empty recommendedBooks}">
                        <p style="color: var(--text-muted); font-size: 13px; text-align: center; padding: 16px 0;">暂无推荐</p>
                    </c:if>
                </div>
            </div>
        </div>
    </c:if>

</div>
</div>

<!-- 底部结算栏 -->
<c:if test="${not empty cart}">
    <div class="settlement-bar" id="settlementBar">
        <div class="settlement-inner">
            <label class="settlement-select-all">
                <input type="checkbox" class="cart-checkbox" id="settlementSelectAll" checked />
                <span>全选</span>
            </label>
            <div class="settlement-info">
                <span class="settlement-count">已选 <strong id="selectedCount">${fn:length(cart)}</strong> 件</span>
                <span class="settlement-total-label">合计：</span>
                <span class="settlement-total-amount" id="settlementTotal">0.00</span>
            </div>
            <a href="${pageContext.request.contextPath}/order" class="btn-settle" id="btnSettle"><i class="fas fa-credit-card"></i> 去结算</a>
        </div>
    </div>
</c:if>

<%-- ========== JavaScript脚本区域 ========== --%>
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<script>
    // 优惠券相关全局变量
    var selectedCouponId = null;  // 选中的优惠券ID
    var selectedCouponDiscount = 0;  // 选中的优惠券减免金额

    $(function() {
        // 设置原价显示（当前价格的1.3倍）
        $('.cart-item-price-old[data-original-price]').each(function() {
            var price = parseFloat($(this).data('original-price'));
            var original = (price * 1.3).toFixed(2);
            $(this).text(original);
        });

        /**
         * 更新结算栏信息
         * 计算选中商品的总金额、数量，并更新页面显示
         */
        function updateSettlement() {
            var total = 0;  // 商品总金额
            var count = 0;  // 商品总数量
            var itemCount = 0;  // 选中的商品种类数
            
            // 遍历所有选中的商品复选框
            $('#cartItemsContainer .item-checkbox:checked').each(function() {
                var card = $(this).closest('.cart-item-card');
                total += parseFloat(card.data('subtotal'));  // 累加小计金额
                count += parseInt(card.find('.qty-input').val());  // 累加数量
                itemCount++;
            });

            var discount = selectedCouponDiscount || 0;  // 优惠券减免金额
            var finalTotal = Math.max(0, total - discount);  // 计算最终金额（不能为负数）

            // 更新页面显示
            $('#selectedCount').text(count);
            $('#settlementTotal').text(finalTotal.toFixed(2));
            $('#summaryCount').text(count + ' 件');
            $('#summarySubtotal').text('\uFFE5' + total.toFixed(2));
            $('#summaryTotal').text('\uFFE5' + finalTotal.toFixed(2));

            // 显示/隐藏优惠减免行
            if (discount > 0) {
                $('#summaryDiscountRow').show();
                $('#summaryDiscount').text('-\uFFE5' + discount.toFixed(2));
            } else {
                $('#summaryDiscountRow').hide();
            }

            // 更新全选复选框状态
            var checkedCount = $('#cartItemsContainer .item-checkbox').length;
            var selectedCount = $('#cartItemsContainer .item-checkbox:checked').length;
            $('#settlementSelectAll').prop('checked', selectedCount === checkedCount && checkedCount > 0);
            
            // 没有选中商品时禁用结算按钮
            if (selectedCount === 0) {
                $('#btnSettle').css({opacity: 0.4, pointerEvents: 'none'});
            } else {
                $('#btnSettle').css({opacity: 1, pointerEvents: 'auto'});
            }
        }

        // 全选复选框事件
        $('#settlementSelectAll').on('change', function() {
            $('#cartItemsContainer .item-checkbox').prop('checked', this.checked);
            updateSettlement();
        });

        // 单个商品复选框事件
        $(document).on('change', '.item-checkbox', function() {
            var card = $(this).closest('.cart-item-card');
            if (this.checked) {
                card.addClass('selected');
            } else {
                card.removeClass('selected');
            }
            updateSettlement();
        });

        // 数量增减按钮事件
        $(document).on('click', '.qty-minus, .qty-plus', function() {
            var input = $(this).siblings('.qty-input');
            var pid = input.data('pid');  // 商品ID
            var price = parseFloat(input.data('price'));  // 商品单价
            var currentVal = parseInt(input.val());  // 当前数量
            var newVal = $(this).hasClass('qty-plus') ? currentVal + 1 : currentVal - 1;  // 计算新数量
            if (newVal < 1) return;  // 数量不能小于1

            var btn = $(this);
            // 发送AJAX请求更新购物车数量
            $.ajax({
                url: '${pageContext.request.contextPath}/cart/update',
                type: 'POST',
                data: { productId: pid, quantity: newVal },
                success: function() {
                    input.val(newVal);  // 更新输入框值
                    var subtotal = (price * newVal).toFixed(2);  // 计算新小计
                    var card = input.closest('.cart-item-card');
                    card.find('.cart-subtotal').text(subtotal);  // 更新小计显示
                    card.data('subtotal', parseFloat(subtotal));  // 更新数据属性

                    card.find('.qty-minus').prop('disabled', newVal <= 1);  // 数量为1时禁用减号
                    updateSettlement();  // 更新结算栏
                },
                error: function() {
                    alert('更新数量失败，请重试');
                }
            });
        });

        updateSettlement();  // 初始化结算栏

        // 已登录用户加载优惠券列表
        <c:if test="${not empty sessionScope.user}">
        loadCoupons();
        </c:if>
    });

    /**
     * 加载用户优惠券列表
     * 通过AJAX获取用户的可用优惠券并显示在侧边栏
     */
    function loadCoupons() {
        $.ajax({
            url: '${pageContext.request.contextPath}/api/coupon/my',
            type: 'GET',
            dataType: 'json',
            success: function(data) {
                $('#couponLoading').hide();
                if (data.success && data.coupons && data.coupons.length > 0) {
                    var list = $('#couponList');
                    list.empty();
                    // 遍历优惠券数据，创建优惠券列表项
                    $.each(data.coupons, function(i, c) {
                        var desc = '';
                        // 根据优惠券类型显示不同描述
                        if (c.type === 1 || c.type === '1') {
                            desc = '满' + c.threshold + '减' + c.discount;  // 满减券
                        } else {
                            desc = '直减' + c.discount + '元';  // 直减券
                        }
                        var discountText = c.type === 1 || c.type === '1' ? (c.discount + '元') : (c.discount + '元');
                        // 创建优惠券DOM元素
                        var item = $('<div class="coupon-item" data-coupon-id="' + c.id + '" data-discount="' + c.discount + '" data-threshold="' + (c.threshold || 0) + '"></div>');
                        item.html('<div class="coupon-item-icon">' + discountText + '</div><div class="coupon-item-info"><div class="coupon-item-name">' + (c.name || '优惠券') + '</div><div class="coupon-item-desc">' + desc + '</div></div><input type="radio" class="coupon-item-radio" name="couponSelect" />');
                        // 绑定点击事件
                        item.on('click', function() {
                            selectCoupon($(this), c.id, parseFloat(c.discount), parseFloat(c.threshold || 0));
                        });
                        list.append(item);
                    });
                    list.show();
                } else {
                    $('#couponEmpty').show();  // 显示无优惠券提示
                }
            },
            error: function() {
                $('#couponLoading').hide();
                $('#couponEmpty').show();
            }
        });
    }

    function selectCoupon(el, couponId, discount, threshold) {
        var total = 0;
        $('#cartItemsContainer .item-checkbox:checked').each(function() {
            total += parseFloat($(this).closest('.cart-item-card').data('subtotal'));
        });

        if (el.hasClass('active')) {
            el.removeClass('active');
            el.find('.coupon-item-radio').prop('checked', false);
            selectedCouponId = null;
            selectedCouponDiscount = 0;
            $('#couponDiscountInfo').hide();
            $('#couponMsg').text('').removeClass('success error');
        } else {
            $('.coupon-item').removeClass('active').find('.coupon-item-radio').prop('checked', false);
            el.addClass('active');
            el.find('.coupon-item-radio').prop('checked', true);

            if (threshold > 0 && total < threshold) {
                $('#couponMsg').text('满' + threshold + '元可用，还差' + (threshold - total).toFixed(2) + '元').addClass('error').removeClass('success');
                selectedCouponId = null;
                selectedCouponDiscount = 0;
                el.removeClass('active');
                el.find('.coupon-item-radio').prop('checked', false);
                $('#couponDiscountInfo').hide();
            } else {
                selectedCouponId = couponId;
                selectedCouponDiscount = discount;
                $('#couponDiscountInfo').show();
                $('#couponDiscountAmount').text('-\uFFE5' + discount.toFixed(2));
                $('#couponMsg').text('优惠券已选择').addClass('success').removeClass('error');
            }
        }
        updateSettlementGlobal();
    }

    function updateSettlementGlobal() {
        var total = 0;
        var count = 0;
        $('#cartItemsContainer .item-checkbox:checked').each(function() {
            var card = $(this).closest('.cart-item-card');
            total += parseFloat(card.data('subtotal'));
            count += parseInt(card.find('.qty-input').val());
        });

        var discount = selectedCouponDiscount || 0;
        var finalTotal = Math.max(0, total - discount);

        $('#selectedCount').text(count);
        $('#settlementTotal').text(finalTotal.toFixed(2));
        $('#summaryCount').text(count + ' 件');
        $('#summarySubtotal').text('\uFFE5' + total.toFixed(2));
        $('#summaryTotal').text('\uFFE5' + finalTotal.toFixed(2));

        if (discount > 0) {
            $('#summaryDiscountRow').show();
            $('#summaryDiscount').text('-\uFFE5' + discount.toFixed(2));
        } else {
            $('#summaryDiscountRow').hide();
        }
    }

    function applyCouponCode() {
        var code = $('#couponCodeInput').val().trim();
        if (!code) {
            $('#couponMsg').text('请输入优惠码').addClass('error').removeClass('success');
            return;
        }
        $('#couponApplyBtn').prop('disabled', true).text('验证中...');
        $.ajax({
            url: '${pageContext.request.contextPath}/api/coupon/validate',
            type: 'GET',
            data: { code: code },
            dataType: 'json',
            success: function(data) {
                $('#couponApplyBtn').prop('disabled', false).text('兑换');
                if (data.valid) {
                    var discount = parseFloat(data.discount);
                    var threshold = parseFloat(data.threshold || 0);
                    var total = 0;
                    $('#cartItemsContainer .item-checkbox:checked').each(function() {
                        total += parseFloat($(this).closest('.cart-item-card').data('subtotal'));
                    });
                    if (threshold > 0 && total < threshold) {
                        $('#couponMsg').text('满' + threshold + '元可用，还差' + (threshold - total).toFixed(2) + '元').addClass('error').removeClass('success');
                        return;
                    }
                    selectedCouponDiscount = discount;
                    $('#couponDiscountInfo').show();
                    $('#couponDiscountAmount').text('-\uFFE5' + discount.toFixed(2));
                    $('#couponMsg').text(data.name + ' 已生效').addClass('success').removeClass('error');
                    updateSettlementGlobal();
                } else {
                    $('#couponMsg').text(data.message || '优惠码无效').addClass('error').removeClass('success');
                }
            },
            error: function() {
                $('#couponApplyBtn').prop('disabled', false).text('兑换');
                $('#couponMsg').text('验证失败，请重试').addClass('error').removeClass('success');
            }
        });
    }
</script>

</body>
</html>
