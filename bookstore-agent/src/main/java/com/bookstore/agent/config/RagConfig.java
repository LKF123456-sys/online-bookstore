package com.bookstore.agent.config; // config 包：存放 Spring Boot 配置类，通过 @Bean 方法创建和配置 Bean

// Lombok 的 @Slf4j 注解，编译时自动生成 private static final Logger log
import lombok.extern.slf4j.Slf4j;
// Spring AI 的 EmbeddingModel 接口 — 将文本转换为向量（如 text-embedding-3-small）
import org.springframework.ai.embedding.EmbeddingModel;
// Spring AI 的 VectorStore 接口 — 向量向量存储的抽象层，支持语义相似度搜索（接口）
import org.springframework.ai.vectorstore.VectorStore;
// Spring AI Redis 向量存储实现 — 基于 RediSearch 模块的 VectorStore
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
// Spring 的 @Value 注解 — 从 application.yml 或环境变量中注入属性值
import org.springframework.beans.factory.annotation.Value;
// Spring 的 @Bean 注解 — 标记方法返回值为 Spring 容器管理的 Bean
import org.springframework.context.annotation.Bean;
// Spring 的 @Configuration 注解 — 标记此类为配置类
import org.springframework.context.annotation.Configuration;
// Jedis 连接池客户端（JedisPooled）— Spring AI RedisVectorStore 要求使用 Jedis 客户端
import redis.clients.jedis.JedisPooled;

/**
 * RAG（检索增强生成）配置类
 *
 * RAG 是什么？
 *   RAG = Retrieval-Augmented Generation（检索增强生成）
 *   核心思想：在 LLM 生成回答前，先从知识库中检索最相关的文档片段，
 *   作为附加上下文注入到 Prompt 中，让 LLM 基于真实知识库生成答案。
 *
 * RAG 完整流程：
 *   ┌──────────────────────────────────────────────────────────────┐
 *   │  离线阶段（知识库构建）：                                      │
 *   │    文档（Markdown/PDF/TXT）→ 文本分块（Chunking）→ Embedding │
 *   │    → 存入 VectorStore（Redis HNSW 索引）                     │
 *   │                                                              │
 *   │  在线阶段（用户查询）：                                       │
 *   │    用户问题 → Embedding → VectorStore 相似度检索 → Top-K 文档 │
 *   │    → 拼接 Prompt 上下文 → LLM 生成含知识库信息的回答          │
 *   └──────────────────────────────────────────────────────────────┘
 *
 * 技术选型：
 *   - VectorStore：RedisVectorStore（基于 RediSearch 模块）
 *   - 向量算法：HNSW（Hierarchical Navigable Small World），近似最近邻搜索
 *   - EmbeddingModel：由 spring-ai-openai 自动配置（text-embedding-3-small）
 *   - 数据持久化：向量存储在 Redis 中，Redis RDB/AOF 保证重启不丢失
 *
 * 注意：RedisVectorStore 要求 Redis Stack Server（内置 RediSearch），
 *   标准 Redis 不包含 RediSearch 模块，需使用 docker-compose.yml 中的 redis 服务。
 *
 * 面试亮点：
 *   1. RAG 三步流程：文档分块 → Embedding → 语义检索 → 上下文注入
 *   2. VectorStore 接口抽象：底层可无缝切换（Simple → Redis → PgVector → Milvus）
 *   3. Embedding 与 ChatModel 分离：向量检索不消耗 LLM Token，只消耗 Embedding API
 *   4. HNSW 算法：图索引结构，O(log N) 搜索复杂度，比 FLAT 暴力搜索快数百倍
 */
@Slf4j // Lombok：自动生成 log 对象
@Configuration // Spring：标记为配置类，启动时处理其中的 @Bean 方法
public class RagConfig {
    private static final RedisVectorStore.Algorithm HSNW = RedisVectorStore.Algorithm.HSNW; // HSNW 向量搜索算法常量 (Spring AI 1.0.9 拼写是 HSNW)

    @Value("${spring.data.redis.host:localhost}") // 从配置中注入 Redis 主机地址，默认 localhost
    private String redisHost; // Redis 服务器 IP 或域名

    @Value("${spring.data.redis.port:6379}") // 从配置中注入 Redis 端口，默认 6379
    private int redisPort; // Redis 端口号

    /**
     * VectorStore Bean 工厂方法
     * 创建基于 Redis 的向量存储，使用 RediSearch 模块进行向量相似度搜索。
     *
     * 配置说明：
     *   - indexName：RediSearch 索引名称，用于标识向量搜索索引
     *   - prefix：Redis Key 前缀，所有向量文档 Key 以 "bookstore:vector:" 开头
     *   - vectorAlgorithm：HNSW 图索引算法，搜索效率 O(log N)
     *   - initializeSchema：启动时自动创建 RediSearch 索引 Schema
     *
     * 为什么用 JedisPooled 而不是 Lettuce？
     *   Spring AI 的 RedisVectorStore 源码要求 Jedis 客户端（使用的是 Jedis 的
     *   RediSearch 命令封装），与项目 Spring Data Redis 使用的 Lettuce 不冲突，
     *   两者通过不同的连接池管理，访问同一个 Redis 实例。
     *
     * HNSW vs FLAT 对比：
     *   ┌────────┬─────────────┬──────────────┐
     *   │ 算法   │ 搜索复杂度  │ 适用场景     │
     *   ├────────┼─────────────┼──────────────┤
     *   │ HNSW   │ O(log N)    │ 生产环境推荐 │
     *   │ FLAT   │ O(N)        │ 小数据量测试 │
     *   └────────┴─────────────┴──────────────┘
     *
     * @param embeddingModel Spring AI 自动配置的 EmbeddingModel Bean（由框架注入）
     * @return RedisVectorStore 实例，实现 VectorStore 接口
     */
    @Bean // Spring：将此方法返回值注册为 VectorStore 类型的 Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) { // 工厂方法，参数 embeddingModel 由 Spring 自动注入
        log.info("初始化 RedisVectorStore（基于 RediSearch，redis://{}:{}）", redisHost, redisPort); // 启动日志
        // Step 1: 创建 Jedis 连接池 — 直连 Redis（与 Lettuce 并存，互不干扰）
        JedisPooled jedisPooled = new JedisPooled(redisHost, redisPort); // 创建 Jedis 连接池，连接 Redis
        // Step 2: 构建 RedisVectorStore — 配置索引名、Key 前缀、向量算法
        return RedisVectorStore.builder(jedisPooled, embeddingModel) // Builder 模式：传入 Jedis 连接池 + Embedding 模型
                .indexName("bookstore-vector-index") // 设置 RediSearch 索引名称
                .prefix("bookstore:vector:") // 设置 Redis Key 前缀，便于管理和清理
                .vectorAlgorithm(HSNW) // 选择 HSNW 向量搜索算法（近似最近邻）
                .initializeSchema(true) // 启动时自动创建 RediSearch 索引 Schema（首次启动或索引不存在时）
                .build(); // 构建并返回 VectorStore Bean
    }
}
