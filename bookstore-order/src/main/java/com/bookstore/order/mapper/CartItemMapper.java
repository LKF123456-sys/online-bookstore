package com.bookstore.order.mapper;  // 声明当前接口所在的包路径：购物车项Mapper层

// 导入MyBatis-Plus的BaseMapper基类
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 导入购物车项实体类，对应数据库中的购物车项表
import com.bookstore.common.entity.CartItem;
// 导入MyBatis的Mapper注解
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车项Mapper接口
 * 继承MyBatis-Plus的BaseMapper，自动获得对购物车项表的增删改查方法。
 * 购物车项是购物车中的每个商品条目，一个购物车可以有多个购物车项。
 * 常用场景：根据购物车ID查询所有购物车项、按商品ID查询、更新数量、删除等。
 */
@Mapper  // 标记为MyBatis的Mapper接口
public interface CartItemMapper extends BaseMapper<CartItem> {  // 继承BaseMapper，操作的实体类型为CartItem
    // 不需要写任何方法，BaseMapper已经提供了所有常用的CRUD操作
}
