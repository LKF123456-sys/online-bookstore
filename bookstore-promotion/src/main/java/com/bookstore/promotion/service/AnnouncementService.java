package com.bookstore.promotion.service;  // 声明当前类所在的包路径，属于营销服务的业务逻辑层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用于构建类型安全的查询条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;  // 导入MyBatis-Plus的分页对象，用于实现分页查询
import com.bookstore.common.api.vo.AnnouncementVO;  // 导入公告视图对象，用于接收和返回公告信息
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.common.entity.Announcement;  // 导入公告实体类，对应数据库中的公告表
import com.bookstore.promotion.mapper.AnnouncementMapper;  // 导入公告Mapper接口，用于操作公告表
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法
import org.springframework.beans.BeanUtils;  // 导入Spring的Bean工具类，用于对象属性拷贝
import org.springframework.stereotype.Service;  // 导入Spring的Service注解，标记为业务逻辑层组件
import org.springframework.transaction.annotation.Transactional;  // 导入事务注解，用于声明式事务管理

import java.util.List;  // 导入Java集合List，用于存储列表数据
import java.util.stream.Collectors;  // 导入Java Stream的Collectors工具，用于流式数据收集

/**
 * 公告业务服务类
 * 处理所有与公告相关的业务逻辑，包括：
 *   - 用户端：查看激活状态的公告列表
 *   - 管理端：查看全部公告、创建、更新、删除公告、更新公告状态
 *
 * 使用 @Transactional 注解确保数据操作的一致性
 */
@Service  // 标记为Spring的Service组件，会被Spring容器自动扫描和管理
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class AnnouncementService {  // 公告服务类

    private final AnnouncementMapper announcementMapper;  // 注入公告Mapper，用于数据库操作

    /**
     * 获取当前激活状态的公告列表
     * 只返回状态为启用（status=1）的公告，按创建时间倒序排列
     * 供普通用户在前端页面查看
     *
     * @return 激活状态的公告视图对象列表
     */
    public List<AnnouncementVO> getActiveAnnouncements() {  // 获取激活状态的公告列表
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.eq(Announcement::getStatus, 1)  // 添加条件：公告状态等于1（启用）
                .orderByDesc(Announcement::getCreatedAt);  // 按创建时间倒序排列（最新的公告排在前面）
        return announcementMapper.selectList(wrapper).stream()  // 执行查询获取公告列表，并转为Stream流
                .map(this::convertToVO)  // 将每个Announcement实体转换为AnnouncementVO视图对象
                .collect(Collectors.toList());  // 将Stream流收集为List列表并返回
    }

    // 管理员接口

    /**
     * 获取所有公告列表（分页，管理员用）
     * 管理员可以查看所有状态的公告，按创建时间倒序排列
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页的公告视图对象列表
     */
    public PageResult<AnnouncementVO> getAllAnnouncements(Integer pageNum, Integer pageSize) {  // 管理员获取全部公告列表
        Page<Announcement> page = new Page<>(pageNum, pageSize);  // 创建分页对象，指定页码和每页数量
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.orderByDesc(Announcement::getCreatedAt);  // 按创建时间倒序排列（不限制状态）
        Page<Announcement> result = announcementMapper.selectPage(page, wrapper);  // 执行分页查询，返回查询结果
        List<AnnouncementVO> voList = result.getRecords().stream()  // 获取查询结果记录列表，并转为Stream流
                .map(this::convertToVO)  // 将每个Announcement实体转换为AnnouncementVO视图对象
                .collect(Collectors.toList());  // 将Stream流收集为List列表
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果对象返回
    }

    /**
     * 创建公告（管理员操作）
     * 管理员创建新的公告，将视图对象中的数据转换为实体后保存到数据库
     *
     * @param vo 公告视图对象，包含标题、内容等信息
     */
    @Transactional  // 声明式事务，确保创建操作的原子性
    public void createAnnouncement(AnnouncementVO vo) {  // 创建公告方法
        Announcement announcement = new Announcement();  // 创建新的公告实体对象
        BeanUtils.copyProperties(vo, announcement);  // 将视图对象中的属性拷贝到实体对象中
        announcementMapper.insert(announcement);  // 将公告记录插入数据库
    }

    /**
     * 更新公告信息（管理员操作）
     * 管理员可以修改已有公告的标题、内容等信息
     *
     * @param id 公告ID
     * @param vo 公告更新数据
     * @throws IllegalArgumentException 当公告不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保更新操作的原子性
    public void updateAnnouncement(Long id, AnnouncementVO vo) {  // 更新公告方法
        Announcement announcement = announcementMapper.selectById(id);  // 根据ID查询公告
        if (announcement == null) {  // 判断公告是否存在
            throw new IllegalArgumentException("公告不存在");  // 不存在则抛出异常
        }
        BeanUtils.copyProperties(vo, announcement);  // 将视图对象中的属性拷贝到实体对象中，覆盖原有值
        announcement.setId(id);  // 确保ID不被覆盖，设置正确的公告ID
        announcementMapper.updateById(announcement);  // 更新数据库中的公告记录
    }

    /**
     * 删除公告（管理员操作）
     * 物理删除指定的公告记录
     *
     * @param id 公告ID
     */
    @Transactional  // 声明式事务，确保删除操作的原子性
    public void deleteAnnouncement(Long id) {  // 删除公告方法
        announcementMapper.deleteById(id);  // 根据ID删除数据库中的公告记录
    }

    /**
     * 更新公告状态（管理员操作）
     * 管理员可以启用或禁用公告
     *
     * @param id     公告ID
     * @param status 目标状态（1=启用，0=禁用）
     * @throws IllegalArgumentException 当公告不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保状态更新操作的原子性
    public void updateAnnouncementStatus(Long id, Integer status) {  // 更新公告状态方法
        Announcement announcement = announcementMapper.selectById(id);  // 根据ID查询公告
        if (announcement == null) {  // 判断公告是否存在
            throw new IllegalArgumentException("公告不存在");  // 不存在则抛出异常
        }
        announcement.setStatus(status);  // 设置公告的新状态
        announcementMapper.updateById(announcement);  // 更新数据库中的公告记录
    }

    /**
     * 将公告实体对象转换为视图对象
     * 用于向前端返回数据时隐藏数据库内部结构
     *
     * @param announcement 公告实体对象
     * @return 公告视图对象
     */
    private AnnouncementVO convertToVO(Announcement announcement) {  // 私有方法，将实体转换为VO
        AnnouncementVO vo = new AnnouncementVO();  // 创建新的视图对象
        BeanUtils.copyProperties(announcement, vo);  // 将实体中的属性拷贝到视图对象中
        return vo;  // 返回转换后的视图对象
    }
}
