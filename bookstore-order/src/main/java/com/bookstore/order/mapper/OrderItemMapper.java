package com.bookstore.order.mapper;  // 声明当前接口所在的包路径：订单项Mapper层

// 导入MyBatis-Plus的BaseMapper基类
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 导入订单项实体类，对应数据库中的订单项表
import com.bookstore.common.entity.OrderItem;
// 导入MyBatis的Mapper注解
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项Mapper接口
 * 继承MyBatis-Plus的BaseMapper，自动获得对订单项表的增删改查方法。
 * 订单项是订单中的每个商品条目，一个订单可以有多个订单项。
 * 常用场景：根据订单ID查询所有订单项、插入新的订单项等。
 */
@Mapper  // 标记为MyBatis的Mapper接口
public interface OrderItemMapper extends BaseMapper<OrderItem> {  // 继承BaseMapper，操作的实体类型为OrderItem
    // 不需要写任何方法，BaseMapper已经提供了所有常用的CRUD操作
}
