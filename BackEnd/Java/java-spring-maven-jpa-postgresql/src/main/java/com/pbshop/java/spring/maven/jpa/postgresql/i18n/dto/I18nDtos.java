package com.pbshop.java.spring.maven.jpa.postgresql.i18n.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class I18nDtos {

    private I18nDtos() {
    }

    public record UpsertTranslationRequest(
            @NotBlank String locale,
            @NotBlank String namespace,
            @NotBlank String key,
            @NotBlank String value
    ) {
    }

    public record UpsertExchangeRateRequest(
            @NotBlank String baseCurrency,
            @NotBlank String targetCurrency,
            @NotNull @DecimalMin("0.00000001") BigDecimal rate
    ) {
    }
}
