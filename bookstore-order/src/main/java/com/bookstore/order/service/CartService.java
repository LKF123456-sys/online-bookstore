package com.bookstore.order.service;  // 声明当前类所在的包路径：购物车服务层

// 导入MyBatis-Plus的条件构造器，用于构建数据库查询条件
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 导入统一响应结果封装类（用于判断Feign调用的返回结果）
import com.bookstore.common.api.Result;
// 导入购物车添加商品的数据传输对象
import com.bookstore.common.api.dto.CartAddDTO;
// 导入购物车项视图对象，用于向前端展示单个购物车项
import com.bookstore.common.api.vo.CartItemVO;
// 导入购物车视图对象，用于向前端展示整个购物车
import com.bookstore.common.api.vo.CartVO;
// 导入商品视图对象（Feign远程调用返回的商品信息）
import com.bookstore.common.api.vo.ProductVO;
// 导入购物车实体类（对应数据库中的购物车表）
import com.bookstore.common.entity.Cart;
// 导入业务异常类
import com.bookstore.common.exception.BusinessException;
// 导入购物车项实体类（对应数据库中的购物车项表）
import com.bookstore.common.entity.CartItem;
// 导入购物车项的Mapper接口
import com.bookstore.order.mapper.CartItemMapper;
// 导入购物车的Mapper接口
import com.bookstore.order.mapper.CartMapper;
// 导入商品服务的Feign客户端（用于远程调用商品服务）
import com.bookstore.order.feign.ProductFeignClient;
// 导入雪花ID生成器，用于生成全局唯一的购物车项ID
import com.bookstore.common.util.SnowflakeIdGenerator;
// 导入Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 导入Spring的属性拷贝工具
import org.springframework.beans.BeanUtils;
// 导入Spring的Service注解
import org.springframework.stereotype.Service;
// 导入Spring的事务注解
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;  // 导入高精度数字类型，用于金额计算
import java.util.List;  // 导入List集合类
import java.util.stream.Collectors;  // 导入Stream流的收集器

/**
 * 购物车业务服务类
 * 处理所有购物车相关的业务逻辑，包括：
 * - 获取购物车信息（含商品详细信息）
 * - 添加商品到购物车（新增或累加数量）
 * - 更新购物车商品数量（支持按购物车项ID或商品ID）
 * - 从购物车移除商品
 * - 清空购物车
 */
@Service  // 标记为Spring的Service层组件
@RequiredArgsConstructor  // Lombok注解：自动生成包含所有final字段的构造函数
public class CartService {  // 购物车服务类

    private final CartItemMapper cartItemMapper;  // 购物车项Mapper，用于操作购物车项表
    private final CartMapper cartMapper;  // 购物车Mapper，用于操作购物车表
    private final ProductFeignClient productFeignClient;  // 商品服务Feign客户端，用于远程获取商品信息
    private final SnowflakeIdGenerator snowflakeIdGenerator;  // 雪花ID生成器，用于生成全局唯一的购物车项ID

    /**
     * 获取购物车信息
     * 查询购物车中的所有商品项，并通过Feign远程调用获取每个商品的最新信息（名称、价格、图片等）
     * @param cartId 购物车ID（在本系统中等同于用户ID）
     * @return 购物车视图对象，包含商品列表和统计信息
     */
    public CartVO getCart(String cartId) {  // 获取购物车信息的方法
        // 根据购物车ID查询所有购物车项
        List<CartItem> items = cartItemMapper.selectList(  // 查询购物车项列表
                new LambdaQueryWrapper<CartItem>()  // 创建查询条件
                        .eq(CartItem::getCartid, cartId));  // 条件：购物车ID匹配

        CartVO cartVO = new CartVO();  // 创建购物车视图对象
        // 将每个购物车项转换为视图对象，并补充商品详细信息
        List<CartItemVO> itemVOs = items.stream().map(item -> {  // 使用Stream流遍历转换
            CartItemVO vo = new CartItemVO();  // 创建购物车项视图对象
            vo.setItemid(item.getItemid());  // 设置购物车项ID
            vo.setCartid(item.getCartid());  // 设置所属购物车ID
            vo.setProductId(item.getProductId());  // 设置商品ID
            vo.setQuantity(item.getQuantity());  // 设置购买数量
            // 通过Feign远程调用商品服务，获取商品的最新信息
            try {
                Result<ProductVO> pr = productFeignClient.getProductById(item.getProductId());  // 远程调用获取商品信息
                if (pr != null && pr.getData() != null) {  // 判断返回结果是否有效
                    ProductVO p = pr.getData();  // 取出商品信息
                    vo.setName(p.getName());  // 设置商品名称
                    vo.setPrice(p.getPrice());  // 设置商品单价
                    vo.setImageUrl(p.getImageUrl());  // 设置商品图片URL
                    vo.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));  // 计算小计 = 单价 × 数量
                }
            } catch (Exception e) {  // 如果远程调用失败（网络异常、服务不可用等）
                vo.setName("商品信息获取失败");  // 设置错误提示名称
                vo.setPrice(BigDecimal.ZERO);  // 价格设为0
                vo.setSubtotal(BigDecimal.ZERO);  // 小计设为0
            }
            return vo;  // 返回转换后的视图对象
        }).collect(Collectors.toList());  // 收集为List集合

        cartVO.setItems(itemVOs);  // 设置购物车的商品项列表
        cartVO.setTotalCount(itemVOs.stream().mapToInt(CartItemVO::getQuantity).sum());  // 设置购物车中的商品总数量（所有商品数量之和）

        return cartVO;  // 返回购物车视图对象
    }

    /**
     * 添加商品到购物车
     * 如果购物车不存在则自动创建；如果商品已在购物车中则增加数量
     * @param cartId 购物车ID
     * @param dto 添加商品信息（商品ID和数量）
     */
    @Transactional  // 开启事务
    public void addToCart(String cartId, CartAddDTO dto) {  // 添加商品到购物车的方法
        // 确保购物车存在，如果不存在则自动创建
        if (cartMapper.selectById(cartId) == null) {  // 根据ID查询购物车，如果不存在
            Cart cart = new Cart();  // 创建新的购物车实体
            cart.setCartid(cartId);  // 设置购物车ID
            cart.setUserid(cartId);  // 设置用户ID（购物车ID和用户ID相同）
            cartMapper.insert(cart);  // 将新购物车插入数据库
        }

        // 通过Feign远程调用商品服务，验证商品是否存在
        Result<ProductVO> result = productFeignClient.getProductById(dto.getProductId());  // 获取商品信息
        if (result == null || result.getData() == null) {  // 如果商品不存在
            throw new BusinessException(404, "商品不存在");  // 抛出异常
        }

        // 检查该商品是否已经在购物车中
        CartItem existingItem = cartItemMapper.selectOne(  // 查询是否已存在该商品的购物车项
                new LambdaQueryWrapper<CartItem>()  // 创建查询条件
                        .eq(CartItem::getCartid, cartId)  // 条件：购物车ID匹配
                        .eq(CartItem::getProductId, dto.getProductId()));  // 条件：商品ID匹配

        if (existingItem != null) {  // 如果商品已在购物车中
            existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());  // 在原数量基础上增加
            cartItemMapper.updateById(existingItem);  // 更新数据库记录
        } else {  // 如果商品不在购物车中
            CartItem item = new CartItem();  // 创建新的购物车项实体
            item.setItemid(snowflakeIdGenerator.nextOrderId("CI"));  // 使用雪花算法生成全局唯一的购物车项ID（替代UUID截取，避免碰撞）
            item.setCartid(cartId);  // 设置所属购物车ID
            item.setProductId(dto.getProductId());  // 设置商品ID
            item.setQuantity(dto.getQuantity());  // 设置购买数量
            cartItemMapper.insert(item);  // 将新购物车项插入数据库
        }
    }

    /**
     * 通过购物车项ID更新商品数量
     * 如果数量小于等于0，则删除该购物车项
     * @param cartId 购物车ID
     * @param itemId 购物车项ID
     * @param quantity 新的数量
     */
    @Transactional  // 开启事务
    public void updateQuantity(String cartId, String itemId, Integer quantity) {  // 更新购物车项数量的方法
        CartItem item = cartItemMapper.selectOne(  // 查询指定的购物车项
                new LambdaQueryWrapper<CartItem>()  // 创建查询条件
                        .eq(CartItem::getItemid, itemId)  // 条件：购物车项ID匹配
                        .eq(CartItem::getCartid, cartId));  // 条件：购物车ID匹配（安全校验）
        if (item == null) {  // 如果购物车项不存在
            throw new BusinessException(404, "购物车项不存在");  // 抛出异常
        }
        if (quantity <= 0) {  // 如果数量小于等于0
            cartItemMapper.deleteById(itemId);  // 删除该购物车项（相当于移除商品）
        } else {  // 如果数量大于0
            item.setQuantity(quantity);  // 更新数量
            cartItemMapper.updateById(item);  // 更新数据库记录
        }
    }

    /**
     * 通过购物车项ID从购物车中移除商品
     * @param cartId 购物车ID
     * @param itemId 购物车项ID
     */
    @Transactional  // 开启事务
    public void removeFromCart(String cartId, String itemId) {  // 移除购物车项的方法
        CartItem item = cartItemMapper.selectOne(  // 查询指定的购物车项
                new LambdaQueryWrapper<CartItem>()  // 创建查询条件
                        .eq(CartItem::getItemid, itemId)  // 条件：购物车项ID匹配
                        .eq(CartItem::getCartid, cartId));  // 条件：购物车ID匹配
        if (item == null) {  // 如果购物车项不存在
            throw new BusinessException(404, "购物车项不存在");  // 抛出异常
        }
        cartItemMapper.deleteById(itemId);  // 根据ID删除购物车项
    }

    /**
     * 清空购物车
     * 删除指定购物车中的所有商品项
     * @param cartId 购物车ID
     */
    @Transactional  // 开启事务
    public void clearCart(String cartId) {  // 清空购物车的方法
        cartItemMapper.delete(  // 批量删除购物车项
                new LambdaQueryWrapper<CartItem>()  // 创建删除条件
                        .eq(CartItem::getCartid, cartId));  // 条件：购物车ID匹配，删除该购物车下所有项
    }

    /**
     * 通过商品ID更新购物车中商品的数量
     * 与updateQuantity不同的是，这里通过商品ID而非购物车项ID来定位
     * @param cartId 购物车ID
     * @param productId 商品ID
     * @param quantity 新的数量
     */
    @Transactional  // 开启事务
    public void updateQuantityByProductId(String cartId, String productId, Integer quantity) {  // 通过商品ID更新数量
        CartItem item = cartItemMapper.selectOne(  // 查询购物车中该商品的购物车项
                new LambdaQueryWrapper<CartItem>()  // 创建查询条件
                        .eq(CartItem::getProductId, productId)  // 条件：商品ID匹配
                        .eq(CartItem::getCartid, cartId));  // 条件：购物车ID匹配
        if (item == null) {  // 如果购物车中没有该商品
            throw new BusinessException(404, "购物车项不存在");  // 抛出异常
        }
        if (quantity <= 0) {  // 如果数量小于等于0
            cartItemMapper.deleteById(item.getItemid());  // 删除该购物车项
        } else {  // 如果数量大于0
            item.setQuantity(quantity);  // 更新数量
            cartItemMapper.updateById(item);  // 更新数据库记录
        }
    }

    /**
     * 通过商品ID从购物车中移除商品
     * @param cartId 购物车ID
     * @param productId 商品ID
     */
    @Transactional  // 开启事务
    public void removeByProductId(String cartId, String productId) {  // 通过商品ID移除购物车项
        CartItem item = cartItemMapper.selectOne(  // 查询购物车中该商品的购物车项
                new LambdaQueryWrapper<CartItem>()  // 创建查询条件
                        .eq(CartItem::getProductId, productId)  // 条件：商品ID匹配
                        .eq(CartItem::getCartid, cartId));  // 条件：购物车ID匹配
        if (item != null) {  // 如果找到了该购物车项
            cartItemMapper.deleteById(item.getItemid());  // 根据购物车项ID删除
        }
    }
}
