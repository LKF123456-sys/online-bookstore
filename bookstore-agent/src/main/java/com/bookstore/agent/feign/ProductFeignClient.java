package com.bookstore.agent.feign; // feign 包：OpenFeign 声明式 HTTP 客户端

// 导入统一响应
import com.bookstore.common.api.Result;
// 导入商品 VO
import com.bookstore.common.api.vo.ProductVO;
// 导入分页结果
import com.bookstore.common.api.vo.PageResult;
// @FeignClient 注解
import org.springframework.cloud.openfeign.FeignClient;
// Spring MVC 注解
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

// Java List 接口
import java.util.List;

/**
 * 商品服务 Feign 客户端 — AI Agent 调用商品微服务（bookstore-product）的工具接口
 *
 * 提供搜索、详情、推荐、热销等商品查询能力。
 * 注意：商品查询不需要 userId（公开数据），因此接口中不传递 X-User-Id。
 *
 * 容错设计：
 *   fallbackFactory = ProductFeignFallbackFactory.class
 *   当商品服务不可用时，返回空产品或错误提示，不影响其他 Agent 功能。
 */
@FeignClient(name = "bookstore-product", fallbackFactory = ProductFeignFallbackFactory.class) // Feign 客户端：服务名 + 降级工厂
public interface ProductFeignClient { // 商品服务 Feign 客户端

    /**
     * 搜索商品（全文检索）
     * 调用 bookstore-product 的 GET /api/search?keyword=xxx
     *
     * @param keyword 搜索关键词（书名/作者/分类）
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     * @return Result<List<ProductVO>> 匹配的商品列表
     */
    @GetMapping("/api/search") // GET 搜索端点
    Result<List<ProductVO>> searchProducts( // 搜索商品方法
            @RequestParam("keyword") String keyword, // 搜索关键词
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, // 页码
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize); // 每页条数

    /**
     * 获取商品详情
     * 调用 bookstore-product 的 GET /api/product/{id}
     *
     * @param id 商品 ID
     * @return Result<ProductVO> 商品完整信息
     */
    @GetMapping("/api/product/{id}") // GET 详情端点
    Result<ProductVO> getProductById(@PathVariable("id") String id); // 路径参数：商品 ID

    /**
     * 获取推荐商品
     * 调用 bookstore-product 的 GET /api/product/recommend?limit=5
     *
     * @param limit 返回数量，默认 5
     * @return Result<List<ProductVO>> 推荐商品列表
     */
    @GetMapping("/api/product/recommend") // GET 推荐端点
    Result<List<ProductVO>> getRecommendProducts( // 获取推荐商品
            @RequestParam(value = "limit", defaultValue = "5") int limit); // 数量限制

    /**
     * 获取热销商品
     * 调用 bookstore-product 的 GET /api/product/hot?limit=5
     *
     * @param limit 返回数量，默认 5
     * @return Result<List<ProductVO>> 热销商品排行
     */
    @GetMapping("/api/product/hot") // GET 热销端点
    Result<List<ProductVO>> getHotProducts( // 获取热销商品
            @RequestParam(value = "limit", defaultValue = "5") int limit); // 数量限制

    /**
     * 商品列表（分页、多条件筛选）
     * 调用 bookstore-product 的 GET /api/product/list
     * 支持按关键词、分类、价格区间筛选
     *
     * @param keyword 搜索关键词（可选）
     * @param category 分类筛选（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     * @return Result<PageResult<ProductVO>> 分页商品列表
     */
    @GetMapping("/api/product/list") // GET 列表端点（多条件筛选）
    Result<PageResult<ProductVO>> listProducts( // 商品列表方法
            @RequestParam(value = "keyword", required = false) String keyword, // 关键词（可选）
            @RequestParam(value = "category", required = false) String category, // 分类（可选）
            @RequestParam(value = "minPrice", required = false) Double minPrice, // 最低价（可选）
            @RequestParam(value = "maxPrice", required = false) Double maxPrice, // 最高价（可选）
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, // 页码
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize); // 每页条数
}
