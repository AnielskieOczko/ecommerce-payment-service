package com.rj.payment_service.service;

import com.rj.payment_service.dto.response.CheckoutSessionResponseDTO;
import com.rj.payment_service.producer.RabbitMQProducer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeEventWebHookImpl implements StripeWebHook {

    private final RabbitMQProducer rabbitMQProducer;

    @Override
    public void handleCheckoutSessionCompleted(Session session) {
        log.info("Processing event: checkout session completed: {}", session.getId());

        String orderId = session.getMetadata().get("orderId");
        // Create any additional details that don't fit in the main fields
        Map<String, String> additionalDetails = new HashMap<>();

        CheckoutSessionResponseDTO verificationResponse = CheckoutSessionResponseDTO.builder()
                .sessionId(session.getId())
                .orderId(orderId)
                .status("CHECKOUT_COMPLETED")
                .paymentStatus(session.getPaymentStatus())
                .checkoutUrl(session.getUrl())
                .currency(session.getCurrency())
                .amountTotal(session.getAmountTotal())
                .customerEmail(session.getCustomerEmail())
                .processedAt(LocalDateTime.now())
                .additionalDetails(additionalDetails)
                .build();

        // Send verification response to queue
        rabbitMQProducer.sendCheckoutSessionResponse(verificationResponse, session.getId());

        log.info("Checkout session completed for order: {}", orderId);
    }

    @Override
    public void handleCheckoutSessionExpired(Session session) {
        log.info("Processing event: checkout session expired: {}", session.getId());

        String orderId = session.getMetadata().get("orderId");

        // Calculate expiration time if available
        LocalDateTime expiresAt = session.getExpiresAt() != null ?
                LocalDateTime.ofEpochSecond(session.getExpiresAt(), 0, java.time.ZoneOffset.UTC) :
                null;

        // Create any additional details that don't fit in the main fields
        Map<String, String> additionalDetails = new HashMap<>();
        // Add any other details you might need

        CheckoutSessionResponseDTO verificationResponse = CheckoutSessionResponseDTO.builder()
                .sessionId(session.getId())
                .orderId(orderId)
                .status("CHECKOUT_EXPIRED")
                .paymentStatus(session.getPaymentStatus())
                .checkoutUrl(session.getUrl())
                .currency(session.getCurrency())
                .amountTotal(session.getAmountTotal())
                .customerEmail(session.getCustomerEmail())
                .processedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .additionalDetails(additionalDetails)
                .build();

        // Send verification response to queue
        rabbitMQProducer.sendCheckoutSessionResponse(verificationResponse, session.getId());

        log.warn("Checkout session expired for order: {}", orderId);
    }

    @Override
    public void handleUnknownEvent(String eventType, StripeObject object) {
        log.info("Unhandled event type: {} with object: {}", eventType, object);

        // Create a generic verification response for unknown events
        Map<String, String> additionalDetails = new HashMap<>();
        additionalDetails.put("eventType", eventType);
        additionalDetails.put("objectType", object.getClass().getSimpleName());

        CheckoutSessionResponseDTO verificationResponse = CheckoutSessionResponseDTO.builder()
                .status("UNKNOWN_EVENT")
                .processedAt(LocalDateTime.now())
                .additionalDetails(additionalDetails)
                .build();

        // Generate a correlation ID for the unknown event
        String correlationId = "unknown-" + System.currentTimeMillis();

        // Send verification response to queue
        rabbitMQProducer.sendCheckoutSessionResponse(verificationResponse, correlationId);

        log.warn("Processed unknown Stripe event type: {}", eventType);
    }
}
