package com.pbshop.springshop.compare.dto;

import jakarta.validation.constraints.NotNull;

public final class CompareDtos {
    private CompareDtos() {
    }

    public record AddCompareItemRequest(@NotNull Long productId) { }
}
