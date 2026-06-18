package com.blitz.seckill.domain.port;

import com.blitz.seckill.domain.model.SeckillActivity;
import java.util.Optional;

public interface SeckillActivityRepository {
    Optional<SeckillActivity> findById(Long id);
    SeckillActivity save(SeckillActivity activity);
}
