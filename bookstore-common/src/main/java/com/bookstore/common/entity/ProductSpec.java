package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 商品规格实体类
 * 对应数据库中的 product_spec 表，存储商品的规格信息
 * 例如：颜色、尺寸、版本等规格类型及其可选值
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("product_spec")  // MyBatis-Plus注解，指定对应的数据库表名为"product_spec"
public class ProductSpec {  // 商品规格实体类

    @TableId(type = IdType.AUTO)  // 主键注解，使用数据库自增策略生成主键
    private Long id;  // 规格ID，Long类型，由数据库自动生成

    private String productId;  // 关联的商品ID
    private String specName;  // 规格名称，如"颜色"、"尺寸"等
    private String specValues; // 规格可选值，JSON数组格式，如["红色","蓝色","绿色"]
    private Integer sort;  // 排序序号，数值越小越靠前显示
}
