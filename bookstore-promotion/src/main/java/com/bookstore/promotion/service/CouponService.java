package com.bookstore.promotion.service;  // 声明当前类所在的包路径，属于营销服务的业务逻辑层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用于构建类型安全的查询条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;  // 导入MyBatis-Plus的分页对象，用于实现分页查询
import com.bookstore.common.api.dto.CouponCreateDTO;  // 导入优惠券创建数据传输对象，用于接收创建/更新优惠券的数据
import com.bookstore.common.api.vo.CouponVO;  // 导入优惠券视图对象，用于向前端返回优惠券信息
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.common.entity.Coupon;  // 导入优惠券实体类，对应数据库中的优惠券表
import com.bookstore.common.entity.UserCoupon;  // 导入用户优惠券实体类，对应数据库中的用户-优惠券关联表
import com.bookstore.promotion.mapper.CouponMapper;  // 导入优惠券Mapper接口，用于操作优惠券表
import com.bookstore.promotion.mapper.UserCouponMapper;  // 导入用户优惠券Mapper接口，用于操作用户优惠券关联表
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法
import lombok.extern.slf4j.Slf4j;  // 导入Lombok的Slf4j注解，自动生成log日志对象
import org.springframework.beans.BeanUtils;  // 导入Spring的Bean工具类，用于对象属性拷贝
import org.springframework.stereotype.Service;  // 导入Spring的Service注解，标记为业务逻辑层组件
import org.springframework.transaction.annotation.Transactional;  // 导入事务注解，用于声明式事务管理

import java.time.LocalDateTime;  // 导入Java8日期时间类，用于处理时间相关的逻辑
import java.util.List;  // 导入Java集合List，用于存储列表数据
import java.util.stream.Collectors;  // 导入Java Stream的Collectors工具，用于流式数据收集

/**
 * 优惠券业务服务类
 * 处理所有与优惠券相关的业务逻辑，包括：
 *   - 用户端：查看可用优惠券列表、领取优惠券、查看我的优惠券、使用优惠券
 *   - 管理端：查看全部优惠券、创建、更新、删除优惠券、更新优惠券状态
 *
 * 使用 @Transactional 注解确保数据操作的一致性
 */
@Slf4j  // Lombok注解，自动生成log日志对象，用于记录日志
@Service  // 标记为Spring的Service组件，会被Spring容器自动扫描和管理
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class CouponService {  // 优惠券服务类

    private final CouponMapper couponMapper;  // 注入优惠券Mapper，用于数据库操作
    private final UserCouponMapper userCouponMapper;  // 注入用户优惠券Mapper，用于用户-优惠券关联表的操作

    /**
     * 获取可用优惠券列表（分页）
     * 只返回状态为启用（status=1）且当前时间在有效期内的优惠券
     * 按创建时间倒序排列（最新的排在前面）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页的优惠券视图对象列表
     */
    public PageResult<CouponVO> getCouponList(Integer pageNum, Integer pageSize) {  // 获取用户端可见的优惠券列表
        Page<Coupon> page = new Page<>(pageNum, pageSize);  // 创建分页对象，指定页码和每页数量
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.eq(Coupon::getStatus, 1)  // 添加条件：优惠券状态等于1（启用）
                .le(Coupon::getStartTime, LocalDateTime.now())  // 添加条件：开始时间小于等于当前时间（已开始）
                .ge(Coupon::getEndTime, LocalDateTime.now())  // 添加条件：结束时间大于等于当前时间（未过期）
                .orderByDesc(Coupon::getCreateTime);  // 按创建时间倒序排列
        Page<Coupon> result = couponMapper.selectPage(page, wrapper);  // 执行分页查询，返回查询结果
        List<CouponVO> voList = result.getRecords().stream()  // 获取查询结果记录列表，并转为Stream流
                .map(this::convertToVO)  // 将每个Coupon实体转换为CouponVO视图对象
                .collect(Collectors.toList());  // 将Stream流收集为List列表
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果对象，包含数据列表、总数、页码、每页数量
    }

    /**
     * 领取优惠券
     * 用户领取优惠券时会进行以下校验：
     *   1. 优惠券是否存在
     *   2. 优惠券是否处于启用状态
     *   3. 是否到了领取时间
     *   4. 是否已过期
     *   5. 是否已被领完
     *   6. 用户是否已领取过
     * 校验通过后创建用户-优惠券关联记录，并增加已领取数量
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @throws IllegalArgumentException 当任何校验不通过时抛出异常
     */
    @Transactional  // 声明式事务，确保领取操作的原子性（要么全部成功，要么全部回滚）
    public void claimCoupon(String userId, Long couponId) {  // 领取优惠券方法
        log.info("用户领取优惠券 / User claiming coupon, userId={}, couponId={}", userId, couponId);
        Coupon coupon = couponMapper.selectById(couponId);  // 根据优惠券ID查询数据库获取优惠券信息
        if (coupon == null) {  // 判断优惠券是否存在
            throw new IllegalArgumentException("优惠券不存在");  // 不存在则抛出异常提示
        }
        if (coupon.getStatus() != 1) {  // 判断优惠券是否处于启用状态（1=启用）
            throw new IllegalArgumentException("优惠券已禁用");  // 非启用状态则抛出异常
        }
        if (coupon.getStartTime().isAfter(LocalDateTime.now())) {  // 判断当前时间是否已到达优惠券的开始领取时间
            throw new IllegalArgumentException("优惠券未到领取时间");  // 还未到领取时间则抛出异常
        }
        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {  // 判断优惠券是否已过期
            throw new IllegalArgumentException("优惠券已过期");  // 已过期则抛出异常
        }
        if (coupon.getUsedCount() >= coupon.getTotalCount()) {  // 判断已领取数量是否达到发放总量
            throw new IllegalArgumentException("优惠券已领完");  // 已领完则抛出异常
        }

        // 检查是否已领取
        UserCoupon existing = userCouponMapper.selectOne(  // 查询该用户是否已领取过这张优惠券
                new LambdaQueryWrapper<UserCoupon>()  // 创建查询条件构造器
                        .eq(UserCoupon::getUserId, userId)  // 条件：用户ID等于当前用户
                        .eq(UserCoupon::getCouponId, couponId));  // 条件：优惠券ID等于当前优惠券
        if (existing != null) {  // 如果查询到了记录，说明用户已领取过
            throw new IllegalArgumentException("已领取过该优惠券");  // 已领取过则抛出异常
        }

        // 领取优惠券
        UserCoupon userCoupon = new UserCoupon();  // 创建新的用户优惠券关联对象
        userCoupon.setUserId(userId);  // 设置用户ID
        userCoupon.setCouponId(couponId);  // 设置优惠券ID
        userCoupon.setIsUsed(0); // 未使用  // 设置使用状态为0（未使用）
        userCouponMapper.insert(userCoupon);  // 将用户优惠券关联记录插入数据库

        // 更新领取数量
        coupon.setUsedCount(coupon.getUsedCount() + 1);  // 将优惠券的已领取数量加1
        couponMapper.updateById(coupon);  // 更新数据库中优惠券的信息
    }

    /**
     * 获取用户已领取的优惠券列表
     * 查询指定用户名下所有未使用的优惠券
     *
     * @param userId 用户ID
     * @return 用户的优惠券视图对象列表
     */
    public List<CouponVO> getUserCoupons(String userId) {  // 获取用户已领取且未使用的优惠券列表
        List<UserCoupon> userCoupons = userCouponMapper.selectList(  // 查询用户优惠券关联表
                new LambdaQueryWrapper<UserCoupon>()  // 创建查询条件构造器
                        .eq(UserCoupon::getUserId, userId)  // 条件：用户ID等于指定用户
                        .eq(UserCoupon::getIsUsed, 0));  // 条件：使用状态等于0（未使用）
        return userCoupons.stream().map(uc -> {  // 将查询结果转为Stream流，对每条记录进行转换
            Coupon coupon = couponMapper.selectById(uc.getCouponId());  // 根据优惠券ID查询优惠券详情
            CouponVO vo = convertToVO(coupon);  // 将优惠券实体转换为视图对象
            vo.setUserStatus(uc.getIsUsed());  // 设置用户的使用状态（0=未使用）
            return vo;  // 返回转换后的视图对象
        }).collect(Collectors.toList());  // 将Stream流收集为List列表并返回
    }

    /**
     * 使用优惠券
     * 用户下单时调用，将优惠券标记为已使用
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @throws IllegalArgumentException 当优惠券不存在或已使用时抛出异常
     */
    @Transactional  // 声明式事务，确保使用操作的原子性
    public void useCoupon(String userId, Long couponId) {  // 使用优惠券方法
        log.info("用户使用优惠券 / User using coupon, userId={}, couponId={}", userId, couponId);
        UserCoupon userCoupon = userCouponMapper.selectOne(  // 查询用户优惠券关联记录
                new LambdaQueryWrapper<UserCoupon>()  // 创建查询条件构造器
                        .eq(UserCoupon::getUserId, userId)  // 条件：用户ID等于指定用户
                        .eq(UserCoupon::getCouponId, couponId)  // 条件：优惠券ID等于指定优惠券
                        .eq(UserCoupon::getIsUsed, 0));  // 条件：使用状态等于0（未使用）
        if (userCoupon == null) {  // 如果查不到记录，说明优惠券不存在或已被使用
            throw new IllegalArgumentException("优惠券不存在或已使用");  // 抛出异常提示
        }
        userCoupon.setIsUsed(1); // 已使用  // 将使用状态设置为1（已使用）
        userCoupon.setUseTime(LocalDateTime.now());  // 记录使用时间为当前时间
        userCouponMapper.updateById(userCoupon);  // 更新数据库中的用户优惠券记录
    }

    // 管理员接口

    /**
     * 获取所有优惠券列表（分页，管理员用）
     * 与用户端不同，管理员可以查看所有状态的优惠券，不受状态和有效期限制
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页的优惠券视图对象列表
     */
    public PageResult<CouponVO> getAllCoupons(Integer pageNum, Integer pageSize) {  // 管理员获取全部优惠券列表
        Page<Coupon> page = new Page<>(pageNum, pageSize);  // 创建分页对象，指定页码和每页数量
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.orderByDesc(Coupon::getCreateTime);  // 按创建时间倒序排列（不限制状态和有效期）
        Page<Coupon> result = couponMapper.selectPage(page, wrapper);  // 执行分页查询，返回查询结果
        List<CouponVO> voList = result.getRecords().stream()  // 获取查询结果记录列表，并转为Stream流
                .map(this::convertToVO)  // 将每个Coupon实体转换为CouponVO视图对象
                .collect(Collectors.toList());  // 将Stream流收集为List列表
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果对象返回
    }

    /**
     * 创建优惠券（管理员操作）
     * 管理员创建新的优惠券，初始已领取数量为0，状态为启用
     *
     * @param dto 优惠券创建数据
     */
    @Transactional  // 声明式事务，确保创建操作的原子性
    public void createCoupon(CouponCreateDTO dto) {  // 创建优惠券方法
        log.info("管理员创建优惠券 / Admin creating coupon, name={}", dto.getName());
        Coupon coupon = new Coupon();  // 创建新的优惠券实体对象
        BeanUtils.copyProperties(dto, coupon);  // 将DTO中的属性拷贝到实体对象中（自动映射同名属性）
        coupon.setUsedCount(0);  // 设置已领取数量为0（新创建的优惠券还没有人领取）
        coupon.setStatus(1);  // 设置状态为1（启用），新创建的优惠券默认启用
        couponMapper.insert(coupon);  // 将优惠券记录插入数据库
    }

    /**
     * 更新优惠券信息（管理员操作）
     * 管理员可以修改已有优惠券的各项信息
     *
     * @param id  优惠券ID
     * @param dto 优惠券更新数据
     * @throws IllegalArgumentException 当优惠券不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保更新操作的原子性
    public void updateCoupon(Long id, CouponCreateDTO dto) {  // 更新优惠券方法
        log.info("管理员更新优惠券 / Admin updating coupon, id={}", id);
        Coupon coupon = couponMapper.selectById(id);  // 根据ID查询优惠券
        if (coupon == null) {  // 判断优惠券是否存在
            throw new IllegalArgumentException("优惠券不存在");  // 不存在则抛出异常
        }
        BeanUtils.copyProperties(dto, coupon);  // 将DTO中的属性拷贝到实体对象中，覆盖原有值
        couponMapper.updateById(coupon);  // 更新数据库中的优惠券记录
    }

    /**
     * 删除优惠券（管理员操作）
     * 物理删除指定的优惠券记录
     *
     * @param id 优惠券ID
     */
    @Transactional  // 声明式事务，确保删除操作的原子性
    public void deleteCoupon(Long id) {  // 删除优惠券方法
        log.info("管理员删除优惠券 / Admin deleting coupon, id={}", id);
        couponMapper.deleteById(id);  // 根据ID删除数据库中的优惠券记录
    }

    /**
     * 更新优惠券状态（管理员操作）
     * 管理员可以启用或禁用优惠券
     *
     * @param id     优惠券ID
     * @param status 目标状态（1=启用，0=禁用）
     * @throws IllegalArgumentException 当优惠券不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保状态更新操作的原子性
    public void updateCouponStatus(Long id, Integer status) {  // 更新优惠券状态方法
        log.info("管理员更新优惠券状态 / Admin updating coupon status, id={}, status={}", id, status);
        Coupon coupon = couponMapper.selectById(id);  // 根据ID查询优惠券
        if (coupon == null) {  // 判断优惠券是否存在
            throw new IllegalArgumentException("优惠券不存在");  // 不存在则抛出异常
        }
        coupon.setStatus(status);  // 设置优惠券的新状态
        couponMapper.updateById(coupon);  // 更新数据库中的优惠券记录
    }

    /**
     * 将优惠券实体对象转换为视图对象
     * 用于向前端返回数据时隐藏数据库内部结构
     *
     * @param coupon 优惠券实体对象
     * @return 优惠券视图对象
     */
    private CouponVO convertToVO(Coupon coupon) {  // 私有方法，将实体转换为VO
        CouponVO vo = new CouponVO();  // 创建新的视图对象
        BeanUtils.copyProperties(coupon, vo);  // 将实体中的属性拷贝到视图对象中
        return vo;  // 返回转换后的视图对象
    }

    /**
     * 获取优惠券类型的中文文本
     * 将优惠券类型标识转换为可读的中文名称
     *
     * @param type 优惠券类型标识
     * @return 中文类型名称
     */
    private String getTypeText(String type) {  // 私有方法，获取类型对应的中文文本
        switch (type) {  // 根据类型标识进行匹配
            case "满减": return "满减";  // 满减类型，返回"满减"
            case "折扣": return "折扣";  // 折扣类型，返回"折扣"
            case "无门槛": return "无门槛";  // 无门槛类型，返回"无门槛"
            default: return type;  // 默认情况，直接返回原始类型标识
        }
    }
}
