package com.bookstore.agent.feign; // feign 包：OpenFeign 声明式 HTTP 客户端

// 导入统一响应 Result<T>
import com.bookstore.common.api.Result;
// 导入分页结果 PageResult<T>
import com.bookstore.common.api.vo.PageResult;
// 导入评价视图对象 ReviewVO
import com.bookstore.common.api.vo.ReviewVO;
// @FeignClient 注解
import org.springframework.cloud.openfeign.FeignClient;
// Spring MVC 注解
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 评价服务 Feign 客户端 — AI Agent 调用促销/评价微服务（bookstore-promotion）的工具接口
 *
 * 注意：此处服务名为 bookstore-promotion 而非 bookstore-review，
 *   因为评价功能归属于促销服务模块，统一管理用户评价、促销活动等营销相关功能。
 *   评价数据是公开的（任何人可查看任意图书的评价），因此不传用户身份标识。
 *
 * 容错设计：
 *   fallbackFactory = ReviewFeignFallbackFactory.class
 *   下游不可用时返回空评价数据，不影响对话体验。
 */
@FeignClient(name = "bookstore-promotion", fallbackFactory = ReviewFeignFallbackFactory.class) // Feign 客户端：促销服务 + 降级工厂
public interface ReviewFeignClient { // 评价服务 Feign 客户端

    /**
     * 获取商品评价列表（分页）
     * 调用 bookstore-promotion 的 GET /api/review/product/{productId}
     *
     * @param productId 商品 ID（路径参数）
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @return Result<PageResult<ReviewVO>> 分页评价列表，含评分、内容、点赞、商家回复
     */
    @GetMapping("/api/review/product/{productId}") // GET 映射：{productId} 为路径变量
    Result<PageResult<ReviewVO>> getProductReviews( // 获取商品评价方法
            @PathVariable("productId") String productId, // 路径参数：商品 ID
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, // 页码，默认1
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize); // 每页条数，默认10
}
