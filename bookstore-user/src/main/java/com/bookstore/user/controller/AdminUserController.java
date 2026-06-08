package com.bookstore.user.controller;  // 声明当前类所在的包路径，这里是控制器层

import com.bookstore.common.api.Result;  // 导入统一结果封装类，用于包装所有接口的返回数据
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，包含数据列表和分页信息（总条数、当前页、每页大小）
import com.bookstore.common.api.vo.UserVO;  // 导入用户视图对象（VO），用于返回给前端的用户数据
import com.bookstore.user.service.AccountService;  // 导入用户账户服务类，负责处理用户相关的业务逻辑
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造函数
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关的注解（@RestController、@RequestMapping、@GetMapping等）

/**
 * 管理端用户控制器
 * 供管理员使用的用户管理接口，包括分页查询用户列表、修改用户状态、删除用户等操作。
 * <p>
 * 该控制器提供以下接口：
 * - GET    /admin/user/list：分页查询用户列表（支持关键词搜索）
 * - PUT    /admin/user/{id}/status：修改用户状态（启用/禁用）
 * - DELETE /admin/user/{id}：删除用户
 * <p>
 * 注解说明：
 * - @RestController：REST风格控制器，方法返回值自动序列化为JSON
 * - @RequestMapping("/admin/user")：该控制器下所有接口的URL前缀为 /admin/user
 * - @RequiredArgsConstructor：Lombok注解，自动生成包含final字段的构造函数
 */
@RestController  // REST控制器注解，方法返回值直接作为HTTP响应体（JSON格式）
@RequestMapping("/admin/user")  // 请求映射，该控制器下所有接口的URL前缀为 /admin/user
@RequiredArgsConstructor  // Lombok注解，自动生成包含final成员变量的构造函数，用于Spring的构造函数依赖注入
public class AdminUserController {

    private final AccountService accountService;  // 用户账户服务，处理用户管理相关的业务逻辑

    /**
     * 分页查询用户列表
     * 管理员可以查看所有用户信息，支持按关键词搜索（用户名、邮箱、手机号）。
     *
     * @param pageNum  当前页码，默认为第1页（从前端查询参数中获取）
     * @param pageSize 每页显示条数，默认为10条（从前端查询参数中获取）
     * @param keyword  搜索关键词（可选），支持模糊匹配用户名、邮箱或手机号
     * @return 统一结果对象，成功时包含分页数据（用户列表 + 总条数 + 分页信息）
     */
    @GetMapping("/list")  // GET请求映射，处理 /admin/user/list 路径的查询请求
    public Result<PageResult<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,  // @RequestParam 从查询参数获取页码，默认值为1
            @RequestParam(defaultValue = "10") Integer pageSize,  // @RequestParam 从查询参数获取每页条数，默认值为10
            @RequestParam(required = false) String keyword) {  // @RequestParam 获取搜索关键词，required=false 表示该参数可选
        return Result.success(accountService.getUserList(pageNum, pageSize, keyword));  // 调用服务层分页查询用户列表
    }

    /**
     * 修改用户状态
     * 管理员可以启用或禁用指定用户账号。status=1表示启用，status=0表示禁用。
     *
     * @param id     用户ID，从URL路径中提取（如 /admin/user/123/status 中的 123）
     * @param status 目标状态值（1=启用，0=禁用），从查询参数获取
     * @return 统一结果对象，成功时无额外数据
     */
    @PutMapping("/{id}/status")  // PUT请求映射，处理 /admin/user/{id}/status 路径的状态修改请求
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {  // @PathVariable提取路径中的id，@RequestParam获取状态值
        accountService.updateUserStatus(id, status);  // 调用服务层修改用户状态
        return Result.success();  // 修改成功，返回不带数据的成功结果
    }

    /**
     * 删除用户
     * 管理员可以删除指定的用户账号。
     *
     * @param id 用户ID，从URL路径中提取
     * @return 统一结果对象，成功时无额外数据
     */
    @DeleteMapping("/{id}")  // DELETE请求映射，处理 /admin/user/{id} 路径的删除请求
    public Result<Void> deleteUser(@PathVariable Long id) {  // @PathVariable 从URL路径中提取用户ID
        accountService.deleteUser(id);  // 调用服务层删除用户
        return Result.success();  // 删除成功，返回不带数据的成功结果
    }
}
