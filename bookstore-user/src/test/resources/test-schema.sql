-- ============================================================
-- Testcontainers 测试用表结构初始化（用户服务）
-- ============================================================

CREATE TABLE IF NOT EXISTS `account` (
    `userid`     VARCHAR(50)  NOT NULL COMMENT '用户名（主键）',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱',
    `firstname`  VARCHAR(50)  DEFAULT '' COMMENT '名',
    `lastname`   VARCHAR(50)  DEFAULT '' COMMENT '姓',
    `status`     INT          DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `addr1`      VARCHAR(200) DEFAULT '' COMMENT '地址1',
    `addr2`      VARCHAR(200) DEFAULT '' COMMENT '地址2',
    `city`       VARCHAR(100) DEFAULT '' COMMENT '城市',
    `state`      VARCHAR(100) DEFAULT '' COMMENT '省份',
    `zip`        VARCHAR(20)  DEFAULT '' COMMENT '邮编',
    `country`    VARCHAR(100) DEFAULT '' COMMENT '国家',
    `phone`      VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `role`       VARCHAR(20)  DEFAULT 'user' COMMENT '角色：user/admin',
    `avatar`     VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
