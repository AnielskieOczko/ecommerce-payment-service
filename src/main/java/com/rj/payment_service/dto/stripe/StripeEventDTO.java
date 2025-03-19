package com.rj.payment_service.dto.stripe;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record StripeEventDTO(
        String id,
        String type, // payment_intent.succeeded, payment_intent.failed, etc.
        Map<String, Object> data,
        LocalDateTime created
) {
}
