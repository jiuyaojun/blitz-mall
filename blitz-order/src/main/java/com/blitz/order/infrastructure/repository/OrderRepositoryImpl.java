package com.blitz.order.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.order.domain.model.Order;
import com.blitz.order.domain.port.OrderRepository;
import com.blitz.order.infrastructure.mapper.OrderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;

    public OrderRepositoryImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orderMapper.selectById(id));
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return Optional.ofNullable(
            orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
            )
        );
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime)
        );
    }

    @Override
    public List<Order> findByUserIdAndStatus(Long userId, Integer status) {
        return orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, status)
                .orderByDesc(Order::getCreateTime)
        );
    }

    @Override
    public Page<Order> findByUserIdPage(Long userId, Page<Order> page) {
        return orderMapper.selectPage(page,
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime)
        );
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            orderMapper.insert(order);
        } else {
            orderMapper.updateById(order);
        }
        return order;
    }

    @Override
    public int updateStatusWithVersion(Long id, Integer newStatus, Integer oldVersion) {
        return orderMapper.update(null,
            new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getVersion, oldVersion)
                .set(Order::getStatus, newStatus)
        );
    }
}
