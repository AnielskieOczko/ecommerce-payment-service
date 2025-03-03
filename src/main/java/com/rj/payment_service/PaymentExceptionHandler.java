package com.rj.payment_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {

    public ResponseEntity<ErrorResponse> handlePaymentProcessingException(PaymentProcessingException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("PAYMENT_PROCESSING_ERROR",e.getMessage()));
    }

}
