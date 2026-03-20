package com.pbshop.java.spring.maven.jpa.postgresql.common.api;

import java.time.OffsetDateTime;
import java.util.Map;

import com.pbshop.java.spring.maven.jpa.postgresql.common.web.ApiRequestContext;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiMeta meta,
        ApiError error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, ApiMeta.fromCurrentRequest(), null);
    }

    public static ApiResponse<Void> failure(String code, String message) {
        return failure(code, message, Map.of());
    }

    public static ApiResponse<Void> failure(String code, String message, Map<String, Object> details) {
        return new ApiResponse<>(false, null, ApiMeta.fromCurrentRequest(), new ApiError(code, message, details));
    }

    public record ApiMeta(
            String timestamp,
            String locale,
            String currency,
            String requestId
    ) {

        static ApiMeta fromCurrentRequest() {
            ApiRequestContext context = ApiRequestContext.current();

            return new ApiMeta(
                    OffsetDateTime.now().toString(),
                    context.locale(),
                    context.currency(),
                    context.requestId()
            );
        }
    }

    public record ApiError(
            String code,
            String message,
            Map<String, Object> details
    ) {
    }
}
