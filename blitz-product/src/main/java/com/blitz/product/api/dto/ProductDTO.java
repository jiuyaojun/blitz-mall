package com.blitz.product.api.dto;

import java.util.List;

public record ProductDTO(
    String name,
    Long categoryId,
    String brand,
    String description,
    String mainImage,
    List<String> images,
    List<SkuDTO> skus
) {}
