package com.bookstore.order.feign;  // 声明当前接口所在的包路径：Feign远程调用客户端层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入商品视图对象
import com.bookstore.common.api.vo.ProductVO;
// 导入Spring Cloud的FeignClient注解，用于声明这是一个Feign远程调用客户端
import org.springframework.cloud.openfeign.FeignClient;
// 导入Spring MVC的请求映射注解
import org.springframework.web.bind.annotation.*;

/**
 * 商品服务Feign客户端接口
 * 通过Spring Cloud OpenFeign实现远程调用商品服务（bookstore-product）的API。
 * Feign会根据接口方法上的注解自动生成HTTP请求，就像调用本地方法一样调用远程服务。
 *
 * 注意：
 * - name属性指定了要调用的微服务名称（在Nacos中注册的服务名）
 * - fallbackFactory指定了降级工厂类，当远程服务不可用时会执行降级逻辑
 */
@FeignClient(name = "bookstore-product", fallbackFactory = ProductFeignFallbackFactory.class)  // 声明Feign客户端：调用名为bookstore-product的服务，降级处理由ProductFeignFallbackFactory提供
public interface ProductFeignClient {  // Feign客户端接口，不需要实现类，Feign会自动生成代理对象

    /**
     * 根据商品ID获取商品信息
     * 远程调用商品服务的 GET /api/product/{id} 接口
     * @param id 商品ID
     * @return 商品信息，包装在统一结果对象中
     */
    @GetMapping("/api/product/{id}")  // 映射为GET请求，URL为 /api/product/{id}
    Result<ProductVO> getProductById(@PathVariable("id") String id);  // @PathVariable将方法参数绑定到URL路径变量

    /**
     * 更新商品库存（扣减或恢复）
     * 远程调用商品服务的 PUT /api/product/{id}/stock 接口
     * @param id 商品ID
     * @param quantity 数量（正数表示扣减库存，负数表示恢复库存）
     */
    @PutMapping("/api/product/{id}/stock")  // 映射为PUT请求，URL为 /api/product/{id}/stock
    void updateStock(@PathVariable("id") String id, @RequestParam("quantity") Integer quantity);  // @RequestParam将参数拼接到URL查询字符串中
}
