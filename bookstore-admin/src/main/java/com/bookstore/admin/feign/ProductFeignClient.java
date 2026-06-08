package com.bookstore.admin.feign;

import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.ProductVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品服务 Feign 客户端 — 声明式调用 bookstore-product 微服务
 */
@FeignClient(name = "bookstore-product", path = "/api")
public interface ProductFeignClient {

    /** 商品列表 */
    @GetMapping("/product/list")
    Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "12") int pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String sort);

    /** 商品详情 */
    @GetMapping("/product/{id}")
    Result<Map<String, Object>> detail(@PathVariable Long id);

    /** 推荐商品 */
    @GetMapping("/product/recommend")
    Result<List<ProductVO>> recommend(@RequestParam(defaultValue = "8") int limit);

    /** 热销商品 */
    @GetMapping("/product/hot")
    Result<List<ProductVO>> hot(@RequestParam(defaultValue = "5") int limit);

    /** 商品搜索 */
    @GetMapping("/search")
    Result<Map<String, Object>> search(@RequestParam String keyword,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "12") int pageSize);

    /** 分类列表 */
    @GetMapping("/category/list")
    Result<List<Map<String, Object>>> categoryList();

    // ===== 管理后台接口 =====

    /** 管理后台商品列表 */
    @GetMapping("/admin/product/list")
    Result<Map<String, Object>> adminProductList(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Long categoryId);

    /** 创建商品 */
    @PostMapping("/admin/product")
    Result<Void> createProduct(@RequestBody Map<String, Object> productData);

    /** 更新商品 */
    @PutMapping("/admin/product/{id}")
    Result<Void> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> productData);

    /** 删除商品 */
    @DeleteMapping("/admin/product/{id}")
    Result<Void> deleteProduct(@PathVariable Long id);

    /** 获取商品 SKU */
    @GetMapping("/admin/product/{id}/skus")
    Result<List<Map<String, Object>>> getProductSkus(@PathVariable Long id);
}
