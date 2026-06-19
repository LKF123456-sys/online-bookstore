-- ============================================================
-- Testcontainers 测试用表结构初始化
-- 仅包含订单服务集成测试所需的最小表集合
-- ============================================================

CREATE TABLE IF NOT EXISTS `orders` (
    `orderid` VARCHAR(64) PRIMARY KEY,
    `userid` VARCHAR(64) NOT NULL,
    `orderdate` DATETIME,
    `totalprice` DECIMAL(10,2),
    `originalprice` DECIMAL(10,2),
    `discountamount` DECIMAL(10,2) DEFAULT 0,
    `couponname` VARCHAR(128),
    `status` VARCHAR(32),
    `billtofirstname` VARCHAR(50),
    `billtolastname` VARCHAR(50),
    `billaddr1` VARCHAR(200),
    `billaddr2` VARCHAR(200),
    `billcity` VARCHAR(100),
    `billstate` VARCHAR(100),
    `billzip` VARCHAR(20),
    `billcountry` VARCHAR(100),
    `shiptofirstname` VARCHAR(50),
    `shiptolastname` VARCHAR(50),
    `shipaddr1` VARCHAR(200),
    `shipaddr2` VARCHAR(200),
    `shipcity` VARCHAR(100),
    `shipstate` VARCHAR(100),
    `shipzip` VARCHAR(20),
    `shipcountry` VARCHAR(100),
    `courier` VARCHAR(100),
    `creditcard` VARCHAR(32),
    `exprdate` VARCHAR(16),
    `cardtype` VARCHAR(30),
    `locale` VARCHAR(32),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` VARCHAR(64) NOT NULL,
    `product_id` VARCHAR(64) NOT NULL,
    `product_name` VARCHAR(200),
    `quantity` INT NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `compensation_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` VARCHAR(64) NOT NULL,
    `product_id` VARCHAR(64) NOT NULL,
    `quantity` INT NOT NULL,
    `type` VARCHAR(32) NOT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    `retry_count` INT NOT NULL DEFAULT 0,
    `max_retries` INT NOT NULL DEFAULT 5,
    `error_message` VARCHAR(512),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
