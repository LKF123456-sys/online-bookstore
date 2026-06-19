package com.bookstore.agent.tools;

import com.bookstore.agent.feign.ProductFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品工具集 — 将商品服务 Feign 调用封装为 AI Agent 可调用的 Tool
 *
 * 设计说明：
 *   提供搜索、详情、推荐等工具，LLM 根据用户意图自动选择调用。
 *   搜索结果以格式化文本返回，便于 LLM 整合进自然语言回复。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTools {

    private final ProductFeignClient productFeignClient;

    /**
     * 搜索商品
     * LLM 在用户问"有没有XXX书"、"帮我找XXX"时调用
     */
    @Tool(description = "根据关键词搜索图书商品。当用户想要找某本书、某类书、或提到书名/作者时使用此工具。返回匹配的商品列表。")
    public String searchProducts(
            @ToolParam(description = "搜索关键词，可以是书名、作者名或类别") String keyword) {
        log.info("【Agent Tool】搜索商品: keyword={}", keyword);
        try {
            Result<List<ProductVO>> result = productFeignClient.searchProducts(keyword, 1, 5);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<ProductVO> products = result.getData();
                if (products.isEmpty()) {
                    return "没有找到与「" + keyword + "」相关的图书。";
                }
                StringBuilder sb = new StringBuilder();
                sb.append("搜索「").append(keyword).append("」的结果：\n");
                for (ProductVO p : products) {
                    sb.append(String.format("- 《%s》 作者: %s | 价格: ¥%s | 库存: %d | %s\n",
                            p.getName(), p.getAuthor(), p.getPrice(),
                            p.getStock(), p.getDescription() != null ? truncate(p.getDescription(), 60) : ""));
                }
                return sb.toString();
            }
            return "搜索商品时未获取到结果。";
        } catch (Exception e) {
            log.error("搜索商品失败: {}", e.getMessage());
            return "搜索商品时出现错误: " + e.getMessage();
        }
    }

    /**
     * 获取商品详情
     * LLM 在用户问"这本书怎么样"、"详细介绍一下XXX"时调用
     */
    @Tool(description = "获取指定图书的详细信息，包括名称、作者、价格、库存、详细描述等。当用户想了解某本具体图书的详情时使用此工具。")
    public String getProductDetail(
            @ToolParam(description = "图书的商品ID") String productId) {
        log.info("【Agent Tool】获取商品详情: productId={}", productId);
        try {
            Result<ProductVO> result = productFeignClient.getProductById(productId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                ProductVO p = result.getData();
                return String.format(
                        "《%s》\n作者: %s\n价格: ¥%s\n库存: %d 本\n分类: %s\n简介: %s",
                        p.getName(), p.getAuthor(), p.getPrice(),
                        p.getStock(), p.getCategory(),
                        p.getDescription() != null ? p.getDescription() : "暂无简介");
            }
            return "未找到该商品的详细信息。";
        } catch (Exception e) {
            log.error("获取商品详情失败: {}", e.getMessage());
            return "获取商品详情时出现错误: " + e.getMessage();
        }
    }

    /**
     * 获取推荐商品
     * LLM 在用户问"有什么好书推荐"、"推荐几本书"时调用
     */
    @Tool(description = "获取系统推荐的图书列表。当用户问'有什么好书推荐'、'推荐几本书'或表达想要发现新书的意图时使用此工具。")
    public String getRecommendProducts() {
        log.info("【Agent Tool】获取推荐商品");
        try {
            Result<List<ProductVO>> result = productFeignClient.getRecommendProducts(5);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<ProductVO> products = result.getData();
                if (products.isEmpty()) {
                    return "目前没有推荐商品。";
                }
                StringBuilder sb = new StringBuilder("为您推荐以下图书：\n");
                for (int i = 0; i < products.size(); i++) {
                    ProductVO p = products.get(i);
                    sb.append(String.format("%d. 《%s》 %s | ¥%s\n",
                            i + 1, p.getName(), p.getAuthor(), p.getPrice()));
                }
                return sb.toString();
            }
            return "获取推荐商品失败。";
        } catch (Exception e) {
            log.error("获取推荐商品失败: {}", e.getMessage());
            return "获取推荐商品时出现错误: " + e.getMessage();
        }
    }

    /**
     * 获取热销商品
     * LLM 在用户问"什么书卖得好"、"畅销书"时调用
     */
    @Tool(description = "获取热销图书排行榜。当用户问'什么书卖得好'、'畅销书'、'热门图书'时使用此工具。")
    public String getHotProducts() {
        log.info("【Agent Tool】获取热销商品");
        try {
            Result<List<ProductVO>> result = productFeignClient.getHotProducts(5);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<ProductVO> products = result.getData();
                if (products.isEmpty()) {
                    return "目前没有热销商品数据。";
                }
                StringBuilder sb = new StringBuilder("热销图书排行：\n");
                for (int i = 0; i < products.size(); i++) {
                    ProductVO p = products.get(i);
                    sb.append(String.format("%d. 《%s》 %s | ¥%s | 已售 %d 本\n",
                            i + 1, p.getName(), p.getAuthor(), p.getPrice(),
                            p.getSales() != null ? p.getSales() : 0));
                }
                return sb.toString();
            }
            return "获取热销商品失败。";
        } catch (Exception e) {
            log.error("获取热销商品失败: {}", e.getMessage());
            return "获取热销商品时出现错误: " + e.getMessage();
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        String clean = text.replaceAll("<[^>]+>", ""); // 去 HTML 标签
        return clean.length() > maxLen ? clean.substring(0, maxLen) + "..." : clean;
    }
}
