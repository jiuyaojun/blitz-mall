package com.blitz.order.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_event")
public class OrderEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String eventType; // ORDER_CREATED, ORDER_PAID, ORDER_SHIPPED, ORDER_CANCELLED
    private String eventData; // JSON

    /** 投递状态：PENDING 待投递 / SENT 已投递 / FAILED 超过重试上限。 */
    private String status;
    private Integer retryCount;
    private LocalDateTime sentAt;
    private LocalDateTime nextRetryAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";

    // Event type constants
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_PAID = "ORDER_PAID";
    public static final String ORDER_SHIPPED = "ORDER_SHIPPED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";

    public static OrderEvent create(Long orderId, String eventType, String eventData) {
        OrderEvent e = new OrderEvent();
        e.orderId = orderId;
        e.eventType = eventType;
        e.eventData = eventData;
        return e;
    }
}
