package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import java.time.LocalDateTime;  // 导入Java8日期时间类

import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 用户视图对象（VO）
 * 用于向前端返回用户信息，不包含密码等敏感字段
 * VO（View Object）是专门用于展示层的对象
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class UserVO {  // 用户视图对象类

    private String userid;  // 用户ID
    private String email;  // 用户邮箱
    private String firstname;  // 用户名字（名）
    private String lastname;  // 用户姓氏（姓）
    private String phone;  // 联系电话
    private String avatar;  // 用户头像URL地址
    private String role;  // 用户角色：admin-管理员 user-普通用户
    private Integer status;  // 用户状态：0-禁用 1-启用
    private String addr1;  // 收货地址第一行
    private String addr2;  // 收货地址第二行
    private String city;  // 所在城市
    private String state;  // 所在州/省
    private String zip;  // 邮政编码
    private String country;  // 所在国家
    private LocalDateTime createdAt;  // 账号创建时间
}
