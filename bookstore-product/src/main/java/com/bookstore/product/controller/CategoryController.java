package com.bookstore.product.controller;  // 声明当前类所在的包路径，属于商品服务的控制器层

import com.bookstore.common.api.Result;  // 导入统一响应结果包装类，用于封装API返回数据
import com.bookstore.common.entity.Category;  // 导入商品分类实体类，对应数据库中的分类表
import com.bookstore.product.mapper.CategoryMapper;  // 导入分类Mapper接口，用于操作分类数据表
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用于构建类型安全的查询条件
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含所有final字段的构造函数
import org.springframework.web.bind.annotation.*;  // 导入Spring MVC的Web注解

import java.util.List;  // 导入Java集合框架的List接口

/**
 * 分类控制器（面向前端用户）
 * 提供商品分类的查询接口
 * 所有接口路径前缀为 /api/category
 *
 * 接口列表：
 *   - GET /api/category/list  获取所有分类列表
 */
@RestController  // REST控制器注解，返回JSON格式数据
@RequestMapping("/api/category")  // URL路径前缀，该控制器下所有接口以 /api/category 开头
@RequiredArgsConstructor  // Lombok注解，自动生成包含所有final字段的构造函数，实现依赖注入
public class CategoryController {  // 分类控制器类

    private final CategoryMapper categoryMapper;  // 分类数据访问对象，通过构造函数注入

    /**
     * 获取所有商品分类列表
     * 查询数据库中所有的商品分类，返回完整的分类树
     * 不需要分页，因为分类数据通常较少
     *
     * @return 所有分类的列表
     */
    @GetMapping("/list")  // GET请求映射，处理 /api/category/list 请求
    public Result<List<Category>> getCategoryList() {  // 无参数，查询所有分类
        return Result.success(categoryMapper.selectList(null));  // 调用MyBatis-Plus的selectList方法，传null表示不加任何查询条件，查询全部数据
    }
}
