package com.blitz.ai.api.dto;

import java.time.LocalDateTime;

public record KbDocumentVO(
    Long id, String title, String fileType, String status,
    Integer chunkCount, LocalDateTime createTime
) {}
