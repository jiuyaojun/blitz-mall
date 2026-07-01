package com.blitz.user.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.user.domain.model.Shop;
import com.blitz.user.domain.port.ShopRepository;
import com.blitz.user.infrastructure.mapper.ShopMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ShopRepositoryImpl implements ShopRepository {

    private final ShopMapper shopMapper;

    public ShopRepositoryImpl(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    @Override
    public Optional<Shop> findById(Long id) {
        return Optional.ofNullable(shopMapper.selectById(id));
    }

    @Override
    public Optional<Shop> findByUserId(Long userId) {
        return Optional.ofNullable(
            shopMapper.selectOne(
                new LambdaQueryWrapper<Shop>().eq(Shop::getUserId, userId)
            )
        );
    }

    @Override
    public Shop save(Shop shop) {
        if (shop.getId() == null) {
            shopMapper.insert(shop);
        } else {
            shopMapper.updateById(shop);
        }
        return shop;
    }
}
