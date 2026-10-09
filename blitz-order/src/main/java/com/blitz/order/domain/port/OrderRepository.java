package com.blitz.order.domain.port;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.order.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdAndStatus(Long userId, Integer status);
    Page<Order> findByUserIdPage(Long userId, Page<Order> page, String status);
    Order save(Order order);
    int updateStatusWithVersion(Long id, Integer newStatus, Integer oldVersion);
}
