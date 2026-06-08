package com.bookstore.message.mapper;  // 声明当前类所属的包路径，属于消息服务的数据访问层

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口，提供通用的CRUD方法
import com.bookstore.common.entity.Message;  // 导入消息实体类，对应数据库中的消息表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的@Mapper注解，标记这是一个Mapper接口

/**
 * 消息数据访问接口（Mapper层）
 * 继承MyBatis-Plus的BaseMapper，自动获得常用的数据库操作方法
 *
 * 继承BaseMapper后，无需编写任何方法即可使用以下功能：
 *   - insert(T entity)       插入一条记录
 *   - deleteById(Serializable) 根据ID删除一条记录
 *   - updateById(T entity)   根据ID更新一条记录
 *   - selectById(Serializable) 根据ID查询一条记录
 *   - selectList(Wrapper)    条件查询列表
 *   - selectPage(Page, Wrapper) 分页查询
 *   - selectCount(Wrapper)   条件统计数量
 *
 * 对应数据库表：message（消息表）
 * 如果需要自定义复杂SQL，可以在对应的 XML 文件中编写
 */
@Mapper  // 标记为MyBatis的Mapper接口，Spring会自动为其生成代理实现类并注册为Bean
public interface MessageMapper extends BaseMapper<Message> {  // 消息Mapper接口，继承BaseMapper获得通用CRUD能力，泛型Message指定操作的实体类型

}
