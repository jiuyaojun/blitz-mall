package com.blitz.order.domain.model;

import lombok.Getter;

/**
 * 订单状态枚举（充血：内聚状态流转规则）
 * <pre>
 * PENDING_PAYMENT → PAID → SHIPPED → RECEIVED
 * PENDING_PAYMENT → CANCELLED (超时/手动)
 * PAID → REFUNDED
 * </pre>
 */
@Getter
public enum OrderStatus {

    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    RECEIVED(3, "已收货"),
    CANCELLED(4, "已取消"),
    REFUNDED(5, "已退款");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 判断当前状态能否流转到目标状态
     */
    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING_PAYMENT -> target == PAID || target == CANCELLED;
            case PAID -> target == SHIPPED || target == REFUNDED;
            case SHIPPED -> target == RECEIVED;
            case RECEIVED, CANCELLED, REFUNDED -> false;
        };
    }

    /**
     * 流转到目标状态，失败抛异常
     */
    public void validateTransition(OrderStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException(
                String.format("Invalid order state transition: %s → %s", this.desc, target.desc)
            );
        }
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
}
