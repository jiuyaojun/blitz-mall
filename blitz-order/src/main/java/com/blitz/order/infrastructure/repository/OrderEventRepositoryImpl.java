package com.blitz.order.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blitz.order.domain.model.OrderEvent;
import com.blitz.order.domain.port.OrderEventRepository;
import com.blitz.order.infrastructure.mapper.OrderEventMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Override
    public List<OrderEvent> findDuePending(int limit) {
        LocalDateTime now = LocalDateTime.now();
        return orderEventMapper.selectList(
            new LambdaQueryWrapper<OrderEvent>()
                .eq(OrderEvent::getStatus, OrderEvent.STATUS_PENDING)
                .and(w -> w.isNull(OrderEvent::getNextRetryAt)
                           .or().le(OrderEvent::getNextRetryAt, now))
                .orderByAsc(OrderEvent::getId)
                .last("LIMIT " + limit)
        );
    }

    @Override
    public void markSent(Long id) {
        orderEventMapper.update(null,
            new LambdaUpdateWrapper<OrderEvent>()
                .eq(OrderEvent::getId, id)
                .set(OrderEvent::getStatus, OrderEvent.STATUS_SENT)
                .set(OrderEvent::getSentAt, LocalDateTime.now())
        );
    }

    @Override
    public void markFailed(Long id, int retryCount, LocalDateTime nextRetryAt, boolean giveUp) {
        orderEventMapper.update(null,
            new LambdaUpdateWrapper<OrderEvent>()
                .eq(OrderEvent::getId, id)
                .set(OrderEvent::getRetryCount, retryCount)
                .set(OrderEvent::getNextRetryAt, nextRetryAt)
                .set(OrderEvent::getStatus,
                     giveUp ? OrderEvent.STATUS_FAILED : OrderEvent.STATUS_PENDING)
        );
    }
}
