package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 购物车实体类
 * 对应数据库中的 cart 表，存储用户的购物车信息
 * 每个用户对应一个购物车，购物车内可以包含多个购物车项
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("cart")  // MyBatis-Plus注解，指定对应的数据库表名为"cart"
public class Cart {  // 购物车实体类

    @TableId(value = "cartid", type = IdType.INPUT)  // 主键注解，指定主键字段为"cartid"，由用户手动设置
    private String cartid;  // 购物车ID，字符串类型，作为购物车的唯一标识

    private String userid;  // 用户ID，关联account表，标识购物车所属用户
    @TableField(fill = FieldFill.INSERT)  // 字段填充注解，在插入数据时自动填充创建时间
    private LocalDateTime createdAt;  // 购物车创建时间
}
