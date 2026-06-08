package com.bookstore.admin.service;

import com.bookstore.admin.feign.UserFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.dto.LoginDTO;
import com.bookstore.common.api.dto.RegisterDTO;
import com.bookstore.common.api.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户服务 — 封装 UserFeignClient，添加统一错误处理和日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserFeignClient userFeignClient;

    /** 用户登录 */
    public Result<Map<String, Object>> login(LoginDTO dto) {
        log.info("UserService.login: username={}", dto.getUsername());
        return userFeignClient.login(dto);
    }

    /** 用户注册 */
    public Result<Map<String, Object>> register(RegisterDTO dto) {
        log.info("UserService.register: username={}", dto.getUsername());
        return userFeignClient.register(dto);
    }

    /** 获取用户信息 */
    public Result<UserVO> getProfile(String userId) {
        return userFeignClient.getProfile(userId);
    }

    /** 更新用户信息 */
    public Result<Void> updateProfile(String userId, Map<String, Object> updates) {
        log.info("UserService.updateProfile: userId={}", userId);
        return userFeignClient.updateProfile(userId, updates);
    }

    /** 修改密码 */
    public Result<Void> updatePassword(String userId, Map<String, String> passwords) {
        log.info("UserService.updatePassword: userId={}", userId);
        return userFeignClient.updatePassword(userId, passwords);
    }

    // ===== 管理后台 =====

    public Result<Map<String, Object>> adminUserList(int pageNum, int pageSize, String keyword) {
        return userFeignClient.adminUserList(pageNum, pageSize, keyword);
    }

    public Result<UserVO> getUserById(Long id) {
        return userFeignClient.getUserById(id);
    }

    public Result<Void> createUser(Map<String, Object> userData) {
        return userFeignClient.createUser(userData);
    }

    public Result<Void> updateUser(Long id, Map<String, Object> userData) {
        return userFeignClient.updateUser(id, userData);
    }

    public Result<Void> deleteUser(Long id) {
        return userFeignClient.deleteUser(id);
    }
}
