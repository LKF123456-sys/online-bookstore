package com.bookstore.order.controller;  // 声明当前类所在的包路径：订单控制器层

// 导入统一响应结果封装类，用于包装API的返回数据
import com.bookstore.common.api.Result;
// 导入订单创建的数据传输对象（DTO），包含创建订单需要的所有信息
import com.bookstore.common.api.dto.OrderCreateDTO;
// 导入订单视图对象（VO），用于向前端展示订单数据
import com.bookstore.common.api.vo.OrderVO;
// 导入分页结果封装类，用于包装分页查询的结果
import com.bookstore.common.api.vo.PageResult;
// 导入订单业务服务层
import com.bookstore.order.service.OrderService;
// 导入Jakarta的参数校验注解，用于自动验证请求参数的合法性
import jakarta.validation.Valid;
// 导入Lombok的注解，自动生成包含所有final字段的构造函数
import lombok.RequiredArgsConstructor;
// 导入Spring MVC的Web注解（@RestController, @RequestMapping等）
import org.springframework.web.bind.annotation.*;

/**
 * 订单API控制器（面向普通用户）
 * 提供订单相关的所有REST接口，包括：
 * - 创建订单
 * - 查询订单详情
 * - 查询订单列表（分页）
 * - 订单支付
 * - 取消订单
 * - 确认收货
 */
@RestController  // 标记为REST控制器，返回值会自动序列化为JSON格式
@RequestMapping("/api/order")  // 设置该控制器所有接口的URL前缀为 /api/order
@RequiredArgsConstructor  // Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
public class OrderApiController {  // 订单API控制器类

    private final OrderService orderService;  // 订单业务服务，通过构造函数注入，final表示初始化后不可修改

    /**
     * 创建订单
     * 用户确认购物车中的商品后，提交订单信息创建新订单
     * @param userId 从请求属性中获取的用户ID（由认证过滤器自动设置）
     * @param dto 订单创建DTO，包含收货地址、账单地址、商品列表等信息，@Valid会自动校验参数合法性
     * @return 创建成功的订单信息，包装在统一结果对象中
     */
    @PostMapping  // 处理POST请求，映射到 /api/order 路径
    public Result<OrderVO> createOrder(@RequestAttribute("userId") String userId, @Valid @RequestBody OrderCreateDTO dto) {  // 从请求属性获取userId，从请求体获取订单数据
        return Result.success(orderService.createOrder(userId, dto));  // 调用服务层创建订单，并将结果包装为成功响应返回
    }

    /**
     * 根据订单ID查询订单详情
     * @param userId 当前登录用户的ID，用于校验订单归属
     * @param id 订单ID，从URL路径中提取
     * @return 订单详情信息
     */
    @GetMapping("/{id}")  // 处理GET请求，{id}是路径变量，映射到 /api/order/{id}
    public Result<OrderVO> getOrderById(@RequestAttribute("userId") String userId, @PathVariable String id) {  // @PathVariable从URL中提取路径变量id
        return Result.success(orderService.getOrderById(userId, id));  // 调用服务层查询订单，传入用户ID和订单ID
    }

    /**
     * 查询当前用户的订单列表（支持分页和按状态筛选）
     * @param userId 当前登录用户的ID
     * @param pageNum 页码，默认为第1页
     * @param pageSize 每页显示条数，默认为10条
     * @param status 订单状态筛选条件（可选），如"待支付"、"已支付"等
     * @return 分页的订单列表
     */
    @GetMapping("/list")  // 处理GET请求，映射到 /api/order/list
    public Result<PageResult<OrderVO>> getOrderList(  // 返回分页的订单列表
            @RequestAttribute("userId") String userId,  // 从请求属性获取当前用户ID
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从URL查询参数获取页码，默认值为1
            @RequestParam(defaultValue = "10") Integer pageSize,  // 从URL查询参数获取每页大小，默认值为10
            @RequestParam(required = false) String status) {  // 从URL查询参数获取订单状态，此参数可选
        return Result.success(orderService.getOrderList(userId, pageNum, pageSize, status));  // 调用服务层进行分页查询
    }

    /**
     * 支付订单
     * 将"待支付"状态的订单标记为"已支付"
     * @param userId 当前登录用户的ID
     * @param id 要支付的订单ID
     * @return 操作成功返回空数据
     */
    @PostMapping("/{id}/pay")  // 处理POST请求，映射到 /api/order/{id}/pay
    public Result<Void> payOrder(  // 返回类型Void表示没有具体数据需要返回
            @RequestAttribute("userId") String userId,  // 从请求属性获取当前用户ID
            @PathVariable String id) {  // 从URL路径获取订单ID
        orderService.payOrder(userId, id);  // 调用服务层执行支付操作
        return Result.success();  // 返回操作成功的空响应
    }

    /**
     * 取消订单
     * 将"待支付"状态的订单标记为"已取消"，并恢复库存
     * @param userId 当前登录用户的ID
     * @param id 要取消的订单ID
     * @return 操作成功返回空数据
     */
    @PostMapping("/{id}/cancel")  // 处理POST请求，映射到 /api/order/{id}/cancel
    public Result<Void> cancelOrder(@RequestAttribute("userId") String userId, @PathVariable String id) {  // 获取用户ID和订单ID
        orderService.cancelOrder(userId, id);  // 调用服务层执行取消订单操作
        return Result.success();  // 返回操作成功的空响应
    }

    /**
     * 确认收货
     * 将"已发货"状态的订单标记为"已完成"
     * @param userId 当前登录用户的ID
     * @param id 要确认收货的订单ID
     * @return 操作成功返回空数据
     */
    @PostMapping("/{id}/confirm")  // 处理POST请求，映射到 /api/order/{id}/confirm
    public Result<Void> confirmReceive(@RequestAttribute("userId") String userId, @PathVariable String id) {  // 获取用户ID和订单ID
        orderService.confirmReceive(userId, id);  // 调用服务层执行确认收货操作
        return Result.success();  // 返回操作成功的空响应
    }
}
