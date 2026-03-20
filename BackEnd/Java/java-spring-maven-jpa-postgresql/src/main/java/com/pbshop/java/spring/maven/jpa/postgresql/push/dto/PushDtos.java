package com.pbshop.java.spring.maven.jpa.postgresql.push.dto;

import jakarta.validation.constraints.NotBlank;

public final class PushDtos {

    private PushDtos() {
    }

    public record RegisterSubscriptionRequest(
            @NotBlank String endpoint,
            String p256dh,
            String auth,
            String vapidPublicKey
    ) {
    }

    public record UnregisterSubscriptionRequest(
            @NotBlank String endpoint
    ) {
    }

    public record UpdatePreferenceRequest(
            boolean marketingEnabled,
            boolean orderEnabled,
            boolean chatEnabled,
            boolean dealEnabled
    ) {
    }
}
