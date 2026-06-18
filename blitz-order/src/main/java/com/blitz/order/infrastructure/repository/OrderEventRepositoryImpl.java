package com.blitz.order.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.order.domain.model.OrderEvent;
import com.blitz.order.domain.port.OrderEventRepository;
import com.blitz.order.infrastructure.mapper.OrderEventMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderEventRepositoryImpl implements OrderEventRepository {

    private final OrderEventMapper orderEventMapper;

    public OrderEventRepositoryImpl(OrderEventMapper orderEventMapper) {
        this.orderEventMapper = orderEventMapper;
    }

    @Override
    public void save(OrderEvent event) {
        orderEventMapper.insert(event);
    }

    @Override
    public List<OrderEvent> findByOrderId(Long orderId) {
        return orderEventMapper.selectList(
            new LambdaQueryWrapper<OrderEvent>()
                .eq(OrderEvent::getOrderId, orderId)
                .orderByAsc(OrderEvent::getCreatedAt)
        );
    }
}
