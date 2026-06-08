package com.bookstore.admin.config; // 声明当前类所在的包路径：配置层

// 导入OpenAPI/Swagger的相关类，用于生成API文档
import io.swagger.v3.oas.models.Components; // API组件定义（安全方案等）
import io.swagger.v3.oas.models.OpenAPI; // OpenAPI文档根对象
import io.swagger.v3.oas.models.info.Contact; // 联系人信息
import io.swagger.v3.oas.models.info.Info; // API基本信息（标题、描述、版本等）
import io.swagger.v3.oas.models.info.License; // 许可证信息
import io.swagger.v3.oas.models.security.SecurityRequirement; // 安全要求
import io.swagger.v3.oas.models.security.SecurityScheme; // 安全方案定义
import io.swagger.v3.oas.models.servers.Server; // 服务器信息
import io.swagger.v3.oas.models.tags.Tag; // API分组标签
// 导入SpringDoc的分组API配置类
import org.springdoc.core.models.GroupedOpenApi;
// 导入Spring配置注解
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List; // 导入List接口

/**
 * OpenAPI/Swagger配置类 - 配置管理后台的API文档
 *
 * 这个配置类用于自定义Swagger UI的显示内容，包括：
 * - API文档的标题、描述、版本、联系方式
 * - 微服务架构概览表格
 * - 认证方式说明
 * - 通用响应格式和HTTP状态码说明
 * - API分组（管理后台API、服务代理API）
 * - Session认证安全方案配置
 *
 * 访问方式：http://localhost:8086/swagger-ui.html（如果启用的话）
 */
@Configuration // 标记这是一个Spring配置类
public class OpenApiConfig {

    /**
     * 配置OpenAPI文档的基本信息
     * 包括标题、描述（HTML格式，展示架构概览）、版本、联系方式、许可证等
     *
     * @return OpenAPI配置对象
     */
    @Bean // 注册为Spring Bean
    public OpenAPI bookstoreOpenAPI() {
        return new OpenAPI()
                .info(new Info() // 设置API基本信息
                        .title("BookVerse 在线书店 — 管理后台 API") // API文档标题
                        // API文档描述（支持HTML格式的文本块）
                        .description("""
                                <div style="background:linear-gradient(135deg,#667eea,#764ba2);padding:20px;border-radius:12px;color:#fff;margin-bottom:16px;">
                                  <h2 style="margin:0 0 8px 0;">📚 BookVerse 管理后台 API 文档</h2>
                                  <p style="margin:0;opacity:0.9;">微服务架构 · Spring Cloud · RESTful API v1.0</p>
                                </div>

                                ## 架构概览
                                | 服务 | 端口 | 说明 |
                                |------|------|------|
                                | gateway | 8081 | API 网关，统一入口 |
                                | bookstore-user | 8082 | 用户服务 |
                                | bookstore-product | 8083 | 商品服务 |
                                | bookstore-order | 8084 | 订单服务 |
                                | bookstore-promotion | 8085 | 促销/优惠券/评价 |
                                | **bookstore-admin** | **8086** | **管理后台 (本服务)** |
                                | bookstore-message | 8087 | 消息通知 |

                                ## 认证方式
                                - **管理端**：Session 认证，访问 `/admin/login` 登录
                                - 登录后获取 `JSESSIONID`，后续请求自动携带

                                ## 通用响应格式
                                ```json
                                {
                                  "success": true,
                                  "message": "操作成功",
                                  "data": {}
                                }
                                ```

                                ## HTTP 状态码
                                | 状态码 | 说明 |
                                |--------|------|
                                | 200 | 成功 |
                                | 302 | 重定向 |
                                | 400 | 参数错误 |
                                | 401 | 未认证 |
                                | 403 | 无权限 |
                                | 404 | 未找到 |
                                | 500 | 服务器错误 |
                                """)
                        .version("1.0.0") // API版本号
                        .contact(new Contact() // 联系人信息
                                .name("BookVerse 开发团队")
                                .email("dev@bookverse.com")
                                .url("https://github.com/bookverse"))
                        .license(new License() // 许可证信息
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of( // 配置可用的服务器地址
                        new Server().url("http://localhost:8081").description("网关入口"), // 通过网关访问
                        new Server().url("http://localhost:8086").description("管理后台直连"))) // 直连管理后台
                .tags(List.of( // 配置API分组标签
                        new Tag().name("管理后台-仪表盘").description("首页统计、数据大屏"),
                        new Tag().name("管理后台-商品").description("商品 CRUD、库存管理、分类管理"),
                        new Tag().name("管理后台-订单").description("订单列表、详情、发货、取消"),
                        new Tag().name("管理后台-用户").description("用户列表、详情、禁用/启用"),
                        new Tag().name("管理后台-优惠券").description("优惠券创建、发放、统计"),
                        new Tag().name("管理后台-评价").description("商品评价审核与管理"),
                        new Tag().name("管理后台-消息").description("系统消息推送"),
                        new Tag().name("管理后台-系统").description("操作日志、公告管理"),
                        new Tag().name("认证代理").description("用户认证转发、服务间调用")))
                .addSecurityItem(new SecurityRequirement().addList("sessionAuth")) // 添加全局安全要求
                .components(new Components() // 配置安全组件
                        .addSecuritySchemes("sessionAuth", // 定义名为sessionAuth的安全方案
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY) // 类型为API Key
                                        .in(SecurityScheme.In.COOKIE) // 通过Cookie传递
                                        .name("JSESSIONID") // Cookie名称
                                        .description("管理后台 Session 认证"))); // 描述说明
    }

    /**
     * 配置管理后台API分组
     * 将所有以/admin/开头的接口归为一组，方便在Swagger UI中查看
     *
     * @return 分组API配置对象
     */
    @Bean // 注册为Spring Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理后台 API") // 分组名称
                .displayName("管理后台全部接口") // 分组显示名称
                .pathsToMatch("/admin/**") // 匹配的路径模式
                .build(); // 构建分组配置
    }

    /**
     * 配置服务代理API分组
     * 将所有以/api/开头的接口归为一组
     *
     * @return 分组API配置对象
     */
    @Bean // 注册为Spring Bean
    public GroupedOpenApi proxyApi() {
        return GroupedOpenApi.builder()
                .group("服务代理 API") // 分组名称
                .displayName("微服务代理转发") // 分组显示名称
                .pathsToMatch("/api/**") // 匹配的路径模式
                .build(); // 构建分组配置
    }
}
