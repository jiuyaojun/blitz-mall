package com.blitz.product.api.controller;

import com.blitz.common.result.Result;
import com.blitz.product.api.dto.CategoryTreeNode;
import com.blitz.product.application.service.CategoryApplicationService;
import com.blitz.security.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    @GetMapping("/tree")
    public Result<List<CategoryTreeNode>> getTree() {
        return Result.success(categoryApplicationService.getTree());
    }

    @GetMapping("/{id}/children")
    public Result<List<CategoryTreeNode>> getChildren(@PathVariable Long id) {
        return Result.success(categoryApplicationService.getChildren(id));
    }

    @PostMapping
    @RequireRole("ADMIN")
    public Result<CategoryTreeNode> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long parentId = body.get("parentId") != null ? ((Number) body.get("parentId")).longValue() : 0L;
        Integer level = body.get("level") != null ? ((Number) body.get("level")).intValue() : 1;
        Integer sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0;
        String icon = (String) body.get("icon");
        return Result.success(categoryApplicationService.create(name, parentId, level, sortOrder, icon));
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<CategoryTreeNode> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long parentId = body.get("parentId") != null ? ((Number) body.get("parentId")).longValue() : null;
        Integer level = body.get("level") != null ? ((Number) body.get("level")).intValue() : null;
        Integer sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null;
        String icon = (String) body.get("icon");
        return Result.success(categoryApplicationService.update(id, name, parentId, level, sortOrder, icon));
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        categoryApplicationService.delete(id);
        return Result.success();
    }
}
