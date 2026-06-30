package com.bookstore.agent.agent; // agent 包：存放 Multi-Agent 系统中的各个专家 Agent 类

// Lombok 注解：编译时为所有 final 字段生成构造函数（构造器注入），Spring 官方推荐方式
import lombok.RequiredArgsConstructor;
// Lombok 注解：编译时自动生成 private static final Logger log，无需手写 getLogger
import lombok.extern.slf4j.Slf4j;
// Spring AI 的 ChatClient — 构建对话请求的流式 API 入口，支持 prompt().call() 和 prompt().stream()
import org.springframework.ai.chat.client.ChatClient;
// Spring AI 的 ChatModel 接口 — 底层 LLM 调用的统一抽象，具体实现由 AiModelConfig 注入
import org.springframework.ai.chat.model.ChatModel;
// Spring 注解：标记此类为组件，由 Spring 容器管理生命周期
import org.springframework.stereotype.Component;
// Project Reactor 的 Flux — 表示 0..N 个元素的异步流，用于 SSE 流式推送
import reactor.core.publisher.Flux;

/**
 * 编排 Agent（Orchestrator）— Multi-Agent 系统的总调度中心
 *
 * 设计模式：Orchestrator Pattern（编排器模式）
 *   1. 接收用户消息后，先用 LLM 做意图分类（classifyIntent）
 *   2. 根据分类结果路由到对应的专家 Agent 进行处理
 *   3. 每个专家 Agent 独立维护自己的 System Prompt、Tool Set 和业务逻辑
 *
 * 意图分类路由表：
 *   CUSTOMER_SERVICE  → CustomerServiceAgent   | 订单查询/取消/状态跟踪
 *   PRODUCT_RECOMMEND → ProductRecommendAgent  | 图书搜索/推荐/热销排行
 *   REVIEW_ANALYSIS   → ReviewAnalysisAgent    | 评价分析/情感分析/口碑总结
 *   GENERAL（默认）     → generalChat()          | 通用闲聊/书店介绍/功能引导
 *
 * 面试亮点：
 *   1. Multi-Agent 架构：关注点分离，每个 Agent 职责单一，可独立演进
 *   2. Prompt-based 意图分类：比规则引擎更灵活，能理解自然语言中的模糊表达
 *   3. 可扩展性：新增 Agent 只需添加分类标签 + 路由分支 + 注入依赖
 *   4. 双模式支持：chat() 同步返回完整结果，chatStream() 流式 SSE 逐 token 推送
 *   5. 容错降级：意图分类失败 → 默认走通用对话，不中断用户体验
 */
@Slf4j // Lombok：自动生成 log 变量，等价于 LoggerFactory.getLogger(OrchestratorAgent.class)
@Component // Spring：将此组件注入到容器中，供 ChatController 等使用者 @Autowired 注入
@RequiredArgsConstructor // Lombok：生成包含所有 final 字段的构造函数，实现构造器注入
public class OrchestratorAgent { // 编排器 Agent，Multi-Agent 系统的入口和大脑

    private final ChatModel chatModel; // 底层 LLM 模型接口，由 AiModelConfig 根据 YAML 配置选择 OpenAI/Ollama/DashScope 等
    private final CustomerServiceAgent customerServiceAgent; // 客服专家 Agent，处理订单相关问题
    private final ProductRecommendAgent productRecommendAgent; // 图书推荐专家 Agent，处理搜索/推荐
    private final ReviewAnalysisAgent reviewAnalysisAgent; // 评价分析专家 Agent，处理评价语义分析

    /**
     * 意图分类提示词（Prompt Template）
     * 使用 Java 15 Text Block（"""）语法编写的 LLM 提示词，通过 String.format 注入用户消息。
     * 设计上要求 LLM 只输出分类标签，不做额外解释，提高解析可靠性。
     * %s 占位符在执行时被用户消息替换。
     */
    private static final String INTENT_CLASSIFICATION_PROMPT = """
            你是一个意图分类器。根据用户的消息，判断用户想要进行什么操作。

            请只返回以下分类标签之一（不要返回任何其他内容）：
            - CUSTOMER_SERVICE：与订单查询、订单取消、订单状态、支付、物流相关
            - PRODUCT_RECOMMEND：与图书搜索、图书推荐、找书、畅销书、新书相关
            - REVIEW_ANALYSIS：与评价、评分、口碑、用户反馈、好不好相关
            - GENERAL：通用对话、问候、闲聊、或不属于以上类别的问题

            用户消息：%s

            分类结果："""; // Prompt 模板常量，static final 保证只初始化一次，节省内存

    /**
     * 通用对话的系统提示词
     * 当意图被分类为 GENERAL 或分类失败降级时，用此提示词构建 ChatClient。
     * 限定了 Agent 的性格（友好、专业）和能力边界（通用问题、日常对话、功能引导）。
     */
    private static final String GENERAL_SYSTEM_PROMPT = """
            你是 BookVerse 书店的 AI 助手。你友好、专业，能够回答关于书店的一般性问题。

            你可以：
            - 回答关于 BookVerse 书店的通用问题
            - 进行友好的日常对话
            - 引导用户使用更专业的功能（订单查询、图书推荐、评价分析）

            始终使用中文回复。如果用户的问题需要专业功能，引导他们使用对应的功能。
            """; // 通用对话 Prompt，引导 LLM 扮演书店助手角色

    /**
     * 同步对话入口
     * 处理流程：用户消息 → classifyIntent 意图分类 → switch 路由 → 专家 Agent.chat() → 返回完整回复
     * @param userId 当前用户 ID（从 HTTP 请求头 X-User-Id 提取）
     * @param userMessage 用户输入的原始消息文本
     * @return AI 生成的完整回复字符串
     */
    public String chat(String userId, String userMessage) { // 同步调用：等待 LLM 生成完整回复后一次性返回
        String intent = classifyIntent(userMessage); // Step 1: 调用 LLM 做意图分类，获取分类标签
        log.info("【Orchestrator】意图分类结果: intent={}, message={}", intent, userMessage); // 记录分类日志，方便排查路由问题

        return switch (intent) { // Step 2: Java 21 增强 switch 表达式，根据意图路由到不同专家 Agent
            case "CUSTOMER_SERVICE" -> customerServiceAgent.chat(userId, userMessage); // 路由到客服 Agent
            case "PRODUCT_RECOMMEND" -> productRecommendAgent.chat(userId, userMessage); // 路由到推荐 Agent
            case "REVIEW_ANALYSIS" -> reviewAnalysisAgent.chat(userId, userMessage); // 路由到评价分析 Agent
            default -> generalChat(userMessage); // 无法匹配则走通用对话（兜底逻辑）
        };
    }

    /**
     * 流式对话入口
     * 与 chat() 的流程相同，区别在于调用 chatStream()，返回 Flux<String> 实现 SSE 逐 token 推送。
     * 前端通过 EventSource API 接收流式数据，实现打字机效果。
     */
    public Flux<String> chatStream(String userId, String userMessage) { // 流式调用：返回 Flux<String> 供 Controller 转为 SSE 事件流
        String intent = classifyIntent(userMessage); // Step 1: 意图分类
        log.info("【Orchestrator】流式对话意图分类: intent={}, message={}", intent, userMessage); // 记录流式路由日志

        return switch (intent) { // Step 2: 根据意图路由到对应 Agent 的流式方法
            case "CUSTOMER_SERVICE" -> customerServiceAgent.chatStream(userId, userMessage); // 客服流式
            case "PRODUCT_RECOMMEND" -> productRecommendAgent.chatStream(userId, userMessage); // 推荐流式
            case "REVIEW_ANALYSIS" -> reviewAnalysisAgent.chatStream(userId, userMessage); // 评价分析流式
            default -> generalChatStream(userMessage); // 通用对话流式
        };
    }

    /**
     * 获取路由到的 Agent 中文名称
     * 用途：前端在上方展示「客服助手为您服务」等标识，让用户知道当前由哪个专家处理。
     * 该方法先做意图分类，然后根据分类结果返回对应 Agent 的 getName()。
     */
    public String getRoutedAgentName(String userMessage) { // 返回被路由到的 Agent 名称，用于前端 UI 展示
        String intent = classifyIntent(userMessage); // 先做意图分类
        return switch (intent) { // 根据意图返回对应的 Agent 中文名
            case "CUSTOMER_SERVICE" -> customerServiceAgent.getName(); // 返回 "客服助手"
            case "PRODUCT_RECOMMEND" -> productRecommendAgent.getName(); // 返回 "图书推荐顾问"
            case "REVIEW_ANALYSIS" -> reviewAnalysisAgent.getName(); // 返回 "评价分析师"
            default -> "通用助手"; // 兜底返回通用助手
        };
    }

    /**
     * 使用 LLM 进行意图分类（核心路由逻辑）
     *
     * 工作流程：
     *   1. 将用户消息填入 INTENT_CLASSIFICATION_PROMPT 模板
     *   2. 构建 ChatClient（不带工具，只做纯文本分类）
     *   3. 调用 LLM，获取分类结果字符串
     *   4. 容错处理：null 检查 → trim → toUpperCase → contains 模糊匹配
     *
     * 为什么用 contains 而非 equals？
     *   LLM 输出不稳定，可能附带换行、标点等额外内容，contains 匹配更鲁棒。
     *
     * 降级策略：
     *   LLM 返回 null / 无法匹配任何标签 / 调用异常 → 统一降级为 GENERAL
     */
    private String classifyIntent(String userMessage) { // 私有的意图分类方法，仅 Orchestrator 内部使用
        try { // try-catch 包裹整个分类过程，防止 LLM 异常导致服务崩溃
            String prompt = String.format(INTENT_CLASSIFICATION_PROMPT, userMessage); // Step 1: 将用户消息填入 Prompt 模板，生成完整 prompt
            ChatClient client = ChatClient.builder(chatModel).build(); // Step 2: 构建 ChatClient（无 System Prompt、无 Tools，纯分类）
            String result = client.prompt() // Step 3: 开始构建请求
                    .user(prompt) // 设置用户角色消息为填充后的分类 prompt
                    .call() // 同步调用 LLM
                    .content(); // 提取 LLM 返回的文本内容

            if (result == null) return "GENERAL"; // 容错1: LLM 返回 null → 降级为通用对话
            result = result.trim().toUpperCase(); // 去除首尾空白并转大写，统一格式便于匹配

            // 容错2: 使用 contains 做模糊匹配，因为 LLM 输出可能夹杂额外文字
            if (result.contains("CUSTOMER_SERVICE")) return "CUSTOMER_SERVICE"; // 匹配到客服标签
            if (result.contains("PRODUCT_RECOMMEND")) return "PRODUCT_RECOMMEND"; // 匹配到推荐标签
            if (result.contains("REVIEW_ANALYSIS")) return "REVIEW_ANALYSIS"; // 匹配到评价分析标签
            return "GENERAL"; // 容错3: 所有标签都不匹配 → 降级为通用对话
        } catch (Exception e) { // 捕获所有异常（网络超时、LLM API 不可用等）
            log.warn("意图分类失败，降级为通用对话: {}", e.getMessage()); // 记录警告日志，不中断服务
            return "GENERAL"; // 降级：意图分类失败时走通用对话，保证用户体验不中断
        }
    }

    /**
     * 通用对话（同步）
     * 不使用任何 Tool，纯文本对话，适合闲聊、功能询问等场景。
     * 通过 defaultSystem 注入 GENERAL_SYSTEM_PROMPT，设定助手角色。
     */
    private String generalChat(String userMessage) { // 私有的通用同步对话方法
        ChatClient client = ChatClient.builder(chatModel) // 构建 ChatClient
                .defaultSystem(GENERAL_SYSTEM_PROMPT) // 设置系统提示词，定义助手角色
                .build(); // 构建完成
        return client.prompt() // 开始构建请求
                .user(userMessage) // 设置用户消息
                .call() // 同步调用 LLM
                .content(); // 返回 LLM 生成的文本
    }

    /**
     * 通用对话（流式）
     * 与 generalChat 逻辑相同，区别在于使用 .stream().content() 返回 Flux<String>。
     * Flux 中的每个元素是 LLM 逐 token 生成的字符串片段。
     */
    private Flux<String> generalChatStream(String userMessage) { // 私有的通用流式对话方法
        ChatClient client = ChatClient.builder(chatModel) // 构建 ChatClient
                .defaultSystem(GENERAL_SYSTEM_PROMPT) // 设置系统提示词
                .build(); // 构建完成
        return client.prompt() // 开始构建请求
                .user(userMessage) // 设置用户消息
                .stream() // 流式调用 LLM（区别于 .call()）
                .content(); // 返回 Flux<String>，每个元素是一个 token 片段
    }
}
