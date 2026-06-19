package com.bookstore.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话实体 — 对应 chat_session 表
 * 记录每个对话会话的元信息
 */
@Data
@TableName("chat_session")
public class ChatSession {

    /** 会话ID（UUID，主键） */
    @TableId(type = IdType.INPUT)
    private String sessionId;

    /** 所属用户ID */
    private String userId;

    /** 会话标题（自动取第一条用户消息的前20个字符，或用户自定义） */
    private String title;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;
}
