package com.blitz.order.domain.port;

import com.blitz.order.domain.model.OrderEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderEventRepository {
    void save(OrderEvent event);

    List<OrderEvent> findByOrderId(Long orderId);

    /** outbox 中继器用：取一批到期待投递的事件。 */
    List<OrderEvent> findDuePending(int limit);

    /** 投递成功：置 SENT 并记录时间。 */
    void markSent(Long id);

    /** 投递失败：累加重试次数，未到上限则排下次重试时间，超上限置 FAILED。 */
    void markFailed(Long id, int retryCount, LocalDateTime nextRetryAt, boolean giveUp);
}
