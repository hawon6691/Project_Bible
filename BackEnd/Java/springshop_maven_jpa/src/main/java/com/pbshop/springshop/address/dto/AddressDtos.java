package com.pbshop.springshop.address.dto;

import jakarta.validation.constraints.NotBlank;

public final class AddressDtos {

    private AddressDtos() {
    }

    public record SaveAddressRequest(
            @NotBlank String recipientName,
            @NotBlank String phone,
            @NotBlank String zipCode,
            @NotBlank String address1,
            String address2,
            String label,
            String deliveryRequest,
            Boolean isDefault
    ) {
    }
}
