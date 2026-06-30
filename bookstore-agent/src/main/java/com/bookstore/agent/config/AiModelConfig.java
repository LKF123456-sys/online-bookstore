package com.bookstore.agent.config; // config 包：存放 Spring Boot 配置类（@Configuration），管理 Bean 的创建和配置

// Lombok 的 @Data 注解 — 编译时自动生成所有字段的 getter/setter、toString、equals、hashCode
import lombok.Data;
// Spring AI 的 ChatModel 接口 — LLM 调用的统一抽象层，上层 Agent 只依赖此接口，不关心底层是哪个厂家的模型
import org.springframework.ai.chat.model.ChatModel;
// Spring AI Ollama 模块：OllamaChatModel — 本地部署的开源模型客户端（Qwen、Llama 等）
import org.springframework.ai.ollama.OllamaChatModel;
// Spring AI Ollama 模块：OllamaApi — Ollama REST API 的 HTTP 客户端封装
import org.springframework.ai.ollama.api.OllamaApi;
// Spring AI Ollama 模块：OllamaOptions — Ollama 特有的配置选项（模型名、temperature 等）
import org.springframework.ai.ollama.api.OllamaOptions;
// Spring AI OpenAI 模块：OpenAiChatModel — OpenAI 协议的模型客户端（GPT-4o、GPT-4o-mini）
import org.springframework.ai.openai.OpenAiChatModel;
// Spring AI OpenAI 模块：OpenAiChatOptions — OpenAI 协议特有的配置选项
import org.springframework.ai.openai.OpenAiChatOptions;
// Spring AI OpenAI 模块：OpenAiApi — OpenAI REST API 的 HTTP 客户端，也用于兼容 OpenAI 协议的第三方服务
import org.springframework.ai.openai.api.OpenAiApi;
// Spring Boot 配置绑定注解：将 application.yml 中 agent.model.* 前缀的属性自动映射到此类字段
import org.springframework.boot.context.properties.ConfigurationProperties;
// Spring 的 @Bean 注解 — 标记方法返回值作为 Bean 注册到容器
import org.springframework.context.annotation.Bean;
// Spring 的 @Configuration 注解 — 标记此类为配置类
import org.springframework.context.annotation.Configuration;

/**
 * AI 模型配置类 — 多模型提供商的策略切换中心
 *
 * 架构设计 — 策略模式（Strategy Pattern）：
 *   通过 @ConditionalOnProperty 根据 YAML 中的 agent.model.provider 值，
 *   决定创建哪个具体的 ChatModel Bean。上层 Agent 代码只依赖 ChatModel 接口，
 *   无需关心底层是 OpenAI、Ollama、DashScope 还是 DeepSeek。
 *   切换模型只需修改 application.yml 中一个配置值，零代码改动。
 *
 * 支持的模型提供商及接入方式：
 *   ┌──────────────┬──────────────────────────────────────────────────────┐
 *   │ Provider     │ 接入方式                                             │
 *   ├──────────────┼──────────────────────────────────────────────────────┤
 *   │ openai       │ 原生 Spring AI OpenAiChatModel（官方 Starter 支持） │
 *   │ ollama       │ 原生 Spring AI OllamaChatModel（官方 Starter 支持） │
 *   │ dashscope    │ 兼容 OpenAI 协议（DashScope 提供 compatible-mode）  │
 *   │ deepseek     │ 兼容 OpenAI 协议（DeepSeek API 兼容 OpenAI 格式）   │
 *   │ doubao       │ 兼容 OpenAI 协议（火山引擎豆包 API 兼容 OpenAI）    │
 *   └──────────────┴──────────────────────────────────────────────────────┘
 *
 * 配置属性绑定：
 *   @ConfigurationProperties(prefix = "agent.model") 将 application.yml 中的：
 *     agent.model.provider → this.provider
 *     agent.model.openai.api-key → this.openai.apiKey
 *     agent.model.openai.base-url → this.openai.baseUrl
 *     ... 以此类推，通过嵌套 POJO 映射 YAML 层级结构
 *
 * 面试亮点：
 *   1. 策略模式 + 条件注入：@ConditionalOnProperty 实现运行时模型切换
 *   2. @ConfigurationProperties 类型安全配置：比 @Value 更结构化，支持嵌套
 *   3. OpenAI 协议兼容：多个国产模型通过 OpenAI 兼容协议接入，无需各自开发适配器
 *   4. 所有密钥通过环境变量注入（apiKey=${AGENT_MODEL_OPENAI_API_KEY}），不入库
 */
@Data // Lombok：为所有字段生成 getter/setter，Spring 通过 setter 注入 YAML 属性值
@Configuration // Spring：标记此类为配置类，Spring Boot 启动时会扫描并处理其中的 @Bean 方法
@ConfigurationProperties(prefix = "agent.model") // 将 YAML 中 agent.model.* 自动绑定到此类字段
public class AiModelConfig { // AI 模型配置主类

    private String provider = "openai";  // 模型提供商选择，默认 openai，可选 openai/ollama/dashscope/deepseek/doubao
    private OpenAiProperties openai = new OpenAiProperties(); // OpenAI 配置属性对象，默认初始化避免 NPE
    private OllamaProperties ollama = new OllamaProperties(); // Ollama 本地模型配置
    private DashScopeProperties dashscope = new DashScopeProperties(); // 阿里云 DashScope 配置
    private DeepSeekProperties deepseek = new DeepSeekProperties(); // DeepSeek 配置
    private DoubaoProperties doubao = new DoubaoProperties(); // 火山引擎豆包配置

    /**
     * OpenAI 配置属性内部类
     * 映射 YAML 路径：agent.model.openai.*
     * 默认值采用占位符（sk-placeholder），生产环境通过环境变量覆盖
     */
    @Data // Lombok：生成 getter/setter
    public static class OpenAiProperties { // 静态内部类，作为 @ConfigurationProperties 嵌套绑定目标
        private String apiKey = "sk-placeholder"; // API 密钥，实际通过 ${AGENT_MODEL_OPENAI_API_KEY} 环境变量注入
        private String baseUrl = "https://api.openai.com"; // OpenAI API 端点 URL，也可改为代理地址
        private String model = "gpt-4o-mini"; // 使用的模型名称，gpt-4o-mini 性价比高
        private double temperature = 0.7; // 生成温度（0-2），越高越随机，0.7 是创造性任务的常用值
    }

    /**
     * Ollama 配置属性内部类
     * 映射 YAML 路径：agent.model.ollama.*
     * Ollama 适合开发环境：免费、本地运行、数据不外传
     */
    @Data // Lombok：生成 getter/setter
    public static class OllamaProperties { // Ollama 本地模型配置
        private String baseUrl = "http://localhost:11434"; // Ollama 默认端口 11434
        private String model = "qwen2.5:7b"; // 默认使用通义千问 2.5 7B 参数版本（需先 ollama pull qwen2.5:7b）
        private double temperature = 0.7; // 生成温度
    }

    /**
     * DashScope（阿里云通义千问）配置属性内部类
     * 映射 YAML 路径：agent.model.dashscope.*
     * 国内用户首选云端模型，延迟低，中文能力强
     */
    @Data // Lombok：生成 getter/setter
    public static class DashScopeProperties { // 阿里云通义千问配置
        private String apiKey = "sk-placeholder"; // DashScope API Key，从阿里云控制台获取
        private String model = "qwen-plus"; // 模型名：qwen-plus 性价比版，qwen-max 最强版
        private double temperature = 0.7; // 生成温度
    }

    /**
     * DeepSeek 配置属性内部类
     * 映射 YAML 路径：agent.model.deepseek.*
     * DeepSeek 性价比极高的国产模型，兼容 OpenAI 协议
     */
    @Data // Lombok：生成 getter/setter
    public static class DeepSeekProperties { // DeepSeek 配置
        private String apiKey = "sk-placeholder"; // DeepSeek API Key
        private String baseUrl = "https://api.deepseek.com"; // DeepSeek API 端点，兼容 OpenAI 协议
        private String model = "deepseek-chat"; // deepseek-chat 通用对话模型，deepseek-reasoner 推理模型
        private double temperature = 0.7; // 生成温度
    }

    /**
     * 豆包（火山引擎）配置属性内部类
     * 映射 YAML 路径：agent.model.doubao.*
     * 字节跳动旗下大模型服务，兼容 OpenAI 协议
     */
    @Data // Lombok：生成 getter/setter
    public static class DoubaoProperties { // 火山引擎豆包配置
        private String apiKey = "sk-placeholder"; // 豆包 API Key，从火山引擎控制台获取
        private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3"; // 火山引擎推理端点
        private String model = "doubao-pro-32k"; // doubao-pro-32k（32K 上下文）/ doubao-lite-32k（轻量版）
        private double temperature = 0.7; // 生成温度
    }

    /**
     * OpenAI ChatModel Bean 工厂方法
     * 生效条件：agent.model.provider = "openai"（matchIfMissing = true 表示缺省也生效）
     *
     * 构建过程：
     *   1. OpenAiApi：HTTP 客户端，封装 baseUrl + apiKey
     *   2. OpenAiChatOptions：模型参数，封装 model + temperature
     *   3. OpenAiChatModel：将 Api + Options 组装为 ChatModel 实现
     *
     * 兼容性说明：
     *   任何兼容 OpenAI Chat Completion API 的服务都可以通过修改 baseUrl 接入，
     *   例如：One API、FastGPT、LobeChat 的 API 网关等。
     */
    @Bean // Spring：将此方法的返回值注册为 Bean，Bean 类型为 ChatModel
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty( // 条件注解：仅当属性匹配时创建
            name = "agent.model.provider", havingValue = "openai", matchIfMissing = true) // provider=openai 或未设置时生效
    public ChatModel openAiChatModel() { // 工厂方法：创建 OpenAI ChatModel Bean
        // Step 1: 构建 HTTP API 客户端，配置端点 URL 和认证密钥
        OpenAiApi openAiApi = OpenAiApi.builder() // 使用 Builder 模式构建 OpenAiApi
                .baseUrl(openai.getBaseUrl()) // 设置 API 端点（可从 YAML 或环境变量覆盖）
                .apiKey(openai.getApiKey()) // 设置 API Key 用于认证
                .build(); // 构建 OpenAiApi 实例
        // Step 2: 构建模型选项，配置模型名和生成参数
        OpenAiChatOptions options = OpenAiChatOptions.builder() // Builder 模式构建选项
                .model(openai.getModel()) // 设置模型名（如 gpt-4o-mini）
                .temperature(openai.getTemperature()) // 设置 temperature 控制生成随机性
                .build(); // 构建 Options 实例
        // Step 3: 组装 Api + Options，创建 ChatModel 实现
        return OpenAiChatModel.builder() // Builder 模式组装最终的 ChatModel
                .openAiApi(openAiApi) // 注入 API 客户端
                .defaultOptions(options) // 注入默认选项（每次请求可用 .options() 覆盖）
                .build(); // 返回完整的 ChatModel Bean
    }

    /**
     * Ollama ChatModel Bean 工厂方法
     * 生效条件：agent.model.provider = "ollama"
     *
     * Ollama 特点：
     *   1. 本地运行，无需网络，零 API 费用
     *   2. 数据不出本机，适合敏感数据处理
     *   3. 模型选择丰富：Qwen、Llama、Mistral、Phi 等
     *   4. 注意：7B 模型 Tool Calling 能力有限，建议 14B 以上
     */
    @Bean // 注册为 Spring Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty( // 条件注入
            name = "agent.model.provider", havingValue = "ollama") // 仅当 provider=ollama 时生效
    public ChatModel ollamaChatModel() { // 工厂方法：创建 Ollama ChatModel
        // Step 1: 构建 Ollama API 客户端（无需 apiKey，本地免认证）
        OllamaApi ollamaApi = OllamaApi.builder() // Builder 模式
                .baseUrl(ollama.getBaseUrl()) // Ollama 服务地址，默认 http://localhost:11434
                .build(); // 构建完成
        // Step 2: 构建 Ollama 选项
        OllamaOptions options = OllamaOptions.builder() // Builder 模式
                .model(ollama.getModel()) // 设置 Ollama 模型名（如 qwen2.5:7b）
                .temperature(ollama.getTemperature()) // 设置生成温度
                .build(); // 构建完成
        // Step 3: 组装 ChatModel Bean
        return OllamaChatModel.builder() // Builder 模式
                .ollamaApi(ollamaApi) // 注入 API 客户端
                .defaultOptions(options) // 注入默认选项
                .build(); // 返回 ChatModel Bean
    }

    /**
     * DashScope（阿里云通义千问）ChatModel Bean 工厂方法
     * 生效条件：agent.model.provider = "dashscope"
     *
     * 技术说明：
     *   由于 Spring AI 没有官方的 DashScope Starter，这里使用 DashScope 的
     *   OpenAI 兼容模式（compatible-mode/v1 端点），复用 OpenAiChatModel 实现。
     *   这使得 DashScope 可以无缝对接 Spring AI 的 Tool Calling 等功能。
     *
     * DashScope 兼容端点：https://dashscope.aliyuncs.com/compatible-mode/v1
     */
    @Bean // 注册为 Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty( // 条件注入
            name = "agent.model.provider", havingValue = "dashscope") // 仅 provider=dashscope 时生效
    public ChatModel dashScopeChatModel() { // 工厂方法：创建 DashScope（通义千问）ChatModel
        // DashScope 兼容 OpenAI 协议，使用 OpenAiApi 指向 DashScope 端点
        String dashScopeBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"; // DashScope 的 OpenAI 兼容端点
        OpenAiApi openAiApi = OpenAiApi.builder() // 复用 OpenAI API 客户端
                .baseUrl(dashScopeBaseUrl) // 指向 DashScope 兼容端点
                .apiKey(dashscope.getApiKey()) // 使用 DashScope API Key
                .build(); // 构建完成
        OpenAiChatOptions options = OpenAiChatOptions.builder() // 复用 OpenAI 选项
                .model(dashscope.getModel()) // 设置通义千问模型名（qwen-plus / qwen-max 等）
                .temperature(dashscope.getTemperature()) // 设置温度
                .build(); // 构建完成
        return OpenAiChatModel.builder() // 复用 OpenAI ChatModel
                .openAiApi(openAiApi) // 注入指向 DashScope 的 API 客户端
                .defaultOptions(options) // 注入 DashScope 选项
                .build(); // 返回 ChatModel Bean
    }

    /**
     * DeepSeek ChatModel Bean 工厂方法
     * 生效条件：agent.model.provider = "deepseek"
     *
     * DeepSeek 特点：
     *   1. 性价比极高，API 价格约为 GPT-4 的 1/50
     *   2. 中文能力强，特别适合中文电商场景
     *   3. 原生兼容 OpenAI 协议，无需适配
     *   4. 支持 deepseek-chat（通用对话）和 deepseek-reasoner（深度推理）
     */
    @Bean // 注册为 Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty( // 条件注入
            name = "agent.model.provider", havingValue = "deepseek") // 仅当 provider=deepseek 时生效
    public ChatModel deepSeekChatModel() { // 工厂方法：创建 DeepSeek ChatModel
        // DeepSeek 原生兼容 OpenAI 协议，直接复用 OpenAiApi
        OpenAiApi openAiApi = OpenAiApi.builder() // Builder 模式
                .baseUrl(deepseek.getBaseUrl()) // DeepSeek API 端点
                .apiKey(deepseek.getApiKey()) // DeepSeek API Key
                .build(); // 构建完成
        OpenAiChatOptions options = OpenAiChatOptions.builder() // Builder 模式
                .model(deepseek.getModel()) // deepseek-chat 或 deepseek-reasoner
                .temperature(deepseek.getTemperature()) // 生成温度
                .build(); // 构建完成
        return OpenAiChatModel.builder() // Builder 模式
                .openAiApi(openAiApi) // 注入 API 客户端
                .defaultOptions(options) // 注入选项
                .build(); // 返回 ChatModel Bean
    }

    /**
     * 豆包（火山引擎）ChatModel Bean 工厂方法
     * 生效条件：agent.model.provider = "doubao"
     *
     * 豆包特点：
     *   1. 字节跳动旗下，中文理解能力强
     *   2. 兼容 OpenAI 协议
     *   3. 支持 doubao-pro-32k（32K 上下文）/ doubao-lite-32k（轻量版）
     */
    @Bean // 注册为 Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty( // 条件注入
            name = "agent.model.provider", havingValue = "doubao") // 仅当 provider=doubao 时生效
    public ChatModel doubaoChatModel() { // 工厂方法：创建豆包 ChatModel
        // 豆包兼容 OpenAI 协议，复用 OpenAiApi
        OpenAiApi openAiApi = OpenAiApi.builder() // Builder 模式
                .baseUrl(doubao.getBaseUrl()) // 火山引擎推理端点
                .apiKey(doubao.getApiKey()) // 豆包 API Key
                .build(); // 构建完成
        OpenAiChatOptions options = OpenAiChatOptions.builder() // Builder 模式
                .model(doubao.getModel()) // 豆包模型名
                .temperature(doubao.getTemperature()) // 生成温度
                .build(); // 构建完成
        return OpenAiChatModel.builder() // Builder 模式
                .openAiApi(openAiApi) // 注入 API 客户端
                .defaultOptions(options) // 注入选项
                .build(); // 返回 ChatModel Bean
    }
}
