package com.blitz.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.common.result.Result;
import com.blitz.order.domain.model.Order;
import com.blitz.order.infrastructure.mapper.OrderMapper;
import com.blitz.product.domain.model.Product;
import com.blitz.product.infrastructure.mapper.ProductMapper;
import com.blitz.security.annotation.RequireRole;
import com.blitz.user.domain.model.User;
import com.blitz.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminController {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        long products = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getDeleted, 0));
        long users = userMapper.selectCount(new LambdaQueryWrapper<>());
        long orders = orderMapper.selectCount(new LambdaQueryWrapper<>());
        // Query actual GMV from paid/shipped/received orders
        Long gmv = orderMapper.selectObjs(
            new LambdaQueryWrapper<Order>()
                .select(Order::getTotalAmount)
                .in(Order::getStatus, "PAID", "SHIPPED", "RECEIVED"))
            .stream().mapToLong(o -> o != null ? ((Number) o).longValue() : 0).sum();
        return Result.success(Map.of(
            "products", products, "users", users, "orders", orders,
            "gmv", gmv, "shops", 2
        ));
    }

    @GetMapping("/users")
    public Result<IPage<User>> users(@RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="10") int size) {
        return Result.success(userMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<>()));
    }

    @GetMapping("/orders")
    public Result<IPage<Order>> orders(@RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="10") int size) {
        return Result.success(orderMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime)));
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.fail(404, "用户不存在");
        Object roleIdObj = body.get("roleId");
        if (roleIdObj == null) return Result.fail(400, "缺少 roleId 参数");
        long roleId = Long.valueOf(roleIdObj.toString());
        if (roleId < 1 || roleId > 3) return Result.fail(400, "无效的角色ID（有效值：1=USER, 2=MERCHANT, 3=ADMIN）");
        u.setRoleId(roleId);
        userMapper.updateById(u);
        return Result.success();
    }

    @PostMapping("/users/{id}/ban")
    public Result<Void> ban(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.fail(404, "用户不存在");
        // Only ban active users (status=1), do not toggle
        if (u.getStatus() != null && u.getStatus() == 1) {
            u.setStatus(0);
            userMapper.updateById(u);
        }
        return Result.success();
    }

    @PostMapping("/users/{id}/unban")
    public Result<Void> unban(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return Result.fail(404, "用户不存在");
        if (u.getStatus() != null && u.getStatus() == 0) {
            u.setStatus(1);
            userMapper.updateById(u);
        }
        return Result.success();
    }
}
