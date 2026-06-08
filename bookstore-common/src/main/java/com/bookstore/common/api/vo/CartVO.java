package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格
import java.util.List;  // 导入List集合类

/**
 * 购物车视图对象（VO）
 * 用于向前端返回购物车的完整信息
 * 包含购物车项列表和商品总数量
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class CartVO {  // 购物车视图对象类

    private List<CartItemVO> items;  // 购物车项列表
    private Integer totalCount;  // 购物车中商品总数量
}
