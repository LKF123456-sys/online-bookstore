package com.bookstore.message.controller;  // 声明当前类所属的包路径，属于消息服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一响应结果包装类，用于封装API返回数据
import com.bookstore.common.api.vo.MessageVO;  // 导入消息视图对象，用于返回给前端的消息数据结构
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果包装类，用于封装分页查询的返回数据
import com.bookstore.message.service.MessageService;  // 导入消息业务服务类，处理消息相关的业务逻辑
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含所有final字段的构造函数
import org.springframework.web.bind.annotation.*;  // 导入Spring MVC的Web注解，包含@RestController、@GetMapping等

/**
 * 管理端消息控制器（面向管理员）
 * 提供管理员对消息系统的管理接口
 * 所有接口路径前缀为 /admin/message
 *
 * 接口列表：
 *   - GET  /admin/message/list       获取所有消息列表（分页，管理员视角）
 *   - POST /admin/message/broadcast  发送系统广播消息给所有用户
 */
@RestController  // REST控制器注解，所有方法返回JSON格式数据
@RequestMapping("/admin/message")  // URL路径前缀，该控制器下所有接口以 /admin/message 开头
@RequiredArgsConstructor  // Lombok注解，自动生成包含所有final字段的构造函数，实现依赖注入
public class AdminMessageController {  // 管理端消息控制器类

    private final MessageService messageService;  // 消息业务服务对象，通过构造函数注入

    /**
     * 获取所有消息列表（管理员视角）
     * 不区分接收人，查询系统中所有消息，按创建时间倒序排列
     * 管理员可以查看所有用户的消息，用于消息管理和监控
     *
     * @param pageNum  当前页码，默认为第1页
     * @param pageSize 每页显示的条数，默认为10条
     * @return 分页结果，包含消息列表、总条数、当前页码、每页大小
     */
    @GetMapping("/list")  // GET请求映射，处理 /admin/message/list 请求
    public Result<PageResult<MessageVO>> getAllMessages(  // 获取所有消息列表的方法
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从URL查询参数中获取页码，默认值为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 从URL查询参数中获取每页条数，默认值为10
        return Result.success(messageService.getAllMessages(pageNum, pageSize));  // 调用Service层查询所有消息，用Result.success包装后返回
    }

    /**
     * 发送系统广播消息
     * 管理员向所有用户发送系统公告或通知
     * 广播消息的接收者为"all"，表示所有用户都能看到
     *
     * @param content 广播消息的内容文本
     * @return 操作结果（无返回数据）
     */
    @PostMapping("/broadcast")  // POST请求映射，处理 /admin/message/broadcast 请求
    public Result<Void> sendBroadcast(@RequestParam String content) {  // @RequestParam从URL参数或表单数据中获取消息内容
        messageService.sendBroadcast(content);  // 调用Service层发送广播消息
        return Result.success();  // 返回成功结果，无数据体
    }
}
