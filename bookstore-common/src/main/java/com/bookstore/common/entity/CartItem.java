package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 购物车项实体类
 * 对应数据库中的 cartitem 表，存储购物车中的单个商品信息
 * 每个购物车项记录了用户添加到购物车的商品及其数量
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("cartitem")  // MyBatis-Plus注解，指定对应的数据库表名为"cartitem"
public class CartItem {  // 购物车项实体类

    @TableId(value = "itemid", type = IdType.INPUT)  // 主键注解，指定主键字段为"itemid"，由用户手动设置
    private String itemid;  // 购物车项ID，字符串类型，作为购物车项的唯一标识

    private String cartid;  // 购物车ID，关联cart表，标识该项所属的购物车
    @TableField("productid")  // 字段映射注解，指定Java字段对应的数据库列名为"productid"
    private String productId;  // 商品ID，关联product表，标识购物车中的商品
    private Integer quantity;  // 商品数量，用户添加到购物车的商品数量
}
