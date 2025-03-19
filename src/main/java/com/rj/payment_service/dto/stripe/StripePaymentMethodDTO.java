package com.rj.payment_service.dto.stripe;

import lombok.Builder;

import java.util.Map;

@Builder
public record StripePaymentMethodDTO(
        String id,
        String type, // card, bank_transfer, etc.
        Map<String, Object> card, // Details about the card
        Map<String, Object> billingDetails
) {
}
