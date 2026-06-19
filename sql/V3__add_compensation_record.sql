-- ============================================================
-- V3: 补偿记录表 — 分布式事务补偿追踪
-- 设计模式：Saga 补偿事务持久化
-- ============================================================

CREATE TABLE IF NOT EXISTS `compensation_record` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `order_id`      VARCHAR(64)  NOT NULL COMMENT '关联订单ID',
    `product_id`    VARCHAR(64)  NOT NULL COMMENT '关联商品ID',
    `quantity`      INT          NOT NULL COMMENT '操作数量',
    `type`          VARCHAR(32)  NOT NULL COMMENT '补偿类型: COMPENSATION(库存回补) / RESTORE(库存恢复)',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '补偿状态: PENDING / SUCCESS / FAILED',
    `retry_count`   INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `max_retries`   INT          NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `error_message` VARCHAR(512) NULL COMMENT '最近一次失败原因',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分布式事务补偿记录表';
