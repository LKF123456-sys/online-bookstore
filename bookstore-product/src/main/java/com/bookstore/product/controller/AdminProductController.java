package com.bookstore.product.controller;  // 声明当前类所在的包路径，属于商品服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一响应结果包装类
import com.bookstore.common.api.vo.ProductVO;  // 导入商品视图对象
import com.bookstore.product.service.ProductService;  // 导入商品业务服务类
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成构造函数
import org.springframework.web.bind.annotation.*;  // 导入Spring MVC的Web注解

/**
 * 管理端商品控制器（面向后台管理员）
 * 提供商品的增删改查以及状态管理等REST接口
 * 所有接口路径前缀为 /admin/product，与用户端的 /api/product 区分开
 *
 * 接口列表：
 *   - POST   /admin/product           新增商品
 *   - PUT    /admin/product/{id}       修改商品信息
 *   - DELETE /admin/product/{id}       删除商品
 *   - PUT    /admin/product/{id}/status 更新商品状态（上架/下架）
 */
@RestController  // REST控制器注解，返回JSON格式数据
@RequestMapping("/admin/product")  // URL路径前缀，所有接口以 /admin/product 开头，标识这是管理端接口
@RequiredArgsConstructor  // Lombok注解，自动生成构造函数，实现依赖注入
public class AdminProductController {  // 管理端商品控制器类

    private final ProductService productService;  // 商品业务服务，通过构造函数注入

    /**
     * 新增商品
     * 管理员在后台添加新商品时调用
     * 商品创建后默认为上架状态（status=1）
     *
     * @param vo 商品视图对象，通过@RequestBody从请求体JSON中自动解析
     * @return 无返回数据的成功结果
     */
    @PostMapping  // POST请求映射，处理 /admin/product 的HTTP POST请求
    public Result<Void> addProduct(@RequestBody ProductVO vo) {  // @RequestBody将请求体中的JSON数据自动反序列化为ProductVO对象
        productService.addProduct(vo);  // 调用服务层新增商品方法
        return Result.success();  // 返回成功结果（不携带数据）
    }

    /**
     * 修改商品信息
     * 管理员在后台编辑商品信息时调用
     *
     * @param id 商品ID，从URL路径中提取
     * @param vo 商品视图对象，包含要修改的商品信息
     * @return 无返回数据的成功结果
     */
    @PutMapping("/{id}")  // PUT请求映射，处理 /admin/product/{id} 的HTTP PUT请求
    public Result<Void> updateProduct(@PathVariable String id, @RequestBody ProductVO vo) {  // 路径变量和请求体参数
        vo.setId(id);  // 将URL路径中的商品ID设置到VO对象中，确保修改的是正确的商品
        productService.updateProduct(vo);  // 调用服务层修改商品方法
        return Result.success();  // 返回成功结果
    }

    /**
     * 删除商品
     * 管理员在后台删除商品时调用
     *
     * @param id 要删除的商品ID，从URL路径中提取
     * @return 无返回数据的成功结果
     */
    @DeleteMapping("/{id}")  // DELETE请求映射，处理 /admin/product/{id} 的HTTP DELETE请求
    public Result<Void> deleteProduct(@PathVariable String id) {  // @PathVariable从URL路径中提取商品ID
        productService.deleteProduct(id);  // 调用服务层删除商品方法
        return Result.success();  // 返回成功结果
    }

    /**
     * 更新商品状态（上架/下架）
     * 管理员可以通过此接口切换商品的上架/下架状态
     * status=1 表示上架，status=0 表示下架
     *
     * @param id 商品ID，从URL路径中提取
     * @param status 目标状态值（1=上架，0=下架），从URL查询参数获取
     * @return 无返回数据的成功结果
     */
    @PutMapping("/{id}/status")  // PUT请求映射，处理 /admin/product/{id}/status 请求
    public Result<Void> updateProductStatus(@PathVariable String id, @RequestParam Integer status) {  // 路径变量和查询参数
        productService.updateProductStatus(id, status);  // 调用服务层更新商品状态方法
        return Result.success();  // 返回成功结果
    }
}
