package com.blitz.order.api.controller;

import com.blitz.common.result.PageResult;
import com.blitz.common.result.Result;
import com.blitz.order.api.dto.CreateOrderRequest;
import com.blitz.order.api.dto.OrderVO;
import com.blitz.order.application.service.OrderApplicationService;
import com.blitz.order.application.service.OrderQueryService;
import com.blitz.security.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final OrderQueryService orderQueryService;
    private final HttpServletRequest request;

    /** 创建订单 */
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody CreateOrderRequest req) {
        Long userId = getCurrentUserId();
        var items = req.items().stream()
            .map(i -> new OrderApplicationService.CreateOrderItem(
                i.skuId(), i.skuName(), i.skuSpec(), i.price(), i.quantity()))
            .toList();
        var order = orderApplicationService.createOrder(userId, items,
            req.receiverName(), req.receiverPhone(), req.receiverAddress(), req.note());
        return Result.success(orderQueryService.getById(order.getId()));
    }

    /** 查询订单详情（所有者/管理员/商家可查看） */
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        OrderVO order = orderQueryService.getById(id);
        if (order == null) return Result.fail(404, "订单不存在");
        if (!canAccessOrder(order)) return Result.fail(403, "无权查看此订单");
        return Result.success(order);
    }

    /** 查询订单详情（按订单号） */
    @GetMapping("/no/{orderNo}")
    public Result<OrderVO> detailByNo(@PathVariable String orderNo) {
        OrderVO order = orderQueryService.getByOrderNo(orderNo);
        if (order == null) return Result.fail(404, "订单不存在");
        if (!canAccessOrder(order)) return Result.fail(403, "无权查看此订单");
        return Result.success(order);
    }

    /** Check if current user can access the given order */
    private boolean canAccessOrder(OrderVO order) {
        Long userId = getCurrentUserId();
        if (order.userId().equals(userId)) return true;
        String role = (String) request.getAttribute("role");
        return "ADMIN".equals(role) || "MERCHANT".equals(role);
    }

    /** 当前用户订单列表 */
    @GetMapping("/list")
    public Result<List<OrderVO>> list() {
        Long userId = getCurrentUserId();
        return Result.success(orderQueryService.listByUser(userId));
    }

    /** 当前用户订单分页（支持状态筛选） */
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status) {
        Long userId = getCurrentUserId();
        return Result.success(orderQueryService.pageByUser(userId, page, size, status));
    }

    /** 支付订单 */
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id,
                            @RequestParam(defaultValue = "1") Integer paymentMethod) {
        Long userId = getCurrentUserId();
        orderApplicationService.payOrder(id, userId, paymentMethod);
        return Result.success();
    }

    /** 取消订单 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam(defaultValue = "手动取消") String reason) {
        Long userId = getCurrentUserId();
        orderApplicationService.cancelOrder(id, userId, reason);
        return Result.success();
    }

    /** 确认收货 */
    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderApplicationService.receiveOrder(id, userId);
        return Result.success();
    }

    /** 发货（管理员/商家） */
    @PostMapping("/{id}/ship")
    @RequireRole({"ADMIN", "MERCHANT"})
    public Result<Void> ship(@PathVariable Long id) {
        orderApplicationService.shipOrder(id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((Number) principal).longValue();
    }
}
