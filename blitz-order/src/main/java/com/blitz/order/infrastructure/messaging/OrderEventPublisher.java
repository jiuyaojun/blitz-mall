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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@org.springframework.context.annotation.Profile("!minimal")
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

    /**
     * 事务提交后再发送。
     *
     * <p>原来是在 {@code createOrder()} 的 {@code @Transactional} 里直接发，
     * 事务提交前消息就已经出去了。一旦事务回滚（库存扣减失败、乐观锁冲突等），
     * 下游已经收到一条「订单已创建」的事件，而库里根本没有这条订单。
     *
     * <p>这里用 {@link TransactionSynchronization#afterCommit()} 把发送推到提交之后：
     * 提交失败就不发，代价是「本地事务成功但发送失败」的窗口需要靠重试或 outbox 兜底。
     */
    private void send(String key, String eventType, String payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(key, eventType, payload);
                }
            });
            return;
        }
        doSend(key, eventType, payload);
    }

    private void doSend(String key, String eventType, String payload) {
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
