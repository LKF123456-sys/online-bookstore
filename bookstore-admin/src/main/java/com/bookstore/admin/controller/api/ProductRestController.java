package com.bookstore.admin.controller.api;

import com.bookstore.admin.service.ProductService;
import com.bookstore.common.api.Result;
import com.bookstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 商品 REST API — 为 Vue 前端提供商品相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    /** 商品分页列表 */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "12") int pageSize,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String sort) {
        return productService.list(pageNum, pageSize, keyword, categoryId, sort);
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return productService.detail(id);
    }

    /** 推荐商品 */
    @GetMapping("/recommend")
    public Result<?> recommend(@RequestParam(defaultValue = "8") int limit) {
        return productService.recommend(limit);
    }

    /** 热销商品 */
    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "5") int limit) {
        return productService.hot(limit);
    }

    /** 商品搜索 */
    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword,
                            @RequestParam(defaultValue = "1") int pageNum,
                            @RequestParam(defaultValue = "12") int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException(400, "搜索关键词不能为空");
        }
        return productService.search(keyword.trim(), pageNum, pageSize);
    }

    /** 分类列表 */
    @GetMapping("/categories")
    public Result<?> categories() {
        return productService.categoryList();
    }
}
