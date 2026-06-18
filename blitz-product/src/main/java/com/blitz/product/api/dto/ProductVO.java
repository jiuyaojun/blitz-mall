package com.blitz.product.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductVO(
    Long id,
    String name,
    Long categoryId,
    String brand,
    Integer status,
    List<SkuDTO> skus,
    LocalDateTime createTime
) {}
