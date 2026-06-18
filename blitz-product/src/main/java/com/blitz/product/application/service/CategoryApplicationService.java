package com.blitz.product.application.service;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.product.api.dto.CategoryTreeNode;
import com.blitz.product.domain.model.Category;
import com.blitz.product.domain.port.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;

    public List<CategoryTreeNode> getTree() {
        List<Category> all = categoryRepository.findAll();
        Map<Long, List<Category>> parentMap = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));

        return buildChildren(0L, parentMap);
    }

    public List<CategoryTreeNode> getChildren(Long parentId) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        return children.stream()
                .sorted(Comparator.comparing(Category::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(c -> new CategoryTreeNode(c.getId(), c.getName(), c.getParentId(),
                        c.getLevel(), c.getSortOrder(), c.getIcon(), List.of()))
                .toList();
    }

    @Transactional
    public CategoryTreeNode create(String name, Long parentId, Integer level, Integer sortOrder, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setLevel(level);
        category.setSortOrder(sortOrder);
        category.setIcon(icon);
        categoryRepository.save(category);
        return new CategoryTreeNode(category.getId(), category.getName(), category.getParentId(),
                category.getLevel(), category.getSortOrder(), category.getIcon(), List.of());
    }

    @Transactional
    public CategoryTreeNode update(Long id, String name, Long parentId, Integer level, Integer sortOrder, String icon) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "分类不存在"));
        if (name != null) category.setName(name);
        if (parentId != null) category.setParentId(parentId);
        if (level != null) category.setLevel(level);
        if (sortOrder != null) category.setSortOrder(sortOrder);
        if (icon != null) category.setIcon(icon);
        categoryRepository.save(category);
        return new CategoryTreeNode(category.getId(), category.getName(), category.getParentId(),
                category.getLevel(), category.getSortOrder(), category.getIcon(), List.of());
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND.getCode(), "分类不存在"));
        // Check if has children
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "请先删除子分类");
        }
        categoryRepository.deleteById(id);
    }

    // ---- helper methods ----

    private List<CategoryTreeNode> buildChildren(Long parentId, Map<Long, List<Category>> parentMap) {
        List<Category> children = parentMap.getOrDefault(parentId, List.of());
        return children.stream()
                .sorted(Comparator.comparing(Category::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(c -> {
                    List<CategoryTreeNode> subChildren = buildChildren(c.getId(), parentMap);
                    return new CategoryTreeNode(c.getId(), c.getName(), c.getParentId(),
                            c.getLevel(), c.getSortOrder(), c.getIcon(), subChildren);
                })
                .toList();
    }
}
