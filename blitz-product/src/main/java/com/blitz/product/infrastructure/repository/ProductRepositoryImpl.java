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
    public void deleteById(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public List<Product> findAll() {
        return productMapper.selectList(null);
    }

    @Override
    public List<Product> findAll(int page, int size, String sort) {
        LambdaQueryWrapper<Product> qw = buildSort(new LambdaQueryWrapper<Product>(), sort);
        IPage<Product> p = productMapper.selectPage(new Page<>(page, size), qw);
        return p.getRecords();
    }

    @Override
    public List<Product> findByShopId(Long shopId) {
        return productMapper.selectList(
            new LambdaQueryWrapper<Product>().eq(Product::getShopId, shopId)
        );
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId, int page, int size, String sort) {
        LambdaQueryWrapper<Product> qw = buildSort(
            new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, categoryId), sort);
        IPage<Product> p = productMapper.selectPage(new Page<>(page, size), qw);
        return p.getRecords();
    }

    private LambdaQueryWrapper<Product> buildSort(LambdaQueryWrapper<Product> qw, String sort) {
        if ("sales".equals(sort))      return qw.orderByDesc(Product::getSaleCount);
        if ("price_asc".equals(sort))  return qw.orderByAsc(Product::getId); // price via SKU, fallback
        if ("price_desc".equals(sort)) return qw.orderByDesc(Product::getId);
        if ("newest".equals(sort))     return qw.orderByDesc(Product::getCreateTime);
        return qw.orderByDesc(Product::getCreateTime); // default
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

    @Override
    public long countAll() {
        return productMapper.selectCount(null);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return productMapper.selectCount(
            new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, categoryId)
        );
    }

    @Override
    public long countByKeyword(String keyword) {
        return productMapper.selectCount(
            new LambdaQueryWrapper<Product>()
                .like(Product::getName, keyword)
                .or()
                .like(Product::getBrand, keyword)
        );
    }
}
