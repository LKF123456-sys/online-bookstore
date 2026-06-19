package com.bookstore.agent.feign;

import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.ProductVO;
import com.bookstore.common.api.vo.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商品服务 Feign 客户端 — AI Agent 调用商品微服务的工具接口
 */
@FeignClient(name = "bookstore-product")
public interface ProductFeignClient {

    /**
     * 搜索商品（全文检索）
     */
    @GetMapping("/api/search")
    Result<List<ProductVO>> searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize);

    /**
     * 获取商品详情
     */
    @GetMapping("/api/product/{id}")
    Result<ProductVO> getProductById(@PathVariable("id") String id);

    /**
     * 获取推荐商品
     */
    @GetMapping("/api/product/recommend")
    Result<List<ProductVO>> getRecommendProducts(
            @RequestParam(value = "limit", defaultValue = "5") int limit);

    /**
     * 获取热销商品
     */
    @GetMapping("/api/product/hot")
    Result<List<ProductVO>> getHotProducts(
            @RequestParam(value = "limit", defaultValue = "5") int limit);

    /**
     * 商品列表（分页、筛选）
     */
    @GetMapping("/api/product/list")
    Result<PageResult<ProductVO>> listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize);
}
