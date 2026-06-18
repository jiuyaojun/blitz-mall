package com.blitz.product.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.product.domain.model.Sku;
import com.blitz.product.domain.port.SkuRepository;
import com.blitz.product.infrastructure.mapper.SkuMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SkuRepositoryImpl implements SkuRepository {

    private final SkuMapper skuMapper;

    public SkuRepositoryImpl(SkuMapper skuMapper) {
        this.skuMapper = skuMapper;
    }

    @Override
    public Optional<Sku> findById(Long id) {
        return Optional.ofNullable(skuMapper.selectById(id));
    }

    @Override
    public List<Sku> findByProductId(Long productId) {
        return skuMapper.selectList(
            new LambdaQueryWrapper<Sku>().eq(Sku::getProductId, productId)
        );
    }

    @Override
    public Sku save(Sku sku) {
        if (sku.getId() == null) {
            skuMapper.insert(sku);
        } else {
            skuMapper.updateById(sku);
        }
        return sku;
    }

    @Override
    public List<Sku> saveBatch(List<Sku> skus) {
        for (Sku sku : skus) {
            save(sku);
        }
        return skus;
    }

    @Override
    public void deleteByProductId(Long productId) {
        skuMapper.delete(
            new LambdaQueryWrapper<Sku>().eq(Sku::getProductId, productId)
        );
    }
}
