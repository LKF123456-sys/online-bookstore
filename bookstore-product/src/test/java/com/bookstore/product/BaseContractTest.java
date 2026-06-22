package com.bookstore.product;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Spring Cloud Contract 验证测试基类
 * Product 服务作为契约提供方，通过此基类自动生成验证测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseContractTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
