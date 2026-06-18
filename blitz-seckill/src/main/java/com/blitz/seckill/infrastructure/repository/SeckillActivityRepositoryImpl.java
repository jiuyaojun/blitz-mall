package com.blitz.seckill.infrastructure.repository;

import com.blitz.seckill.domain.model.SeckillActivity;
import com.blitz.seckill.domain.port.SeckillActivityRepository;
import com.blitz.seckill.infrastructure.mapper.SeckillActivityMapper;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class SeckillActivityRepositoryImpl implements SeckillActivityRepository {
    private final SeckillActivityMapper mapper;
    public SeckillActivityRepositoryImpl(SeckillActivityMapper mapper) { this.mapper = mapper; }

    @Override public Optional<SeckillActivity> findById(Long id) { return Optional.ofNullable(mapper.selectById(id)); }
    @Override public SeckillActivity save(SeckillActivity a) {
        if (a.getId() == null) mapper.insert(a); else mapper.updateById(a);
        return a;
    }
}
