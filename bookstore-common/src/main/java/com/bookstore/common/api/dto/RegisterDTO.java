package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import jakarta.validation.constraints.Email;  // 导入邮箱格式校验注解
import jakarta.validation.constraints.NotBlank;  // 导入非空校验注解
import jakarta.validation.constraints.Pattern;  // 导入正则表达式校验注解
import jakarta.validation.constraints.Size;  // 导入长度校验注解
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 注册数据传输对象（DTO）
 * 用于接收用户注册时提交的信息
 * 包含用户名、密码、邮箱和手机号，并使用Jakarta Validation进行参数校验
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class RegisterDTO {  // 注册DTO类

    @NotBlank(message = "用户名不能为空")  // 非空校验，用户名不能为null或空字符串
    @Size(min = 3, max = 20, message = "用户名长度3-20位")  // 长度校验，用户名必须在3-20个字符之间
    private String username;  // 用户名，用于登录，长度3-20位

    @NotBlank(message = "密码不能为空")  // 非空校验，密码不能为null或空字符串
    @Size(min = 6, max = 20, message = "密码长度6-20位")  // 长度校验，密码必须在6-20个字符之间
    private String password;  // 密码，明文传输，后端会进行BCrypt加密存储

    @NotBlank(message = "邮箱不能为空")  // 非空校验，邮箱不能为null或空字符串
    @Email(message = "邮箱格式不正确")  // 邮箱格式校验，必须符合邮箱格式
    private String email;  // 用户邮箱，用于接收通知和找回密码

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")  // 正则校验，手机号必须为11位且以1开头
    private String phone;  // 用户手机号，可选填
}
