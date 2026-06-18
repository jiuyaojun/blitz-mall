package com.blitz.seckill.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.seckill.domain.model.SeckillStockFlow;
import com.blitz.seckill.domain.port.SeckillStockFlowRepository;
import com.blitz.seckill.infrastructure.mapper.SeckillStockFlowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SeckillStockFlowRepositoryImpl implements SeckillStockFlowRepository {
    private final SeckillStockFlowMapper mapper;
    public SeckillStockFlowRepositoryImpl(SeckillStockFlowMapper mapper) { this.mapper = mapper; }

    @Override public void save(SeckillStockFlow flow) { mapper.insert(flow); }

    @Override public List<SeckillStockFlow> findBySeckillProductId(Long spId) {
        return mapper.selectList(new LambdaQueryWrapper<SeckillStockFlow>()
            .eq(SeckillStockFlow::getSeckillProductId, spId));
    }

    @Override public List<SeckillStockFlow> findByOrderNo(String orderNo) {
        return mapper.selectList(new LambdaQueryWrapper<SeckillStockFlow>()
            .eq(SeckillStockFlow::getOrderNo, orderNo));
    }
}
