package com.blitz.product.domain.port;

import com.blitz.product.domain.model.SkuStock;

import java.util.Optional;

public interface SkuStockRepository {
    Optional<SkuStock> findBySkuId(Long skuId);
    SkuStock save(SkuStock skuStock);
    int deductStock(Long skuId, int qty, int oldVersion);
    void deleteBySkuId(Long skuId);
}
