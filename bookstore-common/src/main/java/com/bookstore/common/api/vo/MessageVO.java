package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.time.LocalDateTime;  // 导入Java8日期时间类

/**
 * 消息视图对象（VO）
 * 用于向前端返回消息的详细信息
 * 包含发送者、接收者、消息内容和已读状态
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class MessageVO {  // 消息视图对象类

    private Long id;  // 消息ID
    private String senderId;  // 发送者ID
    private String senderType;  // 发送者类型：user-用户 admin-管理员
    private String receiverId;  // 接收者ID
    private String receiverType;  // 接收者类型：user-用户 admin-管理员
    private String content;  // 消息内容
    private Integer readStatus;  // 已读状态：0-未读 1-已读
    private LocalDateTime createTime;  // 消息创建时间
}
