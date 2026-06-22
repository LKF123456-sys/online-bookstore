-- ============================================================
-- V5: 微服务数据库权限隔离
-- ============================================================
-- 设计原则：每个微服务使用独立的数据库账号，仅授权其拥有的表（最小权限原则）
-- 物理上共享同一个 MySQL 实例（降低运维成本），逻辑上通过权限隔离实现 database-per-service 的效果
-- 这样即使某个服务被攻破，攻击者也无法访问其他服务的数据
-- ============================================================

-- ==================== 创建服务专属账号 ====================

-- 用户服务账号：仅能访问 account 表
CREATE USER IF NOT EXISTS 'bookstore_user'@'%' IDENTIFIED BY 'BookStore@User2024';

-- 商品服务账号：仅能访问商品相关的 4 张表
CREATE USER IF NOT EXISTS 'bookstore_product'@'%' IDENTIFIED BY 'BookStore@Product2024';

-- 订单服务账号：仅能访问订单相关的 4 张表
CREATE USER IF NOT EXISTS 'bookstore_order'@'%' IDENTIFIED BY 'BookStore@Order2024';

-- 营销服务账号：仅能访问营销相关的 4 张表
CREATE USER IF NOT EXISTS 'bookstore_promotion'@'%' IDENTIFIED BY 'BookStore@Promotion2024';

-- 消息服务账号：仅能访问 message 表
CREATE USER IF NOT EXISTS 'bookstore_message'@'%' IDENTIFIED BY 'BookStore@Message2024';

-- 管理后台账号：仅能读写 admin_log 表（BFF 层，其他数据通过 Feign 调用获取）
CREATE USER IF NOT EXISTS 'bookstore_admin'@'%' IDENTIFIED BY 'BookStore@Admin2024';

-- AI 智能体账号：仅能读写聊天相关的 2 张表（Agent 层，其他数据通过 Feign 调用获取）
CREATE USER IF NOT EXISTS 'bookstore_agent'@'%' IDENTIFIED BY 'BookStore@Agent2024';

-- ==================== 授权（最小权限原则） ====================

-- 用户服务（bookstore-user）：account 表的完整 CRUD 权限
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.account TO 'bookstore_user'@'%';

-- 商品服务（bookstore-product）：商品、分类、SKU、规格 4 张表的完整 CRUD 权限
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.product TO 'bookstore_product'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.category TO 'bookstore_product'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.product_sku TO 'bookstore_product'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.product_spec TO 'bookstore_product'@'%';

-- 订单服务（bookstore-order）：订单、订单明细、购物车、购物车项 4 张表的完整 CRUD 权限
-- 注意：订单服务通过 Feign 调用商品服务获取商品信息，不直接访问 product 表
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.orders TO 'bookstore_order'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.order_item TO 'bookstore_order'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.cart TO 'bookstore_order'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.cartitem TO 'bookstore_order'@'%';

-- 营销服务（bookstore-promotion）：优惠券、用户优惠券、书评、公告 4 张表的完整 CRUD 权限
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.coupon TO 'bookstore_promotion'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.user_coupon TO 'bookstore_promotion'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.book_review TO 'bookstore_promotion'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.announcement TO 'bookstore_promotion'@'%';

-- 消息服务（bookstore-message）：message 表的完整 CRUD 权限
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.message TO 'bookstore_message'@'%';

-- 管理后台（bookstore-admin）：admin_log 表的只读 + 写入权限（仅记录操作日志，不需要修改和删除）
GRANT SELECT, INSERT ON bookstore.admin_log TO 'bookstore_admin'@'%';

-- AI 智能体（bookstore-agent）：聊天会话和历史记录表的完整 CRUD 权限
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.chat_session TO 'bookstore_agent'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookstore.chat_history TO 'bookstore_agent'@'%';

-- 刷新权限缓存，使上述授权立即生效
FLUSH PRIVILEGES;
