package com.blitz.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.common.result.Result;
import com.blitz.product.domain.model.Product;
import com.blitz.product.domain.model.Sku;
import com.blitz.product.infrastructure.mapper.ProductMapper;
import com.blitz.product.infrastructure.mapper.SkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/filter")
@RequiredArgsConstructor
public class FilterController {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    @GetMapping("/products")
    public Result<Map<String,Object>> filter(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(defaultValue = "") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {

        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getDeleted, 0);
        if (categoryId != null) qw.eq(Product::getCategoryId, categoryId);
        if (brand != null && !brand.isEmpty()) qw.eq(Product::getBrand, brand);

        // Batch-fetch all SKUs at once for price filtering and enrichment
        List<Product> allProducts = productMapper.selectList(qw);
        List<Long> allProductIds = allProducts.stream().map(Product::getId).toList();
        final Map<Long, List<Sku>> skuMap;
        if (!allProductIds.isEmpty()) {
            List<Sku> allSkus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().in(Sku::getProductId, allProductIds));
            skuMap = allSkus.stream().collect(Collectors.groupingBy(Sku::getProductId));
        } else {
            skuMap = Collections.emptyMap();
        }

        // Price filter: check against min SKU price using pre-fetched data
        List<Long> filteredIds = new ArrayList<>();
        for (Product p : allProducts) {
            List<Sku> skus = skuMap.getOrDefault(p.getId(), Collections.emptyList());
            if (skus.isEmpty()) continue;
            BigDecimal minPrice = skus.stream().map(Sku::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            if (priceMin != null && minPrice.compareTo(priceMin) < 0) continue;
            if (priceMax != null && minPrice.compareTo(priceMax) > 0) continue;
            filteredIds.add(p.getId());
        }
        if ((priceMin != null || priceMax != null) && filteredIds.isEmpty()) {
            return Result.success(Map.of("records", List.of(), "total", 0, "current", page, "pages", 0));
        }

        // Rebuild query for pagination
        qw = new LambdaQueryWrapper<>();
        if (!filteredIds.isEmpty()) {
            qw.in(Product::getId, filteredIds);
        }
        qw.eq(Product::getDeleted, 0);

        // Sort
        if ("sales".equals(sort)) {
            qw.orderByDesc(Product::getSaleCount);
        } else if ("newest".equals(sort)) {
            qw.orderByDesc(Product::getCreateTime);
        } else {
            qw.orderByDesc(Product::getCreateTime); // price sort done after enrichment
        }

        IPage<Product> result = productMapper.selectPage(new Page<>(page, size), qw);

        // Enrich with min price and image using pre-fetched SKU data
        List<Map<String,Object>> records = result.getRecords().stream().map(p -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("categoryId", p.getCategoryId());
            m.put("brand", p.getBrand());
            m.put("description", p.getDescription());
            m.put("image", p.getMainImage());
            m.put("sales", p.getSaleCount());
            m.put("shopId", p.getShopId());
            List<Sku> skus = skuMap.getOrDefault(p.getId(), Collections.emptyList());
            BigDecimal minPrice = skus.stream().map(Sku::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = skus.stream().map(Sku::getPrice).max(BigDecimal::compareTo).orElse(minPrice);
            m.put("minPrice", minPrice);
            m.put("originalPrice", maxPrice.compareTo(minPrice) > 0 ? maxPrice : minPrice.multiply(new BigDecimal("1.3")));
            m.put("skus", skus.stream().map(s -> {
                Map<String,Object> sm = new HashMap<>();
                sm.put("id", s.getId());
                sm.put("name", s.getName());
                sm.put("price", s.getPrice());
                sm.put("spec", s.getSpec());
                return sm;
            }).collect(Collectors.toList()));
            return m;
        }).collect(Collectors.toList());

        // Price sort after enrichment
        if ("price_asc".equals(sort)) {
            records.sort(Comparator.comparing(m -> ((BigDecimal) m.get("minPrice"))));
        } else if ("price_desc".equals(sort)) {
            records.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(m -> (BigDecimal) m.get("minPrice")).reversed());
        }

        return Result.success(Map.of(
            "records", records, "total", result.getTotal(),
            "current", (int) result.getCurrent(), "pages", (int) result.getPages()
        ));
    }

    @GetMapping("/brands")
    public Result<List<String>> brands(@RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.select(Product::getBrand).eq(Product::getDeleted, 0)
          .isNotNull(Product::getBrand).ne(Product::getBrand, "");
        if (categoryId != null) qw.eq(Product::getCategoryId, categoryId);
        qw.groupBy(Product::getBrand);
        List<Product> list = productMapper.selectList(qw);
        return Result.success(list.stream().map(Product::getBrand).distinct().sorted().toList());
    }
}
