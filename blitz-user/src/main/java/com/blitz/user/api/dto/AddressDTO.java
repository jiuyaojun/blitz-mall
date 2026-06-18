package com.blitz.user.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDTO(
    @NotBlank String receiverName,
    @NotBlank String receiverPhone,
    String province,
    String city,
    String district,
    String detail,
    Integer isDefault
) {}
