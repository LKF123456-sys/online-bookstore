package com.bookstore.common.exception;  // 声明包路径：属于异常类的单元测试，与 BusinessException 源码同包

import org.junit.jupiter.api.DisplayName;  // 导入 JUnit 5 的 @DisplayName 注解，为测试方法提供可读的中文描述
import org.junit.jupiter.api.Test;  // 导入 JUnit 5 的 @Test 注解，标识方法为测试用例

import static org.junit.jupiter.api.Assertions.*;  // 静态导入 JUnit 5 所有断言方法（如 assertEquals、assertInstanceOf 等）

/**
 * BusinessException 业务异常类的单元测试
 * 验证异常对象的构造行为：默认错误码、自定义错误码、继承关系
 */
class BusinessExceptionTest {  // 测试类（默认访问级别，符合 JUnit 5 惯例）

    /**
     * 测试场景：使用单参数构造器创建异常，验证默认错误码为 500
     * 预期结果：getCode() 返回 500，getMessage() 返回传入的消息文本
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("创建业务异常 — 默认500错误码")  // JUnit 5 注解：为测试提供中文描述，便于在测试报告中阅读
    void shouldCreateWithDefaultCode() {  // 测试方法：验证默认错误码构造行为
        BusinessException ex = new BusinessException("库存不足");  // 使用单参数构造器创建异常实例，仅传入消息
        assertEquals(500, ex.getCode());  // 断言：错误码应为默认值 500
        assertEquals("库存不足", ex.getMessage());  // 断言：消息应与传入的字符串一致
    }

    /**
     * 测试场景：使用双参数构造器创建异常，验证自定义错误码被正确存储
     * 预期结果：getCode() 返回自定义的 401，getMessage() 返回传入的消息文本
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("创建业务异常 — 自定义错误码")  // JUnit 5 注解：为测试提供中文描述
    void shouldCreateWithCustomCode() {  // 测试方法：验证自定义错误码构造行为
        BusinessException ex = new BusinessException(401, "请先登录");  // 使用双参数构造器创建异常实例，传入自定义错误码 401 和消息
        assertEquals(401, ex.getCode());  // 断言：错误码应为自定义值 401（HTTP 401 Unauthorized）
        assertEquals("请先登录", ex.getMessage());  // 断言：消息应与传入的字符串一致
    }

    /**
     * 测试场景：验证 BusinessException 是 RuntimeException 的子类
     * 预期结果：assertInstanceOf 断言通过，确认继承关系正确
     * 目的：确保异常为非受检异常，调用方无需强制 try-catch
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("BusinessException 是 RuntimeException 的子类")  // JUnit 5 注解：为测试提供中文描述
    void shouldBeRuntimeException() {  // 测试方法：验证继承关系
        BusinessException ex = new BusinessException("test");  // 创建一个 BusinessException 实例
        assertInstanceOf(RuntimeException.class, ex);  // 断言：ex 是 RuntimeException 的实例，验证继承链正确
    }
}
