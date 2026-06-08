package com.bookstore.product.controller;  // 声明当前类所在的包路径，属于商品服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一响应结果包装类
import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象，用于返回搜索结果
import com.bookstore.product.service.ElasticsearchService;  // 导入Elasticsearch搜索服务类
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成构造函数
import org.springframework.web.bind.annotation.*;  // 导入Spring MVC的Web注解

import java.util.List;  // 导入Java集合框架的List接口

/**
 * 搜索API控制器（面向前端用户）
 * 提供基于Elasticsearch的商品全文搜索接口
 * 支持对商品名称和描述进行模糊搜索，具备自动纠错能力
 * 所有接口路径前缀为 /api/search
 *
 * 接口列表：
 *   - GET /api/search?keyword=xxx&pageNum=1&pageSize=10  搜索商品
 */
@RestController  // REST控制器注解，返回JSON格式数据
@RequestMapping("/api/search")  // URL路径前缀，该控制器下所有接口以 /api/search 开头
@RequiredArgsConstructor  // Lombok注解，自动生成构造函数，实现依赖注入
public class SearchApiController {  // 搜索API控制器类

    private final ElasticsearchService elasticsearchService;  // Elasticsearch搜索服务，通过构造函数注入

    /**
     * 商品搜索接口
     * 使用Elasticsearch进行全文搜索，支持对商品名称和描述的模糊匹配
     * 具备自动纠错（fuzziness）能力，即使用户输入有误也能返回相关结果
     *
     * @param keyword 搜索关键词，必填参数
     * @param pageNum 页码，从1开始，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 搜索匹配的商品列表
     */
    @GetMapping  // GET请求映射，处理 /api/search 请求（无额外路径）
    public Result<List<ProductVO>> searchProducts(  // 返回搜索结果列表
            @RequestParam String keyword,  // @RequestParam从URL查询参数中获取keyword，必填
            @RequestParam(defaultValue = "1") Integer pageNum,  // 页码参数，不传时默认为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 每页数量参数，不传时默认为10
        return Result.success(elasticsearchService.searchProducts(keyword, pageNum, pageSize));  // 调用ES搜索服务，包装结果返回
    }
}
