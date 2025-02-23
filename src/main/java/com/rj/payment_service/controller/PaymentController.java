package com.rj.payment_service.controller;

import com.rj.payment_service.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> payload) {
            log.info("Creating payment intent");
            log.debug("Payload: {}", payload);
        try {
            Long amount = Long.valueOf(payload.get("amount").toString());
            String currency = payload.get("currency").toString();
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(amount, currency);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("clientSecret", paymentIntent.getClientSecret()));
        } catch (StripeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
