package com.blitz.order.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotEmpty List<OrderItemRequest> items,
    @NotBlank String receiverName,
    @NotBlank String receiverPhone,
    @NotBlank String receiverAddress,
    String note
) {
    public record OrderItemRequest(
        @NotNull Long skuId,
        @NotBlank String skuName,
        String skuSpec,
        @NotNull BigDecimal price,
        @NotNull Integer quantity
    ) {}
}
