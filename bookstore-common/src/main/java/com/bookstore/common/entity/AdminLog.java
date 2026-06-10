package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 管理操作日志实体类
 * 对应数据库中的 admin_log 表，记录管理员的操作日志
 * 用于审计和追踪管理员的所有操作行为
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("admin_log")  // MyBatis-Plus注解，指定对应的数据库表名为"admin_log"
public class AdminLog {  // 管理操作日志实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // 日志ID，Long类型，由数据库自动生成

    @TableField("admin_name")  // 字段映射注解，指定Java字段对应的数据库列名为"admin_name"
    private String adminName;  // 管理员用户名
    private String operation;  // 操作类型，如"添加商品"、"删除订单"等
    private String target;  // 操作对象，如商品ID、订单ID等
    private String detail;  // 操作详情，记录具体的操作内容
    private String ip;  // 操作者IP地址
    @TableField(value = "create_time", fill = FieldFill.INSERT)  // 字段映射注解，指定数据库列名并设置插入时自动填充
    private LocalDateTime createTime;  // 操作时间
}
