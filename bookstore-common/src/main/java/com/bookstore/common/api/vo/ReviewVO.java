package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import java.time.LocalDateTime;  // 导入Java8日期时间类

import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 评论视图对象（VO）
 * 用于向前端返回商品评论的详细信息
 * 包含评论内容、评分、点赞数、管理员回复等
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class ReviewVO {  // 评论视图对象类

    private Long id;  // 评论ID
    private String orderId;  // 关联的订单ID
    private String productId;  // 关联的商品ID
    private String userId;  // 发表评论的用户ID
    private Integer rating;  // 评分（1-5星）
    private String content;  // 评论内容
    private String image;  // 评论图片URL
    private Integer likes;  // 点赞数量
    private Integer isTop;  // 是否置顶：0-不置顶 1-置顶
    private String reply;  // 管理员回复内容
    private LocalDateTime createTime;  // 评论创建时间
    private Integer blocked;  // 是否被屏蔽：0-正常 1-已屏蔽
}
