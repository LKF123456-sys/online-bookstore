// 声明当前类所属的包路径，遵循 Spring Boot 项目标准分包结构
package com.bookstore.admin.service;

// 导入用户服务的 Feign 客户端接口，用于声明式调用 bookstore-user 微服务
import com.bookstore.admin.feign.UserFeignClient;
// 导入公共模块的统一响应封装类 Result，所有 Feign 调用返回值都通过此类包装
import com.bookstore.common.api.Result;
// 导入登录请求的 DTO，用于传递用户名和密码给下游微服务
import com.bookstore.common.api.dto.LoginDTO;
// 导入注册请求的 DTO，用于传递注册信息给下游微服务
import com.bookstore.common.api.dto.RegisterDTO;
// 导入用户视图对象 VO，用于返回脱敏后的用户信息（不含密码等敏感字段）
import com.bookstore.common.api.vo.UserVO;
// Lombok 注解 @RequiredArgsConstructor：为所有 final 字段生成构造函数，Spring 会自动注入依赖
import lombok.RequiredArgsConstructor;
// Lombok 注解 @Slf4j：自动生成 log 静态字段（使用 SLF4J 日志门面），无需手动声明 Logger
import lombok.extern.slf4j.Slf4j;
// Spring 的 @Service 注解：将该类标记为 Spring 容器管理的业务逻辑层 Bean
import org.springframework.stereotype.Service;

// 导入 Java 集合框架的 Map 接口，用于灵活传递键值对数据
import java.util.Map;

/**
 * 用户服务 — 封装 UserFeignClient，添加统一错误处理和日志
 * <p>
 * 该类是 Admin 端的用户业务逻辑层，负责：
 * <ul>
 *   <li>封装对 bookstore-user 微服务的远程调用（通过 UserFeignClient）</li>
 *   <li>在调用前后添加日志记录，便于问题排查</li>
 *   <li>作为中间层，未来可在此添加缓存、权限校验、数据转换等增强逻辑</li>
 * </ul>
 * 所有方法均直接委托给 UserFeignClient 执行实际的 HTTP 调用，由 Feign 框架负责序列化、服务发现和负载均衡。
 */
// @Slf4j：Lombok 会在编译时生成 private static final org.slf4j.Logger log = ...; 语句
@Slf4j
// @Service：标识这是一个 Service 层组件，Spring 会扫描并创建单例 Bean
@Service
// @RequiredArgsConstructor：Lombok 自动生成包含所有 final 字段的构造函数，Spring 通过构造函数注入 userFeignClient
@RequiredArgsConstructor
public class UserService {

    // 用户服务 Feign 客户端，通过构造函数注入，由 Spring 和 Feign 框架共同管理
    // 声明为 final 确保一旦注入后不可变，配合 @RequiredArgsConstructor 自动生成构造函数
    private final UserFeignClient userFeignClient;

    /**
     * 用户登录
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/auth/login 接口。
     * 使用 INFO 级别记录登录请求（记录用户名，不记录密码以保护隐私）。
     *
     * @param dto 登录请求体，包含 username 和 password
     * @return Result 包装的登录结果 Map，成功时 data 中包含 JWT token 与用户简要信息
     */
    public Result<Map<String, Object>> login(LoginDTO dto) {
        // 记录 INFO 级别日志：标记方法名和登录用户名，用于审计和问题排查，不记录密码以保障安全
        log.info("UserService.login: username={}", dto.getUsername());
        // 委托 Feign 客户端发起 HTTP 请求到 bookstore-user 微服务，并将结果直接返回
        return userFeignClient.login(dto);
    }

    /**
     * 用户注册
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/auth/register 接口。
     *
     * @param dto 注册请求体，包含 username、password、email 等
     * @return Result 包装的注册结果 Map
     */
    public Result<Map<String, Object>> register(RegisterDTO dto) {
        // 记录 INFO 日志：标记注册操作及用户名，不记录密码等敏感信息
        log.info("UserService.register: username={}", dto.getUsername());
        // 委托 Feign 客户端发起注册请求
        return userFeignClient.register(dto);
    }

    /**
     * 获取当前用户的个人信息
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/user/profile 接口。
     * 用户身份通过请求头 X-User-Id 传递给下游微服务。
     *
     * @param userId 当前登录用户的 ID（通常由网关或拦截器从 JWT 中解析后传入）
     * @return Result 包装的 UserVO 对象
     */
    public Result<UserVO> getProfile(String userId) {
        // 直接委托，无日志记录（此操作频率较高，记录日志意义不大且可能影响性能）
        return userFeignClient.getProfile(userId);
    }

    /**
     * 更新用户个人信息
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/user/profile 接口（PUT 方法）。
     *
     * @param userId  当前登录用户的 ID
     * @param updates 要更新的字段 Map（如 nickname、avatar、phone 等）
     * @return Result 包装的空返回体
     */
    public Result<Void> updateProfile(String userId, Map<String, Object> updates) {
        // 记录 INFO 日志：记录操作的用户 ID，便于审计追踪
        log.info("UserService.updateProfile: userId={}", userId);
        // 委托 Feign 客户端发起更新请求
        return userFeignClient.updateProfile(userId, updates);
    }

    /**
     * 修改用户密码
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/user/password 接口（PUT 方法）。
     * 注意：日志中不记录任何密码信息，以保障安全性。
     *
     * @param userId    当前登录用户的 ID
     * @param passwords 包含 oldPassword 和 newPassword 的 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> updatePassword(String userId, Map<String, String> passwords) {
        // 记录 INFO 日志：仅记录操作和用户 ID，不记录密码明文
        log.info("UserService.updatePassword: userId={}", userId);
        // 委托 Feign 客户端发起密码修改请求
        return userFeignClient.updatePassword(userId, passwords);
    }

    // ===== 以下为管理后台专用方法 =====

    /**
     * 管理后台 — 获取用户分页列表
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/user/admin/list 接口。
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @param keyword  搜索关键词（可选）
     * @return Result 包装的分页用户数据 Map
     */
    public Result<Map<String, Object>> adminUserList(int pageNum, int pageSize, String keyword) {
        // 直接委托 Feign 客户端，管理后台查询操作可通过 AOP 统一记录日志
        return userFeignClient.adminUserList(pageNum, pageSize, keyword);
    }

    /**
     * 管理后台 — 根据 ID 获取用户详情
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/user/{id} 接口。
     *
     * @param id 用户主键 ID
     * @return Result 包装的 UserVO 对象
     */
    public Result<UserVO> getUserById(Long id) {
        return userFeignClient.getUserById(id);
    }

    /**
     * 管理后台 — 创建新用户
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/user/admin 接口（POST 方法）。
     *
     * @param userData 用户数据 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> createUser(Map<String, Object> userData) {
        return userFeignClient.createUser(userData);
    }

    /**
     * 管理后台 — 更新用户信息
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/user/admin/{id} 接口（PUT 方法）。
     *
     * @param id       用户主键 ID
     * @param userData 要更新的字段 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> updateUser(Long id, Map<String, Object> userData) {
        return userFeignClient.updateUser(id, userData);
    }

    /**
     * 管理后台 — 删除用户
     * <p>
     * 通过 Feign 调用 bookstore-user 微服务的 /api/user/admin/{id} 接口（DELETE 方法）。
     *
     * @param id 用户主键 ID
     * @return Result 包装的空返回体
     */
    public Result<Void> deleteUser(Long id) {
        return userFeignClient.deleteUser(id);
    }
}
