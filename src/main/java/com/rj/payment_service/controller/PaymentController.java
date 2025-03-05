package com.rj.payment_service.controller;

import com.rj.payment_service.PaymentIntentDTO;
import com.rj.payment_service.PaymentRequestDTO;
import com.rj.payment_service.config.StripeProperties;
import com.rj.payment_service.service.PaymentService;
import com.rj.payment_service.service.StripeEventHandler;
import com.rj.payment_service.service.StripeEventType;
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
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeProperties stripeProperties;
    private final StripeEventHandler eventHandler;

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
