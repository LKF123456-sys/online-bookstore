package com.bookstore.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天记录实体 — 对应 chat_history 表
 * 存储每一条对话消息的完整记录，用于历史查询和数据分析
 */
@Data
@TableName("chat_history")
public class ChatHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID，标识一次连续对话 */
    private String sessionId;

    /** 角色：user（用户）/ assistant（助手）/ system（系统） */
    private String role;

    /** 消息内容（用户输入或 AI 回复的完整文本） */
    private String content;

    /** Agent 名称（仅 assistant 消息有值，如"客服助手"、"图书推荐顾问"） */
    private String agentName;

    /** 创建时间 */
    private LocalDateTime createTime;
}
