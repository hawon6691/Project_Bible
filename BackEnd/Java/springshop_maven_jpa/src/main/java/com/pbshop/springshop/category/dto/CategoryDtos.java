package com.pbshop.springshop.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class CategoryDtos {

    private CategoryDtos() {
    }

    public record SaveCategoryRequest(
            Long parentId,
            @NotBlank @Size(max = 255) String name,
            String slug,
            @PositiveOrZero Integer sortOrder,
            Boolean isVisible
    ) {
    }
}
