package com.blitz.seckill.application.service;

import com.blitz.seckill.domain.model.SeckillProduct;
import com.blitz.seckill.domain.port.SeckillProductRepository;
import com.blitz.seckill.infrastructure.redis.SeckillStockRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 秒杀库存缓存预热——活动开始前将库存加载到 Redis
 */
@Slf4j
@Service
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!minimal")
public class SeckillCacheService {

    private final SeckillProductRepository seckillProductRepository;
    private final SeckillStockRedisService stockRedisService;

    /** 预热活动所有商品的库存到 Redis */
    public void warmUpActivity(Long activityId) {
        List<SeckillProduct> products = seckillProductRepository.findByActivityId(activityId);
        for (SeckillProduct product : products) {
            stockRedisService.initStock(product.getId(), product.getSeckillStock());
            log.info("Stock warmed up: seckillProductId={}, stock={}", product.getId(), product.getSeckillStock());
        }
    }

    /** 预热单个商品 */
    public void warmUpProduct(Long seckillProductId) {
        SeckillProduct product = seckillProductRepository.findById(seckillProductId)
            .orElseThrow(() -> new IllegalArgumentException("Seckill product not found: " + seckillProductId));
        stockRedisService.initStock(seckillProductId, product.getSeckillStock());
    }
}
