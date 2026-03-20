package com.pbshop.java.spring.maven.jpa.postgresql.recommendation.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public final class RecommendationDtos {

    private RecommendationDtos() {
    }

    public record SaveRecommendationRequest(
            @NotNull Long productId,
            String targetType,
            Long targetUserId,
            String reason,
            BigDecimal score
    ) {
    }
}
