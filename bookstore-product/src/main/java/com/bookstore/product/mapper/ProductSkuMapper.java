package com.bookstore.product.mapper;  // 声明当前类所在的包路径，属于商品服务的数据访问层

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 导入MyBatis-Plus的基础Mapper接口
import com.bookstore.common.entity.ProductSku;  // 导入商品SKU实体类，对应数据库中的SKU表
import org.apache.ibatis.annotations.Mapper;  // 导入MyBatis的Mapper注解

/**
 * 商品SKU Mapper接口（数据访问层）
 * 继承了MyBatis-Plus的BaseMapper，自动获得通用CRUD方法
 * 用于操作数据库中的商品SKU表（product_sku表）
 *
 * SKU（Stock Keeping Unit，库存量单位）是商品的具体销售单元
 * 例如同一款手机可能有多个SKU：
 *   - SKU1：红色 + 128GB，价格3999元，库存100
 *   - SKU2：红色 + 256GB，价格4499元，库存50
 *   - SKU3：蓝色 + 128GB，价格3999元，库存80
 * 每个SKU有独立的库存、价格、条码等信息
 */
@Mapper  // 标记为MyBatis的Mapper接口
public interface ProductSkuMapper extends BaseMapper<ProductSku> {  // 继承BaseMapper，泛型参数为SKU实体类
    // 继承自BaseMapper的通用CRUD方法，无需手动编写SQL
}
