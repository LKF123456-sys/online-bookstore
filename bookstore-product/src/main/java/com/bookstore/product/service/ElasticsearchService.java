package com.bookstore.product.service;  // 声明当前类所在的包路径，属于商品服务的业务逻辑层

import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象，用于返回搜索结果
import com.bookstore.product.document.ProductDocument;  // 导入商品ES文档对象，对应Elasticsearch中的商品索引
import com.bookstore.product.repository.ProductSearchRepository;  // 导入商品搜索仓库接口，提供基本的ES操作
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成构造函数
import org.springframework.data.domain.Page;  // 导入Spring Data的分页对象
import org.springframework.data.domain.PageRequest;  // 导入Spring Data的分页请求对象，用于指定页码和大小
import org.springframework.data.elasticsearch.client.elc.NativeQuery;  // 导入ES原生查询对象，支持构建复杂的ES查询
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;  // 导入ES操作模板，用于执行ES搜索查询
import org.springframework.data.elasticsearch.core.SearchHit;  // 导入ES搜索命中对象，包含单条搜索结果及其元信息
import org.springframework.data.elasticsearch.core.SearchHits;  // 导入ES搜索命中集合，包含所有搜索结果
import org.springframework.data.elasticsearch.core.query.Query;  // 导入ES查询接口（父接口）
import org.springframework.stereotype.Service;  // 导入Spring的Service注解

import java.util.List;  // 导入Java集合框架的List接口
import java.util.stream.Collectors;  // 导入Java Stream的Collectors工具类

/**
 * Elasticsearch搜索服务类
 * 提供基于Elasticsearch的商品全文搜索功能
 *
 * 为什么使用Elasticsearch？
 *   MySQL的LIKE查询在数据量大时性能很差（需要全表扫描）
 *   Elasticsearch是专业的搜索引擎，支持：
 *   1. 全文搜索（对文本内容进行分词后搜索）
 *   2. 模糊匹配（fuzzy search，容忍拼写错误）
 *   3. 相关性排序（按匹配程度排序）
 *   4. 高性能（倒排索引，适合海量数据搜索）
 *
 * 该类提供三个核心功能：
 *   - 搜索商品：支持关键词模糊搜索、自动纠错、分页
 *   - 索引商品：将商品数据写入ES索引
 *   - 删除商品：从ES索引中删除商品
 */
@Service  // 标记为Spring的Service组件
@RequiredArgsConstructor  // Lombok注解，自动生成构造函数实现依赖注入
public class ElasticsearchService {  // Elasticsearch搜索服务类

    private final ElasticsearchOperations elasticsearchOperations;  // ES操作模板，用于执行复杂的搜索查询（通过构造函数注入）
    private final ProductSearchRepository productSearchRepository;  // ES搜索仓库，提供基本的CRUD操作（通过构造函数注入）

    /**
     * 搜索商品
     * 使用ES的multi_match查询，在商品名称和描述两个字段中进行全文搜索
     * 支持自动纠错（fuzziness=AUTO），即使用户输入有小错误也能返回相关结果
     *
     * 工作原理：
     *   1. ES会对关键词进行分词处理
     *   2. 在name和description字段的倒排索引中查找匹配的分词
     *   3. 根据相关性（TF-IDF等算法）计算每条结果的得分
     *   4. 按得分降序返回结果
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @return 搜索匹配的商品列表
     */
    public List<ProductVO> searchProducts(String keyword, Integer pageNum, Integer pageSize) {  // 搜索商品的方法
        // 构建ES原生查询
        NativeQuery query = NativeQuery.builder()  // 使用Builder模式构建查询
                .withQuery(q -> q.multiMatch(m -> m  // 设置查询类型为multi_match（多字段匹配查询）
                        .fields("name", "description")  // 指定搜索的字段：商品名称和商品描述
                        .query(keyword)  // 设置搜索关键词
                        .fuzziness("AUTO")))  // 设置模糊匹配级别为AUTO（自动根据词长决定容忍的错误数）
                .withPageable(PageRequest.of(pageNum - 1, pageSize))  // 设置分页参数（注意：ES的页码从0开始，所以要减1）
                .build();  // 构建查询对象

        // 执行搜索查询
        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);  // 执行ES搜索，返回搜索命中的结果集合
        // 将搜索结果转换为视图对象列表
        return hits.getSearchHits().stream()  // 获取所有搜索命中结果并转为Stream流
                .map(hit -> {  // 对每条搜索命中结果进行转换
                    ProductDocument doc = hit.getContent();  // 从搜索命中中获取文档内容（即商品数据）
                    ProductVO vo = new ProductVO();  // 创建商品视图对象
                    vo.setId(doc.getId());  // 设置商品ID
                    vo.setName(doc.getName());  // 设置商品名称
                    vo.setDescription(doc.getDescription());  // 设置商品描述
                    vo.setCategory(doc.getCategory());  // 设置商品分类
                    vo.setPrice(doc.getPrice());  // 设置商品价格
                    vo.setImageUrl(doc.getImage());  // 设置商品图片URL（注意：文档字段名为image，VO字段名为imageUrl）
                    vo.setSales(doc.getSales());  // 设置商品销量
                    vo.setStock(doc.getStock());  // 设置商品库存
                    return vo;  // 返回转换后的视图对象
                })
                .collect(Collectors.toList());  // 将Stream收集为List返回
    }

    /**
     * 将商品文档索引到Elasticsearch中
     * 当新增或修改商品时，需要同步更新ES中的索引数据
     *
     * @param document 商品文档对象，包含要索引的商品数据
     */
    public void indexProduct(ProductDocument document) {  // 索引商品到ES的方法
        productSearchRepository.save(document);  // 调用仓库的save方法，如果ID已存在则更新，不存在则插入
    }

    /**
     * 从Elasticsearch中删除商品索引
     * 当删除商品时，需要同步从ES中删除对应的索引数据
     *
     * @param id 要删除的商品ID
     */
    public void deleteProduct(String id) {  // 从ES中删除商品的方法
        productSearchRepository.deleteById(id);  // 根据ID从ES索引中删除商品文档
    }
}
