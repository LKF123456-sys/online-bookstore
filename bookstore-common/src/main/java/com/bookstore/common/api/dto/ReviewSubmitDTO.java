package com.bookstore.common.api.dto;  // 声明当前类所属的包路径

import jakarta.validation.constraints.Max;  // 导入最大值校验注解
import jakarta.validation.constraints.Min;  // 导入最小值校验注解
import jakarta.validation.constraints.NotBlank;  // 导入非空校验注解
import lombok.Data;  // 导入Lombok的@Data注解

/**
 * 评论提交数据传输对象（DTO）
 * 用于接收用户提交商品评论时的信息
 * 包含商品ID、评分、评论内容和图片
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class ReviewSubmitDTO {  // 评论提交DTO类

    @NotBlank(message = "商品ID不能为空")  // 非空校验，商品ID不能为null或空字符串
    private String productId;  // 要评论的商品ID

    @Min(value = 1, message = "评分最低1星")  // 最小值校验，评分不能低于1
    @Max(value = 5, message = "评分最高5星")  // 最大值校验，评分不能高于5
    private Integer rating;  // 评分，1-5星

    @NotBlank(message = "评价内容不能为空")  // 非空校验，评论内容不能为null或空字符串
    private String content;  // 评论文字内容

    private String image;  // 评论图片URL地址，可选
}
