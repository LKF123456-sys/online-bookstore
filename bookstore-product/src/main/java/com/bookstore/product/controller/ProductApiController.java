package com.bookstore.product.controller;  // 声明当前类所在的包路径，属于商品服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一响应结果包装类，用于封装API返回数据
import com.bookstore.common.api.dto.ProductQueryDTO;  // 导入商品查询数据传输对象，包含分页、关键词、分类等查询条件
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果包装类，包含数据列表和分页信息
import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象，用于向前端返回商品数据
import com.bookstore.product.service.ProductService;  // 导入商品业务服务类，处理商品相关的业务逻辑
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含所有final字段的构造函数，实现构造函数注入
import org.springframework.web.bind.annotation.*;  // 导入Spring MVC的Web注解，包括@RestController、@GetMapping等

import java.util.List;  // 导入Java集合框架的List接口，用于表示商品列表

/**
 * 商品API控制器（面向前端用户）
 * 提供商品查询、商品详情、推荐商品、热门商品、库存更新等REST接口
 * 所有接口路径前缀为 /api/product，这是面向C端用户的商品接口
 *
 * 接口列表：
 *   - GET  /api/product/list        获取商品列表（支持分页、筛选、排序）
 *   - GET  /api/product/{id}        根据ID获取商品详情
 *   - GET  /api/product/recommend   获取推荐商品列表
 *   - GET  /api/product/hot         获取热门商品列表
 *   - PUT  /api/product/{id}/stock  更新商品库存
 */
@RestController  // REST控制器注解，标记这是一个控制器类，且所有方法返回值直接作为HTTP响应体（JSON格式）
@RequestMapping("/api/product")  // URL路径前缀，该控制器下所有接口的路径都以 /api/product 开头
@RequiredArgsConstructor  // Lombok注解，自动生成包含所有final字段的构造函数，Spring会通过构造函数自动注入依赖
public class ProductApiController {  // 商品API控制器类

    private final ProductService productService;  // 商品业务服务，通过构造函数注入（final保证不可变）

    /**
     * 获取商品列表
     * 支持分页查询，可按关键词搜索、按分类筛选、按价格范围过滤、按价格/销量排序
     * 只返回上架状态（status=1）的商品
     *
     * @param query 查询条件对象，包含：pageNum(页码)、pageSize(每页数量)、keyword(关键词)、
     *              category(分类)、minPrice(最低价)、maxPrice(最高价)、sortBy(排序字段)、sortOrder(排序方向)
     * @return 分页的商品列表，包含数据列表、总条数、当前页码、每页大小
     */
    @GetMapping("/list")  // GET请求映射，处理 /api/product/list 的HTTP GET请求
    public Result<PageResult<ProductVO>> getProductList(ProductQueryDTO query) {  // query参数自动从URL查询字符串中绑定
        return Result.success(productService.getProductList(query));  // 调用服务层查询商品列表，用Result包装为统一格式返回
    }

    /**
     * 根据商品ID获取商品详情
     * 包含商品基本信息、SKU列表、规格列表
     * 内部有Redis缓存，缓存时间为5分钟
     *
     * @param id 商品ID，从URL路径中提取（如 /api/product/123 中的 "123"）
     * @return 商品详情视图对象，包含商品信息、SKU列表、规格列表
     */
    @GetMapping("/{id}")  // GET请求映射，{id}是路径变量，表示商品ID
    public Result<ProductVO> getProductById(@PathVariable String id) {  // @PathVariable从URL路径中提取{id}的值
        return Result.success(productService.getProductById(id));  // 调用服务层查询商品详情，包装成功结果返回
    }

    /**
     * 获取推荐商品列表
     * 查询标记为"推荐"的上架商品，按销量降序排列
     *
     * @param limit 返回的商品数量，默认为8个
     * @return 推荐商品列表
     */
    @GetMapping("/recommend")  // GET请求映射，处理 /api/product/recommend 请求
    public Result<List<ProductVO>> getRecommendProducts(@RequestParam(defaultValue = "8") Integer limit) {  // @RequestParam从URL查询参数中获取limit，不传时默认为8
        return Result.success(productService.getRecommendProducts(limit));  // 调用服务层获取推荐商品，包装返回
    }

    /**
     * 获取热门商品列表
     * 查询上架商品，按销量降序排列，取销量最高的几个
     *
     * @param limit 返回的商品数量，默认为8个
     * @return 热门商品列表
     */
    @GetMapping("/hot")  // GET请求映射，处理 /api/product/hot 请求
    public Result<List<ProductVO>> getHotProducts(@RequestParam(defaultValue = "8") Integer limit) {  // @RequestParam从URL查询参数中获取limit，不传时默认为8
        return Result.success(productService.getHotProducts(limit));  // 调用服务层获取热门商品，包装返回
    }

    /**
     * 更新商品库存（扣减库存 + 增加销量）
     * 当用户下单时，由订单服务调用此接口扣减库存
     * 如果库存不足会抛出异常
     *
     * @param id 商品ID，从URL路径中提取
     * @param quantity 要扣减的数量，从URL查询参数中获取
     * @return 无返回数据的成功结果
     */
    @PutMapping("/{id}/stock")  // PUT请求映射，处理 /api/product/{id}/stock 的HTTP PUT请求
    public Result<Void> updateStock(@PathVariable String id, @RequestParam Integer quantity) {  // 路径变量和查询参数分别绑定
        productService.updateStock(id, quantity);  // 调用服务层扣减库存方法
        return Result.success();  // 返回成功结果（不携带数据）
    }
}
