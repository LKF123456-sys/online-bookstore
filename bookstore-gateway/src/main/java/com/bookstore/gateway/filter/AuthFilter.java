package com.bookstore.gateway.filter; // 声明当前类所在的包路径，filter包专门存放过滤器相关代码

import org.springframework.cloud.gateway.filter.GatewayFilterChain; // 导入网关过滤器链，用于将请求传递给下一个过滤器
import org.springframework.cloud.gateway.filter.GlobalFilter; // 导入全局过滤器接口，实现此接口的过滤器会对所有请求生效
import org.springframework.core.Ordered; // 导入Ordered接口，用于控制过滤器的执行顺序
import org.springframework.http.HttpHeaders; // 导入HTTP请求头常量类，包含常见的请求头名称
import org.springframework.http.server.reactive.ServerHttpRequest; // 导入响应式HTTP请求对象，用于读取和修改请求信息
import org.springframework.stereotype.Component; // 导入组件注解，让Spring自动创建并管理这个类的实例
import org.springframework.web.server.ServerWebExchange; // 导入服务端Web交换对象，封装了请求和响应信息
import reactor.core.publisher.Mono; // 导入Reactor的Mono类型，表示一个异步的0或1个元素的流

import java.util.Base64; // 导入Base64工具类，用于解码JWT Token中的Base64编码内容
import java.util.List; // 导入List集合接口，用于存储白名单路径

/**
 * 认证过滤器 - 网关的全局过滤器
 * 作用：拦截所有经过网关的请求，提取用户身份信息，并传递给下游微服务
 *
 * 工作流程：
 * 1. 检查请求路径是否在白名单中，如果是则直接放行（不需要登录的接口）
 * 2. 尝试从请求头的Authorization字段中解析JWT Token，提取userId
 * 3. 如果没有Token，则尝试从URL查询参数中获取userId（方便开发测试）
 * 4. 如果都没有，使用"anonymous"作为默认值（匿名用户）
 * 5. 将userId添加到请求头X-User-Id中，传递给下游微服务使用
 */
@Component // 将这个类注册为Spring组件，Spring容器会自动创建它的实例（Bean）
public class AuthFilter implements GlobalFilter, Ordered { // 实现GlobalFilter接口使其成为全局过滤器，实现Ordered接口控制执行顺序

    /**
     * 白名单路径列表
     * 这些路径对应的接口不需要用户登录就可以访问
     * 例如：登录接口、注册接口、商品列表等公开接口
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login", "/api/auth/register",  // 登录和注册接口，未登录用户也需要访问
            "/api/product/list", "/api/product/recommend", "/api/product/hot",  // 商品列表、推荐、热门商品接口
            "/api/category/list", "/api/announcement/list",  // 分类列表和公告列表接口
            "/api/search", "/api/coupon/list"  // 搜索接口和优惠券列表接口
    );

    /**
     * 核心过滤方法 - 每个请求经过网关时都会执行此方法
     * @param exchange Web交换对象，包含当前请求和响应的所有信息
     * @param chain 过滤器链，调用chain.filter()将请求传递给下一个过滤器或目标服务
     * @return Mono<Void> 表示异步操作完成后没有返回值
     */
    @Override // 表示重写GlobalFilter接口中的filter方法
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest(); // 从交换对象中获取当前的HTTP请求对象
        String path = request.getURI().getPath(); // 获取请求的URL路径，例如 /api/product/list

        // 白名单路径直接放行，不进行认证检查
        for (String whitePath : WHITE_LIST) { // 遍历白名单中的每一个路径
            if (path.startsWith(whitePath) || path.equals(whitePath)) { // 判断当前请求路径是否以白名单路径开头或完全匹配
                return chain.filter(exchange); // 匹配则直接放行，将请求传递给下一个过滤器或目标服务
            }
        }

        String userId = null; // 初始化用户ID为null，后续会尝试从不同来源获取

        // 第1步：尝试从JWT Token中提取userId
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION); // 从请求头中获取Authorization字段的值
        if (authHeader != null && authHeader.startsWith("Bearer ")) { // 判断Authorization头是否存在且以"Bearer "开头（JWT Token的标准格式）
            try {
                String token = authHeader.substring(7); // 去掉"Bearer "前缀（7个字符），得到纯Token字符串
                String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1])); // JWT由三部分（header.payload.signature）用"."分隔，取第2部分（payload）并用Base64解码
                // 简单从解码后的JSON字符串中提取userId字段
                int start = payload.indexOf("\"userId\":\"") + 10; // 查找"userId":"的位置，并跳过这10个字符（即"userId":"的长度），定位到userId值的起始位置
                if (start > 9) { // 如果找到（start > 9说明indexOf找到了匹配项，没找到会返回-1）
                    int end = payload.indexOf("\"", start); // 从userId值的起始位置向后查找下一个双引号，即userId值的结束位置
                    userId = payload.substring(start, end); // 截取userId的值
                }
            } catch (Exception ignored) { // 如果Token解析过程出现任何异常（格式错误等），静默忽略，继续后续逻辑
            }
        }

        // 第2步：如果没有从Token中获取到userId，尝试从URL查询参数中获取（方便开发和测试阶段使用）
        if (userId == null) { // 检查上一步是否成功获取到了userId
            userId = request.getQueryParams().getFirst("userId"); // 从URL查询参数中获取userId，例如 ?userId=123
        }

        // 第3步：如果还是没有获取到userId，使用默认值"anonymous"表示匿名用户
        if (userId == null) { // 再次检查是否获取到了userId
            userId = "anonymous"; // 设置默认值为"anonymous"，表示未登录的匿名用户
        }

        // 将提取到的userId添加到请求头中，传递给下游的微服务使用
        ServerHttpRequest mutatedRequest = request.mutate() // 创建请求的修改器（Builder模式），用于修改请求内容
                .header("X-User-Id", userId) // 添加自定义请求头X-User-Id，值为提取到的userId
                .build(); // 构建修改后的新请求对象

        return chain.filter(exchange.mutate().request(mutatedRequest).build()); // 将修改后的请求放入exchange中，传递给过滤器链继续处理
    }

    /**
     * 获取过滤器的执行顺序（优先级）
     * 数值越小，优先级越高，越早执行
     * @return 返回-100，表示这个过滤器会在大多数其他过滤器之前执行
     */
    @Override // 表示重写Ordered接口中的getOrder方法
    public int getOrder() {
        return -100; // 返回-100，优先级较高，确保认证逻辑在路由转发之前执行
    }
}
