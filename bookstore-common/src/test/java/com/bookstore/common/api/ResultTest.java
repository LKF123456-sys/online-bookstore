package com.bookstore.common.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    @DisplayName("成功结果 — 带数据")
    void shouldReturnSuccessWithData() {
        Result<String> result = Result.success("hello");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("hello", result.getData());
    }

    @Test
    @DisplayName("成功结果 — 无数据")
    void shouldReturnSuccessWithoutData() {
        Result<Void> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("错误结果 — 自定义错误码")
    void shouldReturnErrorWithCustomCode() {
        Result<Void> result = Result.error(401, "未授权");
        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("错误结果 — 默认500错误码")
    void shouldReturnErrorWithDefaultCode() {
        Result<Void> result = Result.error("服务器错误");
        assertEquals(500, result.getCode());
        assertEquals("服务器错误", result.getMessage());
    }

    @Test
    @DisplayName("Result 无参构造测试")
    void shouldHaveNoArgsConstructor() {
        Result<String> result = new Result<>();
        result.setCode(200);
        result.setMessage("ok");
        result.setData("data");
        assertEquals(200, result.getCode());
        assertEquals("ok", result.getMessage());
        assertEquals("data", result.getData());
    }
}
