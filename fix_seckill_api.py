#!/usr/bin/env python3
"""Fix SeckillProductItem to include product name/image, and fix SeckillController to populate them"""

# Fix 1: Update DTO
dto_path = '/home/ubuntu/blitz/blitz-seckill/src/main/java/com/blitz/seckill/api/dto/SeckillProductItem.java'
with open(dto_path, 'w') as f:
    f.write('''package com.blitz.seckill.api.dto;

import java.math.BigDecimal;

public record SeckillProductItem(
    Long id,
    Long productId,
    BigDecimal seckillPrice,
    Integer seckillStock,
    String name,
    String image,
    BigDecimal originalPrice
) {}
''')
print("DTO updated")

# Fix 2: Update SeckillController to populate new fields
ctrl_path = '/home/ubuntu/blitz/blitz-seckill/src/main/java/com/blitz/seckill/api/controller/SeckillController.java'
with open(ctrl_path, 'r') as f:
    c = f.read()

# Find the listProducts method and fix it
old_list = """List<com.blitz.seckill.api.dto.SeckillProductItem> items = products.stream().map(p ->
            new com.blitz.seckill.api.dto.SeckillProductItem(
                p.getId(), p.getProductId(), p.getSeckillPrice(), p.getSeckillStock()))
            .toList();"""

new_list = """List<com.blitz.seckill.api.dto.SeckillProductItem> items = products.stream().map(p -> {
            com.blitz.product.domain.model.Product prod = null;
            try { prod = productRepository.findById(p.getProductId()).orElse(null); } catch(Exception ignored) {}
            String prodName = prod != null ? prod.getName() : "商品#" + p.getProductId();
            String prodImage = prod != null ? prod.getMainImage() : null;
            java.math.BigDecimal origPrice = prod != null && prod.getSaleCount() != null ?
                p.getSeckillPrice().multiply(java.math.BigDecimal.valueOf(1.3)) : p.getSeckillPrice();
            return new com.blitz.seckill.api.dto.SeckillProductItem(
                p.getId(), p.getProductId(), p.getSeckillPrice(), p.getSeckillStock(),
                prodName, prodImage, origPrice);
        }).toList();"""

c = c.replace(old_list, new_list)

# Also need to inject ProductRepository
old_field = "private final SeckillActivityRepository seckillActivityRepository;"
new_field = """private final SeckillActivityRepository seckillActivityRepository;
    private final com.blitz.product.domain.port.ProductRepository productRepository;"""
c = c.replace(old_field, new_field)

with open(ctrl_path, 'w') as f:
    f.write(c)
print("Controller updated")
