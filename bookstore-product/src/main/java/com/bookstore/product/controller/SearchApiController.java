package com.bookstore.product.controller;  // 声明当前类所在的包路径，属于商品服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一响应结果包装类，用于封装API返回数据
import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象，用于向前端返回商品数据
import com.bookstore.product.service.SearchService;  // 导入搜索服务接口，提供商品搜索功能
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含所有final字段的构造函数，实现构造函数注入
import org.springframework.web.bind.annotation.*;  // 导入Spring MVC的Web注解，包括@RestController、@GetMapping、@RequestParam等

import java.util.List;  // 导入Java集合框架的List接口，用于表示商品列表

/**
 * 搜索API控制器（面向前端用户）
 * 提供商品搜索的REST接口
 * 所有接口路径前缀为 /api/search
 *
 * 接口列表：
 *   - GET /api/search?keyword=xxx&pageNum=1&pageSize=10  搜索商品
 *
 * 搜索服务有Elasticsearch和数据库两种实现，会根据配置自动选择：
 *   - 如果配置了Elasticsearch连接地址，使用ES全文搜索
 *   - 如果未配置ES，回退到MySQL的LIKE模糊查询
 */
@RestController  // REST控制器注解，标记这是一个控制器类，且所有方法返回值直接作为HTTP响应体（JSON格式）
@RequestMapping("/api/search")  // URL路径前缀，该控制器下所有接口的路径都以 /api/search 开头
@RequiredArgsConstructor  // Lombok注解，自动生成包含所有final字段的构造函数，Spring会通过构造函数自动注入依赖
public class SearchApiController {  // 搜索API控制器类

    private final SearchService searchService;  // 搜索服务对象，通过构造函数注入（final保证不可变），实际注入的是ES或数据库实现

    /**
     * 搜索商品接口
     * 根据关键词搜索商品，支持分页返回结果
     * 搜索会匹配商品名称和描述字段
     *
     * @param keyword 搜索关键词，必填参数，从URL查询参数中获取（如 ?keyword=Java）
     * @param pageNum 页码，从URL查询参数中获取，不传时默认为第1页
     * @param pageSize 每页数量，从URL查询参数中获取，不传时默认每页10条
     * @return 搜索匹配的商品列表，用Result包装为统一格式
     */
    @GetMapping  // GET请求映射，处理 /api/search 的HTTP GET请求（无额外路径，通过查询参数传参）
    public Result<List<ProductVO>> searchProducts(  // 搜索商品的方法，返回商品列表
            @RequestParam String keyword,  // @RequestParam从URL查询参数中获取keyword，必填参数（无defaultValue即为必填）
            @RequestParam(defaultValue = "1") Integer pageNum,  // @RequestParam从URL查询参数中获取pageNum，不传时默认值为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // @RequestParam从URL查询参数中获取pageSize，不传时默认值为10
        return Result.success(searchService.searchProducts(keyword, pageNum, pageSize));  // 调用搜索服务执行搜索，用Result包装成功结果返回
    }
}
