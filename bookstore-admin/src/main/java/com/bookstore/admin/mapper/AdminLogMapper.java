package com.bookstore.admin.mapper; // 声明当前接口所在的包路径：Mapper数据访问层

// 导入MyBatis-Plus的BaseMapper接口，提供通用的CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 导入操作日志实体类，对应数据库中的admin_log表
import com.bookstore.common.entity.AdminLog;
// 导入MyBatis的@Mapper注解，标记这是一个Mapper接口
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口 - 操作日志的数据访问层
 *
 * 继承MyBatis-Plus的BaseMapper<AdminLog>后，自动获得以下常用方法：
 * - insert(entity)：插入一条记录
 * - deleteById(id)：根据主键删除
 * - updateById(entity)：根据主键更新
 * - selectById(id)：根据主键查询
 * - selectList(wrapper)：条件查询列表
 * - selectPage(page, wrapper)：分页查询
 * - selectCount(wrapper)：条件计数
 *
 * 无需编写任何SQL语句，MyBatis-Plus会根据实体类自动生成对应的SQL。
 * 如果需要自定义复杂SQL，可以在resources/mapper/目录下编写XML映射文件。
 */
@Mapper // 标记这是一个MyBatis Mapper接口，Spring会自动扫描并注册为Bean
public interface AdminLogMapper extends BaseMapper<AdminLog> {
    // 继承BaseMapper后已具备完整的CRUD能力，无需额外定义方法
    // 如需自定义查询方法，可在此添加，配合XML映射文件或@Select注解实现
}
