package com.bookstore.admin.service; // 声明当前类所在的包路径：服务层

// 导入MyBatis-Plus的Lambda查询条件构造器，用于构建类型安全的查询条件
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 导入MyBatis-Plus的分页对象，用于封装分页查询参数
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// 导入分页结果类，用于封装分页查询的最终返回结果
import com.bookstore.common.api.vo.PageResult;
// 导入操作日志实体类，对应数据库中的admin_log表
import com.bookstore.common.entity.AdminLog;
// 导入操作日志Mapper接口，用于数据库CRUD操作
import com.bookstore.admin.mapper.AdminLogMapper;
// 导入Lombok的@RequiredArgsConstructor注解
import lombok.RequiredArgsConstructor;
// 导入Spring的@Service注解，标记这是一个业务逻辑服务类
import org.springframework.stereotype.Service;
// 导入Spring的@Transactional注解，用于声明事务管理
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // 导入Java 8的日期时间类
import java.time.ZoneId; // 导入时区类
import java.util.List; // 导入List接口
import java.util.Map; // 导入Map接口
import java.util.HashMap; // 导入HashMap类

/**
 * 操作日志服务 - 提供操作日志的记录和查询功能
 *
 * 这个服务负责：
 * 1. 记录管理员的操作日志（谁、什么时间、做了什么操作）
 * 2. 查询操作日志列表（支持分页和关键词搜索）
 * 3. 查询单条日志详情
 *
 * 日志记录是管理后台的重要功能，用于审计和追踪管理员的操作行为。
 */
@Service // 标记这是一个Spring Service组件，会被Spring自动扫描和管理
@RequiredArgsConstructor // 使用Lombok自动生成包含final字段的构造方法（依赖注入）
public class AdminLogService {
    // 注入操作日志Mapper，用于数据库操作
    private final AdminLogMapper adminLogMapper;

    /**
     * 私有工具方法 - 将LocalDateTime转换为java.util.Date
     * 因为JSP的JSTL格式化标签需要java.util.Date类型
     *
     * @param ldt Java 8的LocalDateTime对象
     * @return 转换后的java.util.Date对象，如果输入为null则返回null
     */
    private java.util.Date toDate(LocalDateTime ldt) {
        if (ldt == null) return null; // 空值检查
        // 将LocalDateTime通过系统默认时区转换为Date
        return java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 保存操作日志记录
     * 将管理员的操作行为记录到数据库中
     *
     * @param adminName 执行操作的管理员名称
     * @param operation 操作类型描述（如"新增图书"、"编辑用户"等）
     * @param target 操作目标（通常是请求的URL路径）
     * @param detail 操作详情（如"成功"、"失败"等）
     * @param ip 操作者的IP地址
     */
    @Transactional // 声明此方法在事务中执行，确保数据一致性
    /** @see #saveLog(String, String, String, String, String) 不记录IP时的便捷版本 */
    public void addLog(String adminName, String operation, String target, String detail) {
        this.saveLog(adminName, operation, target, detail, "");
    }

    public void saveLog(String adminName, String operation, String target, String detail, String ip) {
        AdminLog log = new AdminLog(); // 创建日志实体对象
        log.setAdminName(adminName); // 设置管理员名称
        log.setOperation(operation); // 设置操作类型
        log.setTarget(target); // 设置操作目标
        log.setDetail(detail); // 设置操作详情
        log.setIp(ip); // 设置操作者IP
        adminLogMapper.insert(log); // 使用MyBatis-Plus的insert方法插入数据库
    }

    /**
     * 获取操作日志列表（分页查询）
     * 支持按关键词搜索，搜索范围包括操作类型、目标、管理员名称
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @param keyword 搜索关键词（可选）
     * @return 分页结果对象，包含日志列表、总数、页码等信息
     */
    public PageResult<Map<String, Object>> getLogList(Integer pageNum, Integer pageSize, String keyword) {
        // 创建分页对象，指定页码和每页条数
        Page<AdminLog> page = new Page<>(pageNum, pageSize);
        // 创建Lambda查询条件构造器（类型安全，避免写错字段名）
        LambdaQueryWrapper<AdminLog> wrapper = new LambdaQueryWrapper<>();
        // 如果有搜索关键词，构建模糊查询条件
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(AdminLog::getOperation, keyword) // 操作类型包含关键词
                    .or().like(AdminLog::getTarget, keyword) // 或 操作目标包含关键词
                    .or().like(AdminLog::getAdminName, keyword); // 或 管理员名称包含关键词
        }
        // 按创建时间降序排列（最新的日志在前面）
        wrapper.orderByDesc(AdminLog::getCreateTime);
        // 执行分页查询
        Page<AdminLog> result = adminLogMapper.selectPage(page, wrapper);

        // 将实体对象列表转换为Map列表（VO层数据转换）
        List<Map<String, Object>> voList = result.getRecords().stream().map(log -> {
            Map<String, Object> map = new HashMap<>(); // 创建Map存储日志字段
            map.put("id", log.getId()); // 日志ID
            map.put("adminName", log.getAdminName()); // 管理员名称
            map.put("operation", log.getOperation()); // 操作类型
            map.put("target", log.getTarget()); // 操作目标
            map.put("detail", log.getDetail()); // 操作详情
            map.put("ip", log.getIp()); // 操作者IP
            map.put("createTime", toDate(log.getCreateTime())); // 创建时间（转换为Date类型）
            return map; // 返回转换后的Map
        }).collect(java.util.stream.Collectors.toList()); // 收集为List

        // 返回分页结果，包含VO列表、总记录数、当前页码、每页条数
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);
    }

    /**
     * 根据ID获取单条日志详情
     *
     * @param id 日志记录的ID
     * @return 包含日志详情的Map对象
     * @throws IllegalArgumentException 如果日志不存在则抛出异常
     */
    public Map<String, Object> getLogById(Long id) {
        // 使用MyBatis-Plus的selectById方法根据主键查询
        AdminLog log = adminLogMapper.selectById(id);
        // 如果日志不存在，抛出业务异常
        if (log == null) {
            throw new IllegalArgumentException("日志不存在");
        }
        // 将实体对象转换为Map
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId()); // 日志ID
        map.put("adminName", log.getAdminName()); // 管理员名称
        map.put("operation", log.getOperation()); // 操作类型
        map.put("target", log.getTarget()); // 操作目标
        map.put("detail", log.getDetail()); // 操作详情
        map.put("ip", log.getIp()); // 操作者IP
        map.put("createTime", toDate(log.getCreateTime())); // 创建时间
        return map; // 返回日志详情Map
    }
}
