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
}
