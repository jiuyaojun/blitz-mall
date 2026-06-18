package com.blitz.seckill.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.seckill.domain.model.SeckillOrder;
import com.blitz.seckill.domain.port.SeckillOrderRepository;
import com.blitz.seckill.infrastructure.mapper.SeckillOrderMapper;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class SeckillOrderRepositoryImpl implements SeckillOrderRepository {
    private final SeckillOrderMapper mapper;
    public SeckillOrderRepositoryImpl(SeckillOrderMapper mapper) { this.mapper = mapper; }

    @Override public Optional<SeckillOrder> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }

    @Override public Optional<SeckillOrder> findByUserAndSeckillProduct(Long userId, Long spId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<SeckillOrder>()
            .eq(SeckillOrder::getUserId, userId)
            .eq(SeckillOrder::getSeckillProductId, spId)));
    }

    @Override public boolean existsByUserAndSeckillProduct(Long userId, Long spId) {
        return mapper.exists(new LambdaQueryWrapper<SeckillOrder>()
            .eq(SeckillOrder::getUserId, userId)
            .eq(SeckillOrder::getSeckillProductId, spId));
    }

    @Override public SeckillOrder save(SeckillOrder o) {
        if (o.getId() == null) mapper.insert(o); else mapper.updateById(o);
        return o;
    }
}
