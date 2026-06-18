package com.blitz.seckill.domain.port;

import com.blitz.seckill.domain.model.SeckillStockFlow;
import java.util.List;

public interface SeckillStockFlowRepository {
    void save(SeckillStockFlow flow);
    List<SeckillStockFlow> findBySeckillProductId(Long seckillProductId);
    List<SeckillStockFlow> findByOrderNo(String orderNo);
}
