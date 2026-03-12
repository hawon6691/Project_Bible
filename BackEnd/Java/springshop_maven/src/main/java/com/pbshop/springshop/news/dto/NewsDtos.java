package com.pbshop.springshop.news.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public final class NewsDtos {
    private NewsDtos() {
    }
    public record CreateNewsRequest(Long categoryId, @NotBlank String title, @NotBlank String content, String thumbnailUrl, List<Long> productIds) { }
    public record UpdateNewsRequest(Long categoryId, String title, String content, String thumbnailUrl, List<Long> productIds) { }
    public record CreateNewsCategoryRequest(@NotBlank String name, String slug) { }
}
