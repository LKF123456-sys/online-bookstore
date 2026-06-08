package com.bookstore.admin.feign;

import com.bookstore.common.api.Result;
import com.bookstore.common.api.dto.LoginDTO;
import com.bookstore.common.api.dto.RegisterDTO;
import com.bookstore.common.api.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户服务 Feign 客户端 — 声明式调用 bookstore-user 微服务
 * 替代原有 RestTemplate + Map<String,Object> 的直调模式
 */
@FeignClient(name = "bookstore-user", path = "/api")
public interface UserFeignClient {

    /** 用户登录 */
    @PostMapping("/auth/login")
    Result<Map<String, Object>> login(@RequestBody LoginDTO dto);

    /** 用户注册 */
    @PostMapping("/auth/register")
    Result<Map<String, Object>> register(@RequestBody RegisterDTO dto);

    /** 获取用户信息 */
    @GetMapping("/user/profile")
    Result<UserVO> getProfile(@RequestHeader("X-User-Id") String userId);

    /** 更新用户信息 */
    @PutMapping("/user/profile")
    Result<Void> updateProfile(@RequestHeader("X-User-Id") String userId,
                               @RequestBody Map<String, Object> updates);

    /** 修改密码 */
    @PutMapping("/user/password")
    Result<Void> updatePassword(@RequestHeader("X-User-Id") String userId,
                                @RequestBody Map<String, String> passwords);

    // ===== 管理后台接口 =====

    /** 用户列表（管理后台） */
    @GetMapping("/user/admin/list")
    Result<Map<String, Object>> adminUserList(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) String keyword);

    /** 获取单个用户 */
    @GetMapping("/user/{id}")
    Result<UserVO> getUserById(@PathVariable Long id);

    /** 创建用户 */
    @PostMapping("/user/admin")
    Result<Void> createUser(@RequestBody Map<String, Object> userData);

    /** 更新用户 */
    @PutMapping("/user/admin/{id}")
    Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData);

    /** 删除用户 */
    @DeleteMapping("/user/admin/{id}")
    Result<Void> deleteUser(@PathVariable Long id);
}
