package com.bookstore.entity;

public class User {
    // 对应 account 表字段
    private String userid;      // 用户名/账号
    private String password;    // 密码
    private String firstname;   // 名
    private String lastname;    // 姓
    private String email;       // 邮箱
    private String addr1;       // 地址1
    private String city;        // 城市
    private String state;       // 州/省
    private String zip;         // 邮编
    private String phone;       // 电话
    private int status;         // 状态

    // 1. 无参构造
    public User() {}

    // 2. Getter 和 Setter 方法
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddr1() { return addr1; }
    public void setAddr1(String addr1) { this.addr1 = addr1; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}