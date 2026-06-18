package com.blitz.seckill.application.service;

import cn.hutool.core.util.IdUtil;
import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.seckill.domain.model.SeckillActivity;
import com.blitz.seckill.domain.model.SeckillProduct;
import com.blitz.seckill.domain.port.SeckillActivityRepository;
import com.blitz.seckill.domain.port.SeckillProductRepository;
import com.blitz.seckill.infrastructure.kafka.SeckillOrderMessage;
import com.blitz.seckill.infrastructure.kafka.SeckillOrderProducer;
import com.blitz.seckill.infrastructure.redis.SeckillStockRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 秒杀核心应用服务——编排 8 步秒杀链路
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillApplicationService {

    private final SeckillActivityRepository activityRepository;
    private final SeckillProductRepository seckillProductRepository;
    private final SeckillStockRedisService stockRedisService;
    private final SeckillPathService seckillPathService;
    private final SeckillOrderProducer orderProducer;
    private final RedissonClient redissonClient;

    /** 获取秒杀动态路径（第②步：HMAC签名） */
    public String getSeckillPath(Long seckillProductId, Long userId) {
        // 校验活动是否进行中
        SeckillProduct sp = seckillProductRepository.findById(seckillProductId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        SeckillActivity activity = activityRepository.findById(sp.getActivityId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!activity.isInProgress()) {
            throw new BusinessException(ErrorCode.SECKILL_OVER);
        }
        return seckillPathService.generatePath(seckillProductId, userId);
    }

    /**
     * 执行秒杀（第③-⑥步主链路）
     *
     * @return Map with "orderNo" and "status" ("QUEUING" = 排队中)
     */
    public Map<String, String> executeSeckill(Long seckillProductId, Long userId, String path) {
        // ② HMAC 路径校验
        Long validated = seckillPathService.validatePath(path, seckillProductId, userId);
        if (validated == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "秒杀路径无效或已过期");
        }

        // ③ Redisson 用户限流（1 QPS per user）
        String rateLimiterKey = "seckill:ratelimit:user:" + userId;
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);
        rateLimiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.SECONDS);
        if (!rateLimiter.tryAcquire()) {
            throw new BusinessException(429, "操作太频繁，请稍后再试");
        }

        // ④ 快速失败：检查 Redis 售罄标记
        if (stockRedisService.isOver(seckillProductId)) {
            throw new BusinessException(ErrorCode.SECKILL_OVER);
        }

        // ⑤ Redis Lua 原子预减库存
        long result = stockRedisService.executeDeduct(seckillProductId, userId);

        if (result == -1) {
            throw new BusinessException(ErrorCode.SECKILL_OVER);
        }
        if (result == -2) {
            throw new BusinessException(ErrorCode.SECKILL_REPEAT);
        }
        if (result == -3) {
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }

        // ⑥ 生成订单号 + 发送 Kafka 异步下单
        String orderNo = IdUtil.getSnowflake(2, 1).nextIdStr();
        SeckillOrderMessage message = new SeckillOrderMessage(
            userId, seckillProductId, orderNo, System.currentTimeMillis());

        try {
            orderProducer.send(message);
        } catch (Exception e) {
            // Kafka 发送失败——回滚 Redis 库存
            stockRedisService.rollbackStock(seckillProductId, userId);
            throw new BusinessException(500, "系统繁忙，请重试");
        }

        log.info("Seckill request accepted: orderNo={}, userId={}, spId={}", orderNo, userId, seckillProductId);
        return Map.of("orderNo", orderNo, "status", "QUEUING");
    }

    /** 查询秒杀结果（轮询降级方案） */
    public String queryResult(Long seckillProductId, Long userId) {
        return stockRedisService.isOver(seckillProductId)
            ? "SOLD_OUT" : "UNKNOWN";
    }
}
