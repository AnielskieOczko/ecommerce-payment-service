package com.rj.payment_service.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface PaymentService {

    public PaymentIntent createPaymentIntent(Long amountInCents, String currency) throws StripeException;
}
