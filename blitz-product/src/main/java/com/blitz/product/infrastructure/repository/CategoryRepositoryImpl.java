package com.blitz.product.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.product.domain.model.Category;
import com.blitz.product.domain.port.CategoryRepository;
import com.blitz.product.infrastructure.mapper.CategoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryMapper categoryMapper;

    public CategoryRepositoryImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(categoryMapper.selectById(id));
    }

    @Override
    public List<Category> findAll() {
        return categoryMapper.selectList(null);
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<Category>().eq(Category::getParentId, parentId)
        );
    }

    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
        return category;
    }

    @Override
    public void deleteById(Long id) {
        categoryMapper.deleteById(id);
    }
}
