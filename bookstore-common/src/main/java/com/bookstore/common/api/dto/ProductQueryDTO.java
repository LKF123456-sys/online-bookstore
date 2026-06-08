package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 商品查询数据传输对象（DTO）
 * 用于接收前端商品搜索和筛选的条件
 * 支持关键字搜索、分类筛选、价格区间、排序和分页
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class ProductQueryDTO {  // 商品查询DTO类

    private String keyword;  // 搜索关键字，用于模糊匹配商品名称或描述
    private String category;  // 分类ID，用于按分类筛选商品
    private Double minPrice;  // 最低价格，用于价格区间筛选
    private Double maxPrice;  // 最高价格，用于价格区间筛选
    private Integer pageNum = 1;  // 当前页码，默认第1页
    private Integer pageSize = 10;  // 每页显示数量，默认10条
    private String sortBy; // 排序字段：price-按价格 sales-按销量 createTime-按创建时间
    private String sortOrder; // 排序方式：asc-升序 desc-降序
}
