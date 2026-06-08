package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格

/**
 * 商品SKU实体类
 * 对应数据库中的 product_sku 表，存储商品的具体销售单元信息
 * SKU（Stock Keeping Unit）是库存管理的最小单位
 * 例如：一本图书的"精装版"和"平装版"就是两个不同的SKU
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("product_sku")  // MyBatis-Plus注解，指定对应的数据库表名为"product_sku"
public class ProductSku {  // 商品SKU实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // SKU ID，Long类型，由数据库自动生成

    private String productId;  // 关联的商品ID
    private String skuName;  // SKU名称，如"红色-XL"、"精装版"等
    private String specs; // SKU规格信息，JSON格式，如{"颜色":"红色","尺寸":"XL"}
    private BigDecimal price;  // SKU价格，可能与商品主价格不同
    private Integer stock;  // SKU库存数量
    private Integer status; // SKU状态：0-禁用 1-启用，禁用后不可购买
}
