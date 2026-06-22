package com.bookstore.agent.feign;  // 声明当前类所在的包路径：Feign降级工厂层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入订单视图对象
import com.bookstore.common.api.vo.OrderVO;
// 导入分页结果封装
import com.bookstore.common.api.vo.PageResult;
// 导入Lombok的Slf4j日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring Cloud的FallbackFactory接口，用于创建Feign降级处理对象
import org.springframework.cloud.openfeign.FallbackFactory;
// 导入Spring的Component注解
import org.springframework.stereotype.Component;

/**
 * 订单服务Feign降级工厂
 * 当订单服务不可用（网络异常、服务宕机、超时等）时，会执行这里的降级逻辑。
 *
 * 降级的作用：
 * - 防止因为一个服务的故障导致整个系统雪崩（服务雪崩效应）
 * - 给用户一个友好的错误提示，而不是直接报错
 * - 记录错误日志，方便排查问题
 *
 * 本类的降级策略：
 * - 只读方法（查询订单详情、查询订单列表）返回友好的错误提示Result
 * - 写操作方法（取消订单、支付订单）抛出运行时异常，避免静默失败导致数据不一致
 */
@Slf4j  // Lombok注解：自动生成名为log的SLF4J日志对象
@Component  // 标记为Spring组件，注册到Spring容器中
public class OrderFeignFallbackFactory implements FallbackFactory<OrderFeignClient> {  // 实现Feign降级工厂接口

    /**
     * 创建降级处理对象
     * 当Feign远程调用失败时，Feign框架会自动调用此方法获取降级处理对象
     * @param cause 导致降级的异常原因（如网络超时、连接拒绝等）
     * @return 降级处理对象（OrderFeignClient的匿名实现）
     */
    @Override
    public OrderFeignClient create(Throwable cause) {  // 重写create方法
        log.error("订单服务调用失败", cause);  // 记录错误日志，包含异常堆栈信息
        return new OrderFeignClient() {  // 返回OrderFeignClient的匿名内部类实现（即降级逻辑）

            /**
             * 查询订单详情的降级实现
             * 当订单服务不可用时，返回友好的错误提示Result
             */
            @Override
            public Result<OrderVO> getOrderById(String userId, String orderId) {
                log.warn("查询订单详情降级处理: userId={}, orderId={}, 原因: {}", userId, orderId, cause.getMessage());
                return Result.error(503, "订单服务暂时不可用");
            }

            /**
             * 查询用户订单列表的降级实现
             * 当订单服务不可用时，返回友好的错误提示Result
             */
            @Override
            public Result<PageResult<OrderVO>> listOrders(String userId, int pageNum, int pageSize, String status) {
                log.warn("查询订单列表降级处理: userId={}, 原因: {}", userId, cause.getMessage());
                return Result.error(503, "订单服务暂时不可用");
            }

            /**
             * 取消订单的降级实现
             * 取消订单是写操作，不能静默失败（否则用户以为取消成功但实际未取消）
             * 因此抛出运行时异常，提示用户稍后重试
             */
            @Override
            public Result<Void> cancelOrder(String userId, String orderId) {
                log.error("取消订单失败: userId={}, orderId={}", userId, orderId);
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 支付订单的降级实现
             * 支付订单是关键业务路径，不能静默失败（否则会导致支付状态不一致）
             * 因此抛出运行时异常，提示用户稍后重试
             */
            @Override
            public Result<Void> payOrder(String userId, String orderId) {
                log.error("支付订单失败: userId={}, orderId={}", userId, orderId);
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }
        };
    }
}
