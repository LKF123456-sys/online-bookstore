package com.bookstore.admin.controller; // 声明当前类所在的包路径：控制器层

// 导入Jackson的ObjectMapper类，用于JSON解析
import com.fasterxml.jackson.databind.ObjectMapper;
// 导入Spring MVC的@Controller注解
import org.springframework.stereotype.Controller;
// 导入Spring MVC的Model接口
import org.springframework.ui.Model;
// 导入@GetMapping注解
import org.springframework.web.bind.annotation.GetMapping;
// 导入RestTemplate，用于调用其他微服务
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList; // 导入ArrayList
import java.util.Map; // 导入Map接口

/**
 * 分类页面控制器 - 管理后台分类管理页面
 * 负责处理管理后台中商品分类相关的页面请求
 * 从商品服务获取分类数据并渲染到JSP页面
 */
@Controller // 标记这是一个Spring MVC控制器（返回视图名称，不是JSON）
public class CategoryPageController {

    // RestTemplate用于调用商品微服务获取分类数据
    private final RestTemplate restTemplate;
    // Jackson的ObjectMapper用于JSON解析
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造方法，通过Spring自动注入RestTemplate依赖
     *
     * @param restTemplate Spring自动注入的RestTemplate实例
     */
    public CategoryPageController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate; // 注入RestTemplate
    }

    /**
     * 管理后台分类列表页面
     * 加载所有商品分类并展示在管理后台
     *
     * @param model Spring MVC的Model对象，用于向JSP传递数据
     * @return 分类列表管理页视图名称
     */
    @GetMapping("/admin/category") // 处理GET /admin/category请求
    public String adminCategoryList(Model model) {
        loadCategories(model); // 调用辅助方法加载分类数据
        return "admin/category/list"; // 返回分类列表页视图
    }

    /**
     * 管理后台分类列表页面（别名路径）
     * 与/admin/category功能完全相同，兼容/admin/category/list的访问方式
     *
     * @param model Spring MVC的Model对象
     * @return 分类列表管理页视图名称
     */
    @GetMapping("/admin/category/list") // 处理GET /admin/category/list请求
    public String adminCategoryListAlias(Model model) {
        loadCategories(model); // 调用辅助方法加载分类数据
        return "admin/category/list"; // 返回分类列表页视图
    }

    /**
     * 私有辅助方法 - 从商品服务加载分类数据
     * 通过RestTemplate调用商品服务的分类列表API，获取所有分类数据
     *
     * @param model Spring MVC的Model对象，分类数据将存入此对象
     */
    private void loadCategories(Model model) {
        try {
            // 向商品服务发送GET请求获取所有分类数据
            // http://bookstore-product 是商品服务的服务名（通过Nacos解析）
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/category/list", String.class);
            // 将JSON响应字符串解析为Map对象
            Map<String, Object> result = objectMapper.readValue(resp, Map.class);
            // 将分类数据列表存入Model，供JSP页面使用
            model.addAttribute("categoryList", result.get("data"));
        } catch (Exception e) {
            // 如果获取分类失败（如商品服务不可用），存入空列表避免页面报错
            model.addAttribute("categoryList", new ArrayList<>());
        }
    }
}
