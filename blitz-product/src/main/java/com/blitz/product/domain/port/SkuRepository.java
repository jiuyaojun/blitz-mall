package com.blitz.product.domain.port;

import com.blitz.product.domain.model.Sku;

import java.util.List;
import java.util.Optional;

public interface SkuRepository {
    Optional<Sku> findById(Long id);
    List<Sku> findByProductId(Long productId);
    Sku save(Sku sku);
    List<Sku> saveBatch(List<Sku> skus);
    void deleteByProductId(Long productId);
}
