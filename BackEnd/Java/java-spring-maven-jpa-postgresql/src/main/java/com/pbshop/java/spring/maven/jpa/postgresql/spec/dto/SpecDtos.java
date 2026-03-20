package com.pbshop.java.spring.maven.jpa.postgresql.spec.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public final class SpecDtos {

    private SpecDtos() {
    }

    public record SaveSpecDefinitionRequest(
            @NotNull Long categoryId,
            @NotBlank String key,
            @NotBlank String name,
            String inputType,
            List<String> options,
            String unit,
            Integer sortOrder,
            Boolean filterable
    ) {
    }

    public record ProductSpecValueRequest(
            @NotBlank String key,
            @NotBlank String value,
            Integer sortOrder
    ) {
    }

    public record SetProductSpecsRequest(
            @NotEmpty List<@Valid ProductSpecValueRequest> specs
    ) {
    }
}
