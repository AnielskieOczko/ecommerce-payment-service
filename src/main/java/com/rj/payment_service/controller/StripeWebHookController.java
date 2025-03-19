package com.rj.payment_service.controller;

import com.rj.payment_service.dto.request.PaymentIntentRequestDTO;
import com.rj.payment_service.dto.stripe.StripePaymentIntentDTO;
import com.rj.payment_service.config.WebSecurityConfig;
import com.rj.payment_service.service.PaymentService;
import com.rj.payment_service.service.StripeWebHook;
import com.rj.payment_service.type.StripeEventType;
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

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class StripeWebHookController {

    private final PaymentService paymentService;
    private final WebSecurityConfig stripeProperties;
    private final StripeWebHook eventHandler;

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
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize stripe object"));

            StripeEventType eventType = StripeEventType.fromStripeEventName(event.getType());

            if (eventType == null) {
                log.error("Unhandled event type: {}", event.getType());
                eventHandler.handleUnknownEvent(event.getType(), stripeObject);
                return ResponseEntity.ok().build();
            }

            switch (eventType) {
                case PAYMENT_INTENT_SUCCEEDED ->
                        eventHandler.handlePaymentIntentSucceeded((PaymentIntent) stripeObject);
                case PAYMENT_INTENT_FAILED -> eventHandler.handlePaymentIntentFailed((PaymentIntent) stripeObject);
                case PAYMENT_INTENT_CREATED -> eventHandler.handlePaymentIntentCreated((PaymentIntent) stripeObject);
                case PAYMENT_INTENT_CANCELED -> eventHandler.handlePaymentIntentCanceled((PaymentIntent) stripeObject);
                case CHARGE_FAILED -> eventHandler.handleChargeFailed((Charge) stripeObject);
                case CHARGE_SUCCEEDED -> eventHandler.handleChargeSucceeded((Charge) stripeObject);
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
    public ResponseEntity<StripePaymentIntentDTO> createPaymentIntent(@RequestBody PaymentIntentRequestDTO request) throws StripeException {
        PaymentIntent intent = paymentService.createPaymentIntent(request);

        StripePaymentIntentDTO response = StripePaymentIntentDTO.builder()
                .id(intent.getId())
                .clientSecret(intent.getClientSecret())
                .amount(intent.getAmount())
                .currency(intent.getCurrency())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<StripePaymentIntentDTO> confirmPayment(@PathVariable String paymentIntentId) throws StripeException {
        PaymentIntent confirmedIntent = paymentService.confirmPayment(paymentIntentId);

        StripePaymentIntentDTO response = StripePaymentIntentDTO.builder()
                .id(confirmedIntent.getId())
                .clientSecret(confirmedIntent.getClientSecret())
                .amount(confirmedIntent.getAmount())
                .currency(confirmedIntent.getCurrency())
                .build();

        return ResponseEntity.ok(response);
    }
}
