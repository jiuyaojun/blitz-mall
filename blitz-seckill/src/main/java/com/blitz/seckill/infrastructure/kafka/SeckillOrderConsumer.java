package com.blitz.seckill.infrastructure.kafka;

import com.blitz.seckill.domain.model.SeckillOrder;
import com.blitz.seckill.domain.model.SeckillProduct;
import com.blitz.seckill.domain.model.SeckillStockFlow;
import com.blitz.seckill.domain.port.SeckillOrderRepository;
import com.blitz.seckill.domain.port.SeckillProductRepository;
import com.blitz.seckill.domain.port.SeckillStockFlowRepository;
import com.blitz.seckill.infrastructure.redis.SeckillStockRedisService;
import com.blitz.seckill.infrastructure.websocket.SeckillWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@org.springframework.context.annotation.Profile("!minimal")
@RequiredArgsConstructor
public class SeckillOrderConsumer {

    private final SeckillOrderRepository seckillOrderRepository;
    private final SeckillProductRepository seckillProductRepository;
    private final SeckillStockFlowRepository stockFlowRepository;
    private final SeckillStockRedisService stockRedisService;
    private final SeckillWebSocketHandler webSocketHandler;

    @KafkaListener(topics = "blitz-seckill-order", groupId = "blitz-seckill")
    @Transactional
    public void consume(SeckillOrderMessage message) {
        log.info("Processing seckill order: orderNo={}, userId={}", message.orderNo(), message.userId());

        try {
            // 1. 幂等性检查——MySQL UNIQUE KEY uk_user_seckill 兜底
            if (seckillOrderRepository.existsByUserAndSeckillProduct(
                    message.userId(), message.seckillProductId())) {
                log.warn("Duplicate seckill order: userId={}, spId={}",
                    message.userId(), message.seckillProductId());
                return; // 重复消费，直接 ACK
            }

            // 2. 扣减 MySQL 秒杀库存（乐观锁 CAS——最终一致性兜底）
            SeckillProduct sp = seckillProductRepository.findById(message.seckillProductId())
                .orElseThrow(() -> new RuntimeException("Seckill product not found"));

            int oldStock = sp.getSeckillStock();
            if (oldStock <= 0) {
                // Redis Lua 已预减但 MySQL 已无库存——回滚 Redis
                stockRedisService.rollbackStock(message.seckillProductId(), message.userId());
                pushResult(message, false, "库存不足");
                return;
            }

            int affected = seckillProductRepository.deductStockCas(
                message.seckillProductId(), 1, oldStock);
            if (affected == 0) {
                // 乐观锁冲突——库存已被其他消费者扣完
                stockRedisService.rollbackStock(message.seckillProductId(), message.userId());
                pushResult(message, false, "库存不足");
                return;
            }

            // 3. 创建秒杀订单记录
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setOrderNo(message.orderNo());
            seckillOrder.setUserId(message.userId());
            seckillOrder.setSeckillProductId(message.seckillProductId());
            seckillOrder.setStatus(1);
            seckillOrderRepository.save(seckillOrder);

            // 4. 记录库存流水
            SeckillStockFlow flow = SeckillStockFlow.of(
                message.seckillProductId(), -1,
                SeckillStockFlow.MYSQL_CONFIRM, message.orderNo());
            stockFlowRepository.save(flow);

            // 5. 推送成功结果
            pushResult(message, true, "秒杀成功");
            log.info("Seckill order confirmed: orderNo={}", message.orderNo());

        } catch (Exception e) {
            log.error("Seckill order processing failed: orderNo={}", message.orderNo(), e);
            // 回滚 Redis 库存
            try {
                stockRedisService.rollbackStock(message.seckillProductId(), message.userId());
            } catch (Exception rollbackEx) {
                log.error("Rollback failed for orderNo={}", message.orderNo(), rollbackEx);
            }
            // 推送失败结果
            pushResult(message, false, "系统繁忙，请重试");
        }
    }

    private void pushResult(SeckillOrderMessage msg, boolean success, String message) {
        try {
            webSocketHandler.pushSeckillResult(msg.userId(), Map.of(
                "orderNo", msg.orderNo(),
                "success", success,
                "message", message,
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Failed to push WebSocket result: userId={}", msg.userId(), e);
        }
    }
}
