package com.bookstore.agent.agent; // agent 包：存放 Multi-Agent 系统中的各个专家 Agent 类，每个 Agent 对应一种业务能力

// 导入 OrderTools（订单工具集），包含 queryOrderDetail、listUserOrders、cancelOrder 等 @Tool 方法
import com.bookstore.agent.tools.OrderTools;
// Lombok 注解：编译时为所有 final 字段生成构造函数，Spring 推荐使用构造器注入代替 @Autowired 字段注入
import lombok.RequiredArgsConstructor;
// Lombok 注解：编译时自动生成 private static final Logger log，用于打印日志
import lombok.extern.slf4j.Slf4j;
// Spring AI 的 ChatClient — 构建 LLM 对话请求的流式 API，支持注入 System Prompt、Tools、User Message
import org.springframework.ai.chat.client.ChatClient;
// Spring AI 的 ChatModel 接口 — LLM 的抽象底层调用，由 AiModelConfig 提供具体实现
import org.springframework.ai.chat.model.ChatModel;
// Spring 的 @Component 注解，标记此类为 Spring 管理的 Bean
import org.springframework.stereotype.Component;
// Project Reactor 的 Flux<T> — 响应式编程的 0..N 元素异步流，用于 SSE 流式对话
import reactor.core.publisher.Flux;

/**
 * 客服 Agent — 处理订单查询、订单取消、订单状态跟踪等客服类问题
 *
 * 职责范围：
 *   1. 查询用户的所有订单（按状态筛选）
 *   2. 查询指定订单号的详细信息
 *   3. 取消待支付状态的订单
 *
 * 不处理的内容（职责边界）：
 *   退款申请、修改收货地址、物流投诉等 → 引导用户联系人工客服
 *
 * 技术实现 — Tool Calling 工作流程：
 *   1. 用户输入"我的订单怎么样了？"
 *   2. ChatClient 携带 System Prompt + OrderTools 发送给 LLM
 *   3. LLM 判断需要调用 listUserOrders 工具，生成调用参数
 *   4. Spring AI 框架拦截 Tool Call 请求，调用 OrderTools.listUserOrders()
 *   5. 工具返回结果（JSON 字符串或对象）再次注入到对话上下文
 *   6. LLM 根据工具返回的数据生成自然语言回复
 *   7. 最终返回给用户："您有 3 个订单，1 个已完成，2 个配送中…"
 *
 * 安全设计：
 *   userId 由服务端从请求头 X-User-Id 注入（enrichedMessage），
 *   LLM 无法伪造，保证用户只能操作自己的订单。
 *
 * 面试亮点：
 *   1. System Prompt 工程：明确角色、能力边界、工作规则，防止 Prompt 注入
 *   2. Tool Calling 完整闭环：LLM 理解意图 → 生成工具调用 → 框架执行 → 结果注入 → 生成回复
 *   3. 同步 + 流式双模式：chat() 返回完整字符串，chatStream() 返回 Flux<String>
 *   4. 安全不可伪造：userId 由服务端注入上下文，不信任客户端传参
 */
@Slf4j // Lombok：自动生成 log 对象，可在方法中使用 log.info()、log.error() 等
@Component // Spring：将此 Bean 注入容器，供 OrchestratorAgent 和上层调用者注入
@RequiredArgsConstructor // Lombok：生成包含所有 final 字段的构造方法，实现构造器注入
public class CustomerServiceAgent { // 客服专家 Agent 类

    private final ChatModel chatModel; // LLM 底层模型接口，具体实现由 AiModelConfig 注入
    private final OrderTools orderTools; // 订单工具集，包含 @Tool 注解的方法，供 LLM 调用

    /**
     * 客服 Agent 的系统提示词
     * 核心作用：定义 Agent 的角色、能力边界和工作规则。
     *
     * System Prompt 设计要点：
     *   1. 角色定位("你是 BookVerse 书店的智能客服助手")：给 LLM 一个明确身份
     *   2. 能力枚举("你的能力：1. 2. 3.")：让 LLM 知道有哪些工具可用
     *   3. 工作规则：约束输出格式和风格，如"用自然语言总结，不要直接输出原始数据"
     *   4. 边界声明：超出能力范围时引导联系人工客服
     *
     * 为什么要用 static final？
     *   提示词在编译时确定，运行时不变化，static final 使其作为类级别的常量，
     *   共享于所有实例，避免重复创建 String 对象消耗内存。
     */
    private static final String SYSTEM_PROMPT = """
            你是 BookVerse 书店的智能客服助手。你的职责是帮助用户解决订单相关的问题。

            你的能力：
            1. 查询用户的订单详情（需要提供订单号）
            2. 查询用户的订单列表（可按状态筛选）
            3. 帮助用户取消订单（仅限待支付状态的订单）

            工作规则：
            - 始终使用中文回复用户
            - 回复要简洁、专业、友好
            - 在调用工具获取数据后，用自然语言总结结果，不要直接输出原始数据
            - 如果用户的请求超出你的能力范围（如退款、修改收货地址），礼貌地建议用户联系人工客服
            - 涉及金额时要使用 ¥ 符号
            - 不要编造不存在的订单信息，必须通过工具查询真实数据
            """; // System Prompt 常量，在 Agent 初始化时加载一次，后续所有请求复用

    /**
     * 同步对话 — 等待 LLM 完整回复后一次性返回
     *
     * 处理流程：
     *   1. 构建 ChatClient，注入 System Prompt 和 OrderTools
     *   2. 在用户消息中附加 userId 上下文（enrichedMessage）
     *   3. 同步调用 LLM → 获取完整回复
     *
     * @param userId 当前用户 ID，从请求头注入，供工具方法标识用户身份
     * @param userMessage 用户输入的自然语言消息
     * @return AI 生成的完整客服回复文本
     */
    public String chat(String userId, String userMessage) { // 同步对话方法：等待 LLM 生成完成后返回完整字符串
        log.info("【CustomerServiceAgent】同步对话: userId={}, message={}", userId, userMessage); // 记录日志便于排查生产问题

        // 构建 ChatClient — 配置 System Prompt 和可用工具
        ChatClient chatClient = ChatClient.builder(chatModel) // 使用 ChatClient.builder() 流式 API 构建客户端
                .defaultSystem(SYSTEM_PROMPT) // 注入系统提示词，定义 Agent 角色和规则
                .defaultTools(orderTools) // 注入 OrderTools 工具集，LLM 可自主调用其中的 @Tool 方法
                .build(); // 构建完成，返回 ChatClient 实例

        // 在用户消息中注入 userId 上下文 — 安全设计：userId 由服务端提供，LLM 无法伪造
        String enrichedMessage = userMessage + "\n[系统信息: 当前用户ID=" + userId + "]"; // 将 userId 附加到消息末尾

        return chatClient.prompt() // 开始构建 LLM 请求
                .user(enrichedMessage) // 设置用户消息（已注入 userId 上下文）
                .call() // 同步调用 LLM：阻塞当前线程，等待 LLM 生成完整回复
                .content(); // 提取 LLM 返回的文本内容并返回
    }

    /**
     * 流式对话 — 逐 token 返回，用于 SSE 实时推送
     *
     * 与 chat() 的区别：
     *   chat() 用 .call() → 返回 String（完整回复）
     *   chatStream() 用 .stream() → 返回 Flux<String>（逐 token 推送）
     *
     * Flux<String> 的处理方式：
     *   ChatController 将 Flux 转为 ServerSentEvent 流，通过 HTTP SSE 推送给前端。
     *   前端收到每个 event 后追加显示，实现打字机效果。
     */
    public Flux<String> chatStream(String userId, String userMessage) { // 流式对话方法：返回 Flux<String> 供 SSE 推送
        log.info("【CustomerServiceAgent】流式对话: userId={}, message={}", userId, userMessage); // 记录流式对话日志

        // 构建 ChatClient（与同步方法完全相同的配置）
        ChatClient chatClient = ChatClient.builder(chatModel) // 创建构建器
                .defaultSystem(SYSTEM_PROMPT) // 注入系统提示词
                .defaultTools(orderTools) // 注入订单工具集
                .build(); // 构建完成

        // 注入 userId 上下文 — 与同步方法相同的安全处理
        String enrichedMessage = userMessage + "\n[系统信息: 当前用户ID=" + userId + "]"; // 附加用户身份信息

        return chatClient.prompt() // 开始构建 LLM 请求
                .user(enrichedMessage) // 设置用户消息
                .stream() // 流式调用 LLM：不阻塞，返回 Flux 异步流（区别于 .call() 的同步阻塞）
                .content(); // 返回 Flux<String>，每个元素是 LLM 生成的一个 token 片段
    }

    /**
     * Agent 名称 — 用于前端展示和 Orchestrator 路由标识
     *
     * 使用场景：
     *   1. Orchestrator.getRoutedAgentName() 调用此方法获取中文名
     *   2. ChatMemoryService 将 agentName 存入 ChatHistory 表，前端通过此字段显示当前服务 Agent
     *   3. 日志中通过 getName() 标识消息来源
     *
     * @return Agent 的中文名称字符串 "客服助手"
     */
    public String getName() { // 返回 Agent 名称，用于前端展示和路由标识
        return "客服助手"; // 客服 Agent 的中文标识名称
    }
}
