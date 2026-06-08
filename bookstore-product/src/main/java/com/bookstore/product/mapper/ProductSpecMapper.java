package com.bookstore.product.mapper;  // 声明当前类所在的包路径，属于商品服务的数据访问层

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口
import com.bookstore.common.entity.ProductSpec;  // 导入商品规格实体类，对应数据库中的规格表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的Mapper注解

/**
 * 商品规格Mapper接口（数据访问层）
 * 继承了MyBatis-Plus的BaseMapper，自动获得通用CRUD方法
 * 用于操作数据库中的商品规格表（product_spec表）
 *
 * 商品规格（Spec）指的是商品的可选属性，例如：
 *   - 颜色：红色、蓝色、白色
 *   - 尺码：S、M、L、XL
 *   - 存储容量：128GB、256GB、512GB
 *   这些规格组合起来就形成了SKU（库存量单位）
 */
@Mapper  // 标记为MyBatis的Mapper接口
public interface ProductSpecMapper extends BaseMapper<ProductSpec> {  // 继承BaseMapper，泛型参数为规格实体类
    // 继承自BaseMapper的通用CRUD方法，无需手动编写SQL
}
