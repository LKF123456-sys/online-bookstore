package com.bookstore.agent.agent; // agent 包：存放 Multi-Agent 系统中的各个专家 Agent 类，每个 Agent 独立管理自己的工具和 Prompt

// 导入 ProductTools（商品工具集），包含 searchBooks、getBookDetail、getRecommendBooks、getHotSales 等 @Tool 方法
import com.bookstore.agent.tools.ProductTools;
// 导入 RagSearchTool（RAG 语义搜索工具），基于向量相似度的语义检索，理解模糊意图如"轻松睡前读物"
import com.bookstore.agent.rag.RagSearchTool;
// 导入 ChatMemoryService（对话记忆服务），提供 Redis 短期记忆能力，注入历史上下文
import com.bookstore.agent.service.ChatMemoryService;
// Lombok 注解：编译时为所有 final 字段生成构造器注入函数
import lombok.RequiredArgsConstructor;
// Lombok 注解：编译时自动生成 private static final Logger log，无需手写
import lombok.extern.slf4j.Slf4j;
// Spring AI 的 ChatClient — 构建 LLM 对话请求的流式 API
import org.springframework.ai.chat.client.ChatClient;
// Spring AI 的 ChatModel 接口 — LLM 抽象层，由 AiModelConfig 提供具体实现
import org.springframework.ai.chat.model.ChatModel;
// Spring 的 @Component 注解，标记为容器管理的 Bean
import org.springframework.stereotype.Component;
// Project Reactor 的 Flux — 0..N 元素异步流，支持 backpressure，用于 SSE 流式推送
import reactor.core.publisher.Flux;

/**
 * 商品推荐 Agent — 处理图书搜索、推荐、热销排行等商品类问题
 *
 * 双通道检索策略：
 *   通道1 — ProductTools（关键词精确匹配）：
 *     适用场景：用户明确知道书名/作者，如"搜索《三体》"、"刘慈欣的书"
 *     技术实现：Feign → bookstore-product 微服务 → MySQL 全文索引/模糊查询
 *
 *   通道2 — RagSearchTool（语义近似搜索）：
 *     适用场景：用户描述模糊需求，如"有没有适合睡前轻松阅读的书"
 *     技术实现：用户问题 → Embedding 向量化 → Redis VectorStore HNSW 检索 → 返回语义相似文档
 *
 * 历史上下文注入（ChatMemory）：
 *   通过 ChatMemoryService 从 Redis 获取最近 N 条对话记录，
 *   构建 enrichedMessage 时拼接到用户消息前，让 LLM 理解对话上下文。
 *   例如：用户先问"推荐小说"→ 又问"有没有类似的"，LLM 通过上下文理解"类似"指的是小说。
 *
 * 知识库覆盖范围：
 *   知识库包含 BookCategoryKnowledge.md 中的图书分类知识、阅读建议等，
 *   使推荐不仅有商品数据，还有专业领域知识支撑，推荐更有深度。
 *
 * 面试亮点：
 *   1. 关键词匹配 + 语义搜索双通道互补，覆盖精确搜索与模糊意图
 *   2. RAG 完整流程：Query → Embedding → Vector Store → Top-K → Context Injection → LLM Generation
 *   3. ChatMemory 上下文注入，实现多轮对话连贯性
 *   4. LLM 自主决策选哪个工具（Spring AI Tool Calling 自动路由）
 */
@Slf4j // Lombok：自动生成 log 对象
@Component // Spring：标记为 Bean，由容器管理
@RequiredArgsConstructor // Lombok：为所有 final 字段生成构造函数，实现构造器注入
public class ProductRecommendAgent { // 图书推荐专家 Agent

    private final ChatModel chatModel; // LLM 模型接口，由 AiModelConfig 注入具体实现
    private final ProductTools productTools; // 商品工具集，提供关键词搜索和商品数据获取
    private final RagSearchTool ragSearchTool; // RAG 语义搜索工具，提供向量相似度检索
    private final ChatMemoryService chatMemoryService; // 对话记忆服务，提供 Redis 短期记忆

    /**
     * 图书推荐 Agent 的系统提示词
     *
     * 核心设计思想：
     *   1. 角色人设("图书推荐顾问"、"资深书店店员")：塑造热情专业的形象
     *   2. 能力清单：让 LLM 知道可以调用哪些工具做什么事
     *   3. 工具选择策略：明确何时用关键词搜索、何时用语义搜索
     *   4. 输出质量要求：推荐时说明理由、结合领域知识、性价比建议
     */
    private static final String SYSTEM_PROMPT = """
            你是 BookVerse 书店的图书推荐顾问。你的职责是帮助用户发现和了解图书。

            你的能力：
            1. 根据关键词搜索图书（精确匹配书名、作者）
            2. 基于语义理解搜索图书（理解用户意图，如"轻松睡前读物"）
            3. 搜索图书领域知识库（分类指南、阅读建议等专业知识）
            4. 查看图书详细信息
            5. 获取系统推荐图书和热销排行

            工作规则：
            - 始终使用中文回复用户
            - 回复要热情、专业，像一位资深书店店员
            - 当用户描述模糊需求时（如"有没有好看的小说"），优先使用语义搜索工具
            - 当用户提到具体书名或作者时，使用关键词搜索工具
            - 推荐时说明推荐理由，结合知识库中的领域知识给出有深度的建议
            - 涉及价格时要有性价比方面的建议
            """; // 推荐 Agent 的 System Prompt，引导 LLM 成为专业的图书顾问

    /**
     * 同步对话 — 注入双通道工具 + 历史上下文后同步调用 LLM
     *
     * @param userId 当前用户 ID
     * @param userMessage 用户输入的自然语言消息
     * @return AI 生成的完整推荐回复
     */
    public String chat(String userId, String userMessage) { // 同步推荐对话方法
        log.info("【ProductRecommendAgent】同步对话: userId={}, message={}", userId, userMessage); // 记录日志

        // 构建 ChatClient — 注入两个工具集（ProductTools + RagSearchTool），LLM 自主选择调用哪个
        ChatClient chatClient = ChatClient.builder(chatModel) // 创建构建器
                .defaultSystem(SYSTEM_PROMPT) // 注入系统提示词，定义推荐顾问角色
                .defaultTools(productTools, ragSearchTool) // 注入工具集 — 两个工具同时提供，LLM 自主决策
                .build(); // 构建完成

        // 构建增强消息：注入历史对话上下文（Redis 短期记忆）
        String enrichedMessage = buildEnrichedMessage(userId, userMessage); // 调用 buildEnrichedMessage 拼接历史上下文

        return chatClient.prompt() // 开始构建 LLM 请求
                .user(enrichedMessage) // 设置已增强的用户消息
                .call() // 同步调用 LLM，等待完整回复
                .content(); // 返回 LLM 生成的文本
    }

    /**
     * 流式对话 — 逐 token 返回推荐内容，用于 SSE 实时推送
     */
    public Flux<String> chatStream(String userId, String userMessage) { // 流式推荐对话方法
        log.info("【ProductRecommendAgent】流式对话: userId={}, message={}", userId, userMessage); // 记录流式日志

        // 构建 ChatClient — 配置与同步方法完全相同（System Prompt + 工具）
        ChatClient chatClient = ChatClient.builder(chatModel) // 创建构建器
                .defaultSystem(SYSTEM_PROMPT) // 注入系统提示词
                .defaultTools(productTools, ragSearchTool) // 注入双通道工具
                .build(); // 构建完成

        // 构建增强消息 — 注入历史上下文
        String enrichedMessage = buildEnrichedMessage(userId, userMessage); // 拼接历史上下文

        return chatClient.prompt() // 开始构建 LLM 请求
                .user(enrichedMessage) // 设置增强后的用户消息
                .stream() // 流式调用 LLM，返回 Flux<String>（区别于 .call() 的阻塞调用）
                .content(); // 返回 Flux<String> 逐 token 流
    }

    /**
     * 构建增强消息：注入历史对话上下文
     *
     * 工作流程：
     *   1. 调用 chatMemoryService.getRecentContext(null) 从 Redis 获取最近 N 条消息
     *   2. 如果存在历史上下文，添加 [历史对话上下文] 标记后拼接
     *   3. 拼接当前用户消息，添加 [用户当前消息] 标记
     *   4. 返回完整的增强消息供 LLM 理解上下文
     *
     * 示例输出：
     *   [历史对话上下文]
     *   用户: 推荐几本科幻小说
     *   助手: 我为您推荐《三体》《银河帝国》...
     *   [用户当前消息]
     *   有没有类似《三体》的？
     *
     * 注意：getRecentContext 传入 null 作为 sessionId 参数，
     *   ChatMemoryService 内部使用 ThreadLocal 获取当前请求的 sessionId。
     */
    private String buildEnrichedMessage(String userId, String userMessage) { // 构建增强消息的私有方法
        StringBuilder sb = new StringBuilder(); // 使用 StringBuilder 高效拼接字符串

        // 注入历史对话（Redis 短期记忆）— 让 LLM 理解对话上下文
        String recentContext = chatMemoryService.getRecentContext(null); // 从 Redis 获取最近 N 条消息（null 表示使用 ThreadLocal 中的 sessionId）
        if (recentContext != null && !recentContext.isBlank()) { // 检查历史上下文是否有效
            sb.append("[历史对话上下文]\n").append(recentContext).append("\n"); // 用标记包裹历史上下文，让 LLM 区分历史和当前消息
        }

        sb.append("[用户当前消息]\n").append(userMessage); // 拼接当前用户消息，用标记标注
        return sb.toString(); // 返回增强后的完整消息字符串
    }

    /**
     * Agent 名称
     * @return "图书推荐顾问" — 用于前端展示当前 AI 服务身份
     */
    public String getName() { // 返回 Agent 中文名称
        return "图书推荐顾问"; // 推荐 Agent 的中文名称
    }
}
