package com.blitz.seckill.domain.port;

import com.blitz.seckill.domain.model.SeckillOrder;
import java.util.Optional;

public interface SeckillOrderRepository {
    Optional<SeckillOrder> findById(Long id);
    Optional<SeckillOrder> findByUserAndSeckillProduct(Long userId, Long seckillProductId);
    boolean existsByUserAndSeckillProduct(Long userId, Long seckillProductId);
    SeckillOrder save(SeckillOrder order);
}
