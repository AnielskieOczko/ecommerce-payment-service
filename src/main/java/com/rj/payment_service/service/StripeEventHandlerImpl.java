package com.rj.payment_service.service;

import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeEventHandlerImpl implements StripeEventHandler{
    @Override
    public void handlePaymentIntentCreated(PaymentIntent paymentIntent) {
        log.info("Processing event: payment intent created: {}", paymentIntent.getId());
    }

    @Override
    public void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        log.info("Processing event: payment intent succeeded: {}", paymentIntent.getId());
    }

    @Override
    public void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        log.info("Processing event: payment intent failed: {}", paymentIntent.getId());
    }

    @Override
    public void handlePaymentIntentCanceled(PaymentIntent paymentIntent) {
        log.info("Processing event: payment intent canceled: {}", paymentIntent.getId());
    }

    @Override
    public void handleChargeFailed(Charge charge) {
        log.info("Processing event: charge failed: {}", charge.getId());
    }

    @Override
    public void handleChargeSucceeded(Charge charge) {
        log.info("Processing event: charge succeeded: {}", charge.getId());
    }

    @Override
    public void handleUnknownEvent(String eventType, StripeObject object) {
        log.info("Unhandled event type: {} with object: {}", eventType, object);
    }
}
