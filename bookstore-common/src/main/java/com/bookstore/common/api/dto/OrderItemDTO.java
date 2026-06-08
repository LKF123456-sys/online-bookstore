package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import jakarta.validation.constraints.Min;  // 导入最小值校验注解
import jakarta.validation.constraints.NotBlank;  // 导入非空校验注解
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 订单项数据传输对象（DTO）
 * 用于表示订单中的单个商品信息
 * 在创建订单时，每个要购买的商品都对应一个OrderItemDTO
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class OrderItemDTO {  // 订单项DTO类

    @NotBlank(message = "商品ID不能为空")  // 非空校验，商品ID不能为null或空字符串
    private String productId;  // 商品ID

    @Min(value = 1, message = "数量至少为1")  // 最小值校验，购买数量不能小于1
    private Integer quantity;  // 购买数量
}
