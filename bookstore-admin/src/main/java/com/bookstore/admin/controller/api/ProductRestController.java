package com.bookstore.admin.controller.api;

import com.bookstore.admin.service.ProductService;
import com.bookstore.common.api.Result;
import com.bookstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 商品 REST API 控制器
 * <p>
 * 职责：为 Vue 前端提供商品相关的 RESTful 接口，包括商品分页列表、
 *       商品详情、推荐商品、热销商品、商品搜索、分类列表等只读查询功能。
 * <p>
 * 所属模块：bookstore-admin · controller · api
 * <p>
 * 说明：本控制器仅提供商品查询类接口（GET 请求），不涉及商品的增删改，
 *       增加 / 修改 / 删除操作由后台管理端另行的管理接口负责。
 * <p>
 * 包含接口：
 * <ul>
 *   <li>GET /api/products             — 商品分页列表（支持关键词 / 分类 / 排序）</li>
 *   <li>GET /api/products/{id}        — 商品详情</li>
 *   <li>GET /api/products/recommend   — 推荐商品</li>
 *   <li>GET /api/products/hot         — 热销商品</li>
 *   <li>GET /api/products/search      — 商品搜索</li>
 *   <li>GET /api/products/categories  — 分类列表</li>
 * </ul>
 *
 * @author bookstore
 */
// @Slf4j：Lombok 注解，自动生成 log 日志对象，用于记录运行时日志
@Slf4j
// @RestController：Spring MVC 注解，标识该类为 REST 控制器，
// 所有方法返回值自动序列化为 JSON 响应体
@RestController
// @RequestMapping：将控制器映射到 /api/products 路径下，所有接口 URL 以此为前缀
@RequestMapping("/api/products")
// @RequiredArgsConstructor：Lombok 注解，为所有 final 字段生成构造方法，
// Spring 自动注入对应的 Bean
@RequiredArgsConstructor
public class ProductRestController {

    // 商品服务层依赖，处理商品查询、搜索、分类等核心业务逻辑
    private final ProductService productService;

    // ========================================================================
    // 商品分页列表接口
    // ========================================================================

    /**
     * 获取商品分页列表
     * <p>
     * 支持可选的搜索关键词、分类筛选和排序方式，返回分页后的商品数据。
     *
     * @param pageNum    页码，默认第 1 页
     * @param pageSize   每页条数，默认 12 条
     * @param keyword    搜索关键词（可选），用于模糊匹配商品名称
     * @param categoryId 分类 ID（可选），用于按分类筛选商品
     * @param sort       排序方式（可选），如 "price_asc" 价格升序、"price_desc" 价格降序等
     * @return Result 包含分页商品数据（列表 + 分页信息）的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/products
    @GetMapping
    // @RequestParam：从 URL 查询参数中获取值
    // defaultValue 指定参数缺省时的默认值，required=false 表示可选参数
    public Result<?> list(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "12") int pageSize,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String sort) {
        // 委托 ProductService 执行分页查询，传入所有可选筛选条件
        return productService.list(pageNum, pageSize, keyword, categoryId, sort);
    }

    // ========================================================================
    // 商品详情接口
    // ========================================================================

    /**
     * 获取指定商品的详细信息
     *
     * @param id 商品主键 ID，由路径变量 {id} 解析
     * @return Result 包含商品详细信息的成功响应（商品基本信息 + 描述 + 图片等）
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/products/{id}
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable String id) {
        return productService.detail(id);
    }

    // ========================================================================
    // 推荐商品接口
    // ========================================================================

    /**
     * 获取推荐商品列表
     * <p>
     * 通常基于销量、评分等指标排序，返回指定数量的热门推荐商品。
     *
     * @param limit 返回商品数量上限，默认 8 条
     * @return Result 包含推荐商品列表的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/products/recommend
    @GetMapping("/recommend")
    public Result<?> recommend(@RequestParam(defaultValue = "8") int limit) {
        // 委托 ProductService 获取推荐商品
        return productService.recommend(limit);
    }

    // ========================================================================
    // 热销商品接口
    // ========================================================================

    /**
     * 获取热销商品列表
     * <p>
     * 按销量排序，返回当前最热卖的商品。
     *
     * @param limit 返回商品数量上限，默认 5 条
     * @return Result 包含热销商品列表的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/products/hot
    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "5") int limit) {
        // 委托 ProductService 获取热销商品
        return productService.hot(limit);
    }

    // ========================================================================
    // 商品搜索接口
    // ========================================================================

    /**
     * 根据关键词搜索商品
     * <p>
     * 对关键词做空白校验，空白或空字符串时直接抛出参数异常。
     *
     * @param keyword  搜索关键词（必填），不能为空或全空白
     * @param pageNum  页码，默认第 1 页
     * @param pageSize 每页条数，默认 12 条
     * @return Result 包含搜索结果分页数据的成功响应
     * @throws BusinessException(400) 当关键词为空或全空白时
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/products/search
    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword,
                            @RequestParam(defaultValue = "1") int pageNum,
                            @RequestParam(defaultValue = "12") int pageSize) {
        // 校验关键词不能为 null 或全空白字符串，防止无效搜索请求
        if (keyword == null || keyword.trim().isEmpty()) {
            // 关键词无效，抛出 400 参数错误，由全局异常处理器返回给前端
            throw new BusinessException(400, "搜索关键词不能为空");
        }
        // 去除关键词首尾空白后，委托 ProductService 执行搜索
        return productService.search(keyword.trim(), pageNum, pageSize);
    }

    // ========================================================================
    // 分类列表接口
    // ========================================================================

    /**
     * 获取所有商品分类列表
     * <p>
     * 用于前端展示分类导航，返回全部可用分类（通常为树形结构）。
     *
     * @return Result 包含分类列表的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/products/categories
    @GetMapping("/categories")
    public Result<?> categories() {
        // 委托 ProductService 查询全部分类
        return productService.categoryList();
    }
}
