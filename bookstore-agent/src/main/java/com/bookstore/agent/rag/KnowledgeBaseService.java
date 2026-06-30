package com.bookstore.agent.rag; // rag 包：存放 RAG（检索增强生成）相关代码，包括知识库管理和搜索工具

// 导入 ProductFeignClient — 商品微服务 Feign 客户端，用于获取商品数据构建知识库
import com.bookstore.agent.feign.ProductFeignClient;
// 导入统一响应 Result<T>
import com.bookstore.common.api.Result;
// 导入商品视图对象
import com.bookstore.common.api.vo.ProductVO;
// Lombok 构造器注入
import lombok.RequiredArgsConstructor;
// Lombok 日志
import lombok.extern.slf4j.Slf4j;
// Spring AI 的 Document 类 — 表示一个可被 Embedding 索引的文档片段
import org.springframework.ai.document.Document;
// Spring AI 的 EmbeddingModel — 文本向量化模型（在本类中未直接使用，由 VectorStore 内部调用）
import org.springframework.ai.embedding.EmbeddingModel;
// Spring AI 的 TextReader — 从 Resource 中读取文本内容
import org.springframework.ai.reader.TextReader;
// Spring AI 的 TokenTextSplitter — 将长文本按 Token 数量切分为小块
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
// Spring AI 的 VectorStore — 向量存储接口，支持添加和搜索操作
import org.springframework.ai.vectorstore.VectorStore;
// Spring @Value 注解 — 注入配置值
import org.springframework.beans.factory.annotation.Value;
// Spring Resource 接口 — 表示 classpath 或文件系统中的资源
import org.springframework.core.io.Resource;
// Spring @Scheduled 注解 — 定时任务执行
import org.springframework.scheduling.annotation.Scheduled;
// Spring @Service 注解
import org.springframework.stereotype.Service;

// Jakarta @PostConstruct — Bean 初始化后立即执行的回调方法
import jakarta.annotation.PostConstruct;
// Java 集合工具
import java.util.*;
// 重复导入（可保留）
import java.util.List;

/**
 * 知识库服务 — ETL Pipeline：Extract → Transform → Load
 *
 * 职责说明：
 *   1. 启动时加载静态知识文档（classpath:knowledge/*.md）+ 动态商品数据
 *   2. 对文档进行分块处理（TokenTextSplitter），保证每块适合 Embedding 模型
 *   3. 将文档块 Embedding 后写入 VectorStore（RedisVectorStore）
 *   4. 定时增量同步商品数据，保持索引与最新数据一致
 *
 * ETL Pipeline 详细流程：
 *   ┌──────────┐     ┌──────────────┐     ┌───────────────┐
 *   │ Extract  │ →   │  Transform   │ →   │     Load      │
 *   │ 读取文档 │     │ 文本分块     │     │ Embedding+存储│
 *   └──────────┘     └──────────────┘     └───────────────┘
 *   TextReader       TokenTextSplitter    VectorStore.add()
 *   FeignClient                            (Redis HNSW)
 *
 * TokenTextSplitter 参数说明：
 *   - chunkSize: 每个分块的最大 Token 数
 *   - minChunkSizeChars: 最小字符数，避免过小的碎片
 *   通过分块保证每个嵌入向量包含有效语义，并控制 Embedding API 调用次数。
 *
 * 两个知识源：
 *   1. 静态文档（type=knowledge）：classpath:knowledge/ 下的 .md 文件，
 *      包含图书分类指南、阅读推荐等专业领域知识。
 *   2. 动态商品数据（type=product）：从 bookstore-product 微服务实时拉取，
 *      包含书名、作者、分类、简介等商品信息。
 *
 * 面试亮点：
 *   1. ETL Pipeline 设计：Reader → Splitter → VectorStore，职责清晰
 *   2. 增量同步：indexedProductIds 记录已索引商品，避免重复 Embedding
 *   3. 定时任务：@Scheduled 自动保持知识库与商品数据同步
 *   4. 元数据注入：每条 Document 附带 source/type/productId，支持过滤查询
 */
@Slf4j // Lombok：自动生成 log
@Service // Spring：标记为 Service Bean
@RequiredArgsConstructor // Lombok：构造器注入
public class KnowledgeBaseService { // 知识库服务类

    private final VectorStore vectorStore; // 向量存储（RedisVectorStore），负责 Embedding 和语义搜索
    private final ProductFeignClient productFeignClient; // 商品微服务 Feign 客户端

    @Value("classpath:knowledge/*.md") // 注入 classpath:knowledge/ 下所有 .md 文件资源
    private Resource[] knowledgeResources; // 静态知识文档资源数组

    /**
     * 已索引的商品 ID 集合（用于增量同步去重）
     * 使用 Collections.synchronizedSet + HashSet 实现线程安全的 ID 集合。
     * 因为 @Scheduled 定时任务和 @PostConstruct 可能在并发环境下操作此集合。
     */
    private final Set<String> indexedProductIds = Collections.synchronizedSet(new HashSet<>()); // 线程安全的已索引 ID 集合

    /**
     * Bean 初始化后自动执行的回调方法
     * @PostConstruct 保证在依赖注入完成后执行，此时 vectorStore 等依赖已就绪。
     * 执行顺序：加载静态文档 → 同步商品数据
     */
    @PostConstruct // Jakarta 注解：Bean 初始化完成后执行
    public void initializeKnowledgeBase() { // 初始化知识库入口
        log.info("===== 初始化 RAG 知识库 ====="); // 启动标记
        loadStaticDocuments(); // Step 1: 加载静态知识文档
        syncProductData(); // Step 2: 同步动态商品数据
        log.info("===== RAG 知识库初始化完成 ====="); // 完成标记
    }

    /**
     * 加载静态知识文档（ETL: Extract → Transform → Load）
     *
     * 处理流程：
     *   1. 遍历 knowledgeResources 中的每个 .md 文件
     *   2. 使用 TextReader 读取文档内容
     *   3. 为每个 Document 添加元数据（source=文件名, type=knowledge）
     *   4. 使用 TokenTextSplitter 将长文档切分为小块
     *   5. 将分块后的所有 Document 写入 VectorStore
     *
     * 容错设计：
     *   整个方法包裹在 try-catch 中，加载失败仅记录警告，不影响服务启动。
     */
    private void loadStaticDocuments() { // 加载静态知识文档
        try { // 异常捕获，加载失败不影响启动
            TokenTextSplitter splitter = new TokenTextSplitter(); // 创建默认配置的文本分块器
            int docCount = 0; // 计数器，统计分块数量

            for (Resource resource : knowledgeResources) { // 遍历 classpath:knowledge/*.md
                if (!resource.exists()) continue; // 跳过不存在的资源

                TextReader reader = new TextReader(resource); // 使用 TextReader 读取文档内容
                List<Document> documents = reader.get(); // 读取文档，返回 Document 列表（单文档可能被 reader 拆为多个段落）

                // 为每个文档片段添加元数据，方便后续过滤和追溯
                for (Document doc : documents) { // 遍历文档片段
                    doc.getMetadata().put("source", resource.getFilename()); // 来源文件名（如 BookCategoryKnowledge.md）
                    doc.getMetadata().put("type", "knowledge"); // 类型标识为知识文档
                }

                // 分块处理：将文档切分为适合 Embedding 的大小
                List<Document> chunks = splitter.apply(documents); // 按 Token 数切分文档
                vectorStore.add(chunks); // 写入 VectorStore：自动 Embedding → 存储到 Redis HNSW 索引
                docCount += chunks.size(); // 累加分块计数
            }

            log.info("静态知识文档加载完成: {} 个分块", docCount); // 输出加载统计
        } catch (Exception e) { // 异常捕获
            log.warn("静态知识文档加载失败（不影响核心功能）: {}", e.getMessage()); // 仅记录警告
        }
    }

    /**
     * 同步商品数据到向量索引
     *
     * 处理流程：
     *   1. 从商品微服务获取推荐商品（20本）+ 热销商品（20本）
     *   2. 去重：用 LinkedHashMap 去重（保留首次出现顺序）
     *   3. 检查 indexedProductIds，跳过已索引的商品
     *   4. 为每个新商品构建 Document 文本 + 元数据
     *   5. 写入 VectorStore
     *   6. 记录商品 ID 到 indexedProductIds
     *
     * 增量同步逻辑：
     *   首次调用：indexedProductIds 为空，全部商品都会索引。
     *   后续调用：只索引 indexedProductIds 中不存在的商品。
     *   注意：商品信息更新（如价格变动）不会触发重新索引，设计上可接受。
     */
    public void syncProductData() { // 同步商品数据方法
        try { // 异常捕获
            log.info("开始同步商品数据到向量索引..."); // 日志

            // Step 1: 获取商品数据（推荐 + 热销，覆盖面最广的数据）
            List<ProductVO> products = new ArrayList<>(); // 聚合商品列表
            Result<List<ProductVO>> recommendResult = productFeignClient.getRecommendProducts(20); // 获取推荐商品
            if (recommendResult != null && recommendResult.getData() != null) { // 检查结果
                products.addAll(recommendResult.getData()); // 合并推荐数据
            }
            Result<List<ProductVO>> hotResult = productFeignClient.getHotProducts(20); // 获取热销商品
            if (hotResult != null && hotResult.getData() != null) { // 检查结果
                products.addAll(hotResult.getData()); // 合并热销数据
            }

            // Step 2: 去重（推荐和热销可能有重复商品）
            Map<String, ProductVO> uniqueProducts = new LinkedHashMap<>(); // LinkedHashMap 保持插入顺序
            for (ProductVO p : products) { // 遍历
                uniqueProducts.putIfAbsent(p.getId(), p); // 以 ID 为 key 去重，保留首次出现
            }

            // Step 3: 增量索引
            int added = 0; // 新增计数
            for (ProductVO product : uniqueProducts.values()) { // 遍历去重后的商品
                if (indexedProductIds.contains(product.getId())) continue; // 已索引，跳过

                // Step 4: 构建商品文档文本
                String content = buildProductDocument(product); // 将商品属性拼接为结构化文本
                // 创建 Document，附带元数据便于过滤和溯源
                Document doc = new Document(content, Map.of( // Java 9 Map.of 创建不可变元数据
                        "source", "product-service", // 数据来源
                        "type", "product", // 类型：商品
                        "productId", product.getId(), // 商品 ID（可关联查询详情）
                        "productName", product.getName(), // 商品名
                        "author", product.getAuthor() != null ? product.getAuthor() : "", // 作者
                        "category", product.getCategory() != null ? product.getCategory() : "" // 分类
                ));

                // Step 5: 写入 VectorStore
                vectorStore.add(List.of(doc)); // 单条写入：自动 Embedding → Redis HNSW 索引
                indexedProductIds.add(product.getId()); // 记录已索引 ID
                added++; // 计数 +1
            }

            log.info("商品数据同步完成: 新增 {} 条, 总计 {} 条", added, indexedProductIds.size()); // 输出同步统计
        } catch (Exception e) { // 异常捕获
            log.warn("商品数据同步失败（不影响核心功能）: {}", e.getMessage()); // 仅记录警告
        }
    }

    /**
     * 定时同步商品索引
     *
     * @Scheduled 参数说明：
     *   - fixedDelay = 600000：上次执行结束 10 分钟后再次执行（非 fixedRate）
     *   - initialDelay = 300000：应用启动 5 分钟后首次执行（给 @PostConstruct 留足时间）
     *
     * 为什么要定时同步？
     *   商品数据会变化（新增/下架/价格变动），定时同步保持知识库与最新商品数据一致。
     *   RAG 语义搜索依赖知识库中的商品数据，过期数据会导致幻读。
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 300000) // 每10分钟执行一次，启动后5分钟首次执行
    public void scheduledSync() { // 定时同步方法
        log.debug("定时同步商品数据到向量索引..."); // DEBUG 级别日志
        syncProductData(); // 调用同步方法
    }

    /**
     * 构建商品文档文本（用于 Embedding）
     *
     * 将商品的结构化属性预处理为自然语言文本，让 Embedding 向量更好地表达语义。
     * 格式：书名：《xxx》
作者：xxx
分类：xxx
价格：¥xxx
简介：xxx
     *
     * 为什么要去除 HTML 标签？
     *   商品描述可能包含富文本 HTML 标签（如 <p>、<br/>），
     *   HTML 标签对语义理解无帮助，反而会干扰 Embedding 质量。
     *
     * @param product 商品视图对象
     * @return 拼接后的自然语言文本
     */
    private String buildProductDocument(ProductVO product) { // 构建商品文档文本
        StringBuilder sb = new StringBuilder(); // 高效字符串拼接
        sb.append("书名：").append(product.getName()).append("\n"); // 书名（必有）
        if (product.getAuthor() != null) { // 作者可选字段
            sb.append("作者：").append(product.getAuthor()).append("\n"); // 作者
        }
        if (product.getCategory() != null) { // 分类可选字段
            sb.append("分类：").append(product.getCategory()).append("\n"); // 分类
        }
        sb.append("价格：¥").append(product.getPrice()).append("\n"); // 价格
        if (product.getDescription() != null) { // 描述可选字段
            // 去除 HTML 标签后取纯净文本
            String desc = product.getDescription().replaceAll("<[^>]+>", "").trim(); // 正则去除 HTML 标签
            sb.append("简介：").append(desc); // 简介
        }
        return sb.toString(); // 返回拼接后的文本
    }

    /**
     * 获取知识库统计信息
     * 用于运维监控或 LLM 了解知识库覆盖范围。
     *
     * @return 统计 Map：indexedProducts（已索引商品数）、knowledgeDocuments（知识文档数）、vectorStoreType（存储类型）
     */
    public Map<String, Object> getStats() { // 获取统计信息
        return Map.of( // Java 9 不可变 Map
                "indexedProducts", indexedProductIds.size(), // 已索引商品数
                "knowledgeDocuments", knowledgeResources != null ? knowledgeResources.length : 0, // 知识文档数
                "vectorStoreType", vectorStore.getClass().getSimpleName() // 向量存储类型（如 RedisVectorStore）
        );
    }
}
