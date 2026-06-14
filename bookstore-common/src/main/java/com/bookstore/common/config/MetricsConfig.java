package com.bookstore.common.config;  // 声明当前类所在的包路径：公共模块配置层

import io.micrometer.core.instrument.Counter;  // 导入Micrometer的Counter接口，用于创建计数器指标
import io.micrometer.core.instrument.DistributionSummary;  // 导入DistributionSummary，用于创建分布统计指标（如分位数、最大值、最小值）
import io.micrometer.core.instrument.MeterRegistry;  // 导入MeterRegistry，Micrometer的核心注册器，管理所有指标
import org.springframework.context.annotation.Bean;  // 导入Bean注解
import org.springframework.context.annotation.Configuration;  // 导入Configuration注解，标记为配置类

/**
 * 自定义监控指标配置类
 * 使用 Micrometer 注册业务级别的自定义指标，用于系统可观测性（Observability）
 *
 * 指标类型说明：
 *   - Counter（计数器）：只增不减的累计值，适用于事件统计（如订单数、缓存命中数）
 *   - DistributionSummary（分布摘要）：记录值的分布情况，适用于金额、耗时等需要统计分位数的场景
 *
 * 采集流程：
 *   应用代码 -> Micrometer Counter/Summary -> Actuator /prometheus 端点 -> Prometheus 采集 -> Grafana 展示
 *
 * 注册的指标清单：
 *   1. bookstore.orders.created   - 创建订单总数（Counter）
 *   2. bookstore.orders.paid      - 支付订单总数（Counter）
 *   3. bookstore.orders.cancelled - 取消订单总数（Counter）
 *   4. bookstore.orders.amount    - 订单金额分布（DistributionSummary）
 *   5. bookstore.stock.deductions - 库存扣减总次数（Counter）
 *   6. bookstore.cache.hits       - 缓存命中次数（Counter）
 *   7. bookstore.cache.misses     - 缓存未命中次数（Counter）
 */
@Configuration  // 标记为Spring配置类
public class MetricsConfig {

    // ==================== 订单相关指标 ====================

    /**
     * 订单创建计数器
     * 每次成功创建订单时 +1，用于监控订单创建速率和总量
     */
    @Bean
    public Counter ordersCreatedCounter(MeterRegistry registry) {  // 注册订单创建计数器
        return Counter.builder("bookstore.orders.created")  // 指标名称
                .description("订单创建总数")  // 指标描述
                .register(registry);  // 注册到MeterRegistry
    }

    /**
     * 订单支付计数器
     * 每次成功支付订单时 +1，用于监控支付转化率和支付速率
     */
    @Bean
    public Counter ordersPaidCounter(MeterRegistry registry) {  // 注册订单支付计数器
        return Counter.builder("bookstore.orders.paid")
                .description("订单支付总数")
                .register(registry);
    }

    /**
     * 订单取消计数器
     * 每次取消订单时 +1，用于监控取消率和取消趋势
     */
    @Bean
    public Counter ordersCancelledCounter(MeterRegistry registry) {  // 注册订单取消计数器
        return Counter.builder("bookstore.orders.cancelled")
                .description("订单取消总数")
                .register(registry);
    }

    /**
     * 订单金额分布摘要
     * 记录每笔订单的金额，可以统计：平均值、最大值、最小值、P50/P95/P99分位数
     * 用于分析客单价分布和消费趋势
     */
    @Bean
    public DistributionSummary ordersAmountSummary(MeterRegistry registry) {  // 注册订单金额分布摘要
        return DistributionSummary.builder("bookstore.orders.amount")  // 指标名称
                .description("订单金额分布统计")  // 指标描述
                .baseUnit("元")  // 基础单位为"元"（人民币）
                .register(registry);  // 注册到MeterRegistry
    }

    // ==================== 库存相关指标 ====================

    /**
     * 库存扣减计数器
     * 每次扣减库存时 +1，用于监控库存操作的频率
     */
    @Bean
    public Counter stockDeductionsCounter(MeterRegistry registry) {  // 注册库存扣减计数器
        return Counter.builder("bookstore.stock.deductions")
                .description("库存扣减总次数")
                .register(registry);
    }

    // ==================== 缓存相关指标 ====================

    /**
     * 缓存命中计数器
     * 每次从缓存中成功获取数据时 +1
     * 与 cache.misses 结合可以计算缓存命中率：hits / (hits + misses)
     */
    @Bean
    public Counter cacheHitsCounter(MeterRegistry registry) {  // 注册缓存命中计数器
        return Counter.builder("bookstore.cache.hits")
                .description("缓存命中次数")
                .register(registry);
    }

    /**
     * 缓存未命中计数器
     * 每次缓存中找不到数据需要查数据库时 +1
     * 高 miss 率可能意味着缓存策略需要优化
     */
    @Bean
    public Counter cacheMissesCounter(MeterRegistry registry) {  // 注册缓存未命中计数器
        return Counter.builder("bookstore.cache.misses")
                .description("缓存未命中次数")
                .register(registry);
    }
}
