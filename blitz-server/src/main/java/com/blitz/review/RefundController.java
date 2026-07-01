package com.blitz.review;

import com.blitz.common.result.Result;
import com.blitz.order.domain.model.Order;
import com.blitz.order.domain.model.OrderStatus;
import com.blitz.order.infrastructure.mapper.OrderMapper;
import com.blitz.security.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
public class RefundController {

    private final OrderMapper orderMapper;

    /** User requests a refund for a PAID order (before shipping) — ownership verified in method body */
    @RequireRole("USER")
    @PostMapping("/request/{orderId}")
    public Result<Void> requestRefund(@PathVariable Long orderId, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) return Result.fail(404, "订单不存在");
        if (!order.getUserId().equals(userId)) return Result.fail(403, "无权操作此订单");

        OrderStatus current = order.getOrderStatus();
        if (current != OrderStatus.PAID) {
            return Result.fail(400, "当前订单状态(" + current.getDesc() + ")不支持退款，仅已支付未发货订单可退款");
        }

        try {
            order.refund(); // validates transition PAID → REFUNDED
            orderMapper.updateById(order);
            return Result.success();
        } catch (IllegalStateException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Number) return ((Number) principal).longValue();
        return Long.valueOf(principal.toString());
    }
}
