package com.pbshop.springshop.price.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public final class PriceDtos {

    private PriceDtos() {
    }

    public record SavePriceEntryRequest(
            @NotNull Long sellerId,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @DecimalMin("0.0") BigDecimal originalPrice,
            @DecimalMin("0.0") BigDecimal shippingFee,
            String stockStatus,
            String purchaseUrl
    ) {
    }

    public record UpdatePriceEntryRequest(
            @DecimalMin("0.0") BigDecimal price,
            @DecimalMin("0.0") BigDecimal originalPrice,
            @DecimalMin("0.0") BigDecimal shippingFee,
            String stockStatus,
            String purchaseUrl
    ) {
    }

    public record CreatePriceAlertRequest(
            @NotNull Long productId,
            @NotNull @DecimalMin("0.0") BigDecimal targetPrice
    ) {
    }
}
