package com.blitz.product.api.dto;

import java.util.List;

public record CategoryTreeNode(
    Long id,
    String name,
    Long parentId,
    Integer level,
    Integer sortOrder,
    String icon,
    List<CategoryTreeNode> children
) {}
