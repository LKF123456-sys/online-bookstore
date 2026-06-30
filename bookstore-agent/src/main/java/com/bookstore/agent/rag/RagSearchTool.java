package com.bookstore.agent.rag; // rag 包：存放 RAG（检索增强生成）相关代码

// Lombok 构造器注入
import lombok.RequiredArgsConstructor;
// Lombok 日志
import lombok.extern.slf4j.Slf4j;
// Spring AI Document — 向量存储中检索返回的文档对象，包含文本内容和元数据
import org.springframework.ai.document.Document;
// Spring AI @Tool 注解 — 注册为 LLM 可调用的工具
import org.springframework.ai.tool.annotation.Tool;
// Spring AI @ToolParam 注解 — 描述工具参数
import org.springframework.ai.tool.annotation.ToolParam;
// Spring AI SearchRequest — 向量搜索请求构建器，包含 query、topK、similarityThreshold
import org.springframework.ai.vectorstore.SearchRequest;
// Spring AI VectorStore — 向量存储接口
import org.springframework.ai.vectorstore.VectorStore;
// Spring @Component 注解
import org.springframework.stereotype.Component;

// Java List 接口
import java.util.List;

/**
 * RAG 语义搜索工具 — 将向量检索封装为 @Tool，供 LLM 调用
 *
 * 与 ProductTools 关键词搜索的对比：
 *   ┌──────────────────┬─────────────────────┬─────────────────────────┐
 *   │ 维度             │ ProductTools（关键词）│ RagSearchTool（语义）    │
 *   ├──────────────────┼─────────────────────┼─────────────────────────┤
 *   │ 匹配方式         │ MySQL LIKE/全文索引  │ 向量余弦相似度           │
 *   │ 输入要求         │ 精确书名/作者        │ 自然语言描述即可         │
 *   │ 模糊意图支持     │ ❌ 不支持            │ ✅ 核心能力             │
 *   │ 典型场景         │ "搜索《三体》"       │ "适合睡前看的轻松读物"   │
 *   │ 延迟             │ 低（索引查询）       │ 稍高（Embedding+检索）   │
 *   └──────────────────┴─────────────────────┴─────────────────────────┘
 *
 * RAG 在线阶段流程（用户查询时）：
 *   1. 用户输入自然语言查询（如"关于人工智能的入门书"）
 *   2. LLM 判断需调用 semanticSearchBooks 工具
 *   3. Spring AI 将查询文本 Embedding 为向量
 *   4. 在 RedisVectorStore 中执行 HNSW 近似最近邻搜索
 *   5. 返回 Top-K 语义最相似的文档片段
 *   6. LLM 基于检索到的文档上下文生成自然语言回复
 *
 * 面试亮点：
 *   1. 语义 vs 关键词互补：模糊意图走语义搜索，精确匹配走关键词
 *   2. Top-K + 相似度：控制召回数量和质量
 *   3. 元数据过滤：可按 type=product / type=knowledge 区分搜索目标
 *   4. 完整 RAG 闭环：Query → Embedding → Vector Search → Context → LLM Generation
 */
@Slf4j // Lombok：自动生成 log
@Component // Spring：标记为 Bean
@RequiredArgsConstructor // Lombok：构造器注入
public class RagSearchTool { // RAG 语义搜索工具类

    private final VectorStore vectorStore; // 向量存储（RedisVectorStore），由 RagConfig 创建

    /**
     * 语义搜索图书 — 基于用户意图描述而非精确关键词
     *
     * LLM 调用场景：用户描述模糊需求时，如：
     *   - "有没有关于人工智能的入门书"
     *   - "适合亲子共读的绘本推荐"
     *   - "跟《小王子》类似的治愈系读物"
     *
     * 实现流程：
     *   1. 用用户查询文本构建 SearchRequest（query + topK）
     *   2. VectorStore.similaritySearch 自动处理 Embedding → HNSW 检索
     *   3. 结果按相似度降序排列
     *   4. 格式化输出：序号 + 相关度百分比 + 文档内容
     *
     * @param query 用户的自然语言搜索描述
     * @param topK 返回结果数量，默认 5
     * @return 格式化的语义搜索结果文本
     */
    @Tool(description = "基于语义理解搜索图书。不同于关键词搜索，此工具能理解用户的意图和偏好描述，返回语义最相关的图书。适合用户描述模糊需求时使用，如'适合睡前看的书'、'关于人工智能的入门读物'、'轻松有趣的科普书'。")
    public String semanticSearchBooks( // 语义搜索图书工具
            @ToolParam(description = "用户的搜索意图描述，可以是自然语言，不需要精确关键词") String query, // 自然语言查询
            @ToolParam(description = "返回结果数量，默认5") int topK) { // 结果数量
        log.info("【RAG Tool】语义搜索: query={}, topK={}", query, topK); // 记录搜索日志

        try { // 异常捕获
            // 执行向量相似度搜索 — VectorStore 内部自动完成 Query → Embedding → HNSW 检索
            List<Document> results = vectorStore.similaritySearch( // 执行相似度搜索
                    SearchRequest.builder() // 构建搜索请求
                            .query(query) // 设置查询文本（会被自动 Embedding）
                            .topK(topK > 0 ? topK : 5) // 设置返回 Top-K 结果数，topK<=0 时默认 5
                            .build() // 构建 SearchRequest
            );

            if (results.isEmpty()) { // 无搜索结果
                return "未在知识库中找到与「" + query + "」语义相关的图书。建议尝试关键词搜索。"; // 返回空结果提示，引导用户换搜索方式
            }

            StringBuilder sb = new StringBuilder(); // 构建格式化输出
            sb.append("基于语义分析，与「").append(query).append("」最相关的图书：\n\n"); // 搜索结果标题

            for (int i = 0; i < results.size(); i++) { // 遍历搜索结果
                Document doc = results.get(i); // 获取当前文档
                String type = (String) doc.getMetadata().getOrDefault("type", "unknown"); // 提取文档类型（product/knowledge）
                // 计算相关度分数：将 distance（距离）转换为 similarity（相似度百分比）
                double score = doc.getMetadata().containsKey("distance") // 检查元数据是否包含 distance
                        ? 1.0 - ((Number) doc.getMetadata().get("distance")).doubleValue() // similarity = 1 - distance（distance 越小越相似）
                        : 0.0; // 无 distance 元数据时默认 0

                sb.append(String.format("%d. [相关度: %.0f%%]\n", i + 1, score * 100)); // 序号 + 相关度百分比
                sb.append(doc.getText()).append("\n\n"); // 文档内容 + 空行分隔
            }

            return sb.toString(); // 返回格式化搜索结果
        } catch (Exception e) { // 异常捕获
            log.error("语义搜索失败: {}", e.getMessage()); // 记录错误
            return "语义搜索时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }

    /**
     * 搜索图书领域知识 — 从知识库中检索专业信息
     *
     * LLM 调用场景：用户询问领域知识时，如：
     *   - "科幻小说有哪些经典作品"
     *   - "怎样选一本好的编程入门书"
     *   - "有哪些获得过茅盾文学奖的图书"
     *
     * 与 semanticSearchBooks 的区别：
     *   本工具固定 topK=3，且返回格式更简洁（不显示相关度百分比），
     *   适合作为 Prompt 中的参考知识注入。
     *
     * @param query 要查询的领域知识问题
     * @return 格式化的知识库检索结果
     */
    @Tool(description = "搜索图书领域知识库，获取关于图书分类、阅读建议、文学流派等方面的专业知识。当用户询问领域知识（如'科幻小说有哪些经典'、'怎样选一本好的编程书'）时使用此工具。")
    public String searchKnowledge( // 搜索领域知识工具
            @ToolParam(description = "要查询的领域知识问题") String query) { // 知识查询文本
        log.info("【RAG Tool】知识库搜索: query={}", query); // 记录日志

        try { // 异常捕获
            List<Document> results = vectorStore.similaritySearch( // 执行向量搜索
                    SearchRequest.builder() // 构建搜索请求
                            .query(query) // 设置查询
                            .topK(3) // 固定返回 3 条最相关知识
                            .build() // 构建
            );

            if (results.isEmpty()) { // 无结果
                return "知识库中暂无与「" + query + "」相关的信息。"; // 返回空结果
            }

            StringBuilder sb = new StringBuilder("以下是相关的领域知识：\n\n"); // 知识标题
            for (Document doc : results) { // 遍历结果
                String source = (String) doc.getMetadata().getOrDefault("source", "知识库"); // 来源文件名
                sb.append("[").append(source).append("]\n"); // 来源标注
                sb.append(doc.getText()).append("\n\n"); // 文档内容
            }

            return sb.toString(); // 返回知识检索结果
        } catch (Exception e) { // 异常捕获
            log.error("知识库搜索失败: {}", e.getMessage()); // 记录错误
            return "搜索知识库时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }

    /**
     * 获取知识库统计信息 — 让 LLM 了解当前知识库状态
     *
     * LLM 调用场景：当用户问"你的知识库有多大"、"你能回答哪些问题"时
     *
     * @return 知识库状态描述字符串
     */
    @Tool(description = "获取当前 RAG 知识库的统计信息，包括已索引的图书数量和文档数量。当需要了解知识库覆盖范围时使用。")
    public String getKnowledgeBaseStats() { // 知识库统计工具
        log.info("【RAG Tool】知识库统计"); // 记录日志
        // 返回简化统计（实际可通过注入 KnowledgeBaseService 获取详细数据）
        return "RAG 知识库已加载，包含图书描述和领域知识文档。支持语义搜索和知识检索。"; // 返回描述
    }
}
