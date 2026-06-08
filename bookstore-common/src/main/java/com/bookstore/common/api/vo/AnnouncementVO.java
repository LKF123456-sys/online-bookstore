package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 公告视图对象（VO）
 * 用于向前端返回系统公告的详细信息
 * 包含公告标题、内容、状态和创建时间
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class AnnouncementVO {  // 公告视图对象类

    private Long id;  // 公告ID
    private String title;  // 公告标题
    private String content;  // 公告内容
    private Integer status;  // 公告状态：0-草稿 1-已发布
    private LocalDateTime createdAt;  // 公告创建时间
}
