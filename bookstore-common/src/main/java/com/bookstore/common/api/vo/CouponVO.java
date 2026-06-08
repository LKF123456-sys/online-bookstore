package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示金额
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 优惠券视图对象（VO）
 * 用于向前端返回优惠券的详细信息
 * 包含优惠券基本信息和当前用户的领取使用状态
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class CouponVO {  // 优惠券视图对象类

    private Long id;  // 优惠券ID
    private String name;  // 优惠券名称
    private String type;  // 优惠券类型
    private BigDecimal discount;  // 优惠金额
    private BigDecimal threshold;  // 使用门槛金额
    private Integer totalCount;  // 总发行数量
    private Integer usedCount;  // 已使用数量
    private LocalDateTime startTime;  // 生效开始时间
    private LocalDateTime endTime;  // 生效结束时间
    private Integer status;  // 优惠券状态：0-禁用 1-启用
    private LocalDateTime createTime;  // 创建时间
    private Integer userStatus; // 当前用户的领取状态：0-未领取 1-已领取 2-已使用
}
