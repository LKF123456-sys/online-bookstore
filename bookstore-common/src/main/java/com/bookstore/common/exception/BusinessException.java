package com.bookstore.common.exception;  // 声明包路径：属于 bookstore-common 通用模块的异常子包

/**
 * 业务异常类 — 用于在业务层面传递错误信息给全局异常处理器（GlobalExceptionHandler）
 * 继承自 RuntimeException，属于非受检异常，无需在方法签名中显式声明 throws
 * 
 * 设计目的：替代原有 catch(Exception ignored) {} 静默吞异常的反模式，
 * 确保业务错误能被统一捕获并转换为规范的 JSON 错误响应返回给前端
 * 
 * 字段说明：
 *   code    — 业务错误码（int），如 401 未登录、404 资源不存在、500 服务器错误
 *   message — 异常描述信息，继承自父类 Throwable，通过 getMessage() 获取
 * 
 * 构造器说明：
 *   BusinessException(String)       — 仅传消息，错误码默认 500（对应 HTTP 500 Internal Server Error）
 *   BusinessException(int, String)  — 同时指定错误码和消息，用于自定义业务错误码场景
 * 
 * super() 初始化说明：
 *   两个构造器均调用 super(message)，将消息传递给 RuntimeException → Throwable，
 *   最终存入 Throwable.detailMessage 字段，后续可通过 getMessage() 获取
 */
public class BusinessException extends RuntimeException {  // 继承 RuntimeException：非受检异常，调用方无需 try-catch 或声明 throws

    private final int code;  // 业务错误码，final 修饰表示一旦在构造器中赋值后不可更改

    /**
     * 单参数构造器：只传异常消息，错误码默认为 500（服务器内部错误）
     * 适用场景：无需区分具体错误码的通用业务异常
     * @param message 业务异常描述信息，会通过 super(message) 传递给父类 RuntimeException
     */
    public BusinessException(String message) {  // 构造器签名：仅接收异常消息字符串
        super(message);  // 调用父类 RuntimeException(String) 构造器，将 message 存入 Throwable.detailMessage
        this.code = 500;  // 未显式指定错误码时默认设为 500，表示服务器内部错误
    }

    /**
     * 双参数构造器：同时指定错误码和异常消息
     * 适用场景：需要区分不同业务错误类型的场景，如 401 未授权、404 未找到
     * @param code    业务错误码，如 401 表示未授权、404 表示资源未找到
     * @param message 业务异常描述信息
     */
    public BusinessException(int code, String message) {  // 构造器签名：同时接收自定义错误码和消息
        super(message);  // 调用父类 RuntimeException(String) 构造器，将 message 存入 Throwable.detailMessage
        this.code = code;  // 将传入的自定义错误码赋值给当前实例的 code 字段
    }

    /**
     * 获取业务错误码的 getter 方法
     * 供 GlobalExceptionHandler 在捕获异常后构建 Result.error(code, message) 响应时使用
     * @return int 类型的业务错误码
     */
    public int getCode() {  // getter 方法，返回业务错误码
        return code;  // 返回当前实例存储的错误码值
    }
}
