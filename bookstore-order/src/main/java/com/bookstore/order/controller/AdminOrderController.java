package com.bookstore.order.controller;  // 声明当前类所在的包路径：管理端控制器层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入订单视图对象
import com.bookstore.common.api.vo.OrderVO;
// 导入分页结果封装类
import com.bookstore.common.api.vo.PageResult;
// 导入订单业务服务层
import com.bookstore.order.service.OrderService;
// 导入Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 导入Spring MVC的Web注解
import org.springframework.web.bind.annotation.*;

/**
 * 管理端订单控制器（面向管理员）
 * 提供管理员操作订单的REST接口，包括：
 * - 查看所有订单列表（分页）
 * - 查看任意订单详情
 * - 订单发货操作
 *
 * 与OrderApiController的区别：
 * - 该控制器不校验用户身份，管理员可以操作所有订单
 * - URL前缀为 /admin/order，而用户端为 /api/order
 */
@RestController  // 标记为REST控制器
@RequestMapping("/admin/order")  // 设置URL前缀为 /admin/order，与用户端接口区分
@RequiredArgsConstructor  // Lombok注解：自动生成构造函数实现依赖注入
public class AdminOrderController {  // 管理端订单控制器类

    private final OrderService orderService;  // 订单业务服务，通过构造函数注入

    /**
     * 获取所有订单列表（管理员视角）
     * 管理员可以查看系统中所有用户的订单，不限定用户
     * @param pageNum 页码，默认为第1页
     * @param pageSize 每页显示条数，默认为10条
     * @param status 订单状态筛选条件（可选）
     * @return 分页的订单列表
     */
    @GetMapping("/list")  // 处理GET请求，映射到 /admin/order/list
    public Result<PageResult<OrderVO>> getOrderList(  // 返回分页的订单列表
            @RequestParam(defaultValue = "1") Integer pageNum,  // 从URL查询参数获取页码，默认为1
            @RequestParam(defaultValue = "10") Integer pageSize,  // 从URL查询参数获取每页大小，默认为10
            @RequestParam(required = false) String status) {  // 订单状态筛选，可选参数
        return Result.success(orderService.getAllOrders(pageNum, pageSize, status));  // 调用服务层获取所有订单
    }

    /**
     * 根据订单ID查看订单详情（管理员视角）
     * 管理员传入null作为userId，服务层不会校验订单归属
     * @param id 订单ID，从URL路径中提取
     * @return 订单详情信息
     */
    @GetMapping("/{id}")  // 处理GET请求，映射到 /admin/order/{id}
    public Result<OrderVO> getOrderById(@PathVariable String id) {  // 从URL路径获取订单ID
        // 管理员可以查看任何订单，传入null作为userId表示不校验归属
        return Result.success(orderService.getOrderById(null, id));  // userId传null，服务层会跳过用户校验
    }

    /**
     * 订单发货操作
     * 将"已支付"状态的订单标记为"已发货"
     * @param id 要发货的订单ID
     * @return 操作成功返回空数据
     */
    @PostMapping("/{id}/ship")  // 处理POST请求，映射到 /admin/order/{id}/ship
    public Result<Void> shipOrder(@PathVariable String id) {  // 从URL路径获取订单ID
        orderService.shipOrder(id);  // 调用服务层执行发货操作
        return Result.success();  // 返回操作成功的空响应
    }
}
