package com.pbshop.springshop.adminsettings.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public final class AdminSettingsDtos {
    private AdminSettingsDtos() {
    }

    public record UpdateExtensionsRequest(@NotEmpty List<String> extensions) { }
    public record UpdateUploadLimitsRequest(@NotNull Integer image, @NotNull Integer video, @NotNull Integer audio) { }
    public record UpdateReviewPolicyRequest(@NotNull Integer maxImageCount, @NotNull Integer pointAmount) { }
}
