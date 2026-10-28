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
        // 三个条件缺一不可：
        // 1) eq(version, oldVersion) —— CAS：版本变了说明别人先改过，本次放弃；
        // 2) ge(stock, qty)          —— 原子防空：库存不足时这条 UPDATE 匹配 0 行，
        //                               而不是把 stock 扣成负数（调用方的 "先查再扣" 是 TOCTOU，
        //                               两个线程可以同时通过检查）；
        // 3) version = version + 1   —— 之前漏了这句，版本号永远不变，
        //                               第二次扣减照样命中同一个 version，CAS 形同虚设。
        return skuStockMapper.update(null,
            new LambdaUpdateWrapper<SkuStock>()
                .eq(SkuStock::getSkuId, skuId)
                .eq(SkuStock::getVersion, oldVersion)
                .ge(SkuStock::getStock, qty)
                .setSql("version = version + 1")
                .apply("stock = stock - {0}", qty)
        );
    }

    @Override
    public int updateStockWithVersion(Long skuId, int newStock, int oldVersion) {
        return skuStockMapper.update(null,
            new LambdaUpdateWrapper<SkuStock>()
                .eq(SkuStock::getSkuId, skuId)
                .eq(SkuStock::getVersion, oldVersion)
                .set(SkuStock::getStock, newStock)
                .setSql("version = version + 1")
        );
    }

    @Override
    public void deleteBySkuId(Long skuId) {
        skuStockMapper.delete(
            new LambdaQueryWrapper<SkuStock>().eq(SkuStock::getSkuId, skuId)
        );
    }
}
