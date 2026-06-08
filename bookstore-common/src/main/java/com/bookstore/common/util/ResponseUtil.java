package com.bookstore.common.util;  // 声明当前类所属的包路径

import com.bookstore.common.api.Result;  // 导入统一返回结果类
import com.fasterxml.jackson.databind.ObjectMapper;  // 导入Jackson的JSON处理工具
import jakarta.servlet.http.HttpServletResponse;  // 导入HTTP响应对象
import java.io.IOException;  // 导入IO异常类

/**
 * 响应工具类
 * 提供向HTTP响应中写入JSON数据的便捷方法
 * 主要用于过滤器（Filter）中返回JSON格式的响应
 */
public class ResponseUtil {  // 响应工具类

    private static final ObjectMapper objectMapper = new ObjectMapper();  // 创建JSON序列化工具实例

    /**
     * 向HTTP响应中写入JSON格式的Result对象
     * @param response HTTP响应对象
     * @param result 统一返回结果对象
     * @throws IOException IO异常
     */
    public static void writeJson(HttpServletResponse response, Result<?> result) throws IOException {  // 写入JSON方法
        response.setContentType("application/json;charset=UTF-8");  // 设置响应内容类型为JSON，字符编码为UTF-8
        response.setStatus(result.getCode());  // 设置HTTP状态码
        objectMapper.writeValue(response.getOutputStream(), result);  // 将Result对象序列化为JSON并写入响应输出流
    }

    /**
     * 向HTTP响应中写入成功结果
     * @param response HTTP响应对象
     * @param data 成功时返回的数据
     * @throws IOException IO异常
     */
    public static void writeSuccess(HttpServletResponse response, Object data) throws IOException {  // 写入成功结果方法
        writeJson(response, Result.success(data));  // 调用writeJson写入成功结果
    }

    /**
     * 向HTTP响应中写入错误结果
     * @param response HTTP响应对象
     * @param code 错误码
     * @param message 错误信息
     * @throws IOException IO异常
     */
    public static void writeError(HttpServletResponse response, int code, String message) throws IOException {  // 写入错误结果方法
        writeJson(response, Result.error(code, message));  // 调用writeJson写入错误结果
    }
}
