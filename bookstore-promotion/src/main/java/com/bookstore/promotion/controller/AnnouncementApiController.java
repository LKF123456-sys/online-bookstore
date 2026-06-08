package com.bookstore.promotion.controller;  // 声明当前类所在的包路径，属于营销服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一返回结果封装类，用于包装API响应数据
import com.bookstore.common.api.vo.AnnouncementVO;  // 导入公告视图对象，用于向前端返回公告信息
import com.bookstore.promotion.service.AnnouncementService;  // 导入公告服务类，处理公告相关的业务逻辑
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关注解，用于定义REST接口

import java.util.List;  // 导入Java集合List，用于存储列表数据

/**
 * 面向普通用户的公告API控制器
 * 提供用户端查看公告的接口，只返回当前处于激活状态的公告
 *
 * 所有接口路径以 /api/announcement 开头
 */
@RestController  // 标记为REST控制器，方法返回值会自动序列化为JSON响应
@RequestMapping("/api/announcement")  // 设置该控制器所有接口的URL前缀为 /api/announcement
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class AnnouncementApiController {  // 公告API控制器类

    private final AnnouncementService announcementService;  // 注入公告服务，用于处理公告相关的业务逻辑

    /**
     * 获取当前所有激活状态的公告
     * 用户访问首页或公告页面时调用，只返回状态为激活的公告
     *
     * @return 激活状态的公告列表
     */
    @GetMapping("/active")  // 映射GET请求到 /api/announcement/active
    public Result<List<AnnouncementVO>> getActiveAnnouncements() {  // 获取激活公告列表，不需要任何参数
        return Result.success(announcementService.getActiveAnnouncements());  // 调用服务层获取激活公告列表，包装成成功响应返回
    }
}
