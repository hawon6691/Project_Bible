package com.pbshop.java.spring.maven.jpa.postgresql.pcbuilder.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PcBuilderDtos {
    private PcBuilderDtos() {
    }
    public record CreateBuildRequest(@NotBlank String name, String description) { }
    public record UpdateBuildRequest(String name, String description) { }
    public record AddPartRequest(@NotBlank String partType, @NotNull Long productId, Integer quantity) { }
    public record CompatibilityRuleRequest(@NotBlank String name, @NotBlank String sourcePartType, @NotBlank String targetPartType,
                                           @NotBlank String ruleType, Map<String, Object> ruleValue) { }
}
