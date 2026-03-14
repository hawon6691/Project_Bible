package com.pbshop.springshop.automotive.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public final class AutoDtos {
    private AutoDtos() {
    }

    public record EstimateAutoRequest(@NotNull Long modelId, @NotNull Long trimId, List<Long> optionIds) { }
}
