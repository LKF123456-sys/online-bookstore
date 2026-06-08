package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格

/**
 * 订单项实体类
 * 对应数据库中的 order_item 表，存储订单中的单个商品信息
 * 一个订单可以包含多个订单项，每个订单项对应一个商品
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("order_item")  // MyBatis-Plus注解，指定对应的数据库表名为"order_item"
public class OrderItem {  // 订单项实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // 订单项ID，Long类型，由数据库自动生成

    @TableField("order_id")  // 字段映射注解，指定Java字段对应的数据库列名为"order_id"
    private String orderId;  // 订单ID，关联orders表，标识该项所属的订单
    @TableField("product_id")  // 字段映射注解，指定Java字段对应的数据库列名为"product_id"
    private String productId;  // 商品ID，关联product表，标识订单中的商品
    @TableField("product_name")  // 字段映射注解，指定Java字段对应的数据库列名为"product_name"
    private String productName;  // 商品名称，冗余存储，避免商品信息变更影响历史订单
    private Integer quantity;  // 购买数量
    private BigDecimal price;  // 购买时的商品单价
}
