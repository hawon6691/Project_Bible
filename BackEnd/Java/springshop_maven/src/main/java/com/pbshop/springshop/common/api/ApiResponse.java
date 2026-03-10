package com.pbshop.springshop.common.api;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiResponse<T>(
    boolean success,
    T data,
    ApiMeta meta,
    ApiError error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            true,
            data,
            new ApiMeta(OffsetDateTime.now().toString(), "ko-KR", "KRW"),
            null
        );
    }

    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(
            false,
            null,
            new ApiMeta(OffsetDateTime.now().toString(), "ko-KR", "KRW"),
            new ApiError(code, message, Map.of())
        );
    }

    public record ApiMeta(
        String timestamp,
        String locale,
        String currency
    ) {}

    public record ApiError(
        String code,
        String message,
        Map<String, Object> details
    ) {}
}
