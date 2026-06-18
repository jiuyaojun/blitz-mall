package com.blitz.seckill.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blitz.seckill.domain.model.SeckillProduct;
import com.blitz.seckill.domain.port.SeckillProductRepository;
import com.blitz.seckill.infrastructure.mapper.SeckillProductMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class SeckillProductRepositoryImpl implements SeckillProductRepository {
    private final SeckillProductMapper mapper;
    public SeckillProductRepositoryImpl(SeckillProductMapper mapper) { this.mapper = mapper; }

    @Override public Optional<SeckillProduct> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }

    @Override public List<SeckillProduct> findByActivityId(Long activityId) {
        return mapper.selectList(new LambdaQueryWrapper<SeckillProduct>().eq(SeckillProduct::getActivityId, activityId));
    }

    @Override public SeckillProduct save(SeckillProduct p) {
        if (p.getId() == null) mapper.insert(p); else mapper.updateById(p);
        return p;
    }

    /** CAS: update seckill_stock = seckill_stock - qty WHERE id=? AND seckill_stock=? */
    @Override public int deductStockCas(Long id, int qty, int expectedOldStock) {
        return mapper.update(null, new LambdaUpdateWrapper<SeckillProduct>()
            .eq(SeckillProduct::getId, id)
            .eq(SeckillProduct::getSeckillStock, expectedOldStock)
            .setSql("seckill_stock = seckill_stock - " + qty));
    }
}
