package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import jakarta.validation.constraints.Min;  // 导入最小值校验注解
import jakarta.validation.constraints.NotBlank;  // 导入非空校验注解
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 购物车添加数据传输对象（DTO）
 * 用于接收用户添加商品到购物车时提交的信息
 * 包含商品ID和购买数量
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class CartAddDTO {  // 购物车添加DTO类

    @NotBlank(message = "商品ID不能为空")  // 非空校验，商品ID不能为null或空字符串
    private String productId;  // 要添加到购物车的商品ID

    @Min(value = 1, message = "数量至少为1")  // 最小值校验，数量不能小于1
    private Integer quantity = 1;  // 购买数量，默认为1
}
