package com.pbshop.java.spring.maven.jpa.postgresql.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record SaveProductRequest(
            @NotNull Long categoryId,
            @NotBlank @Size(max = 255) String name,
            String slug,
            String brand,
            String description,
            String thumbnailUrl,
            @PositiveOrZero Integer reviewCount,
            @PositiveOrZero BigDecimal ratingAvg,
            String status
    ) {
    }
}
