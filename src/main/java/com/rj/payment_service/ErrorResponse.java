package com.rj.payment_service;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String code,
        String message
) {
}
