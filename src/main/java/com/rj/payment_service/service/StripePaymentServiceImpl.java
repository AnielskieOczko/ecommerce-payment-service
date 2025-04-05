package com.rj.payment_service.service;

import com.rj.payment_service.dto.request.CheckoutSessionRequestDTO;
import com.rj.payment_service.exception.PaymentProcessingException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentServiceImpl implements PaymentService {


    @Override
    public Session createCheckoutSession(CheckoutSessionRequestDTO request) throws StripeException {
        log.info("Creating checkout session for order: {}", request.orderId());

        try {
            // Validate request
            if (request.orderId() == null || request.orderId().trim().isEmpty()) {
                throw new IllegalArgumentException("Order ID is required");
            }
            if (request.customerEmail() == null || !isValidEmail(request.customerEmail())) {
                throw new IllegalArgumentException("Valid customer email is required");
            }
            if (request.successUrl() == null || request.successUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Success URL is required");
            }
            if (request.cancelUrl() == null || request.cancelUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Cancel URL is required");
            }
            if (request.lineItems() == null || request.lineItems().isEmpty()) {
                throw new IllegalArgumentException("At least one line item is required");
            }

            // Create line items for the session
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

            for (CheckoutSessionRequestDTO.CheckoutLineItemDTO item : request.lineItems()) {
                lineItems.add(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(Long.valueOf(item.quantity()))
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(item.currency().toLowerCase())
                                                .setUnitAmount(item.unitAmount())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(item.name())
                                                                .setDescription(item.description())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );
            }

            // Build the session parameters
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(request.customerEmail())
                    .setSuccessUrl(request.successUrl())
                    .setCancelUrl(request.cancelUrl())
                    .addAllLineItem(lineItems)
                    .putMetadata("orderId", request.orderId());

            // Add any additional metadata
            if (request.metadata() != null) {
                request.metadata().forEach(paramsBuilder::putMetadata);
            }

            // Create the session
            Session session = Session.create(paramsBuilder.build());

            log.info("Created checkout session: {} for order: {}", session.getId(), request.orderId());
            return session;
        } catch (StripeException e) {
            log.error("Error creating checkout session", e);
            throw new PaymentProcessingException("Failed to create checkout session", e);
        }
    }

    private boolean isValidEmail(String email) {
        // Basic email validation - you might want to use a more robust solution
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
