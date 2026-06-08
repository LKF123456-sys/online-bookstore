package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格

/**
 * 商品SKU视图对象（VO）
 * 用于向前端返回商品SKU的详细信息
 * 包含SKU名称、规格、价格和库存
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class ProductSkuVO {  // 商品SKU视图对象类

    private Long id;  // SKU ID
    private String skuName;  // SKU名称，如"红色-XL"
    private String specs;  // SKU规格信息，JSON格式
    private BigDecimal price;  // SKU价格
    private Integer stock;  // SKU库存数量
}
