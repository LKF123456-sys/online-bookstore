package com.bookstore.common.api;  // 声明包路径：属于统一返回结果类的单元测试，与 Result 源码同包

import org.junit.jupiter.api.DisplayName;  // 导入 JUnit 5 的 @DisplayName 注解，为测试方法提供可读的中文描述
import org.junit.jupiter.api.Test;  // 导入 JUnit 5 的 @Test 注解，标识方法为测试用例

import static org.junit.jupiter.api.Assertions.*;  // 静态导入 JUnit 5 所有断言方法（assertEquals、assertNull 等）

/**
 * Result 统一返回结果类的单元测试
 * 验证静态工厂方法（success / error）和手动 setter 方式构建的 Result 对象行为
 */
class ResultTest {  // 测试类（默认访问级别，JUnit 5 推荐）

    /**
     * 测试场景：调用 Result.success(data) 创建带数据的成功结果
     * 预期结果：code=200，message="success"，data 为传入的字符串 "hello"
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("成功结果 — 带数据")  // JUnit 5 注解：为测试提供中文描述
    void shouldReturnSuccessWithData() {  // 测试方法：验证带数据的成功结果
        Result<String> result = Result.success("hello");  // 调用静态工厂方法创建成功结果，泛型参数为 String
        assertEquals(200, result.getCode());  // 断言：状态码应为 200（HTTP OK）
        assertEquals("success", result.getMessage());  // 断言：消息应为默认的 "success"
        assertEquals("hello", result.getData());  // 断言：携带的数据应为传入的 "hello"
    }

    /**
     * 测试场景：调用 Result.success() 无参方法创建成功结果
     * 预期结果：code=200，message="success"，data 为 null（无数据）
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("成功结果 — 无数据")  // JUnit 5 注解：为测试提供中文描述
    void shouldReturnSuccessWithoutData() {  // 测试方法：验证无数据的成功结果
        Result<Void> result = Result.success();  // 调用无参静态工厂方法创建成功结果，泛型为 Void
        assertEquals(200, result.getCode());  // 断言：状态码应为 200（HTTP OK）
        assertEquals("success", result.getMessage());  // 断言：消息应为默认的 "success"
        assertNull(result.getData());  // 断言：未传数据时 data 应为 null
    }

    /**
     * 测试场景：调用 Result.error(code, message) 创建自定义错误码的错误结果
     * 预期结果：code=401，message="未授权"，data 为 null
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("错误结果 — 自定义错误码")  // JUnit 5 注解：为测试提供中文描述
    void shouldReturnErrorWithCustomCode() {  // 测试方法：验证自定义错误码的错误结果
        Result<Void> result = Result.error(401, "未授权");  // 调用静态工厂方法创建错误结果，指定错误码 401 和消息
        assertEquals(401, result.getCode());  // 断言：状态码应为传入的自定义值 401（HTTP 401 Unauthorized）
        assertEquals("未授权", result.getMessage());  // 断言：消息应为传入的 "未授权"
        assertNull(result.getData());  // 断言：错误结果不应携带数据
    }

    /**
     * 测试场景：调用 Result.error(message) 单参方法创建错误结果
     * 预期结果：code 使用默认值 500，message 为传入的字符串，data 为 null
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("错误结果 — 默认500错误码")  // JUnit 5 注解：为测试提供中文描述
    void shouldReturnErrorWithDefaultCode() {  // 测试方法：验证默认错误码的错误结果
        Result<Void> result = Result.error("服务器错误");  // 调用单参静态工厂方法创建错误结果，仅传入消息
        assertEquals(500, result.getCode());  // 断言：未指定错误码时应默认为 500（HTTP 500 Internal Server Error）
        assertEquals("服务器错误", result.getMessage());  // 断言：消息应为传入的 "服务器错误"
    }

    /**
     * 测试场景：使用无参构造器 + setter 手动构建 Result 对象
     * 验证 Lombok 生成的 @NoArgsConstructor 和 @Data（setter）功能正常
     * 预期结果：手动 set 的值能通过 getter 正确获取
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("Result 无参构造测试")  // JUnit 5 注解：为测试提供中文描述
    void shouldHaveNoArgsConstructor() {  // 测试方法：验证无参构造和 setter 行为
        Result<String> result = new Result<>();  // 使用无参构造器创建空的 Result 对象（由 Lombok @NoArgsConstructor 生成）
        result.setCode(200);  // 通过 setter 手动设置状态码为 200（由 Lombok @Data 生成）
        result.setMessage("ok");  // 通过 setter 手动设置消息为 "ok"
        result.setData("data");  // 通过 setter 手动设置数据为 "data"
        assertEquals(200, result.getCode());  // 断言：getCode() 应返回 set 的值 200
        assertEquals("ok", result.getMessage());  // 断言：getMessage() 应返回 set 的值 "ok"
        assertEquals("data", result.getData());  // 断言：getData() 应返回 set 的值 "data"
    }
}
