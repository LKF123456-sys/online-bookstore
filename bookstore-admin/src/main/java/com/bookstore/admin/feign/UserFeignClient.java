// 声明当前类所属的包路径，遵循 Spring Boot 项目标准分包结构
package com.bookstore.admin.feign;

// 导入公共模块的统一响应封装类 Result，用于包装所有接口返回结果
import com.bookstore.common.api.Result;
// 导入登录请求的数据传输对象 DTO，包含 username 和 password 字段
import com.bookstore.common.api.dto.LoginDTO;
// 导入注册请求的数据传输对象 DTO，包含 username、password、email 等注册信息
import com.bookstore.common.api.dto.RegisterDTO;
// 导入用户信息视图对象 VO，用于返回脱敏后的用户信息
import com.bookstore.common.api.vo.UserVO;
// 导入 Spring Cloud OpenFeign 的 @FeignClient 注解，用于声明式 HTTP 客户端
import org.springframework.cloud.openfeign.FeignClient;
// 导入 Spring MVC 的请求映射注解（GetMapping、PostMapping、PutMapping、DeleteMapping、RequestHeader、PathVariable、RequestParam、RequestBody）
// 这些注解用于描述 HTTP 请求的 URL、方法与参数绑定方式
import org.springframework.web.bind.annotation.*;

// 导入 Java 集合框架的 List 接口
import java.util.List;
// 导入 Java 集合框架的 Map 接口
import java.util.Map;

/**
 * 用户服务 Feign 客户端 — 声明式调用 bookstore-user 微服务
 * 替代原有 RestTemplate + Map<String,Object> 的直调模式
 * <p>
 * 该接口通过 Spring Cloud OpenFeign 以声明方式定义 HTTP 调用，无需手动编写请求代码。
 * Feign 框架会在运行时动态生成代理实现类，自动完成服务发现、负载均衡与 HTTP 请求发送。
 */
// @FeignClient 声明这是一个 Feign 客户端接口
//   - name = "bookstore-user"：指定要调用的微服务名称，对应 Nacos/注册中心中的服务名，Feign 会通过服务发现自动解析为实际 IP:Port
//   - path = "/api"：指定所有方法 URL 的统一路径前缀，即每个方法的完整 URL = /api + 方法上声明的路径
@FeignClient(name = "bookstore-user", path = "/api", fallbackFactory = UserFeignFallbackFactory.class)
public interface UserFeignClient {

    /**
     * 用户登录
     * <p>
     * 调用 bookstore-user 微服务的 /api/auth/login 接口
     * 请求方式：POST
     * 请求体：LoginDTO 对象（JSON 格式，包含 username 和 password）
     * 返回值：Result 统一响应体，data 为 Map 结构，包含 token、用户基本信息等
     *
     * @param dto 登录请求体，包含用户名和密码
     * @return Result 包装的登录结果，成功时 data 中包含 JWT token 与用户简要信息
     */
    // @PostMapping：将 HTTP POST 请求映射到当前方法，路径为 /auth/login，与类级别 path="/api" 拼接后完整路径为 /api/auth/login
    @PostMapping("/auth/login")
    // @RequestBody：将 HTTP 请求体（JSON 格式）自动反序列化为 LoginDTO 对象
    Result<Map<String, Object>> login(@RequestBody LoginDTO dto);

    /**
     * 用户注册
     * <p>
     * 调用 bookstore-user 微服务的 /api/auth/register 接口
     * 请求方式：POST
     * 请求体：RegisterDTO 对象（JSON 格式，包含用户名、密码、邮箱等注册信息）
     *
     * @param dto 注册请求体，包含用户名、密码、邮箱等注册信息
     * @return Result 包装的注册结果，成功时 data 中包含注册成功的用户标识
     */
    // @PostMapping：将 HTTP POST 请求映射到 /auth/register 路径
    @PostMapping("/auth/register")
    // @RequestBody：将 HTTP 请求体反序列化为 RegisterDTO 对象
    Result<Map<String, Object>> register(@RequestBody RegisterDTO dto);

    /**
     * 获取用户信息（个人中心）
     * <p>
     * 调用 bookstore-user 微服务的 /api/user/profile 接口
     * 请求方式：GET
     * 用户身份通过请求头 X-User-Id 传递，而非 URL 路径参数，以增强安全性
     *
     * @param userId 当前登录用户的 ID，通过请求头 X-User-Id 传递给下游微服务
     * @return Result 包装的 UserVO 对象，包含昵称、头像、邮箱等用户信息
     */
    // @GetMapping：将 HTTP GET 请求映射到 /user/profile 路径
    @GetMapping("/user/profile")
    // @RequestHeader("X-User-Id")：将 HTTP 请求头 X-User-Id 的值绑定到方法参数 userId
    // Feign 会自动在发出的 HTTP 请求中添加此请求头
    Result<UserVO> getProfile(@RequestHeader("X-User-Id") String userId);

    /**
     * 更新用户信息（个人中心）
     * <p>
     * 调用 bookstore-user 微服务的 /api/user/profile 接口
     * 请求方式：PUT
     *
     * @param userId  当前登录用户的 ID，通过请求头 X-User-Id 传递
     * @param updates 要更新的用户信息字段，以 Map 键值对形式传递（如 nickname、avatar 等）
     * @return Result 包装的空返回体，表示操作成功或失败
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /user/profile 路径，PUT 语义表示幂等的全量/部分更新
    @PutMapping("/user/profile")
    Result<Void> updateProfile(@RequestHeader("X-User-Id") String userId,
                               // @RequestBody：更新的字段数据通过请求体以 JSON 格式传入
                               @RequestBody Map<String, Object> updates);

    /**
     * 修改密码
     * <p>
     * 调用 bookstore-user 微服务的 /api/user/password 接口
     * 请求方式：PUT
     *
     * @param userId    当前登录用户的 ID，通过请求头 X-User-Id 传递
     * @param passwords 包含旧密码和新密码的 Map 结构（oldPassword、newPassword）
     * @return Result 包装的空返回体
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /user/password 路径
    @PutMapping("/user/password")
    Result<Void> updatePassword(@RequestHeader("X-User-Id") String userId,
                                // @RequestBody：密码数据通过请求体传入（旧密码 + 新密码）
                                @RequestBody Map<String, String> passwords);

    // ===== 以下为管理后台专用接口 =====

    /**
     * 管理后台 — 获取用户分页列表
     * <p>
     * 调用 bookstore-user 微服务的 /api/user/admin/list 接口
     * 请求方式：GET
     * 支持关键词模糊搜索
     *
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 10
     * @param keyword  搜索关键词（可选），用于按用户名/邮箱模糊匹配
     * @return Result 包装的分页数据 Map，包含 records（用户列表）、total（总记录数）等字段
     */
    // @GetMapping：将 HTTP GET 请求映射到 /user/admin/list 路径
    @GetMapping("/user/admin/list")
    Result<Map<String, Object>> adminUserList(
            // @RequestParam：将 URL 查询参数 pageNum 绑定到方法参数，defaultValue="1" 表示如果请求中未传此参数则默认为 1
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：将 URL 查询参数 pageSize 绑定到方法参数，默认每页 10 条
            @RequestParam(defaultValue = "10") int pageSize,
            // @RequestParam(required = false)：keyword 为可选查询参数，如果没有传递则为 null
            @RequestParam(required = false) String keyword);

    /**
     * 管理后台 — 根据 ID 获取单个用户信息
     * <p>
     * 调用 bookstore-user 微服务的 /api/user/{id} 接口
     * 请求方式：GET
     *
     * @param id 用户主键 ID，作为 URL 路径变量
     * @return Result 包装的 UserVO 对象
     */
    // @GetMapping：将 HTTP GET 请求映射到 /user/{id} 路径，{id} 为路径变量占位符
    @GetMapping("/user/{id}")
    // @PathVariable：将 URL 路径中的 {id} 占位符的值绑定到方法参数 id
    Result<UserVO> getUserById(@PathVariable Long id);

    /**
     * 管理后台 — 创建新用户
     * <p>
     * 调用 bookstore-user 微服务的 /api/user/admin 接口
     * 请求方式：POST
     *
     * @param userData 用户数据 Map，包含 username、password、email、role 等字段
     * @return Result 包装的空返回体
     */
    // @PostMapping：将 HTTP POST 请求映射到 /user/admin 路径
    @PostMapping("/user/admin")
    // @RequestBody：用户数据通过请求体以 JSON 格式传入
    Result<Void> createUser(@RequestBody Map<String, Object> userData);

    /**
     * 管理后台 — 更新用户信息
     * <p>
     * 调用 bookstore-user 微服务的 /api/user/admin/{id} 接口
     * 请求方式：PUT
     *
     * @param id       用户主键 ID，作为 URL 路径变量
     * @param userData 要更新的用户数据 Map
     * @return Result 包装的空返回体
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /user/admin/{id} 路径
    @PutMapping("/user/admin/{id}")
    // @PathVariable：将 URL 路径中的 {id} 占位符的值绑定到方法参数 id
    // @RequestBody：更新的用户数据通过请求体传入
    Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData);

    /**
     * 管理后台 — 删除用户
     * <p>
     * 调用 bookstore-user 微服务的 /api/user/admin/{id} 接口
     * 请求方式：DELETE
     *
     * @param id 用户主键 ID，作为 URL 路径变量
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将 HTTP DELETE 请求映射到 /user/admin/{id} 路径，DELETE 语义表示删除资源
    @DeleteMapping("/user/admin/{id}")
    // @PathVariable：将 URL 路径中的 {id} 占位符的值绑定到方法参数 id
    Result<Void> deleteUser(@PathVariable Long id);
}
