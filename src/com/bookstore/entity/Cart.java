package com.bookstore.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items = new ArrayList<>();

    // 添加商品
    public void addItem(CartItem item) {
        for (CartItem cartItem : items) {
            if (cartItem.getProduct().getProductid().equals(item.getProduct().getProductid())) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                return;
            }
        }
        items.add(item);
    }

    // 删除商品
    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getProductid().equals(productId));
    }

    // 清空购物车（✅ 100% 正确，方法名 clear）
    public void clear() {
        items.clear();
    }

    // 计算总金额
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getSubTotal());
        }
        return total;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }
}