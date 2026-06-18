package com.blitz.product.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blitz.product.domain.model.SkuStock;
import com.blitz.product.domain.port.SkuStockRepository;
import com.blitz.product.infrastructure.mapper.SkuStockMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SkuStockRepositoryImpl implements SkuStockRepository {

    private final SkuStockMapper skuStockMapper;

    public SkuStockRepositoryImpl(SkuStockMapper skuStockMapper) {
        this.skuStockMapper = skuStockMapper;
    }

    @Override
    public Optional<SkuStock> findBySkuId(Long skuId) {
        return Optional.ofNullable(
            skuStockMapper.selectOne(
                new LambdaQueryWrapper<SkuStock>().eq(SkuStock::getSkuId, skuId)
            )
        );
    }

    @Override
    public SkuStock save(SkuStock skuStock) {
        if (skuStock.getId() == null) {
            skuStockMapper.insert(skuStock);
        } else {
            skuStockMapper.updateById(skuStock);
        }
        return skuStock;
    }

    @Override
    public int deductStock(Long skuId, int qty, int oldVersion) {
        return skuStockMapper.update(null,
            new LambdaUpdateWrapper<SkuStock>()
                .eq(SkuStock::getSkuId, skuId)
                .eq(SkuStock::getVersion, oldVersion)
                .setSql("stock = stock - " + qty)
        );
    }

    @Override
    public void deleteBySkuId(Long skuId) {
        skuStockMapper.delete(
            new LambdaQueryWrapper<SkuStock>().eq(SkuStock::getSkuId, skuId)
        );
    }
}
