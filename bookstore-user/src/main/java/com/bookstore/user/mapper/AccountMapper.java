package com.bookstore.user.mapper;  // 声明当前接口所在的包路径，这里是Mapper（数据访问）层

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口，提供了通用的CRUD方法
import com.bookstore.common.entity.Account;  // 导入用户账户实体类，对应数据库中的用户表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的@Mapper注解，标记该接口为MyBatis的Mapper

/**
 * 用户账户Mapper接口
 * 用于操作数据库中的用户账户表（account表）。
 * <p>
 * 该接口继承了MyBatis-Plus的 {@link BaseMapper}，自动获得了以下常用的数据库操作方法：
 * - insert(T entity)：插入一条记录
 * - deleteById(Serializable id)：根据主键删除
 * - updateById(T entity)：根据主键更新
 * - selectById(Serializable id)：根据主键查询
 * - selectOne(Wrapper<T> queryWrapper)：根据条件查询单条记录
 * - selectList(Wrapper<T> queryWrapper)：根据条件查询多条记录
 * - selectPage(Page<T> page, Wrapper<T> queryWrapper)：分页查询
 * <p>
 * 注解说明：
 * - @Mapper：标记该接口为MyBatis的Mapper接口，MyBatis会为该接口生成代理实现类
 */
@Mapper  // MyBatis的Mapper注解，标记该接口为数据访问层的Mapper，MyBatis会自动为其生成代理实现类
public interface AccountMapper extends BaseMapper<Account> {  // 继承BaseMapper，泛型参数Account指定了该Mapper操作的实体类型
    // 该接口无需定义任何方法，BaseMapper已经提供了所有常用的CRUD操作
    // 如需自定义复杂的SQL查询，可以在这里定义方法，并在对应的XML映射文件中编写SQL
}
