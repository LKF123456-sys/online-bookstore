package com.bookstore.order.mapper;  // 声明当前接口所在的包路径：订单Mapper层

// 导入MyBatis-Plus的BaseMapper基类，提供了通用的CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 导入订单实体类，对应数据库中的订单表
import com.bookstore.common.entity.Orders;
// 导入MyBatis的Mapper注解
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 * 继承MyBatis-Plus的BaseMapper，自动获得对订单表的增删改查方法。
 * 包括：insert（插入）、deleteById（删除）、updateById（更新）、selectById（按ID查询）、selectList（列表查询）等。
 * 无需手写SQL语句，MyBatis-Plus会根据实体类自动生成。
 */
@Mapper  // 标记为MyBatis的Mapper接口，Spring会自动创建代理实现类并注册为Bean
public interface OrdersMapper extends BaseMapper<Orders> {  // 继承BaseMapper，泛型参数指定操作的实体类型为Orders
    // 不需要写任何方法，BaseMapper已经提供了所有常用的CRUD操作
}
