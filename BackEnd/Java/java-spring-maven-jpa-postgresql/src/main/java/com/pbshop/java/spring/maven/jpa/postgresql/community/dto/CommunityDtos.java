package com.pbshop.java.spring.maven.jpa.postgresql.community.dto;

import jakarta.validation.constraints.NotBlank;

public final class CommunityDtos {

    private CommunityDtos() {
    }

    public record SavePostRequest(
            @NotBlank String title,
            @NotBlank String content
    ) {
    }

    public record SaveCommentRequest(
            @NotBlank String content
    ) {
    }
}
