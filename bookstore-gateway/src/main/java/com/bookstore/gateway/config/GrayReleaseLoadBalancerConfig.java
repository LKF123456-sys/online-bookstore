package com.bookstore.gateway.config;

import com.bookstore.common.config.GrayReleaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 灰度发布 LoadBalancer 配置
 *
 * 根据请求头 X-Gray-Version 筛选注册中心中具有对应 gray-version 元数据的实例。
 * 部署灰度实例时需添加 Nacos metadata: gray-version=v2
 *
 * 使用方式：
 * 1. 部署 v2 版本的服务实例，注册到 Nacos 时添加 metadata.gray-version=v2
 * 2. 在 Nacos 配置中启用灰度规则
 * 3. 灰度用户请求到 Gateway 时，GrayReleaseFilter 注入 X-Gray-Version 头
 * 4. 此配置根据该头筛选出对应版本的服务实例
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class GrayReleaseLoadBalancerConfig {

    private final ObjectProvider<GrayReleaseConfig> grayReleaseConfigProvider;

    public GrayReleaseLoadBalancerConfig(ObjectProvider<GrayReleaseConfig> grayReleaseConfigProvider) {
        this.grayReleaseConfigProvider = grayReleaseConfigProvider;
    }

    /**
     * 自定义 LoadBalancer — 支持灰度版本实例筛选
     *
     * 当请求携带 X-Gray-Version 头时，优先筛选匹配的实例；
     * 无匹配实例时降级到所有可用实例。
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(
            Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {

        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME, "bookstore-gateway");

        ObjectProvider<ServiceInstanceListSupplier> delegateProvider =
                loadBalancerClientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class);

        ObjectProvider<ServiceInstanceListSupplier> grayProvider = new ObjectProvider<ServiceInstanceListSupplier>() {
            @Override
            public ServiceInstanceListSupplier getObject() {
                return new GrayAwareServiceInstanceListSupplier(delegateProvider.getObject());
            }

            @Override
            public ServiceInstanceListSupplier getObject(Object... args) {
                return getObject();
            }

            @Override
            public ServiceInstanceListSupplier getIfAvailable() {
                ServiceInstanceListSupplier delegate = delegateProvider.getIfAvailable();
                return delegate != null ? new GrayAwareServiceInstanceListSupplier(delegate) : null;
            }

            @Override
            public ServiceInstanceListSupplier getIfUnique() {
                ServiceInstanceListSupplier delegate = delegateProvider.getIfUnique();
                return delegate != null ? new GrayAwareServiceInstanceListSupplier(delegate) : null;
            }

            @Override
            public ServiceInstanceListSupplier getIfAvailable(java.util.function.Supplier<ServiceInstanceListSupplier> defaultSupplier) {
                ServiceInstanceListSupplier delegate = delegateProvider.getIfAvailable();
                return delegate != null ? new GrayAwareServiceInstanceListSupplier(delegate) : defaultSupplier.get();
            }
        };

        return new RoundRobinLoadBalancer(grayProvider, serviceId);
    }

    /**
     * 灰度感知的服务实例列表供应商
     * 从 ServiceInstanceListSupplier 获取实例列表后，按 X-Gray-Version 筛选
     */
    public class GrayAwareServiceInstanceListSupplier implements ServiceInstanceListSupplier {

        private final ServiceInstanceListSupplier delegate;

        public GrayAwareServiceInstanceListSupplier(ServiceInstanceListSupplier delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getServiceId() {
            return delegate.getServiceId();
        }

        @Override
        public Flux<List<ServiceInstance>> get() {
            return delegate.get();
        }

        @Override
        public Flux<List<ServiceInstance>> get(Request request) {
            return delegate.get(request).map(instances -> {
                GrayReleaseConfig config = grayReleaseConfigProvider.getIfAvailable();
                if (config == null || !config.isEnabled()) {
                    return instances;
                }

                String grayVersion = null;
                if (request.getContext() instanceof RequestDataContext ctx) {
                    var headers = ctx.getClientRequest().getHeaders();
                    if (headers != null) {
                        grayVersion = headers.getFirst("X-Gray-Version");
                    }
                }

                if (grayVersion == null || grayVersion.isEmpty()) {
                    return instances;
                }

                final String version = grayVersion;

                List<ServiceInstance> grayInstances = instances.stream()
                        .filter(inst -> {
                            String metaVersion = inst.getMetadata().get("gray-version");
                            return version.equals(metaVersion);
                        })
                        .collect(Collectors.toList());

                if (!grayInstances.isEmpty()) {
                    log.debug("Gray LB: {} instances match gray-version={} for service {}",
                            grayInstances.size(), grayVersion, instances.get(0).getServiceId());
                    return grayInstances;
                }

                log.warn("Gray LB: no instance with gray-version={} found for {}, falling back to all instances",
                        grayVersion, instances.get(0).getServiceId());
                return instances;
            });
        }
    }
}
