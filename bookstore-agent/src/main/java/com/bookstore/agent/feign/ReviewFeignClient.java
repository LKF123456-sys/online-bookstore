package com.bookstore.agent.feign;

import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ReviewVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 评价服务 Feign 客户端 — AI Agent 调用营销微服务的评价接口
 */
@FeignClient(name = "bookstore-promotion")
public interface ReviewFeignClient {

    /**
     * 获取商品评价列表（分页）
     */
    @GetMapping("/api/review/product/{productId}")
    Result<PageResult<ReviewVO>> getProductReviews(
            @PathVariable("productId") String productId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize);
}
