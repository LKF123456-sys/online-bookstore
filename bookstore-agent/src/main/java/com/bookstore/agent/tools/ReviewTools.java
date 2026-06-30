package com.bookstore.agent.tools; // tools 包：存放 Spring AI @Tool 工具类

// 导入 ReviewFeignClient — 评价微服务 Feign 客户端
import com.bookstore.agent.feign.ReviewFeignClient;
// 导入统一响应 Result<T>
import com.bookstore.common.api.Result;
// 导入分页结果 PageResult<T>
import com.bookstore.common.api.vo.PageResult;
// 导入评价视图对象 ReviewVO，包含 rating（评分）、content（内容）、likes（点赞数）、reply（商家回复）
import com.bookstore.common.api.vo.ReviewVO;
// Lombok 构造器注入
import lombok.RequiredArgsConstructor;
// Lombok 日志
import lombok.extern.slf4j.Slf4j;
// Spring AI @Tool 注解
import org.springframework.ai.tool.annotation.Tool;
// Spring AI @ToolParam 注解
import org.springframework.ai.tool.annotation.ToolParam;
// Spring @Component 注解
import org.springframework.stereotype.Component;

/**
 * 评价工具集 — 将评价微服务的 Feign 调用封装为 LLM 可调用的 Tool 函数
 *
 * 设计思想：工具只负责获取原始数据，不替代 LLM
 *   本工具仅从评价微服务拉取原始评价数据（JSON 数组），并做基本的统计计算（平均评分），
 *   将原始数据以格式化文本返回给 LLM。情感分析、优缺点提取、摘要生成等语义加工
 *   全部由 LLM 的推理能力完成，工具不越界做智能分析。
 *
 * 为什么要计算平均评分？
 *   平均评分是一个简单的数学运算，LLM 不应该被要求做数学计算。
 *   在工具层面算好平均值返回给 LLM，比让 LLM 逐条加总再除以总数更可靠。
 */
@Slf4j // Lombok：自动生成 log 对象
@Component // Spring：标记为 Bean
@RequiredArgsConstructor // Lombok：构造器注入
public class ReviewTools { // 评价工具集类

    private final ReviewFeignClient reviewFeignClient; // 评价微服务 Feign 客户端

    /**
     * 获取商品评价列表工具
     * LLM 调用场景：用户问"《三体》评价怎么样"、"大家觉得这本书好不好"
     *
     * 返回数据包含：
     *   1. 评价总数 + 平均评分（工具层计算的统计信息）
     *   2. 每条评价的评分、内容、点赞数
     *   3. 商家回复（如果有）
     *
     * LLM 拿到这些数据后，会进行：
     *   - 情感倾向分析（正面/中性/负面占比）
     *   - 高频关键词提取
     *   - 优缺点总结
     *   - 生成结构化评价报告
     *
     * @param productId 商品 ID
     * @param pageNum 页码，从 1 开始
     * @return 格式化的评价列表文本，含平均评分
     */
    @Tool(description = "获取指定图书的用户评价列表，包括评分、评价内容和时间。当用户想了解某本书的口碑、评价、用户反馈时使用此工具。获取到原始评价后，你可以进行情感分析和摘要。")
    public String getProductReviews( // 获取商品评价工具
            @ToolParam(description = "图书的商品ID") String productId, // 商品 ID
            @ToolParam(description = "页码，从1开始") int pageNum) { // 分页页码
        log.info("【Agent Tool】获取商品评价: productId={}, page={}", productId, pageNum); // 记录工具调用日志
        try { // 异常捕获
            Result<PageResult<ReviewVO>> result = reviewFeignClient.getProductReviews(productId, pageNum, 10); // Feign 调用评价服务，每页10条
            if (result != null && result.getCode() == 200 && result.getData() != null) { // 检查结果有效性
                PageResult<ReviewVO> page = result.getData(); // 提取分页数据
                if (page.getRecords() == null || page.getRecords().isEmpty()) { // 无评价
                    return "该商品暂无用户评价。"; // 返回空结果提示
                }
                // 计算平均评分 — 使用 Java Stream API 做数学运算，避免 LLM 做不擅长的计算
                double avgRating = page.getRecords().stream() // 转为 Stream<ReviewVO>
                        .filter(r -> r.getRating() != null) // 过滤掉评分为 null 的记录
                        .mapToInt(ReviewVO::getRating) // 方法引用：提取评分（int）
                        .average() // 计算平均值，返回 OptionalDouble
                        .orElse(0); // 如果为空则默认 0
                StringBuilder sb = new StringBuilder(); // 构建格式化输出
                sb.append(String.format("共 %d 条评价，平均评分 %.1f 星：\n", page.getTotal(), avgRating)); // 表头：总数 + 平均分
                for (ReviewVO review : page.getRecords()) { // 遍历评价列表
                    sb.append(String.format("[%d星] %s (评分: %d, 点赞: %d)\n", // 格式化每条评价
                            review.getRating(), review.getContent(), // 星级 + 评价内容
                            review.getRating(), review.getLikes() != null ? review.getLikes() : 0)); // 评分值（重复显示） + 点赞数
                    if (review.getReply() != null && !review.getReply().isBlank()) { // 有商家回复
                        sb.append("  商家回复: ").append(review.getReply()).append("\n"); // 追加回复内容
                    }
                }
                return sb.toString(); // 返回格式化评价列表
            }
            return "获取评价失败。"; // 调用失败
        } catch (Exception e) { // 异常捕获
            log.error("获取商品评价失败: {}", e.getMessage()); // 记录错误
            return "获取商品评价时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }
}
