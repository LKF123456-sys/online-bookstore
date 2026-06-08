package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import jakarta.validation.constraints.Future;  // 导入未来时间校验注解
import jakarta.validation.constraints.Min;  // 导入最小值校验注解
import jakarta.validation.constraints.NotBlank;  // 导入非空校验注解
import jakarta.validation.constraints.NotNull;  // 导入非null校验注解
import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示金额
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 优惠券创建数据传输对象（DTO）
 * 用于接收管理员创建优惠券时提交的信息
 * 包含优惠券名称、类型、金额、发行数量和有效期等
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class CouponCreateDTO {  // 优惠券创建DTO类

    @NotBlank(message = "优惠券名称不能为空")  // 非空校验，优惠券名称不能为null或空字符串
    private String name;  // 优惠券名称，如"满100减10"

    @NotBlank(message = "优惠券类型不能为空")  // 非空校验，优惠券类型不能为null或空字符串
    private String type;  // 优惠券类型，如"满减"、"折扣"等

    @NotNull(message = "优惠金额不能为空")  // 非null校验，优惠金额不能为null
    @Min(value = 1, message = "优惠金额至少1元")  // 最小值校验，优惠金额不能小于1
    private BigDecimal discount;  // 优惠金额，使用优惠券可减免的金额

    private BigDecimal threshold; // 使用门槛金额（最低消费金额），可选

    @NotNull(message = "发行数量不能为空")  // 非null校验，发行数量不能为null
    @Min(value = 1, message = "发行数量至少1张")  // 最小值校验，发行数量不能小于1
    private Integer totalCount;  // 优惠券总发行数量

    @NotNull(message = "开始时间不能为空")  // 非null校验，开始时间不能为null
    private LocalDateTime startTime;  // 优惠券生效开始时间

    @NotNull(message = "结束时间不能为空")  // 非null校验，结束时间不能为null
    @Future(message = "结束时间必须在当前时间之后")  // 未来时间校验，结束时间必须是未来的时间
    private LocalDateTime endTime;  // 优惠券生效结束时间
}
