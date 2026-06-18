package com.blitz.user.api.dto;

import java.time.LocalDateTime;

public record UserVO(
    Long id,
    String username,
    String phone,
    String email,
    String avatar,
    Long roleId,
    Integer status,
    LocalDateTime createTime
) {}
