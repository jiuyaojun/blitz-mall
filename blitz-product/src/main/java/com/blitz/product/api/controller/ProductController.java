package com.blitz.product.api.controller;

import com.blitz.common.result.Result;
import com.blitz.product.api.dto.PageResult;
import com.blitz.product.api.dto.ProductDTO;
import com.blitz.product.api.dto.ProductVO;
import com.blitz.product.application.service.ProductApplicationService;
import com.blitz.security.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductApplicationService productApplicationService;

    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        return Result.success(productApplicationService.getById(id));
    }

    @GetMapping("/list")
    public Result<PageResult<ProductVO>> listByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(productApplicationService.listByCategory(categoryId, page, size));
    }

    @GetMapping("/search")
    public Result<PageResult<ProductVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(productApplicationService.search(keyword, page, size));
    }

    @PostMapping
    @RequireRole("ADMIN")
    public Result<ProductVO> create(@RequestBody ProductDTO dto) {
        return Result.success(productApplicationService.create(dto));
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<ProductVO> update(@PathVariable Long id, @RequestBody ProductDTO dto) {
        return Result.success(productApplicationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        productApplicationService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    @RequireRole("ADMIN")
    public Result<Void> publish(@PathVariable Long id) {
        productApplicationService.publish(id);
        return Result.success();
    }

    @PutMapping("/{id}/withdraw")
    @RequireRole("ADMIN")
    public Result<Void> withdraw(@PathVariable Long id) {
        productApplicationService.withdraw(id);
        return Result.success();
    }
}
