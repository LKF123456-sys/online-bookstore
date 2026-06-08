package com.bookstore.common.entity;  // 声明当前类所属的包路径

import com.baomidou.mybatisplus.annotation.*;  // 导入MyBatis-Plus注解包
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 商品分类实体类
 * 对应数据库中的 category 表，存储商品分类信息
 * 用于对商品进行分类管理，方便用户浏览和筛选
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
@TableName("category")  // MyBatis-Plus注解，指定对应的数据库表名为"category"
public class Category {  // 商品分类实体类

    @TableId(value = "categoryid", type = IdType.INPUT)  // 主键注解，指定主键字段为"categoryid"，由用户手动设置
    private String categoryid;  // 分类ID，字符串类型，作为分类的唯一标识

    private String categoryname;  // 分类名称，如"文学"、"科技"、"教育"等
    private String categorydesc;  // 分类描述信息，对分类的补充说明

    // ========== 前端兼容方法 ==========
    // 以下方法是为了兼容前端传来的字段名与数据库字段名不一致的情况

    /**
     * 获取分类ID（前端兼容方法）
     * @return 分类ID
     */
    public String getId() { return categoryid; }  // 返回分类ID，供前端使用"getId"调用

    /**
     * 设置分类ID（前端兼容方法）
     * @param id 分类ID
     */
    public void setId(String id) { this.categoryid = id; }  // 设置分类ID，接收前端传来的"id"字段

    /**
     * 获取分类名称（前端兼容方法）
     * @return 分类名称
     */
    public String getName() { return categoryname; }  // 返回分类名称，供前端使用"getName"调用

    /**
     * 设置分类名称（前端兼容方法）
     * @param name 分类名称
     */
    public void setName(String name) { this.categoryname = name; }  // 设置分类名称，接收前端传来的"name"字段
}
