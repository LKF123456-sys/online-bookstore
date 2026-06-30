package com.bookstore.agent.mapper; // mapper 包：存放 MyBatis-Plus 数据访问层接口，负责与数据库交互

// 导入 MyBatis-Plus 的 BaseMapper 接口，提供内置的通用 CRUD 方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 导入对应的实体类 ChatSession，作为泛型参数告诉 BaseMapper 操作的是 chat_session 表
import com.bookstore.agent.entity.ChatSession;
// 导入 MyBatis 的 @Mapper 注解，标记此接口为 MyBatis Mapper
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话 Mapper 接口 — 基于 MyBatis-Plus 的自动 CRUD
 *
 * 功能说明：
 *   BaseMapper<ChatSession> 提供了完整的单表 CRUD 方法，典型使用场景包括：
 *     - insert(ChatSession session)：创建新会话（ChatMemoryService.ensureSession 中调用）
 *     - selectById(String sessionId)：根据 UUID 主键查询会话
 *     - selectList(Wrapper<ChatSession> wrapper)：按 userId 查询用户的所有会话列表
 *     - updateById(ChatSession session)：更新会话标题或更新时间
 *     - deleteById(String sessionId)：删除整个会话（及其关联的消息记录）
 *
 * 主键处理：
 *   由于 ChatSession 使用 IdType.INPUT（UUID 由应用生成），insert 前需要：
 *   session.setSessionId(UUID.randomUUID().toString());
 *
 * 与 ChatHistoryMapper 的关系：
 *   两者协同工作：先通过 ChatSessionMapper 管理会话元信息，
 *   再通过 ChatHistoryMapper 管理会话内的具体消息。
 */
@Mapper // MyBatis 注解：标记此接口为 Mapper，Spring Boot 启动时自动扫描并创建代理 Bean
public interface ChatSessionMapper extends BaseMapper<ChatSession> { // 继承 BaseMapper<ChatSession> 获得 CRUD 能力，泛型参数 ChatSession 指定操作的实体类型
}
