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
                                <div style="background:linear-gradient(135deg,#0d0d2b,#1a0a2e);padding:28px;border-radius:16px;border:1px solid rgba(0,240,255,0.15);margin-bottom:24px;position:relative;overflow:hidden;">
                                  <div style="position:absolute;top:-40px;right:-20px;width:120px;height:120px;background:radial-gradient(circle,rgba(0,240,255,0.15),transparent 70%);border-radius:50%;"></div>
                                  <div style="position:absolute;bottom:-30px;left:30%;width:100px;height:100px;background:radial-gradient(circle,rgba(255,0,255,0.1),transparent 70%);border-radius:50%;"></div>
                                  <h2 style="margin:0 0 6px 0;font-size:26px;background:linear-gradient(90deg,#00f0ff,#ff00ff);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text;">BOOKVERSE API v1.0</h2>
                                  <p style="margin:0;color:#8899bb;font-size:14px;font-family:monospace;">&gt; 微服务架构 · Spring Cloud 2023 · 7 个独立服务 · Nacos 注册中心</p>
                                </div>

                                <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:12px;margin-bottom:24px;">
                                  <div style="background:rgba(0,240,255,0.06);border:1px solid rgba(0,240,255,0.12);border-radius:10px;padding:14px;text-align:center;">
                                    <div style="font-size:22px;font-weight:800;color:#00f0ff;font-family:monospace;">7</div>
                                    <div style="font-size:11px;color:#8899bb;margin-top:4px;">微服务模块</div>
                                  </div>
                                  <div style="background:rgba(0,255,136,0.06);border:1px solid rgba(0,255,136,0.12);border-radius:10px;padding:14px;text-align:center;">
                                    <div style="font-size:22px;font-weight:800;color:#00ff88;font-family:monospace;">60+</div>
                                    <div style="font-size:11px;color:#8899bb;margin-top:4px;">API 接口</div>
                                  </div>
                                  <div style="background:rgba(255,0,255,0.06);border:1px solid rgba(255,0,255,0.12);border-radius:10px;padding:14px;text-align:center;">
                                    <div style="font-size:22px;font-weight:800;color:#ff00ff;font-family:monospace;">15</div>
                                    <div style="font-size:11px;color:#8899bb;margin-top:4px;">数据表</div>
                                  </div>
                                  <div style="background:rgba(153,69,255,0.06);border:1px solid rgba(153,69,255,0.12);border-radius:10px;padding:14px;text-align:center;">
                                    <div style="font-size:22px;font-weight:800;color:#9945ff;font-family:monospace;">JWT</div>
                                    <div style="font-size:11px;color:#8899bb;margin-top:4px;">认证方式</div>
                                  </div>
                                  <div style="background:rgba(255,107,53,0.06);border:1px solid rgba(255,107,53,0.12);border-radius:10px;padding:14px;text-align:center;">
                                    <div style="font-size:22px;font-weight:800;color:#ff6b35;font-family:monospace;">ES</div>
                                    <div style="font-size:11px;color:#8899bb;margin-top:4px;">搜索引擎</div>
                                  </div>
                                </div>

                                ## 架构拓扑

                                | 服务 | 端口 | 技术栈 | 职责 |
                                |------|------|--------|------|
                                | **Gateway** | `8080` | Spring Cloud Gateway | 统一路由 & JWT 鉴权 |
                                | **User** | `8081` | Spring Security + JWT | 注册 / 登录 / 用户管理 |
                                | **Product** | `8082` | Elasticsearch | 商品 CRUD / 搜索 / 分类 |
                                | **Order** | `8083` | OpenFeign | 订单 / 购物车 / 支付 |
                                | **Promotion** | `8085` | - | 优惠券 / 评价 / 公告 |
                                | **Admin** | `8086` | JSP + RestTemplate | 管理后台 (本服务) |
                                | **Message** | `8087` | WebSocket | 站内信 / 实时通知 |

                                ## 安全策略
                                - **认证**：JWT Token (Header: `Authorization: Bearer <token>`)
                                - **管理端**：Session Cookie (`JSESSIONID`)
                                - **密码加密**：BCrypt 哈希算法
                                - **跨服务调用**：内部服务通过 Nacos 发现直连，外部请求必须经过 Gateway

                                ## 统一响应

                                ```json
                                {
                                  "code": 200,
                                  "message": "success",
                                  "data": {}
                                }
                                ```

                                ## 状态码速查

                                | 状态码 | 含义 | 触发场景 |
                                |--------|------|----------|
                                | `200` | OK | 请求成功 |
                                | `302` | Redirect | 页面重定向 |
                                | `400` | Bad Request | 参数校验失败 |
                                | `401` | Unauthorized | Token 无效或过期 |
                                | `403` | Forbidden | 权限不足 |
                                | `404` | Not Found | 资源不存在 |
                                | `500` | Server Error | 服务内部异常 |
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
