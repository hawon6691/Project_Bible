package com.pbshop.springshop.seller.dto;

import jakarta.validation.constraints.NotBlank;

public final class SellerDtos {

    private SellerDtos() {
    }

    public record SaveSellerRequest(
            @NotBlank String name,
            @NotBlank String code,
            String homepageUrl,
            String status
    ) {
    }
}
