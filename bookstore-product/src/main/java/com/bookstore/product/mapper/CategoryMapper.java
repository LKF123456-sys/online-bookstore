package com.bookstore.product.mapper;  // 声明当前类所在的包路径，属于商品服务的数据访问层

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口，提供通用CRUD方法
import com.bookstore.common.entity.Category;  // 导入商品分类实体类，对应数据库中的分类表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的Mapper注解

/**
 * 商品分类Mapper接口（数据访问层）
 * 继承了MyBatis-Plus的BaseMapper，自动获得了通用的增删改查方法
 * 用于操作数据库中的商品分类表（category表）
 *
 * 通过继承BaseMapper，可以直接使用以下方法：
 *   - selectList(null)：查询所有分类
 *   - selectById(id)：根据ID查询分类
 *   - insert(category)：新增分类
 *   - updateById(category)：修改分类
 *   - deleteById(id)：删除分类
 */
@Mapper  // 标记为MyBatis的Mapper接口
public interface CategoryMapper extends BaseMapper<Category> {  // 继承BaseMapper，泛型参数为分类实体类
    // 继承自BaseMapper的通用CRUD方法，无需手动编写SQL
}
