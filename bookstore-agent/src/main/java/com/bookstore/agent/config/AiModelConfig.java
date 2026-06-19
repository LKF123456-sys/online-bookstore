package com.bookstore.agent.config;

import lombok.Data;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模型配置 — 根据 agent.model.provider 属性切换底层 LLM
 *
 * 设计说明：
 *   通过 @ConfigurationProperties 读取 YAML 中 agent.model.* 配置，
 *   使用 @ConditionalOnProperty 按 provider 值创建对应的 ChatModel Bean。
 *   三种模型提供商（OpenAI / Ollama / DashScope）只会有一个生效。
 *
 * 面试亮点：
 *   1. @ConditionalOnProperty 实现策略切换，零代码改动切换模型
 *   2. 所有 API Key 通过环境变量注入，不硬编码
 *   3. DashScope 无官方 Spring AI Starter，通过自定义 ChatModel 适配
 *   4. 统一的 ChatModel 接口，上层 Agent 代码与底层模型解耦
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agent.model")
public class AiModelConfig {

    private String provider = "openai";  // 默认使用 OpenAI
    private OpenAiProperties openai = new OpenAiProperties();
    private OllamaProperties ollama = new OllamaProperties();
    private DashScopeProperties dashscope = new DashScopeProperties();
    private DeepSeekProperties deepseek = new DeepSeekProperties();
    private DoubaoProperties doubao = new DoubaoProperties();

    @Data
    public static class OpenAiProperties {
        private String apiKey = "sk-placeholder";
        private String baseUrl = "https://api.openai.com";
        private String model = "gpt-4o-mini";
        private double temperature = 0.7;
    }

    @Data
    public static class OllamaProperties {
        private String baseUrl = "http://localhost:11434";
        private String model = "qwen2.5:7b";
        private double temperature = 0.7;
    }

    @Data
    public static class DashScopeProperties {
        private String apiKey = "sk-placeholder";
        private String model = "qwen-plus";
        private double temperature = 0.7;
    }

    @Data
    public static class DeepSeekProperties {
        private String apiKey = "sk-placeholder";
        private String baseUrl = "https://api.deepseek.com";
        private String model = "deepseek-chat";
        private double temperature = 0.7;
    }

    @Data
    public static class DoubaoProperties {
        private String apiKey = "sk-placeholder";
        private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";
        private String model = "doubao-pro-32k";
        private double temperature = 0.7;
    }

    /**
     * OpenAI ChatModel — 当 provider=openai 时创建
     * 支持 GPT-4o、GPT-4o-mini 等 OpenAI 模型
     * 也可兼容其他 OpenAI 协议的模型（如 DeepSeek、Moonshot），只需修改 base-url
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            name = "agent.model.provider", havingValue = "openai", matchIfMissing = true)
    public ChatModel openAiChatModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(openai.getBaseUrl())
                .apiKey(openai.getApiKey())
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(openai.getModel())
                .temperature(openai.getTemperature())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * Ollama ChatModel — 当 provider=ollama 时创建
     * 支持本地部署的开源模型（Qwen、Llama、Mistral 等）
     * 适用于开发环境和对数据隐私有要求的场景
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            name = "agent.model.provider", havingValue = "ollama")
    public ChatModel ollamaChatModel() {
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(ollama.getBaseUrl())
                .build();
        OllamaOptions options = OllamaOptions.builder()
                .model(ollama.getModel())
                .temperature(ollama.getTemperature())
                .build();
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * DashScope ChatModel — 当 provider=dashscope 时创建
     * 阿里云通义千问系列模型，通过 HTTP API 调用
     * 由于 Spring AI 无官方 DashScope Starter，此处使用 OpenAI 兼容协议
     * （DashScope 提供了 OpenAI 兼容的 API 端点）
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            name = "agent.model.provider", havingValue = "dashscope")
    public ChatModel dashScopeChatModel() {
        // DashScope 兼容 OpenAI 协议，使用 OpenAiApi 指向 DashScope 端点
        String dashScopeBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(dashScopeBaseUrl)
                .apiKey(dashscope.getApiKey())
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(dashscope.getModel())
                .temperature(dashscope.getTemperature())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * DeepSeek ChatModel — 当 provider=deepseek 时创建
     * DeepSeek 通用大模型，兼容 OpenAI 协议
     * 支持 deepseek-chat（通用对话）和 deepseek-coder（代码生成）
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            name = "agent.model.provider", havingValue = "deepseek")
    public ChatModel deepSeekChatModel() {
        // DeepSeek 兼容 OpenAI 协议
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(deepseek.getBaseUrl())
                .apiKey(deepseek.getApiKey())
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(deepseek.getModel())
                .temperature(deepseek.getTemperature())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * 豆包 ChatModel — 当 provider=doubao 时创建
     * 火山引擎豆包大模型，兼容 OpenAI 协议
     * 支持 doubao-pro-32k / doubao-lite-32k 等模型
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            name = "agent.model.provider", havingValue = "doubao")
    public ChatModel doubaoChatModel() {
        // 豆包兼容 OpenAI 协议
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(doubao.getBaseUrl())
                .apiKey(doubao.getApiKey())
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(doubao.getModel())
                .temperature(doubao.getTemperature())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }
}
