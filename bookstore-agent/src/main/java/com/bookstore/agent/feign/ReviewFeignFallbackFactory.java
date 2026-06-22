package com.bookstore.agent.feign;  // 声明当前类所在的包路径：Feign降级工厂层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入分页结果封装
import com.bookstore.common.api.vo.PageResult;
// 导入评价视图对象
import com.bookstore.common.api.vo.ReviewVO;
// 导入Lombok的Slf4j日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring Cloud的FallbackFactory接口，用于创建Feign降级处理对象
import org.springframework.cloud.openfeign.FallbackFactory;
// 导入Spring的Component注解
import org.springframework.stereotype.Component;

/**
 * 评价服务Feign降级工厂
 * 当评价服务（挂载在营销微服务上）不可用（网络异常、服务宕机、超时等）时，会执行这里的降级逻辑。
 *
 * 降级的作用：
 * - 防止因为一个服务的故障导致整个系统雪崩（服务雪崩效应）
 * - 给用户一个友好的错误提示，而不是直接报错
 * - 记录错误日志，方便排查问题
 *
 * 本类的降级策略：评价查询为只读操作，返回友好的错误提示Result
 */
@Slf4j  // Lombok注解：自动生成名为log的SLF4J日志对象
@Component  // 标记为Spring组件，注册到Spring容器中
public class ReviewFeignFallbackFactory implements FallbackFactory<ReviewFeignClient> {  // 实现Feign降级工厂接口

    /**
     * 创建降级处理对象
     * 当Feign远程调用失败时，Feign框架会自动调用此方法获取降级处理对象
     * @param cause 导致降级的异常原因（如网络超时、连接拒绝等）
     * @return 降级处理对象（ReviewFeignClient的匿名实现）
     */
    @Override
    public ReviewFeignClient create(Throwable cause) {  // 重写create方法
        log.error("评价服务调用失败", cause);  // 记录错误日志，包含异常堆栈信息
        return new ReviewFeignClient() {  // 返回ReviewFeignClient的匿名内部类实现（即降级逻辑）

            /**
             * 获取商品评价列表的降级实现
             * 当评价服务不可用时，返回友好的错误提示Result
             */
            @Override
            public Result<PageResult<ReviewVO>> getProductReviews(String productId, int pageNum, int pageSize) {
                log.warn("获取商品评价降级处理: productId={}, 原因: {}", productId, cause.getMessage());
                return Result.error(503, "评价服务暂时不可用");
            }
        };
    }
}
