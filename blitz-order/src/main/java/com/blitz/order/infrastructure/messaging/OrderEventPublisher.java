package com.blitz.order.infrastructure.messaging;

import cn.hutool.json.JSONUtil;
import com.blitz.order.domain.model.Order;
import com.blitz.order.domain.model.OrderEvent;
import com.blitz.order.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_ORDER_EVENT = "blitz-order-event";

    /**
     * 发布订单创建事件
     */
    public void publishOrderCreated(Order order, List<OrderItem> items) {
        String payload = JSONUtil.toJsonStr(Map.of(
            "orderNo", order.getOrderNo(),
            "userId", order.getUserId(),
            "totalAmount", order.getTotalAmount().toString(),
            "items", items.stream().map(i -> Map.of(
                "skuId", i.getSkuId().toString(),
                "skuName", i.getSkuName(),
                "price", i.getPrice().toString(),
                "quantity", i.getQuantity()
            )).toList()
        ));
        send(order.getOrderNo(), OrderEvent.ORDER_CREATED, payload);
    }

    /**
     * 发布订单支付事件
     */
    public void publishOrderPaid(Order order) {
        String payload = JSONUtil.toJsonStr(Map.of(
            "orderNo", order.getOrderNo(),
            "paymentMethod", order.getPaymentMethod(),
            "paymentTime", order.getPaymentTime() != null ? order.getPaymentTime().toString() : null,
            "amount", order.getPayableAmount().toString()
        ));
        send(order.getOrderNo(), OrderEvent.ORDER_PAID, payload);
    }

    /**
     * 发布订单取消事件（用于恢复库存）
     */
    public void publishOrderCancelled(Order order, String reason) {
        String payload = JSONUtil.toJsonStr(Map.of(
            "orderNo", order.getOrderNo(),
            "reason", reason,
            "cancelTime", order.getCancelTime() != null ? order.getCancelTime().toString() : null
        ));
        send(order.getOrderNo(), OrderEvent.ORDER_CANCELLED, payload);
    }

    private void send(String key, String eventType, String payload) {
        CompletableFuture<SendResult<String, String>> future =
            kafkaTemplate.send(TOPIC_ORDER_EVENT, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish order event: type={}, orderNo={}", eventType, key, ex);
            } else {
                log.info("Order event published: type={}, orderNo={}, offset={}",
                    eventType, key, result.getRecordMetadata().offset());
            }
        });
    }
}
