package com.blitz.order.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer status; // DB column, mapped to OrderStatus
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal discountAmount;
    private Integer paymentMethod;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime cancelTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String note;
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<OrderItem> items;

    // ---- Behavior methods ----

    public OrderStatus getOrderStatus() {
        return OrderStatus.fromCode(this.status);
    }

    /** 支付订单 */
    public void pay(Integer paymentMethod) {
        OrderStatus current = getOrderStatus();
        current.validateTransition(OrderStatus.PAID);
        this.status = OrderStatus.PAID.getCode();
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
    }

    /** 发货 */
    public void ship() {
        OrderStatus current = getOrderStatus();
        current.validateTransition(OrderStatus.SHIPPED);
        this.status = OrderStatus.SHIPPED.getCode();
        this.deliveryTime = LocalDateTime.now();
    }

    /** 确认收货 */
    public void receive() {
        OrderStatus current = getOrderStatus();
        current.validateTransition(OrderStatus.RECEIVED);
        this.status = OrderStatus.RECEIVED.getCode();
        this.receiveTime = LocalDateTime.now();
    }

    /** 取消订单（手动或超时） */
    public void cancel() {
        OrderStatus current = getOrderStatus();
        current.validateTransition(OrderStatus.CANCELLED);
        this.status = OrderStatus.CANCELLED.getCode();
        this.cancelTime = LocalDateTime.now();
    }

    /** 退款 */
    public void refund() {
        OrderStatus current = getOrderStatus();
        current.validateTransition(OrderStatus.REFUNDED);
        this.status = OrderStatus.REFUNDED.getCode();
    }

    /** 是否可取消（仅待支付） */
    public boolean canCancel() {
        return getOrderStatus() == OrderStatus.PENDING_PAYMENT;
    }

    /** 计算实付金额 */
    public BigDecimal getPayableAmount() {
        BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        BigDecimal freight = freightAmount != null ? freightAmount : BigDecimal.ZERO;
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        return total.add(freight).subtract(discount);
    }
}
