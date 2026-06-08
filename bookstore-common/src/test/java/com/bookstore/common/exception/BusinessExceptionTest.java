package com.bookstore.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    @DisplayName("创建业务异常 — 默认500错误码")
    void shouldCreateWithDefaultCode() {
        BusinessException ex = new BusinessException("库存不足");
        assertEquals(500, ex.getCode());
        assertEquals("库存不足", ex.getMessage());
    }

    @Test
    @DisplayName("创建业务异常 — 自定义错误码")
    void shouldCreateWithCustomCode() {
        BusinessException ex = new BusinessException(401, "请先登录");
        assertEquals(401, ex.getCode());
        assertEquals("请先登录", ex.getMessage());
    }

    @Test
    @DisplayName("BusinessException 是 RuntimeException 的子类")
    void shouldBeRuntimeException() {
        BusinessException ex = new BusinessException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
