package com.rj.payment_service;

public record PaymentIntentDTO(
        String id,
        String clientSecret,
        Long amount,
        String currency) {
}
