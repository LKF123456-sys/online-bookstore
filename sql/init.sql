-- ============================================================
-- BookVerse 在线书店 - 数据库初始化脚本
-- 数据库: bookstore
-- MySQL 版本: 8.0+
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS bookstore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bookstore;

-- ============================================================
-- 1. 用户账户表
-- ============================================================
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
    `userid`     VARCHAR(50)  NOT NULL COMMENT '用户名(主键)',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱',
    `firstname`  VARCHAR(50)  DEFAULT '' COMMENT '名',
    `lastname`   VARCHAR(50)  DEFAULT '' COMMENT '姓',
    `password`   VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    `status`     TINYINT      DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    `addr1`      VARCHAR(200) DEFAULT '' COMMENT '地址行1',
    `addr2`      VARCHAR(200) DEFAULT '' COMMENT '地址行2',
    `city`       VARCHAR(50)  DEFAULT '' COMMENT '城市',
    `state`      VARCHAR(50)  DEFAULT '' COMMENT '省份/州',
    `zip`        VARCHAR(20)  DEFAULT '' COMMENT '邮编',
    `country`    VARCHAR(50)  DEFAULT '中国' COMMENT '国家',
    `phone`      VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    `role`       VARCHAR(20)  DEFAULT 'user' COMMENT '角色: user/admin',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `avatar`     VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    PRIMARY KEY (`userid`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账户表';

-- ============================================================
-- 2. 商品分类表
-- ============================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `categoryid`   VARCHAR(50)  NOT NULL COMMENT '分类ID',
    `categoryname` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `categorydesc` VARCHAR(500) DEFAULT '' COMMENT '分类描述',
    PRIMARY KEY (`categoryid`),
    KEY `idx_name` (`categoryname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ============================================================
-- 3. 商品表
-- ============================================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `productid`    VARCHAR(50)    NOT NULL COMMENT '商品ID',
    `category`     VARCHAR(50)    NOT NULL COMMENT '分类ID(FK->category)',
    `name`         VARCHAR(200)   NOT NULL COMMENT '商品名称',
    `descn`        TEXT           COMMENT '商品描述(HTML)',
    `author`       VARCHAR(100)   DEFAULT '' COMMENT '作者',
    `price`        DECIMAL(10,2)  NOT NULL COMMENT '售价',
    `image`        VARCHAR(500)   DEFAULT '' COMMENT '封面图片URL',
    `stock`        INT            DEFAULT 0 COMMENT '库存数量',
    `sales`        INT            DEFAULT 0 COMMENT '销量',
    `is_recommend` TINYINT       DEFAULT 0 COMMENT '是否推荐: 0=否 1=是',
    `status`       TINYINT       DEFAULT 1 COMMENT '状态: 0=下架 1=上架',
    PRIMARY KEY (`productid`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_sales` (`sales`),
    KEY `idx_price` (`price`),
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category`) REFERENCES `category` (`categoryid`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================================
-- 4. 商品SKU表
-- ============================================================
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
    `id`         BIGINT         AUTO_INCREMENT COMMENT '主键',
    `product_id` VARCHAR(50)    NOT NULL COMMENT '商品ID(FK->product)',
    `sku_name`   VARCHAR(100)   DEFAULT '' COMMENT 'SKU名称',
    `specs`      JSON           COMMENT '规格属性(JSON)',
    `price`      DECIMAL(10,2)  NOT NULL COMMENT '价格',
    `stock`      INT            DEFAULT 0 COMMENT '库存',
    `status`     TINYINT       DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    CONSTRAINT `fk_sku_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`productid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- ============================================================
-- 5. 商品规格表
-- ============================================================
DROP TABLE IF EXISTS `product_spec`;
CREATE TABLE `product_spec` (
    `id`          BIGINT       AUTO_INCREMENT COMMENT '主键',
    `product_id`  VARCHAR(50)  NOT NULL COMMENT '商品ID(FK->product)',
    `spec_name`   VARCHAR(50)  NOT NULL COMMENT '规格名称',
    `spec_values` JSON         COMMENT '规格值数组(JSON)',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    CONSTRAINT `fk_spec_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`productid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表';

-- ============================================================
-- 6. 购物车表
-- ============================================================
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
    `cartid`     VARCHAR(50) NOT NULL COMMENT '购物车ID',
    `userid`     VARCHAR(50) NOT NULL COMMENT '用户ID(FK->account)',
    `created_at` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`cartid`),
    KEY `idx_userid` (`userid`),
    CONSTRAINT `fk_cart_account` FOREIGN KEY (`userid`) REFERENCES `account` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================================
-- 7. 购物车项表
-- ============================================================
DROP TABLE IF EXISTS `cartitem`;
CREATE TABLE `cartitem` (
    `itemid`    VARCHAR(50) NOT NULL COMMENT '购物车项ID',
    `cartid`    VARCHAR(50) NOT NULL COMMENT '购物车ID(FK->cart)',
    `productid` VARCHAR(50) NOT NULL COMMENT '商品ID(FK->product)',
    `quantity`  INT         DEFAULT 1 COMMENT '数量',
    PRIMARY KEY (`itemid`),
    KEY `idx_cartid` (`cartid`),
    KEY `idx_productid` (`productid`),
    CONSTRAINT `fk_cartitem_cart` FOREIGN KEY (`cartid`) REFERENCES `cart` (`cartid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_cartitem_product` FOREIGN KEY (`productid`) REFERENCES `product` (`productid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车项表';

-- ============================================================
-- 8. 订单表
-- ============================================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `orderid`          VARCHAR(50)    NOT NULL COMMENT '订单号',
    `userid`           VARCHAR(50)    NOT NULL COMMENT '用户ID(FK->account)',
    `orderdate`        DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `totalprice`       DECIMAL(10,2)  NOT NULL COMMENT '实付金额',
    `originalprice`    DECIMAL(10,2)  DEFAULT 0 COMMENT '原始金额',
    `discountamount`   DECIMAL(10,2)  DEFAULT 0 COMMENT '优惠金额',
    `couponname`       VARCHAR(100)   DEFAULT '' COMMENT '使用的优惠券名称',
    `status`           VARCHAR(20)    DEFAULT 'pending' COMMENT '状态: pending/paid/shipped/completed/cancelled',
    `billtofirstname`  VARCHAR(50)    DEFAULT '' COMMENT '账单-名',
    `billtolastname`   VARCHAR(50)    DEFAULT '' COMMENT '账单-姓',
    `billaddr1`        VARCHAR(200)   DEFAULT '' COMMENT '账单地址1',
    `billaddr2`        VARCHAR(200)   DEFAULT '' COMMENT '账单地址2',
    `billcity`         VARCHAR(50)    DEFAULT '' COMMENT '账单城市',
    `billstate`        VARCHAR(50)    DEFAULT '' COMMENT '账单省份',
    `billzip`          VARCHAR(20)    DEFAULT '' COMMENT '账单邮编',
    `billcountry`      VARCHAR(50)    DEFAULT '' COMMENT '账单国家',
    `shipaddr1`        VARCHAR(200)   DEFAULT '' COMMENT '收货地址1',
    `shipaddr2`        VARCHAR(200)   DEFAULT '' COMMENT '收货地址2',
    `shipcity`         VARCHAR(50)    DEFAULT '' COMMENT '收货城市',
    `shipstate`        VARCHAR(50)    DEFAULT '' COMMENT '收货省份',
    `shipzip`          VARCHAR(20)    DEFAULT '' COMMENT '收货邮编',
    `shipcountry`      VARCHAR(50)    DEFAULT '' COMMENT '收货国家',
    `shiptofirstname`  VARCHAR(50)    DEFAULT '' COMMENT '收货人-名',
    `shiptolastname`   VARCHAR(50)    DEFAULT '' COMMENT '收货人-姓',
    `courier`          VARCHAR(50)    DEFAULT '' COMMENT '快递公司',
    `creditcard`       VARCHAR(50)    DEFAULT '' COMMENT '支付方式标识(脱敏)',
    `exprdate`         VARCHAR(20)    DEFAULT '' COMMENT '有效期',
    `cardtype`         VARCHAR(50)    DEFAULT '' COMMENT '支付类型',
    `locale`           VARCHAR(20)    DEFAULT 'zh_CN' COMMENT '语言区域',
    PRIMARY KEY (`orderid`),
    KEY `idx_userid` (`userid`),
    KEY `idx_status` (`status`),
    KEY `idx_orderdate` (`orderdate`),
    CONSTRAINT `fk_orders_account` FOREIGN KEY (`userid`) REFERENCES `account` (`userid`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 9. 订单项表
-- ============================================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id`           BIGINT         AUTO_INCREMENT COMMENT '主键',
    `order_id`     VARCHAR(50)    NOT NULL COMMENT '订单号(FK->orders)',
    `product_id`   VARCHAR(50)    NOT NULL COMMENT '商品ID(FK->product)',
    `product_name` VARCHAR(200)   DEFAULT '' COMMENT '商品名称(冗余)',
    `quantity`     INT            NOT NULL COMMENT '数量',
    `price`        DECIMAL(10,2)  NOT NULL COMMENT '购买时单价',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`),
    CONSTRAINT `fk_orderitem_orders` FOREIGN KEY (`order_id`) REFERENCES `orders` (`orderid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_orderitem_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`productid`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- ============================================================
-- 10. 优惠券表
-- ============================================================
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
    `id`          BIGINT         AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(100)   NOT NULL COMMENT '优惠券名称',
    `type`        VARCHAR(20)    NOT NULL COMMENT '类型: 满减/折扣',
    `threshold`   DECIMAL(10,2)  DEFAULT 0 COMMENT '使用门槛金额',
    `discount`    DECIMAL(10,2)  NOT NULL COMMENT '优惠金额/折扣值',
    `total_count` INT            DEFAULT 0 COMMENT '发放总量',
    `used_count`  INT            DEFAULT 0 COMMENT '已领取数量',
    `start_time`  DATETIME       NOT NULL COMMENT '有效期开始',
    `end_time`    DATETIME       NOT NULL COMMENT '有效期结束',
    `status`      TINYINT       DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- ============================================================
-- 11. 用户优惠券关联表
-- ============================================================
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
    `id`        BIGINT    AUTO_INCREMENT COMMENT '主键',
    `user_id`   VARCHAR(50) NOT NULL COMMENT '用户ID(FK->account)',
    `coupon_id` BIGINT      NOT NULL COMMENT '优惠券ID(FK->coupon)',
    `is_used`   TINYINT   DEFAULT 0 COMMENT '是否使用: 0=未用 1=已用',
    `grant_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time`   DATETIME DEFAULT NULL COMMENT '使用时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
    CONSTRAINT `fk_usercoupon_account` FOREIGN KEY (`user_id`) REFERENCES `account` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_usercoupon_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券关联表';

-- ============================================================
-- 12. 图书评价表
-- ============================================================
DROP TABLE IF EXISTS `book_review`;
CREATE TABLE `book_review` (
    `id`          BIGINT       AUTO_INCREMENT COMMENT '主键',
    `order_id`    VARCHAR(50)  DEFAULT '' COMMENT '订单号',
    `product_id`  VARCHAR(50)  NOT NULL COMMENT '商品ID(FK->product)',
    `user_id`     VARCHAR(50)  NOT NULL COMMENT '用户ID(FK->account)',
    `rating`      TINYINT     DEFAULT 5 COMMENT '评分(1-5星)',
    `content`     TEXT         COMMENT '评价内容',
    `image`       VARCHAR(500) DEFAULT '' COMMENT '评价图片URL',
    `likes`       INT          DEFAULT 0 COMMENT '点赞数',
    `is_top`      TINYINT     DEFAULT 0 COMMENT '是否置顶: 0=否 1=是',
    `reply`       TEXT         COMMENT '商家回复',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `blocked`     TINYINT     DEFAULT 0 COMMENT '是否屏蔽: 0=正常 1=屏蔽',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`productid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_review_account` FOREIGN KEY (`user_id`) REFERENCES `account` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书评价表';

-- ============================================================
-- 13. 站内消息表
-- ============================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`            BIGINT       AUTO_INCREMENT COMMENT '主键',
    `sender_id`     VARCHAR(50)  NOT NULL COMMENT '发送者ID',
    `sender_type`   VARCHAR(20)  DEFAULT 'user' COMMENT '发送者类型: user/admin/system',
    `receiver_id`   VARCHAR(50)  NOT NULL COMMENT '接收者ID',
    `receiver_type` VARCHAR(20)  DEFAULT 'user' COMMENT '接收者类型: user/admin',
    `content`       TEXT         NOT NULL COMMENT '消息内容',
    `read_status`   TINYINT     DEFAULT 0 COMMENT '已读状态: 0=未读 1=已读',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_receiver` (`receiver_id`, `receiver_type`),
    KEY `idx_sender` (`sender_id`, `sender_type`),
    KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息表';

-- ============================================================
-- 14. 系统公告表
-- ============================================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`         BIGINT       AUTO_INCREMENT COMMENT '主键',
    `title`      VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content`    TEXT         NOT NULL COMMENT '公告内容',
    `status`     TINYINT     DEFAULT 1 COMMENT '状态: 0=草稿 1=已发布',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- ============================================================
-- 15. 管理员操作日志表
-- ============================================================
DROP TABLE IF EXISTS `admin_log`;
CREATE TABLE `admin_log` (
    `id`          BIGINT       AUTO_INCREMENT COMMENT '主键',
    `admin_name`  VARCHAR(50)  NOT NULL COMMENT '管理员用户名',
    `operation`   VARCHAR(50)  NOT NULL COMMENT '操作类型',
    `target`      VARCHAR(200) DEFAULT '' COMMENT '操作对象',
    `detail`      TEXT         COMMENT '操作详情',
    `ip`          VARCHAR(50)  DEFAULT '' COMMENT '操作IP',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_admin_name` (`admin_name`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';


-- ============================================================
-- 种子数据
-- ============================================================

-- 管理员账号 (密码: admin123)
INSERT INTO `account` (`userid`, `email`, `firstname`, `lastname`, `password`, `role`, `status`, `phone`, `addr1`, `city`, `country`)
VALUES ('admin', 'admin@bookverse.com', '系统', '管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin', 1, '13800000000', '科技园路1号', '北京', '中国');

-- 测试用户 (密码: 123456)
INSERT INTO `account` (`userid`, `email`, `firstname`, `lastname`, `password`, `role`, `status`, `phone`, `addr1`, `city`, `country`)
VALUES ('testuser', 'test@bookverse.com', '测试', '用户', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'user', 1, '13900000001', '中关村大街100号', '北京', '中国');

INSERT INTO `account` (`userid`, `email`, `firstname`, `lastname`, `password`, `role`, `status`, `phone`, `addr1`, `city`, `country`)
VALUES ('zhangsan', 'zhangsan@example.com', '三', '张', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'user', 1, '13900000002', '南京西路200号', '上海', '中国');

-- 商品分类
INSERT INTO `category` VALUES ('CAT001', '文学小说', '经典文学、现当代小说、外国文学等');
INSERT INTO `category` VALUES ('CAT002', '科技计算机', '编程开发、人工智能、计算机科学等');
INSERT INTO `category` VALUES ('CAT003', '历史人文', '中国历史、世界历史、哲学思想等');
INSERT INTO `category` VALUES ('CAT004', '经济管理', '经济学、企业管理、投资理财等');
INSERT INTO `category` VALUES ('CAT005', '生活百科', '健康养生、旅行、美食等');
INSERT INTO `category` VALUES ('CAT006', '少儿读物', '绘本、童话故事、少儿科普等');

-- 商品数据
INSERT INTO `product` (`productid`, `category`, `name`, `descn`, `author`, `price`, `image`, `stock`, `sales`, `is_recommend`, `status`) VALUES
('P001', 'CAT001', '百年孤独', '魔幻现实主义文学的代表作，描写了布恩迪亚家族七代人的传奇故事和加勒比海沿岸小镇马孔多的百年兴衰。', '加西亚·马尔克斯', 55.00, '/img/books/bainian.jpg', 120, 856, 1, 1),
('P002', 'CAT001', '活着', '一个人和他命运之间的友情，讲述人如何去承受巨大的苦难，讲述了眼泪的宽广和丰富。', '余华', 28.00, '/img/books/huozhe.jpg', 200, 2341, 1, 1),
('P003', 'CAT001', '三体', '地球文明向宇宙发出的第一声啼哭，引来了三体世界的注视，人类与三体文明的史诗博弈就此展开。', '刘慈欣', 93.00, '/img/books/santi.jpg', 85, 3102, 1, 1),
('P004', 'CAT002', '深入理解计算机系统', '从程序员的视角详细阐述计算机系统的经典教材，覆盖数据表示、程序机器级表示、处理器体系结构等核心内容。', 'Randal E.Bryant', 139.00, '/img/books/csapp.jpg', 45, 423, 1, 1),
('P005', 'CAT002', 'Java核心技术 第12版', 'Java领域最有影响力和最有价值的著作之一，涵盖Java SE全部核心API和高级特性。', '凯·霍斯特曼', 119.00, '/img/books/java.jpg', 78, 687, 0, 1),
('P006', 'CAT002', '算法导论', '全面介绍计算机算法的理论和实践，是MIT等全球顶级高校的经典教材。', 'Thomas H.Cormen', 128.00, '/img/books/algo.jpg', 30, 215, 0, 1),
('P007', 'CAT002', '设计模式', '软件工程领域最具影响力的著作之一，介绍了23种经典的设计模式。', 'Erich Gamma', 69.00, '/img/books/design.jpg', 55, 389, 0, 1),
('P008', 'CAT003', '人类简史', '从认知革命、农业革命到科学革命，重新审视人类历史的宏大叙事。', '尤瓦尔·赫拉利', 68.00, '/img/books/sapiens.jpg', 150, 1567, 1, 1),
('P009', 'CAT003', '明朝那些事儿', '以通俗易懂的写法讲述明朝三百年间的历史故事，让历史变得鲜活有趣。', '当年明月', 178.00, '/img/books/mingchao.jpg', 90, 934, 0, 1),
('P010', 'CAT004', '穷查理宝典', '查理·芒格的智慧箴言录，涵盖其人生哲学、投资理念和思维模型。', '彼得·考夫曼', 88.00, '/img/books/munger.jpg', 65, 445, 0, 1),
('P011', 'CAT001', '追风筝的人', '关于友谊、背叛、救赎与自我救赎的感人故事，跨越阿富汗三十年的动荡历史。', '卡勒德·胡赛尼', 36.00, '/img/books/kite.jpg', 180, 1876, 1, 1),
('P012', 'CAT001', '白夜行', '一段跨越十九年的凄美爱情故事，背后隐藏着令人震惊的犯罪真相。', '东野圭吾', 42.00, '/img/books/byakuya.jpg', 110, 1234, 0, 1),
('P013', 'CAT002', 'Python编程从入门到实践', '零基础学Python的最佳入门书籍，涵盖基础语法和项目实战。', 'Eric Matthes', 75.00, '/img/books/python.jpg', 95, 812, 1, 1),
('P014', 'CAT005', '断舍离', '通过整理物品来整理内心，让生活变得更加轻松自在的生活哲学。', '山下英子', 32.00, '/img/books/duansheli.jpg', 200, 567, 0, 1),
('P015', 'CAT006', '小王子', '写给大人看的童话，关于爱、责任与生命的意义，全球销量超过2亿册。', '安东尼·德·圣-埃克苏佩里', 25.00, '/img/books/xiaowangzi.jpg', 300, 2089, 1, 1);

-- 优惠券数据
INSERT INTO `coupon` (`name`, `type`, `threshold`, `discount`, `total_count`, `used_count`, `start_time`, `end_time`, `status`)
VALUES ('新人满减券', '满减', 100.00, 20.00, 1000, 120, '2024-01-01 00:00:00', '2026-12-31 23:59:59', 1);
INSERT INTO `coupon` (`name`, `type`, `threshold`, `discount`, `total_count`, `used_count`, `start_time`, `end_time`, `status`)
VALUES ('读书月8折券', '折扣', 50.00, 8.00, 500, 45, '2024-04-01 00:00:00', '2026-12-31 23:59:59', 1);
INSERT INTO `coupon` (`name`, `type`, `threshold`, `discount`, `total_count`, `used_count`, `start_time`, `end_time`, `status`)
VALUES ('满减50元大额券', '满减', 300.00, 50.00, 200, 12, '2024-06-01 00:00:00', '2026-12-31 23:59:59', 1);

-- 公告数据
INSERT INTO `announcement` (`title`, `content`, `status`)
VALUES ('BookVerse 正式上线', '欢迎来到 BookVerse 在线书店！我们致力于为广大读者提供优质的图书和便捷的购书体验。新用户注册即送20元优惠券！', 1);
INSERT INTO `announcement` (`title`, `content`, `status`)
VALUES ('春季读书月活动', '4月读书月来袭！全场满100减20，满300减50，部分图书低至5折，快来选购心仪好书吧！', 1);

-- 测试购物车
INSERT INTO `cart` (`cartid`, `userid`) VALUES ('CART-testuser', 'testuser');
INSERT INTO `cartitem` (`itemid`, `cartid`, `productid`, `quantity`) VALUES ('CI001', 'CART-testuser', 'P003', 1);
INSERT INTO `cartitem` (`itemid`, `cartid`, `productid`, `quantity`) VALUES ('CI002', 'CART-testuser', 'P004', 1);

-- 测试订单
INSERT INTO `orders` (`orderid`, `userid`, `orderdate`, `totalprice`, `originalprice`, `discountamount`, `status`, `billtofirstname`, `billtolastname`, `billaddr1`, `billcity`, `billcountry`, `shipaddr1`, `shipcity`, `shipcountry`, `shiptofirstname`, `shiptolastname`, `courier`, `creditcard`, `cardtype`)
VALUES ('ORD20240101001', 'testuser', '2024-01-15 10:30:00', 121.00, 121.00, 0.00, 'completed', '测试', '用户', '中关村大街100号', '北京', '中国', '中关村大街100号', '北京', '中国', '测试', '用户', '顺丰快递', '****1234', '微信支付');
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`) VALUES ('ORD20240101001', 'P002', '活着', 1, 28.00);
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`) VALUES ('ORD20240101001', 'P004', '深入理解计算机系统', 1, 93.00);

INSERT INTO `orders` (`orderid`, `userid`, `orderdate`, `totalprice`, `originalprice`, `discountamount`, `couponname`, `status`, `billtofirstname`, `billtolastname`, `billaddr1`, `billcity`, `billcountry`, `shipaddr1`, `shipcity`, `shipcountry`, `shiptofirstname`, `shiptolastname`, `courier`, `creditcard`, `cardtype`)
VALUES ('ORD20240201001', 'testuser', '2024-02-20 14:00:00', 73.00, 93.00, 20.00, '新人满减券', 'paid', '测试', '用户', '中关村大街100号', '北京', '中国', '中关村大街100号', '北京', '中国', '测试', '用户', '', '****5678', '支付宝');
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`) VALUES ('ORD20240201001', 'P003', '三体', 1, 93.00);

INSERT INTO `orders` (`orderid`, `userid`, `orderdate`, `totalprice`, `originalprice`, `discountamount`, `status`, `billtofirstname`, `billtolastname`, `billaddr1`, `billcity`, `billcountry`, `shipaddr1`, `shipcity`, `shipcountry`, `shiptofirstname`, `shiptolastname`, `courier`, `creditcard`, `cardtype`)
VALUES ('ORD20240301001', 'zhangsan', '2024-03-10 09:15:00', 104.00, 104.00, 0.00, 'shipped', '三', '张', '南京西路200号', '上海', '中国', '南京西路200号', '上海', '中国', '三', '张', '中通快递', '****9012', '微信支付');
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`) VALUES ('ORD20240301001', 'P011', '追风筝的人', 1, 36.00);
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`) VALUES ('ORD20240301001', 'P008', '人类简史', 1, 68.00);

-- 测试评价
INSERT INTO `book_review` (`product_id`, `user_id`, `order_id`, `rating`, `content`, `create_time`)
VALUES ('P002', 'testuser', 'ORD20240101001', 5, '读完之后久久不能平静，余华用平实的语言讲述了一个震撼人心的故事。强烈推荐！', '2024-01-20 15:30:00');
INSERT INTO `book_review` (`product_id`, `user_id`, `order_id`, `rating`, `content`, `create_time`)
VALUES ('P003', 'testuser', 'ORD20240201001', 5, '想象力惊人，三体世界的世界观让人叹为观止。中国科幻的巅峰之作！', '2024-02-25 20:00:00');
INSERT INTO `book_review` (`product_id`, `user_id`, `order_id`, `rating`, `content`, `create_time`, `reply`)
VALUES ('P008', 'zhangsan', 'ORD20240301001', 4, '从全新的角度审视人类历史，很多观点令人深思。不过部分内容有些过于简化。', '2024-03-15 11:00:00', '感谢您的评价！这本书确实提供了独特的历史视角，适合入门阅读。');

-- 测试消息
INSERT INTO `message` (`sender_id`, `sender_type`, `receiver_id`, `receiver_type`, `content`, `read_status`)
VALUES ('system', 'system', 'testuser', 'user', '欢迎来到BookVerse！您已成功注册，祝您购书愉快！', 1);
INSERT INTO `message` (`sender_id`, `sender_type`, `receiver_id`, `receiver_type`, `content`, `read_status`)
VALUES ('admin', 'admin', 'testuser', 'user', '您的订单 ORD20240201001 已发货，预计3天内送达，请留意查收。', 0);
