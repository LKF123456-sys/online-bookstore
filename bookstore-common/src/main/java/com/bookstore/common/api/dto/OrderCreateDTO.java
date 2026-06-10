package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示金额
import java.util.List;  // 导入List集合类

/**
 * 订单创建数据传输对象（DTO）
 * 用于接收用户创建订单时提交的完整信息
 * 包括购物车商品、账单地址、收货地址、支付信息和优惠信息
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知字段，允许前端传额外字段（如 billingAddress）
public class OrderCreateDTO {  // 订单创建DTO类

    // ========== 商品来源（二选一） ==========
    private List<String> cartItemIds; // 购物车项ID列表，从购物车下单时使用
    private List<OrderItemDTO> items; // 直接购买的商品列表，不经过购物车直接下单时使用

    // ========== 账单地址信息 ==========
    private String billToFirstName;  // 账单收件人名字
    private String billToLastName;  // 账单收件人姓氏
    private String billAddr1;  // 账单地址第一行（主要地址）
    private String billAddr2;  // 账单地址第二行（补充地址）
    private String billCity;  // 账单地址城市
    private String billState;  // 账单地址州/省
    private String billZip;  // 账单地址邮编
    private String billCountry;  // 账单地址国家

    // ========== 收货地址信息 ==========
    private String shipToFirstName;  // 收货人名字
    private String shipToLastName;  // 收货人姓氏
    private String shipAddr1;  // 收货地址第一行（主要地址）
    private String shipAddr2;  // 收货地址第二行（补充地址）
    private String shipCity;  // 收货地址城市
    private String shipState;  // 收货地址州/省
    private String shipZip;  // 收货地址邮编
    private String shipCountry;  // 收货地址国家

    // ========== 支付信息 ==========
    private String courier;  // 快递公司名称
    private String creditCard;  // 信用卡号
    private String exprDate;  // 信用卡过期日期
    private String cardType;  // 信用卡类型（如Visa、MasterCard等）
    private String locale;  // 地区设置，用于国际化

    // ========== 优惠信息 ==========
    private String couponName;  // 使用的优惠券名称
    private BigDecimal discountAmount;  // 优惠金额
}
