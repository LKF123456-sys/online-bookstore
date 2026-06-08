package com.bookstore.admin.controller; // 声明当前类所在的包路径：控制器层

// 导入统一响应结果类，用于封装API的统一返回格式
import com.bookstore.common.api.Result;
// 导入分页结果类，用于封装分页查询结果
import com.bookstore.common.api.vo.PageResult;
// 导入操作日志服务类，提供日志查询功能
import com.bookstore.admin.service.AdminLogService;
// 导入Swagger/OpenAPI的@Operation注解，用于描述API接口的功能
import io.swagger.v3.oas.annotations.Operation;
// 导入Swagger/OpenAPI的@Parameter注解，用于描述接口参数
import io.swagger.v3.oas.annotations.Parameter;
// 导入Swagger/OpenAPI的@Tag注解，用于对接口进行分组归类
import io.swagger.v3.oas.annotations.tags.Tag;
// 导入Lombok的@RequiredArgsConstructor注解，自动生成包含final字段的构造方法
import lombok.RequiredArgsConstructor;
// 导入Spring Web的常用注解
import org.springframework.web.bind.annotation.*;

import java.util.Map; // 导入Map接口

/**
 * 管理后台API控制器 - 提供操作日志等系统管理接口
 * 这个控制器提供RESTful API接口，供前端AJAX调用或外部系统集成使用
 * 主要功能是查询系统操作日志
 */
@RestController // 标记这是一个REST控制器，返回值直接作为HTTP响应体（JSON格式）
@RequestMapping("/admin/api") // 设置该控制器所有接口的URL前缀为/admin/api
@RequiredArgsConstructor // 使用Lombok自动生成包含final字段的构造方法（依赖注入）
@Tag(name = "管理后台-系统", description = "操作日志等系统管理接口") // Swagger文档分组标签
public class AdminApiController {
    // 注入操作日志服务，用于查询操作日志
    private final AdminLogService adminLogService;

    /**
     * 获取操作日志列表接口
     * 支持分页查询和关键词搜索，返回分页后的日志数据
     *
     * @param pageNum 页码，默认第1页
     * @param pageSize 每页条数，默认10条
     * @param keyword 搜索关键词（可选），支持搜索操作类型、目标、管理员名称
     * @return 统一响应格式的分页日志数据
     */
    @Operation(summary = "获取操作日志列表", description = "分页查询系统操作日志，支持关键词搜索") // Swagger文档描述
    @GetMapping("/log/list") // 处理GET /admin/api/log/list请求
    public Result<PageResult<Map<String, Object>>> getLogList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum, // 页码参数
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize, // 每页条数参数
            @RequestParam(required = false) String keyword) { // 搜索关键词（可选）
        // 调用日志服务查询日志列表，用Result.success()包装为统一成功响应
        return Result.success(adminLogService.getLogList(pageNum, pageSize, keyword));
    }

    /**
     * 获取日志详情接口
     * 根据日志ID查询单条操作日志的详细信息
     *
     * @param id 日志记录的唯一标识ID
     * @return 统一响应格式的日志详情数据
     */
    @Operation(summary = "获取日志详情", description = "根据日志ID查询单条操作日志的详细信息") // Swagger文档描述
    @GetMapping("/log/{id}") // 处理GET /admin/api/log/{id}请求，{id}是路径变量
    public Result<Map<String, Object>> getLogById(@Parameter(description = "日志ID") @PathVariable Long id) { // @PathVariable从URL路径中提取id参数
        // 调用日志服务根据ID查询日志详情
        return Result.success(adminLogService.getLogById(id));
    }
}
