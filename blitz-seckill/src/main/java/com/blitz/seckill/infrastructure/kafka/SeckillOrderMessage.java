package com.blitz.seckill.infrastructure.kafka;

import java.io.Serializable;

public record SeckillOrderMessage(
    Long userId,
    Long seckillProductId,
    String orderNo,
    long timestamp
) implements Serializable {}
