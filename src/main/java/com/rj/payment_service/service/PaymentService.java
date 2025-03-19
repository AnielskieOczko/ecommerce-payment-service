package com.rj.payment_service.service;

import com.rj.payment_service.dto.request.PaymentIntentRequestDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface PaymentService {

    public PaymentIntent createPaymentIntent(PaymentIntentRequestDTO request) throws StripeException;

    public PaymentIntent confirmPayment(String paymentIntentId) throws StripeException;
}
