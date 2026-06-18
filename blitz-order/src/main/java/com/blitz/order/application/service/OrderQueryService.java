package com.blitz.order.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.common.result.PageResult;
import com.blitz.order.domain.model.Order;
import com.blitz.order.domain.model.OrderItem;
import com.blitz.order.domain.port.OrderRepository;
import com.blitz.order.api.dto.OrderVO;
import com.blitz.order.api.dto.OrderItemVO;
import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final com.blitz.order.domain.port.OrderItemRepository orderItemRepository;

    public OrderVO getById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return toVO(order);
    }

    public OrderVO getByOrderNo(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return toVO(order);
    }

    public List<OrderVO> listByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
            .map(this::toVO).toList();
    }

    public PageResult<OrderVO> pageByUser(Long userId, int page, int size) {
        Page<Order> orderPage = orderRepository.findByUserIdPage(
            userId, new Page<>(page, size));
        List<OrderVO> records = orderPage.getRecords().stream()
            .map(this::toVO).toList();
        return new PageResult<>(records, orderPage.getTotal(), page, size);
    }

    private OrderVO toVO(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemVO> itemVOs = items.stream().map(i ->
            new OrderItemVO(i.getId(), i.getSkuId(), i.getSkuName(),
                i.getSkuSpec(), i.getPrice(), i.getQuantity())
        ).toList();
        return new OrderVO(
            order.getId(), order.getOrderNo(), order.getUserId(),
            order.getStatus(), order.getTotalAmount(), order.getFreightAmount(),
            order.getDiscountAmount(), order.getPaymentMethod(),
            order.getPaymentTime(), order.getDeliveryTime(),
            order.getReceiveTime(), order.getCancelTime(),
            order.getReceiverName(), order.getReceiverPhone(),
            order.getReceiverAddress(), order.getNote(),
            order.getCreateTime(), itemVOs
        );
    }
}
