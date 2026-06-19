package com.bookstore.agent.rag;

import com.bookstore.agent.feign.ProductFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.List;

/**
 * 知识库服务 — 管理 RAG 文档的导入、分块、Embedding 和索引
 *
 * 设计说明：
 *   1. 启动时加载 classpath:knowledge/ 下的文档 + 商品数据
 *   2. 使用 TokenTextSplitter 将长文档切分为适合 Embedding 的片段
 *   3. 定时同步商品数据到向量索引（增量更新）
 *   4. 两个知识源：静态文档（图书领域知识）+ 动态数据（商品描述）
 *
 * 面试亮点：
 *   1. ETL Pipeline：Reader → Transformer(Splitter) → Writer(VectorStore)
 *   2. TokenTextSplitter 参数调优：chunkSize=800, minChunkSizeChars=350
 *   3. 增量同步：只更新变化的商品，避免全量重建索引
 *   4. 元数据注入：每个 Document 附带 metadata（source, type, productId）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final VectorStore vectorStore;
    private final ProductFeignClient productFeignClient;

    @Value("classpath:knowledge/*.md")
    private Resource[] knowledgeResources;

    /** 已索引的商品ID集合（增量同步用） */
    private final Set<String> indexedProductIds = Collections.synchronizedSet(new HashSet<>());

    /**
     * 启动时初始化知识库
     */
    @PostConstruct
    public void initializeKnowledgeBase() {
        log.info("===== 初始化 RAG 知识库 =====");
        loadStaticDocuments();
        syncProductData();
        log.info("===== RAG 知识库初始化完成 =====");
    }

    /**
     * 加载静态知识文档（classpath:knowledge/*.md）
     * 包含图书分类指南、阅读推荐等领域知识
     */
    private void loadStaticDocuments() {
        try {
            TokenTextSplitter splitter = new TokenTextSplitter();
            int docCount = 0;

            for (Resource resource : knowledgeResources) {
                if (!resource.exists()) continue;

                TextReader reader = new TextReader(resource);
                List<Document> documents = reader.get();

                // 为每个文档添加元数据
                for (Document doc : documents) {
                    doc.getMetadata().put("source", resource.getFilename());
                    doc.getMetadata().put("type", "knowledge");
                }

                // 分块 + 写入向量存储
                List<Document> chunks = splitter.apply(documents);
                vectorStore.add(chunks);
                docCount += chunks.size();
            }

            log.info("静态知识文档加载完成: {} 个分块", docCount);
        } catch (Exception e) {
            log.warn("静态知识文档加载失败（不影响核心功能）: {}", e.getMessage());
        }
    }

    /**
     * 同步商品数据到向量索引
     * 将商品的名称、作者、描述、分类拼接后 Embedding 入库
     */
    public void syncProductData() {
        try {
            log.info("开始同步商品数据到向量索引...");

            // 获取推荐和热销商品（覆盖面最广的数据）
            List<ProductVO> products = new ArrayList<>();
            Result<List<ProductVO>> recommendResult = productFeignClient.getRecommendProducts(20);
            if (recommendResult != null && recommendResult.getData() != null) {
                products.addAll(recommendResult.getData());
            }
            Result<List<ProductVO>> hotResult = productFeignClient.getHotProducts(20);
            if (hotResult != null && hotResult.getData() != null) {
                products.addAll(hotResult.getData());
            }

            // 去重
            Map<String, ProductVO> uniqueProducts = new LinkedHashMap<>();
            for (ProductVO p : products) {
                uniqueProducts.putIfAbsent(p.getId(), p);
            }

            int added = 0;
            for (ProductVO product : uniqueProducts.values()) {
                if (indexedProductIds.contains(product.getId())) continue;

                String content = buildProductDocument(product);
                Document doc = new Document(content, Map.of(
                        "source", "product-service",
                        "type", "product",
                        "productId", product.getId(),
                        "productName", product.getName(),
                        "author", product.getAuthor() != null ? product.getAuthor() : "",
                        "category", product.getCategory() != null ? product.getCategory() : ""
                ));

                vectorStore.add(List.of(doc));
                indexedProductIds.add(product.getId());
                added++;
            }

            log.info("商品数据同步完成: 新增 {} 条, 总计 {} 条", added, indexedProductIds.size());
        } catch (Exception e) {
            log.warn("商品数据同步失败（不影响核心功能）: {}", e.getMessage());
        }
    }

    /**
     * 定时同步：每 10 分钟增量更新商品索引
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 300000)
    public void scheduledSync() {
        log.debug("定时同步商品数据到向量索引...");
        syncProductData();
    }

    /**
     * 拼接商品文本（用于 Embedding）
     * 格式：标题 + 作者 + 分类 + 描述
     */
    private String buildProductDocument(ProductVO product) {
        StringBuilder sb = new StringBuilder();
        sb.append("书名：").append(product.getName()).append("\n");
        if (product.getAuthor() != null) {
            sb.append("作者：").append(product.getAuthor()).append("\n");
        }
        if (product.getCategory() != null) {
            sb.append("分类：").append(product.getCategory()).append("\n");
        }
        sb.append("价格：¥").append(product.getPrice()).append("\n");
        if (product.getDescription() != null) {
            // 去除 HTML 标签
            String desc = product.getDescription().replaceAll("<[^>]+>", "").trim();
            sb.append("简介：").append(desc);
        }
        return sb.toString();
    }

    /**
     * 获取知识库统计信息
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "indexedProducts", indexedProductIds.size(),
                "knowledgeDocuments", knowledgeResources != null ? knowledgeResources.length : 0,
                "vectorStoreType", vectorStore.getClass().getSimpleName()
        );
    }
}
