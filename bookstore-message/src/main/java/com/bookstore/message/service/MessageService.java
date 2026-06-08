package com.bookstore.message.service;  // 声明当前类所属的包路径，属于消息服务的业务逻辑层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用Java方法引用构建类型安全的SQL条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;  // 导入MyBatis-Plus的分页对象，用于分页查询
import com.bookstore.common.api.vo.MessageVO;  // 导入消息视图对象，用于返回给前端的消息数据结构
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果包装类，用于封装分页查询的返回数据
import com.bookstore.common.entity.Message;  // 导入消息实体类，对应数据库中的消息表
import com.bookstore.message.mapper.MessageMapper;  // 导入消息Mapper接口，用于操作消息数据表
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含所有final字段的构造函数
import org.springframework.beans.BeanUtils;  // 导入Spring的Bean工具类，用于对象属性拷贝
import org.springframework.stereotype.Service;  // 导入Spring的Service注解，标记为业务层组件
import org.springframework.transaction.annotation.Transactional;  // 导入Spring事务注解，用于声明式事务管理

import java.util.List;  // 导入Java集合框架的List接口
import java.util.stream.Collectors;  // 导入Java Stream的Collectors工具类，用于将Stream收集为集合

/**
 * 消息业务服务类
 * 处理消息相关的所有业务逻辑，包括：
 *   - 用户消息列表查询（分页，按接收人筛选）
 *   - 单条消息标记已读
 *   - 批量标记所有消息为已读
 *   - 未读消息数量统计
 *   - 发送点对点消息
 *   - 发送系统广播消息
 *   - 管理员查看所有消息
 *
 * 该类是控制器层和数据访问层之间的桥梁，负责：
 *   1. 接收控制器传来的参数
 *   2. 构建查询条件并调用Mapper层查询数据库
 *   3. 实体对象（Entity）到视图对象（VO）的转换
 *   4. 事务管理（保证数据一致性）
 */
@Service  // 标记为Spring的Service组件，会被Spring自动扫描并注册为Bean
@RequiredArgsConstructor  // Lombok注解，自动生成包含所有final字段的构造函数，实现依赖注入
public class MessageService {  // 消息业务服务类

    private final MessageMapper messageMapper;  // 消息数据访问对象，用于操作消息表（通过构造函数注入）

    /**
     * 获取指定用户的消息列表（分页查询）
     * 查询该用户收到的所有消息，按创建时间倒序排列（最新的在前面）
     *
     * @param receiverId 消息接收者的用户ID
     * @param pageNum    当前页码
     * @param pageSize   每页显示的条数
     * @return 分页结果，包含消息VO列表、总条数、当前页码、每页大小
     */
    public PageResult<MessageVO> getMessageList(String receiverId, Integer pageNum, Integer pageSize) {  // 获取用户消息列表的方法
        Page<Message> page = new Page<>(pageNum, pageSize);  // 创建分页对象，传入当前页码和每页大小
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.eq(Message::getReceiverId, receiverId);  // 条件：消息接收者ID等于传入的receiverId，只查询该用户的消息
        wrapper.orderByDesc(Message::getCreateTime);  // 按创建时间降序排列，最新的消息排在最前面
        Page<Message> result = messageMapper.selectPage(page, wrapper);  // 执行分页查询，返回分页结果
        List<MessageVO> voList = result.getRecords().stream()  // 获取查询结果的记录列表，并转为Stream流
                .map(this::convertToVO)  // 对每个实体对象调用convertToVO方法，转换为VO对象
                .collect(Collectors.toList());  // 将Stream流收集为List集合
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果返回
    }

    /**
     * 标记单条消息为已读
     * 先验证消息是否存在且属于当前用户，再将已读状态设为1
     * 使用事务保证操作的原子性
     *
     * @param receiverId 消息接收者的用户ID（当前登录用户）
     * @param messageId  要标记为已读的消息ID
     * @throws IllegalArgumentException 如果消息不存在或不属于该用户则抛出异常
     */
    @Transactional  // 声明式事务注解，保证方法内的数据库操作要么全部成功，要么全部回滚
    public void markAsRead(String receiverId, Long messageId) {  // 标记单条消息已读的方法
        Message message = messageMapper.selectOne(  // 查询单条消息记录
                new LambdaQueryWrapper<Message>()  // 创建查询条件
                        .eq(Message::getId, messageId)  // 条件：消息ID等于传入的messageId
                        .eq(Message::getReceiverId, receiverId));  // 条件：接收者ID等于传入的receiverId，确保只能标记自己的消息
        if (message == null) {  // 如果查询结果为空，说明消息不存在或不属于该用户
            throw new IllegalArgumentException("消息不存在");  // 抛出非法参数异常
        }
        message.setReadStatus(1);  // 将消息的已读状态设为1（已读），0表示未读
        messageMapper.updateById(message);  // 根据消息ID更新数据库中的记录
    }

    /**
     * 批量标记指定用户的所有未读消息为已读
     * 一键操作，将该用户所有未读消息全部标记为已读
     * 使用事务保证操作的原子性
     *
     * @param receiverId 消息接收者的用户ID（当前登录用户）
     */
    @Transactional  // 声明式事务注解，保证批量更新操作的原子性
    public void markAllAsRead(String receiverId) {  // 批量标记所有消息已读的方法
        Message update = new Message();  // 创建一个消息实体对象，用于设置要更新的字段值
        update.setReadStatus(1);  // 设置已读状态为1（已读）
        messageMapper.update(update,  // 使用MyBatis-Plus的条件更新方法，将匹配的消息更新为已读
                new LambdaQueryWrapper<Message>()  // 创建查询条件
                        .eq(Message::getReceiverId, receiverId)  // 条件：接收者ID等于传入的receiverId
                        .eq(Message::getReadStatus, 0));  // 条件：当前已读状态为0（未读），只更新未读的消息
    }

    /**
     * 获取指定用户的未读消息数量
     * 统计该用户所有未读状态（readStatus=0）的消息条数
     * 常用于前端页面的消息角标显示
     *
     * @param receiverId 消息接收者的用户ID
     * @return 未读消息的数量
     */
    public Long getUnreadCount(String receiverId) {  // 获取未读消息数量的方法
        return messageMapper.selectCount(  // 使用MyBatis-Plus的selectCount方法统计匹配的记录数
                new LambdaQueryWrapper<Message>()  // 创建查询条件
                        .eq(Message::getReceiverId, receiverId)  // 条件：接收者ID等于传入的receiverId
                        .eq(Message::getReadStatus, 0));  // 条件：已读状态为0（未读）
    }

    /**
     * 发送点对点消息
     * 创建一条消息记录，从发送者发给指定的接收者
     * 新消息默认为未读状态（readStatus=0）
     *
     * @param senderId     发送者ID
     * @param senderType   发送者类型（如"user"表示普通用户，"system"表示系统）
     * @param receiverId   接收者ID
     * @param receiverType 接收者类型（如"user"表示普通用户，"all"表示所有用户）
     * @param content      消息内容文本
     */
    @Transactional  // 声明式事务注解，保证消息插入操作的原子性
    public void sendMessage(String senderId, String senderType, String receiverId, String receiverType, String content) {  // 发送消息的方法
        Message message = new Message();  // 创建消息实体对象
        message.setSenderId(senderId);  // 设置发送者ID
        message.setSenderType(senderType);  // 设置发送者类型（如"user"、"system"）
        message.setReceiverId(receiverId);  // 设置接收者ID
        message.setReceiverType(receiverType);  // 设置接收者类型（如"user"、"all"）
        message.setContent(content);  // 设置消息内容
        message.setReadStatus(0);  // 设置已读状态为0（未读），新发送的消息默认是未读的
        messageMapper.insert(message);  // 将消息记录插入数据库
    }

    // ========== 管理员接口 ==========  // 以下方法供管理端使用

    /**
     * 获取所有消息列表（管理员视角）
     * 不区分接收人，查询系统中所有消息，按创建时间倒序排列
     * 管理员可以查看所有用户的消息，用于消息管理和监控
     *
     * @param pageNum  当前页码
     * @param pageSize 每页显示的条数
     * @return 分页结果，包含消息列表、总条数、当前页码、每页大小
     */
    public PageResult<MessageVO> getAllMessages(Integer pageNum, Integer pageSize) {  // 管理员获取所有消息列表的方法
        Page<Message> page = new Page<>(pageNum, pageSize);  // 创建分页对象，传入当前页码和每页大小
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.orderByDesc(Message::getCreateTime);  // 按创建时间降序排列，最新的消息排在最前面
        Page<Message> result = messageMapper.selectPage(page, wrapper);  // 执行分页查询，返回分页结果
        List<MessageVO> voList = result.getRecords().stream()  // 获取查询结果的记录列表，并转为Stream流
                .map(this::convertToVO)  // 对每个实体对象调用convertToVO方法，转换为VO对象
                .collect(Collectors.toList());  // 将Stream流收集为List集合
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果返回
    }

    /**
     * 发送系统广播消息
     * 管理员向所有用户发送系统公告或通知
     * 发送者为系统（senderId="0"，senderType="system"）
     * 接收者为所有用户（receiverId="all"，receiverType="all"）
     *
     * @param content 广播消息的内容文本
     */
    @Transactional  // 声明式事务注解，保证消息插入操作的原子性
    public void sendBroadcast(String content) {  // 发送广播消息的方法
        Message message = new Message();  // 创建消息实体对象
        message.setSenderId("0");  // 设置发送者ID为"0"，表示系统发送
        message.setSenderType("system");  // 设置发送者类型为"system"，表示系统消息
        message.setReceiverId("all");  // 设置接收者ID为"all"，表示发送给所有用户
        message.setReceiverType("all");  // 设置接收者类型为"all"，表示面向所有用户
        message.setContent(content);  // 设置广播消息的内容
        message.setReadStatus(0);  // 设置已读状态为0（未读），新发送的广播默认是未读的
        messageMapper.insert(message);  // 将广播消息记录插入数据库
    }

    /**
     * 将消息实体对象转换为视图对象
     * 使用Spring的BeanUtils进行属性拷贝
     * 实体对象是数据库映射，视图对象是返回给前端的数据结构
     *
     * @param message 消息实体对象
     * @return 消息视图对象
     */
    private MessageVO convertToVO(Message message) {  // 私有方法，仅在本类内使用
        MessageVO vo = new MessageVO();  // 创建消息视图对象
        BeanUtils.copyProperties(message, vo);  // 将实体对象的所有同名属性拷贝到视图对象
        return vo;  // 返回转换后的视图对象
    }
}
