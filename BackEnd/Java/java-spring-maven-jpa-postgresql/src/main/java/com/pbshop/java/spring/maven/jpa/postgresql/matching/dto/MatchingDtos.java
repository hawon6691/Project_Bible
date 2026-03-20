package com.pbshop.java.spring.maven.jpa.postgresql.matching.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class MatchingDtos {
    private MatchingDtos() {
    }
    public record ApproveMappingRequest(@NotNull Long productId) { }
    public record RejectMappingRequest(@NotBlank String reason) { }
}
