package com.bookstore.order.feign;  // 声明当前类所在的包路径：Feign降级工厂层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入商品视图对象
import com.bookstore.common.api.vo.ProductVO;
// 导入Lombok的Slf4j日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring Cloud的FallbackFactory接口，用于创建Feign降级处理对象
import org.springframework.cloud.openfeign.FallbackFactory;
// 导入Spring的Component注解
import org.springframework.stereotype.Component;

/**
 * 商品服务Feign降级工厂
 * 当商品服务不可用（网络异常、服务宕机、超时等）时，会执行这里的降级逻辑。
 *
 * 降级的作用：
 * - 防止因为一个服务的故障导致整个系统雪崩（服务雪崩效应）
 * - 给用户一个友好的错误提示，而不是直接报错
 * - 记录错误日志，方便排查问题
 *
 * 本类的降级策略：记录错误日志并抛出运行时异常，告知用户商品服务暂时不可用
 */
@Slf4j  // Lombok注解：自动生成名为log的SLF4J日志对象，可以在代码中直接使用log.error()、log.info()等方法
@Component  // 标记为Spring组件，注册到Spring容器中
public class ProductFeignFallbackFactory implements FallbackFactory<ProductFeignClient> {  // 实现Feign降级工厂接口，泛型指定要降级的Feign客户端

    /**
     * 创建降级处理对象
     * 当Feign远程调用失败时，Feign框架会自动调用此方法获取降级处理对象
     * @param cause 导致降级的异常原因（如网络超时、连接拒绝等）
     * @return 降级处理对象（ProductFeignClient的匿名实现）
     */
    @Override
    public ProductFeignClient create(Throwable cause) {  // 重写create方法，参数cause是导致降级的原始异常
        log.error("商品服务调用失败", cause);  // 记录错误日志，包含异常堆栈信息
        return new ProductFeignClient() {  // 返回ProductFeignClient的匿名内部类实现（即降级逻辑）
            /**
             * 获取商品信息的降级实现
             * 当商品服务不可用时，返回友好的错误提示Result，而不是直接抛出异常
             * 这样可以避免因为查询商品详情失败而中断整个业务流程（如订单列表页加载）
             */
            @Override
            public Result<ProductVO> getProductById(String id) {  // 降级方法：获取商品信息
                log.warn("获取商品信息降级处理: id={}, 原因: {}", id, cause.getMessage());  // 记录降级日志（warn级别，非error）
                return Result.error(503, "商品服务暂时不可用");  // 返回503降级响应，调用方可根据code判断是否降级
            }

            /**
             * 更新商品库存的降级实现
             * 当商品服务不可用时，仍然抛出异常
             * 库存操作是关键业务路径，不能静默失败（否则会导致库存数据不一致）
             */
            @Override
            public void updateStock(String id, Integer quantity) {  // 降级方法：更新库存
                log.error("更新商品库存失败: {}, 数量: {}", id, quantity);  // 记录错误日志，包含商品ID和数量
                throw new RuntimeException("商品服务暂时不可用，请稍后重试");  // 抛出运行时异常，提示用户稍后重试
            }
        };
    }
}
