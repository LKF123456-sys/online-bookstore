package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 消息实体类
 * 对应数据库中的 message 表，存储系统消息和用户间的消息
 * 支持用户与用户、用户与管理员之间的消息传递
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("message")  // MyBatis-Plus注解，指定对应的数据库表名为"message"
public class Message {  // 消息实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // 消息ID，Long类型，由数据库自动生成

    @TableField("sender_id")  // 字段映射注解，指定Java字段对应的数据库列名为"sender_id"
    private String senderId;  // 发送者ID
    @TableField("sender_type")  // 字段映射注解，指定Java字段对应的数据库列名为"sender_type"
    private String senderType;  // 发送者类型，如"user"（用户）或"admin"（管理员）
    @TableField("receiver_id")  // 字段映射注解，指定Java字段对应的数据库列名为"receiver_id"
    private String receiverId;  // 接收者ID
    @TableField("receiver_type")  // 字段映射注解，指定Java字段对应的数据库列名为"receiver_type"
    private String receiverType;  // 接收者类型，如"user"（用户）或"admin"（管理员）
    private String content;  // 消息内容
    @TableField("read_status")  // 字段映射注解，指定Java字段对应的数据库列名为"read_status"
    private Integer readStatus;  // 已读状态：0-未读 1-已读
    @TableField("create_time")  // 字段映射注解，指定Java字段对应的数据库列名为"create_time"
    private LocalDateTime createTime;  // 消息创建时间
}
