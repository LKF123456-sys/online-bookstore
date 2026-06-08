// 声明当前类所属的包路径，遵循 Spring Boot 项目标准分包结构
package com.bookstore.admin.service;

// 导入营销服务的 Feign 客户端接口，用于声明式调用 bookstore-promotion 微服务
import com.bookstore.admin.feign.PromotionFeignClient;
// 导入公共模块的统一响应封装类 Result，所有 Feign 调用返回值都通过此类包装
import com.bookstore.common.api.Result;
// Lombok 注解 @RequiredArgsConstructor：为所有 final 字段生成构造函数，Spring 会自动注入依赖
import lombok.RequiredArgsConstructor;
// Lombok 注解 @Slf4j：自动生成 log 静态字段（使用 SLF4J 日志门面），无需手动声明 Logger
import lombok.extern.slf4j.Slf4j;
// Spring 的 @Service 注解：将该类标记为 Spring 容器管理的业务逻辑层 Bean
import org.springframework.stereotype.Service;

// 导入 Java 集合框架的 List 接口
import java.util.List;
// 导入 Java 集合框架的 Map 接口
import java.util.Map;

/**
 * 营销服务 — 封装 PromotionFeignClient，添加统一错误处理和日志
 * <p>
 * 该类是 Admin 端的营销业务逻辑层，负责：
 * <ul>
 *   <li>封装对 bookstore-promotion 微服务的远程调用（通过 PromotionFeignClient）</li>
 *   <li>评价管理：提交评价、查看评价、删除评价</li>
 *   <li>优惠券管理：查看可用优惠券、领取优惠券、后台管理优惠券</li>
 *   <li>公告管理：查看公告、后台管理公告</li>
 *   <li>关键写操作（提交评价、删除评价、领取优惠券）使用 INFO 日志记录</li>
 *   <li>读操作直接委托，不额外记录日志以保持性能</li>
 * </ul>
 */
// @Slf4j：Lombok 会在编译时生成日志对象 log，可直接使用 log.info() 等方法
@Slf4j
// @Service：标识这是一个 Service 层组件，Spring 会扫描并创建单例 Bean 管理其生命周期
@Service
// @RequiredArgsConstructor：Lombok 自动生成包含所有 final 字段的构造函数，实现构造函数注入
@RequiredArgsConstructor
public class PromotionService {

    // 营销服务 Feign 客户端，通过构造函数注入
    // 声明为 final 确保一旦注入后不可变，提高线程安全性
    private final PromotionFeignClient promotionFeignClient;

    // ===== 评价（Review）相关方法 =====

    /**
     * 提交商品评价
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /api/review 接口（POST 方法）。
     * 用户购买后可对商品进行评分和文字评价。
     *
     * @param userId     当前登录用户的 ID
     * @param reviewData 评价数据 Map，包含 productId、rating、content 等
     * @return Result 包装的空返回体
     */
    public Result<Void> submitReview(String userId, Map<String, Object> reviewData) {
        // 记录 INFO 日志：评价提交属于用户交互操作，便于审计
        log.info("PromotionService.submitReview: userId={}", userId);
        // 委托 Feign 客户端向 bookstore-promotion 微服务发送 POST 请求提交评价
        return promotionFeignClient.submitReview(userId, reviewData);
    }

    /**
     * 获取当前用户的评价列表
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /api/review/my 接口。
     *
     * @param userId   当前登录用户的 ID
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return Result 包装的分页评价数据 Map
     */
    public Result<Map<String, Object>> userReviews(String userId, int pageNum, int pageSize) {
        // 直接委托 Feign 客户端，读操作不记录日志
        return promotionFeignClient.userReviews(userId, pageNum, pageSize);
    }

    /**
     * 获取指定商品的评价列表
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /api/review/product/{productId} 接口。
     * 用于商品详情页展示所有用户的评价。
     *
     * @param productId 商品 ID
     * @param pageNum   页码（从 1 开始）
     * @param pageSize  每页条数
     * @return Result 包装的分页评价数据 Map
     */
    public Result<Map<String, Object>> productReviews(String productId, int pageNum, int pageSize) {
        // 直接委托 Feign 客户端
        return promotionFeignClient.productReviews(productId, pageNum, pageSize);
    }

    /**
     * 删除自己的评价
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /api/review/{reviewId} 接口（DELETE 方法）。
     * 用户只能删除自己提交的评价，微服务端会校验归属。
     *
     * @param userId   当前登录用户的 ID
     * @param reviewId 评价主键 ID
     * @return Result 包装的空返回体
     */
    public Result<Void> deleteReview(String userId, Long reviewId) {
        // 记录 INFO 日志：删除评价是关键操作，记录操作人和评价 ID 用于审计
        log.info("PromotionService.deleteReview: reviewId={}, userId={}", reviewId, userId);
        // 委托 Feign 客户端发送 DELETE 请求
        return promotionFeignClient.deleteReview(userId, reviewId);
    }

    // ===== 优惠券（Coupon）相关方法 =====

    /**
     * 获取可用优惠券列表（用户端，公开可领）
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /api/coupon/list 接口。
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return Result 包装的分页优惠券数据 Map
     */
    public Result<Map<String, Object>> couponList(int pageNum, int pageSize) {
        return promotionFeignClient.couponList(pageNum, pageSize);
    }

    /**
     * 获取当前用户已领取的优惠券
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /api/coupon/my 接口。
     *
     * @param userId 当前登录用户的 ID
     * @return Result 包装的优惠券列表
     */
    public Result<List<Map<String, Object>>> userCoupons(String userId) {
        // 直接委托 Feign 客户端，通过请求头传递用户身份
        return promotionFeignClient.userCoupons(userId);
    }

    /**
     * 领取优惠券
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /api/coupon/{couponId}/claim 接口（POST 方法）。
     * 微服务端会校验库存、领取次数限制等规则。
     *
     * @param userId   当前登录用户的 ID
     * @param couponId 优惠券主键 ID
     * @return Result 包装的空返回体
     */
    public Result<Void> claimCoupon(String userId, Long couponId) {
        // 直接委托 Feign 客户端发送 POST 请求领取优惠券
        return promotionFeignClient.claimCoupon(userId, couponId);
    }

    // ===== 公告（Announcement）相关方法 =====

    /**
     * 获取公告列表（用户端，公开可见）
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /api/announcement/list 接口。
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return Result 包装的分页公告数据 Map
     */
    public Result<Map<String, Object>> announcementList(int pageNum, int pageSize) {
        // 直接委托 Feign 客户端
        return promotionFeignClient.announcementList(pageNum, pageSize);
    }

    // ===== 以下为管理后台专用方法 =====

    /**
     * 管理后台 — 获取评价列表（所有用户的评价）
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /admin/review/list 接口。
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @param keyword  搜索关键词（可选）
     * @return Result 包装的分页评价数据 Map
     */
    public Result<Map<String, Object>> adminReviewList(int pageNum, int pageSize, String keyword) {
        return promotionFeignClient.adminReviewList(pageNum, pageSize, keyword);
    }

    /**
     * 管理后台 — 删除评价（管理员强制删除任何用户的评价）
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /admin/review/{reviewId} 接口（DELETE 方法）。
     *
     * @param reviewId 评价主键 ID
     * @return Result 包装的空返回体
     */
    public Result<Void> adminDeleteReview(Long reviewId) {
        return promotionFeignClient.adminDeleteReview(reviewId);
    }

    /**
     * 管理后台 — 获取优惠券列表
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /admin/coupon/list 接口。
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return Result 包装的分页优惠券数据 Map
     */
    public Result<Map<String, Object>> adminCouponList(int pageNum, int pageSize) {
        return promotionFeignClient.adminCouponList(pageNum, pageSize);
    }

    /**
     * 管理后台 — 创建新优惠券
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /admin/coupon 接口（POST 方法）。
     *
     * @param couponData 优惠券数据 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> createCoupon(Map<String, Object> couponData) {
        return promotionFeignClient.createCoupon(couponData);
    }

    /**
     * 管理后台 — 获取公告列表
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /admin/announcement/list 接口。
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return Result 包装的分页公告数据 Map
     */
    public Result<Map<String, Object>> adminAnnouncementList(int pageNum, int pageSize) {
        return promotionFeignClient.adminAnnouncementList(pageNum, pageSize);
    }

    /**
     * 管理后台 — 创建新公告
     * <p>
     * 通过 Feign 调用 bookstore-promotion 微服务的 /admin/announcement 接口（POST 方法）。
     *
     * @param announcementData 公告数据 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> createAnnouncement(Map<String, Object> announcementData) {
        return promotionFeignClient.createAnnouncement(announcementData);
    }
}
