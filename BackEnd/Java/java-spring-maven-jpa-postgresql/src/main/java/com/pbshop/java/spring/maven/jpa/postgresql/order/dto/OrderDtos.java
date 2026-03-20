package com.pbshop.java.spring.maven.jpa.postgresql.order.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotNull Long addressId,
            @Valid List<CreateOrderItemRequest> items,
            @NotNull Boolean fromCart,
            @NotNull @Min(0) BigDecimal pointUsed,
            String memo
    ) {
    }

    public record CreateOrderItemRequest(
            @NotNull Long productId,
            @NotNull Long sellerId,
            @NotNull @Min(1) Integer quantity,
            List<String> selectedOptions
    ) {
    }

    public record UpdateOrderStatusRequest(
            @NotBlank String status
    ) {
    }
}
