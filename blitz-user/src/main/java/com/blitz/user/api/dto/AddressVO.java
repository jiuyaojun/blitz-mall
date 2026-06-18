package com.blitz.user.api.dto;

public record AddressVO(
    Long id,
    Long userId,
    String receiverName,
    String receiverPhone,
    String province,
    String city,
    String district,
    String detail,
    Integer isDefault
) {}
