package com.blitz.user.domain.port;

import com.blitz.user.domain.model.Shop;

import java.util.Optional;

public interface ShopRepository {
    Optional<Shop> findById(Long id);
    Optional<Shop> findByUserId(Long userId);
    Shop save(Shop shop);
}
