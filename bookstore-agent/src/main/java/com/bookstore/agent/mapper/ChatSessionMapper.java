package com.bookstore.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.agent.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话 Mapper — MyBatis-Plus 自动 CRUD
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
