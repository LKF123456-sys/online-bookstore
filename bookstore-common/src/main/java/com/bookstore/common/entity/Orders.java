package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示金额
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 订单实体类
 * 对应数据库中的 orders 表，存储订单的完整信息
 * 包括订单金额、账单地址、收货地址、支付方式等
 * 注意：表名使用"orders"而非"order"，因为"order"是SQL关键字
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("orders")  // MyBatis-Plus注解，指定对应的数据库表名为"orders"
public class Orders {  // 订单实体类

    @TableId(value = "orderid", type = IdType.INPUT)  // 主键注解，指定主键字段为"orderid"，由用户手动设置
    private String orderid;  // 订单ID，字符串类型，作为订单的唯一标识

    private String userid;  // 用户ID，关联account表，标识下单用户
    @TableField("orderdate")  // 字段映射注解，指定Java字段对应的数据库列名为"orderdate"
    private LocalDateTime orderdate;  // 下单时间
    @TableField("totalprice")  // 字段映射注解，指定Java字段对应的数据库列名为"totalprice"
    private BigDecimal totalprice;  // 订单实付总价（优惠后）
    @TableField("originalprice")  // 字段映射注解，指定Java字段对应的数据库列名为"originalprice"
    private BigDecimal originalprice;  // 订单原价（优惠前）
    @TableField("discountamount")  // 字段映射注解，指定Java字段对应的数据库列名为"discountamount"
    private BigDecimal discountamount;  // 优惠金额（原价 - 实付价）
    private String couponname;  // 使用的优惠券名称
    private String status;  // 订单状态，如：待支付、已支付、已发货、已完成、已取消等
    private String billtofirstname;  // 账单收件人名字
    private String billtolastname;  // 账单收件人姓氏
    private String billaddr1;  // 账单地址第一行
    private String billaddr2;  // 账单地址第二行
    private String billcity;  // 账单地址城市
    private String billstate;  // 账单地址州/省
    private String billzip;  // 账单地址邮编
    private String billcountry;  // 账单地址国家
    private String shipaddr1;  // 收货地址第一行
    private String shipaddr2;  // 收货地址第二行
    private String shipcity;  // 收货地址城市
    private String shipstate;  // 收货地址州/省
    private String shipzip;  // 收货地址邮编
    private String shipcountry;  // 收货地址国家
    private String shiptofirstname;  // 收货人名字
    private String shiptolastname;  // 收货人姓氏
    private String courier;  // 快递公司名称
    private String creditcard;  // 信用卡号（部分隐藏）
    private String exprdate;  // 信用卡过期日期
    private String cardtype;  // 信用卡类型（如Visa、MasterCard等）
    private String locale;  // 地区设置，用于国际化

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间，插入时自动填充

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间，插入和更新时自动填充
}
