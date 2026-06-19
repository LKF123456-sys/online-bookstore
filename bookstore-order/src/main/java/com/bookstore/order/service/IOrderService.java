package com.bookstore.order.service;

import com.bookstore.common.api.dto.OrderCreateDTO;
import com.bookstore.common.api.vo.OrderVO;
import com.bookstore.common.api.vo.PageResult;

/**
 * 订单业务服务接口
 * 定义所有订单相关的业务操作，包括创建、查询、支付、取消、确认收货和管理员操作
 */
public interface IOrderService {

    /**
     * 创建订单（含 Feign 远程调用商品服务获取商品信息、扣减库存）
     * @param userId 用户ID
     * @param dto 订单创建信息
     * @return 创建成功的订单视图对象
     */
    OrderVO createOrder(String userId, OrderCreateDTO dto);

    /**
     * 根据订单ID查询订单详情
     * @param userId 用户ID（null 表示不校验归属，管理员使用）
     * @param orderId 订单ID
     * @return 订单视图对象
     */
    OrderVO getOrderById(String userId, String orderId);

    /**
     * 分页查询指定用户的订单列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 订单状态筛选（可选）
     * @return 分页的订单视图对象列表
     */
    PageResult<OrderVO> getOrderList(String userId, Integer pageNum, Integer pageSize, String status);

    /**
     * 订单支付
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    void payOrder(String userId, String orderId);

    /**
     * 取消订单，并恢复商品库存
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    void cancelOrder(String userId, String orderId);

    /**
     * 确认收货
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    void confirmReceive(String userId, String orderId);

    /**
     * 获取所有订单列表（管理员接口）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 状态筛选（可选）
     * @return 分页的订单视图对象列表
     */
    PageResult<OrderVO> getAllOrders(Integer pageNum, Integer pageSize, String status);

    /**
     * 订单发货（管理员接口）
     * @param orderId 订单ID
     */
    void shipOrder(String orderId);
}
