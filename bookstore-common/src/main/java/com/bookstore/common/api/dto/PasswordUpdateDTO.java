package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import jakarta.validation.constraints.NotBlank;  // 导入非空校验注解
import jakarta.validation.constraints.Size;  // 导入长度校验注解
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 密码更新数据传输对象（DTO）
 * 用于接收用户修改密码时提交的信息
 * 需要提供原密码和新密码
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class PasswordUpdateDTO {  // 密码更新DTO类

    @NotBlank(message = "原密码不能为空")  // 非空校验，原密码不能为null或空字符串
    private String oldPassword;  // 原密码，用于验证用户身份

    @NotBlank(message = "新密码不能为空")  // 非空校验，新密码不能为null或空字符串
    @Size(min = 6, max = 20, message = "密码长度6-20位")  // 长度校验，新密码必须在6-20个字符之间
    private String newPassword;  // 新密码，将进行BCrypt加密后存储
}
