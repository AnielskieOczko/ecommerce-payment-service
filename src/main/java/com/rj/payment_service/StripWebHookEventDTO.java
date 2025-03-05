package com.rj.payment_service;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record StripWebHookEventDTO(
        String id,
        String type,
        Object data,
        @JsonProperty("created")
        LocalDateTime created
) {
}
