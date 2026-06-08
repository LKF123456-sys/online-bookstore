// 声明当前类所属的包路径，遵循 Spring Boot 项目标准分包结构
package com.bookstore.admin.feign;

// 导入公共模块的统一响应封装类 Result，用于包装所有接口返回结果
import com.bookstore.common.api.Result;
// 导入 Spring Cloud OpenFeign 的 @FeignClient 注解，用于声明式 HTTP 客户端
import org.springframework.cloud.openfeign.FeignClient;
// 导入 Spring MVC 的请求映射注解（GetMapping、PostMapping、DeleteMapping、RequestHeader、PathVariable、RequestParam、RequestBody）
// 这些注解用于描述 HTTP 请求的 URL、方法与参数绑定方式
import org.springframework.web.bind.annotation.*;

// 导入 Java 集合框架的 List 接口
import java.util.List;
// 导入 Java 集合框架的 Map 接口
import java.util.Map;

/**
 * 营销服务 Feign 客户端 — 声明式调用 bookstore-promotion 微服务
 * <p>
 * 该接口通过 Spring Cloud OpenFeign 以声明方式定义对营销微服务的 HTTP 调用。
 * Feign 框架会在运行时动态生成代理实现类，自动完成服务发现、负载均衡与 HTTP 请求发送。
 * 涵盖评价、优惠券、公告三大业务模块，以及对应的管理后台操作。
 * 注意：此类未设置 path 前缀，所有方法的 URL 路径需在方法级别各自完整声明。
 */
// @FeignClient 声明这是一个 Feign 客户端接口
//   - name = "bookstore-promotion"：指定要调用的微服务名称，对应 Nacos/注册中心中的服务名
//   - 注意：此接口未设置 path 属性，因此每个方法的路径需要包含完整 URL
@FeignClient(name = "bookstore-promotion")
public interface PromotionFeignClient {

    // ===== 评价（Review）接口 =====

    /**
     * 提交商品评价
     * <p>
     * 调用 bookstore-promotion 微服务的 /api/review 接口
     * 请求方式：POST
     * 用户购买并收货后可对商品进行评价，评价内容可包含评分、文字、图片等
     *
     * @param userId     当前登录用户的 ID，通过请求头 X-User-Id 传递（允许为空，以兼容未登录场景如匿名访问）
     * @param reviewData 评价数据 Map，包含 productId、orderId、rating（评分）、content（评价内容）、images（图片列表）等字段
     * @return Result 包装的空返回体，表示提交成功或失败
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/review 路径
    @PostMapping("/api/review")
    Result<Void> submitReview(
            // required=false 表示此请求头可选，允许某些场景下不传用户 ID
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            // @RequestBody：评价数据通过请求体以 JSON 格式传入
            @RequestBody Map<String, Object> reviewData);

    /**
     * 获取当前用户的评价列表
     * <p>
     * 调用 bookstore-promotion 微服务的 /api/review/my 接口
     * 请求方式：GET
     * 返回当前登录用户提交的所有评价记录
     *
     * @param userId   当前登录用户的 ID，通过请求头 X-User-Id 传递（必填）
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 10
     * @return Result 包装的分页评价数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/review/my 路径
    @GetMapping("/api/review/my")
    Result<Map<String, Object>> userReviews(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize);

    /**
     * 获取指定商品的评价列表
     * <p>
     * 调用 bookstore-promotion 微服务的 /api/review/product/{productId} 接口
     * 请求方式：GET
     * 用于商品详情页展示该商品的所有用户评价
     *
     * @param productId 商品 ID，作为 URL 路径变量
     * @param pageNum   页码（从 1 开始），默认值为 1
     * @param pageSize  每页条数，默认值为 10
     * @return Result 包装的分页评价数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/review/product/{productId} 路径
    @GetMapping("/api/review/product/{productId}")
    Result<Map<String, Object>> productReviews(
            // @PathVariable：将 URL 中的 {productId} 绑定到方法参数
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize);

    /**
     * 删除自己的评价
     * <p>
     * 调用 bookstore-promotion 微服务的 /api/review/{reviewId} 接口
     * 请求方式：DELETE
     * 用户只能删除自己提交的评价
     *
     * @param userId   当前登录用户的 ID，通过请求头传递，用于校验评价归属
     * @param reviewId 评价主键 ID，作为 URL 路径变量
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将 HTTP DELETE 请求映射到 /api/review/{reviewId} 路径，DELETE 语义表示删除资源
    @DeleteMapping("/api/review/{reviewId}")
    Result<Void> deleteReview(@RequestHeader("X-User-Id") String userId,
                              @PathVariable Long reviewId);

    // ===== 优惠券（Coupon）接口 =====

    /**
     * 获取可用优惠券列表（用户端，公开可领）
     * <p>
     * 调用 bookstore-promotion 微服务的 /api/coupon/list 接口
     * 请求方式：GET
     * 返回所有当前有效的优惠券，用户可以浏览和领取
     *
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 20
     * @return Result 包装的分页优惠券数据 Map，包含优惠券名称、面额、使用条件、有效期等信息
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/coupon/list 路径
    @GetMapping("/api/coupon/list")
    Result<Map<String, Object>> couponList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize);

    /**
     * 获取当前用户已领取的优惠券
     * <p>
     * 调用 bookstore-promotion 微服务的 /api/coupon/my 接口
     * 请求方式：GET
     *
     * @param userId 当前登录用户的 ID，通过请求头 X-User-Id 传递
     * @return Result 包装的优惠券列表，每个元素为包含优惠券详情和使用状态的 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/coupon/my 路径
    @GetMapping("/api/coupon/my")
    Result<List<Map<String, Object>>> userCoupons(@RequestHeader("X-User-Id") String userId);

    /**
     * 领取优惠券
     * <p>
     * 调用 bookstore-promotion 微服务的 /api/coupon/{couponId}/claim 接口
     * 请求方式：POST
     * 用户领取一张优惠券，微服务会校验库存、领取限制等规则
     *
     * @param userId   当前登录用户的 ID
     * @param couponId 优惠券主键 ID，作为 URL 路径变量
     * @return Result 包装的空返回体，成功时表示领取成功
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/coupon/{couponId}/claim 路径
    @PostMapping("/api/coupon/{couponId}/claim")
    Result<Void> claimCoupon(@RequestHeader("X-User-Id") String userId,
                             @PathVariable Long couponId);

    // ===== 公告（Announcement）接口 =====

    /**
     * 获取公告列表（用户端，公开可见）
     * <p>
     * 调用 bookstore-promotion 微服务的 /api/announcement/list 接口
     * 请求方式：GET
     *
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 10
     * @return Result 包装的分页公告数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/announcement/list 路径
    @GetMapping("/api/announcement/list")
    Result<Map<String, Object>> announcementList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize);

    // ===== 以下为管理后台专用接口 =====

    /**
     * 管理后台 — 获取评价列表（所有用户）
     * <p>
     * 调用 bookstore-promotion 微服务的 /admin/review/list 接口
     * 请求方式：GET
     * 管理后台可查看所有用户的评价，支持关键词模糊搜索
     *
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 10
     * @param keyword  搜索关键词（可选），用于按评价内容或商品名称模糊匹配
     * @return Result 包装的分页评价数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /admin/review/list 路径
    @GetMapping("/admin/review/list")
    Result<Map<String, Object>> adminReviewList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword);

    /**
     * 管理后台 — 删除评价（管理员强制删除）
     * <p>
     * 调用 bookstore-promotion 微服务的 /admin/review/{reviewId} 接口
     * 请求方式：DELETE
     * 与用户自行删除不同，管理员可以删除任何用户的评价
     *
     * @param reviewId 评价主键 ID，作为 URL 路径变量
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将 HTTP DELETE 请求映射到 /admin/review/{reviewId} 路径
    @DeleteMapping("/admin/review/{reviewId}")
    Result<Void> adminDeleteReview(@PathVariable Long reviewId);

    /**
     * 管理后台 — 获取优惠券列表
     * <p>
     * 调用 bookstore-promotion 微服务的 /admin/coupon/list 接口
     * 请求方式：GET
     *
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 10
     * @return Result 包装的分页优惠券数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /admin/coupon/list 路径
    @GetMapping("/admin/coupon/list")
    Result<Map<String, Object>> adminCouponList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize);

    /**
     * 管理后台 — 创建新优惠券
     * <p>
     * 调用 bookstore-promotion 微服务的 /admin/coupon 接口
     * 请求方式：POST
     *
     * @param couponData 优惠券数据 Map，包含名称、类型（满减/折扣）、面额、使用门槛、有效期、发放总量等
     * @return Result 包装的空返回体
     */
    // @PostMapping：将 HTTP POST 请求映射到 /admin/coupon 路径
    @PostMapping("/admin/coupon")
    // @RequestBody：优惠券数据通过请求体以 JSON 格式传入
    Result<Void> createCoupon(@RequestBody Map<String, Object> couponData);

    /**
     * 管理后台 — 获取公告列表
     * <p>
     * 调用 bookstore-promotion 微服务的 /admin/announcement/list 接口
     * 请求方式：GET
     *
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 10
     * @return Result 包装的分页公告数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /admin/announcement/list 路径
    @GetMapping("/admin/announcement/list")
    Result<Map<String, Object>> adminAnnouncementList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize);

    /**
     * 管理后台 — 创建新公告
     * <p>
     * 调用 bookstore-promotion 微服务的 /admin/announcement 接口
     * 请求方式：POST
     *
     * @param announcementData 公告数据 Map，包含标题、内容、优先级、生效时间等字段
     * @return Result 包装的空返回体
     */
    // @PostMapping：将 HTTP POST 请求映射到 /admin/announcement 路径
    @PostMapping("/admin/announcement")
    // @RequestBody：公告数据通过请求体以 JSON 格式传入
    Result<Void> createAnnouncement(@RequestBody Map<String, Object> announcementData);
}
