// 声明当前类所属的包路径，遵循 Spring Boot 项目标准分包结构
package com.bookstore.admin.feign;

// 导入公共模块的统一响应封装类 Result，用于包装所有接口返回结果
import com.bookstore.common.api.Result;
// 导入 Spring Cloud OpenFeign 的 @FeignClient 注解，用于声明式 HTTP 客户端
import org.springframework.cloud.openfeign.FeignClient;
// 导入 Spring MVC 的请求映射注解（GetMapping、PostMapping、PutMapping、DeleteMapping、RequestHeader、PathVariable、RequestParam、RequestBody）
// 这些注解用于描述 HTTP 请求的 URL、方法与参数绑定方式
import org.springframework.web.bind.annotation.*;

// 导入 Java 集合框架的 Map 接口，用于灵活传递键值对数据
import java.util.Map;

/**
 * 订单服务 Feign 客户端 — 声明式调用 bookstore-order 微服务
 * <p>
 * 该接口通过 Spring Cloud OpenFeign 以声明方式定义对订单微服务的 HTTP 调用。
 * Feign 框架会在运行时动态生成代理实现类，自动完成服务发现、负载均衡与 HTTP 请求发送。
 * 所有方法的 URL 均以类级别 path="/api" 为前缀。
 * 涵盖订单、购物车两大业务域，以及管理后台操作。
 */
// @FeignClient 声明这是一个 Feign 客户端接口
//   - name = "bookstore-order"：指定要调用的微服务名称，对应 Nacos/注册中心中的服务名
//   - path = "/api"：指定所有方法 URL 的统一路径前缀
@FeignClient(name = "bookstore-order", path = "/api")
public interface OrderFeignClient {

    /**
     * 创建新订单
     * <p>
     * 调用 bookstore-order 微服务的 /api/order 接口
     * 请求方式：POST
     * 用户从购物车结算或直接购买时调用，微服务会校验库存、计算金额并生成订单记录
     *
     * @param userId    当前登录用户的 ID，通过请求头 X-User-Id 传递以标识下单用户
     * @param orderData 订单数据 Map，包含收货地址、商品列表、优惠券 ID、备注等信息
     * @return Result 包装的订单信息 Map，包含订单号、应付金额、订单状态等
     */
    // @PostMapping：将 HTTP POST 请求映射到 /order 路径，POST 语义表示创建新资源
    @PostMapping("/order")
    Result<Map<String, Object>> createOrder(
            // @RequestHeader("X-User-Id")：用户身份通过 HTTP 请求头传递，避免暴露在 URL 中
            @RequestHeader("X-User-Id") String userId,
            // @RequestBody：订单数据通过请求体以 JSON 格式传入
            @RequestBody Map<String, Object> orderData);

    /**
     * 获取用户订单列表（前台/用户端）
     * <p>
     * 调用 bookstore-order 微服务的 /api/order/list 接口
     * 请求方式：GET
     * 仅返回当前登录用户的订单，支持按状态筛选
     *
     * @param userId   当前登录用户的 ID，通过请求头 X-User-Id 传递
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 10
     * @param status   订单状态筛选（可选），如 "PENDING_PAY"（待支付）、"SHIPPED"（已发货）等
     * @return Result 包装的分页订单数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /order/list 路径
    @GetMapping("/order/list")
    Result<Map<String, Object>> userOrderList(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            // status 为可选筛选条件
            @RequestParam(required = false) String status);

    /**
     * 获取订单详情
     * <p>
     * 调用 bookstore-order 微服务的 /api/order/{orderId} 接口
     * 请求方式：GET
     *
     * @param userId  当前登录用户的 ID，通过请求头 X-User-Id 传递（允许为空，用于管理后台查询时也能复用）
     * @param orderId 订单号，作为 URL 路径变量
     * @return Result 包装的订单详情 Map，包含订单商品明细、物流信息、支付信息等
     */
    // @GetMapping：将 HTTP GET 请求映射到 /order/{orderId} 路径
    @GetMapping("/order/{orderId}")
    Result<Map<String, Object>> orderDetail(
            // required=false 表示该请求头允许不传，方便管理后台复用同一接口
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            // @PathVariable：将 URL 中的 {orderId} 绑定到方法参数
            @PathVariable String orderId);

    /**
     * 支付订单
     * <p>
     * 调用 bookstore-order 微服务的 /api/order/{orderId}/pay 接口
     * 请求方式：POST
     * 触发订单支付流程，通常对接第三方支付网关（微信支付、支付宝等）
     *
     * @param userId  当前登录用户的 ID，用于校验订单归属
     * @param orderId 订单号，作为 URL 路径变量
     * @return Result 包装的空返回体，成功时表示支付已发起或已完成
     */
    // @PostMapping：将 HTTP POST 请求映射到 /order/{orderId}/pay 路径
    @PostMapping("/order/{orderId}/pay")
    Result<Void> payOrder(@RequestHeader("X-User-Id") String userId,
                          @PathVariable String orderId);

    /**
     * 取消订单
     * <p>
     * 调用 bookstore-order 微服务的 /api/order/{orderId}/cancel 接口
     * 请求方式：POST
     * 仅允许取消待支付状态的订单，取消后会恢复商品库存
     *
     * @param userId  当前登录用户的 ID，用于校验订单归属
     * @param orderId 订单号，作为 URL 路径变量
     * @return Result 包装的空返回体
     */
    // @PostMapping：取消操作使用 POST 而非 DELETE，因为取消是一个业务动作而非纯资源删除
    @PostMapping("/order/{orderId}/cancel")
    Result<Void> cancelOrder(@RequestHeader("X-User-Id") String userId,
                             @PathVariable String orderId);

    /**
     * 确认收货
     * <p>
     * 调用 bookstore-order 微服务的 /api/order/{orderId}/confirm 接口
     * 请求方式：POST
     * 用户收到商品后点击确认收货，订单状态变更为"已完成"
     *
     * @param userId  当前登录用户的 ID，用于校验订单归属
     * @param orderId 订单号，作为 URL 路径变量
     * @return Result 包装的空返回体
     */
    // @PostMapping：确认收货是一个业务动作，使用 POST 语义
    @PostMapping("/order/{orderId}/confirm")
    Result<Void> confirmReceive(@RequestHeader("X-User-Id") String userId,
                                @PathVariable String orderId);

    // ===== 购物车相关接口 =====

    /**
     * 获取当前用户的购物车内容
     * <p>
     * 调用 bookstore-order 微服务的 /api/cart 接口
     * 请求方式：GET
     *
     * @param userId 当前登录用户的 ID，通过请求头 X-User-Id 传递
     * @return Result 包装的购物车数据 Map，包含购物车商品条目列表及总金额
     */
    // @GetMapping：将 HTTP GET 请求映射到 /cart 路径
    @GetMapping("/cart")
    Result<Map<String, Object>> getCart(@RequestHeader("X-User-Id") String userId);

    /**
     * 添加商品到购物车
     * <p>
     * 调用 bookstore-order 微服务的 /api/cart 接口
     * 请求方式：POST
     * 如果购物车中已存在相同 SKU，则累加数量
     *
     * @param userId   当前登录用户的 ID
     * @param cartItem 购物车条目数据 Map，包含 productId、skuId、quantity 等字段
     * @return Result 包装的空返回体
     */
    // @PostMapping：将 HTTP POST 请求映射到 /cart 路径
    @PostMapping("/cart")
    Result<Void> addToCart(@RequestHeader("X-User-Id") String userId,
                           @RequestBody Map<String, Object> cartItem);

    /**
     * 更新购物车中某一项的数量
     * <p>
     * 调用 bookstore-order 微服务的 /api/cart/{itemId} 接口
     * 请求方式：PUT
     *
     * @param userId 当前登录用户的 ID
     * @param itemId 购物车条目主键 ID，作为 URL 路径变量
     * @param update 更新数据 Map，主要包含 quantity（新数量）字段
     * @return Result 包装的空返回体
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /cart/{itemId} 路径，PUT 语义表示更新资源
    @PutMapping("/cart/{itemId}")
    Result<Void> updateCartItem(@RequestHeader("X-User-Id") String userId,
                                @PathVariable Long itemId,
                                @RequestBody Map<String, Object> update);

    /**
     * 删除购物车中的某一项
     * <p>
     * 调用 bookstore-order 微服务的 /api/cart/{itemId} 接口
     * 请求方式：DELETE
     *
     * @param userId 当前登录用户的 ID
     * @param itemId 购物车条目主键 ID，作为 URL 路径变量
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将 HTTP DELETE 请求映射到 /cart/{itemId} 路径，DELETE 语义表示删除资源
    @DeleteMapping("/cart/{itemId}")
    Result<Void> deleteCartItem(@RequestHeader("X-User-Id") String userId,
                                @PathVariable Long itemId);

    /**
     * 清空当前用户的购物车
     * <p>
     * 调用 bookstore-order 微服务的 /api/cart 接口
     * 请求方式：DELETE
     * 删除该用户购物车中的所有条目
     *
     * @param userId 当前登录用户的 ID
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将 HTTP DELETE 请求映射到 /cart 路径，不指定 itemId 表示清空整个购物车
    @DeleteMapping("/cart")
    Result<Void> clearCart(@RequestHeader("X-User-Id") String userId);

    // ===== 以下为管理后台专用接口 =====

    /**
     * 管理后台 — 获取所有订单分页列表
     * <p>
     * 调用 bookstore-order 微服务的 /api/admin/order/list 接口
     * 请求方式：GET
     * 管理后台可查看所有用户的订单，支持按状态和关键词筛选
     *
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 10
     * @param status   订单状态筛选（可选）
     * @param keyword  搜索关键词（可选），用于按订单号或收货人模糊匹配
     * @return Result 包装的分页订单数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /admin/order/list 路径
    @GetMapping("/admin/order/list")
    Result<Map<String, Object>> adminOrderList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword);

    /**
     * 管理后台 — 更新订单状态
     * <p>
     * 调用 bookstore-order 微服务的 /api/admin/order/{orderId}/status 接口
     * 请求方式：PUT
     * 用于管理员手动更改订单状态（如标记为已发货、已完成等）
     *
     * @param orderId      订单号，作为 URL 路径变量
     * @param statusUpdate 状态更新数据 Map，包含目标状态及相关操作说明
     * @return Result 包装的空返回体
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /admin/order/{orderId}/status 路径
    @PutMapping("/admin/order/{orderId}/status")
    Result<Void> updateOrderStatus(@PathVariable String orderId,
                                   @RequestBody Map<String, Object> statusUpdate);

    /**
     * 管理后台 — 获取订单详情
     * <p>
     * 调用 bookstore-order 微服务的 /api/admin/order/{orderId} 接口
     * 请求方式：GET
     * 与前台订单详情接口的区别在于无需传递用户 ID 进行归属校验
     *
     * @param orderId 订单号，作为 URL 路径变量
     * @return Result 包装的订单详情 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /admin/order/{orderId} 路径
    @GetMapping("/admin/order/{orderId}")
    Result<Map<String, Object>> adminOrderDetail(@PathVariable String orderId);
}
