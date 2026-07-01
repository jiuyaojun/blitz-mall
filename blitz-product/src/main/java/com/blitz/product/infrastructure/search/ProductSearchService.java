package com.blitz.product.infrastructure.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.product.domain.model.Product;
import com.blitz.product.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Product full-text search service.
 * Phase 2: MySQL LIKE fallback.
 * Phase 6: Integrate Elasticsearch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductMapper productMapper;

    public List<Product> search(String keyword, int page, int size) {
        // Future enhancement: try ES client first, fallback to MySQL on failure
        IPage<Product> p = productMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Product>()
                .like(Product::getName, keyword)
                .or()
                .like(Product::getBrand, keyword)
        );
        return p.getRecords();
    }

    public long count(String keyword) {
        return productMapper.selectCount(
            new LambdaQueryWrapper<Product>()
                .like(Product::getName, keyword)
                .or()
                .like(Product::getBrand, keyword)
        );
    }
}
