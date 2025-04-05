package com.rj.payment_service.controller;

import com.rj.payment_service.config.WebSecurityConfig;
import com.rj.payment_service.service.PaymentService;
import com.rj.payment_service.service.StripeWebHook;
import com.rj.payment_service.type.StripeEventType;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
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
                case CHECKOUT_SESSION_COMPLETED -> eventHandler.handleCheckoutSessionCompleted((Session) stripeObject);
                case CHECKOUT_SESSION_EXPIRED -> eventHandler.handleCheckoutSessionExpired((Session) stripeObject);
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

}
