package com.blitz.order.api.controller;

import com.blitz.common.result.PageResult;
import com.blitz.common.result.Result;
import com.blitz.order.api.dto.CreateOrderRequest;
import com.blitz.order.api.dto.OrderVO;
import com.blitz.order.application.service.OrderApplicationService;
import com.blitz.order.application.service.OrderQueryService;
import com.blitz.security.annotation.RequireRole;
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

    /** 查询订单详情（仅订单所有者可查看） */
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        OrderVO order = orderQueryService.getById(id);
        if (order == null) return Result.fail(404, "订单不存在");
        if (!order.userId().equals(userId)) return Result.fail(403, "无权查看此订单");
        return Result.success(order);
    }

    /** 查询订单详情（按订单号，仅订单所有者可查看） */
    @GetMapping("/no/{orderNo}")
    public Result<OrderVO> detailByNo(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();
        OrderVO order = orderQueryService.getByOrderNo(orderNo);
        if (order == null) return Result.fail(404, "订单不存在");
        if (!order.userId().equals(userId)) return Result.fail(403, "无权查看此订单");
        return Result.success(order);
    }

    /** 当前用户订单列表 */
    @GetMapping("/list")
    public Result<List<OrderVO>> list() {
        Long userId = getCurrentUserId();
        return Result.success(orderQueryService.listByUser(userId));
    }

    /** 当前用户订单分页 */
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        return Result.success(orderQueryService.pageByUser(userId, page, size));
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

    /** 发货（管理员） */
    @PostMapping("/{id}/ship")
    @RequireRole("ADMIN")
    public Result<Void> ship(@PathVariable Long id) {
        orderApplicationService.shipOrder(id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((Number) principal).longValue();
    }
}
