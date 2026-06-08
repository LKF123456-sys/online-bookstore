package com.bookstore.product.document;  // 声明当前类所在的包路径，属于商品服务的ES文档对象

import lombok.Data;  // 导入Lombok的Data注解，自动生成getter、setter、toString、equals、hashCode方法
import org.springframework.data.annotation.Id;  // 导入Spring Data的@Id注解，标记文档的唯一标识字段
import org.springframework.data.elasticsearch.annotations.Document;  // 导入ES的@Document注解，指定文档对应的索引名称
import org.springframework.data.elasticsearch.annotations.Field;  // 导入ES的@Field注解，定义字段的类型和分词器
import org.springframework.data.elasticsearch.annotations.FieldType;  // 导入ES字段类型枚举

import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示价格（避免浮点数精度问题）

/**
 * 商品ES文档对象
 * 对应Elasticsearch中的 "product" 索引
 *
 * 与数据库中的Product实体不同，这个类专门用于ES搜索
 * ES中的字段类型和数据库字段类型可能不同，例如：
 *   - 数据库中的name是VARCHAR，ES中是Text（支持分词搜索）
 *   - 数据库中的price是DECIMAL，ES中是Double
 *
 * 字段类型说明：
 *   - Text：文本类型，会被分词器处理，适合做全文搜索
 *   - Keyword：关键词类型，不分词，适合精确匹配和聚合
 *   - Double/Integer：数值类型，适合范围查询和排序
 */
@Data  // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@Document(indexName = "product")  // 指定该文档对应的ES索引名称为 "product"
public class ProductDocument {  // 商品ES文档类

    @Id  // 标记为文档的唯一标识（ES中的文档ID）
    private String id;  // 商品ID，作为ES文档的唯一标识

    @Field(type = FieldType.Text, analyzer = "standard")  // 定义为Text类型，使用标准分词器，支持全文搜索
    private String name;  // 商品名称，会被分词处理，用户搜索时可以模糊匹配

    @Field(type = FieldType.Text, analyzer = "standard")  // 定义为Text类型，使用标准分词器
    private String description;  // 商品描述，也会被分词，支持在描述内容中搜索

    @Field(type = FieldType.Keyword)  // 定义为Keyword类型，不分词，适合精确匹配
    private String category;  // 商品分类，使用Keyword类型因为分类不需要分词（精确匹配"小说"而不是匹配"小"或"说"）

    @Field(type = FieldType.Double)  // 定义为Double类型，支持数值范围查询
    private BigDecimal price;  // 商品价格，支持按价格范围搜索

    @Field(type = FieldType.Keyword)  // 定义为Keyword类型，不分词
    private String image;  // 商品图片URL，使用Keyword类型存储完整URL

    @Field(type = FieldType.Integer)  // 定义为Integer类型
    private Integer sales;  // 商品销量，支持按销量排序

    @Field(type = FieldType.Integer)  // 定义为Integer类型
    private Integer stock;  // 商品库存数量
}
