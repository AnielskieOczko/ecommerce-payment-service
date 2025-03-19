package com.rj.payment_service.listener;

import com.rj.payment_service.dto.request.PaymentIntentRequestDTO;

import com.rj.payment_service.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static com.rj.payment_service.config.RabbitMQConfig.PAYMENT_INTENT_QUEUE;


@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = PAYMENT_INTENT_QUEUE)
    public void handlePaymentIntentRequest(
            PaymentIntentRequestDTO request,
            Message message
    ) throws StripeException {
        String correlationId = message.getMessageProperties().getCorrelationId();
        log.info("Received payment request. CorrelationId: {}, OrderId: {}",
                correlationId, request.orderId());

        paymentService.createPaymentIntent(request);
        log.info("Payment intent created successfully. CorrelationId: {}, OrderId: {}",
                correlationId, request.orderId());
    }
}
