package com.bookstore.order.controller;  // 声明当前类所在的包路径：购物车控制器层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入购物车添加商品的数据传输对象
import com.bookstore.common.api.dto.CartAddDTO;
// 导入购物车视图对象，用于向前端展示购物车数据
import com.bookstore.common.api.vo.CartVO;
// 导入购物车业务服务层
import com.bookstore.order.service.CartService;
// 导入Jakarta的参数校验注解
import jakarta.validation.Valid;
// 导入Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 导入Spring MVC的Web注解
import org.springframework.web.bind.annotation.*;

import java.util.Map;  // 导入Map集合类，用于接收JSON格式的请求体

/**
 * 购物车API控制器（面向普通用户）
 * 提供购物车相关的所有REST接口，包括：
 * - 获取购物车信息
 * - 添加商品到购物车
 * - 更新购物车商品数量（通过商品ID或购物车项ID）
 * - 从购物车移除商品（通过商品ID或购物车项ID）
 * - 清空购物车
 */
@RestController  // 标记为REST控制器，返回值自动序列化为JSON
@RequestMapping("/api/cart")  // 设置该控制器所有接口的URL前缀为 /api/cart
@RequiredArgsConstructor  // Lombok注解：自动生成包含所有final字段的构造函数
public class CartApiController {  // 购物车API控制器类

    private final CartService cartService;  // 购物车业务服务，通过构造函数注入

    /**
     * 获取当前用户的购物车信息
     * 会返回购物车中所有商品的详细信息（名称、价格、小计等）
     * @param userId 当前登录用户的ID
     * @return 购物车视图对象，包含商品列表和总数量
     */
    @GetMapping  // 处理GET请求，映射到 /api/cart
    public Result<CartVO> getCart(@RequestAttribute("userId") String userId) {  // 从请求属性获取用户ID
        return Result.success(cartService.getCart(userId));  // 调用服务层获取购物车数据并返回
    }

    /**
     * 添加商品到购物车
     * 如果商品已在购物车中，则增加数量；否则新增一条购物车项
     * @param userId 当前登录用户的ID
     * @param dto 购物车添加DTO，包含商品ID和数量，@Valid自动校验参数
     * @return 操作成功返回空数据
     */
    @PostMapping  // 处理POST请求，映射到 /api/cart
    public Result<Void> addToCart(@RequestAttribute("userId") String userId, @Valid @RequestBody CartAddDTO dto) {  // 从请求体获取购物车添加数据
        cartService.addToCart(userId, dto);  // 调用服务层添加商品到购物车
        return Result.success();  // 返回操作成功的空响应
    }

    /**
     * 通过商品ID更新购物车中商品的数量
     * 前端传入productId来定位购物车项，而不是直接传itemId
     * @param userId 当前登录用户的ID
     * @param body 请求体，包含productId（商品ID）和quantity（新数量）
     * @return 操作成功返回空数据
     */
    @PutMapping("/item")  // 处理PUT请求，映射到 /api/cart/item
    public Result<Void> updateQuantityByProductId(  // 通过商品ID更新数量的方法
            @RequestAttribute("userId") String userId,  // 从请求属性获取用户ID
            @RequestBody Map<String, Object> body) {  // 使用Map接收JSON请求体，灵活性更高
        String productId = (String) body.get("productId");  // 从Map中取出商品ID，强制转换为String类型
        Integer quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;  // 取出数量，如果为空默认为1；Number转换兼容前端可能传入的各种数字类型
        cartService.updateQuantityByProductId(userId, productId, quantity);  // 调用服务层更新数量
        return Result.success();  // 返回操作成功的空响应
    }

    /**
     * 通过商品ID从购物车中移除商品
     * @param userId 当前登录用户的ID
     * @param productId 要移除的商品ID，从URL路径中提取
     * @return 操作成功返回空数据
     */
    @DeleteMapping("/item/{productId}")  // 处理DELETE请求，映射到 /api/cart/item/{productId}
    public Result<Void> removeByProductId(@RequestAttribute("userId") String userId, @PathVariable String productId) {  // 获取用户ID和商品ID
        cartService.removeByProductId(userId, productId);  // 调用服务层根据商品ID移除购物车项
        return Result.success();  // 返回操作成功的空响应
    }

    /**
     * 通过购物车项ID更新商品数量
     * @param userId 当前登录用户的ID
     * @param id 购物车项ID，从URL路径中提取
     * @param quantity 新的数量，从URL查询参数中获取
     * @return 操作成功返回空数据
     */
    @PutMapping("/{id}/quantity")  // 处理PUT请求，映射到 /api/cart/{id}/quantity
    public Result<Void> updateQuantity(  // 通过购物车项ID更新数量
            @RequestAttribute("userId") String userId,  // 从请求属性获取用户ID
            @PathVariable String id,  // 从URL路径获取购物车项ID
            @RequestParam Integer quantity) {  // 从URL查询参数获取新数量
        cartService.updateQuantity(userId, id, quantity);  // 调用服务层更新数量
        return Result.success();  // 返回操作成功的空响应
    }

    /**
     * 通过购物车项ID从购物车中移除商品
     * @param userId 当前登录用户的ID
     * @param id 购物车项ID，从URL路径中提取
     * @return 操作成功返回空数据
     */
    @DeleteMapping("/{id}")  // 处理DELETE请求，映射到 /api/cart/{id}
    public Result<Void> removeFromCart(@RequestAttribute("userId") String userId, @PathVariable String id) {  // 获取用户ID和购物车项ID
        cartService.removeFromCart(userId, id);  // 调用服务层移除购物车项
        return Result.success();  // 返回操作成功的空响应
    }

    /**
     * 清空当前用户的购物车
     * 删除购物车中的所有商品项
     * @param userId 当前登录用户的ID
     * @return 操作成功返回空数据
     */
    @DeleteMapping("/clear")  // 处理DELETE请求，映射到 /api/cart/clear
    public Result<Void> clearCart(@RequestAttribute("userId") String userId) {  // 获取用户ID
        cartService.clearCart(userId);  // 调用服务层清空购物车
        return Result.success();  // 返回操作成功的空响应
    }
}
