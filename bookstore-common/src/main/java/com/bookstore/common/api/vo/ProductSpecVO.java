package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 商品规格视图对象（VO）
 * 用于向前端返回商品规格的详细信息
 * 包含规格名称和规格可选值
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class ProductSpecVO {  // 商品规格视图对象类

    private Long id;  // 规格ID
    private String specName;  // 规格名称，如"颜色"、"尺寸"等
    private String specValues;  // 规格可选值，JSON数组格式
}
