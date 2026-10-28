package com.blitz.order.infrastructure.outbox;

import com.blitz.order.domain.model.OrderEvent;
import com.blitz.order.domain.port.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * outbox 中继器：把 {@code order_event} 里 PENDING 的记录投递到 Kafka。
 *
 * <p>为什么需要它：业务事务只保证「事件落库」，不保证「消息发出」。
 * 如果直接在事务里发 Kafka，会出现两种错位——
 * 事务提交前消息已出门（回滚即有幽灵事件），或者事务提交了但发送失败没人补。
 * 把发送挪到这里之后，只有 commit 成功的事件才会被扫到，发送失败还能重试。
 *
 * <p>代价也明确：事件投递有最多一个扫描周期的延迟（默认 5 秒），
 * 并且「已提交但始终发不出去」只能靠重试上限 + FAILED 状态暴露出来，不是无解的保证。
 */
@Slf4j
@Component
@Profile("!minimal")
@RequiredArgsConstructor
public class OrderEventRelay {

    private static final String TOPIC = "blitz-order-event";
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRY = 5;
    private static final long SEND_TIMEOUT_SECONDS = 3L;

    private final OrderEventRepository orderEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(
        fixedDelayString = "${blitz.outbox.interval-ms:5000}",
        initialDelayString = "${blitz.outbox.initial-delay-ms:10000}")
    public void relayDueEvents() {
        List<OrderEvent> due;
        try {
            due = orderEventRepository.findDuePending(BATCH_SIZE);
        } catch (Exception e) {
            // 扫库本身失败（数据库抖动等）不该让调度线程死掉
            log.error("[outbox] 扫描待投递事件失败，本轮跳过", e);
            return;
        }
        if (due.isEmpty()) {
            return;
        }

        int sent = 0;
        int failed = 0;
        for (OrderEvent event : due) {
            if (deliver(event)) {
                sent++;
            } else {
                failed++;
            }
        }
        log.info("[outbox] 本轮待投递 {} 条：成功 {}，失败 {}", due.size(), sent, failed);
    }

    private boolean deliver(OrderEvent event) {
        String key = String.valueOf(event.getOrderId());
        try {
            kafkaTemplate.send(TOPIC, key, event.getEventData())
                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            orderEventRepository.markSent(event.getId());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[outbox] 投递被中断: id={}", event.getId(), e);
            return false;
        } catch (Exception e) {
            int retry = (event.getRetryCount() == null ? 0 : event.getRetryCount()) + 1;
            boolean giveUp = retry >= MAX_RETRY;
            // 退避：10s、20s、30s、40s、50s，封顶 300s
            LocalDateTime next = LocalDateTime.now().plusSeconds(Math.min(300L, 10L * retry));
            orderEventRepository.markFailed(event.getId(), retry, giveUp ? null : next, giveUp);
            log.error("[outbox] 投递失败 id={} type={} retry={}/{} giveUp={}",
                event.getId(), event.getEventType(), retry, MAX_RETRY, giveUp, e);
            return false;
        }
    }
}
