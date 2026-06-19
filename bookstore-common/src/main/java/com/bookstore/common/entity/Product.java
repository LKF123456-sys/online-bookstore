package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.IdType;  // 导入主键类型枚举
import com.baomidou.mybatisplus.annotation.TableField;  // 导入字段映射注解
import com.baomidou.mybatisplus.annotation.TableId;  // 导入主键注解
import com.baomidou.mybatisplus.annotation.TableName;  // 导入表名注解
import lombok.Data;  // 导入Lombok的@Data注解

import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格等金额
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 商品实体类
 * 对应数据库中的 product 表，存储商品的基本信息
 * 包含商品名称、价格、库存、销量等核心属性
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("product")  // MyBatis-Plus注解，指定对应的数据库表名为"product"
public class Product {  // 商品实体类

    @TableId(value = "productid", type = IdType.INPUT)  // 主键注解，指定主键字段为"productid"，由用户手动设置
    private String productid;  // 商品ID，字符串类型，作为商品的唯一标识

    private String category;  // 商品分类ID，关联分类表
    private String name;  // 商品名称
    private String descn;  // 商品描述信息（description的缩写）
    private String author;  // 作者（适用于图书类商品）
    private BigDecimal price;  // 商品价格，使用BigDecimal避免浮点数精度问题
    private String image;  // 商品图片URL地址
    private Integer stock;  // 商品库存数量
    private Integer sales;  // 商品销量（已售出数量）
    @TableField("is_recommend")  // 字段映射注解，指定Java字段对应的数据库列名为"is_recommend"
    private Integer isRecommend;  // 是否推荐：0-不推荐 1-推荐，用于首页推荐展示
    private Integer status;  // 商品状态：0-下架 1-上架

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;  // 创建时间，插入时自动填充

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 更新时间，插入和更新时自动填充

    // ========== 前端兼容方法 ==========
    // 以下方法是为了兼容前端传来的字段名与数据库字段名不一致的情况

    /**
     * 获取商品ID（前端兼容方法）
     * @return 商品ID
     */
    public String getId() { return productid; }  // 返回商品ID，供前端使用"getId"调用

    /**
     * 设置商品ID（前端兼容方法）
     * @param id 商品ID
     */
    public void setId(String id) { this.productid = id; }  // 设置商品ID，接收前端传来的"id"字段

    /**
     * 获取商品描述（前端兼容方法）
     * @return 商品描述
     */
    public String getDescription() { return descn; }  // 返回商品描述，供前端使用"getDescription"调用

    /**
     * 设置商品描述（前端兼容方法）
     * @param desc 商品描述
     */
    public void setDescription(String desc) { this.descn = desc; }  // 设置商品描述，接收前端传来的"description"字段

    /**
     * 获取商品图片URL（前端兼容方法）
     * @return 图片URL
     */
    public String getImageUrl() { return image; }  // 返回图片URL，供前端使用"getImageUrl"调用

    /**
     * 设置商品图片URL（前端兼容方法）
     * @param url 图片URL
     */
    public void setImageUrl(String url) { this.image = url; }  // 设置图片URL，接收前端传来的"imageUrl"字段
}
