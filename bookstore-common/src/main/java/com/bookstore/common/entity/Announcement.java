package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 公告实体类
 * 对应数据库中的 announcement 表，存储系统公告信息
 * 管理员发布的公告会在用户端展示
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("announcement")  // MyBatis-Plus注解，指定对应的数据库表名为"announcement"
public class Announcement {  // 公告实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // 公告ID，Long类型，由数据库自动生成

    private String title;  // 公告标题
    private String content;  // 公告内容
    private Integer status;  // 公告状态：0-草稿 1-已发布
    @TableField(value = "created_at", fill = FieldFill.INSERT)  // 字段映射注解，指定数据库列名并设置插入时自动填充
    private LocalDateTime createdAt;  // 公告创建时间
}
