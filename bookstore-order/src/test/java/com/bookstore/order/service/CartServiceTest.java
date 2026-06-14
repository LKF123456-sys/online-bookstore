package com.bookstore.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.dto.CartAddDTO;
import com.bookstore.common.api.vo.CartVO;
import com.bookstore.common.api.vo.ProductVO;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.entity.Cart;
import com.bookstore.common.entity.CartItem;
import com.bookstore.common.util.SnowflakeIdGenerator;
import com.bookstore.order.mapper.CartItemMapper;
import com.bookstore.order.mapper.CartMapper;
import com.bookstore.order.feign.ProductFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private ProductFeignClient productFeignClient;
    @Mock
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @InjectMocks
    private CartService cartService;

    private ProductVO sampleProduct;
    private CartItem sampleCartItem;

    @BeforeEach
    void setUp() {
        sampleProduct = new ProductVO();
        sampleProduct.setId("P001");
        sampleProduct.setName("Java编程思想");
        sampleProduct.setPrice(new BigDecimal("89.00"));
        sampleProduct.setStock(100);
        sampleProduct.setImageUrl("http://img.example.com/java.jpg");

        sampleCartItem = new CartItem();
        sampleCartItem.setItemid("ITEM001");
        sampleCartItem.setCartid("user001");
        sampleCartItem.setProductId("P001");
        sampleCartItem.setQuantity(2);
    }

    // ==================== 获取购物车测试 ====================

    @Nested
    @DisplayName("获取购物车")
    class GetCartTests {

        @Test
        @DisplayName("获取成功 — 购物车有商品，包含商品详情")
        void shouldReturnCartWithItems() {
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleCartItem));
            when(productFeignClient.getProductById("P001"))
                    .thenReturn(Result.success(sampleProduct));

            CartVO cart = cartService.getCart("user001");

            assertNotNull(cart);
            assertEquals(1, cart.getItems().size());
            assertEquals(2, cart.getTotalCount());
            assertEquals("Java编程思想", cart.getItems().get(0).getName());
            assertEquals(new BigDecimal("89.00"), cart.getItems().get(0).getPrice());
            assertEquals(new BigDecimal("178.00"), cart.getItems().get(0).getSubtotal());
        }

        @Test
        @DisplayName("获取成功 — 购物车为空时返回空列表")
        void shouldReturnEmptyCart() {
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            CartVO cart = cartService.getCart("user001");

            assertNotNull(cart);
            assertTrue(cart.getItems().isEmpty());
            assertEquals(0, cart.getTotalCount());
        }

        @Test
        @DisplayName("商品服务不可用时 — 优雅降级显示默认信息")
        void shouldDegradeWhenProductServiceUnavailable() {
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleCartItem));
            when(productFeignClient.getProductById("P001"))
                    .thenThrow(new RuntimeException("Service unavailable"));

            CartVO cart = cartService.getCart("user001");

            assertNotNull(cart);
            assertEquals(1, cart.getItems().size());
            assertEquals("商品信息获取失败", cart.getItems().get(0).getName());
            assertEquals(BigDecimal.ZERO, cart.getItems().get(0).getPrice());
            assertEquals(BigDecimal.ZERO, cart.getItems().get(0).getSubtotal());
        }
    }

    // ==================== 添加商品到购物车测试 ====================

    @Nested
    @DisplayName("添加商品到购物车")
    class AddToCartTests {

        @Test
        @DisplayName("添加成功 — 购物车已存在且商品不在购物车中")
        void shouldAddNewItemToExistingCart() {
            CartAddDTO dto = new CartAddDTO();
            dto.setProductId("P001");
            dto.setQuantity(1);

            when(cartMapper.selectById("user001")).thenReturn(new Cart());
            when(productFeignClient.getProductById("P001")).thenReturn(Result.success(sampleProduct));
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(snowflakeIdGenerator.nextOrderId("CI")).thenReturn("CI123456");
            when(cartItemMapper.insert(any(CartItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> cartService.addToCart("user001", dto));

            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemMapper).insert(captor.capture());
            CartItem saved = captor.getValue();
            assertEquals("user001", saved.getCartid());
            assertEquals("P001", saved.getProductId());
            assertEquals(1, saved.getQuantity());
        }

        @Test
        @DisplayName("添加成功 — 购物车不存在时自动创建")
        void shouldCreateCartIfNotExists() {
            CartAddDTO dto = new CartAddDTO();
            dto.setProductId("P001");
            dto.setQuantity(1);

            when(cartMapper.selectById("user001")).thenReturn(null);
            when(cartMapper.insert(any(Cart.class))).thenReturn(1);
            when(productFeignClient.getProductById("P001")).thenReturn(Result.success(sampleProduct));
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(snowflakeIdGenerator.nextOrderId("CI")).thenReturn("CI789012");
            when(cartItemMapper.insert(any(CartItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> cartService.addToCart("user001", dto));

            verify(cartMapper).insert(any(Cart.class));
            verify(cartItemMapper).insert(any(CartItem.class));
        }

        @Test
        @DisplayName("添加成功 — 商品已在购物车中时增加数量")
        void shouldIncrementQuantityWhenItemExists() {
            CartAddDTO dto = new CartAddDTO();
            dto.setProductId("P001");
            dto.setQuantity(3);

            when(cartMapper.selectById("user001")).thenReturn(new Cart());
            when(productFeignClient.getProductById("P001")).thenReturn(Result.success(sampleProduct));
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleCartItem);
            when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> cartService.addToCart("user001", dto));

            assertEquals(5, sampleCartItem.getQuantity()); // 原来2 + 新增3
            verify(cartItemMapper).updateById(sampleCartItem);
            verify(cartItemMapper, never()).insert(any(CartItem.class));
        }

        @Test
        @DisplayName("添加失败 — 商品不存在时抛出异常")
        void shouldThrowWhenProductNotFound() {
            CartAddDTO dto = new CartAddDTO();
            dto.setProductId("INVALID");
            dto.setQuantity(1);

            when(cartMapper.selectById("user001")).thenReturn(new Cart());
            when(productFeignClient.getProductById("INVALID")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> cartService.addToCart("user001", dto));
            assertEquals("商品不存在", ex.getMessage());
        }
    }

    // ==================== 更新数量测试 ====================

    @Nested
    @DisplayName("更新购物车商品数量")
    class UpdateQuantityTests {

        @Test
        @DisplayName("更新成功 — 设置新数量")
        void shouldUpdateQuantity() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleCartItem);
            when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> cartService.updateQuantity("user001", "ITEM001", 5));

            assertEquals(5, sampleCartItem.getQuantity());
            verify(cartItemMapper).updateById(sampleCartItem);
        }

        @Test
        @DisplayName("更新成功 — 数量为0时删除购物车项")
        void shouldDeleteItemWhenQuantityZero() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleCartItem);

            assertDoesNotThrow(() -> cartService.updateQuantity("user001", "ITEM001", 0));

            verify(cartItemMapper).deleteById("ITEM001");
            verify(cartItemMapper, never()).updateById(any(CartItem.class));
        }

        @Test
        @DisplayName("更新成功 — 数量为负数时删除购物车项")
        void shouldDeleteItemWhenQuantityNegative() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleCartItem);

            assertDoesNotThrow(() -> cartService.updateQuantity("user001", "ITEM001", -1));

            verify(cartItemMapper).deleteById("ITEM001");
        }

        @Test
        @DisplayName("更新失败 — 购物车项不存在时抛出异常")
        void shouldThrowWhenItemNotFound() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> cartService.updateQuantity("user001", "INVALID", 5));
            assertEquals("购物车项不存在", ex.getMessage());
        }
    }

    // ==================== 删除购物车项测试 ====================

    @Nested
    @DisplayName("从购物车移除商品")
    class RemoveFromCartTests {

        @Test
        @DisplayName("删除成功 — 按购物车项ID删除")
        void shouldRemoveByItemId() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleCartItem);

            assertDoesNotThrow(() -> cartService.removeFromCart("user001", "ITEM001"));

            verify(cartItemMapper).deleteById("ITEM001");
        }

        @Test
        @DisplayName("删除失败 — 购物车项不存在时抛出异常")
        void shouldThrowWhenItemNotFound() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> cartService.removeFromCart("user001", "INVALID"));
            assertEquals("购物车项不存在", ex.getMessage());
        }

        @Test
        @DisplayName("删除成功 — 按商品ID删除")
        void shouldRemoveByProductId() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleCartItem);

            assertDoesNotThrow(() -> cartService.removeByProductId("user001", "P001"));

            verify(cartItemMapper).deleteById("ITEM001");
        }

        @Test
        @DisplayName("删除 — 商品不在购物车时不报错")
        void shouldNotThrowWhenProductNotInCart() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertDoesNotThrow(() -> cartService.removeByProductId("user001", "P999"));

            verify(cartItemMapper, never()).deleteById(anyString());
        }
    }

    // ==================== 清空购物车测试 ====================

    @Nested
    @DisplayName("清空购物车")
    class ClearCartTests {

        @Test
        @DisplayName("清空成功 — 删除所有购物车项")
        void shouldClearAllItems() {
            when(cartItemMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);

            assertDoesNotThrow(() -> cartService.clearCart("user001"));

            verify(cartItemMapper).delete(any(LambdaQueryWrapper.class));
        }
    }

    // ==================== 按商品ID更新数量测试 ====================

    @Nested
    @DisplayName("通过商品ID更新数量")
    class UpdateQuantityByProductIdTests {

        @Test
        @DisplayName("更新成功")
        void shouldUpdateByProductId() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleCartItem);
            when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

            assertDoesNotThrow(() -> cartService.updateQuantityByProductId("user001", "P001", 10));

            assertEquals(10, sampleCartItem.getQuantity());
        }

        @Test
        @DisplayName("数量为0时删除")
        void shouldDeleteWhenQuantityZero() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleCartItem);

            assertDoesNotThrow(() -> cartService.updateQuantityByProductId("user001", "P001", 0));

            verify(cartItemMapper).deleteById("ITEM001");
        }

        @Test
        @DisplayName("商品不在购物车时抛出异常")
        void shouldThrowWhenProductNotInCart() {
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> cartService.updateQuantityByProductId("user001", "P999", 5));
            assertEquals("购物车项不存在", ex.getMessage());
        }
    }
}
