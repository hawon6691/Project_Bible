package com.pbshop.java.spring.maven.jpa.postgresql.cart.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class CartDtos {

    private CartDtos() {
    }

    public record StoreCartItemRequest(
            @NotNull Long productId,
            @NotNull Long sellerId,
            @NotNull @Min(1) Integer quantity,
            List<String> selectedOptions
    ) {
    }

    public record UpdateCartItemRequest(
            @NotNull @Min(1) Integer quantity
    ) {
    }
}
