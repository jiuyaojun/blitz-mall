package com.blitz.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank String message,
    Long sessionId  // null = create new session
) {}
