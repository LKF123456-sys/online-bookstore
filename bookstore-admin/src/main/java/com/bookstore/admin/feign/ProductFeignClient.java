package com.bookstore.admin.feign; // 声明当前接口所属的包路径，遵循Spring Boot项目标准分包结构

// 导入公共模块的统一响应封装类Result，用于包装所有接口返回结果
import com.bookstore.common.api.Result;
// 导入商品视图对象ProductVO，用于展示商品简要信息
import com.bookstore.common.api.vo.ProductVO;
// 导入Spring Cloud OpenFeign的@FeignClient注解，用于声明式HTTP客户端
import org.springframework.cloud.openfeign.FeignClient;
// 导入Spring MVC的请求映射注解，用于描述HTTP请求的URL、方法与参数绑定方式
import org.springframework.web.bind.annotation.*;

import java.util.List; // 导入Java集合框架的List接口
import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 商品服务 Feign 客户端 — 声明式调用 bookstore-product 微服务
 *
 * 该接口通过 Spring Cloud OpenFeign 以声明方式定义对商品微服务的 HTTP 调用。
 * Feign 框架会在运行时动态生成代理实现类，自动完成服务发现、负载均衡与 HTTP 请求发送。
 * 涵盖商品列表、详情、搜索、推荐、热销、分类以及管理后台的商品CRUD操作。
 *
 * @FeignClient 注解说明：
 *   - name = "bookstore-product"：指定要调用的微服务名称，对应Nacos注册中心中的服务名
 *   - path = "/api"：指定所有方法URL的统一路径前缀，即完整URL = /api + 方法上声明的路径
 */
@FeignClient(name = "bookstore-product", path = "/api", fallbackFactory = ProductFeignFallbackFactory.class) // 声明Feign客户端，目标服务为bookstore-product，统一路径前缀为/api，熔断降级工厂为ProductFeignFallbackFactory
public interface ProductFeignClient {

    /**
     * 获取商品分页列表（前台/用户端）
     *
     * @param pageNum    页码（从1开始），默认值为1
     * @param pageSize   每页条数，默认值为12
     * @param keyword    搜索关键词（可选），用于模糊匹配商品名称
     * @param categoryId 分类ID（可选），用于按分类筛选商品
     * @param sort       排序方式（可选），如"price_asc"价格升序等
     * @return Result 包装的分页商品数据Map，包含records（商品列表）、total（总记录数）等
     */
    // @GetMapping：将HTTP GET请求映射到/product/list路径，与类级别path="/api"拼接后完整路径为/api/product/list
    @GetMapping("/product/list")
    Result<Map<String, Object>> list(
            // @RequestParam：将URL查询参数pageNum绑定到方法参数，defaultValue="1"表示缺省时默认为1
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：将URL查询参数pageSize绑定到方法参数，默认每页12条
            @RequestParam(defaultValue = "12") int pageSize,
            // @RequestParam(required = false)：keyword为可选查询参数，未传递时为null
            @RequestParam(required = false) String keyword,
            // @RequestParam(required = false)：分类ID为可选参数
            @RequestParam(required = false) Long categoryId,
            // @RequestParam(required = false)：排序方式为可选参数
            @RequestParam(required = false) String sort);

    /**
     * 获取商品详情
     *
     * @param id 商品主键ID，作为URL路径变量
     * @return Result 包装的商品详情Map，包含商品基本信息、描述、图片等
     */
    // @GetMapping：将HTTP GET请求映射到/product/{id}路径，{id}为路径变量占位符
    @GetMapping("/product/{id}")
    // @PathVariable：将URL路径中的{id}占位符的值绑定到方法参数id
    Result<Map<String, Object>> detail(@PathVariable String id);

    /**
     * 获取推荐商品列表
     * 通常基于销量、评分等指标排序，返回指定数量的热门推荐商品
     *
     * @param limit 返回商品数量上限，默认值为8
     * @return Result 包装的ProductVO列表
     */
    // @GetMapping：将HTTP GET请求映射到/product/recommend路径
    @GetMapping("/product/recommend")
    // @RequestParam：限制返回数量，默认为8
    Result<List<ProductVO>> recommend(@RequestParam(defaultValue = "8") int limit);

    /**
     * 获取热销商品列表
     * 按销量排序，返回当前最热卖的商品
     *
     * @param limit 返回商品数量上限，默认值为5
     * @return Result 包装的ProductVO列表
     */
    // @GetMapping：将HTTP GET请求映射到/product/hot路径
    @GetMapping("/product/hot")
    // @RequestParam：限制返回数量，默认为5
    Result<List<ProductVO>> hot(@RequestParam(defaultValue = "5") int limit);

    /**
     * 商品全文搜索
     * 根据关键词搜索商品，返回分页的搜索结果
     *
     * @param keyword  搜索关键词（必填）
     * @param pageNum  页码（从1开始），默认值为1
     * @param pageSize 每页条数，默认值为12
     * @return Result 包装的分页搜索结果Map
     */
    // @GetMapping：将HTTP GET请求映射到/product/search路径
    @GetMapping("/product/search")
    Result<Map<String, Object>> search(
            // @RequestParam：搜索关键词为必填参数（required默认为true）
            @RequestParam String keyword,
            // @RequestParam：页码，默认第1页
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：每页条数，默认12条
            @RequestParam(defaultValue = "12") int pageSize);

    /**
     * 获取商品分类列表
     * 返回所有可用的商品分类，通常为树形结构
     *
     * @return Result 包装的分类列表，每个元素为包含分类信息的Map
     */
    // @GetMapping：将HTTP GET请求映射到/category/list路径
    @GetMapping("/category/list")
    Result<List<Map<String, Object>>> categoryList();

    // ===== 以下为管理后台专用接口 =====

    /**
     * 管理后台 — 获取商品分页列表（含已下架商品）
     * 与前台列表不同，管理后台可以看到所有状态的商品
     *
     * @param pageNum    页码（从1开始），默认值为1
     * @param pageSize   每页条数，默认值为10
     * @param keyword    搜索关键词（可选），用于按商品名称模糊匹配
     * @param categoryId 分类ID（可选），用于按分类筛选
     * @return Result 包装的分页商品数据Map
     */
    // @GetMapping：将HTTP GET请求映射到/admin/product/list路径
    @GetMapping("/admin/product/list")
    Result<Map<String, Object>> adminProductList(
            // @RequestParam：页码，默认第1页
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：每页条数，默认10条
            @RequestParam(defaultValue = "10") int pageSize,
            // @RequestParam(required = false)：搜索关键词为可选参数
            @RequestParam(required = false) String keyword,
            // @RequestParam(required = false)：分类ID为可选参数
            @RequestParam(required = false) Long categoryId);

    /**
     * 管理后台 — 创建新商品
     *
     * @param productData 商品数据Map，包含name、price、stock、categoryId、description等字段
     * @return Result 包装的空返回体
     */
    // @PostMapping：将HTTP POST请求映射到/admin/product路径
    @PostMapping("/admin/product")
    // @RequestBody：商品数据通过请求体以JSON格式传入
    Result<Void> createProduct(@RequestBody Map<String, Object> productData);

    /**
     * 管理后台 — 更新商品信息
     *
     * @param id          商品主键ID，作为URL路径变量
     * @param productData 要更新的商品数据Map
     * @return Result 包装的空返回体
     */
    // @PutMapping：将HTTP PUT请求映射到/admin/product/{id}路径，PUT语义表示幂等更新
    @PutMapping("/admin/product/{id}")
    // @PathVariable：将URL路径中的{id}占位符的值绑定到方法参数id
    // @RequestBody：更新的商品数据通过请求体传入
    Result<Void> updateProduct(@PathVariable String id, @RequestBody Map<String, Object> productData);

    /**
     * 管理后台 — 删除商品（逻辑删除）
     *
     * @param id 商品主键ID，作为URL路径变量
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将HTTP DELETE请求映射到/admin/product/{id}路径，DELETE语义表示删除资源
    @DeleteMapping("/admin/product/{id}")
    // @PathVariable：将URL路径中的{id}占位符的值绑定到方法参数id
    Result<Void> deleteProduct(@PathVariable String id);

    /**
     * 管理后台 — 获取商品的所有SKU（库存量单位）
     * 用于管理后台查看和编辑商品的多规格信息（如不同颜色、尺寸的库存）
     *
     * @param id 商品主键ID，作为URL路径变量
     * @return Result 包装的SKU列表，每个元素为包含SKU信息的Map
     */
    // @GetMapping：将HTTP GET请求映射到/admin/product/{id}/skus路径
    @GetMapping("/admin/product/{id}/skus")
    // @PathVariable：将URL路径中的{id}占位符的值绑定到方法参数id
    Result<List<Map<String, Object>>> getProductSkus(@PathVariable String id);
}
