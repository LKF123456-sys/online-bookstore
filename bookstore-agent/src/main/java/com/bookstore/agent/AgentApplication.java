package com.bookstore.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI 智能体服务启动类
 *
 * 功能概述：
 *   - 多 Agent 协作架构：Orchestrator → CustomerService / ProductRecommend / ReviewAnalysis
 *   - Tool Calling：Agent 通过 Feign 调用现有微服务 API 作为工具
 *   - SSE 流式对话：实时返回 LLM 生成的 token
 *   - 多模型切换：OpenAI / Ollama / DashScope 通过配置切换
 *   - 会话记忆：Redis 短期窗口 + MySQL 长期归档
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.bookstore.agent.mapper")
@ComponentScan(basePackages = {"com.bookstore.agent", "com.bookstore.common"})
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
