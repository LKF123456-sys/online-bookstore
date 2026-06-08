package com.bookstore.common.exception;

/**
 * 业务异常 — 用于传递业务层面的错误信息给全局异常处理器
 * 替代原有的 catch(Exception ignored) {} 静默吞异常模式
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
