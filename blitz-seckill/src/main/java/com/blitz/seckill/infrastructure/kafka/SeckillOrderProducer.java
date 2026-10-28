package com.blitz.seckill.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@org.springframework.context.annotation.Profile("!minimal")
@RequiredArgsConstructor
public class SeckillOrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "blitz-seckill-order";

    /** 等 broker 确认的上限。超时即视为发送失败，由调用方回滚 Redis 预扣。 */
    private static final long SEND_TIMEOUT_SECONDS = 3L;

    /**
     * 发送秒杀订单到 Kafka（使用 seckillProductId 作为 key 保证同一商品的消息有序）。
     *
     * <p>这里**必须同步等确认**，不能只挂 {@code whenComplete} 记日志：调用方
     * {@code SeckillApplicationService.executeSeckill()} 用 {@code try / catch}
     * 兜住「发送失败 → 回滚 Redis 预扣库存」，如果 send 立刻返回 void，
     * 异常发生在回调线程里，那个 catch 永远进不去，等于回滚逻辑是死代码。
     *
     * @throws IllegalStateException broker 未在超时内确认，或线程被中断
     */
    public void send(SeckillOrderMessage message) {
        String key = String.valueOf(message.seckillProductId());
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(TOPIC, key, message);
        try {
            SendResult<String, Object> result = future.get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("Seckill order sent: orderNo={}, partition={}, offset={}",
                    message.orderNo(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("发送秒杀订单被中断: " + message.orderNo(), e);
        } catch (ExecutionException | TimeoutException e) {
            log.error("Failed to send seckill order: orderNo={}", message.orderNo(), e);
            throw new IllegalStateException("发送秒杀订单失败: " + message.orderNo(), e);
        }
    }
}
