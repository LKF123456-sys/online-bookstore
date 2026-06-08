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
 * 公告 &amp; 优惠券 REST API 控制器
 * <p>
 * 职责：为 Vue 前端提供优惠券和公告相关的 RESTful 接口，包括可用优惠券列表、
 *       用户已领优惠券、领取优惠券，以及公告列表查询。
 * <p>
 * 所属模块：bookstore-admin · controller · api
 * <p>
 * 说明：优惠券与公告业务逻辑委托给 PromotionService（促销服务）统一处理。
 *       虽然 RequestMapping 路径为 /api/coupons，但公告接口也归入此控制器，
 *       因为公告和优惠券同属促销 / 营销模块。
 * <p>
 * 包含接口：
 * <ul>
 *   <li>GET  /api/coupons                      — 可用优惠券列表</li>
 *   <li>GET  /api/coupons/my                   — 用户已领取的优惠券</li>
 *   <li>POST /api/coupons/{couponId}/claim     — 领取优惠券</li>
 *   <li>GET  /api/coupons/announcements        — 公告列表</li>
 * </ul>
 *
 * @author bookstore
 */
// @Slf4j：Lombok 注解，自动生成 log 日志对象，用于记录运行时日志
@Slf4j
// @RestController：Spring MVC 注解，标识该类为 REST 控制器，
// 所有方法返回值自动序列化为 JSON 响应体
@RestController
// @RequestMapping：将控制器映射到 /api/coupons 路径下，所有接口 URL 以此为前缀
@RequestMapping("/api/coupons")
// @RequiredArgsConstructor：Lombok 注解，为所有 final 字段生成构造方法，
// Spring 自动注入对应的 Bean
@RequiredArgsConstructor
public class CouponRestController {

    // 促销服务层依赖，处理优惠券查询、领取以及公告查询等业务逻辑
    private final PromotionService promotionService;

    // ========================================================================
    // 可用优惠券列表接口
    // ========================================================================

    /**
     * 获取所有可用的优惠券列表（分页）
     * <p>
     * 返回当前有效的、所有用户均可领取的公共优惠券，供前端"领券中心"展示。
     * 此接口不需要登录即可访问。
     *
     * @param pageNum  页码，默认第 1 页
     * @param pageSize 每页条数，默认 20 条
     * @return Result 包含分页优惠券列表（Map 结构）的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/coupons
    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "20") int pageSize) {
        // 委托 PromotionService 查询可用优惠券分页列表（公开接口，无需登录）
        return promotionService.couponList(pageNum, pageSize);
    }

    // ========================================================================
    // 用户已领优惠券接口
    // ========================================================================

    /**
     * 获取当前用户已领取的优惠券列表
     * <p>
     * 需要用户登录，返回该用户已领取但可能尚未使用的优惠券。
     *
     * @param session HTTP 会话对象，用于获取当前用户身份
     * @param userId  请求头 X-User-Id（可选）
     * @return Result 包含用户优惠券列表的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/coupons/my
    @GetMapping("/my")
    public Result<?> myCoupons(HttpSession session,
                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后查询该用户已领取的优惠券
        return promotionService.userCoupons(resolveUserId(session, userId));
    }

    // ========================================================================
    // 领取优惠券接口
    // ========================================================================

    /**
     * 当前用户领取指定优惠券
     * <p>
     * 需要用户登录。Service 层会校验优惠券是否有效、库存是否充足、用户是否已领取等。
     *
     * @param couponId 优惠券主键 ID（路径变量）
     * @param session  HTTP 会话对象
     * @param userId   请求头 X-User-Id（可选）
     * @return Result 空成功响应，表示领取成功
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/coupons/{couponId}/claim
    @PostMapping("/{couponId}/claim")
    public Result<Void> claim(@PathVariable Long couponId,
                               HttpSession session,
                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后领取指定优惠券
        return promotionService.claimCoupon(resolveUserId(session, userId), couponId);
    }

    // ========================================================================
    // 公告列表接口
    // ========================================================================

    /**
     * 获取系统公告列表（分页）
     * <p>
     * 用于前端展示平台公告、活动通知等。此接口不需要登录即可访问。
     *
     * @param pageNum  页码，默认第 1 页
     * @param pageSize 每页条数，默认 10 条
     * @return Result 包含分页公告列表（Map 结构）的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/coupons/announcements
    @GetMapping("/announcements")
    public Result<Map<String, Object>> announcements(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        // 委托 PromotionService 查询公告分页列表（公开接口，无需登录）
        return promotionService.announcementList(pageNum, pageSize);
    }

    // ========================================================================
    // 用户身份解析辅助方法
    // ========================================================================

    /**
     * 解析当前请求的用户 ID
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
    private String resolveUserId(HttpSession session, String headerUserId) {
        // 优先使用请求头中的用户 ID（JWT 无状态认证模式）
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        // 回退到 Session 模式：从会话中获取登录时存入的 user 对象
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        // 若 Session 中有 user 对象，提取其中的 userid 字段作为用户标识
        if (user != null) return String.valueOf(user.get("userid"));
        // 无法识别用户身份，抛出 401 异常，需前端跳转登录页
        throw new BusinessException(401, "请先登录");
    }
}
