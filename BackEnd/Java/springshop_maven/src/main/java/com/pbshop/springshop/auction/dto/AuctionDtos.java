package com.pbshop.springshop.auction.dto;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AuctionDtos {
    private AuctionDtos() {
    }

    public record CreateAuctionRequest(
            @NotBlank String title,
            @NotBlank String description,
            Long categoryId,
            Map<String, Object> specs,
            BigDecimal budget
    ) { }

    public record CreateAuctionBidRequest(
            @NotNull BigDecimal price,
            String description,
            Integer deliveryDays
    ) { }

    public record UpdateAuctionBidRequest(
            BigDecimal price,
            String description,
            Integer deliveryDays
    ) { }
}
