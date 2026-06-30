package com.bookstore.agent.mapper; // mapper 包：存放 MyBatis-Plus 数据访问层接口，负责与数据库交互

// 导入 MyBatis-Plus 的 BaseMapper 接口，提供内置的通用 CRUD 方法：insert、deleteById、selectById、selectList 等
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 导入对应的实体类 ChatHistory，作为泛型参数告诉 BaseMapper 操作的是哪张表
import com.bookstore.agent.entity.ChatHistory;
// 导入 MyBatis 的 @Mapper 注解，标记此接口为 MyBatis Mapper，会被 Spring 扫描并生成代理实现
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记录 Mapper 接口 — 基于 MyBatis-Plus 的自动 CRUD
 *
 * 功能说明：
 *   BaseMapper<ChatHistory> 提供了 17+ 内置方法，无需编写任何 SQL 即可完成：
 *     - insert(ChatHistory entity)：插入一条聊天记录
 *     - deleteById(Serializable id)：根据主键删除
 *     - selectById(Serializable id)：根据主键查询
 *     - selectList(Wrapper<ChatHistory> wrapper)：条件查询（如按 sessionId 查询所有消息）
 *     - selectPage(IPage<ChatHistory> page, Wrapper<ChatHistory> wrapper)：分页查询
 *
 * 使用方式：
 *   在 ChatMemoryService 中通过 @Autowired 注入，调用继承的方法即可操作 chat_history 表。
 *   如需自定义复杂查询（如联表、聚合），可在此接口中添加 @Select/@Update 等方法声明。
 *
 * MyBatis-Plus 原理：
 *   Spring 启动时扫描 @Mapper 注解 → 通过 JDK 动态代理生成实现类 → 注入到 Spring 容器
 */
@Mapper // MyBatis 注解：标记此接口为 Mapper，Spring Boot 启动时会自动扫描并创建代理 Bean
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> { // 继承 BaseMapper<ChatHistory> 获得 CRUD 能力，泛型参数 ChatHistory 指定操作的实体类型
}
