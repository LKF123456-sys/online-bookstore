package com.bookstore.common.api;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import lombok.NoArgsConstructor;  // 导入Lombok的@NoArgsConstructor注解，生成无参构造方法

/**
 * 统一返回结果包装类（泛型）
 * 用于封装所有API接口的返回结果
 * 包含状态码、提示消息和数据三部分
 * @param <T> 返回数据的类型
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@NoArgsConstructor  // Lombok注解，自动生成无参构造方法
public class Result<T> {  // 统一返回结果类，T为泛型参数

    private int code;  // 状态码，如200表示成功，400表示参数错误，500表示服务器错误
    private String message;  // 提示消息，如"success"或错误描述
    private T data;  // 返回的数据，泛型类型

    /**
     * 返回成功结果（带数据）
     * @param data 成功时返回的数据
     * @param <T> 数据类型
     * @return 成功的Result对象
     */
    public static <T> Result<T> success(T data) {  // 成功结果方法（带数据）
        Result<T> result = new Result<>();  // 创建Result对象
        result.setCode(200);  // 设置状态码为200
        result.setMessage("success");  // 设置提示消息为"success"
        result.setData(data);  // 设置返回数据
        return result;  // 返回Result对象
    }

    /**
     * 返回成功结果（不带数据）
     * @param <T> 数据类型
     * @return 成功的Result对象
     */
    public static <T> Result<T> success() {  // 成功结果方法（不带数据）
        return success(null);  // 调用带参数的success方法，数据为null
    }

    /**
     * 返回错误结果
     * @param code 错误码
     * @param message 错误信息
     * @param <T> 数据类型
     * @return 错误的Result对象
     */
    public static <T> Result<T> error(int code, String message) {  // 错误结果方法（带错误码）
        Result<T> result = new Result<>();  // 创建Result对象
        result.setCode(code);  // 设置错误码
        result.setMessage(message);  // 设置错误信息
        return result;  // 返回Result对象
    }

    /**
     * 返回错误结果（默认500错误码）
     * @param message 错误信息
     * @param <T> 数据类型
     * @return 错误的Result对象
     */
    public static <T> Result<T> error(String message) {  // 错误结果方法（默认500错误码）
        return error(500, message);  // 调用带错误码的error方法，默认500
    }
}
