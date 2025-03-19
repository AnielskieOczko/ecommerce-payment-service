package com.rj.payment_service.dto.stripe;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record StripePaymentIntentDTO(
        String id,
        Long amount,
        String currency,
        String clientSecret,
        String status,
        Map<String, String> metadata,
        String customerId,
        String paymentMethodId,
        LocalDateTime createdAt
) {
}
