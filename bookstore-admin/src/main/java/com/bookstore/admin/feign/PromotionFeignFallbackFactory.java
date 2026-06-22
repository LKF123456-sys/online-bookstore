package com.bookstore.admin.feign; // 声明当前类所在的包路径：Feign降级工厂层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入Lombok的Slf4j日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring Cloud的FallbackFactory接口，用于创建Feign降级处理对象
import org.springframework.cloud.openfeign.FallbackFactory;
// 导入Spring的Component注解
import org.springframework.stereotype.Component;

import java.util.List; // 导入Java集合框架的List接口
import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 营销服务Feign降级工厂
 * 当营销服务不可用（网络异常、服务宕机、超时等）时，会执行这里的降级逻辑。
 *
 * 降级的作用：
 * - 防止因为一个服务的故障导致整个系统雪崩（服务雪崩效应）
 * - 给用户一个友好的错误提示，而不是直接报错
 * - 记录错误日志，方便排查问题
 *
 * 本类的降级策略：
 * - 查询类方法：返回友好的错误提示Result（503状态码）
 * - 写入/变更类方法：抛出RuntimeException，阻止业务继续执行
 */
@Slf4j // Lombok注解：自动生成名为log的SLF4J日志对象
@Component // 标记为Spring组件，注册到Spring容器中
public class PromotionFeignFallbackFactory implements FallbackFactory<PromotionFeignClient> { // 实现Feign降级工厂接口，泛型指定要降级的Feign客户端

    /**
     * 创建降级处理对象
     * 当Feign远程调用失败时，Feign框架会自动调用此方法获取降级处理对象
     * @param cause 导致降级的异常原因（如网络超时、连接拒绝等）
     * @return 降级处理对象（PromotionFeignClient的匿名实现）
     */
    @Override
    public PromotionFeignClient create(Throwable cause) { // 重写create方法，参数cause是导致降级的原始异常
        log.error("营销服务调用失败", cause); // 记录错误日志，包含异常堆栈信息
        return new PromotionFeignClient() { // 返回PromotionFeignClient的匿名内部类实现（即降级逻辑）

            // ======================== 查询类方法（返回友好错误提示） ========================

            /**
             * 获取当前用户的评价列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> userReviews(String userId, int pageNum, int pageSize) {
                log.warn("获取用户评价列表降级处理: userId={}, 原因: {}", userId, cause.getMessage());
                return Result.error(503, "营销服务暂时不可用");
            }

            /**
             * 获取指定商品的评价列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> productReviews(String productId, int pageNum, int pageSize) {
                log.warn("获取商品评价列表降级处理: productId={}, 原因: {}", productId, cause.getMessage());
                return Result.error(503, "营销服务暂时不可用");
            }

            /**
             * 获取可用优惠券列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> couponList(int pageNum, int pageSize) {
                log.warn("获取优惠券列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "营销服务暂时不可用");
            }

            /**
             * 获取当前用户已领取的优惠券 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<List<Map<String, Object>>> userCoupons(String userId) {
                log.warn("获取用户优惠券降级处理: userId={}, 原因: {}", userId, cause.getMessage());
                return Result.error(503, "营销服务暂时不可用");
            }

            /**
             * 获取公告列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> announcementList(int pageNum, int pageSize) {
                log.warn("获取公告列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "营销服务暂时不可用");
            }

            /**
             * 管理后台 — 获取评价列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> adminReviewList(int pageNum, int pageSize, String keyword) {
                log.warn("管理后台获取评价列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "营销服务暂时不可用");
            }

            /**
             * 管理后台 — 获取优惠券列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> adminCouponList(int pageNum, int pageSize) {
                log.warn("管理后台获取优惠券列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "营销服务暂时不可用");
            }

            /**
             * 管理后台 — 获取公告列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> adminAnnouncementList(int pageNum, int pageSize) {
                log.warn("管理后台获取公告列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "营销服务暂时不可用");
            }

            // ======================== 写入/变更类方法（抛出异常） ========================

            /**
             * 提交商品评价 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> submitReview(String userId, Map<String, Object> reviewData) {
                log.error("提交评价失败: userId={}, 原因: {}", userId, cause.getMessage());
                throw new RuntimeException("营销服务暂时不可用，请稍后重试");
            }

            /**
             * 删除自己的评价 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> deleteReview(String userId, Long reviewId) {
                log.error("删除评价失败: reviewId={}, 原因: {}", reviewId, cause.getMessage());
                throw new RuntimeException("营销服务暂时不可用，请稍后重试");
            }

            /**
             * 领取优惠券 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> claimCoupon(String userId, Long couponId) {
                log.error("领取优惠券失败: couponId={}, 原因: {}", couponId, cause.getMessage());
                throw new RuntimeException("营销服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 删除评价 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> adminDeleteReview(Long reviewId) {
                log.error("管理后台删除评价失败: reviewId={}, 原因: {}", reviewId, cause.getMessage());
                throw new RuntimeException("营销服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 创建新优惠券 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> createCoupon(Map<String, Object> couponData) {
                log.error("创建优惠券失败: 原因: {}", cause.getMessage());
                throw new RuntimeException("营销服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 创建新公告 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> createAnnouncement(Map<String, Object> announcementData) {
                log.error("创建公告失败: 原因: {}", cause.getMessage());
                throw new RuntimeException("营销服务暂时不可用，请稍后重试");
            }
        };
    }
}
