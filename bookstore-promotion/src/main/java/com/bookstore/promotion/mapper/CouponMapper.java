package com.bookstore.promotion.mapper;  // 声明当前接口所在的包路径，属于营销服务的数据访问层

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口，提供了通用的CRUD方法
import com.bookstore.common.entity.Coupon;  // 导入优惠券实体类，对应数据库中的优惠券表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的Mapper注解，标记为数据访问层组件

/**
 * 优惠券数据访问接口（Mapper）
 * 继承了MyBatis-Plus的BaseMapper，自动获得以下通用方法：
 *   - insert()：插入记录
 *   - deleteById()：根据ID删除记录
 *   - updateById()：根据ID更新记录
 *   - selectById()：根据ID查询记录
 *   - selectList()：查询列表
 *   - selectPage()：分页查询
 *   - 等等
 *
 * 无需编写SQL即可实现基本的数据库操作
 * 如需复杂查询，可在对应的XML映射文件中编写自定义SQL
 */
@Mapper  // 标记为MyBatis的Mapper接口，MyBatis会为此接口生成代理实现类
public interface CouponMapper extends BaseMapper<Coupon> {  // 继承BaseMapper，泛型参数为Coupon实体类
}
