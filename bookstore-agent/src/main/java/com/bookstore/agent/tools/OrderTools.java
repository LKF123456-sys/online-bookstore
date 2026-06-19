package com.bookstore.agent.tools;

import com.bookstore.agent.feign.OrderFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.OrderVO;
import com.bookstore.common.api.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 订单工具集 — 将订单服务 Feign 调用封装为 AI Agent 可调用的 Tool
 *
 * 设计说明：
 *   Spring AI 的 @Tool 注解会将方法元数据（名称、描述、参数）注册到 LLM，
 *   LLM 根据用户意图决定是否调用此工具，并自动生成参数。
 *   工具执行结果会被注入回 LLM 上下文，供其生成最终回复。
 *
 * 面试亮点：
 *   1. Tool Calling 模式：LLM → JSON 参数 → 工具执行 → 结果回注 → 生成回复
 *   2. 权限隔离：所有操作通过 userId 参数限制在用户自己的订单范围内
 *   3. 异常容错：工具调用失败时返回错误描述而非抛异常，LLM 可据此生成友好提示
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTools {

    private final OrderFeignClient orderFeignClient;

    /**
     * 查询用户的订单详情
     * LLM 会在用户询问"我的订单XXX怎么样了"时调用此工具
     */
    @Tool(description = "查询指定订单的详细信息，包括订单状态、商品列表、金额、收货地址等。当用户询问某个具体订单的情况时使用此工具。")
    public String queryOrderDetail(
            @ToolParam(description = "当前用户的ID") String userId,
            @ToolParam(description = "要查询的订单ID") String orderId) {
        log.info("【Agent Tool】查询订单详情: userId={}, orderId={}", userId, orderId);
        try {
            Result<OrderVO> result = orderFeignClient.getOrderById(userId, orderId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                OrderVO order = result.getData();
                return formatOrderDetail(order);
            }
            return "未找到订单，订单ID可能不正确或该订单不属于当前用户。";
        } catch (Exception e) {
            log.error("查询订单失败: {}", e.getMessage());
            return "查询订单时出现错误: " + e.getMessage();
        }
    }

    /**
     * 查询用户的订单列表
     * LLM 会在用户询问"我最近的订单"、"有没有待支付的订单"时调用此工具
     */
    @Tool(description = "查询用户的订单列表，支持按状态筛选和分页。当用户询问'我的订单'、'最近的订单'、'待支付的订单'等问题时使用此工具。")
    public String queryOrderList(
            @ToolParam(description = "当前用户的ID") String userId,
            @ToolParam(description = "订单状态筛选，可选值：PENDING_PAYMENT(待支付)、PAID(已支付)、SHIPPED(已发货)、COMPLETED(已完成)、CANCELLED(已取消)。不传则查询全部状态。") String status,
            @ToolParam(description = "页码，从1开始") int pageNum) {
        log.info("【Agent Tool】查询订单列表: userId={}, status={}, page={}", userId, status, pageNum);
        try {
            String statusParam = (status == null || status.isBlank()) ? null : status;
            Result<PageResult<OrderVO>> result = orderFeignClient.listOrders(userId, pageNum, 5, statusParam);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                PageResult<OrderVO> page = result.getData();
                if (page.getRecords() == null || page.getRecords().isEmpty()) {
                    return "当前没有符合条件的订单。";
                }
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("共 %d 条订单记录（第 %d 页）：\n", page.getTotal(), pageNum));
                for (OrderVO order : page.getRecords()) {
                    sb.append(String.format("- 订单号: %s | 状态: %s | 金额: ¥%s | 日期: %s\n",
                            order.getOrderid(), order.getStatus(),
                            order.getTotalprice(), order.getOrderdate()));
                }
                return sb.toString();
            }
            return "查询订单列表失败。";
        } catch (Exception e) {
            log.error("查询订单列表失败: {}", e.getMessage());
            return "查询订单列表时出现错误: " + e.getMessage();
        }
    }

    /**
     * 取消订单
     * LLM 会在用户明确说"帮我取消订单XXX"时调用此工具
     */
    @Tool(description = "取消指定订单并恢复库存。只有待支付状态的订单可以取消。当用户明确要求取消某个订单时使用此工具。")
    public String cancelOrder(
            @ToolParam(description = "当前用户的ID") String userId,
            @ToolParam(description = "要取消的订单ID") String orderId) {
        log.info("【Agent Tool】取消订单: userId={}, orderId={}", userId, orderId);
        try {
            Result<Void> result = orderFeignClient.cancelOrder(userId, orderId);
            if (result != null && result.getCode() == 200) {
                return "订单 " + orderId + " 已成功取消，库存已恢复。";
            }
            return "取消订单失败: " + (result != null ? result.getMessage() : "未知错误");
        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage());
            return "取消订单时出现错误: " + e.getMessage();
        }
    }

    /**
     * 格式化订单详情为可读文本
     */
    private String formatOrderDetail(OrderVO order) {
        StringBuilder sb = new StringBuilder();
        sb.append("订单详情:\n");
        sb.append("  订单号: ").append(order.getOrderid()).append("\n");
        sb.append("  状态: ").append(order.getStatus()).append("\n");
        sb.append("  下单时间: ").append(order.getOrderdate()).append("\n");
        sb.append("  总金额: ¥").append(order.getTotalprice()).append("\n");
        if (order.getOriginalprice() != null) {
            sb.append("  原价: ¥").append(order.getOriginalprice()).append("\n");
        }
        if (order.getDiscountamount() != null) {
            sb.append("  优惠金额: ¥").append(order.getDiscountamount()).append("\n");
        }
        sb.append("  收货人: ").append(order.getShiptofirstname()).append(" ").append(order.getShiptolastname()).append("\n");
        sb.append("  收货地址: ").append(order.getShipaddr1());
        if (order.getShipcity() != null) {
            sb.append(", ").append(order.getShipcity());
        }
        sb.append("\n");
        // 商品列表
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            sb.append("  商品列表:\n");
            for (var item : order.getItems()) {
                sb.append(String.format("    - %s x%d ¥%s\n",
                        item.getProductName(), item.getQuantity(), item.getPrice()));
            }
        }
        return sb.toString();
    }
}
