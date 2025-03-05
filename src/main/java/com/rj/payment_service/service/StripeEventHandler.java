package com.rj.payment_service.service;

import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;

public interface StripeEventHandler {
    void handlePaymentIntentCreated(PaymentIntent paymentIntent);
    void handlePaymentIntentSucceeded(PaymentIntent paymentIntent);
    void handlePaymentIntentFailed(PaymentIntent paymentIntent);
    void handlePaymentIntentCanceled(PaymentIntent paymentIntent);
    void handleChargeFailed(Charge charge);
    void handleChargeSucceeded(Charge charge);
    void handleUnknownEvent(String eventType, StripeObject object);
}
