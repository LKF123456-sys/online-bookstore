package com.bookstore.promotion.controller;  // 声明当前类所在的包路径，属于营销服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一返回结果封装类，用于包装API响应数据
import com.bookstore.common.api.vo.AnnouncementVO;  // 导入公告视图对象，用于接收和返回公告信息
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.promotion.service.AnnouncementService;  // 导入公告服务类，处理公告相关的业务逻辑
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关注解，用于定义REST接口

/**
 * 管理端公告控制器
 * 提供管理员对公告的增删改查和状态管理接口，包括：
 *   - 查看所有公告列表
 *   - 创建公告
 *   - 编辑公告
 *   - 删除公告
 *   - 启用/禁用公告
 *
 * 所有接口路径以 /admin/announcement 开头
 */
@RestController  // 标记为REST控制器，方法返回值会自动序列化为JSON响应
@RequestMapping("/admin/announcement")  // 设置该控制器所有接口的URL前缀为 /admin/announcement
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class AdminAnnouncementController {  // 管理端公告控制器类

    private final AnnouncementService announcementService;  // 注入公告服务，用于处理公告相关的业务逻辑

    /**
     * 获取所有公告列表（分页）
     * 管理员可以查看所有公告，包括已禁用的
     *
     * @param pageNum  页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页的公告列表
     */
    @GetMapping("/list")  // 映射GET请求到 /admin/announcement/list
    public Result<PageResult<AnnouncementVO>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从请求参数获取页码，未传时默认为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 从请求参数获取每页大小，未传时默认为10
        return Result.success(announcementService.getAllAnnouncements(pageNum, pageSize));  // 调用服务层获取全部公告列表，包装成成功响应返回
    }

    /**
     * 创建公告
     * 管理员填写公告标题和内容后提交，系统创建新公告
     *
     * @param vo 公告视图对象，包含标题、内容等信息，从请求体JSON反序列化
     * @return 操作结果
     */
    @PostMapping  // 映射POST请求到 /admin/announcement（使用控制器的前缀）
    public Result<Void> createAnnouncement(@RequestBody AnnouncementVO vo) {  // 从请求体JSON反序列化为AnnouncementVO对象
        announcementService.createAnnouncement(vo);  // 调用服务层创建公告
        return Result.success();  // 创建成功，返回成功响应
    }

    /**
     * 更新公告信息
     * 管理员可以修改已有公告的标题、内容等信息
     *
     * @param id 公告ID
     * @param vo 公告更新数据，从请求体JSON反序列化
     * @return 操作结果
     */
    @PutMapping("/{id}")  // 映射PUT请求到 /admin/announcement/{id}，用于更新资源
    public Result<Void> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementVO vo) {  // id从URL路径获取，vo从请求体获取
        announcementService.updateAnnouncement(id, vo);  // 调用服务层更新公告信息
        return Result.success();  // 更新成功，返回成功响应
    }

    /**
     * 删除公告
     * 管理员可以删除不需要的公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")  // 映射DELETE请求到 /admin/announcement/{id}，用于删除资源
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {  // 从URL路径获取公告ID
        announcementService.deleteAnnouncement(id);  // 调用服务层删除公告
        return Result.success();  // 删除成功，返回成功响应
    }

    /**
     * 更新公告状态（启用/禁用）
     * 管理员可以启用或禁用公告，禁用后普通用户看不到该公告
     *
     * @param id     公告ID
     * @param status 目标状态（1=启用，0=禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")  // 映射PUT请求到 /admin/announcement/{id}/status，用于更新公告状态
    public Result<Void> updateAnnouncementStatus(@PathVariable Long id, @RequestParam Integer status) {  // id从URL路径获取，status从请求参数获取
        announcementService.updateAnnouncementStatus(id, status);  // 调用服务层更新公告状态
        return Result.success();  // 状态更新成功，返回成功响应
    }
}
