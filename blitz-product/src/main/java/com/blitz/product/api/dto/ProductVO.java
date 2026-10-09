package com.blitz.product.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductVO(
    Long id,
    String name,
    Long categoryId,
    String brand,
    Integer status,
    String mainImage,
    String description,
    BigDecimal minPrice,
    Integer saleCount,
    List<SkuDTO> skus,
    LocalDateTime createTime
) {}
