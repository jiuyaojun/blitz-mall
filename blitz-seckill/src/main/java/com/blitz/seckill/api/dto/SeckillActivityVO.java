package com.blitz.seckill.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SeckillActivityVO(
    Long id, String name, LocalDateTime startTime, LocalDateTime endTime,
    Integer status, BigDecimal seckillPrice, Integer seckillStock
) {}
