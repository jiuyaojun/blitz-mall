package com.blitz.product.domain.port;

import com.blitz.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
    List<Product> findAll();
    List<Product> findAll(int page, int size, String sort);
    List<Product> findByShopId(Long shopId);
    List<Product> findByCategoryId(Long categoryId, int page, int size, String sort);
    List<Product> search(String keyword, int page, int size);
    long countAll();
    long countByCategoryId(Long categoryId);
    long countByKeyword(String keyword);
}
