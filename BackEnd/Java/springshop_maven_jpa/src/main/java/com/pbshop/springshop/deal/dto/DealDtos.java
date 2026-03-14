package com.pbshop.springshop.deal.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public final class DealDtos {

    private DealDtos() {
    }

    public record SaveDealRequest(
            @NotNull Long productId,
            @NotBlank String title,
            @NotBlank String type,
            String description,
            @NotNull BigDecimal dealPrice,
            @Min(0) @Max(100) Integer discountRate,
            @PositiveOrZero Integer stock,
            String bannerUrl,
            @NotNull OffsetDateTime startAt,
            @NotNull OffsetDateTime endAt
    ) {
    }

    public record UpdateDealRequest(
            String title,
            String type,
            String description,
            BigDecimal dealPrice,
            @Min(0) @Max(100) Integer discountRate,
            @PositiveOrZero Integer stock,
            String bannerUrl,
            String status,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
    }
}
