-- ============================================================
-- V4 迁移脚本 — AI 智能体对话表
-- 创建 chat_session 和 chat_history 表，用于存储 AI 对话记录
-- ============================================================

-- 聊天会话表：记录每次对话会话的元信息
CREATE TABLE IF NOT EXISTS chat_session (
    session_id  VARCHAR(64)   NOT NULL  COMMENT '会话ID（UUID）',
    user_id     VARCHAR(64)   NOT NULL  COMMENT '所属用户ID',
    title       VARCHAR(200)  DEFAULT '新对话' COMMENT '会话标题',
    create_time DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 对话会话表';

-- 聊天历史表：存储每一条对话消息
CREATE TABLE IF NOT EXISTS chat_history (
    id          BIGINT        NOT NULL  AUTO_INCREMENT COMMENT '主键ID',
    session_id  VARCHAR(64)   NOT NULL  COMMENT '所属会话ID',
    role        VARCHAR(20)   NOT NULL  COMMENT '角色：user/assistant/system',
    content     TEXT          NOT NULL  COMMENT '消息内容',
    agent_name  VARCHAR(50)   DEFAULT NULL COMMENT 'Agent名称（仅assistant消息有值）',
    create_time DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_session_id (session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 对话历史记录表';
