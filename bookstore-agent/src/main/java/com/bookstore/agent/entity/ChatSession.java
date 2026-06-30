package com.bookstore.agent.entity; // 声明当前类所在的包：entity 包专门存放数据表对应的实体类

// 导入 MyBatis-Plus 的主键生成策略枚举，INPUT 表示由用户手动输入主键值
import com.baomidou.mybatisplus.annotation.IdType;
// 导入 MyBatis-Plus 的 @TableId 注解，用于标识实体类的主键字段
import com.baomidou.mybatisplus.annotation.TableId;
// 导入 MyBatis-Plus 的 @TableName 注解，用于指定实体类对应的数据库表名
import com.baomidou.mybatisplus.annotation.TableName;
// 导入 Lombok 的 @Data 注解，自动生成 getter/setter/toString/equals/hashCode 方法
import lombok.Data;

// 导入 Java 8 的日期时间类，用于记录创建时间和更新时间
import java.time.LocalDateTime;

/**
 * 聊天会话实体类 — 映射到数据库的 chat_session 表
 *
 * 表设计用途：
 *   1. 记录每次对话会话的元信息（谁、什么时候、什么主题）
 *   2. 支持用户查看自己的历史会话列表
 *   3. 通过 sessionId 关联 chat_history 表中的消息记录
 *
 * 与 ChatHistory 的关系：
 *   一条 ChatSession 对应一次完整对话（1:N 关系），包含多条 ChatHistory 记录。
 *   ChatSession 是「会话级」记录，ChatHistory 是「消息级」记录。
 *
 * 主键策略：
 *   使用 IdType.INPUT，即 sessionId 由应用程序生成（UUID），
 *   而非数据库自增。这样可以确保 Redis 和 MySQL 中使用相同的 sessionId。
 */
@Data // Lombok 注解：编译时自动生成所有字段的 getter、setter 方法
@TableName("chat_session") // MyBatis-Plus 注解：指定此实体类映射到 chat_session 表
public class ChatSession { // 聊天会话实体，每条记录代表一次完整的用户对话

    /**
     * 会话ID — 主键，由应用程序生成
     * - 格式：UUID 随机字符串，如 "a1b2c3d4-e5f6-..."
     * - 在 ChatController 中，如果前端不传 sessionId，则自动生成 UUID
     * - TYPE = INPUT 表示此 ID 由代码生成而非数据库自增
     * - 同时作为 Redis key（chat:history:{sessionId}）和 MySQL 主键
     */
    @TableId(type = IdType.INPUT) // 标识此字段为主键，ID 生成策略为手动输入（INPUT）
    private String sessionId; // UUID 格式的会话唯一标识符

    /**
     * 所属用户ID
     * - 来自网关转发时注入的 X-User-Id 请求头
     * - 用于会话列表的权限隔离（用户只能看到自己的会话）
     * - 匿名用户值为 "anonymous"
     */
    private String userId; // 会话所属的用户标识，用于权限隔离和会话列表查询

    /**
     * 会话标题
     * - 初始值为"新对话"（在 ChatMemoryService.ensureSession 中设置）
     * - 可以自动截取第一条用户消息的前20个字符作为标题
     * - 也支持用户手动修改标题
     * - 用于前端会话列表展示，帮助用户快速定位历史对话
     */
    private String title; // 会话的显示标题，默认为"新对话"，可由第一条消息自动生成

    /**
     * 会话创建时间
     * - 记录会话首次创建的时间戳
     * - 用于按时间排序用户的历史会话列表（最新的排最前）
     * - 在 ensureSession 第一次创建时设置
     */
    private LocalDateTime createTime; // 会话创建的时间戳，用于排序

    /**
     * 会话最后更新时间
     * - 每次有新消息时更新为此时间
     * - 用于判断会话活跃度和排序
     * - 也可用于判断会话是否过期（长时间未更新的会话可被清理）
     */
    private LocalDateTime updateTime; // 最后一条消息的时间戳，用于活跃度排序和过期清理
}
