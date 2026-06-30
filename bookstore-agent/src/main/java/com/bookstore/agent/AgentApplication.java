package com.bookstore.agent; // 根包：Spring Boot 应用的主入口所在包

// MyBatis-Plus 的 @MapperScan 注解 — 扫描指定包下的 Mapper 接口，自动生成代理实现
import org.mybatis.spring.annotation.MapperScan;
// Spring Boot 的核心启动类 SpringApplication — 通过 run() 方法启动应用
import org.springframework.boot.SpringApplication;
// Spring Boot 的 @SpringBootApplication 注解 — 复合注解，包含 @Configuration + @EnableAutoConfiguration + @ComponentScan
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Spring Cloud 的 @EnableDiscoveryClient — 启用服务注册与发现（Nacos）
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// Spring Cloud OpenFeign 的 @EnableFeignClients — 启用 Feign 声明式 HTTP 客户端
import org.springframework.cloud.openfeign.EnableFeignClients;
// Spring 的 @ComponentScan 注解 — 指定要扫描的组件包路径，覆盖默认扫描范围
import org.springframework.context.annotation.ComponentScan;
// Spring 的 @EnableScheduling 注解 — 启用定时任务支持（@Scheduled）
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BookVerse AI 智能体服务 — Spring Boot 应用启动类
 *
 * 功能概览：
 *   ┌──────────────────────────┬───────────────────────────────────────────┐
 *   │ 功能模块                 │ 说明                                      │
 *   ├──────────────────────────┼───────────────────────────────────────────┤
 *   │ Multi-Agent 协作         │ Orchestrator → Customer/Product/Review    │
 *   │ RAG 检索增强             │ VectorStore + Embedding 语义搜索          │
 *   │ MCP 协议服务             │ JSON-RPC 2.0 over SSE 工具暴露            │
 *   │ Tool Calling             │ Agent 通过 Feign 调用微服务作为工具        │
 *   │ SSE 流式对话             │ 逐 token 实时推送（打字机效果）            │
 *   │ 多模型切换               │ OpenAI / Ollama / DashScope / DeepSeek     │
 *   │ 会话记忆                 │ Redis 热数据 + MySQL 冷数据                │
 *   │ 服务注册                 │ Nacos 注册中心（Spring Cloud）             │
 *   └──────────────────────────┴───────────────────────────────────────────┘
 *
 * 注解说明：
 *   - @SpringBootApplication：Spring Boot 核心注解，启用自动配置
 *   - @EnableDiscoveryClient：将服务注册到 Nacos，支持服务发现
 *   - @EnableFeignClients：启用 Feign 声明式 HTTP 客户端，扫描带 @FeignClient 的接口
 *   - @EnableScheduling：启用 @Scheduled 定时任务（如 KnowledgeBaseService 的知识库同步）
 *   - @MapperScan：扫描 MyBatis-Plus Mapper 接口包，生成代理实现
 *   - @ComponentScan：显式指定扫描包，额外包含 com.bookstore.common（公共模块）
 */
@SpringBootApplication // Spring Boot 核心注解：= @Configuration + @EnableAutoConfiguration + @ComponentScan
@EnableDiscoveryClient // 启用 Nacos 服务注册与发现
@EnableFeignClients // 启用 Feign 声明式 HTTP 客户端，扫描所有 @FeignClient 接口
@EnableScheduling // 启用 Spring 定时任务（@Scheduled）
@MapperScan("com.bookstore.agent.mapper") // 扫描 MyBatis-Plus Mapper 接口包
@ComponentScan(basePackages = {"com.bookstore.agent", "com.bookstore.common"}) // 显式指定组件扫描路径，包含 common 公共模块
public class AgentApplication { // AI 智能体服务启动类

    /**
     * 应用主方法 — Spring Boot 应用入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) { // JVM 入口点
        SpringApplication.run(AgentApplication.class, args); // 启动 Spring Boot 应用：初始化 IoC 容器 → 扫描组件 → 启动内嵌 Tomcat
    }
}
