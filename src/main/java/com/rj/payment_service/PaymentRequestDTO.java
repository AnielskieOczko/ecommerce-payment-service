package com.rj.payment_service;

public record PaymentRequestDTO(
        Long amountInCents,
        String currency) {
}
