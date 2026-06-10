package com.bookstore.promotion.controller;  // 声明当前类所在的包路径，属于营销服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一返回结果封装类，用于包装API响应数据
import com.bookstore.common.api.vo.CouponVO;  // 导入优惠券视图对象，用于向前端返回优惠券信息
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.promotion.service.CouponService;  // 导入优惠券服务类，处理优惠券相关的业务逻辑
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法（实现依赖注入）
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关注解，用于定义REST接口

import java.util.List;  // 导入Java集合List，用于存储列表数据

/**
 * 面向普通用户的优惠券API控制器
 * 提供用户端的优惠券相关接口，包括：
 *   - 查看可用优惠券列表
 *   - 领取优惠券
 *   - 查看我的优惠券
 *
 * 所有接口路径以 /api/coupon 开头
 */
@RestController  // 标记为REST控制器，方法返回值会自动序列化为JSON响应
@RequestMapping("/api/coupon")  // 设置该控制器所有接口的URL前缀为 /api/coupon
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class CouponApiController {  // 优惠券API控制器类

    private final CouponService couponService;  // 注入优惠券服务，用于处理优惠券的业务逻辑

    /**
     * 获取可用优惠券列表（分页）
     * 用户可以浏览当前有效的、可领取的优惠券
     *
     * @param pageNum  页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页的优惠券列表
     */
    @GetMapping("/list")  // 映射GET请求到 /api/coupon/list
    public Result<PageResult<CouponVO>> getCouponList(
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从请求参数获取页码，未传时默认为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 从请求参数获取每页大小，未传时默认为10
        return Result.success(couponService.getCouponList(pageNum, pageSize));  // 调用服务层获取分页优惠券列表，包装成成功响应返回
    }

    /**
     * 领取优惠券
     * 用户点击"领取"按钮后调用此接口，会校验优惠券的有效性、是否已领取等
     *
     * @param userId     当前登录用户的ID（从请求头/网关注入）
     * @param id         优惠券ID
     * @return 操作结果
     */
    @PostMapping("/{id}/claim")  // 映射POST请求到 /api/coupon/{id}/claim，{id}是路径变量
    public Result<Void> claimCoupon(@RequestHeader(value = "X-User-Id", required = false) String userId, @PathVariable Long id) {  // userId从请求头中获取（由网关注入），id从URL路径中获取
        couponService.claimCoupon(userId, id);  // 调用服务层执行领取优惠券的逻辑
        return Result.success();  // 领取成功，返回成功响应（无返回数据）
    }

    /**
     * 获取当前用户已领取的优惠券列表
     * 查询该用户名下所有未使用的优惠券
     *
     * @param userId 当前登录用户的ID
     * @return 用户的优惠券列表
     */
    @GetMapping("/my")  // 映射GET请求到 /api/coupon/my
    public Result<List<CouponVO>> getUserCoupons(@RequestHeader(value = "X-User-Id", required = false) String userId) {  // 从请求头中获取当前登录用户的ID
        return Result.success(couponService.getUserCoupons(userId));  // 调用服务层查询用户优惠券列表，包装成成功响应返回
    }
}
