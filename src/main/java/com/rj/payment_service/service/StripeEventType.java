package com.rj.payment_service.service;

import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;

public enum StripeEventType {
    PAYMENT_INTENT_CREATED("payment_intent.created", PaymentIntent.class),
    PAYMENT_INTENT_SUCCEEDED("payment_intent.succeeded", PaymentIntent.class),
    PAYMENT_INTENT_FAILED("payment_intent.failed", PaymentIntent.class),
    PAYMENT_INTENT_CANCELED("payment_intent.canceled", PaymentIntent.class),
    CHARGE_FAILED("charge.failed", Charge.class),
    CHARGE_SUCCEEDED("charge.succeeded", Charge.class);

    private final String eventName;

    // expected class of the event object type
    private final Class<? extends StripeObject> eventClass;

    StripeEventType(String eventName, Class<? extends StripeObject> eventClass) {
        this.eventName = eventName;
        this.eventClass = eventClass;
    }

    public String getEventName() {
        return eventName;
    }

    public Class<? extends StripeObject> getEventClass() {
        return eventClass;
    }

    public static StripeEventType fromStripeEventName(String eventName) {
        // values() returns array of all enum constants
        for (StripeEventType eventType: values()) {
            if (eventType.eventName.equals(eventName)) {
                return eventType;
            }
        }
        return null;
    }

}
