package com.pbshop.springshop.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record CreatePaymentRequest(
            @NotNull Long orderId,
            @NotBlank String method
    ) {
    }
}
