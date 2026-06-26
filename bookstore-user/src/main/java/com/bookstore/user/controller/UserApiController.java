package com.bookstore.user.controller;  // 声明当前类所在的包路径，这里是控制器层

import com.bookstore.common.api.Result;  // 导入统一结果封装类，用于包装所有接口的返回数据
import com.bookstore.common.api.dto.PasswordUpdateDTO;  // 导入密码修改数据传输对象，包含旧密码和新密码
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，包含数据列表和分页信息（虽然本类未直接使用，但其他相关类会用到）
import com.bookstore.common.api.vo.UserVO;  // 导入用户视图对象（VO），用于返回给前端的用户数据
import com.bookstore.user.service.AccountService;  // 导入用户账户服务类，负责处理用户相关的业务逻辑
import jakarta.validation.Valid;  // 导入参数校验注解，用于触发对DTO对象的字段校验
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造函数
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关的注解（@RestController、@RequestMapping、@GetMapping等）

/**
 * 用户API控制器
 * 处理普通用户对自己账户信息的操作请求，如查询个人信息、修改密码、修改资料等。
 * <p>
 * 该控制器提供以下接口：
 * - GET  /api/user/{id}：根据ID查询用户信息
 * - PUT  /api/user/{id}/password：修改用户密码
 * - PUT  /api/user/{id}/profile：修改用户资料（邮箱、手机号、头像等）
 * <p>
 * 注解说明：
 * - @RestController：REST风格控制器，方法返回值自动序列化为JSON
 * - @RequestMapping("/api/user")：该控制器下所有接口的URL前缀为 /api/user
 * - @RequiredArgsConstructor：Lombok注解，自动生成包含final字段的构造函数
 */
@RestController  // REST控制器注解，方法返回值直接作为HTTP响应体（JSON格式）
@RequestMapping("/api/user")  // 请求映射，该控制器下所有接口的URL前缀为 /api/user
@RequiredArgsConstructor  // Lombok注解，自动生成包含final成员变量的构造函数，用于Spring的构造函数依赖注入
public class UserApiController {

    private final AccountService accountService;  // 用户账户服务，处理用户信息查询、修改等业务逻辑

    /**
     * 根据用户ID查询用户信息
     * 前端传入用户ID，返回该用户的详细信息（不含密码等敏感字段）。
     *
     * @param id 用户ID，从URL路径中提取（如 /api/user/123 中的 123）
     * @return 统一结果对象，成功时包含用户视图对象（UserVOX
     */
    @GetMapping("/{id}")  // GET请求映射，处理 /api/user/{id} 路径的查询请求，{id} 是路径变量
    public Result<UserVO> getUserById(@PathVariable String id) {  // @PathVariable 从URL路径中提取id参数值
        return Result.success(accountService.getUserById(id));  // 调用服务层查询用户信息，用Result包装返回
    }

    /**
     * 修改用户密码
     * 用户提交旧密码和新密码，验证旧密码正确后更新为新密码。
     *
     * @param id  用户ID，从URL路径中提取
     * @param dto 密码修改对象，包含旧密码（oldPassword）和新密码（newPassword）
     * @return 统一结果对象，成功时无额外数据
     */
    @PutMapping("/{id}/password")  // PUT请求映射，处理 /api/user/{id}/password 路径的密码修改请求
    public Result<Void> updatePassword(@PathVariable String id, @Valid @RequestBody PasswordUpdateDTO dto) {  // @Valid校验参数，@RequestBody接收JSON请求体
        accountService.updatePassword(id, dto);  // 调用服务层修改密码
        return Result.success();  // 修改成功，返回不带数据的成功结果
    }

    /**
     * 修改用户资料
     * 用户可以修改自己的邮箱、手机号、头像等个人信息。
     *
     * @param id 用户ID，从URL路径中提取
     * @param vo 用户视图对象，包含要修改的资料信息（邮箱、手机号、头像等）
     * @return 统一结果对象，成功时无额外数据
     */
    @PutMapping("/{id}/profile")  // PUT请求映射，处理 /api/user/{id}/profile 路径的资料修改请求
    public Result<Void> updateProfile(@PathVariable String id, @RequestBody UserVO vo) {  // @RequestBody将JSON请求体转换为UserVO对象
        accountService.updateProfile(id, vo);  // 调用服务层修改用户资料
        return Result.success();  // 修改成功，返回不带数据的成功结果
    }
}
