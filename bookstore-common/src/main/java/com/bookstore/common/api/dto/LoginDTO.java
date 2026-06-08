package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import jakarta.validation.constraints.NotBlank;  // 导入Jakarta验证注解，用于非空校验
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 登录数据传输对象（DTO）
 * 用于接收用户登录时提交的用户名和密码
 * 使用Jakarta Validation进行参数校验
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class LoginDTO {  // 登录DTO类

    @NotBlank(message = "用户名不能为空")  // 非空校验注解，用户名不能为null或空字符串
    private String username;  // 用户名，用于登录

    @NotBlank(message = "密码不能为空")  // 非空校验注解，密码不能为null或空字符串
    private String password;  // 密码，明文传输，后端会进行加密比对
}
