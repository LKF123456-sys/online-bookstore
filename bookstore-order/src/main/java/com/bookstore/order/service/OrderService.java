package com.bookstore.order.service;  // 声明当前类所在的包路径：订单服务层

// 导入MyBatis-Plus的条件构造器，用于构建数据库查询条件
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 导入MyBatis-Plus的分页类
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// 导入统一响应结果封装类（用于判断Feign调用的返回结果）
import com.bookstore.common.api.Result;
// 导入订单创建的数据传输对象
import com.bookstore.common.api.dto.OrderCreateDTO;
// 导入订单项的数据传输对象
import com.bookstore.common.api.dto.OrderItemDTO;
// 导入订单项视图对象
import com.bookstore.common.api.vo.OrderItemVO;
// 导入订单视图对象
import com.bookstore.common.api.vo.OrderVO;
// 导入分页结果封装类
import com.bookstore.common.api.vo.PageResult;
// 导入商品视图对象（Feign远程调用返回的商品信息）
import com.bookstore.common.api.vo.ProductVO;
// 导入订单项实体类（对应数据库中的订单项表）
import com.bookstore.common.entity.OrderItem;
// 导入订单实体类（对应数据库中的订单表）
import com.bookstore.common.entity.Orders;
// 导入订单项的Mapper接口
import com.bookstore.order.mapper.OrderItemMapper;
// 导入订单的Mapper接口
import com.bookstore.order.mapper.OrdersMapper;
// 导入商品服务的Feign客户端（用于远程调用商品服务）
import com.bookstore.order.feign.ProductFeignClient;
// 导入订单消息生产者（用于发送异步消息到RabbitMQ）
import com.bookstore.order.mq.OrderMessageProducer;
// 导入Lombok注解，自动生成构造函数
import com.bookstore.common.util.SnowflakeIdGenerator;
// 导入Micrometer指标类，用于自定义监控指标
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
// 导入Spring的属性拷贝工具，用于对象之间的属性复制
import org.springframework.beans.BeanUtils;
// 导入Spring的Service注解，标记为业务层组件
import org.springframework.stereotype.Service;
// 导入Spring的事务注解，用于管理数据库事务
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;  // 导入高精度数字类型，用于处理金额计算
import java.time.LocalDateTime;  // 导入日期时间类
import java.time.format.DateTimeFormatter;  // 导入日期时间格式化器
import java.util.ArrayList;  // 导入ArrayList，用于记录已扣减库存的商品
import java.util.List;  // 导入List集合类
import java.util.Random;  // 导入随机数生成器
import java.util.stream.Collectors;  // 导入Stream流的收集器工具

import com.bookstore.common.exception.BusinessException;  // 导入自定义业务异常类

/**
 * 订单业务服务类
 * 处理所有订单相关的业务逻辑，包括：
 * - 创建订单（含Feign远程调用商品服务获取商品信息、扣减库存）
 * - 查询订单详情和订单列表
 * - 订单支付、取消、确认收货
 * - 管理员查看所有订单、发货操作
 *
 * 事务管理：涉及数据修改的方法都使用了@Transactional注解，
 * 保证数据库操作的原子性（要么全部成功，要么全部回滚）
 */
@Slf4j
@Service
public class OrderService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductFeignClient productFeignClient;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final OrderMessageProducer orderMessageProducer;  // 订单消息生产者，用于发送异步消息

    // ==================== 自定义监控指标 ====================
    private final Counter ordersCreatedCounter;     // 订单创建计数器
    private final Counter ordersPaidCounter;        // 订单支付计数器
    private final Counter ordersCancelledCounter;   // 订单取消计数器
    private final DistributionSummary ordersAmountSummary;  // 订单金额分布统计

    /**
     * 构造函数，通过参数注入所有依赖
     * @param ordersMapper 订单数据访问层
     * @param orderItemMapper 订单项数据访问层
     * @param productFeignClient 商品服务Feign客户端
     * @param snowflakeIdGenerator 雪花ID生成器
     * @param orderMessageProducer 订单消息生产者
     * @param meterRegistry Micrometer指标注册器，用于获取自定义Counter和Summary
     */
    public OrderService(OrdersMapper ordersMapper, OrderItemMapper orderItemMapper,
                        ProductFeignClient productFeignClient, SnowflakeIdGenerator snowflakeIdGenerator,
                        OrderMessageProducer orderMessageProducer, MeterRegistry meterRegistry) {
        this.ordersMapper = ordersMapper;
        this.orderItemMapper = orderItemMapper;
        this.productFeignClient = productFeignClient;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.orderMessageProducer = orderMessageProducer;
        // 从MeterRegistry中获取自定义指标Bean（在MetricsConfig中注册）
        this.ordersCreatedCounter = meterRegistry.counter("bookstore.orders.created");
        this.ordersPaidCounter = meterRegistry.counter("bookstore.orders.paid");
        this.ordersCancelledCounter = meterRegistry.counter("bookstore.orders.cancelled");
        this.ordersAmountSummary = meterRegistry.summary("bookstore.orders.amount");
    }

    /**
     * 创建订单
     * 完整流程：生成订单ID -> 设置订单基本信息 -> 遍历商品项获取商品信息 -> 校验库存 -> 创建订单项 -> 扣减库存 -> 计算总价 -> 保存订单
     * @param userId 用户ID
     * @param dto 订单创建信息（收货地址、账单地址、商品列表等）
     * @return 创建成功的订单视图对象
     * @throws IllegalArgumentException 当商品不存在或库存不足时抛出异常
     */
    @Transactional  // 开启事务：如果过程中任何一步失败，所有数据库操作都会回滚
    @Tags({
            @Tag(key = "order.userId", value = "arg[0]"),
            @Tag(key = "order.itemsCount", value = "arg[1].items.size()")
    })
    public OrderVO createOrder(String userId, OrderCreateDTO dto) {  // 创建订单的方法，接收用户ID和订单创建DTO
        // 创建订单实体对象，用于保存到数据库
        Orders order = new Orders();  // 实例化一个新的订单对象
        order.setOrderid(generateOrderId());  // 调用私有方法生成唯一的订单ID（时间戳+随机数）
        order.setUserid(userId);  // 设置订单所属用户ID
        order.setOrderdate(LocalDateTime.now());  // 设置订单日期为当前时间
        order.setStatus("待支付");  // 设置订单初始状态为"待支付"

        // 设置账单地址信息（发票/账单寄送地址）
        order.setBilltofirstname(dto.getBillToFirstName());  // 账单收件人名
        order.setBilltolastname(dto.getBillToLastName());  // 账单收件人姓
        order.setBilladdr1(dto.getBillAddr1());  // 账单地址第一行
        order.setBilladdr2(dto.getBillAddr2());  // 账单地址第二行（可选）
        order.setBillcity(dto.getBillCity());  // 账单所在城市
        order.setBillstate(dto.getBillState());  // 账单所在州/省
        order.setBillzip(dto.getBillZip());  // 账单邮编
        order.setBillcountry(dto.getBillCountry());  // 账单所在国家

        // 设置收货地址信息
        order.setShiptofirstname(dto.getShipToFirstName());  // 收货人名
        order.setShiptolastname(dto.getShipToLastName());  // 收货人姓
        order.setShipaddr1(dto.getShipAddr1());  // 收货地址第一行
        order.setShipaddr2(dto.getShipAddr2());  // 收货地址第二行（可选）
        order.setShipcity(dto.getShipCity());  // 收货城市
        order.setShipstate(dto.getShipState());  // 收货州/省
        order.setShipzip(dto.getShipZip());  // 收货邮编
        order.setShipcountry(dto.getShipCountry());  // 收货国家

        // 设置支付相关信息
        order.setCourier(dto.getCourier());  // 快递方式
        order.setCreditcard(dto.getCreditCard());  // 信用卡号
        order.setExprdate(dto.getExprDate());  // 信用卡过期日期
        order.setCardtype(dto.getCardType());  // 信用卡类型（如Visa、MasterCard等）
        order.setLocale(dto.getLocale());  // 地区设置

        // 设置优惠券信息
        order.setCouponname(dto.getCouponName());  // 优惠券名称
        if (dto.getDiscountAmount() != null) {  // 判断是否设置了优惠金额
            order.setDiscountamount(dto.getDiscountAmount());  // 设置优惠金额
        } else {
            order.setDiscountamount(BigDecimal.ZERO);  // 没有优惠则设为0
        }

        BigDecimal totalPrice = BigDecimal.ZERO;  // 初始化订单总价为0，用BigDecimal避免浮点数精度问题

        // ==================== 分布式事务补偿：记录已扣减库存的商品 ====================
        // 用于在本地事务回滚时，补偿已经通过Feign远程扣减的库存
        // Feign调用不受本地@Transactional管理，因此需要手动补偿
        List<String[]> deductedItems = new ArrayList<>();  // 记录已扣减的商品ID和数量，格式: [productId, quantity]

        try {
            // 处理订单中的商品项
            if (dto.getItems() != null && !dto.getItems().isEmpty()) {  // 判断订单中是否有商品项
                for (OrderItemDTO itemDTO : dto.getItems()) {  // 遍历每个订单项DTO
                    // 通过Feign远程调用商品服务，获取商品的详细信息
                    Result<ProductVO> result = productFeignClient.getProductById(itemDTO.getProductId());  // 远程调用商品服务获取商品信息
                    if (result == null || result.getData() == null) {  // 判断商品是否存在
                        throw new BusinessException(404, "商品不存在: " + itemDTO.getProductId());  // 商品不存在时抛出异常
                    }
                    ProductVO product = result.getData();  // 从响应中取出商品信息
                    if (product.getStock() < itemDTO.getQuantity()) {  // 判断商品库存是否充足
                        throw new BusinessException(409, "商品库存不足: " + product.getName());  // 库存不足时抛出异常
                    }

                    OrderItem item = new OrderItem();  // 创建订单项实体对象
                    item.setOrderId(order.getOrderid());  // 设置订单项所属的订单ID
                    item.setProductId(itemDTO.getProductId());  // 设置商品ID
                    item.setProductName(product.getName());  // 设置商品名称（从商品服务获取的真实名称）
                    item.setPrice(product.getPrice());  // 设置商品单价（从商品服务获取的真实价格）
                    item.setQuantity(itemDTO.getQuantity());  // 设置购买数量
                    totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));  // 累加总价：单价 × 数量

                    orderItemMapper.insert(item);  // 将订单项插入数据库

                    // 通过Feign远程调用商品服务，扣减对应商品的库存
                    productFeignClient.updateStock(itemDTO.getProductId(), itemDTO.getQuantity());  // 调用商品服务扣减库存
                    // 记录已扣减的商品，用于后续可能的补偿操作
                    deductedItems.add(new String[]{itemDTO.getProductId(), String.valueOf(itemDTO.getQuantity())});
                }
            }

            order.setOriginalprice(totalPrice);  // 设置订单原价（优惠前的总价）
            order.setTotalprice(totalPrice.subtract(order.getDiscountamount()));  // 设置订单实付价 = 原价 - 优惠金额

            // ==================== 关键：订单插入可能失败，但库存已经扣减 ====================
            // 此处用try-catch包裹，如果插入失败，需要补偿已扣减的库存
            ordersMapper.insert(order);  // 将订单插入数据库

        } catch (Exception e) {
            // ==================== 分布式事务补偿逻辑 ====================
            // 如果本地事务中任何步骤失败（如订单插入失败），但库存已通过Feign扣减，
            // 必须通过反向Feign调用来恢复库存，否则会导致库存数据不一致
            if (!deductedItems.isEmpty()) {
                log.error("创建订单失败，开始补偿已扣减的库存，订单ID: {}, 失败原因: {}",
                        order.getOrderid(), e.getMessage());
                for (String[] deducted : deductedItems) {  // 遍历所有已扣减的商品
                    String productId = deducted[0];  // 商品ID
                    int quantity = Integer.parseInt(deducted[1]);  // 扣减数量
                    try {
                        // 补偿操作：恢复已扣减的库存（传入负数表示增加库存）
                        productFeignClient.updateStock(productId, -quantity);
                        log.info("库存补偿成功: productId={}, 恢复数量={}", productId, quantity);
                    } catch (Exception compensateError) {
                        // 补偿操作也失败了，记录严重错误，需要人工介入
                        log.error("【严重】库存补偿失败，需要人工介入! productId={}, 需恢复数量={}, 错误: {}",
                                productId, quantity, compensateError.getMessage(), compensateError);
                    }
                }
            }
            // 重新抛出异常，让@Transactional回滚本地数据库操作（订单项插入等）
            if (e instanceof BusinessException) {
                throw e;  // 业务异常直接抛出
            }
            throw new BusinessException(500, "创建订单失败: " + e.getMessage());  // 其他异常包装为BusinessException
        }

        // 订单创建成功，增加监控计数器和金额统计
        ordersCreatedCounter.increment();  // 订单创建计数 +1
        ordersAmountSummary.record(order.getTotalprice().doubleValue());  // 记录订单金额到分布统计

        return convertToVO(order);  // 将订单实体转换为视图对象并返回
    }

    /**
     * 根据订单ID查询订单详情
     * @param userId 用户ID，如果为null则不校验订单归属（管理员使用）
     * @param orderId 订单ID
     * @return 订单视图对象
     * @throws IllegalArgumentException 当订单不存在时抛出异常
     */
    public OrderVO getOrderById(String userId, String orderId) {  // 查询单个订单的方法
        // 构建查询条件：按订单ID查询
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()  // 创建Lambda风格的查询条件构造器
                .eq(Orders::getOrderid, orderId);  // 添加条件：订单ID等于传入的orderId
        if (userId != null) {  // 如果传入了userId（非管理员场景）
            wrapper.eq(Orders::getUserid, userId);  // 额外添加条件：用户ID必须匹配，确保用户只能查看自己的订单
        }
        Orders order = ordersMapper.selectOne(wrapper);  // 执行数据库查询，返回单条记录
        if (order == null) {  // 如果查询结果为空
            throw new BusinessException(404, "订单不存在");  // 抛出异常提示订单不存在
        }
        return convertToVO(order);  // 将订单实体转换为视图对象并返回
    }

    /**
     * 分页查询指定用户的订单列表
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param status 订单状态筛选条件（可选）
     * @return 分页的订单视图对象列表
     */
    public PageResult<OrderVO> getOrderList(String userId, Integer pageNum, Integer pageSize, String status) {  // 查询订单列表的方法
        Page<Orders> page = new Page<>(pageNum, pageSize);  // 创建分页对象，指定页码和每页大小
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();  // 创建查询条件构造器
        wrapper.eq(Orders::getUserid, userId);  // 添加条件：只查询当前用户的订单
        if (status != null && !status.isEmpty()) {  // 如果传入了状态筛选条件
            wrapper.eq(Orders::getStatus, status);  // 添加条件：按订单状态筛选
        }
        wrapper.orderByDesc(Orders::getOrderdate);  // 按订单日期降序排列（最新的订单排在前面）
        Page<Orders> result = ordersMapper.selectPage(page, wrapper);  // 执行分页查询
        // 将查询结果中的实体对象列表转换为视图对象列表
        List<OrderVO> voList = result.getRecords().stream()  // 获取查询到的记录列表，并转为Stream流
                .map(this::convertToVO)  // 对每条记录调用convertToVO方法进行转换
                .collect(Collectors.toList());  // 将转换后的流收集为List集合
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 封装分页结果（包含数据列表、总记录数、页码、每页大小）
    }

    /**
     * 订单支付
     * 将"待支付"状态的订单更新为"已支付"
     * @param userId 用户ID
     * @param orderId 订单ID
     * @throws IllegalArgumentException 当订单不存在或状态不正确时抛出异常
     */
    @Transactional  // 开启事务
    @Tags({
            @Tag(key = "order.userId", value = "arg[0]"),
            @Tag(key = "order.orderId", value = "arg[1]")
    })
    public void payOrder(String userId, String orderId) {  // 订单支付方法
        // 查询当前用户的指定订单
        Orders order = ordersMapper.selectOne(  // 执行数据库查询
                new LambdaQueryWrapper<Orders>()  // 创建查询条件
                        .eq(Orders::getOrderid, orderId)  // 条件：订单ID匹配
                        .eq(Orders::getUserid, userId));  // 条件：用户ID匹配（确保只能支付自己的订单）
        if (order == null) {  // 如果订单不存在
            throw new BusinessException(404, "订单不存在");  // 抛出异常
        }
        if (!"待支付".equals(order.getStatus())) {  // 检查订单状态是否为"待支付"
            throw new BusinessException(400, "订单状态异常");  // 只有待支付的订单才能支付
        }
        order.setStatus("已支付");  // 更新订单状态为"已支付"
        ordersMapper.updateById(order);  // 根据订单主键更新数据库记录

        // 支付成功后增加监控计数
        ordersPaidCounter.increment();  // 订单支付计数 +1

        // 发送订单支付成功的异步消息到RabbitMQ，触发下游处理（通知、积分等）
        orderMessageProducer.sendOrderPaidMessage(orderId, userId);
    }

    /**
     * 取消订单
     * 将"待支付"状态的订单更新为"已取消"，并恢复商品库存
     * @param userId 用户ID
     * @param orderId 订单ID
     * @throws IllegalArgumentException 当订单不存在或状态不正确时抛出异常
     */
    @Transactional  // 开启事务：取消订单和恢复库存需要保持原子性
    @Tags({
            @Tag(key = "order.userId", value = "arg[0]"),
            @Tag(key = "order.orderId", value = "arg[1]")
    })
    public void cancelOrder(String userId, String orderId) {  // 取消订单方法
        // 查询当前用户的指定订单
        Orders order = ordersMapper.selectOne(  // 执行数据库查询
                new LambdaQueryWrapper<Orders>()  // 创建查询条件
                        .eq(Orders::getOrderid, orderId)  // 条件：订单ID匹配
                        .eq(Orders::getUserid, userId));  // 条件：用户ID匹配
        if (order == null) {  // 如果订单不存在
            throw new BusinessException(404, "订单不存在");  // 抛出异常
        }
        if (!"待支付".equals(order.getStatus())) {  // 检查订单状态是否为"待支付"
            throw new BusinessException(400, "只能取消待支付订单");  // 只有待支付的订单才能取消
        }
        order.setStatus("已取消");  // 更新订单状态为"已取消"
        ordersMapper.updateById(order);  // 根据订单主键更新数据库记录

        // 取消订单后需要恢复商品库存
        List<OrderItem> items = orderItemMapper.selectList(  // 查询该订单的所有订单项
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));  // 条件：订单ID匹配
        for (OrderItem item : items) {  // 遍历每个订单项
            try {
                // 调用商品服务恢复库存（传入负数表示增加库存）
                productFeignClient.updateStock(item.getProductId(), -item.getQuantity());
                log.info("取消订单恢复库存成功: orderId={}, productId={}, 恢复数量={}",
                        orderId, item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                // 库存恢复失败时记录详细错误信息，方便人工介入
                log.error("【警告】取消订单恢复库存失败，需要人工介入! orderId={}, productId={}, " +
                                "需恢复数量={}, 错误信息: {}",
                        orderId, item.getProductId(), item.getQuantity(), e.getMessage(), e);
                // 注意：此处不抛出异常中断流程，因为订单状态已经更新为"已取消"
                // 库存问题可以通过异步消息或定时任务来最终一致性修复
            }
        }

        // 增加监控计数
        ordersCancelledCounter.increment();  // 订单取消计数 +1

        // 发送订单取消的异步消息到RabbitMQ，由消费者异步处理库存恢复等后续操作
        orderMessageProducer.sendOrderCancelledMessage(orderId, userId);
    }

    /**
     * 确认收货
     * 将"已发货"状态的订单更新为"已完成"
     * @param userId 用户ID
     * @param orderId 订单ID
     * @throws IllegalArgumentException 当订单不存在或状态不正确时抛出异常
     */
    @Transactional  // 开启事务
    @Tags({
            @Tag(key = "order.userId", value = "arg[0]"),
            @Tag(key = "order.orderId", value = "arg[1]")
    })
    public void confirmReceive(String userId, String orderId) {  // 确认收货方法
        // 查询当前用户的指定订单
        Orders order = ordersMapper.selectOne(  // 执行数据库查询
                new LambdaQueryWrapper<Orders>()  // 创建查询条件
                        .eq(Orders::getOrderid, orderId)  // 条件：订单ID匹配
                        .eq(Orders::getUserid, userId));  // 条件：用户ID匹配
        if (order == null) {  // 如果订单不存在
            throw new BusinessException(404, "订单不存在");  // 抛出异常
        }
        if (!"已发货".equals(order.getStatus())) {  // 检查订单状态是否为"已发货"
            throw new BusinessException(400, "订单状态异常");  // 只有已发货的订单才能确认收货
        }
        order.setStatus("已完成");  // 更新订单状态为"已完成"
        ordersMapper.updateById(order);  // 根据订单主键更新数据库记录
    }

    /**
     * 获取所有订单列表（管理员接口）
     * 不限定用户，可以查看系统中所有订单
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 状态筛选（可选）
     * @return 分页的订单视图对象列表
     */
    // 管理员接口：查询所有用户的订单
    public PageResult<OrderVO> getAllOrders(Integer pageNum, Integer pageSize, String status) {  // 管理员查询所有订单的方法
        Page<Orders> page = new Page<>(pageNum, pageSize);  // 创建分页对象
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();  // 创建查询条件构造器
        if (status != null && !status.isEmpty()) {  // 如果传入了状态筛选条件
            wrapper.eq(Orders::getStatus, status);  // 按状态筛选
        }
        wrapper.orderByDesc(Orders::getOrderdate);  // 按订单日期降序排列
        Page<Orders> result = ordersMapper.selectPage(page, wrapper);  // 执行分页查询（不添加用户ID条件，查所有用户的订单）
        List<OrderVO> voList = result.getRecords().stream()  // 获取记录列表并转为Stream流
                .map(this::convertToVO)  // 转换为视图对象
                .collect(Collectors.toList());  // 收集为List
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 返回分页结果
    }

    /**
     * 订单发货（管理员接口）
     * 将"已支付"状态的订单更新为"已发货"
     * @param orderId 订单ID
     * @throws IllegalArgumentException 当订单不存在或状态不正确时抛出异常
     */
    @Transactional  // 开启事务
    public void shipOrder(String orderId) {  // 管理员发货方法
        Orders order = ordersMapper.selectById(orderId);  // 根据主键ID查询订单
        if (order == null) {  // 如果订单不存在
            throw new BusinessException(404, "订单不存在");  // 抛出异常
        }
        if (!"已支付".equals(order.getStatus())) {  // 检查订单状态是否为"已支付"
            throw new BusinessException(400, "订单状态异常");  // 只有已支付的订单才能发货
        }
        order.setStatus("已发货");  // 更新订单状态为"已发货"
        ordersMapper.updateById(order);  // 根据订单主键更新数据库记录
    }

    /**
     * 将订单实体对象转换为订单视图对象
     * 同时查询并填充该订单的所有订单项信息
     * @param order 订单实体对象
     * @return 订单视图对象（包含订单项列表）
     */
    private OrderVO convertToVO(Orders order) {  // 实体转视图对象的私有方法
        OrderVO vo = new OrderVO();  // 创建订单视图对象
        BeanUtils.copyProperties(order, vo);  // 使用Spring的工具类将实体属性复制到视图对象（同名属性自动映射）
        // 查询该订单的所有订单项
        List<OrderItem> items = orderItemMapper.selectList(  // 查询订单项列表
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getOrderid()));  // 条件：订单ID匹配
        // 将订单项实体列表转换为订单项视图对象列表，并补充商品图片信息
        vo.setItems(items.stream().map(item -> {  // 使用Stream流进行转换
            OrderItemVO itemVO = new OrderItemVO();  // 创建订单项视图对象
            BeanUtils.copyProperties(item, itemVO);  // 复制属性
            // 通过商品服务获取商品图片URL，补充到订单项VO中（OrderItem实体没有image字段）
            try {
                Result<ProductVO> productResult = productFeignClient.getProductById(item.getProductId());  // 远程调用获取商品信息
                if (productResult != null && productResult.getData() != null) {  // 判断返回结果是否有效
                    itemVO.setImage(productResult.getData().getImageUrl());  // 设置商品图片URL
                }
            } catch (Exception e) {  // 如果远程调用失败
                log.warn("获取商品图片失败, productId: {}", item.getProductId(), e);  // 记录警告日志，不影响订单返回
            }
            return itemVO;  // 返回转换后的视图对象
        }).collect(Collectors.toList()));  // 收集为List并设置到订单视图对象中
        return vo;  // 返回完整的订单视图对象
    }

    /**
     * 获取订单状态的中文文本
     * @param status 订单状态码
     * @return 中文状态描述
     */
    private String getStatusText(String status) {  // 状态码转中文的私有方法
        if (status == null) return "未知";  // 如果状态为null，返回"未知"
        switch (status) {  // 根据状态码进行匹配
            case "待支付": return "待支付";  // 待支付状态
            case "已支付": return "已支付";  // 已支付状态
            case "已发货": return "已发货";  // 已发货状态
            case "已完成": return "已完成";  // 已完成状态
            case "已取消": return "已取消";  // 已取消状态
            default: return status;  // 未知状态直接返回原始值
        }
    }

    /**
     * 生成唯一的订单ID
     * 格式：年月日时分秒 + 4位随机数
     * 例如：20240101123045 + 1234 = "202401011230451234"
     * @return 18位的订单ID字符串
     */
    /**
     * 生成全局唯一的订单号
     * 使用 Snowflake 算法，避免并发冲突
     */
    private String generateOrderId() {
        return snowflakeIdGenerator.nextOrderId("ORD");
    }
}
