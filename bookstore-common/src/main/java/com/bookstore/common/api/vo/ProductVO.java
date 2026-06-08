package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import com.fasterxml.jackson.annotation.JsonIgnore;  // 导入Jackson的@JsonIgnore注解，用于忽略字段的序列化
import com.fasterxml.jackson.annotation.JsonProperty;  // 导入Jackson的@JsonProperty注解，用于自定义JSON字段名
import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格
import java.util.List;  // 导入List集合类

/**
 * 商品视图对象（VO）
 * 用于向前端返回商品的完整信息
 * 包含商品基本信息、SKU列表和规格列表
 * 使用@JsonProperty注解实现Java字段名与JSON字段名的映射
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class ProductVO {  // 商品视图对象类

    @JsonProperty("productid")  // Jackson注解，序列化时将"id"映射为JSON中的"productid"
    private String id;  // 商品ID

    private String name;  // 商品名称
    @JsonProperty("descn")  // Jackson注解，序列化时将"description"映射为JSON中的"descn"
    private String description;  // 商品描述
    private String author;  // 作者（适用于图书类商品）
    private String category;  // 商品分类ID
    private BigDecimal price;  // 商品价格
    @JsonProperty("image")  // Jackson注解，序列化时将"imageUrl"映射为JSON中的"image"
    private String imageUrl;  // 商品图片URL地址
    private Integer stock;  // 商品库存数量
    private Integer sales;  // 商品销量
    @JsonProperty("is_recommend")  // Jackson注解，序列化时将"isRecommend"映射为JSON中的"is_recommend"
    private Integer isRecommend;  // 是否推荐：0-不推荐 1-推荐
    private Integer status;  // 商品状态：0-下架 1-上架
    private List<ProductSkuVO> skus;  // 商品SKU列表，包含不同规格的价格和库存
    private List<ProductSpecVO> specs;  // 商品规格列表，包含规格名称和可选值

    // ========== 兼容方法，用于MyBatis-Plus等框架的数据映射 ==========

    @JsonIgnore  // Jackson注解，序列化时忽略此方法，避免JSON中出现重复字段
    public String getProductid() { return id; }  // 获取商品ID，兼容数据库字段名

    /**
     * 设置商品ID
     * @param productid 商品ID
     */
    public void setProductid(String productid) { this.id = productid; }  // 设置商品ID，兼容数据库字段名

    @JsonIgnore  // Jackson注解，序列化时忽略此方法
    public String getDescn() { return description; }  // 获取商品描述，兼容数据库字段名

    /**
     * 设置商品描述
     * @param descn 商品描述
     */
    public void setDescn(String descn) { this.description = descn; }  // 设置商品描述，兼容数据库字段名

    @JsonIgnore  // Jackson注解，序列化时忽略此方法
    public String getImage() { return imageUrl; }  // 获取图片URL，兼容数据库字段名

    /**
     * 设置图片URL
     * @param image 图片URL
     */
    public void setImage(String image) { this.imageUrl = image; }  // 设置图片URL，兼容数据库字段名
}
