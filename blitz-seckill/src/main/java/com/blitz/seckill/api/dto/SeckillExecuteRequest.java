package com.blitz.seckill.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeckillExecuteRequest(
    @NotNull Long seckillProductId,
    @NotBlank String path
) {}
