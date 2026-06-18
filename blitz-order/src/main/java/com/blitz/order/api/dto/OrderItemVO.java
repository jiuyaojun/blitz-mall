package com.blitz.order.api.dto;

import java.math.BigDecimal;

public record OrderItemVO(
    Long id,
    Long skuId,
    String skuName,
    String skuSpec,
    BigDecimal price,
    Integer quantity
) {}
