package com.blitz.order.domain.port;

import com.blitz.order.domain.model.OrderEvent;

import java.util.List;

public interface OrderEventRepository {
    void save(OrderEvent event);
    List<OrderEvent> findByOrderId(Long orderId);
}
