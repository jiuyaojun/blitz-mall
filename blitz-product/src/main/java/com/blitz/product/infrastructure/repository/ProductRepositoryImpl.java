package com.blitz.product.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.product.domain.model.Product;
import com.blitz.product.domain.port.ProductRepository;
import com.blitz.product.infrastructure.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper productMapper;

    public ProductRepositoryImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(productMapper.selectById(id));
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.updateById(product);
        }
        return product;
    }

    @Override
    public List<Product> findAll() {
        return productMapper.selectList(null);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId, int page, int size) {
        IPage<Product> p = productMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, categoryId)
        );
        return p.getRecords();
    }

    @Override
    public List<Product> search(String keyword, int page, int size) {
        IPage<Product> p = productMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Product>()
                .like(Product::getName, keyword)
                .or()
                .like(Product::getBrand, keyword)
        );
        return p.getRecords();
    }
}
