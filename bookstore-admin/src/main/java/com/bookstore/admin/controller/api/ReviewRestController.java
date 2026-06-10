package com.bookstore.admin.controller.api; // 声明当前接口所属的包路径：REST API控制器层

// 导入促销服务类，处理评价、优惠券、公告等营销业务逻辑
import com.bookstore.admin.service.PromotionService;
// 导入公共模块的统一响应封装类Result，所有接口返回值都通过此类包装
import com.bookstore.common.api.Result;
// 导入业务异常类，用于抛出业务逻辑相关的异常（如未登录、权限不足等）
import com.bookstore.common.exception.BusinessException;
// 导入Jakarta Servlet的HTTP会话接口，用于获取当前登录用户信息
import jakarta.servlet.http.HttpSession;
// 导入Lombok的@RequiredArgsConstructor注解，为所有final字段生成构造函数
import lombok.RequiredArgsConstructor;
// 导入Lombok的@Slf4j注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 评价 REST API 控制器
 * 为Vue前端提供商品评价相关的RESTful接口，包括提交评价、查看评价、删除评价等。
 * 用户身份优先从请求头X-User-Id获取（JWT认证模式），其次从Session获取（兼容模式）。
 */
@Slf4j // Lombok注解，自动生成log日志对象
@RestController // 标记为REST控制器，返回值自动序列化为JSON响应体
@RequestMapping("/api/reviews") // 设置该控制器所有接口的URL前缀为/api/reviews
@RequiredArgsConstructor // Lombok自动生成包含final字段的构造函数，实现构造函数注入
public class ReviewRestController {

    // 注入促销服务，处理评价相关的业务逻辑
    private final PromotionService promotionService;

    /**
     * 获取当前登录用户的ID（私有辅助方法）
     * 优先从请求头X-User-Id获取（由JWT过滤器/网关解析后注入），
     * 其次从HTTP Session的user对象中提取userid字段。
     * 两种方式均失败则抛出未登录异常。
     *
     * @param session      HTTP会话对象，用于兼容模式下读取user属性
     * @param headerUserId 请求头X-User-Id的值，可为null
     * @return 当前登录用户的ID字符串
     * @throws BusinessException(401) 当用户未登录或无法识别身份时
     */
    private String getUserId(HttpSession session, @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        // 优先使用请求头中的用户ID（JWT无状态认证模式）
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        // 回退到Session模式：从会话中获取登录时存入的user对象
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        // 若Session中有user对象，提取其中的userid字段作为用户标识
        if (user != null) return String.valueOf(user.get("userid"));
        // 无法识别用户身份，抛出401未登录异常
        throw new BusinessException(401, "请先登录");
    }

    /**
     * 提交商品评价
     * 用户对已购买的商品进行评分和文字评价
     *
     * @param reviewData 评价数据Map，包含productId（商品ID）、rating（评分1-5）、content（评价内容）等
     * @param session    HTTP会话对象
     * @param userId     请求头X-User-Id（可选）
     * @return Result 包装的空返回体，表示提交成功或失败
     */
    // @PostMapping：将HTTP POST请求映射到/api/reviews路径，POST语义表示创建新资源
    @PostMapping
    public Result<Void> submit(
            // @RequestBody：评价数据通过请求体以JSON格式传入
            @RequestBody Map<String, Object> reviewData,
            // 注入HTTP会话对象，用于获取用户身份
            HttpSession session,
            // @RequestHeader：从请求头X-User-Id获取用户ID（可选，由网关注入）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用促销服务提交评价，getUserId解析用户身份
        return promotionService.submitReview(getUserId(session, userId), reviewData);
    }

    /**
     * 获取当前用户的评价列表（分页）
     *
     * @param pageNum 页码（从1开始），默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param session HTTP会话对象
     * @param userId 请求头X-User-Id（可选）
     * @return Result 包装的分页评价数据Map
     */
    // @GetMapping：将HTTP GET请求映射到/api/reviews路径
    @GetMapping
    public Result<Map<String, Object>> myReviews(
            // @RequestParam：页码，默认第1页
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：每页条数，默认10条
            @RequestParam(defaultValue = "10") int pageSize,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用促销服务查询当前用户的评价列表
        return promotionService.userReviews(getUserId(session, userId), pageNum, pageSize);
    }

    /**
     * 获取当前用户的评价列表（别名路径，兼容不同前端调用方式）
     * 功能与myReviews完全相同
     *
     * @param pageNum 页码（从1开始），默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param session HTTP会话对象
     * @param userId 请求头X-User-Id（可选）
     * @return Result 包装的分页评价数据Map
     */
    // @GetMapping：将HTTP GET请求映射到/api/reviews/user路径
    @GetMapping("/user")
    public Result<Map<String, Object>> myReviewsAlias(
            // @RequestParam：页码，默认第1页
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：每页条数，默认10条
            @RequestParam(defaultValue = "10") int pageSize,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 与myReviews逻辑完全相同，委托促销服务查询评价列表
        return promotionService.userReviews(getUserId(session, userId), pageNum, pageSize);
    }

    /**
     * 获取指定商品的评价列表（分页）
     * 用于商品详情页展示该商品的所有用户评价
     *
     * @param productId 商品ID，作为URL路径变量
     * @param pageNum   页码（从1开始），默认值为1
     * @param pageSize  每页条数，默认值为10
     * @return Result 包装的分页评价数据Map
     */
    // @GetMapping：将HTTP GET请求映射到/api/reviews/product/{productId}路径
    @GetMapping("/product/{productId}")
    public Result<Map<String, Object>> productReviews(
            // @PathVariable：将URL路径中的{productId}占位符的值绑定到方法参数
            @PathVariable String productId,
            // @RequestParam：页码，默认第1页
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：每页条数，默认10条
            @RequestParam(defaultValue = "10") int pageSize) {
        // 调用促销服务查询指定商品的评价列表（不需要用户身份）
        return promotionService.productReviews(productId, pageNum, pageSize);
    }

    /**
     * 删除评价
     * 用户只能删除自己提交的评价，微服务端会校验评价归属
     *
     * @param reviewId 评价主键ID，作为URL路径变量
     * @param session  HTTP会话对象
     * @param userId   请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将HTTP DELETE请求映射到/api/reviews/{reviewId}路径，DELETE语义表示删除资源
    @DeleteMapping("/{reviewId}")
    public Result<Void> delete(
            // @PathVariable：将URL路径中的{reviewId}占位符的值绑定到方法参数
            @PathVariable Long reviewId,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用促销服务删除评价，getUserId解析用户身份用于校验归属
        return promotionService.deleteReview(getUserId(session, userId), reviewId);
    }
}
