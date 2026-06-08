package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包，包含@TableId、@TableName等
import lombok.Data;  // 导入Lombok的@Data注解，自动生成getter/setter/toString等方法
import java.time.LocalDateTime;  // 导入Java8日期时间类，表示不带时区的日期时间

/**
 * 用户账户实体类
 * 对应数据库中的 account 表，存储用户的基本信息和收货地址
 * 使用 @TableName 注解映射数据库表名
 */
@Data  // Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@TableName("account")  // MyBatis-Plus注解，指定该实体类对应的数据库表名为"account"
public class Account {  // 用户账户实体类

    @TableId(value = "userid", type = IdType.INPUT)  // 主键注解，指定主键字段为"userid"，类型为INPUT表示由用户手动设置
    private String userid;  // 用户ID，字符串类型，作为用户的唯一标识

    private String email;  // 用户邮箱，用于登录和接收通知
    private String firstname;  // 用户名字（名）
    private String lastname;  // 用户姓氏（姓）
    private String password;  // 用户密码，存储BCrypt加密后的密文
    private Integer status; // 用户状态：0-禁用 1-启用，控制账号是否可以登录
    private String addr1;  // 收货地址第一行（主要地址）
    private String addr2;  // 收货地址第二行（补充地址，如门牌号等）
    private String city;  // 所在城市
    private String state;  // 所在州/省份
    private String zip;  // 邮政编码
    private String country;  // 所在国家
    private String phone;  // 联系电话
    private String role; // 用户角色：admin-管理员 user-普通用户
    @TableField("created_at")  // 字段映射注解，指定Java字段对应的数据库列名为"created_at"
    private LocalDateTime createdAt;  // 账号创建时间
    private String avatar;  // 用户头像URL地址
}
