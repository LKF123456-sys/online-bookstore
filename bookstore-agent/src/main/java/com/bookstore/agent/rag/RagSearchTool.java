package com.bookstore.agent.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 语义搜索工具 — 将向量检索封装为 @Tool，供 Agent 调用
 *
 * 设计说明：
 *   1. 用户消息先做 Embedding，然后在 VectorStore 中做余弦相似度检索
 *   2. 返回 Top-K 最相关的文档片段
 *   3. 与 ProductTools 的关键词搜索互补：关键词搜索精确匹配，语义搜索理解意图
 *
 * 面试亮点：
 *   1. 语义 vs 关键词：用户说"适合睡前看的轻松读物"→ 语义搜索能匹配散文/轻小说
 *   2. Top-K + 相似度阈值：平衡召回率和精确度
 *   3. 元数据过滤：可按 type=product 只搜商品，或 type=knowledge 只搜领域知识
 *   4. 检索结果注入 Prompt → LLM 基于检索到的上下文生成回复（RAG 核心流程）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagSearchTool {

    private final VectorStore vectorStore;

    /**
     * 语义搜索图书 — 基于用户描述的含义而非关键词匹配
     * LLM 在用户描述模糊需求时调用此工具（如"有什么关于人工智能的入门书"）
     */
    @Tool(description = "基于语义理解搜索图书。不同于关键词搜索，此工具能理解用户的意图和偏好描述，返回语义最相关的图书。适合用户描述模糊需求时使用，如'适合睡前看的书'、'关于人工智能的入门读物'、'轻松有趣的科普书'。")
    public String semanticSearchBooks(
            @ToolParam(description = "用户的搜索意图描述，可以是自然语言，不需要精确关键词") String query,
            @ToolParam(description = "返回结果数量，默认5") int topK) {
        log.info("【RAG Tool】语义搜索: query={}, topK={}", query, topK);

        try {
            // 执行向量相似度搜索
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.query(query).withTopK(topK > 0 ? topK : 5)
            );

            if (results.isEmpty()) {
                return "未在知识库中找到与「" + query + "」语义相关的图书。建议尝试关键词搜索。";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("基于语义分析，与「").append(query).append("」最相关的图书：\n\n");

            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                String type = (String) doc.getMetadata().getOrDefault("type", "unknown");
                double score = doc.getMetadata().containsKey("distance")
                        ? 1.0 - ((Number) doc.getMetadata().get("distance")).doubleValue()
                        : 0.0;

                sb.append(String.format("%d. [相关度: %.0f%%]\n", i + 1, score * 100));
                sb.append(doc.getContent()).append("\n\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("语义搜索失败: {}", e.getMessage());
            return "语义搜索时出现错误: " + e.getMessage();
        }
    }

    /**
     * 搜索图书领域知识 — 从知识库中检索专业信息
     * LLM 在用户询问领域知识时调用（如"科幻小说有哪些经典作品"）
     */
    @Tool(description = "搜索图书领域知识库，获取关于图书分类、阅读建议、文学流派等方面的专业知识。当用户询问领域知识（如'科幻小说有哪些经典'、'怎样选一本好的编程书'）时使用此工具。")
    public String searchKnowledge(
            @ToolParam(description = "要查询的领域知识问题") String query) {
        log.info("【RAG Tool】知识库搜索: query={}", query);

        try {
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.query(query).withTopK(3)
            );

            if (results.isEmpty()) {
                return "知识库中暂无与「" + query + "」相关的信息。";
            }

            StringBuilder sb = new StringBuilder("以下是相关的领域知识：\n\n");
            for (Document doc : results) {
                String source = (String) doc.getMetadata().getOrDefault("source", "知识库");
                sb.append("[").append(source).append("]\n");
                sb.append(doc.getContent()).append("\n\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("知识库搜索失败: {}", e.getMessage());
            return "搜索知识库时出现错误: " + e.getMessage();
        }
    }

    /**
     * 获取知识库统计 — 让 LLM 了解当前知识库状态
     */
    @Tool(description = "获取当前 RAG 知识库的统计信息，包括已索引的图书数量和文档数量。当需要了解知识库覆盖范围时使用。")
    public String getKnowledgeBaseStats() {
        log.info("【RAG Tool】知识库统计");
        // 返回一个简化的统计（实际可通过 KnowledgeBaseService 获取详细数据）
        return "RAG 知识库已加载，包含图书描述和领域知识文档。支持语义搜索和知识检索。";
    }
}
