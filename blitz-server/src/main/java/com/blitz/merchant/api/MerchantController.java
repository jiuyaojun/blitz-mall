package com.blitz.merchant.api;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.common.result.Result;
import com.blitz.merchant.application.MerchantApplicationService;
import com.blitz.product.api.dto.ProductVO;
import com.blitz.order.domain.model.Order;
import com.blitz.order.infrastructure.mapper.OrderMapper;
import com.blitz.security.annotation.RequireRole;
import com.blitz.user.domain.model.Shop;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantApplicationService merchantApplicationService;
    private final OrderMapper orderMapper;

    @GetMapping("/dashboard")
    @RequireRole("MERCHANT")
    public Result<Map<String, Object>> dashboard() {
        Long userId = getCurrentUserId();
        return Result.success(merchantApplicationService.getDashboard(userId));
    }

    @GetMapping("/shop")
    @RequireRole("MERCHANT")
    public Result<Shop> getShop() {
        Long userId = getCurrentUserId();
        return Result.success(merchantApplicationService.getShop(userId));
    }

    @PostMapping("/shop")
    @RequireRole("MERCHANT")
    public Result<Shop> createShop(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        Shop shop = merchantApplicationService.createShop(
            userId,
            body.getOrDefault("name", "我的店铺"),
            body.getOrDefault("description", "")
        );
        return Result.success(shop);
    }

    @PutMapping("/shop")
    @RequireRole("MERCHANT")
    public Result<Shop> updateShop(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        Shop shop = merchantApplicationService.updateShop(
            userId,
            body.get("name"),
            body.get("description"),
            body.get("logo")
        );
        return Result.success(shop);
    }

    @GetMapping("/products")
    @RequireRole("MERCHANT")
    public Result<List<ProductVO>> listProducts() {
        Long userId = getCurrentUserId();
        return Result.success(merchantApplicationService.listProducts(userId));
    }

    @PostMapping("/products")
    @RequireRole("MERCHANT")
    public Result<ProductVO> createProduct(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        String name = (String) body.get("name");
        Long categoryId = body.get("categoryId") != null ? ((Number) body.get("categoryId")).longValue() : 1L;
        String brand = (String) body.getOrDefault("brand", "");
        String description = (String) body.getOrDefault("description", "");
        String mainImage = (String) body.getOrDefault("mainImage", "");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skuList = (List<Map<String, Object>>) body.getOrDefault("skus", List.of());
        List<MerchantApplicationService.SkuInput> skus = skuList.stream().map(s -> {
            @SuppressWarnings("unchecked")
            Map<String, String> spec = (Map<String, String>) s.getOrDefault("spec", Map.of());
            return new MerchantApplicationService.SkuInput(
                (String) s.get("name"),
                spec,
                new java.math.BigDecimal(String.valueOf(s.getOrDefault("price", 0))),
                s.get("stock") != null ? ((Number) s.get("stock")).intValue() : 0
            );
        }).toList();

        return Result.success(merchantApplicationService.createProduct(
            userId, name, categoryId, brand, description, mainImage, skus));
    }

    @PutMapping("/products/{id}")
    @RequireRole("MERCHANT")
    public Result<ProductVO> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        return Result.success(merchantApplicationService.updateProduct(
            userId, id,
            (String) body.get("name"),
            body.get("categoryId") != null ? ((Number) body.get("categoryId")).longValue() : null,
            (String) body.get("brand"),
            (String) body.get("description"),
            (String) body.get("mainImage")
        ));
    }

    @DeleteMapping("/products/{id}")
    @RequireRole("MERCHANT")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        merchantApplicationService.deleteProduct(userId, id);
        return Result.success();
    }

    @PostMapping("/products/{id}/toggle")
    @RequireRole("MERCHANT")
    public Result<Void> toggleProduct(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        merchantApplicationService.toggleProduct(userId, id);
        return Result.success();
    }

    @GetMapping("/orders")
    @RequireRole("MERCHANT")
    public Result<List<Order>> listOrders() {
        var allOrders = orderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime)
        );
        return Result.success(allOrders.stream().limit(50).toList());
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ((Number) auth.getPrincipal()).longValue();
    }
}
