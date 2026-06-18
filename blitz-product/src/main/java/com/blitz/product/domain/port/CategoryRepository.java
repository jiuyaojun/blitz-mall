package com.blitz.product.domain.port;

import com.blitz.product.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(Long id);
    List<Category> findAll();
    List<Category> findByParentId(Long parentId);
    Category save(Category category);
    void deleteById(Long id);
}
