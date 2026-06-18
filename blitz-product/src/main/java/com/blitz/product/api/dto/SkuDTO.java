package com.blitz.product.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record SkuDTO(
    String name,
    Map<String, String> spec,
    BigDecimal price,
    Integer stock
) {}
