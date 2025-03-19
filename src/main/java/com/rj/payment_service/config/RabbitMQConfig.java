package com.rj.payment_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    // Payment Intent Creation
    public static final String PAYMENT_INTENT_QUEUE = "payment-intent-queue";
    public static final String PAYMENT_INTENT_EXCHANGE = "payment-intent-exchange";
    public static final String PAYMENT_INTENT_ROUTING_KEY = "payment-intent-routing-key";

    // Payment Intent Response
    public static final String PAYMENT_RESPONSE_QUEUE = "payment-response-queue";
    public static final String PAYMENT_RESPONSE_EXCHANGE = "payment-response-exchange";
    public static final String PAYMENT_RESPONSE_ROUTING_KEY = "payment-response-routing-key";

    // Payment Verification
    public static final String PAYMENT_VERIFICATION_QUEUE = "payment-verification-queue";
    public static final String PAYMENT_VERIFICATION_EXCHANGE = "payment-verification-exchange";
    public static final String PAYMENT_VERIFICATION_ROUTING_KEY = "payment-verification-routing-key";

    // Payment Verification Response
    public static final String VERIFICATION_RESPONSE_QUEUE = "verification-response-queue";
    public static final String VERIFICATION_RESPONSE_EXCHANGE = "verification-response-exchange";
    public static final String VERIFICATION_RESPONSE_ROUTING_KEY = "verification-response-routing-key";

    // Dead Letter Queue
    public static final String DLQ_QUEUE = "payment-dlq-queue";
    public static final String DLQ_EXCHANGE = "payment-dlq-exchange";
    public static final String DLQ_ROUTING_KEY = "payment-dlq-routing-key";



    @Bean
    public TopicExchange paymentVerificationExchange() {
        return new TopicExchange(PAYMENT_VERIFICATION_EXCHANGE, true, false);
    }
    @Bean
    public Queue paymentVerificationQueue() {
        return new Queue(PAYMENT_VERIFICATION_QUEUE, true);
    }
    @Bean
    public Binding paymentVerificationBinding() {
        return BindingBuilder
                .bind(paymentVerificationQueue())
                .to(paymentVerificationExchange())
                .with(PAYMENT_VERIFICATION_ROUTING_KEY);
    }

    @Bean
    public TopicExchange verificationResponseExchange() {
        return new TopicExchange(VERIFICATION_RESPONSE_EXCHANGE, true, false);
    }

    @Bean
    public Queue verificationResponseQueue() {
        return new Queue(VERIFICATION_RESPONSE_QUEUE, true);
    }

    @Bean
    public Binding verificationResponseBinding() {
        return BindingBuilder
                .bind(verificationResponseQueue())
                .to(verificationResponseExchange())
                .with(VERIFICATION_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLQ_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ_QUEUE, true);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public TopicExchange paymentServiceExchange() {
        return new TopicExchange(PAYMENT_INTENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentServiceQueue() {
        return new Queue(PAYMENT_INTENT_QUEUE, true);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(paymentServiceQueue())
                .to(paymentServiceExchange())
                .with(PAYMENT_INTENT_ROUTING_KEY);
    }

    @Bean
    public TopicExchange paymentNotificationExchange() {
        return new TopicExchange(PAYMENT_RESPONSE_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentNotificationQueue() {
        return new Queue(PAYMENT_RESPONSE_QUEUE, true);
    }

    @Bean
    public Binding paymentNotificationBinding() {
        return BindingBuilder
                .bind(paymentNotificationQueue())
                .to(paymentNotificationExchange())
                .with(PAYMENT_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
