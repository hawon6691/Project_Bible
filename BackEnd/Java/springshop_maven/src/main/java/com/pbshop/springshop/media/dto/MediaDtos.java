package com.pbshop.springshop.media.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class MediaDtos {
    private MediaDtos() {
    }
    public record UploadFileItem(@NotBlank String fileName, @NotBlank String fileUrl, @NotBlank String mimeType, @NotNull Long size) { }
    public record UploadMediaRequest(String ownerType, Long ownerId, @NotNull List<UploadFileItem> files) { }
    public record PresignedUrlRequest(@NotBlank String fileName) { }
}
