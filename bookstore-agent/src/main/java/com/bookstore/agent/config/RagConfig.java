package com.bookstore.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * RAG（检索增强生成）配置
 *
 * 设计说明：
 *   1. VectorStore — 存储文档的向量表示，支持语义相似度搜索
 *   2. 使用 RedisVectorStore（基于 RediSearch），数据持久化，重启不丢失
 *   3. 需要 Redis Stack Server（内置 RediSearch 模块），参考 docker-compose.yml
 *   4. EmbeddingModel 由 spring-ai-openai-spring-boot-starter 自动配置（text-embedding-3-small）
 *
 * 面试点：
 *   1. RAG 三步流程：文档分块 → Embedding → 语义检索 → 上下文注入
 *   2. VectorStore 接口抽象：底层可无缝切换（Simple → Redis → PgVector）
 *   3. Embedding 与 ChatModel 分离：向量检索不需要 LLM，只消耗 Embedding API
 *   4. JedisPooled 直连 Redis，与项目已有的 Lettuce 连接池并存，互不干扰
 */
@Slf4j
@Configuration
public class RagConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * 向量存储 — Redis 实现（基于 RediSearch）
     *
     * RedisVectorStore 将向量和索引存储在 Redis 中，数据持久化，重启不丢失。
     * 使用 JedisPooled 直连 Redis（Spring AI RedisVectorStore 要求 Jedis 客户端），
     * 与项目已有的 Lettuce 连接池并存，两者互不干扰。
     * 启动时自动创建 RediSearch 索引（initializeSchema=true），支持向量相似度搜索。
     *
     * 向量算法：HSNW（Hierarchical Navigable Small World）
     *   近似最近邻搜索，速度快，适合大规模向量检索。
     *   对比 FLAT：全量暴力搜索，数据量大时延迟高。
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("初始化 RedisVectorStore（基于 RediSearch，redis://{}:{}）", redisHost, redisPort);
        JedisPooled jedisPooled = new JedisPooled(redisHost, redisPort);
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("bookstore-vector-index")
                .prefix("bookstore:vector:")
                .vectorAlgorithm(RedisVectorStore.Algorithm.HSNW)
                .initializeSchema(true)
                .build();
    }
}
