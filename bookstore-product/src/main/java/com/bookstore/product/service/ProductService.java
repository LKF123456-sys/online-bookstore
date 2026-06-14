package com.bookstore.product.service;  // 声明当前类所在的包路径，属于商品服务的业务逻辑层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用Java方法引用构建类型安全的SQL条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;  // 导入MyBatis-Plus的分页对象，用于分页查询
import com.bookstore.common.api.dto.ProductQueryDTO;  // 导入商品查询数据传输对象，封装前端传来的查询条件
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果包装类
import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象，用于返回给前端的数据
import com.bookstore.common.entity.Product;  // 导入商品实体类，对应数据库中的商品表
import com.bookstore.common.entity.ProductSku;  // 导入商品SKU实体类，对应数据库中的SKU表（如不同颜色、尺码的变体）
import com.bookstore.common.entity.ProductSpec;  // 导入商品规格实体类，对应数据库中的规格表（如颜色、尺码的可选值）
import com.bookstore.common.exception.BusinessException;  // 导入业务异常类，用于抛出带状态码的业务异常
import com.bookstore.product.mapper.ProductMapper;  // 导入商品Mapper接口
import com.bookstore.product.mapper.ProductSkuMapper;  // 导入SKU Mapper接口
import com.bookstore.product.mapper.ProductSpecMapper;  // 导入规格Mapper接口
// 导入Micrometer指标类，用于缓存命中/未命中的监控统计
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // 导入Lombok注解，自动生成构造函数
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.springframework.beans.BeanUtils;  // 导入Spring的Bean工具类，用于对象属性拷贝
import org.springframework.data.redis.core.RedisTemplate;  // 导入Redis操作模板类，用于缓存操作
import org.springframework.stereotype.Service;  // 导入Spring的Service注解，标记为业务层组件
import org.springframework.transaction.annotation.Transactional;  // 导入Spring事务注解，用于声明式事务管理

import java.util.List;  // 导入Java集合框架的List接口
import java.util.Random;  // 导入随机数生成器，用于缓存TTL随机抖动（防雪崩）
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
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;  // 商品数据访问对象，用于操作商品表（通过构造函数注入）
    private final ProductSkuMapper productSkuMapper;  // SKU数据访问对象，用于操作SKU表（通过构造函数注入）
    private final ProductSpecMapper productSpecMapper;  // 规格数据访问对象，用于操作规格表（通过构造函数注入）
    private final RedisTemplate<String, Object> redisTemplate;  // Redis操作模板，用于缓存商品数据（通过构造函数注入）
    private final MeterRegistry meterRegistry;  // Micrometer指标注册器，用于获取缓存命中/未命中计数器

    // ==================== 缓存相关常量 ====================

    /** 缓存空值哨兵的Key前缀，用于防止缓存穿透 */
    private static final String NULL_SENTINEL = "##NULL##";  // 特殊标记值，表示数据库中不存在该商品

    /** 缓存基础过期时间（分钟） */
    private static final int CACHE_BASE_TTL_MINUTES = 5;

    /** 缓存过期时间随机抖动范围（秒），防止缓存雪崩 */
    private static final int CACHE_TTL_JITTER_SECONDS = 60;

    /** 分布式锁的最大重试次数 */
    private static final int LOCK_MAX_RETRIES = 3;

    /** 分布式锁重试间隔（毫秒） */
    private static final long LOCK_RETRY_INTERVAL_MS = 100;

    /** 随机数生成器，用于缓存TTL抖动 */
    private final Random random = new Random();

    /**
     * 获取缓存命中计数器（懒加载方式，避免循环依赖）
     */
    private Counter getCacheHitsCounter() {
        return meterRegistry.counter("bookstore.cache.hits");
    }

    /**
     * 获取缓存未命中计数器（懒加载方式，避免循环依赖）
     */
    private Counter getCacheMissesCounter() {
        return meterRegistry.counter("bookstore.cache.misses");
    }

    /**
     * 获取库存扣减计数器
     */
    private Counter getStockDeductionsCounter() {
        return meterRegistry.counter("bookstore.stock.deductions");
    }

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
            wrapper.and(w -> w.like(Product::getName, query.getKeyword())  // 按商品名称进行模糊匹配
                    .or().like(Product::getDescn, query.getKeyword()));  // 或者按商品描述进行模糊匹配，用and()包裹OR条件确保优先级正确
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
     * 根据商品ID获取商品详情（含三重缓存保护）
     * 包含商品基本信息、SKU列表（不同颜色/尺码等变体）、规格列表
     *
     * 缓存保护机制：
     *   1. 缓存穿透保护：对不存在的商品缓存空值哨兵（NULL sentinel），避免反复查询数据库
     *   2. 缓存击穿保护：使用Redis分布式锁，防止大量并发请求同时重建缓存
     *   3. 缓存雪崩保护：在基础TTL上添加随机抖动（5分钟 + 随机0~60秒），避免缓存同时过期
     *
     * @param id 商品ID
     * @return 商品详情视图对象（包含SKU和规格信息）
     * @throws IllegalArgumentException 如果商品不存在则抛出异常
     */
    @Tag(key = "product.id", value = "arg[0]")
    public ProductVO getProductById(String id) {  // 根据ID查询商品详情的方法
        String cacheKey = "product:" + id;  // 拼接Redis缓存Key，格式为 "product:商品ID"
        String lockKey = "lock:cache:product:" + id;  // 缓存重建分布式锁的Key

        // ==================== 第一步：尝试从缓存读取 ====================
        try {
            Object cachedObj = redisTemplate.opsForValue().get(cacheKey);  // 从Redis中获取缓存对象

            // 【缓存穿透保护】检查是否为空值哨兵
            if (NULL_SENTINEL.equals(cachedObj)) {  // 如果缓存的是空值哨兵
                getCacheHitsCounter().increment();  // 缓存命中计数 +1
                log.debug("命中空值哨兵缓存，商品不存在: id={}", id);
                throw new BusinessException(404, "商品不存在");  // 直接返回404，不查询数据库
            }

            if (cachedObj instanceof ProductVO) {  // 判断缓存对象是否为ProductVO类型
                getCacheHitsCounter().increment();  // 缓存命中计数 +1
                return (ProductVO) cachedObj;  // 缓存命中且类型正确，直接返回缓存中的商品数据
            }

            // 如果不是 ProductVO（如 LinkedHashMap，Redis反序列化时可能变成LinkedHashMap），清除脏缓存
            if (cachedObj != null) {  // 如果缓存存在但类型不正确
                redisTemplate.delete(cacheKey);  // 删除脏缓存数据
            }
        } catch (BusinessException e) {
            throw e;  // 业务异常直接抛出（如空值哨兵触发的404）
        } catch (Exception ignored) {  // 捕获序列化异常
            redisTemplate.delete(cacheKey);  // 删除有问题的缓存
        }

        // ==================== 缓存未命中，准备从数据库加载 ====================
        getCacheMissesCounter().increment();  // 缓存未命中计数 +1

        // 【缓存击穿保护】使用分布式锁防止并发重建缓存
        Boolean lockAcquired = false;
        try {
            // 尝试获取分布式锁，防止多个线程同时查询数据库重建缓存
            lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(lockAcquired)) {
                // ==================== 获取到锁，执行数据库查询和缓存重建 ====================

                // 双重检查：获取锁后再次检查缓存（可能在等锁期间被其他线程重建了缓存）
                Object doubleCheck = redisTemplate.opsForValue().get(cacheKey);
                if (doubleCheck instanceof ProductVO) {
                    return (ProductVO) doubleCheck;  // 其他线程已重建缓存，直接返回
                }
                if (NULL_SENTINEL.equals(doubleCheck)) {
                    throw new BusinessException(404, "商品不存在");
                }

                // 从数据库查询商品
                Product product = productMapper.selectById(id);
                if (product == null) {  // 如果商品不存在
                    // 【缓存穿透保护】缓存空值哨兵，30秒过期（较短TTL防止长期占用）
                    redisTemplate.opsForValue().set(cacheKey, NULL_SENTINEL, 30, TimeUnit.SECONDS);
                    log.info("商品不存在，已缓存空值哨兵: id={}", id);
                    throw new BusinessException(404, "商品不存在");
                }

                ProductVO vo = convertToVO(product);  // 将商品实体转换为视图对象

                // 获取该商品的所有SKU（如不同颜色、尺码的变体）
                List<ProductSku> skus = productSkuMapper.selectList(
                        new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
                vo.setSkus(skus.stream().map(sku -> {
                    var skuVO = new com.bookstore.common.api.vo.ProductSkuVO();
                    BeanUtils.copyProperties(sku, skuVO);
                    return skuVO;
                }).collect(Collectors.toList()));

                // 获取该商品的所有规格（如颜色可选值、尺码可选值）
                List<ProductSpec> specs = productSpecMapper.selectList(
                        new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, id));
                vo.setSpecs(specs.stream().map(spec -> {
                    var specVO = new com.bookstore.common.api.vo.ProductSpecVO();
                    BeanUtils.copyProperties(spec, specVO);
                    return specVO;
                }).collect(Collectors.toList()));

                // 【缓存雪崩保护】基础TTL + 随机抖动，避免大量缓存同时过期
                int jitterSeconds = random.nextInt(CACHE_TTL_JITTER_SECONDS);  // 随机0~60秒的抖动
                int totalTtlSeconds = CACHE_BASE_TTL_MINUTES * 60 + jitterSeconds;  // 基础5分钟 + 随机抖动
                redisTemplate.opsForValue().set(cacheKey, vo, totalTtlSeconds, TimeUnit.SECONDS);
                log.debug("商品详情已缓存，TTL={}秒（含抖动{}秒）: id={}", totalTtlSeconds, jitterSeconds, id);

                return vo;  // 返回商品详情视图对象

            } else {
                // ==================== 未获取到锁，等待并重试读取缓存 ====================
                log.debug("未获取到缓存重建锁，等待后重试: id={}", id);
                try {
                    Thread.sleep(200);  // 等待200ms，让持有锁的线程完成缓存重建
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();  // 恢复中断标志
                }

                // 重试从缓存读取
                Object retryCache = redisTemplate.opsForValue().get(cacheKey);
                if (NULL_SENTINEL.equals(retryCache)) {
                    throw new BusinessException(404, "商品不存在");
                }
                if (retryCache instanceof ProductVO) {
                    getCacheHitsCounter().increment();  // 重试缓存命中
                    return (ProductVO) retryCache;  // 缓存已被其他线程重建，直接返回
                }

                // 缓存仍然未命中，降级直接查数据库（不加锁）
                log.warn("缓存重建等待后仍未命中，降级查询数据库: id={}", id);
                Product product = productMapper.selectById(id);
                if (product == null) {
                    throw new BusinessException(404, "商品不存在");
                }
                return convertToVO(product);  // 返回基本信息（不含SKU和规格，减少降级时的数据库压力）
            }
        } finally {
            // 释放分布式锁
            if (Boolean.TRUE.equals(lockAcquired)) {
                try {
                    redisTemplate.delete(lockKey);  // 删除锁Key，释放锁
                } catch (Exception e) {
                    log.warn("释放缓存重建锁失败: lockKey={}", lockKey, e);
                }
            }
        }
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
            throw new BusinessException(404, "商品不存在");  // 抛出异常
        }
        product.setStatus(status);  // 设置新的商品状态
        productMapper.updateById(product);  // 更新数据库中的商品记录

        // 清除缓存
        clearProductCache();  // 清除商品列表缓存
        redisTemplate.delete("product:" + id);  // 清除该商品的详情缓存
    }

    /**
     * 扣减商品库存（带Redis分布式锁保护）
     * 解决高并发下的读-改-写竞态条件（Race Condition）问题
     *
     * 竞态条件示例（无锁情况）：
     *   线程A读取库存=10 -> 线程B读取库存=10 -> 线程A扣减5写入5 -> 线程B扣减3写入7（实际应为2）
     *   结果：库存7 > 实际应有库存2，数据不一致！
     *
     * 分布式锁方案：
     *   使用 Redis 的 SETNX（setIfAbsent）实现互斥锁，确保同一商品的库存操作串行执行
     *   获取锁失败时自动重试（最多3次，每次间隔100ms），超过重试次数抛出503异常
     *
     * @param id 商品ID
     * @param quantity 要扣减的数量（正数扣减，负数恢复）
     * @throws BusinessException 如果商品不存在、库存不足或获取锁失败则抛出异常
     */
    @Transactional  // 声明式事务注解，保证库存扣减和销量增加在同一事务中执行
    @Tags({
            @Tag(key = "stock.productId", value = "arg[0]"),
            @Tag(key = "stock.quantity", value = "arg[1]")
    })
    public void updateStock(String id, Integer quantity) {  // 扣减库存方法（对外接口，内部委托给带锁方法）
        updateStockWithLock(id, quantity);  // 委托给带分布式锁的方法执行
    }

    /**
     * 带Redis分布式锁的库存更新方法
     * 实现原理：
     *   1. 使用 SETNX 命令尝试获取锁（原子操作，只有一个线程能成功）
     *   2. 获取成功后执行业务逻辑，在 finally 块中释放锁
     *   3. 获取失败则等待100ms后重试，最多重试3次
     *   4. 超过重试次数仍获取不到锁，说明系统负载过高，返回503
     *
     * @param id 商品ID
     * @param quantity 要更新的数量（正数扣减，负数恢复）
     */
    private void updateStockWithLock(String id, Integer quantity) {  // 带分布式锁的库存更新
        String lockKey = "lock:stock:" + id;  // 分布式锁的Key，按商品ID隔离，不同商品的库存操作互不阻塞

        for (int attempt = 1; attempt <= LOCK_MAX_RETRIES; attempt++) {  // 循环尝试获取锁
            // 使用 SETNX 尝试获取分布式锁（原子操作）
            // 参数：Key, Value, 过期时间10秒（防止死锁：如果持有锁的线程异常退出，锁会在10秒后自动释放）
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(acquired)) {  // 成功获取到锁
                try {
                    // ==================== 在锁保护下执行库存操作 ====================
                    Product product = productMapper.selectById(id);  // 根据ID查询商品
                    if (product == null) {  // 如果商品不存在
                        throw new BusinessException(404, "商品不存在");  // 抛出异常
                    }
                    if (product.getStock() < quantity) {  // 如果当前库存小于要扣减的数量
                        throw new BusinessException(409, "库存不足");  // 抛出库存不足异常
                    }
                    product.setStock(product.getStock() - quantity);  // 扣减库存：当前库存 - 扣减数量
                    product.setSales(product.getSales() + quantity);  // 增加销量：当前销量 + 扣减数量
                    productMapper.updateById(product);  // 更新数据库中的商品记录

                    // 清除缓存
                    redisTemplate.delete("product:" + id);  // 清除该商品的详情缓存，因为库存和销量已变化

                    // 库存扣减成功，增加监控计数
                    if (quantity > 0) {  // 只有扣减操作才计数（恢复库存不计数）
                        getStockDeductionsCounter().increment();
                    }

                    log.debug("库存更新成功(已获锁): productId={}, quantity={}, 剩余库存={}",
                            id, quantity, product.getStock());
                    return;  // 成功执行，直接返回

                } finally {
                    // ==================== 释放分布式锁 ====================
                    // 注意：这里使用简单的delete释放锁，严格场景下应该用Lua脚本校验value后再删除
                    // 防止误删其他线程持有的锁（当前场景下10秒内业务操作完成，不会误删）
                    try {
                        redisTemplate.delete(lockKey);  // 删除锁Key，释放锁
                    } catch (Exception e) {
                        log.warn("释放库存分布式锁失败: lockKey={}", lockKey, e);
                    }
                }
            }

            // ==================== 未获取到锁，等待后重试 ====================
            if (attempt < LOCK_MAX_RETRIES) {  // 如果还有重试机会
                log.debug("获取库存锁失败，第{}/{}次重试: productId={}", attempt, LOCK_MAX_RETRIES, id);
                try {
                    Thread.sleep(LOCK_RETRY_INTERVAL_MS);  // 等待100ms后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();  // 恢复中断标志
                    throw new BusinessException(500, "库存操作被中断");
                }
            }
        }

        // 超过最大重试次数仍然获取不到锁，系统可能处于高负载状态
        log.error("获取库存分布式锁失败（已达最大重试次数）: productId={}, maxRetries={}", id, LOCK_MAX_RETRIES);
        throw new BusinessException(503, "系统繁忙，请稍后重试");  // 抛出503异常
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
     * 使用 SCAN 命令替代 KEYS 命令，避免在大量Key时阻塞Redis（KEYS会阻塞Redis主线程）
     * SCAN命令采用游标方式分批遍历，不会阻塞Redis
     */
    private void clearProductCache() {  // 私有方法，清除商品列表缓存
        org.springframework.data.redis.connection.RedisConnection conn = null;  // 声明Redis连接对象，初始化为null
        try {
            conn = redisTemplate.getConnectionFactory().getConnection();  // 从RedisTemplate获取Redis连接工厂，再获取实际的Redis连接
            org.springframework.data.redis.core.Cursor<byte[]> cursor = conn.scan(  // 使用SCAN命令遍历匹配的Key（非阻塞）
                    org.springframework.data.redis.core.ScanOptions.scanOptions().match("product:list:*").count(1000).build());  // 配置SCAN选项：匹配"product:list:*"模式的Key，每次扫描1000个
            while (cursor.hasNext()) {  // 遍历SCAN游标，逐个获取匹配的Key
                redisTemplate.delete(new String(cursor.next(), java.nio.charset.StandardCharsets.UTF_8));  // 将Key从字节数组转为字符串，然后删除该缓存Key
            }
            cursor.close();  // 关闭SCAN游标，释放资源
        } catch (Exception e) {  // 捕获清除缓存过程中的异常
            log.warn("Failed to clear product list cache: {}", e.getMessage());  // 打印警告日志，记录清除缓存失败的信息
        } finally {  // 无论是否异常，都执行finally块
            if (conn != null) {  // 如果连接已创建
                try { conn.close(); } catch (Exception ignored) {}  // 关闭Redis连接，释放连接资源，忽略关闭时的异常
            }
        }
    }
}
