// 声明当前类所属的包路径，遵循 Spring Boot 项目标准分包结构
package com.bookstore.admin.feign;

// 导入公共模块的统一响应封装类 Result，用于包装所有接口返回结果
import com.bookstore.common.api.Result;
// 导入商品视图对象 ProductVO，用于返回前端展示所需的商品信息
import com.bookstore.common.api.vo.ProductVO;
// 导入 Spring Cloud OpenFeign 的 @FeignClient 注解，用于声明式 HTTP 客户端
import org.springframework.cloud.openfeign.FeignClient;
// 导入 Spring MVC 的请求映射注解（GetMapping、PostMapping、PutMapping、DeleteMapping、PathVariable、RequestParam、RequestBody）
// 这些注解用于描述 HTTP 请求的 URL、方法与参数绑定方式
import org.springframework.web.bind.annotation.*;

// 导入 Java 集合框架的 List 接口
import java.util.List;
// 导入 Java 集合框架的 Map 接口
import java.util.Map;

/**
 * 商品服务 Feign 客户端 — 声明式调用 bookstore-product 微服务
 * <p>
 * 该接口通过 Spring Cloud OpenFeign 以声明方式定义对商品微服务的 HTTP 调用。
 * Feign 框架会在运行时动态生成代理实现类，自动完成服务发现、负载均衡与 HTTP 请求发送。
 * 所有方法的 URL 均以类级别 path="/api" 为前缀。
 */
// @FeignClient 声明这是一个 Feign 客户端接口
//   - name = "bookstore-product"：指定要调用的微服务名称，对应 Nacos/注册中心中的服务名
//   - path = "/api"：指定所有方法 URL 的统一路径前缀
@FeignClient(name = "bookstore-product", path = "/api")
public interface ProductFeignClient {

    /**
     * 获取商品分页列表（前台/用户端）
     * <p>
     * 调用 bookstore-product 微服务的 /api/product/list 接口
     * 请求方式：GET
     * 支持多条件筛选：关键词、分类、排序方式
     *
     * @param pageNum    页码（从 1 开始），默认值为 1
     * @param pageSize   每页条数，默认值为 12
     * @param keyword    搜索关键词（可选），用于按商品名称模糊匹配
     * @param categoryId 分类 ID（可选），用于按分类筛选商品
     * @param sort       排序方式（可选），如 "price_asc"（价格升序）、"sales_desc"（销量降序）等
     * @return Result 包装的分页数据 Map，包含 records（商品列表）、total（总记录数）等字段
     */
    // @GetMapping：将 HTTP GET 请求映射到 /product/list 路径
    @GetMapping("/product/list")
    Result<Map<String, Object>> list(
            // @RequestParam：将 URL 查询参数绑定到方法参数，设置默认值以便前端不传参数时也能正常分页
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize,
            // required = false 表示参数可选，前端可以不传递
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort);

    /**
     * 获取商品详情
     * <p>
     * 调用 bookstore-product 微服务的 /api/product/{id} 接口
     * 请求方式：GET
     *
     * @param id 商品主键 ID，作为 URL 路径变量
     * @return Result 包装的商品详情 Map，包含商品基本信息、SKU 列表、商品图片等
     */
    // @GetMapping：将 HTTP GET 请求映射到 /product/{id} 路径
    @GetMapping("/product/{id}")
    // @PathVariable：将 URL 路径中的 {id} 占位符的值绑定到方法参数 id
    Result<Map<String, Object>> detail(@PathVariable Long id);

    /**
     * 获取推荐商品列表
     * <p>
     * 调用 bookstore-product 微服务的 /api/product/recommend 接口
     * 请求方式：GET
     * 返回由推荐算法或运营配置筛选的商品列表
     *
     * @param limit 返回的商品数量上限，默认值为 8
     * @return Result 包装的 ProductVO 列表
     */
    // @GetMapping：将 HTTP GET 请求映射到 /product/recommend 路径
    @GetMapping("/product/recommend")
    // @RequestParam：通过 URL 查询参数 limit 控制返回数量
    Result<List<ProductVO>> recommend(@RequestParam(defaultValue = "8") int limit);

    /**
     * 获取热销商品列表
     * <p>
     * 调用 bookstore-product 微服务的 /api/product/hot 接口
     * 请求方式：GET
     * 返回按销量排序的热门商品
     *
     * @param limit 返回的商品数量上限，默认值为 5
     * @return Result 包装的 ProductVO 列表
     */
    // @GetMapping：将 HTTP GET 请求映射到 /product/hot 路径
    @GetMapping("/product/hot")
    Result<List<ProductVO>> hot(@RequestParam(defaultValue = "5") int limit);

    /**
     * 商品全文搜索
     * <p>
     * 调用 bookstore-product 微服务的 /api/search 接口
     * 请求方式：GET
     * 通常基于 Elasticsearch 或数据库 LIKE 查询实现关键词搜索
     *
     * @param keyword  搜索关键词（必填），用于全文检索
     * @param pageNum  页码（从 1 开始），默认值为 1
     * @param pageSize 每页条数，默认值为 12
     * @return Result 包装的分页搜索结果 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /search 路径
    @GetMapping("/search")
    Result<Map<String, Object>> search(
            // keyword 未设置 defaultValue 和 required=false，因此为必填参数
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize);

    /**
     * 获取商品分类列表
     * <p>
     * 调用 bookstore-product 微服务的 /api/category/list 接口
     * 请求方式：GET
     * 返回所有分类及其层级结构
     *
     * @return Result 包装的分类列表，每个元素为包含 id、name、parentId 等字段的 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /category/list 路径
    @GetMapping("/category/list")
    Result<List<Map<String, Object>>> categoryList();

    // ===== 以下为管理后台专用接口 =====

    /**
     * 管理后台 — 获取商品分页列表
     * <p>
     * 调用 bookstore-product 微服务的 /api/admin/product/list 接口
     * 请求方式：GET
     * 与前台的商品列表接口相比，管理后台接口不会过滤已下架商品
     *
     * @param pageNum    页码（从 1 开始），默认值为 1
     * @param pageSize   每页条数，默认值为 10
     * @param keyword    搜索关键词（可选）
     * @param categoryId 分类 ID（可选）
     * @return Result 包装的分页数据 Map
     */
    // @GetMapping：将 HTTP GET 请求映射到 /admin/product/list 路径
    @GetMapping("/admin/product/list")
    Result<Map<String, Object>> adminProductList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId);

    /**
     * 管理后台 — 创建新商品
     * <p>
     * 调用 bookstore-product 微服务的 /api/admin/product 接口
     * 请求方式：POST
     *
     * @param productData 商品数据 Map，包含名称、描述、价格、分类、图片、SKU 等信息
     * @return Result 包装的空返回体
     */
    // @PostMapping：将 HTTP POST 请求映射到 /admin/product 路径
    @PostMapping("/admin/product")
    // @RequestBody：商品数据通过请求体以 JSON 格式传入
    Result<Void> createProduct(@RequestBody Map<String, Object> productData);

    /**
     * 管理后台 — 更新商品信息
     * <p>
     * 调用 bookstore-product 微服务的 /api/admin/product/{id} 接口
     * 请求方式：PUT
     *
     * @param id          商品主键 ID，作为 URL 路径变量
     * @param productData 要更新的商品数据 Map
     * @return Result 包装的空返回体
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /admin/product/{id} 路径
    @PutMapping("/admin/product/{id}")
    // @PathVariable：将 URL 中的 {id} 绑定到方法参数 id；@RequestBody：更新的数据通过请求体传入
    Result<Void> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> productData);

    /**
     * 管理后台 — 删除商品
     * <p>
     * 调用 bookstore-product 微服务的 /api/admin/product/{id} 接口
     * 请求方式：DELETE
     * 通常执行逻辑删除（设置 is_deleted 标志），而非物理删除
     *
     * @param id 商品主键 ID，作为 URL 路径变量
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将 HTTP DELETE 请求映射到 /admin/product/{id} 路径
    @DeleteMapping("/admin/product/{id}")
    Result<Void> deleteProduct(@PathVariable Long id);

    /**
     * 管理后台 — 获取商品的所有 SKU
     * <p>
     * 调用 bookstore-product 微服务的 /api/admin/product/{id}/skus 接口
     * 请求方式：GET
     * SKU 是商品的具体规格组合（如：颜色=红色 + 尺寸=XL），每种组合有独立的价格和库存
     *
     * @param id 商品主键 ID，作为 URL 路径变量
     * @return Result 包装的 SKU 列表，每个 SKU 包含规格属性、价格、库存等信息
     */
    // @GetMapping：将 HTTP GET 请求映射到 /admin/product/{id}/skus 路径
    @GetMapping("/admin/product/{id}/skus")
    Result<List<Map<String, Object>>> getProductSkus(@PathVariable Long id);
}
