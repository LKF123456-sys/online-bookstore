package com.bookstore.admin.controller.api;

import com.bookstore.admin.service.PromotionService;
import com.bookstore.common.api.Result;
import com.bookstore.common.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评价 REST API 控制器
 * <p>
 * 职责：为 Vue 前端提供商品评价相关的 RESTful 接口，包括提交评价、
 *       查看用户评价列表、查看商品评价列表、删除评价。
 * <p>
 * 所属模块：bookstore-admin · controller · api
 * <p>
 * 说明：评价业务逻辑委托给 PromotionService（促销服务）统一处理，
 *       评价与促销 / 优惠券等共用一个 Service 层。
 * <p>
 * 用户身份获取：通过私有方法 getUserId() 统一处理，
 *       优先从请求头 X-User-Id（JWT 解析结果）获取，其次从 Session 获取。
 * <p>
 * 包含接口：
 * <ul>
 *   <li>POST   /api/reviews                    — 提交评价</li>
 *   <li>GET    /api/reviews                    — 当前用户评价列表</li>
 *   <li>GET    /api/reviews/product/{productId} — 商品评价列表</li>
 *   <li>DELETE /api/reviews/{reviewId}         — 删除评价</li>
 * </ul>
 *
 * @author bookstore
 */
// @Slf4j：Lombok 注解，自动生成 log 日志对象，用于记录运行时日志
@Slf4j
// @RestController：Spring MVC 注解，标识该类为 REST 控制器，
// 所有方法返回值自动序列化为 JSON 响应体
@RestController
// @RequestMapping：将控制器映射到 /api/reviews 路径下，所有接口 URL 以此为前缀
@RequestMapping("/api/reviews")
// @RequiredArgsConstructor：Lombok 注解，为所有 final 字段生成构造方法，
// Spring 自动注入对应的 Bean
@RequiredArgsConstructor
public class ReviewRestController {

    // 促销服务层依赖，评价相关业务逻辑（提交、查询、删除）均委托给该服务处理
    private final PromotionService promotionService;

    // ========================================================================
    // 用户身份辅助方法
    // ========================================================================

    /**
     * 获取当前请求用户的 ID
     * <p>
     * 优先从请求头 X-User-Id 获取（由 JWT 过滤器 / 网关解析后注入），
     * 其次从 HTTP Session 的用户对象中提取 userid 字段。
     * 两种方式均失败则抛出未登录异常。
     *
     * @param session      HTTP 会话对象，用于兼容模式下读取 user 属性
     * @param headerUserId 请求头 X-User-Id 的值，可为 null
     * @return 当前登录用户的 ID 字符串
     * @throws BusinessException(401) 当用户未登录或无法识别身份时
     */
    private String getUserId(HttpSession session, @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        // 优先使用请求头中的用户 ID（JWT 无状态认证模式）
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        // 回退到 Session 模式：从会话中获取登录时存入的 user 对象
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        // 若 Session 中有 user 对象，提取其中的 userid 字段作为用户标识
        if (user != null) return String.valueOf(user.get("userid"));
        // 无法识别用户身份，抛出 401 异常
        throw new BusinessException(401, "请先登录");
    }

    // ========================================================================
    // 提交评价接口
    // ========================================================================

    /**
     * 提交商品评价
     * <p>
     * 接收评价数据（商品 ID、评分、评价内容等），校验后写入数据库。
     * 通常限制同一用户对同一订单中的同一商品只能评价一次。
     *
     * @param reviewData 请求体，包含评价数据 Map（productId、rating、content 等）
     * @param session    HTTP 会话对象，用于获取当前用户 ID
     * @param userId     请求头 X-User-Id（可选）
     * @return Result 空成功响应，表示评价提交成功
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/reviews
    @PostMapping
    // @RequestBody：将 HTTP 请求体 JSON 反序列化为 Map 对象
    public Result<Void> submit(@RequestBody Map<String, Object> reviewData,
                                HttpSession session,
                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后委托 PromotionService 提交评价
        return promotionService.submitReview(getUserId(session, userId), reviewData);
    }

    // ========================================================================
    // 用户评价列表接口
    // ========================================================================

    /**
     * 获取当前登录用户的评价列表（分页）
     * <p>
     * 用户在"我的评价"页面查看自己发表过的所有评价。
     *
     * @param pageNum  页码，默认第 1 页
     * @param pageSize 每页条数，默认 10 条
     * @param session  HTTP 会话对象
     * @param userId   请求头 X-User-Id（可选）
     * @return Result 包含分页评价列表的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/reviews
    @GetMapping
    public Result<Map<String, Object>> myReviews(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  HttpSession session,
                                                  @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后查询该用户的所有评价（分页）
        return promotionService.userReviews(getUserId(session, userId), pageNum, pageSize);
    }

    // ========================================================================
    // 商品评价列表接口
    // ========================================================================

    /**
     * 获取指定商品的所有评价（分页）
     * <p>
     * 不需要用户登录，任何访客均可查看商品评价。
     *
     * @param productId 商品 ID（路径变量），用于筛选该商品下的评价
     * @param pageNum   页码，默认第 1 页
     * @param pageSize  每页条数，默认 10 条
     * @return Result 包含分页评价列表的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/reviews/product/{productId}
    @GetMapping("/product/{productId}")
    // @PathVariable：将 URL 路径中的 {productId} 占位符绑定到方法参数
    public Result<Map<String, Object>> productReviews(@PathVariable String productId,
                                                       @RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize) {
        // 委托 PromotionService 查询指定商品的评价列表（公开接口，无需登录）
        return promotionService.productReviews(productId, pageNum, pageSize);
    }

    // ========================================================================
    // 删除评价接口
    // ========================================================================

    /**
     * 删除指定评价
     * <p>
     * 仅允许评价的发表者本人删除自己的评价（Service 层做权限校验）。
     *
     * @param reviewId 评价主键 ID（路径变量）
     * @param session  HTTP 会话对象
     * @param userId   请求头 X-User-Id（可选）
     * @return Result 空成功响应
     */
    // @DeleteMapping：将 HTTP DELETE 请求映射到 /api/reviews/{reviewId}
    @DeleteMapping("/{reviewId}")
    public Result<Void> delete(@PathVariable Long reviewId,
                                HttpSession session,
                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后删除指定评价（Service 层负责校验是否为本人操作）
        return promotionService.deleteReview(getUserId(session, userId), reviewId);
    }
}
