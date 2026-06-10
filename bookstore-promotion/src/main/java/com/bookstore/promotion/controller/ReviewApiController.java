package com.bookstore.promotion.controller;  // 声明当前类所在的包路径，属于营销服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一返回结果封装类，用于包装API响应数据
import com.bookstore.common.api.dto.ReviewSubmitDTO;  // 导入评价提交数据传输对象，用于接收用户提交评价的数据
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.common.api.vo.ReviewVO;  // 导入评价视图对象，用于向前端返回评价信息
import com.bookstore.promotion.service.BookReviewService;  // 导入图书评价服务类，处理评价相关的业务逻辑
import jakarta.validation.Valid;  // 导入Jakarta验证注解，用于自动校验请求参数是否符合规则
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关注解，用于定义REST接口

/**
 * 面向普通用户的图书评价API控制器
 * 提供用户端的评价相关接口，包括：
 *   - 查看某个商品的评价列表
 *   - 提交评价
 *   - 查看我的评价
 *
 * 所有接口路径以 /api/review 开头
 */
@RestController  // 标记为REST控制器，方法返回值会自动序列化为JSON响应
@RequestMapping("/api/review")  // 设置该控制器所有接口的URL前缀为 /api/review
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class ReviewApiController {  // 图书评价API控制器类

    private final BookReviewService bookReviewService;  // 注入图书评价服务，用于处理评价相关的业务逻辑

    /**
     * 获取某个商品的评价列表（分页）
     * 只返回未被屏蔽的评价，按创建时间倒序排列
     *
     * @param productId 商品ID
     * @param pageNum   页码，默认第1页
     * @param pageSize  每页数量，默认10条
     * @return 分页的评价列表
     */
    @GetMapping("/product/{productId}")  // 映射GET请求到 /api/review/product/{productId}
    public Result<PageResult<ReviewVO>> getProductReviews(
            @PathVariable String productId,  // 从URL路径中获取商品ID
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从请求参数获取页码，未传时默认为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 从请求参数获取每页大小，未传时默认为10
        return Result.success(bookReviewService.getProductReviews(productId, pageNum, pageSize));  // 调用服务层获取商品评价列表，包装成成功响应返回
    }

    /**
     * 提交商品评价
     * 用户购买商品后可以对商品进行评价，包括评分、文字内容和图片
     *
     * @param userId 当前登录用户的ID（从请求属性中获取）
     * @param dto    评价提交数据（包含商品ID、评分、内容等），使用@Valid自动校验
     * @return 操作结果
     */
    @PostMapping  // 映射POST请求到 /api/review（使用控制器的前缀）
    public Result<Void> submitReview(@RequestHeader(value = "X-User-Id", required = false) String userId, @Valid @RequestBody ReviewSubmitDTO dto) {  // userId从请求头获取，dto从请求体JSON反序列化并自动校验
        bookReviewService.submitReview(userId, dto);  // 调用服务层提交评价
        return Result.success();  // 提交成功，返回成功响应
    }

    /**
     * 获取当前用户的评价列表（分页）
     * 查询该用户提交过的所有评价
     *
     * @param userId   当前登录用户的ID
     * @param pageNum  页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页的评价列表
     */
    @GetMapping("/my")  // 映射GET请求到 /api/review/my
    public Result<PageResult<ReviewVO>> getUserReviews(
            @RequestHeader(value = "X-User-Id", required = false) String userId,  // 从请求头中获取当前登录用户的ID
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从请求参数获取页码，未传时默认为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 从请求参数获取每页大小，未传时默认为10
        return Result.success(bookReviewService.getUserReviews(userId, pageNum, pageSize));  // 调用服务层获取用户评价列表，包装成成功响应返回
    }
}
