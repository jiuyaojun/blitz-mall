package com.blitz.order.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.order.domain.model.OrderItem;
import com.blitz.order.domain.port.OrderItemRepository;
import com.blitz.order.infrastructure.mapper.OrderItemMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemMapper orderItemMapper;

    public OrderItemRepositoryImpl(OrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
    }

    @Override
    public void saveBatch(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (item.getId() == null) {
                orderItemMapper.insert(item);
            } else {
                orderItemMapper.updateById(item);
            }
        }
    }
}
