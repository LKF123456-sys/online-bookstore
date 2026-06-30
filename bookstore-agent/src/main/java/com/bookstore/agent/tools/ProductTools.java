package com.bookstore.agent.tools; // tools 包：存放 Spring AI @Tool 工具类

// 导入 ProductFeignClient — 商品微服务 Feign 客户端
import com.bookstore.agent.feign.ProductFeignClient;
// 导入统一响应 Result<T>
import com.bookstore.common.api.Result;
// 导入商品视图对象 ProductVO
import com.bookstore.common.api.vo.ProductVO;
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

// Java List 集合接口
import java.util.List;

/**
 * 商品工具集 — 将商品微服务的 Feign 调用封装为 LLM 可调用的 Tool 函数
 *
 * 工具清单：
 *   ┌─────────────────────┬─────────────────────────────────────────────┐
 *   │ 工具方法            │ LLM 调用场景                                │
 *   ├─────────────────────┼─────────────────────────────────────────────┤
 *   │ searchProducts      │ "有没有《三体》"、"帮我搜刘慈欣的书"       │
 *   │ getProductDetail    │ "《三体》这本书多少钱"、"详细介绍一下"      │
 *   │ getRecommendProducts│ "有什么好书推荐"、"推荐几本"                │
 *   │ getHotProducts      │ "什么书卖得好"、"畅销书排行"                │
 *   └─────────────────────┴─────────────────────────────────────────────┘
 *
 * 数据返回格式：
 *   所有工具返回格式化字符串（而非原始 JSON），便于 LLM 直接理解和整合到自然语言回复中。
 *   搜索结果包含书名、作者、价格、库存、简介（截断后），推荐/热销结果带有序号排名。
 */
@Slf4j // Lombok：自动生成 log 对象
@Component // Spring：标记为 Bean
@RequiredArgsConstructor // Lombok：构造器注入
public class ProductTools { // 商品工具集类

    private final ProductFeignClient productFeignClient; // 商品微服务 Feign 客户端

    /**
     * 关键词搜索商品工具
     * LLM 调用场景：用户明确提到书名或作者名时
     * 与 RagSearchTool.semanticSearchBooks 的区别：
     *   本工具做精确关键词匹配（MySQL LIKE/全文索引），RAG 工具做语义近似搜索
     *
     * @param keyword 搜索关键词（书名、作者名或类别）
     * @return 格式化搜索结果的文本
     */
    @Tool(description = "根据关键词搜索图书商品。当用户想要找某本书、某类书、或提到书名/作者时使用此工具。返回匹配的商品列表。")
    public String searchProducts( // 关键词搜索工具
            @ToolParam(description = "搜索关键词，可以是书名、作者名或类别") String keyword) { // 搜索关键词
        log.info("【Agent Tool】搜索商品: keyword={}", keyword); // 记录工具调用日志
        try { // 异常捕获
            Result<List<ProductVO>> result = productFeignClient.searchProducts(keyword, 1, 5); // Feign 调用搜索接口，第1页，每页5条
            if (result != null && result.getCode() == 200 && result.getData() != null) { // 检查结果有效性
                List<ProductVO> products = result.getData(); // 提取商品列表
                if (products.isEmpty()) { // 无匹配结果
                    return "没有找到与「" + keyword + "」相关的图书。"; // 返回空结果提示
                }
                StringBuilder sb = new StringBuilder(); // 构建格式化输出
                sb.append("搜索「").append(keyword).append("」的结果：\n"); // 搜索结果标题
                for (ProductVO p : products) { // 遍历商品列表
                    sb.append(String.format("- 《%s》 作者: %s | 价格: ¥%s | 库存: %d | %s\n", // 格式化每条结果
                            p.getName(), p.getAuthor(), p.getPrice(), // 书名、作者、价格
                            p.getStock(), p.getDescription() != null ? truncate(p.getDescription(), 60) : "")); // 库存、简介（截断60字）
                }
                return sb.toString(); // 返回格式化搜索结果
            }
            return "搜索商品时未获取到结果。"; // 调用失败
        } catch (Exception e) { // 异常捕获
            log.error("搜索商品失败: {}", e.getMessage()); // 记录错误
            return "搜索商品时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }

    /**
     * 获取商品详情工具
     * LLM 调用场景：用户想了解某本具体图书的详细信息
     *
     * @param productId 商品 ID
     * @return 格式化的商品详情文本
     */
    @Tool(description = "获取指定图书的详细信息，包括名称、作者、价格、库存、详细描述等。当用户想了解某本具体图书的详情时使用此工具。")
    public String getProductDetail( // 获取商品详情工具
            @ToolParam(description = "图书的商品ID") String productId) { // 商品 ID
        log.info("【Agent Tool】获取商品详情: productId={}", productId); // 记录日志
        try { // 异常捕获
            Result<ProductVO> result = productFeignClient.getProductById(productId); // Feign 调用获取详情
            if (result != null && result.getCode() == 200 && result.getData() != null) { // 检查结果
                ProductVO p = result.getData(); // 提取商品数据
                return String.format( // 格式化详情文本
                        "《%s》\n作者: %s\n价格: ¥%s\n库存: %d 本\n分类: %s\n简介: %s", // 结构化输出
                        p.getName(), p.getAuthor(), p.getPrice(), // 书名、作者、价格
                        p.getStock(), p.getCategory(), // 库存、分类
                        p.getDescription() != null ? p.getDescription() : "暂无简介"); // 简介（处理 null）
            }
            return "未找到该商品的详细信息。"; // 商品不存在
        } catch (Exception e) { // 异常捕获
            log.error("获取商品详情失败: {}", e.getMessage()); // 记录错误
            return "获取商品详情时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }

    /**
     * 获取推荐商品工具
     * LLM 调用场景：用户表达发现新书的意图
     *
     * @return 格式化的推荐图书列表
     */
    @Tool(description = "获取系统推荐的图书列表。当用户问'有什么好书推荐'、'推荐几本书'或表达想要发现新书的意图时使用此工具。")
    public String getRecommendProducts() { // 推荐商品工具（无参数）
        log.info("【Agent Tool】获取推荐商品"); // 记录日志
        try { // 异常捕获
            Result<List<ProductVO>> result = productFeignClient.getRecommendProducts(5); // Feign 调用获取推荐（5本）
            if (result != null && result.getCode() == 200 && result.getData() != null) { // 检查结果
                List<ProductVO> products = result.getData(); // 提取商品列表
                if (products.isEmpty()) { // 无推荐数据
                    return "目前没有推荐商品。"; // 返回空结果
                }
                StringBuilder sb = new StringBuilder("为您推荐以下图书：\n"); // 推荐标题
                for (int i = 0; i < products.size(); i++) { // 遍历带序号
                    ProductVO p = products.get(i); // 获取当前商品
                    sb.append(String.format("%d. 《%s》 %s | ¥%s\n", // 带序号的格式化输出
                            i + 1, p.getName(), p.getAuthor(), p.getPrice())); // 序号、书名、作者、价格
                }
                return sb.toString(); // 返回推荐列表
            }
            return "获取推荐商品失败。"; // 调用失败
        } catch (Exception e) { // 异常捕获
            log.error("获取推荐商品失败: {}", e.getMessage()); // 记录错误
            return "获取推荐商品时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }

    /**
     * 获取热销商品工具
     * LLM 调用场景：用户问"什么书卖得好"、"畅销书有哪些"
     *
     * @return 格式化的热销排行列表
     */
    @Tool(description = "获取热销图书排行榜。当用户问'什么书卖得好'、'畅销书'、'热门图书'时使用此工具。")
    public String getHotProducts() { // 热销商品工具（无参数）
        log.info("【Agent Tool】获取热销商品"); // 记录日志
        try { // 异常捕获
            Result<List<ProductVO>> result = productFeignClient.getHotProducts(5); // Feign 调用获取热销（5本）
            if (result != null && result.getCode() == 200 && result.getData() != null) { // 检查结果
                List<ProductVO> products = result.getData(); // 提取商品列表
                if (products.isEmpty()) { // 无热销数据
                    return "目前没有热销商品数据。"; // 返回空结果
                }
                StringBuilder sb = new StringBuilder("热销图书排行：\n"); // 热销标题
                for (int i = 0; i < products.size(); i++) { // 遍历带排名
                    ProductVO p = products.get(i); // 获取当前商品
                    sb.append(String.format("%d. 《%s》 %s | ¥%s | 已售 %d 本\n", // 带销量排名
                            i + 1, p.getName(), p.getAuthor(), p.getPrice(), // 序号、书名、作者、价格
                            p.getSales() != null ? p.getSales() : 0)); // 销量（处理 null）
                }
                return sb.toString(); // 返回热销排行
            }
            return "获取热销商品失败。"; // 调用失败
        } catch (Exception e) { // 异常捕获
            log.error("获取热销商品失败: {}", e.getMessage()); // 记录错误
            return "获取热销商品时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }

    /**
     * 文本截断工具方法
     * 去除 HTML 标签后截断到指定长度，用于搜索结果中缩短显示描述。
     * 被截断的文本末尾追加 "..." 提示用户内容不完整。
     *
     * @param text 原始文本（可能含 HTML 标签）
     * @param maxLen 最大长度
     * @return 截断后的纯文本
     */
    private String truncate(String text, int maxLen) { // 私有截断方法
        if (text == null) return ""; // null 安全检查
        String clean = text.replaceAll("<[^>]+>", ""); // 正则移除所有 HTML 标签（如 <p>、<br/>）
        return clean.length() > maxLen ? clean.substring(0, maxLen) + "..." : clean; // 超过最大长度则截断加省略号
    }
}
