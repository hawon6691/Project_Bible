package com.pbshop.springshop.badge.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class BadgeDtos {

    private BadgeDtos() {
    }

    public record CreateBadgeRequest(
            @NotBlank String name,
            String description,
            String iconUrl,
            String type,
            Map<String, Object> condition,
            String rarity
    ) {
    }

    public record UpdateBadgeRequest(
            String name,
            String description,
            String iconUrl,
            String type,
            Map<String, Object> condition,
            String rarity
    ) {
    }

    public record GrantBadgeRequest(@NotNull Long userId) {
    }
}
