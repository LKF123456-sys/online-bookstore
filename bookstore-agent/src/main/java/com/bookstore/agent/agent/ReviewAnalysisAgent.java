package com.bookstore.agent.agent; // agent 包：存放 Multi-Agent 系统中的各个专家 Agent

// 导入 ReviewTools（评价工具集），包含 getBookReviews 等 @Tool 方法，用于获取某本图书的用户评价数据
import com.bookstore.agent.tools.ReviewTools;
// Lombok 注解：编译时为所有 final 字段生成构造器注入方法
import lombok.RequiredArgsConstructor;
// Lombok 注解：编译时自动生成 private static final Logger log
import lombok.extern.slf4j.Slf4j;
// Spring AI 的 ChatClient — 构建 LLM 对话请求的流式 API
import org.springframework.ai.chat.client.ChatClient;
// Spring AI 的 ChatModel 接口 — LLM 的抽象底层调用
import org.springframework.ai.chat.model.ChatModel;
// Spring 的 @Component 注解，标记此类为容器管理的 Bean
import org.springframework.stereotype.Component;
// Project Reactor 的 Flux — 0..N 元素异步流，用于 SSE 流式推送
import reactor.core.publisher.Flux;

/**
 * 评价分析 Agent — 处理商品评价的情感分析、摘要生成、优缺点提取
 *
 * 职责范围：
 *   1. 获取指定图书的用户评价数据（通过 ReviewTools 调用 bookstore-review 微服务）
 *   2. 对评价进行情感分类：正面 / 中性 / 负面占比统计
 *   3. 提取评价中的高频关键词（内容质量、印刷质量、性价比、物流速度等维度）
 *   4. 总结优缺点，生成结构化的评价摘要报告
 *
 * 技术实现 — LLM 二次分析模式：
 *   第一步（Tool Call）：LLM 调用 ReviewTools 获取原始评价数据（JSON 数组）
 *   第二步（LLM 分析）：LLM 对原始评价数据进行自然语言分析：
 *     - 情感分析（正面/中性/负面）
 *     - 关键词提取（内容好/印刷差/物流快）
 *     - 结构化总结（优点列表 / 缺点列表 / 总体评价）
 *
 * 典型对话示例：
 *   用户："《三体》这本书的评价怎么样？"
 *   Agent → 调用 getBookReviews("三体") → LLM 分析 →
 *   回复："《三体》共有 128 条评价，好评率 92%。读者普遍赞扬其宏大的科幻世界观（优点1）..."
 *
 * 面试亮点：
 *   1. Tool Calling + LLM 二次分析：工具获取数据，LLM 做语义加工，各司其职
 *   2. 结构化输出要求：通过 Prompt Engineering 约束 LLM 输出格式（优点/缺点/总体）
 *   3. 样本量意识：System Prompt 中提醒 LLM 评价少时需说明样本有限
 */
@Slf4j // Lombok：自动生成 log 对象，方便在方法中使用 log.info()、log.error() 等
@Component // Spring：标记为 Bean，由容器管理生命周期
@RequiredArgsConstructor // Lombok：为所有 final 字段生成构造函数，实现依赖注入
public class ReviewAnalysisAgent { // 评价分析专家 Agent

    private final ChatModel chatModel; // LLM 模型接口，由 AiModelConfig 注入
    private final ReviewTools reviewTools; // 评价工具集，提供获取评价数据的 @Tool 方法

    /**
     * 评价分析 Agent 的系统提示词
     *
     * Prompt 设计要点：
     *   1. "评价分析师"角色定位：让 LLM 以分析师的视角处理数据
     *   2. 分析维度明确：情感分析、优缺点提取、摘要生成
     *   3. 输出格式约束：要求结构化呈现（优点列表、缺点列表、总体评价）
     *   4. 质量边界：强调客观性（"基于评价文本，不要添加臆测"）、样本量提醒
     */
    private static final String SYSTEM_PROMPT = """
            你是 BookVerse 书店的评价分析师。你的职责是帮助用户了解图书的真实口碑。

            你的能力：
            1. 获取指定图书的用户评价
            2. 对评价进行情感分析（正面/中性/负面占比）
            3. 提取评价中提到的优点和缺点
            4. 生成评价摘要，帮助用户快速了解口碑

            工作规则：
            - 始终使用中文回复用户
            - 分析要客观、基于评价文本，不要添加臆测
            - 用结构化的方式呈现分析结果（如：优点列表、缺点列表、总体评价）
            - 如果评价数量较少，提醒用户参考样本有限
            - 关注评价中提到的具体方面：内容质量、印刷质量、性价比、物流速度等
            """; // System Prompt 常量，定义评价分析师的角色和行为准则

    /**
     * 同步对话 — 让 LLM 获取评价数据后进行分析，返回完整分析报告
     *
     * 注意：此 Agent 不需要注入 userId，因为评价数据是公开的（任何人可查看任何书的评价）。
     * 不同于 CustomerServiceAgent，评价查询不涉及用户隐私数据隔离。
     *
     * @param userId 当前用户 ID（当前未使用，保留为接口一致性）
     * @param userMessage 用户输入的自然语言消息
     * @return AI 生成的评价分析报告
     */
    public String chat(String userId, String userMessage) { // 同步评价分析方法
        log.info("【ReviewAnalysisAgent】同步对话: userId={}, message={}", userId, userMessage); // 记录日志

        // 构建 ChatClient — 注入 System Prompt 和 ReviewTools
        ChatClient chatClient = ChatClient.builder(chatModel) // 创建构建器
                .defaultSystem(SYSTEM_PROMPT) // 注入系统提示词，定义评价分析师角色
                .defaultTools(reviewTools) // 注入评价工具集，LLM 可调用 getBookReviews 获取原始评价
                .build(); // 构建完成

        return chatClient.prompt() // 开始构建 LLM 请求
                .user(userMessage) // 直接传入用户消息（不注入 userId，因为评价数据是公开的）
                .call() // 同步调用 LLM
                .content(); // 返回分析结果文本
    }

    /**
     * 流式对话 — 逐 token 返回分析报告，用于 SSE 实时推送
     */
    public Flux<String> chatStream(String userId, String userMessage) { // 流式评价分析方法
        log.info("【ReviewAnalysisAgent】流式对话: userId={}, message={}", userId, userMessage); // 记录流式日志

        // 构建 ChatClient — 与同步方法相同配置
        ChatClient chatClient = ChatClient.builder(chatModel) // 创建构建器
                .defaultSystem(SYSTEM_PROMPT) // 注入系统提示词
                .defaultTools(reviewTools) // 注入评价工具集
                .build(); // 构建完成

        return chatClient.prompt() // 开始构建 LLM 请求
                .user(userMessage) // 设置用户消息
                .stream() // 流式调用，返回 Flux<String>
                .content(); // 返回逐 token 的分析流
    }

    /**
     * Agent 名称
     * @return "评价分析师" — 用于前端展示和 Orchestrator 路由
     */
    public String getName() { // 返回 Agent 中文名
        return "评价分析师"; // 评价分析 Agent 的中文名称
    }
}
