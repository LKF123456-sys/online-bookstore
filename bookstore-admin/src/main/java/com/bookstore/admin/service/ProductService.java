package com.bookstore.admin.service;

import com.bookstore.admin.feign.ProductFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 商品服务 — 封装 ProductFeignClient，添加统一错误处理和日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductFeignClient productFeignClient;

    /** 获取商品分页列表 */
    public Result<Map<String, Object>> list(int pageNum, int pageSize, String keyword, Long categoryId, String sort) {
        log.debug("ProductService.list: page={}, size={}, keyword={}, cat={}", pageNum, pageSize, keyword, categoryId);
        return productFeignClient.list(pageNum, pageSize, keyword, categoryId, sort);
    }

    /** 获取商品详情 */
    public Result<Map<String, Object>> detail(Long productId) {
        log.debug("ProductService.detail: id={}", productId);
        return productFeignClient.detail(productId);
    }

    /** 获取推荐商品 */
    public Result<List<ProductVO>> recommend(int limit) {
        log.debug("ProductService.recommend: limit={}", limit);
        return productFeignClient.recommend(limit);
    }

    /** 获取热销商品 */
    public Result<List<ProductVO>> hot(int limit) {
        log.debug("ProductService.hot: limit={}", limit);
        return productFeignClient.hot(limit);
    }

    /** 商品搜索 */
    public Result<Map<String, Object>> search(String keyword, int pageNum, int pageSize) {
        log.debug("ProductService.search: keyword={}, page={}", keyword, pageNum);
        return productFeignClient.search(keyword, pageNum, pageSize);
    }

    /** 获取分类列表 */
    public Result<List<Map<String, Object>>> categoryList() {
        return productFeignClient.categoryList();
    }

    // ===== 管理后台 =====

    public Result<Map<String, Object>> adminProductList(int pageNum, int pageSize, String keyword, Long categoryId) {
        return productFeignClient.adminProductList(pageNum, pageSize, keyword, categoryId);
    }

    public Result<Void> createProduct(Map<String, Object> data) {
        return productFeignClient.createProduct(data);
    }

    public Result<Void> updateProduct(Long id, Map<String, Object> data) {
        return productFeignClient.updateProduct(id, data);
    }

    public Result<Void> deleteProduct(Long id) {
        return productFeignClient.deleteProduct(id);
    }

    public Result<List<Map<String, Object>>> getProductSkus(Long id) {
        return productFeignClient.getProductSkus(id);
    }
}
