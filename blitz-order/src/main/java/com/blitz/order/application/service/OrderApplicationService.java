package com.blitz.order.application.service;

import cn.hutool.core.util.IdUtil;
import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.order.domain.model.*;
import com.blitz.order.domain.port.OrderEventRepository;
import com.blitz.order.domain.port.OrderItemRepository;
import com.blitz.order.domain.port.OrderRepository;
import com.blitz.order.infrastructure.messaging.OrderEventPublisher;
import com.blitz.order.infrastructure.mapper.OrderOperateLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderOperateLogMapper orderOperateLogMapper;
    private final StockDeductionService stockDeductionService;

    /**
     * 创建订单（核心链路）
     * 1. 校验商品 + 锁库存（乐观锁重试3次）
     * 2. 生成订单号（雪花ID）
     * 3. 保存订单 + 订单项
     * 4. 发布 OrderCreatedEvent → Kafka
     */
    @Transactional
    public Order createOrder(Long userId, List<CreateOrderItem> items,
                             String receiverName, String receiverPhone,
                             String receiverAddress, String note) {
        // 1. 扣减库存（乐观锁，最多重试 3 次）
        stockDeductionService.deductWithRetry(items, 3);

        // 2. 计算金额
        BigDecimal totalAmount = items.stream()
            .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 生成订单号
        String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setNote(note);
        orderRepository.save(order);

        // 5. 创建订单项（SKU 快照）
        List<OrderItem> orderItems = items.stream().map(i -> {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setSkuId(i.skuId());
            item.setSkuName(i.skuName());
            item.setSkuSpec(i.skuSpec());
            item.setPrice(i.price());
            item.setQuantity(i.quantity());
            return item;
        }).toList();
        orderItemRepository.saveBatch(orderItems);

        // 6. 保存领域事件
        OrderEvent event = OrderEvent.create(order.getId(),
            OrderEvent.ORDER_CREATED, "{\"orderNo\":\"" + orderNo + "\"}");
        orderEventRepository.save(event);

        // 7. 保存操作日志
        orderOperateLogMapper.insert(
            OrderOperateLog.of(order.getId(), "ORDER_CREATED", "USER", "用户下单")
        );

        // 8. 异步发布 Kafka 事件
        orderEventPublisher.publishOrderCreated(order, orderItems);

        log.info("Order created: orderNo={}, userId={}, amount={}", orderNo, userId, totalAmount);
        return order;
    }

    /**
     * 支付订单
     */
    @Transactional
    public void payOrder(Long orderId, Long userId, Integer paymentMethod) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        order.pay(paymentMethod); // 状态机验证在 pay() 内部
        orderRepository.save(order);

        // 保存事件
        OrderEvent event = OrderEvent.create(order.getId(),
            OrderEvent.ORDER_PAID, "{\"paymentMethod\":" + paymentMethod + "}");
        orderEventRepository.save(event);

        // 操作日志
        orderOperateLogMapper.insert(
            OrderOperateLog.of(order.getId(), "ORDER_PAID", "USER", "用户支付")
        );

        // Kafka 异步通知
        orderEventPublisher.publishOrderPaid(order);

        log.info("Order paid: orderNo={}, paymentMethod={}", order.getOrderNo(), paymentMethod);
    }

    /**
     * 取消订单（手动取消/超时取消）
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        order.cancel(); // 状态机验证
        orderRepository.save(order);

        // 恢复库存（补偿操作）
        stockDeductionService.restoreStock(orderId);

        // 保存事件
        OrderEvent event = OrderEvent.create(order.getId(),
            OrderEvent.ORDER_CANCELLED, "{\"reason\":\"" + reason + "\"}");
        orderEventRepository.save(event);

        // 操作日志
        orderOperateLogMapper.insert(
            OrderOperateLog.of(order.getId(), "ORDER_CANCELLED", "USER", reason)
        );

        // Kafka 异步通知
        orderEventPublisher.publishOrderCancelled(order, reason);

        log.info("Order cancelled: orderNo={}, reason={}", order.getOrderNo(), reason);
    }

    /**
     * 发货（管理员操作）
     */
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.ship(); // 状态机验证
        orderRepository.save(order);

        // 保存事件
        OrderEvent event = OrderEvent.create(order.getId(),
            OrderEvent.ORDER_SHIPPED, "{}");
        orderEventRepository.save(event);

        // 操作日志
        orderOperateLogMapper.insert(
            OrderOperateLog.of(order.getId(), "ORDER_SHIPPED", "ADMIN", "管理员发货")
        );

        log.info("Order shipped: orderNo={}", order.getOrderNo());
    }

    /**
     * 确认收货
     */
    @Transactional
    public void receiveOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        order.receive(); // 状态机验证
        orderRepository.save(order);

        // 操作日志
        orderOperateLogMapper.insert(
            OrderOperateLog.of(order.getId(), "ORDER_RECEIVED", "USER", "用户确认收货")
        );

        log.info("Order received: orderNo={}", order.getOrderNo());
    }

    // ---- DTO ----

    public record CreateOrderItem(
        Long skuId, String skuName, String skuSpec,
        BigDecimal price, Integer quantity
    ) {}
}
