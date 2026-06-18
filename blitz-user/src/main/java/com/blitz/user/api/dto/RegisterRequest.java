package com.blitz.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 32) String username,
    @NotBlank @Size(min = 6, max = 64) String password,
    @Pattern(regexp = "^1[3-9]\\d{9}$") String phone,
    @Email String email
) {}
