package com.bookstore.promotion.service;  // 声明当前类所在的包路径，属于营销服务的业务逻辑层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用于构建类型安全的查询条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;  // 导入MyBatis-Plus的分页对象，用于实现分页查询
import com.bookstore.common.api.dto.ReviewSubmitDTO;  // 导入评价提交数据传输对象，用于接收用户提交评价的数据
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.common.api.vo.ReviewVO;  // 导入评价视图对象，用于向前端返回评价信息
import com.bookstore.common.entity.BookReview;  // 导入图书评价实体类，对应数据库中的评价表
import com.bookstore.promotion.mapper.BookReviewMapper;  // 导入图书评价Mapper接口，用于操作评价表
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造方法
import org.springframework.beans.BeanUtils;  // 导入Spring的Bean工具类，用于对象属性拷贝
import org.springframework.stereotype.Service;  // 导入Spring的Service注解，标记为业务逻辑层组件
import org.springframework.transaction.annotation.Transactional;  // 导入事务注解，用于声明式事务管理

import java.util.List;  // 导入Java集合List，用于存储列表数据
import java.util.stream.Collectors;  // 导入Java Stream的Collectors工具，用于流式数据收集

/**
 * 图书评价业务服务类
 * 处理所有与图书评价相关的业务逻辑，包括：
 *   - 用户端：查看商品评价、提交评价、查看我的评价
 *   - 管理端：查看所有评价、屏蔽/取消屏蔽、置顶/取消置顶、回复评价、删除评价
 *
 * 使用 @Transactional 注解确保数据操作的一致性
 */
@Service  // 标记为Spring的Service组件，会被Spring容器自动扫描和管理
@RequiredArgsConstructor  // Lombok注解，自动为final字段生成构造方法，实现Spring的构造器注入
public class BookReviewService {  // 图书评价服务类

    private final BookReviewMapper bookReviewMapper;  // 注入图书评价Mapper，用于数据库操作

    /**
     * 获取某个商品的评价列表（分页）
     * 只返回未被屏蔽的评价，按创建时间倒序排列
     *
     * @param productId 商品ID
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @return 分页的评价视图对象列表
     */
    public PageResult<ReviewVO> getProductReviews(String productId, Integer pageNum, Integer pageSize) {  // 获取商品评价列表
        Page<BookReview> page = new Page<>(pageNum, pageSize);  // 创建分页对象，指定页码和每页数量
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.eq(BookReview::getProductId, productId)  // 添加条件：商品ID等于指定商品
                .eq(BookReview::getBlocked, 0) // 只显示未屏蔽的  // 添加条件：屏蔽状态等于0（未屏蔽）
                .orderByDesc(BookReview::getCreateTime);  // 按创建时间倒序排列（最新的评价排在前面）
        Page<BookReview> result = bookReviewMapper.selectPage(page, wrapper);  // 执行分页查询，返回查询结果
        List<ReviewVO> voList = result.getRecords().stream()  // 获取查询结果记录列表，并转为Stream流
                .map(this::convertToVO)  // 将每个BookReview实体转换为ReviewVO视图对象
                .collect(Collectors.toList());  // 将Stream流收集为List列表
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果对象返回
    }

    /**
     * 提交商品评价
     * 用户购买商品后可以对商品进行评价，新评价默认未屏蔽、0点赞、未置顶
     *
     * @param userId 用户ID
     * @param dto    评价提交数据（包含商品ID、评分、内容、图片等）
     */
    @Transactional  // 声明式事务，确保提交操作的原子性
    public void submitReview(String userId, ReviewSubmitDTO dto) {  // 提交评价方法
        BookReview review = new BookReview();  // 创建新的评价实体对象
        review.setUserId(userId);  // 设置用户ID
        review.setProductId(dto.getProductId());  // 设置商品ID
        review.setRating(dto.getRating());  // 设置评分（如1-5星）
        review.setContent(dto.getContent());  // 设置评价文字内容
        review.setImage(dto.getImage());  // 设置评价图片URL（可为空）
        review.setBlocked(0);  // 设置屏蔽状态为0（未屏蔽）
        review.setLikes(0);  // 设置点赞数为0（新评价没有点赞）
        review.setIsTop(0);  // 设置置顶状态为0（未置顶）
        bookReviewMapper.insert(review);  // 将评价记录插入数据库
    }

    /**
     * 获取用户的评价列表（分页）
     * 查询指定用户提交过的所有评价，按创建时间倒序排列
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页的评价视图对象列表
     */
    public PageResult<ReviewVO> getUserReviews(String userId, Integer pageNum, Integer pageSize) {  // 获取用户评价列表
        Page<BookReview> page = new Page<>(pageNum, pageSize);  // 创建分页对象，指定页码和每页数量
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        wrapper.eq(BookReview::getUserId, userId)  // 添加条件：用户ID等于指定用户
                .orderByDesc(BookReview::getCreateTime);  // 按创建时间倒序排列
        Page<BookReview> result = bookReviewMapper.selectPage(page, wrapper);  // 执行分页查询，返回查询结果
        List<ReviewVO> voList = result.getRecords().stream()  // 获取查询结果记录列表，并转为Stream流
                .map(this::convertToVO)  // 将每个BookReview实体转换为ReviewVO视图对象
                .collect(Collectors.toList());  // 将Stream流收集为List列表
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果对象返回
    }

    // 管理员接口

    /**
     * 获取所有评价列表（分页，管理员用）
     * 管理员可以查看所有评价，支持按屏蔽状态筛选
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param blocked  屏蔽状态筛选条件（null=查看全部，0=未屏蔽，1=已屏蔽）
     * @return 分页的评价视图对象列表
     */
    public PageResult<ReviewVO> getAllReviews(Integer pageNum, Integer pageSize, Integer blocked) {  // 管理员获取全部评价列表
        Page<BookReview> page = new Page<>(pageNum, pageSize);  // 创建分页对象，指定页码和每页数量
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda查询条件构造器
        if (blocked != null) {  // 如果传入了屏蔽状态筛选条件
            wrapper.eq(BookReview::getBlocked, blocked);  // 添加条件：屏蔽状态等于指定值
        }
        wrapper.orderByDesc(BookReview::getCreateTime);  // 按创建时间倒序排列
        Page<BookReview> result = bookReviewMapper.selectPage(page, wrapper);  // 执行分页查询，返回查询结果
        List<ReviewVO> voList = result.getRecords().stream()  // 获取查询结果记录列表，并转为Stream流
                .map(this::convertToVO)  // 将每个BookReview实体转换为ReviewVO视图对象
                .collect(Collectors.toList());  // 将Stream流收集为List列表
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果对象返回
    }

    /**
     * 屏蔽评价（管理员操作）
     * 将指定评价标记为已屏蔽，屏蔽后普通用户看不到该评价
     *
     * @param id 评价ID
     * @throws IllegalArgumentException 当评价不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保操作的原子性
    public void blockReview(Long id) {  // 屏蔽评价方法
        BookReview review = bookReviewMapper.selectById(id);  // 根据ID查询评价
        if (review == null) {  // 判断评价是否存在
            throw new IllegalArgumentException("评价不存在");  // 不存在则抛出异常
        }
        review.setBlocked(1);  // 将屏蔽状态设置为1（已屏蔽）
        bookReviewMapper.updateById(review);  // 更新数据库中的评价记录
    }

    /**
     * 取消屏蔽评价（管理员操作）
     * 将已屏蔽的评价恢复为可见状态
     *
     * @param id 评价ID
     * @throws IllegalArgumentException 当评价不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保操作的原子性
    public void unblockReview(Long id) {  // 取消屏蔽评价方法
        BookReview review = bookReviewMapper.selectById(id);  // 根据ID查询评价
        if (review == null) {  // 判断评价是否存在
            throw new IllegalArgumentException("评价不存在");  // 不存在则抛出异常
        }
        review.setBlocked(0);  // 将屏蔽状态设置为0（未屏蔽）
        bookReviewMapper.updateById(review);  // 更新数据库中的评价记录
    }

    /**
     * 置顶评价（管理员操作）
     * 将优质评价设置为置顶，置顶的评价会显示在最前面
     *
     * @param id 评价ID
     * @throws IllegalArgumentException 当评价不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保操作的原子性
    public void topReview(Long id) {  // 置顶评价方法
        BookReview review = bookReviewMapper.selectById(id);  // 根据ID查询评价
        if (review == null) {  // 判断评价是否存在
            throw new IllegalArgumentException("评价不存在");  // 不存在则抛出异常
        }
        review.setIsTop(1);  // 将置顶状态设置为1（已置顶）
        bookReviewMapper.updateById(review);  // 更新数据库中的评价记录
    }

    /**
     * 取消置顶评价（管理员操作）
     * 将已置顶的评价取消置顶状态
     *
     * @param id 评价ID
     * @throws IllegalArgumentException 当评价不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保操作的原子性
    public void untopReview(Long id) {  // 取消置顶评价方法
        BookReview review = bookReviewMapper.selectById(id);  // 根据ID查询评价
        if (review == null) {  // 判断评价是否存在
            throw new IllegalArgumentException("评价不存在");  // 不存在则抛出异常
        }
        review.setIsTop(0);  // 将置顶状态设置为0（未置顶）
        bookReviewMapper.updateById(review);  // 更新数据库中的评价记录
    }

    /**
     * 回复评价（管理员操作）
     * 管理员可以对用户的评价进行官方回复，回复内容会显示在评价详情中
     *
     * @param id    评价ID
     * @param reply 回复内容
     * @throws IllegalArgumentException 当评价不存在时抛出异常
     */
    @Transactional  // 声明式事务，确保操作的原子性
    public void replyReview(Long id, String reply) {  // 回复评价方法
        BookReview review = bookReviewMapper.selectById(id);  // 根据ID查询评价
        if (review == null) {  // 判断评价是否存在
            throw new IllegalArgumentException("评价不存在");  // 不存在则抛出异常
        }
        review.setReply(reply);  // 设置回复内容
        bookReviewMapper.updateById(review);  // 更新数据库中的评价记录
    }

    /**
     * 删除评价（管理员操作）
     * 物理删除指定的评价记录
     *
     * @param id 评价ID
     */
    @Transactional  // 声明式事务，确保删除操作的原子性
    public void deleteReview(Long id) {  // 删除评价方法
        bookReviewMapper.deleteById(id);  // 根据ID删除数据库中的评价记录
    }

    /**
     * 将评价实体对象转换为视图对象
     * 用于向前端返回数据时隐藏数据库内部结构
     *
     * @param review 评价实体对象
     * @return 评价视图对象
     */
    private ReviewVO convertToVO(BookReview review) {  // 私有方法，将实体转换为VO
        ReviewVO vo = new ReviewVO();  // 创建新的视图对象
        BeanUtils.copyProperties(review, vo);  // 将实体中的属性拷贝到视图对象中
        return vo;  // 返回转换后的视图对象
    }
}
