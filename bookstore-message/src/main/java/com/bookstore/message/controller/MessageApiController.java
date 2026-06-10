package com.bookstore.message.controller;  // 声明当前类所属的包路径，属于消息服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一响应结果包装类，用于封装API返回数据
import com.bookstore.common.api.vo.MessageVO;  // 导入消息视图对象，用于返回给前端的消息数据结构
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果包装类，用于封装分页查询的返回数据
import com.bookstore.message.service.MessageService;  // 导入消息业务服务类，处理消息相关的业务逻辑
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含所有final字段的构造函数
import org.springframework.web.bind.annotation.*;  // 导入Spring MVC的Web注解，包含@RestController、@GetMapping等

/**
 * 消息API控制器（面向前端用户）
 * 提供用户消息相关的REST接口
 * 所有接口路径前缀为 /api/message
 *
 * 接口列表：
 *   - GET  /api/message/list           获取当前用户的消息列表（分页）
 *   - PUT  /api/message/{id}/read      标记单条消息为已读
 *   - PUT  /api/message/read-all       标记当前用户所有未读消息为已读
 *   - GET  /api/message/unread-count   获取当前用户的未读消息数量
 */
@RestController  // REST控制器注解，所有方法返回JSON格式数据
@RequestMapping("/api/message")  // URL路径前缀，该控制器下所有接口以 /api/message 开头
@RequiredArgsConstructor  // Lombok注解，自动生成包含所有final字段的构造函数，实现依赖注入
public class MessageApiController {  // 消息API控制器类

    private final MessageService messageService;  // 消息业务服务对象，通过构造函数注入

    /**
     * 获取当前用户的消息列表（分页查询）
     * 根据用户ID查询该用户收到的所有消息，按创建时间倒序排列
     *
     * @param userId   当前登录用户的ID，由网关通过请求属性传递
     * @param pageNum  当前页码，默认为第1页
     * @param pageSize 每页显示的条数，默认为10条
     * @return 分页结果，包含消息列表、总条数、当前页码、每页大小
     */
    @GetMapping("/list")  // GET请求映射，处理 /api/message/list 请求
    public Result<PageResult<MessageVO>> getMessageList(  // 获取消息列表的方法，返回分页的消息VO列表
            @RequestHeader(value = "X-User-Id", required = false) String userId,  // 从请求头中获取用户ID（由网关注入），@RequestHeader用于获取请求头中的属性
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从URL查询参数中获取页码，默认值为1，如 ?pageNum=1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 从URL查询参数中获取每页条数，默认值为10，如 ?pageSize=10
        return Result.success(messageService.getMessageList(userId, pageNum, pageSize));  // 调用Service层查询消息列表，用Result.success包装后返回
    }

    /**
     * 标记单条消息为已读
     * 将指定ID的消息的已读状态从"未读"改为"已读"
     *
     * @param userId 当前登录用户的ID，由网关通过请求属性传递
     * @param id     要标记为已读的消息ID，来自URL路径
     * @return 操作结果（无返回数据）
     */
    @PutMapping("/{id}/read")  // PUT请求映射，处理 /api/message/{id}/read 请求，{id}是路径变量
    public Result<Void> markAsRead(@RequestHeader(value = "X-User-Id", required = false) String userId, @PathVariable Long id) {  // @PathVariable用于从URL路径中提取变量值
        messageService.markAsRead(userId, id);  // 调用Service层标记消息为已读
        return Result.success();  // 返回成功结果，无数据体
    }

    /**
     * 标记当前用户所有未读消息为已读
     * 一键将该用户收到的所有未读消息全部标记为已读状态
     *
     * @param userId 当前登录用户的ID，由网关通过请求属性传递
     * @return 操作结果（无返回数据）
     */
    @PutMapping("/read-all")  // PUT请求映射，处理 /api/message/read-all 请求
    public Result<Void> markAllAsRead(@RequestHeader(value = "X-User-Id", required = false) String userId) {  // 标记全部已读的方法
        messageService.markAllAsRead(userId);  // 调用Service层批量标记所有消息为已读
        return Result.success();  // 返回成功结果，无数据体
    }

    /**
     * 获取当前用户的未读消息数量
     * 查询该用户所有未读状态的消息条数，常用于消息角标显示
     *
     * @param userId 当前登录用户的ID，由网关通过请求属性传递
     * @return 未读消息的数量
     */
    @GetMapping("/unread-count")  // GET请求映射，处理 /api/message/unread-count 请求
    public Result<Long> getUnreadCount(@RequestHeader(value = "X-User-Id", required = false) String userId) {  // 获取未读消息数量的方法
        return Result.success(messageService.getUnreadCount(userId));  // 调用Service层查询未读数量，用Result.success包装后返回
    }
}
