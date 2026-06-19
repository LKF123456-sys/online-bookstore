package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;  // 级联校验：items 列表中的每个 OrderItemDTO 也会被校验
import jakarta.validation.constraints.NotBlank;  // 不能为空白字符串
import jakarta.validation.constraints.NotEmpty;  // 集合不能为空
import jakarta.validation.constraints.Pattern;   // 正则表达式校验
import jakarta.validation.constraints.Size;      // 字符串长度限制
import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示金额
import java.util.List;  // 导入List集合类

/**
 * 订单创建数据传输对象（DTO）
 * 用于接收用户创建订单时提交的完整信息
 * 包括购物车商品、账单地址、收货地址、支付信息和优惠信息
 *
 * 校验策略：
 *   - 收货地址：必填（姓名、地址、城市、国家）
 *   - 支付信息：信用卡号格式校验（仅允许数字，13-19位）
 *   - 商品列表：至少包含一个商品项
 *   - 可选字段：账单地址、优惠券、快递等
 *
 * 面试亮点：
 *   1. @Valid 级联校验：嵌套对象的校验注解也会生效
 *   2. @Pattern 正则校验：信用卡号格式验证
 *   3. 分组校验（可扩展）：不同场景（普通订单 vs 预售订单）使用不同校验规则
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知字段，允许前端传额外字段（如 billingAddress）
public class OrderCreateDTO {  // 订单创建DTO类

    // ========== 商品来源（二选一） ==========
    private List<String> cartItemIds; // 购物车项ID列表，从购物车下单时使用

    @Valid  // 级联校验：列表中的每个 OrderItemDTO 都会执行其内部的校验注解
    @NotEmpty(message = "订单至少包含一个商品")  // 商品列表不能为空
    private List<OrderItemDTO> items; // 直接购买的商品列表，不经过购物车直接下单时使用

    // ========== 收货地址信息（必填） ==========

    @NotBlank(message = "收货人名字不能为空")
    @Size(max = 50, message = "收货人名字最多50个字符")
    private String shipToFirstName;  // 收货人名字

    @NotBlank(message = "收货人姓氏不能为空")
    @Size(max = 50, message = "收货人姓氏最多50个字符")
    private String shipToLastName;  // 收货人姓氏

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址最多200个字符")
    private String shipAddr1;  // 收货地址第一行（主要地址）

    @Size(max = 200, message = "收货地址补充最多200个字符")
    private String shipAddr2;  // 收货地址第二行（补充地址）

    @NotBlank(message = "收货城市不能为空")
    @Size(max = 100, message = "收货城市最多100个字符")
    private String shipCity;  // 收货地址城市

    @Size(max = 100, message = "收货州/省最多100个字符")
    private String shipState;  // 收货地址州/省

    @NotBlank(message = "收货邮编不能为空")
    @Size(max = 20, message = "收货邮编最多20个字符")
    private String shipZip;  // 收货地址邮编

    @NotBlank(message = "收货国家不能为空")
    @Size(max = 100, message = "收货国家最多100个字符")
    private String shipCountry;  // 收货地址国家

    // ========== 账单地址信息（可选，默认同收货地址） ==========
    private String billToFirstName;  // 账单收件人名字
    private String billToLastName;  // 账单收件人姓氏
    private String billAddr1;  // 账单地址第一行（主要地址）
    private String billAddr2;  // 账单地址第二行（补充地址）
    private String billCity;  // 账单地址城市
    private String billState;  // 账单地址州/省
    private String billZip;  // 账单地址邮编
    private String billCountry;  // 账单地址国家

    // ========== 支付信息 ==========
    private String courier;  // 快递公司名称

    @Pattern(regexp = "^[0-9]{13,19}$", message = "信用卡号格式不正确（13-19位数字）")
    private String creditCard;  // 信用卡号（校验后在 Service 层脱敏）

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "信用卡过期日期格式不正确（MM/YY）")
    private String exprDate;  // 信用卡过期日期

    @Size(max = 30, message = "信用卡类型最多30个字符")
    private String cardType;  // 信用卡类型（如Visa、MasterCard等）

    private String locale;  // 地区设置，用于国际化

    // ========== 优惠信息 ==========
    private String couponName;  // 使用的优惠券名称
    private BigDecimal discountAmount;  // 优惠金额
}
