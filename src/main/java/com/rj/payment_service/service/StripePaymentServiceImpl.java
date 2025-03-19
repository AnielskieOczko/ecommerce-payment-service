package com.rj.payment_service.service;

import com.rj.payment_service.dto.request.PaymentIntentRequestDTO;
import com.rj.payment_service.exception.PaymentProcessingException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentServiceImpl implements PaymentService {


    @Override
    public PaymentIntent createPaymentIntent(PaymentIntentRequestDTO request) {

        validatePaymentRequest(request);
        log.info("Creating payment intent for order: {}", request.orderId());

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams
                    .builder()
                    .setAmount(request.amount())
                    .setCurrency(request.currency().toLowerCase())
                    .setReceiptEmail(request.customerEmail())
                    // Add metadata for tracking and reconciliation
                    .putMetadata("orderId", request.orderId())
                    .putAllMetadata(request.metadata())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods
                                    .builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .setDescription("Order " + request.orderId())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            log.info("Created payment intent: {} for order: {}", intent.getId(), request.orderId());
            return intent;
        } catch (StripeException e) {
            log.error("Error creating payment intent", e);
            throw new PaymentProcessingException("Failed to create payment intent", e);
        }

    }

    @Override
    public PaymentIntent confirmPayment(String paymentIntentId) throws StripeException {
        log.info("Confirming payment intent: {}", paymentIntentId);
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntent confirmedIntent = intent.confirm();
            log.info("Confirmed payment intent: {}", confirmedIntent.getId());
            return confirmedIntent;
        } catch (StripeException e) {
            log.error("Error confirming payment", e);
            throw new PaymentProcessingException("Failed to confirm payment", e);
        }
    }

    private void validatePaymentRequest(PaymentIntentRequestDTO request) {
        if (request.amount() == null || request.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (request.currency() == null || request.currency().trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (request.orderId() == null || request.orderId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (request.customerEmail() == null || !isValidEmail(request.customerEmail())) {
            throw new IllegalArgumentException("Valid customer email is required");
        }
    }

    private boolean isValidEmail(String email) {
        // Basic email validation - you might want to use a more robust solution
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
