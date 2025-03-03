package com.rj.payment_service.controller;

import com.rj.payment_service.PaymentIntentDTO;
import com.rj.payment_service.PaymentRequestDTO;
import com.rj.payment_service.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payment-intent")
    public ResponseEntity<PaymentIntentDTO> createPaymentIntent(@RequestBody PaymentRequestDTO request) throws StripeException {
        PaymentIntent intent = paymentService.createPaymentIntent(
                request.amountInCents(),
                request.currency()
        );

        PaymentIntentDTO response = new PaymentIntentDTO(
                intent.getId(),
                intent.getClientSecret(),
                intent.getAmount(),
                intent.getCurrency()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentIntentDTO> confirmPayment(@PathVariable String paymentIntentId) throws StripeException {
        PaymentIntent confirmedIntent = paymentService.confirmPayment(paymentIntentId);

        PaymentIntentDTO response = new PaymentIntentDTO(
                confirmedIntent.getId(),
                confirmedIntent.getClientSecret(),
                confirmedIntent.getAmount(),
                confirmedIntent.getCurrency()
        );

        return ResponseEntity.ok(response);
    }
}
