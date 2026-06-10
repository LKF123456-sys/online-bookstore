package com.bookstore.product.service;  // 声明当前接口所在的包路径，属于商品服务的业务逻辑层

import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象，用于返回搜索结果给前端
import java.util.List;  // 导入Java集合框架的List接口，用于表示商品列表

/**
 * 搜索服务接口
 * 定义商品搜索的统一接口，采用策略模式，支持ES和数据库两种实现：
 *   - ElasticsearchService：基于Elasticsearch的全文搜索引擎实现（优先使用）
 *   - DatabaseSearchService：基于MySQL的LIKE模糊查询实现（ES不可用时的回退方案）
 *
 * 通过Spring的条件注解（@ConditionalOnProperty / @ConditionalOnMissingBean）自动选择实现
 */
public interface SearchService {  // 搜索服务接口，定义搜索功能的统一规范

    /**
     * 搜索商品方法
     * 根据关键词在商品名称和描述中进行搜索，返回匹配的商品列表
     *
     * @param keyword  搜索关键词，用于在商品名称和描述中进行模糊匹配
     * @param pageNum  页码（从1开始），指定返回第几页的数据
     * @param pageSize 每页数量，指定每页返回多少条数据
     * @return 搜索匹配的商品列表（List<ProductVO>），包含商品的视图对象
     */
    List<ProductVO> searchProducts(String keyword, Integer pageNum, Integer pageSize);  // 搜索商品的抽象方法，由具体实现类提供实现
}
