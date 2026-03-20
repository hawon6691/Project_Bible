package com.pbshop.java.spring.maven.jpa.postgresql.shortform.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public final class ShortformDtos {
    private ShortformDtos() {
    }
    public record CreateShortformRequest(@NotBlank String title, @NotBlank String videoUrl, String thumbnailUrl, List<Long> productIds) { }
    public record CommentRequest(@NotBlank String content) { }
}
