package com.blitz.seckill.domain.port;

import com.blitz.seckill.domain.model.SeckillProduct;
import java.util.List;
import java.util.Optional;

public interface SeckillProductRepository {
    Optional<SeckillProduct> findById(Long id);
    List<SeckillProduct> findByActivityId(Long activityId);
    SeckillProduct save(SeckillProduct product);
    /** CAS 扣减：只有 stock==expectedOldStock 时才更新 */
    int deductStockCas(Long id, int qty, int expectedOldStock);
}
