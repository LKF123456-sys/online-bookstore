package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格

/**
 * 购物车项视图对象（VO）
 * 用于向前端返回购物车中单个商品的详细信息
 * 包含购物车项基本信息和商品扩展信息（如名称、价格、图片等）
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class CartItemVO {  // 购物车项视图对象类

    private String itemid;  // 购物车项ID
    private String cartid;  // 购物车ID
    private Integer quantity;  // 商品数量

    // ========== 扩展字段（从商品服务获取） ==========
    private String name;  // 商品名称
    private String productId;  // 商品ID
    private BigDecimal price;  // 商品单价
    private String imageUrl;  // 商品图片URL
    private BigDecimal subtotal;  // 小计金额（单价 × 数量）
}
