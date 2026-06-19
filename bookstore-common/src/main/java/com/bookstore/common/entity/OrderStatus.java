package com.bookstore.common.entity;

/**
 * 订单状态枚举
 * 使用枚举替代硬编码的中文字符串，避免拼写错误，便于状态机管理。
 * 枚举值与数据库中存储的状态字符串一一对应。
 */
public enum OrderStatus {

    PENDING_PAYMENT("待支付"),
    PAID("已支付"),
    SHIPPED("已发货"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    /**
     * 根据数据库中的状态字符串查找对应的枚举值
     * @param status 数据库中的状态字符串
     * @return 对应的枚举值，找不到则返回 null
     */
    public static OrderStatus fromStatus(String status) {
        for (OrderStatus s : values()) {
            if (s.status.equals(status)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return status;
    }
}
