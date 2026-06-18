package com.blitz.ai.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatSessionVO(
    Long id, String title, LocalDateTime createTime,
    List<ChatMessageVO> messages
) {}

record ChatMessageVO(Long id, String role, String content, LocalDateTime createdAt) {}
