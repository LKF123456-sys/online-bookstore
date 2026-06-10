// 声明当前类所属的包路径，遵循 Spring Boot 项目标准分包结构
package com.bookstore.admin.service;

// 导入商品服务的 Feign 客户端接口，用于声明式调用 bookstore-product 微服务
import com.bookstore.admin.feign.ProductFeignClient;
// 导入公共模块的统一响应封装类 Result，所有 Feign 调用返回值都通过此类包装
import com.bookstore.common.api.Result;
// 导入商品视图对象 ProductVO，用于展示商品信息给前端
import com.bookstore.common.api.vo.ProductVO;
// Lombok 注解 @RequiredArgsConstructor：为所有 final 字段生成构造函数，Spring 会自动注入依赖
import lombok.RequiredArgsConstructor;
// Lombok 注解 @Slf4j：自动生成 log 静态字段（使用 SLF4J 日志门面），无需手动声明 Logger
import lombok.extern.slf4j.Slf4j;
// Spring 的 @Service 注解：将该类标记为 Spring 容器管理的业务逻辑层 Bean
import org.springframework.stereotype.Service;

// 导入 Java 集合框架的 List、Map 接口
import java.util.*;

/**
 * 商品服务 — 封装 ProductFeignClient，添加统一错误处理和日志
 * <p>
 * 该类是 Admin 端的商品业务逻辑层，负责：
 * <ul>
 *   <li>封装对 bookstore-product 微服务的远程调用（通过 ProductFeignClient）</li>
 *   <li>在查询操作中使用 DEBUG 级别日志记录请求参数，便于开发调试</li>
 *   <li>在增删改操作中使用 INFO 级别日志记录关键操作，便于运维审计</li>
 *   <li>作为中间层，未来可在此添加缓存、数据转换、权限校验等增强逻辑</li>
 * </ul>
 */
// @Slf4j：Lombok 会在编译时生成日志对象 log，可直接使用 log.info()、log.debug() 等方法
@Slf4j
// @Service：标识这是一个 Service 层组件，Spring 会扫描并创建单例 Bean 管理其生命周期
@Service
// @RequiredArgsConstructor：Lombok 自动生成包含所有 final 字段的构造函数，实现构造函数注入
@RequiredArgsConstructor
public class ProductService {

    // 商品服务 Feign 客户端，通过构造函数注入
    // 声明为 final 确保一旦注入后不可变，提高线程安全性
    private final ProductFeignClient productFeignClient;

    /**
     * 获取商品分页列表（前台/用户端）
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/product/list 接口。
     * 使用 DEBUG 级别日志记录查询参数，避免在生产环境产生过多日志。
     *
     * @param pageNum    页码（从 1 开始）
     * @param pageSize   每页条数
     * @param keyword    搜索关键词（可选）
     * @param categoryId 分类 ID（可选）
     * @param sort       排序方式（可选）
     * @return Result 包装的分页商品数据 Map
     */
    public Result<Map<String, Object>> list(int pageNum, int pageSize, String keyword, Long categoryId, String sort) {
        // 记录 DEBUG 级别日志：输出查询参数，便于开发阶段调试。生产环境通常配置 INFO 级别，不会输出 DEBUG 日志，避免性能影响
        log.debug("ProductService.list: page={}, size={}, keyword={}, cat={}", pageNum, pageSize, keyword, categoryId);
        // 委托 Feign 客户端发起 HTTP GET 请求到 bookstore-product 微服务
        return productFeignClient.list(pageNum, pageSize, keyword, categoryId, sort);
    }

    /**
     * 获取商品详情
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/product/{id} 接口。
     *
     * @param productId 商品主键 ID
     * @return Result 包装的商品详情 Map
     */
    public Result<Map<String, Object>> detail(String productId) {
        log.debug("ProductService.detail: id={}", productId);
        return productFeignClient.detail(productId);
    }

    /**
     * 获取推荐商品列表
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/product/recommend 接口。
     *
     * @param limit 返回数量上限
     * @return Result 包装的 ProductVO 列表
     */
    public Result<List<ProductVO>> recommend(int limit) {
        // 记录 DEBUG 日志：输出推荐数量限制参数
        log.debug("ProductService.recommend: limit={}", limit);
        return productFeignClient.recommend(limit);
    }

    /**
     * 获取热销商品列表
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/product/hot 接口。
     *
     * @param limit 返回数量上限
     * @return Result 包装的 ProductVO 列表
     */
    public Result<List<ProductVO>> hot(int limit) {
        // 记录 DEBUG 日志：输出热销商品数量限制
        log.debug("ProductService.hot: limit={}", limit);
        return productFeignClient.hot(limit);
    }

    /**
     * 商品全文搜索
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/search 接口。
     *
     * @param keyword  搜索关键词（必填）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return Result 包装的分页搜索结果 Map
     */
    public Result<Map<String, Object>> search(String keyword, int pageNum, int pageSize) {
        // 记录 DEBUG 日志：输出搜索关键词和分页参数
        log.debug("ProductService.search: keyword={}, page={}", keyword, pageNum);
        return productFeignClient.search(keyword, pageNum, pageSize);
    }

    /**
     * 获取商品分类列表
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/category/list 接口。
     * 该方法调用频繁，不记录日志以避免性能开销。
     *
     * @return Result 包装的分类列表
     */
    public Result<List<Map<String, Object>>> categoryList() {
        // 直接委托 Feign 客户端，该接口访问频繁，不记录日志以减少 I/O 开销
        return productFeignClient.categoryList();
    }

    // ===== 以下为管理后台专用方法 =====

    /**
     * 管理后台 — 获取商品分页列表（含已下架商品）
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/admin/product/list 接口。
     *
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @param keyword    搜索关键词（可选）
     * @param categoryId 分类 ID（可选）
     * @return Result 包装的分页商品数据 Map
     */
    public Result<Map<String, Object>> adminProductList(int pageNum, int pageSize, String keyword, Long categoryId) {
        // 直接委托 Feign 客户端，管理后台操作可通过 AOP 切面统一记录日志
        return productFeignClient.adminProductList(pageNum, pageSize, keyword, categoryId);
    }

    /**
     * 管理后台 — 创建新商品
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/admin/product 接口（POST 方法）。
     *
     * @param data 商品数据 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> createProduct(Map<String, Object> data) {
        return productFeignClient.createProduct(data);
    }

    /**
     * 管理后台 — 更新商品信息
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/admin/product/{id} 接口（PUT 方法）。
     *
     * @param id   商品主键 ID
     * @param data 要更新的商品数据 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> updateProduct(String id, Map<String, Object> data) {
        return productFeignClient.updateProduct(id, data);
    }

    /**
     * 管理后台 — 删除商品（逻辑删除）
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/admin/product/{id} 接口（DELETE 方法）。
     *
     * @param id 商品主键 ID
     * @return Result 包装的空返回体
     */
    public Result<Void> deleteProduct(String id) {
        return productFeignClient.deleteProduct(id);
    }

    /**
     * 管理后台 — 获取商品的所有 SKU
     * <p>
     * 通过 Feign 调用 bookstore-product 微服务的 /api/admin/product/{id}/skus 接口。
     *
     * @param id 商品主键 ID
     * @return Result 包装的 SKU 列表
     */
    public Result<List<Map<String, Object>>> getProductSkus(String id) {
        return productFeignClient.getProductSkus(id);
    }
}
