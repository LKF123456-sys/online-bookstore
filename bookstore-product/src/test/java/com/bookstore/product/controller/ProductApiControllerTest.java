package com.bookstore.product.controller;

import com.bookstore.common.api.dto.ProductQueryDTO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ProductVO;
import com.bookstore.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductApiController.class)
@TestPropertySource(properties = {
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.nacos.config.enabled=false",
    "spring.ai.openai.api-key=test"
})
class ProductApiControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;

    private ProductVO p(String id, String name) {
        ProductVO p = new ProductVO();
        p.setId(id); p.setName(name);
        p.setPrice(new BigDecimal("29.90")); p.setAuthor("Author");
        p.setCategory("Tech"); p.setStock(10);
        return p;
    }

    @Test void listProducts_Ok() throws Exception {
        when(productService.getProductList(any())).thenReturn(new PageResult<>(List.of(p("1","Java")), 1L, 1, 10));
        mockMvc.perform(get("/api/product/list")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test void productById_Ok() throws Exception {
        when(productService.getProductById("1")).thenReturn(p("1","Java"));
        mockMvc.perform(get("/api/product/1")).andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("Java"));
    }

    @Test void productById_404() throws Exception {
        when(productService.getProductById("999")).thenThrow(new com.bookstore.common.exception.BusinessException(404, "\u4e0d\u5b58\u5728"));
        mockMvc.perform(get("/api/product/999")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(404));
    }

    @Test void recommend_Ok() throws Exception {
        when(productService.getRecommendProducts(5)).thenReturn(List.of(p("1","R1")));
        mockMvc.perform(get("/api/product/recommend")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test void hot_Ok() throws Exception {
        when(productService.getHotProducts(5)).thenReturn(List.of(p("1","H1")));
        mockMvc.perform(get("/api/product/hot")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test void listProducts_Empty() throws Exception {
        when(productService.getProductList(any())).thenReturn(new PageResult<>(List.of(), 0L, 1, 10));
        mockMvc.perform(get("/api/product/list")).andExpect(status().isOk()).andExpect(jsonPath("$.data.records").isEmpty());
    }
}
