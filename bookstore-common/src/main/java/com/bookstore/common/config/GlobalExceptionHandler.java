package com.bookstore.common.config;  // 声明当前类所属的包路径

import com.bookstore.common.api.Result;  // 导入统一返回结果类
import com.bookstore.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;  // 导入约束违反异常
import org.slf4j.Logger;  // 导入日志接口
import org.slf4j.LoggerFactory;  // 导入日志工厂类
import org.springframework.http.HttpStatus;  // 导入HTTP状态码枚举
import org.springframework.validation.BindException;  // 导入参数绑定异常
import org.springframework.web.bind.MethodArgumentNotValidException;  // 导入方法参数校验异常
import org.springframework.web.bind.annotation.ExceptionHandler;  // 导入异常处理器注解
import org.springframework.web.bind.annotation.ResponseStatus;  // 导入响应状态码注解
import org.springframework.web.bind.annotation.RestControllerAdvice;  // 导入REST控制器增强注解

/**
 * 全局异常处理器
 * 统一处理Controller层抛出的异常，返回统一格式的错误响应
 * 使用@RestControllerAdvice注解，自动拦截所有Controller的异常
 */
@RestControllerAdvice  // Spring注解，用于全局处理Controller层的异常
public class GlobalExceptionHandler {  // 全局异常处理器类

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);  // 创建日志记录器

    /**
     * 处理方法参数校验异常（@Valid注解触发）
     * @param e 方法参数校验异常
     * @return 统一错误结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)  // 指定处理MethodArgumentNotValidException异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 设置HTTP响应状态码为400（请求参数错误）
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {  // 处理参数校验异常方法
        String message = e.getBindingResult().getFieldErrors().stream()  // 获取所有字段错误并转换为流
                .map(error -> error.getField() + ": " + error.getDefaultMessage())  // 将每个错误转换为"字段名: 错误信息"格式
                .findFirst()  // 获取第一个错误
                .orElse("参数校验失败");  // 如果没有错误则返回默认消息
        return Result.error(400, message);  // 返回400错误结果
    }

    /**
     * 处理参数绑定异常
     * @param e 参数绑定异常
     * @return 统一错误结果
     */
    @ExceptionHandler(BindException.class)  // 指定处理BindException异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 设置HTTP响应状态码为400
    public Result<Void> handleBindException(BindException e) {  // 处理参数绑定异常方法
        String message = e.getFieldErrors().stream()  // 获取所有字段错误并转换为流
                .map(error -> error.getField() + ": " + error.getDefaultMessage())  // 将每个错误转换为"字段名: 错误信息"格式
                .findFirst()  // 获取第一个错误
                .orElse("参数绑定失败");  // 如果没有错误则返回默认消息
        return Result.error(400, message);  // 返回400错误结果
    }

    /**
     * 处理约束违反异常
     * @param e 约束违反异常
     * @return 统一错误结果
     */
    @ExceptionHandler(ConstraintViolationException.class)  // 指定处理ConstraintViolationException异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 设置HTTP响应状态码为400
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {  // 处理约束违反异常方法
        return Result.error(400, e.getMessage());  // 返回400错误结果，包含异常信息
    }

    /**
     * 处理非法参数异常
     * @param e 非法参数异常
     * @return 统一错误结果
     */
    @ExceptionHandler(IllegalArgumentException.class)  // 指定处理IllegalArgumentException异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 设置HTTP响应状态码为400
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {  // 处理非法参数异常方法
        return Result.error(400, e.getMessage());  // 返回400错误结果，包含异常信息
    }

    /**
     * 处理业务异常 — 替代原有 catch(Exception ignored) {} 静默吞异常模式
     * @param e 业务异常
     * @return 统一错误结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理所有未捕获的异常（兜底处理）
     * @param e 异常对象
     * @return 统一错误结果
     */
    @ExceptionHandler(Exception.class)  // 指定处理所有Exception异常（兜底）
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 设置HTTP响应状态码为500（服务器内部错误）
    public Result<Void> handleException(Exception e) {  // 处理通用异常方法
        log.error("服务器异常: ", e);  // 记录错误日志，包含完整异常堆栈
        return Result.error(500, "服务器内部错误: " + e.getMessage());  // 返回500错误结果
    }
}
