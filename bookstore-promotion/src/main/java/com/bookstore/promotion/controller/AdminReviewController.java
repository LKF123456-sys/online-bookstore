package com.bookstore.promotion.controller;  // 声明当前类所在的包路径，属于营销服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一返回结果封装类，用于包装API响应数据
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.common.api.vo.ReviewVO;  // 导入评价视图对象，用于返回评价信息
import com.bookstore.promotion.service.BookReviewService;  // 导入图书评价服务类，处理评价相关的业务逻辑
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关注解，用于定义REST接口

/**
 * 管理端评价控制器
 * 提供管理员对用户评价的管理接口，包括：
 *   - 查看所有评价列表（支持按屏蔽状态筛选）
 *   - 屏蔽/取消屏蔽评价
 *   - 置顶/取消置顶评价
 *   - 回复评价
 *   - 删除评价
 *
 * 所有接口路径以 /admin/review 开头
 */
@RestController  // 标记为REST控制器，方法返回值会自动序列化为JSON响应
@RequestMapping("/admin/review")  // 设置该控制器所有接口的URL前缀为 /admin/review
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class AdminReviewController {  // 管理端评价控制器类

    private final BookReviewService bookReviewService;  // 注入图书评价服务，用于处理评价相关的业务逻辑

    /**
     * 获取所有评价列表（分页）
     * 管理员可以查看所有评价，支持按屏蔽状态筛选
     *
     * @param pageNum  页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @param blocked  屏蔽状态筛选条件（0=未屏蔽，1=已屏蔽），不传则查看全部
     * @return 分页的评价列表
     */
    @GetMapping("/list")  // 映射GET请求到 /admin/review/list
    public Result<PageResult<ReviewVO>> getReviewList(
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从请求参数获取页码，未传时默认为1
            @RequestParam(defaultValue = "10") Integer pageSize,  // 从请求参数获取每页大小，未传时默认为10
            @RequestParam(required = false) Integer blocked) {  // 从请求参数获取屏蔽状态筛选条件，可选参数
        return Result.success(bookReviewService.getAllReviews(pageNum, pageSize, blocked));  // 调用服务层获取评价列表，包装成成功响应返回
    }

    /**
     * 屏蔽评价
     * 管理员可以屏蔽违规或不当的评价，屏蔽后普通用户看不到该评价
     *
     * @param id 评价ID
     * @return 操作结果
     */
    @PostMapping("/{id}/block")  // 映射POST请求到 /admin/review/{id}/block
    public Result<Void> blockReview(@PathVariable Long id) {  // 从URL路径获取评价ID
        bookReviewService.blockReview(id);  // 调用服务层屏蔽评价
        return Result.success();  // 操作成功，返回成功响应
    }

    /**
     * 取消屏蔽评价
     * 管理员可以取消之前被屏蔽的评价，恢复其可见状态
     *
     * @param id 评价ID
     * @return 操作结果
     */
    @PostMapping("/{id}/unblock")  // 映射POST请求到 /admin/review/{id}/unblock
    public Result<Void> unblockReview(@PathVariable Long id) {  // 从URL路径获取评价ID
        bookReviewService.unblockReview(id);  // 调用服务层取消屏蔽评价
        return Result.success();  // 操作成功，返回成功响应
    }

    /**
     * 置顶评价
     * 管理员可以将优质评价置顶，使其在评价列表中排在最前面
     *
     * @param id 评价ID
     * @return 操作结果
     */
    @PostMapping("/{id}/top")  // 映射POST请求到 /admin/review/{id}/top
    public Result<Void> topReview(@PathVariable Long id) {  // 从URL路径获取评价ID
        bookReviewService.topReview(id);  // 调用服务层置顶评价
        return Result.success();  // 操作成功，返回成功响应
    }

    /**
     * 取消置顶评价
     * 管理员可以取消评价的置顶状态
     *
     * @param id 评价ID
     * @return 操作结果
     */
    @PostMapping("/{id}/untop")  // 映射POST请求到 /admin/review/{id}/untop
    public Result<Void> untopReview(@PathVariable Long id) {  // 从URL路径获取评价ID
        bookReviewService.untopReview(id);  // 调用服务层取消置顶评价
        return Result.success();  // 操作成功，返回成功响应
    }

    /**
     * 回复评价
     * 管理员可以对用户的评价进行官方回复
     *
     * @param id    评价ID
     * @param reply 回复内容
     * @return 操作结果
     */
    @PostMapping("/{id}/reply")  // 映射POST请求到 /admin/review/{id}/reply
    public Result<Void> replyReview(@PathVariable Long id, @RequestParam String reply) {  // id从URL路径获取，reply从请求参数获取
        bookReviewService.replyReview(id, reply);  // 调用服务层回复评价
        return Result.success();  // 回复成功，返回成功响应
    }

    /**
     * 删除评价
     * 管理员可以删除违规或不当的评价
     *
     * @param id 评价ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")  // 映射DELETE请求到 /admin/review/{id}，用于删除资源
    public Result<Void> deleteReview(@PathVariable Long id) {  // 从URL路径获取评价ID
        bookReviewService.deleteReview(id);  // 调用服务层删除评价
        return Result.success();  // 删除成功，返回成功响应
    }
}
