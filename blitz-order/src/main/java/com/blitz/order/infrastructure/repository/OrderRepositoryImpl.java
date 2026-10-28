package com.blitz.order.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
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
    public Page<Order> findByUserIdPage(Long userId, Page<Order> page, String status) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
            .eq(Order::getUserId, userId);
        // Map frontend string status codes to numeric codes
        if (status != null && !status.isEmpty()) {
            Integer code = null;
            String s = status.toUpperCase();
            if (s.equals("PENDING_PAYMENT") || s.equals("PENDING")) code = 0;
            else if (s.equals("PAID")) code = 1;
            else if (s.equals("SHIPPED")) code = 2;
            else if (s.equals("RECEIVED")) code = 3;
            else if (s.equals("CANCELLED")) code = 4;
            else if (s.equals("REFUNDED")) code = 5;
            if (code != null) qw.eq(Order::getStatus, code);
        }
        return orderMapper.selectPage(page, qw.orderByDesc(Order::getCreateTime));
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            orderMapper.insert(order);
        } else {
            // 注册了 OptimisticLockerInnerInterceptor 之后，updateById 会带上
            // WHERE version = ? 并自动 version + 1。影响行数为 0 意味着
            // 「这条订单在我读出来之后被别人改过」——必须抛出去，
            // 否则并发改状态会静默覆盖（例如同时发货和退款，后提交的直接赢）。
            int affected = orderMapper.updateById(order);
            if (affected == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                        "订单状态已被并发修改，请重试");
            }
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
                // 与 SkuStock.deductStock 同一个坑：只比版本不改版本，
                // 第二次调用仍会命中同一个 oldVersion，CAS 失效。
                .setSql("version = version + 1")
        );
    }
}
