package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 用户优惠券实体类
 * 对应数据库中的 user_coupon 表，记录用户领取优惠券的信息
 * 记录了用户与优惠券的关联关系，以及优惠券的使用状态
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("user_coupon")  // MyBatis-Plus注解，指定对应的数据库表名为"user_coupon"
public class UserCoupon {  // 用户优惠券实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // 用户优惠券ID，Long类型，由数据库自动生成

    @TableField("user_id")  // 字段映射注解，指定Java字段对应的数据库列名为"user_id"
    private String userId;  // 用户ID，关联account表，标识领取优惠券的用户
    @TableField("coupon_id")  // 字段映射注解，指定Java字段对应的数据库列名为"coupon_id"
    private Integer couponId;  // 优惠券ID，关联coupon表
    @TableField("is_used")  // 字段映射注解，指定Java字段对应的数据库列名为"is_used"
    private Integer isUsed;  // 是否已使用：0-未使用 1-已使用
    @TableField("grant_time")  // 字段映射注解，指定Java字段对应的数据库列名为"grant_time"
    private LocalDateTime grantTime;  // 优惠券领取时间
    @TableField("use_time")  // 字段映射注解，指定Java字段对应的数据库列名为"use_time"
    private LocalDateTime useTime;  // 优惠券使用时间
}
