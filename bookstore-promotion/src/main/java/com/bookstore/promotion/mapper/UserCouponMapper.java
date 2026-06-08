package com.bookstore.promotion.mapper;  // 声明当前接口所在的包路径，属于营销服务的数据访问层

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口，提供了通用的CRUD方法
import com.bookstore.common.entity.UserCoupon;  // 导入用户优惠券实体类，对应数据库中的用户-优惠券关联表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的Mapper注解，标记为数据访问层组件

/**
 * 用户优惠券数据访问接口（Mapper）
 * 用户优惠券表记录了用户与优惠券的领取关系
 * 继承了MyBatis-Plus的BaseMapper，自动获得通用CRUD方法：
 *   - insert()：插入记录（用户领取优惠券时使用）
 *   - selectList()：查询列表（查询用户的优惠券时使用）
 *   - selectOne()：查询单条记录（检查用户是否已领取时使用）
 *   - updateById()：更新记录（标记优惠券为已使用时使用）
 *   - 等等
 */
@Mapper  // 标记为MyBatis的Mapper接口，MyBatis会为此接口生成代理实现类
public interface UserCouponMapper extends BaseMapper<UserCoupon> {  // 继承BaseMapper，泛型参数为UserCoupon实体类
}
