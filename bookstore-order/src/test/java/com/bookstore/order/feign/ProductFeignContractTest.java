package com.bookstore.order.feign;

import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.ProductVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 消费者端契约测试 — 验证 Feign 客户端与 Product 服务的通信契约
 *
 * 通过 StubRunner 启动本地 Mock 服务（基于 Product 服务发布的 contracts），
 * 无需真实启动 bookstore-product 即可验证 Feign 接口兼容性。
 */
@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.bookstore:bookstore-product:+:stubs:8090",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class ProductFeignContractTest {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Test
    void shouldGetProductById() {
        Result<ProductVO> result = productFeignClient.getProductById("101");
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getProductid()).isEqualTo("101");
    }

    @Test
    void shouldUpdateStock() {
        // 契约定义：quantity=-1 表示扣减1件，返回200
        productFeignClient.updateStock("101", -1);
        // 若接口无异常即通过（void方法）
    }

    @Test
    void shouldBatchGetProducts() {
        Result<List<ProductVO>> result = productFeignClient.batchGetProducts("101,102,103");
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).hasSize(2);
    }
}
