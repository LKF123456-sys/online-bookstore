package com.bookstore.agent.tools;

import com.bookstore.agent.feign.ReviewFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 评价工具集 — 将评价服务 Feign 调用封装为 AI Agent 可调用的 Tool
 *
 * 设计说明：
 *   获取商品评价数据后由 LLM 进行情感分析、摘要生成和优缺点提取。
 *   工具只提供原始数据，分析能力由 LLM 的推理能力实现。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewTools {

    private final ReviewFeignClient reviewFeignClient;

    /**
     * 获取商品评价列表
     * LLM 在用户问"这本书评价怎么样"、"大家觉得XXX好不好"时调用
     */
    @Tool(description = "获取指定图书的用户评价列表，包括评分、评价内容和时间。当用户想了解某本书的口碑、评价、用户反馈时使用此工具。获取到原始评价后，你可以进行情感分析和摘要。")
    public String getProductReviews(
            @ToolParam(description = "图书的商品ID") String productId,
            @ToolParam(description = "页码，从1开始") int pageNum) {
        log.info("【Agent Tool】获取商品评价: productId={}, page={}", productId, pageNum);
        try {
            Result<PageResult<ReviewVO>> result = reviewFeignClient.getProductReviews(productId, pageNum, 10);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                PageResult<ReviewVO> page = result.getData();
                if (page.getRecords() == null || page.getRecords().isEmpty()) {
                    return "该商品暂无用户评价。";
                }
                // 计算平均评分
                double avgRating = page.getRecords().stream()
                        .filter(r -> r.getRating() != null)
                        .mapToInt(ReviewVO::getRating)
                        .average()
                        .orElse(0);
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("共 %d 条评价，平均评分 %.1f 星：\n", page.getTotal(), avgRating));
                for (ReviewVO review : page.getRecords()) {
                    sb.append(String.format("[%d星] %s (评分: %d, 点赞: %d)\n",
                            review.getRating(), review.getContent(),
                            review.getRating(), review.getLikes() != null ? review.getLikes() : 0));
                    if (review.getReply() != null && !review.getReply().isBlank()) {
                        sb.append("  商家回复: ").append(review.getReply()).append("\n");
                    }
                }
                return sb.toString();
            }
            return "获取评价失败。";
        } catch (Exception e) {
            log.error("获取商品评价失败: {}", e.getMessage());
            return "获取商品评价时出现错误: " + e.getMessage();
        }
    }
}
