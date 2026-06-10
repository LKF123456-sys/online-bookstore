package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示金额
import java.time.LocalDateTime;  // 导入Java8日期时间类
import java.util.List;  // 导入List集合类

/**
 * 订单视图对象（VO）
 * 用于向前端返回订单的完整信息
 * 包含订单基本信息、账单地址、收货地址、支付信息和订单项列表
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class OrderVO {  // 订单视图对象类

    private String orderid;  // 订单ID
    private String userid;  // 用户ID
    private LocalDateTime orderdate;  // 下单时间
    private BigDecimal totalprice;  // 订单实付总价（优惠后）
    private BigDecimal originalprice;  // 订单原价（优惠前）
    private BigDecimal discountamount;  // 优惠金额
    private String couponname;  // 使用的优惠券名称
    private String status;  // 订单状态

    // ========== 账单地址 ==========
    private String billtofirstname;  // 账单收件人名字
    private String billtolastname;  // 账单收件人姓氏
    private String billaddr1;  // 账单地址第一行
    private String billaddr2;  // 账单地址第二行
    private String billcity;  // 账单地址城市
    private String billstate;  // 账单地址州/省
    private String billzip;  // 账单地址邮编
    private String billcountry;  // 账单地址国家

    // ========== 收货地址 ==========
    private String shipaddr1;  // 收货地址第一行
    private String shipaddr2;  // 收货地址第二行
    private String shipcity;  // 收货地址城市
    private String shipstate;  // 收货地址州/省
    private String shipzip;  // 收货地址邮编
    private String shipcountry;  // 收货地址国家
    private String shiptofirstname;  // 收货人名字
    private String shiptolastname;  // 收货人姓氏

    // ========== 支付信息 ==========
    private String courier;  // 快递公司名称
    private String creditcard;  // 信用卡号
    private String exprdate;  // 信用卡过期日期
    private String cardtype;  // 信用卡类型
    private String locale;  // 地区设置

    // ========== 订单项 ==========
    private List<OrderItemVO> items;  // 订单项列表，包含订单中的所有商品

    // ========== 便捷方法（用于前端展示，不改变存储） ==========
    @com.fasterxml.jackson.annotation.JsonProperty("shippingAddress")  // 序列化为 "shippingAddress"
    public String getShippingAddress() {  // 合并收货地址为单行字符串
        StringBuilder sb = new StringBuilder();  // 创建字符串拼接器
        if (shiptofirstname != null) sb.append(shiptofirstname).append(" ");  // 追加收货人名字
        if (shiptolastname != null) sb.append(shiptolastname).append(" ");  // 追加收货人姓氏
        if (shipaddr1 != null) sb.append(shipaddr1);  // 追加收货地址第一行
        return sb.toString().trim();  // 去除尾部空格后返回
    }

    @com.fasterxml.jackson.annotation.JsonProperty("billingAddress")  // 序列化为 "billingAddress"
    public String getBillingAddress() {  // 合并账单地址为单行字符串
        StringBuilder sb = new StringBuilder();  // 创建字符串拼接器
        if (billtofirstname != null) sb.append(billtofirstname).append(" ");  // 追加账单收件人名字
        if (billtolastname != null) sb.append(billtolastname).append(" ");  // 追加账单收件人姓氏
        if (billaddr1 != null) sb.append(billaddr1);  // 追加账单地址第一行
        return sb.toString().trim();  // 去除尾部空格后返回
    }

    @com.fasterxml.jackson.annotation.JsonProperty("totalAmount")  // 序列化为 "totalAmount"
    public java.math.BigDecimal getTotalAmount() {  // 获取订单总金额的便捷方法
        return this.totalprice;  // 返回订单实付总价
    }
}
