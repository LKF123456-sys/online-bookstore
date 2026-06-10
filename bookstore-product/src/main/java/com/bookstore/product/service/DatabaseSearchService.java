package com.bookstore.product.service;  // 声明当前类所在的包路径，属于商品服务的业务逻辑层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用于构建类型安全的SQL查询条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;  // 导入MyBatis-Plus的分页对象，用于分页查询
import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象，用于返回搜索结果给前端
import com.bookstore.common.entity.Product;  // 导入商品实体类，对应数据库中的商品表
import com.bookstore.product.mapper.ProductMapper;  // 导入商品Mapper接口，用于操作商品数据表
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含所有final字段的构造函数，实现构造函数注入
import lombok.extern.slf4j.Slf4j;  // 导入Lombok的Slf4j注解，自动生成log日志对象，用于打印日志
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;  // 导入条件注解，当容器中缺少指定Bean时才创建当前Bean
import org.springframework.context.annotation.Primary;  // 导入Primary注解，当有多个实现时优先使用被标记的Bean
import org.springframework.stereotype.Service;  // 导入Spring的Service注解，标记为业务层组件

import java.util.List;  // 导入Java集合框架的List接口，用于表示商品列表
import java.util.stream.Collectors;  // 导入Java Stream的Collectors工具类，用于将Stream收集为集合

/**
 * 基于数据库的商品搜索服务
 * 当Elasticsearch不可用时的回退实现（降级方案）
 * 使用MySQL的LIKE查询进行模糊搜索
 *
 * 该类通过 @ConditionalOnMissingBean(ElasticsearchService.class) 注解实现条件装配：
 *   - 如果Spring容器中已经存在ElasticsearchService的Bean，则不创建本类的Bean
 *   - 只有当ElasticsearchService不存在时（即ES未配置或不可用），才会创建本类的Bean
 *
 * 注意：MySQL的LIKE查询性能较差（需要全表扫描），在数据量大时建议使用Elasticsearch
 */
@Slf4j  // Lombok注解，自动生成名为log的SLF4J日志对象，用于在方法中打印日志
@Service  // 标记为Spring的Service层组件，Spring会自动将该类注册为Bean
@ConditionalOnMissingBean(ElasticsearchService.class)  // 条件注解：当容器中没有ElasticsearchService Bean时才创建本Bean（ES不可用时的降级方案）
@RequiredArgsConstructor  // Lombok注解，自动生成包含所有final字段的构造函数，Spring通过构造函数注入依赖
public class DatabaseSearchService implements SearchService {  // 数据库搜索服务类，实现SearchService接口

    private final ProductMapper productMapper;  // 商品数据访问对象，通过构造函数注入，用于查询数据库中的商品数据

    /**
     * 使用数据库进行商品搜索
     * 通过MySQL的LIKE语句在商品名称和描述中进行模糊匹配
     * 只搜索上架状态（status=1）的商品，按销量降序排列
     *
     * @param keyword 搜索关键词，在商品名称和描述中进行模糊匹配
     * @param pageNum 页码（从1开始），指定返回第几页的数据
     * @param pageSize 每页数量，指定每页返回多少条数据
     * @return 搜索匹配的商品视图对象列表
     */
    @Override  // 实现SearchService接口中的searchProducts方法
    public List<ProductVO> searchProducts(String keyword, Integer pageNum, Integer pageSize) {  // 搜索商品方法
        log.info("使用数据库搜索商品 / Searching products via DB, keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);  // 打印搜索日志，记录搜索关键词和分页参数

        Page<Product> page = new Page<>(pageNum, pageSize);  // 创建分页对象，传入页码和每页大小
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.eq(Product::getStatus, 1)  // 条件：商品状态为1（只搜索上架商品）
               .and(w -> w.like(Product::getName, keyword)  // AND条件组内：商品名称模糊匹配关键词
                          .or()  // 或者
                          .like(Product::getDescn, keyword))  // 商品描述模糊匹配关键词
               .orderByDesc(Product::getSales);  // 按销量降序排列，卖得最好的排在前面

        Page<Product> result = productMapper.selectPage(page, wrapper);  // 执行分页查询，返回分页结果对象

        return result.getRecords().stream()  // 获取查询结果的记录列表，并转为Stream流
                .map(this::toVO)  // 对每条商品实体调用toVO方法，转换为视图对象
                .collect(Collectors.toList());  // 将Stream流收集为List集合并返回
    }

    /**
     * 将商品实体对象转换为商品视图对象
     * 逐个字段手动赋值，将数据库实体的字段映射到前端展示的VO对象
     * 注意：这里没有使用BeanUtils.copyProperties，而是手动映射
     *       因为实体和VO的字段名不完全一致（如productid->id, descn->description, image->imageUrl）
     *
     * @param product 商品实体对象，从数据库查询出来的原始数据
     * @return 商品视图对象（ProductVO），用于返回给前端展示
     */
    private ProductVO toVO(Product product) {  // 私有方法，将实体转换为VO
        ProductVO vo = new ProductVO();  // 创建商品视图对象
        vo.setId(product.getProductid());  // 设置商品ID（实体字段为productid，VO字段为id）
        vo.setName(product.getName());  // 设置商品名称
        vo.setDescription(product.getDescn());  // 设置商品描述（实体字段为descn，VO字段为description）
        vo.setCategory(product.getCategory());  // 设置商品分类
        vo.setPrice(product.getPrice());  // 设置商品价格
        vo.setImageUrl(product.getImage());  // 设置商品图片URL（实体字段为image，VO字段为imageUrl）
        vo.setSales(product.getSales());  // 设置商品销量
        vo.setStock(product.getStock());  // 设置商品库存
        vo.setAuthor(product.getAuthor());  // 设置商品作者
        vo.setIsRecommend(product.getIsRecommend());  // 设置是否为推荐商品
        return vo;  // 返回转换后的视图对象
    }
}
