package com.rj.payment_service.dto.request;

import lombok.Builder;

@Builder
public record PaymentVerificationRequestDTO(
        String orderId,
        String paymentIntentId
) {
}
