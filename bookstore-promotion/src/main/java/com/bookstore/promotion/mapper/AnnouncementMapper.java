package com.bookstore.promotion.mapper;  // 声明当前接口所在的包路径，属于营销服务的数据访问层

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口，提供了通用的CRUD方法
import com.bookstore.common.entity.Announcement;  // 导入公告实体类，对应数据库中的公告表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的Mapper注解，标记为数据访问层组件

/**
 * 公告数据访问接口（Mapper）
 * 继承了MyBatis-Plus的BaseMapper，自动获得通用CRUD方法：
 *   - insert()：插入记录（管理员创建公告时使用）
 *   - deleteById()：根据ID删除记录（管理员删除公告时使用）
 *   - updateById()：根据ID更新记录（管理员修改公告或更新状态时使用）
 *   - selectById()：根据ID查询记录
 *   - selectList()：查询列表（查询激活状态的公告时使用）
 *   - selectPage()：分页查询
 *   - 等等
 */
@Mapper  // 标记为MyBatis的Mapper接口，MyBatis会为此接口生成代理实现类
public interface AnnouncementMapper extends BaseMapper<Announcement> {  // 继承BaseMapper，泛型参数为Announcement实体类
}
