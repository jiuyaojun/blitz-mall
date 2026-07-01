package com.blitz.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.common.result.Result;
import com.blitz.product.domain.model.Product;
import com.blitz.product.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ProductMapper productMapper;

    @GetMapping("/{shopId}/products")
    public Result<IPage<Product>> products(@PathVariable Long shopId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getShopId, shopId).eq(Product::getDeleted, 0);
        qw.orderByDesc(Product::getCreateTime);
        return Result.success(productMapper.selectPage(new Page<>(page, size), qw));
    }

    @GetMapping("/{shopId}/info")
    public Result<Map<String,Object>> info(@PathVariable Long shopId) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getShopId, shopId).eq(Product::getDeleted, 0);
        Long count = productMapper.selectCount(qw);
        return Result.success(Map.of("shopId", shopId, "productCount", count));
    }
}
