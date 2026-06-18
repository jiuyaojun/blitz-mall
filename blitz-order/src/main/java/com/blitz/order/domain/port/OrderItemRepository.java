package com.blitz.order.domain.port;

import com.blitz.order.domain.model.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    List<OrderItem> findByOrderId(Long orderId);
    void saveBatch(List<OrderItem> items);
}
