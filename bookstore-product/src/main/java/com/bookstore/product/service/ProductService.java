package com.bookstore.product.service;  // 声明当前类所在的包路径，属于商品服务的业务逻辑层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用Java方法引用构建类型安全的SQL条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;  // 导入MyBatis-Plus的分页对象，用于分页查询
import com.bookstore.common.api.dto.ProductQueryDTO;  // 导入商品查询数据传输对象，封装前端传来的查询条件
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果包装类
import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象，用于返回给前端的数据
import com.bookstore.common.entity.Product;  // 导入商品实体类，对应数据库中的商品表
import com.bookstore.common.entity.ProductSku;  // 导入商品SKU实体类，对应数据库中的SKU表（如不同颜色、尺码的变体）
import com.bookstore.common.entity.ProductSpec;  // 导入商品规格实体类，对应数据库中的规格表（如颜色、尺码的可选值）
import com.bookstore.product.mapper.ProductMapper;  // 导入商品Mapper接口
import com.bookstore.product.mapper.ProductSkuMapper;  // 导入SKU Mapper接口
import com.bookstore.product.mapper.ProductSpecMapper;  // 导入规格Mapper接口
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成构造函数
import org.springframework.beans.BeanUtils;  // 导入Spring的Bean工具类，用于对象属性拷贝
import org.springframework.data.redis.core.RedisTemplate;  // 导入Redis操作模板类，用于缓存操作
import org.springframework.stereotype.Service;  // 导入Spring的Service注解，标记为业务层组件
import org.springframework.transaction.annotation.Transactional;  // 导入Spring事务注解，用于声明式事务管理

import java.util.List;  // 导入Java集合框架的List接口
import java.util.concurrent.TimeUnit;  // 导入Java时间单位枚举，用于设置缓存过期时间
import java.util.stream.Collectors;  // 导入Java Stream的Collectors工具类，用于将Stream收集为集合

/**
 * 商品业务服务类
 * 处理商品相关的所有业务逻辑，包括：
 *   - 商品列表查询（支持分页、筛选、排序）
 *   - 商品详情查询（带Redis缓存）
 *   - 推荐商品和热门商品查询
 *   - 商品的增删改操作
 *   - 商品状态管理（上架/下架）
 *   - 库存扣减
 *
 * 该类是控制器层和数据访问层之间的桥梁，负责：
 *   1. 接收控制器传来的参数
 *   2. 构建查询条件并调用Mapper层查询数据库
 *   3. 实体对象（Entity）到视图对象（VO）的转换
 *   4. Redis缓存的读写和清除
 *   5. 事务管理（保证数据一致性）
 */
@Service  // 标记为Spring的Service组件，会被Spring自动扫描并注册为Bean
@RequiredArgsConstructor  // Lombok注解，自动生成包含所有final字段的构造函数，实现依赖注入
public class ProductService {  // 商品业务服务类

    private final ProductMapper productMapper;  // 商品数据访问对象，用于操作商品表（通过构造函数注入）
    private final ProductSkuMapper productSkuMapper;  // SKU数据访问对象，用于操作SKU表（通过构造函数注入）
    private final ProductSpecMapper productSpecMapper;  // 规格数据访问对象，用于操作规格表（通过构造函数注入）
    private final RedisTemplate<String, Object> redisTemplate;  // Redis操作模板，用于缓存商品数据（通过构造函数注入）

    /**
     * 获取商品列表（分页查询）
     * 支持以下筛选和排序功能：
     *   - 按关键词模糊搜索（搜索商品名称和描述）
     *   - 按分类精确筛选
     *   - 按价格区间过滤（最低价、最高价）
     *   - 按价格或销量排序
     *   - 只返回上架状态（status=1）的商品
     *
     * @param query 查询条件DTO，包含分页参数和筛选条件
     * @return 分页结果，包含商品VO列表、总条数、当前页码、每页大小
     */
    public PageResult<ProductVO> getProductList(ProductQueryDTO query) {  // 获取商品列表的方法
        Page<Product> page = new Page<>(query.getPageNum(), query.getPageSize());  // 创建分页对象，传入当前页码和每页大小
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器

        // 如果关键词不为空，则按商品名称或描述进行模糊搜索
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {  // 判断关键词是否非空
            wrapper.like(Product::getName, query.getKeyword())  // 按商品名称进行模糊匹配（LIKE '%关键词%'）
                    .or().like(Product::getDescn, query.getKeyword());  // 或者按商品描述进行模糊匹配，OR表示两个条件满足一个即可
        }
        // 如果分类不为空，则按分类精确筛选
        if (query.getCategory() != null && !query.getCategory().isEmpty()) {  // 判断分类是否非空
            wrapper.eq(Product::getCategory, query.getCategory());  // 按分类精确匹配（=），只查询该分类下的商品
        }
        // 如果最低价不为空，则过滤大于等于该价格的商品
        if (query.getMinPrice() != null) {  // 判断最低价是否非空
            wrapper.ge(Product::getPrice, query.getMinPrice());  // 过滤价格 >= 最低价的商品（ge = greater or equal）
        }
        // 如果最高价不为空，则过滤小于等于该价格的商品
        if (query.getMaxPrice() != null) {  // 判断最高价是否非空
            wrapper.le(Product::getPrice, query.getMaxPrice());  // 过滤价格 <= 最高价的商品（le = less or equal）
        }

        // 排序逻辑：根据sortBy字段决定排序方式
        if ("price".equals(query.getSortBy())) {  // 如果按价格排序
            wrapper.orderByAsc("asc".equals(query.getSortOrder()), Product::getPrice);  // 价格升序（当sortOrder为"asc"时生效）
            wrapper.orderByDesc(!"asc".equals(query.getSortOrder()), Product::getPrice);  // 价格降序（当sortOrder不为"asc"时生效）
        } else if ("sales".equals(query.getSortBy())) {  // 如果按销量排序
            wrapper.orderByDesc(Product::getSales);  // 按销量降序排列，卖得最好的排在前面
        } else {  // 默认排序方式
            wrapper.orderByDesc(Product::getSales);  // 默认按销量降序排列
        }

        wrapper.eq(Product::getStatus, 1);  // 只查询上架状态的商品（status=1表示上架）
        Page<Product> result = productMapper.selectPage(page, wrapper);  // 执行分页查询，返回分页结果
        // 将实体对象列表转换为视图对象列表
        List<ProductVO> voList = result.getRecords().stream()  // 获取查询结果的记录列表，并转为Stream流
                .map(this::convertToVO)  // 对每个实体对象调用convertToVO方法，转换为VO对象
                .collect(Collectors.toList());  // 将Stream流收集为List集合
        return new PageResult<>(voList, result.getTotal(), query.getPageNum(), query.getPageSize());  // 封装分页结果返回
    }

    /**
     * 根据商品ID获取商品详情
     * 包含商品基本信息、SKU列表（不同颜色/尺码等变体）、规格列表
     * 使用Redis缓存，缓存时间为5分钟，提高查询性能
     *
     * @param id 商品ID
     * @return 商品详情视图对象（包含SKU和规格信息）
     * @throws IllegalArgumentException 如果商品不存在则抛出异常
     */
    public ProductVO getProductById(String id) {  // 根据ID查询商品详情的方法
        // 先从缓存获取
        String cacheKey = "product:" + id;  // 构建Redis缓存Key，格式为 "product:商品ID"
        ProductVO cached = (ProductVO) redisTemplate.opsForValue().get(cacheKey);  // 从Redis中获取缓存的商品数据
        if (cached != null) {  // 如果缓存中存在数据（不为null）
            return cached;  // 直接返回缓存数据，避免查数据库，提高性能
        }

        // 缓存未命中，从数据库查询
        Product product = productMapper.selectById(id);  // 根据ID从数据库查询商品
        if (product == null) {  // 如果商品不存在
            throw new IllegalArgumentException("商品不存在");  // 抛出异常，告诉调用方商品不存在
        }
        ProductVO vo = convertToVO(product);  // 将商品实体转换为视图对象

        // 获取该商品的所有SKU（如不同颜色、尺码的变体）
        List<ProductSku> skus = productSkuMapper.selectList(  // 查询SKU列表
                new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));  // 条件：商品ID等于传入的id
        vo.setSkus(skus.stream().map(sku -> {  // 将SKU实体列表转换为SKU视图对象列表
            var skuVO = new com.bookstore.common.api.vo.ProductSkuVO();  // 创建SKU视图对象
            BeanUtils.copyProperties(sku, skuVO);  // 将SKU实体的属性拷贝到视图对象中
            return skuVO;  // 返回转换后的视图对象
        }).collect(Collectors.toList()));  // 收集为List并设置到商品VO中

        // 获取该商品的所有规格（如颜色可选值、尺码可选值）
        List<ProductSpec> specs = productSpecMapper.selectList(  // 查询规格列表
                new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, id));  // 条件：商品ID等于传入的id
        vo.setSpecs(specs.stream().map(spec -> {  // 将规格实体列表转换为规格视图对象列表
            var specVO = new com.bookstore.common.api.vo.ProductSpecVO();  // 创建规格视图对象
            BeanUtils.copyProperties(spec, specVO);  // 将规格实体的属性拷贝到视图对象中
            return specVO;  // 返回转换后的视图对象
        }).collect(Collectors.toList()));  // 收集为List并设置到商品VO中

        // 缓存5分钟
        redisTemplate.opsForValue().set(cacheKey, vo, 5, TimeUnit.MINUTES);  // 将商品详情存入Redis，设置5分钟后自动过期
        return vo;  // 返回商品详情视图对象
    }

    /**
     * 获取推荐商品列表
     * 查询标记为"推荐"的上架商品，按销量降序排列，取前N个
     *
     * @param limit 返回的商品数量
     * @return 推荐商品列表
     */
    public List<ProductVO> getRecommendProducts(Integer limit) {  // 获取推荐商品的方法
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  // 创建查询条件构造器
        wrapper.eq(Product::getIsRecommend, 1)  // 条件：推荐标记为1（是推荐商品）
                .eq(Product::getStatus, 1)  // 条件：状态为1（上架中）
                .orderByDesc(Product::getSales)  // 按销量降序排列
                .last("LIMIT " + limit);  // 在SQL末尾追加LIMIT子句，限制返回数量
        return productMapper.selectList(wrapper).stream()  // 执行查询并转为Stream流
                .map(this::convertToVO)  // 将每个实体转换为VO
                .collect(Collectors.toList());  // 收集为List返回
    }

    /**
     * 获取热门商品列表
     * 查询上架商品，按销量降序排列，取前N个
     *
     * @param limit 返回的商品数量
     * @return 热门商品列表
     */
    public List<ProductVO> getHotProducts(Integer limit) {  // 获取热门商品的方法
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  // 创建查询条件构造器
        wrapper.eq(Product::getStatus, 1)  // 条件：状态为1（上架中）
                .orderByDesc(Product::getSales)  // 按销量降序排列
                .last("LIMIT " + limit);  // 追加LIMIT限制返回数量
        return productMapper.selectList(wrapper).stream()  // 执行查询并转为Stream流
                .map(this::convertToVO)  // 将每个实体转换为VO
                .collect(Collectors.toList());  // 收集为List返回
    }

    /**
     * 新增商品
     * 将前端传来的商品信息保存到数据库
     * 商品创建后默认为上架状态（status=1）
     * 操作完成后清除商品列表缓存
     *
     * @param vo 商品视图对象，包含要新增的商品信息
     */
    @Transactional  // 声明式事务注解，保证方法内的数据库操作要么全部成功，要么全部回滚
    public void addProduct(ProductVO vo) {  // 新增商品方法
        Product product = new Product();  // 创建商品实体对象
        BeanUtils.copyProperties(vo, product);  // 将VO的属性值拷贝到实体对象中
        product.setStatus(1);  // 设置商品状态为1（上架），新商品默认上架
        productMapper.insert(product);  // 将商品数据插入数据库

        // 清除缓存
        clearProductCache();  // 新增商品后清除商品列表缓存，保证下次查询能获取到最新数据
    }

    /**
     * 修改商品信息
     * 根据商品ID更新数据库中的商品数据
     * 操作完成后清除相关缓存
     *
     * @param vo 商品视图对象，包含要修改的商品信息和商品ID
     */
    @Transactional  // 声明式事务注解，保证数据一致性
    public void updateProduct(ProductVO vo) {  // 修改商品方法
        Product product = new Product();  // 创建商品实体对象
        BeanUtils.copyProperties(vo, product);  // 将VO的属性值拷贝到实体对象中
        productMapper.updateById(product);  // 根据商品ID更新数据库中的商品记录

        // 清除缓存
        clearProductCache();  // 清除商品列表缓存
        redisTemplate.delete("product:" + vo.getId());  // 清除该商品的详情缓存
    }

    /**
     * 删除商品
     * 根据商品ID从数据库中删除商品
     * 操作完成后清除相关缓存
     *
     * @param id 要删除的商品ID
     */
    @Transactional  // 声明式事务注解，保证数据一致性
    public void deleteProduct(String id) {  // 删除商品方法
        productMapper.deleteById(id);  // 根据ID从数据库删除商品记录

        // 清除缓存
        clearProductCache();  // 清除商品列表缓存
        redisTemplate.delete("product:" + id);  // 清除该商品的详情缓存
    }

    /**
     * 更新商品状态（上架/下架）
     * 先查询商品是否存在，再更新状态
     * 操作完成后清除相关缓存
     *
     * @param id 商品ID
     * @param status 目标状态（1=上架，0=下架）
     * @throws IllegalArgumentException 如果商品不存在则抛出异常
     */
    @Transactional  // 声明式事务注解，保证数据一致性
    public void updateProductStatus(String id, Integer status) {  // 更新商品状态方法
        Product product = productMapper.selectById(id);  // 根据ID查询商品
        if (product == null) {  // 如果商品不存在
            throw new IllegalArgumentException("商品不存在");  // 抛出异常
        }
        product.setStatus(status);  // 设置新的商品状态
        productMapper.updateById(product);  // 更新数据库中的商品记录

        // 清除缓存
        clearProductCache();  // 清除商品列表缓存
        redisTemplate.delete("product:" + id);  // 清除该商品的详情缓存
    }

    /**
     * 扣减商品库存（同时增加销量）
     * 这是一个原子操作，在事务中执行
     * 如果库存不足，会抛出异常并回滚事务
     *
     * @param id 商品ID
     * @param quantity 要扣减的数量
     * @throws IllegalArgumentException 如果商品不存在或库存不足则抛出异常
     */
    @Transactional  // 声明式事务注解，保证库存扣减和销量增加在同一事务中执行
    public void updateStock(String id, Integer quantity) {  // 扣减库存方法
        Product product = productMapper.selectById(id);  // 根据ID查询商品
        if (product == null) {  // 如果商品不存在
            throw new IllegalArgumentException("商品不存在");  // 抛出异常
        }
        if (product.getStock() < quantity) {  // 如果当前库存小于要扣减的数量
            throw new IllegalArgumentException("库存不足");  // 抛出库存不足异常
        }
        product.setStock(product.getStock() - quantity);  // 扣减库存：当前库存 - 扣减数量
        product.setSales(product.getSales() + quantity);  // 增加销量：当前销量 + 扣减数量
        productMapper.updateById(product);  // 更新数据库中的商品记录

        // 清除缓存
        redisTemplate.delete("product:" + id);  // 清除该商品的详情缓存，因为库存和销量已变化
    }

    /**
     * 将商品实体对象转换为视图对象
     * 使用Spring的BeanUtils进行属性拷贝
     * 实体对象是数据库映射，视图对象是返回给前端的数据结构
     *
     * @param product 商品实体对象
     * @return 商品视图对象
     */
    private ProductVO convertToVO(Product product) {  // 私有方法，仅在本类内使用
        ProductVO vo = new ProductVO();  // 创建商品视图对象
        BeanUtils.copyProperties(product, vo);  // 将实体对象的所有同名属性拷贝到视图对象
        return vo;  // 返回转换后的视图对象
    }

    /**
     * 清除商品列表相关的所有缓存
     * 查找Redis中所有以 "product:list:" 开头的Key并删除
     * 在商品数据发生变更（增删改、状态变更）时调用
     */
    private void clearProductCache() {  // 私有方法，清除商品列表缓存
        // 清除商品列表缓存
        redisTemplate.delete(redisTemplate.keys("product:list:*"));  // 使用keys命令查找所有匹配的Key并批量删除（注意：生产环境应避免大量使用keys命令）
    }
}
