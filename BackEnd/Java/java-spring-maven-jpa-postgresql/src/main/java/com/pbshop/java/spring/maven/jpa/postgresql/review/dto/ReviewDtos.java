package com.pbshop.java.spring.maven.jpa.postgresql.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record CreateReviewRequest(
            @NotNull Long orderId,
            @Min(1) @Max(5) int rating,
            @NotBlank String content
    ) {
    }

    public record UpdateReviewRequest(
            @Min(1) @Max(5) int rating,
            @NotBlank String content
    ) {
    }
}
