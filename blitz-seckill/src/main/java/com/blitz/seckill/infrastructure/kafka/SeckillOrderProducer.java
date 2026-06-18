package com.blitz.seckill.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "blitz-seckill-order";

    /** 发送秒杀订单到 Kafka（使用 seckillProductId 作为 key 保证同一商品的消息有序） */
    public void send(SeckillOrderMessage message) {
        String key = String.valueOf(message.seckillProductId());
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(TOPIC, key, message);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send seckill order: orderNo={}", message.orderNo(), ex);
            } else {
                log.info("Seckill order sent: orderNo={}, partition={}, offset={}",
                    message.orderNo(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
    }
}
