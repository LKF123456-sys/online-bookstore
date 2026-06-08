package com.bookstore.product.repository;  // 声明当前类所在的包路径，属于商品服务的数据访问层（ES仓库）

import com.bookstore.product.document.ProductDocument;  // 导入商品ES文档对象
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;  // 导入Spring Data Elasticsearch的仓库接口
import org.springframework.stereotype.Repository;  // 导入Spring的Repository注解

/**
 * 商品搜索仓库接口（Elasticsearch数据访问层）
 * 继承了Spring Data Elasticsearch的ElasticsearchRepository接口
 * 提供了对Elasticsearch中商品索引的基本CRUD操作
 *
 * ElasticsearchRepository类似MyBatis-Plus的BaseMapper，提供了以下内置方法：
 *   - save(document)：保存/更新文档（根据ID判断是插入还是更新）
 *   - findById(id)：根据ID查询文档
 *   - deleteById(id)：根据ID删除文档
 *   - findAll()：查询所有文档
 *   - count()：统计文档总数
 *
 * 对于复杂的搜索查询（如全文搜索、模糊匹配），则通过ElasticsearchOperations来实现
 */
@Repository  // 标记为Spring的Repository组件（数据访问层组件）
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {  // 继承ES仓库接口，泛型参数为文档类型和ID类型
    // 继承自ElasticsearchRepository的基本CRUD方法
    // 复杂搜索查询由ElasticsearchService中的ElasticsearchOperations实现
}
