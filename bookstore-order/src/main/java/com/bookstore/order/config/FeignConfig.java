package com.bookstore.order.config;  // 声明当前类所在的包路径：订单服务配置层

import feign.Request;  // 导入Feign的Request类，用于配置请求超时参数
import org.springframework.context.annotation.Bean;  // 导入Bean注解
import org.springframework.context.annotation.Configuration;  // 导入Configuration注解

import java.util.concurrent.TimeUnit;  // 导入时间单位枚举

/**
 * Feign客户端全局配置类
 * 配置所有Feign客户端的默认行为，包括超时时间等
 *
 * 超时参数说明：
 *   - connectTimeout：建立TCP连接的超时时间
 *     如果目标服务在指定时间内未响应TCP握手，则抛出连接超时异常
 *     建议设置较短（5秒），快速发现网络问题
 *
 *   - readTimeout：等待响应数据的超时时间
 *     TCP连接建立后，等待服务端返回数据的最大时间
 *     建议设置较长（10秒），给服务端足够的处理时间
 *
 * 这两个参数配合熔断器使用，可以有效防止因为下游服务响应慢导致的线程池耗尽问题
 */
@Configuration  // 标记为Spring配置类
public class FeignConfig {

    /**
     * 配置Feign客户端的默认超时参数
     * 对所有Feign客户端生效（除非某个客户端单独配置了覆盖值）
     *
     * @return Feign请求配置对象
     */
    @Bean
    public Request.Options feignRequestOptions() {  // 配置Feign请求选项
        return new Request.Options(
                5, TimeUnit.SECONDS,    // connectTimeout = 5秒：建立连接超时时间
                10, TimeUnit.SECONDS,   // readTimeout = 10秒：读取响应超时时间
                true                     // followRedirects = true：自动跟随HTTP 3xx重定向
        );
    }
}
