package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示金额
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 优惠券实体类
 * 对应数据库中的 coupon 表，存储优惠券的基本信息
 * 优惠券可用于订单结算时减免金额
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("coupon")  // MyBatis-Plus注解，指定对应的数据库表名为"coupon"
public class Coupon {  // 优惠券实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // 优惠券ID，Long类型，由数据库自动生成

    private String name;  // 优惠券名称，如"满100减10"
    private String type;  // 优惠券类型，如"满减"、"折扣"等
    private BigDecimal threshold;  // 使用门槛金额，订单金额需达到此值才能使用
    private BigDecimal discount;  // 优惠金额，使用优惠券可减免的金额
    @TableField("total_count")  // 字段映射注解，指定Java字段对应的数据库列名为"total_count"
    private Integer totalCount;  // 优惠券总发放数量
    @TableField("used_count")  // 字段映射注解，指定Java字段对应的数据库列名为"used_count"
    private Integer usedCount;  // 已被使用的数量
    @TableField("start_time")  // 字段映射注解，指定Java字段对应的数据库列名为"start_time"
    private LocalDateTime startTime;  // 优惠券生效开始时间
    @TableField("end_time")  // 字段映射注解，指定Java字段对应的数据库列名为"end_time"
    private LocalDateTime endTime;  // 优惠券生效结束时间
    private Integer status;  // 优惠券状态：0-禁用 1-启用
    @TableField("create_time")  // 字段映射注解，指定Java字段对应的数据库列名为"create_time"
    private LocalDateTime createTime;  // 优惠券创建时间
}
