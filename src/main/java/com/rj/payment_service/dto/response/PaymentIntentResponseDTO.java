package com.rj.payment_service.dto.response;

import lombok.Builder;

@Builder
public record PaymentIntentResponseDTO(
        String orderId,
        String paymentIntentId,
        String clientSecret,
        String status,
        String currency,
        Long amount
) {
}
