package com.bookstore.product.mapper;  // 声明当前类所在的包路径，属于商品服务的数据访问层（Mapper层）

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口，提供通用的CRUD方法
import com.bookstore.common.entity.Product;  // 导入商品实体类，对应数据库中的商品表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的Mapper注解

/**
 * 商品Mapper接口（数据访问层）
 * 继承了MyBatis-Plus的BaseMapper，自动获得了以下通用方法：
 *   - insert(T entity)：插入一条记录
 *   - deleteById(Serializable id)：根据ID删除
 *   - updateById(T entity)：根据ID更新
 *   - selectById(Serializable id)：根据ID查询
 *   - selectList(Wrapper)：条件查询列表
 *   - selectPage(Page, Wrapper)：分页查询
 *   - 等等...
 *
 * 这些方法不需要自己写SQL，MyBatis-Plus会自动生成
 * 如果需要自定义SQL查询，可以在这里声明方法并在对应的XML文件中编写SQL
 *
 * @Mapper 注解标记这是一个MyBatis的Mapper接口
 * （也可以不在这里加@Mapper，因为在启动类上已经用了@MapperScan统一扫描）
 */
@Mapper  // 标记为MyBatis的Mapper接口，MyBatis会为该接口生成代理实现类
public interface ProductMapper extends BaseMapper<Product> {  // 继承BaseMapper，泛型参数为商品实体类
    // 继承自BaseMapper的通用CRUD方法，无需手动编写SQL
    // 如需自定义查询，可在此添加方法并在mapper/*.xml中编写对应SQL
}
