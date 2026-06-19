package com.bookstore.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG (检索增强生成) 配置
 *
 * 设计说明：
 *   1. VectorStore — 存储文档的向量表示，支持语义相似度检索
 *   2. 使用 SimpleVectorStore（内存实现），面试演示足够，生产环境可替换为 Redis/PgVector/Milvus
 *   3. EmbeddingModel 由 spring-ai-openai-spring-boot-starter 自动配置（text-embedding-3-small）
 *
 * 面试亮点：
 *   1. RAG 三步流程：文档分块 → Embedding → 语义检索 → 上下文注入
 *   2. VectorStore 接口抽象：底层可无缝切换（Simple → Redis → PgVector）
 *   3. Embedding 与 ChatModel 分离：向量检索不需要 LLM，只消耗 Embedding API
 */
@Slf4j
@Configuration
public class RagConfig {

    /**
     * 向量存储 — 内存实现（SimpleVectorStore）
     *
     * SimpleVectorStore 将向量存储在内存中的 ConcurrentHashMap，
     * 启动时从本地文件加载（如果有），运行时动态添加/查询。
     *
     * 生产环境替换方案：
     *   - Redis + RediSearch: spring-ai-redis-store-spring-boot-starter
     *   - PostgreSQL + pgvector: spring-ai-pgvector-store-spring-boot-starter
     *   - Milvus: spring-ai-milvus-store-spring-boot-starter
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("初始化 SimpleVectorStore（内存向量存储）");
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
