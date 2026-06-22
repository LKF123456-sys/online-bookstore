package com.bookstore.admin.feign; // 声明当前类所在的包路径：Feign降级工厂层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入登录请求的数据传输对象DTO
import com.bookstore.common.api.dto.LoginDTO;
// 导入注册请求的数据传输对象DTO
import com.bookstore.common.api.dto.RegisterDTO;
// 导入用户信息视图对象VO
import com.bookstore.common.api.vo.UserVO;
// 导入Lombok的Slf4j日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring Cloud的FallbackFactory接口，用于创建Feign降级处理对象
import org.springframework.cloud.openfeign.FallbackFactory;
// 导入Spring的Component注解
import org.springframework.stereotype.Component;

import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 用户服务Feign降级工厂
 * 当用户服务不可用（网络异常、服务宕机、超时等）时，会执行这里的降级逻辑。
 *
 * 降级的作用：
 * - 防止因为一个服务的故障导致整个系统雪崩（服务雪崩效应）
 * - 给用户一个友好的错误提示，而不是直接报错
 * - 记录错误日志，方便排查问题
 *
 * 本类的降级策略：
 * - 查询类方法：返回友好的错误提示Result（503状态码）
 * - 写入/变更类方法：抛出RuntimeException，阻止业务继续执行
 */
@Slf4j // Lombok注解：自动生成名为log的SLF4J日志对象
@Component // 标记为Spring组件，注册到Spring容器中
public class UserFeignFallbackFactory implements FallbackFactory<UserFeignClient> { // 实现Feign降级工厂接口，泛型指定要降级的Feign客户端

    /**
     * 创建降级处理对象
     * 当Feign远程调用失败时，Feign框架会自动调用此方法获取降级处理对象
     * @param cause 导致降级的异常原因（如网络超时、连接拒绝等）
     * @return 降级处理对象（UserFeignClient的匿名实现）
     */
    @Override
    public UserFeignClient create(Throwable cause) { // 重写create方法，参数cause是导致降级的原始异常
        log.error("用户服务调用失败", cause); // 记录错误日志，包含异常堆栈信息
        return new UserFeignClient() { // 返回UserFeignClient的匿名内部类实现（即降级逻辑）

            // ======================== 查询类方法（返回友好错误提示） ========================

            /**
             * 获取用户信息（个人中心） — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<UserVO> getProfile(String userId) {
                log.warn("获取用户信息降级处理: userId={}, 原因: {}", userId, cause.getMessage());
                return Result.error(503, "用户服务暂时不可用");
            }

            /**
             * 管理后台 — 获取用户分页列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> adminUserList(int pageNum, int pageSize, String keyword) {
                log.warn("管理后台获取用户列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "用户服务暂时不可用");
            }

            /**
             * 管理后台 — 根据ID获取单个用户信息 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<UserVO> getUserById(Long id) {
                log.warn("获取用户详情降级处理: id={}, 原因: {}", id, cause.getMessage());
                return Result.error(503, "用户服务暂时不可用");
            }

            // ======================== 写入/变更类方法（抛出异常） ========================

            /**
             * 用户登录 — 降级实现
             * 登录操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Map<String, Object>> login(LoginDTO dto) {
                log.error("用户登录失败: 原因: {}", cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后重试");
            }

            /**
             * 用户注册 — 降级实现
             * 注册操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Map<String, Object>> register(RegisterDTO dto) {
                log.error("用户注册失败: 原因: {}", cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后重试");
            }

            /**
             * 更新用户信息 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> updateProfile(String userId, Map<String, Object> updates) {
                log.error("更新用户信息失败: userId={}, 原因: {}", userId, cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后重试");
            }

            /**
             * 修改密码 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> updatePassword(String userId, Map<String, String> passwords) {
                log.error("修改密码失败: userId={}, 原因: {}", userId, cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 创建新用户 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> createUser(Map<String, Object> userData) {
                log.error("创建用户失败: 原因: {}", cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 更新用户信息 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> updateUser(Long id, Map<String, Object> userData) {
                log.error("更新用户失败: id={}, 原因: {}", id, cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 删除用户 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> deleteUser(Long id) {
                log.error("删除用户失败: id={}, 原因: {}", id, cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后重试");
            }
        };
    }
}
