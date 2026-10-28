-- outbox：给 order_event 补上「投递状态」，让写库与发消息解耦。
--
-- 原来 order_event 只有业务字段，没有状态位，中继器无法分辨「这条事件发过没」，
-- 所以 event 表写了、Kafka 却是另一条直发链路，两者不同步：
--   事务回滚了事件已经在路上 / 事务提交了发送失败却没人补。
--
-- 加状态位之后：业务事务只负责把事件落库（PENDING），
-- 由 OrderEventRelay 定时扫 PENDING 投递成功再标 SENT，失败累加重试次数。

ALTER TABLE order_event
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / SENT / FAILED',
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0 COMMENT '已重试次数',
    ADD COLUMN sent_at DATETIME DEFAULT NULL COMMENT '成功投递时间',
    ADD COLUMN next_retry_at DATETIME DEFAULT NULL COMMENT '下次可重试时间',
    ADD INDEX idx_outbox_due (status, next_retry_at);

-- 历史数据按「已处理」对待：迁移瞬间不要把所有旧事件重放到 Kafka。
-- 这些事件在旧实现里是走直发链路的，重放会造成重复消费。
UPDATE order_event SET status = 'SENT', sent_at = created_at WHERE status = 'PENDING';
