package com.bookstore.agent.tools; // tools 包：存放 Spring AI @Tool 工具类，将业务 API 封装为 LLM 可调用的函数

// 导入 OrderFeignClient — 订单微服务的 Feign 远程调用客户端，通过 HTTP 调用 bookstore-order 服务
import com.bookstore.agent.feign.OrderFeignClient;
// 导入统一响应封装类 Result<T>，标准 API 返回格式 {code, message, data}
import com.bookstore.common.api.Result;
// 导入 OrderVO 视图对象，包含订单号、状态、金额、商品列表、收货地址等字段
import com.bookstore.common.api.vo.OrderVO;
// 导入分页结果封装类 PageResult<T>，包含 records、total、current 等分页信息
import com.bookstore.common.api.vo.PageResult;
// Lombok 构造器注入注解
import lombok.RequiredArgsConstructor;
// Lombok 日志注解
import lombok.extern.slf4j.Slf4j;
// Spring AI 的 @Tool 注解 — 将方法注册为 LLM 可调用的工具函数
import org.springframework.ai.tool.annotation.Tool;
// Spring AI 的 @ToolParam 注解 — 描述工具函数参数的含义，LLM 据此生成调用参数
import org.springframework.ai.tool.annotation.ToolParam;
// Spring 的 @Component 注解，标记为容器管理的 Bean
import org.springframework.stereotype.Component;

/**
 * 订单工具集 — 将订单微服务的 Feign 调用封装为 LLM 可调用的 Tool 函数
 *
 * 工作原理（Tool Calling 机制）：
 *   1. Spring AI 扫描所有带 @Tool 注解的方法，生成工具元数据（名称、描述、参数 Schema）
 *   2. 在 LLM 请求中附带工具元数据（作为 Function Calling 的 functions 参数）
 *   3. LLM 根据用户消息判断是否需要调用工具，如需调用则返回工具名 + JSON 参数
 *   4. Spring AI 框架拦截 LLM 的 Tool Call 响应，调用对应的方法，获取结果
 *   5. 工具结果作为新的 Message 注入对话上下文，LLM 基于结果生成自然语言回复
 *
 * 工具设计原则：
 *   1. 每个工具返回 String 格式：便于 LLM 理解和整合到自然语言回复
 *   2. 异常不抛出，返回错误描述字符串：让 LLM 生成友好的错误提示
 *   3. 描述信息明确使用场景：@Tool(description) 告诉 LLM 何时该调用此工具
 *
 * 面试亮点：
 *   1. Tool Calling 完整闭环：意图理解 → 工具选择 → 参数生成 → 执行 → 结果注入 → 回复
 *   2. 权限隔离：所有工具通过 userId 参数限制在用户自己的数据范围内
 *   3. 异常容错：工具异常不崩溃，返回错误描述让 LLM 生成友好提示
 */
@Slf4j // Lombok：自动生成 log 对象
@Component // Spring：标记为 Bean
@RequiredArgsConstructor // Lombok：构造器注入
public class OrderTools { // 订单工具集类

    private final OrderFeignClient orderFeignClient; // 订单微服务 Feign 客户端，用于远程调用 bookstore-order 服务

    /**
     * 查询订单详情工具
     * LLM 调用场景：用户说"帮我看看订单 ORD00123456 的情况"、"我的订单 ORD00123456 发货了吗"
     *
     * @param userId 当前用户 ID（由 LLM 从用户消息中的上下文提取，安全性由服务端注入保证）
     * @param orderId 要查询的订单编号
     * @return 格式化后的订单详情字符串，包含状态、金额、收货地址、商品列表
     */
    @Tool(description = "查询指定订单的详细信息，包括订单状态、商品列表、金额、收货地址等。当用户询问某个具体订单的情况时使用此工具。") // @Tool 注册到 LLM
    public String queryOrderDetail( // 查询订单详情工具方法
            @ToolParam(description = "当前用户的ID") String userId, // @ToolParam 描述参数，LLM 据此自动填充
            @ToolParam(description = "要查询的订单ID") String orderId) { // 订单 ID 参数
        log.info("【Agent Tool】查询订单详情: userId={}, orderId={}", userId, orderId); // 记录工具调用日志
        try { // 异常捕获：工具异常不抛出，返回错误描述
            Result<OrderVO> result = orderFeignClient.getOrderById(userId, orderId); // Feign 远程调用订单服务
            if (result != null && result.getCode() == 200 && result.getData() != null) { // 检查调用是否成功且有数据
                OrderVO order = result.getData(); // 提取订单数据
                return formatOrderDetail(order); // 格式化为可读文本后返回
            }
            return "未找到订单，订单ID可能不正确或该订单不属于当前用户。"; // 订单不存在或不属于该用户
        } catch (Exception e) { // 捕获所有异常
            log.error("查询订单失败: {}", e.getMessage()); // 记录错误日志
            return "查询订单时出现错误: " + e.getMessage(); // 返回错误描述，LLM 会将其转为用户友好提示
        }
    }

    /**
     * 查询订单列表工具
     * LLM 调用场景：用户说"我有哪些订单"、"最近的订单"、"有没有待支付的订单"
     *
     * @param userId 当前用户 ID
     * @param status 订单状态筛选（可选）：PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CANCELLED
     * @param pageNum 页码，从 1 开始
     * @return 格式化的订单列表字符串，包含订单号、状态、金额、日期
     */
    @Tool(description = "查询用户的订单列表，支持按状态筛选和分页。当用户询问'我的订单'、'最近的订单'、'待支付的订单'等问题时使用此工具。")
    public String queryOrderList( // 查询订单列表工具
            @ToolParam(description = "当前用户的ID") String userId, // 用户 ID
            @ToolParam(description = "订单状态筛选，可选值：PENDING_PAYMENT(待支付)、PAID(已支付)、SHIPPED(已发货)、COMPLETED(已完成)、CANCELLED(已取消)。不传则查询全部状态。") String status, // 状态筛选
            @ToolParam(description = "页码，从1开始") int pageNum) { // 分页页码
        log.info("【Agent Tool】查询订单列表: userId={}, status={}, page={}", userId, status, pageNum); // 记录调用日志
        try { // 异常捕获
            String statusParam = (status == null || status.isBlank()) ? null : status; // 空状态转为 null，查询全部
            Result<PageResult<OrderVO>> result = orderFeignClient.listOrders(userId, pageNum, 5, statusParam); // Feign 调用，每页 5 条
            if (result != null && result.getCode() == 200 && result.getData() != null) { // 检查结果
                PageResult<OrderVO> page = result.getData(); // 提取分页数据
                if (page.getRecords() == null || page.getRecords().isEmpty()) { // 无记录
                    return "当前没有符合条件的订单。"; // 返回空结果提示
                }
                StringBuilder sb = new StringBuilder(); // 构建格式化输出
                sb.append(String.format("共 %d 条订单记录（第 %d 页）：\n", page.getTotal(), pageNum)); // 表头：总数 + 页码
                for (OrderVO order : page.getRecords()) { // 遍历订单列表
                    sb.append(String.format("- 订单号: %s | 状态: %s | 金额: ¥%s | 日期: %s\n", // 格式化每条订单
                            order.getOrderid(), order.getStatus(), // 订单号和状态
                            order.getTotalprice(), order.getOrderdate())); // 金额和日期
                }
                return sb.toString(); // 返回格式化列表
            }
            return "查询订单列表失败。"; // 调用失败但无异常
        } catch (Exception e) { // 异常捕获
            log.error("查询订单列表失败: {}", e.getMessage()); // 记录错误
            return "查询订单列表时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }

    /**
     * 取消订单工具
     * LLM 调用场景：用户明确说"帮我取消订单 ORD00123456"
     * 限制：只能取消 PENDING_PAYMENT（待支付）状态的订单
     * 副作用：取消成功后自动恢复库存
     *
     * @param userId 当前用户 ID
     * @param orderId 要取消的订单 ID
     * @return 取消结果描述
     */
    @Tool(description = "取消指定订单并恢复库存。只有待支付状态的订单可以取消。当用户明确要求取消某个订单时使用此工具。")
    public String cancelOrder( // 取消订单工具
            @ToolParam(description = "当前用户的ID") String userId, // 用户 ID
            @ToolParam(description = "要取消的订单ID") String orderId) { // 订单 ID
        log.info("【Agent Tool】取消订单: userId={}, orderId={}", userId, orderId); // 记录调用日志
        try { // 异常捕获
            Result<Void> result = orderFeignClient.cancelOrder(userId, orderId); // Feign 调用取消订单接口
            if (result != null && result.getCode() == 200) { // 取消成功
                return "订单 " + orderId + " 已成功取消，库存已恢复。"; // 返回成功消息
            }
            return "取消订单失败: " + (result != null ? result.getMessage() : "未知错误"); // 返回失败原因
        } catch (Exception e) { // 异常捕获
            log.error("取消订单失败: {}", e.getMessage()); // 记录错误
            return "取消订单时出现错误: " + e.getMessage(); // 返回错误描述
        }
    }

    /**
     * 格式化订单详情为可读文本
     * 将 OrderVO 对象转换为结构化的多行文本，便于 LLM 理解和用户阅读。
     * 输出包含：订单号、状态、时间、金额（原价/优惠）、收货信息、商品列表。
     *
     * @param order 订单视图对象
     * @return 格式化后的多行文本
     */
    private String formatOrderDetail(OrderVO order) { // 私有格式化方法，仅内部使用
        StringBuilder sb = new StringBuilder(); // StringBuilder 高效拼接
        sb.append("订单详情:\n"); // 标题
        sb.append("  订单号: ").append(order.getOrderid()).append("\n"); // 订单编号
        sb.append("  状态: ").append(order.getStatus()).append("\n"); // 订单状态（中文）
        sb.append("  下单时间: ").append(order.getOrderdate()).append("\n"); // 下单日期
        sb.append("  总金额: ¥").append(order.getTotalprice()).append("\n"); // 实付金额
        if (order.getOriginalprice() != null) { // 有原价信息时才显示
            sb.append("  原价: ¥").append(order.getOriginalprice()).append("\n"); // 原价
        }
        if (order.getDiscountamount() != null) { // 有优惠信息时才显示
            sb.append("  优惠金额: ¥").append(order.getDiscountamount()).append("\n"); // 优惠金额
        }
        sb.append("  收货人: ").append(order.getShiptofirstname()).append(" ").append(order.getShiptolastname()).append("\n"); // 收货人姓名
        sb.append("  收货地址: ").append(order.getShipaddr1()); // 地址第一行
        if (order.getShipcity() != null) { // 城市信息
            sb.append(", ").append(order.getShipcity()); // 追加城市
        }
        sb.append("\n"); // 换行
        // 商品列表
        if (order.getItems() != null && !order.getItems().isEmpty()) { // 有商品明细
            sb.append("  商品列表:\n"); // 商品列表标题
            for (var item : order.getItems()) { // 遍历订单项（Java 10+ var 类型推断）
                sb.append(String.format("    - %s x%d ¥%s\n", // 格式化每条商品
                        item.getProductName(), item.getQuantity(), item.getPrice())); // 商品名、数量、单价
            }
        }
        return sb.toString(); // 返回格式化文本
    }
}
