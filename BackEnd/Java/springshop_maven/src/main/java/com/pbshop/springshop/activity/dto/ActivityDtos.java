package com.pbshop.springshop.activity.dto;

import jakarta.validation.constraints.NotBlank;

public final class ActivityDtos {

    private ActivityDtos() {
    }

    public record CreateSearchRequest(
            @NotBlank String keyword
    ) {
    }
}
