package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格

/**
 * 订单项视图对象（VO）
 * 用于向前端返回订单中单个商品的详细信息
 * 包含商品ID、名称、数量和单价
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class OrderItemVO {  // 订单项视图对象类

    private Long id;  // 订单项ID
    private String orderId;  // 订单ID
    private String productId;  // 商品ID
    private String productName;  // 商品名称
    private Integer quantity;  // 购买数量
    private BigDecimal price;  // 购买时的商品单价
    private String image;  // 商品图片URL（方便前端展示）
}
