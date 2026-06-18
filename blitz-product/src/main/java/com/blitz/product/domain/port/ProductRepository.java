package com.blitz.product.domain.port;

import com.blitz.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    Product save(Product product);
    List<Product> findAll();
    List<Product> findByCategoryId(Long categoryId, int page, int size);
    List<Product> search(String keyword, int page, int size);
}
