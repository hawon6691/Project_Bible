package com.pbshop.java.spring.maven.jpa.postgresql.point.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PointDtos {

    private PointDtos() {
    }

    public record GrantPointRequest(
            @NotNull Long userId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String description
    ) {
    }
}
