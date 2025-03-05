package com.rj.payment_service.controller;

import com.rj.payment_service.PaymentIntentDTO;
import com.rj.payment_service.PaymentRequestDTO;
import com.rj.payment_service.config.StripeProperties;
import com.rj.payment_service.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
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
    private final StripeProperties stripeProperties;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) throws SignatureVerificationException {

        try {
            Event event = Webhook.constructEvent(
                    payload,
                    signature,
                    stripeProperties.getWebhookSecret()
            );

            log.info("Received webhook event: {}", event.getType());
            StripeObject stripeObject = event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to get payment intent from event"));

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    log.info("Payment succeeded");
                    handlePaymentSucceeded((PaymentIntent) stripeObject);
                    break;
                case "payment_intent.payment_failed":
                    log.info("Payment failed");
                    handlePaymentFailed((PaymentIntent) stripeObject);
                    break;
                case "payment_intent.created":
                    log.info("Payment intent created");
                    handlePaymentCreated((PaymentIntent) stripeObject);
                    break;
                case "payment_intent.canceled":
                    log.info("Payment intent canceled");
                    handlePaymentCanceled((PaymentIntent) stripeObject);
                    break;
                case "charge.failed":
                    log.info("Charge failed");
                    handleChargeFailed((Charge) stripeObject);
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        log.info("Webhook handled successfully");
        return ResponseEntity.ok().build();
    }


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

    private void handlePaymentSucceeded(PaymentIntent paymentIntent) {
        log.info("Payment succeeded: {}", paymentIntent.getId());
        // Implement your business logic here
        // For example: update order status, send confirmation email, etc.
    }

    private void handlePaymentFailed(PaymentIntent paymentIntent) {
        log.info("Payment failed: {}", paymentIntent.getId());
        // Implement your failure handling logic here
        // For example: update order status, notify customer, etc.
    }

    private void handlePaymentCreated(PaymentIntent paymentIntent) {
        log.info("Payment intent created: {}", paymentIntent.getId());
    }

    private void handlePaymentCanceled(PaymentIntent paymentIntent) {
        log.info("Payment intent canceled: {}", paymentIntent.getId());
    }

    private void handleChargeFailed(Charge charge) {
        log.info("Charge failed: {}", charge.getId());
    }
}
