package com.bookstore.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.agent.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记录 Mapper — MyBatis-Plus 自动 CRUD
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
}
