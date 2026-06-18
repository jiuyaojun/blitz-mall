package com.blitz.order.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderVO(
    Long id,
    String orderNo,
    Long userId,
    Integer status,
    BigDecimal totalAmount,
    BigDecimal freightAmount,
    BigDecimal discountAmount,
    Integer paymentMethod,
    LocalDateTime paymentTime,
    LocalDateTime deliveryTime,
    LocalDateTime receiveTime,
    LocalDateTime cancelTime,
    String receiverName,
    String receiverPhone,
    String receiverAddress,
    String note,
    LocalDateTime createTime,
    List<OrderItemVO> items
) {}
