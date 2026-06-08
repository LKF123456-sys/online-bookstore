package com.bookstore.order.mapper;  // 声明当前接口所在的包路径：购物车Mapper层

// 导入MyBatis-Plus的BaseMapper基类
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 导入购物车实体类，对应数据库中的购物车表
import com.bookstore.common.entity.Cart;
// 导入MyBatis的Mapper注解
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper接口
 * 继承MyBatis-Plus的BaseMapper，自动获得对购物车表的增删改查方法。
 * 购物车与用户一一对应，每个用户有且仅有一个购物车。
 * 常用场景：根据购物车ID查询购物车、创建新购物车等。
 */
@Mapper  // 标记为MyBatis的Mapper接口
public interface CartMapper extends BaseMapper<Cart> {  // 继承BaseMapper，操作的实体类型为Cart
    // 不需要写任何方法，BaseMapper已经提供了所有常用的CRUD操作
}
