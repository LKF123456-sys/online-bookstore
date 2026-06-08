package com.bookstore.common.config;  // 声明当前类所属的包路径：bookstore-common 通用模块的配置子包

import com.bookstore.common.api.Result;  // 导入统一返回结果类，用于构建规范的 JSON 错误响应体
import com.bookstore.common.exception.BusinessException;  // 导入自定义业务异常类，用于捕获业务层抛出的异常
import jakarta.validation.ConstraintViolationException;  // 导入 Jakarta Bean Validation 约束违反异常（如 @NotNull、@Size 等校验失败时抛出）
import org.slf4j.Logger;  // 导入 SLF4J 日志接口，用于记录异常日志
import org.slf4j.LoggerFactory;  // 导入 SLF4J 日志工厂类，用于创建 Logger 实例
import org.springframework.http.HttpStatus;  // 导入 Spring HTTP 状态码枚举（如 BAD_REQUEST=400、INTERNAL_SERVER_ERROR=500）
import org.springframework.validation.BindException;  // 导入 Spring 数据绑定异常（表单/查询参数绑定到对象失败时抛出）
import org.springframework.web.bind.MethodArgumentNotValidException;  // 导入 Spring 方法参数校验异常（@Valid + @RequestBody 校验失败时抛出）
import org.springframework.web.bind.annotation.ExceptionHandler;  // 导入 Spring MVC 异常处理器注解，用于声明方法处理的异常类型
import org.springframework.web.bind.annotation.ResponseStatus;  // 导入 Spring MVC 响应状态码注解，用于设置 HTTP 响应的状态码
import org.springframework.web.bind.annotation.RestControllerAdvice;  // 导入 Spring MVC REST 控制器增强注解，用于全局拦截 Controller 层异常

/**
 * 全局异常处理器 — 统一处理 Controller 层抛出的所有异常
 * 
 * @RestControllerAdvice 注解说明：
 *   该注解是 @ControllerAdvice + @ResponseBody 的组合：
 *   1. @ControllerAdvice   — 声明该类为全局控制器增强器，自动拦截所有 @Controller / @RestController 中抛出的异常
 *   2. @ResponseBody        — 方法返回值自动序列化为 JSON 写入 HTTP 响应体（而非视图名），适用于 RESTful API
 *   与 @ControllerAdvice 的区别：后者默认返回视图名，需要额外加 @ResponseBody 才能返回 JSON
 * 
 * HTTP 状态码映射逻辑：
 *   本类通过 @ResponseStatus 注解将不同类型的异常映射到对应的 HTTP 状态码：
 *   - MethodArgumentNotValidException → 400 BAD_REQUEST（@Valid 请求体校验失败）
 *   - BindException                     → 400 BAD_REQUEST（表单/Query 参数绑定失败）
 *   - ConstraintViolationException      → 400 BAD_REQUEST（方法级参数校验失败）
 *   - IllegalArgumentException          → 400 BAD_REQUEST（非法参数）
 *   - BusinessException                 → 使用异常自带的 code（由 BusinessException.getCode() 决定，无 @ResponseStatus）
 *   - Exception（兜底）                 → 500 INTERNAL_SERVER_ERROR（未预期的服务器内部错误）
 * 
 * 异常处理优先级：
 *   Spring 按 @ExceptionHandler 的异常类型从具体到宽泛进行匹配，
 *   即 BusinessException 异常不会匹配到 Exception handler，除非没有更具体的处理器
 */
@RestControllerAdvice  // Spring 注解：声明为 REST 全局异常处理器，返回 JSON 格式的错误响应
public class GlobalExceptionHandler {  // 全局异常处理器类，所有方法为静态异常处理方法

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);  // 创建 SLF4J 日志记录器实例，用于输出异常日志

    /**
     * 处理方法参数校验异常 — 由 Controller 方法参数上的 @Valid 注解触发
     * 例如：@Valid @RequestBody LoginDTO dto，当 dto 中的 @NotEmpty、@Size 等校验失败时抛出此异常
     * HTTP 状态码：400 BAD_REQUEST — 表示客户端请求参数不符合校验规则
     * @param e   MethodArgumentNotValidException 异常对象，包含所有字段校验失败的详细信息
     * @return    Result<Void> 统一错误响应，code=400，message 为第一个校验失败字段的错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)  // 声明该方法处理 MethodArgumentNotValidException（@Valid 校验异常）
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 设置 HTTP 响应状态码为 400（Bad Request — 请求参数有误）
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {  // 处理方法参数校验异常，返回统一错误结果
        String message = e.getBindingResult().getFieldErrors().stream()  // 获取绑定结果中所有字段错误列表，转换为 Stream 流进行处理
                .map(error -> error.getField() + ": " + error.getDefaultMessage())  // 将每个字段错误映射为 "字段名: 错误描述" 格式的字符串
                .findFirst()  // 获取流中的第一个错误信息（优先返回首个校验失败的字段），返回 Optional<String>
                .orElse("参数校验失败");  // 如果错误列表为空（理论上不会发生），使用默认消息作为兜底
        return Result.error(400, message);  // 构建并返回 code=400 的统一错误响应，消息为第一个校验失败的字段信息
    }

    /**
     * 处理参数绑定异常 — 由表单数据或查询参数绑定到 Java 对象时触发
     * 例如：/api/user?age=abc 绑定到 int age 时类型转换失败抛出此异常
     * HTTP 状态码：400 BAD_REQUEST — 表示客户端传入的参数无法正确绑定到目标类型
     * @param e   BindException 异常对象，包含字段绑定失败的详细信息
     * @return    Result<Void> 统一错误响应，code=400
     */
    @ExceptionHandler(BindException.class)  // 声明该方法处理 BindException（表单/Query 参数绑定异常）
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 设置 HTTP 响应状态码为 400（Bad Request）
    public Result<Void> handleBindException(BindException e) {  // 处理参数绑定异常方法
        String message = e.getFieldErrors().stream()  // 获取所有字段绑定错误列表，转换为 Stream 流进行处理
                .map(error -> error.getField() + ": " + error.getDefaultMessage())  // 将每个字段错误映射为 "字段名: 错误描述" 格式的字符串
                .findFirst()  // 获取流中的第一个错误信息
                .orElse("参数绑定失败");  // 如果错误列表为空，使用默认消息
        return Result.error(400, message);  // 构建并返回 code=400 的统一错误响应
    }

    /**
     * 处理约束违反异常 — 由方法级别 @Validated 或参数上的校验注解触发
     * 例如：@GetMapping("/users") public Result list(@RequestParam @Min(1) int page) 中 page < 1 时抛出
     * 与 MethodArgumentNotValidException 的区别：前者用于 @RequestBody 校验，后者用于方法参数校验
     * HTTP 状态码：400 BAD_REQUEST
     * @param e   ConstraintViolationException 异常对象
     * @return    Result<Void> 统一错误响应，code=400
     */
    @ExceptionHandler(ConstraintViolationException.class)  // 声明该方法处理 ConstraintViolationException（方法参数校验异常）
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 设置 HTTP 响应状态码为 400（Bad Request）
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {  // 处理约束违反异常方法
        return Result.error(400, e.getMessage());  // 直接使用异常自带的消息文本构建 code=400 的错误响应
    }

    /**
     * 处理非法参数异常 — 由代码中显式 throw new IllegalArgumentException("...") 触发
     * 例如：if (page < 0) throw new IllegalArgumentException("页码不能为负数")
     * HTTP 状态码：400 BAD_REQUEST — 表示参数值不合逻辑
     * @param e   IllegalArgumentException 异常对象
     * @return    Result<Void> 统一错误响应，code=400
     */
    @ExceptionHandler(IllegalArgumentException.class)  // 声明该方法处理 IllegalArgumentException（非法参数异常）
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 设置 HTTP 响应状态码为 400（Bad Request）
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {  // 处理非法参数异常方法
        return Result.error(400, e.getMessage());  // 使用异常自带的消息文本构建 code=400 的错误响应
    }

    /**
     * 处理业务异常 — 捕获业务层抛出的 BusinessException
     * 这是核心处理方法，替代原有 catch(Exception ignored) {} 静默吞异常的反模式
     * 
     * @ResponseStatus 说明：此处未使用 @ResponseStatus 注解，HTTP 响应状态码仍为 200（OK），
     *   但响应体中的 code 字段由异常自带（如 401、404、500），前端根据响应的 code 字段判断业务状态，
     *   而非 HTTP 状态码。这种设计允许在 HTTP 层面通过反向代理/负载均衡，
     *   而在业务层面通过 JSON 响应的 code 字段区分不同错误类型。
     * 
     * HTTP 状态码映射逻辑（无 @ResponseStatus，使用默认 200）：
     *   - Controller 正常返回时 HTTP 状态码 = 200
     *   - 前端根据 JSON 中 code 字段判断业务状态
     * 
     * @param e   BusinessException 业务异常对象，包含错误码和消息
     * @return    Result<Void> 统一错误响应，code 为异常中携带的业务错误码
     */
    @ExceptionHandler(BusinessException.class)  // 声明该方法处理 BusinessException（自定义业务异常）
    public Result<Void> handleBusinessException(BusinessException e) {  // 处理业务异常方法
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage());  // 记录错误日志，包含错误码和消息便于排查
        return Result.error(e.getCode(), e.getMessage());  // 使用异常自带的错误码和消息构建统一错误响应
    }

    /**
     * 兜底异常处理 — 捕获所有未在前面被专门处理的异常
     * 作为最后一道防线，确保即使出现未预期的异常也能返回规范的 JSON 错误响应，而非 Tomcat 默认的 HTML 错误页
     * 
     * HTTP 状态码映射逻辑：
     *   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) → HTTP 500
     *   同时响应体 JSON 中 code 也设为 500，前端可从 HTTP 状态码或 JSON code 任一方式判断
     * 
     * @param e   Exception 异常对象（任何未被前面 handler 捕获的异常）
     * @return    Result<Void> 统一错误响应，code=500
     */
    @ExceptionHandler(Exception.class)  // 声明该方法处理所有 Exception 类型异常（作为兜底处理器，优先级最低）
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 设置 HTTP 响应状态码为 500（Internal Server Error — 服务器内部错误）
    public Result<Void> handleException(Exception e) {  // 通用异常处理方法，兜底处理所有未捕获异常
        log.error("服务器异常: ", e);  // 记录完整的异常堆栈到日志，便于开发者定位问题（使用两个参数格式，第二个参数为异常对象）
        return Result.error(500, "服务器内部错误: " + e.getMessage());  // 构建并返回 code=500 的统一错误响应，附带异常消息
    }
}
