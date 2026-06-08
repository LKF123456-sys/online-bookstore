package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 图书评论实体类
 * 对应数据库中的 book_review 表，存储用户对商品的评价信息
 * 包含评分、评论内容、图片、点赞数等
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("book_review")  // MyBatis-Plus注解，指定对应的数据库表名为"book_review"
public class BookReview {  // 图书评论实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // 评论ID，Long类型，由数据库自动生成

    @TableField("order_id")  // 字段映射注解，指定Java字段对应的数据库列名为"order_id"
    private String orderId;  // 订单ID，关联orders表，标识评论对应的订单
    @TableField("product_id")  // 字段映射注解，指定Java字段对应的数据库列名为"product_id"
    private String productId;  // 商品ID，关联product表，标识评论的商品
    @TableField("user_id")  // 字段映射注解，指定Java字段对应的数据库列名为"user_id"
    private String userId;  // 用户ID，关联account表，标识发表评论的用户
    private Integer rating;  // 评分，通常为1-5星
    private String content;  // 评论内容文本
    private String image;  // 评论图片URL地址
    private Integer likes;  // 点赞数量
    @TableField("is_top")  // 字段映射注解，指定Java字段对应的数据库列名为"is_top"
    private Integer isTop;  // 是否置顶：0-不置顶 1-置顶
    private String reply;  // 管理员回复内容
    @TableField("create_time")  // 字段映射注解，指定Java字段对应的数据库列名为"create_time"
    private LocalDateTime createTime;  // 评论创建时间
    private Integer blocked;  // 是否被屏蔽：0-正常 1-已屏蔽
}
