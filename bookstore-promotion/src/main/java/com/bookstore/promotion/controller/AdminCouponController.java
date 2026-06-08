package com.bookstore.promotion.controller;  // 声明当前类所在的包路径，属于营销服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一返回结果封装类，用于包装API响应数据
import com.bookstore.common.api.dto.CouponCreateDTO;  // 导入优惠券创建数据传输对象，用于接收管理员创建/编辑优惠券的数据
import com.bookstore.common.api.vo.CouponVO;  // 导入优惠券视图对象，用于返回优惠券信息
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.promotion.service.CouponService;  // 导入优惠券服务类，处理优惠券相关的业务逻辑
import jakarta.validation.Valid;  // 导入Jakarta验证注解，用于自动校验请求参数
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关注解，用于定义REST接口

/**
 * 管理端优惠券控制器
 * 提供管理员对优惠券的增删改查和状态管理接口，包括：
 *   - 查看所有优惠券列表（包含已禁用的）
 *   - 创建优惠券
 *   - 编辑优惠券
 *   - 删除优惠券
 *   - 启用/禁用优惠券
 *
 * 所有接口路径以 /admin/coupon 开头
 */
@RestController  // 标记为REST控制器，方法返回值会自动序列化为JSON响应
@RequestMapping("/admin/coupon")  // 设置该控制器所有接口的URL前缀为 /admin/coupon
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class AdminCouponController {  // 管理端优惠券控制器类

    private final CouponService couponService;  // 注入优惠券服务，用于处理优惠券的业务逻辑

    /**
     * 获取所有优惠券列表（分页）
     * 管理员可以查看所有优惠券，包括已禁用和已过期的
     *
     * @param pageNum  页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页的优惠券列表
     */
    @GetMapping("/list")  // 映射GET请求到 /admin/coupon/list
    public Result<PageResult<CouponVO>> getCouponList(
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从请求参数获取页码，未传时默认为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 从请求参数获取每页大小，未传时默认为10
        return Result.success(couponService.getAllCoupons(pageNum, pageSize));  // 调用服务层获取全部优惠券列表，包装成成功响应返回
    }

    /**
     * 创建优惠券
     * 管理员填写优惠券信息后提交，系统创建新的优惠券
     *
     * @param dto 优惠券创建数据（包含名称、类型、金额、有效期等），使用@Valid自动校验
     * @return 操作结果
     */
    @PostMapping  // 映射POST请求到 /admin/coupon（使用控制器的前缀）
    public Result<Void> createCoupon(@Valid @RequestBody CouponCreateDTO dto) {  // 从请求体JSON反序列化为DTO对象，并自动校验字段合法性
        couponService.createCoupon(dto);  // 调用服务层创建优惠券
        return Result.success();  // 创建成功，返回成功响应
    }

    /**
     * 更新优惠券信息
     * 管理员可以修改已有优惠券的名称、金额、有效期等信息
     *
     * @param id  优惠券ID
     * @param dto 优惠券更新数据，使用@Valid自动校验
     * @return 操作结果
     */
    @PutMapping("/{id}")  // 映射PUT请求到 /admin/coupon/{id}，用于更新资源
    public Result<Void> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponCreateDTO dto) {  // id从URL路径获取，dto从请求体获取并校验
        couponService.updateCoupon(id, dto);  // 调用服务层更新优惠券信息
        return Result.success();  // 更新成功，返回成功响应
    }

    /**
     * 删除优惠券
     * 管理员可以删除不需要的优惠券
     *
     * @param id 优惠券ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")  // 映射DELETE请求到 /admin/coupon/{id}，用于删除资源
    public Result<Void> deleteCoupon(@PathVariable Long id) {  // 从URL路径获取优惠券ID
        couponService.deleteCoupon(id);  // 调用服务层删除优惠券
        return Result.success();  // 删除成功，返回成功响应
    }

    /**
     * 更新优惠券状态（启用/禁用）
     * 管理员可以启用或禁用优惠券，禁用后用户无法领取
     *
     * @param id     优惠券ID
     * @param status 目标状态（1=启用，0=禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")  // 映射PUT请求到 /admin/coupon/{id}/status，用于更新优惠券状态
    public Result<Void> updateCouponStatus(@PathVariable Long id, @RequestParam Integer status) {  // id从URL路径获取，status从请求参数获取
        couponService.updateCouponStatus(id, status);  // 调用服务层更新优惠券状态
        return Result.success();  // 状态更新成功，返回成功响应
    }
}
