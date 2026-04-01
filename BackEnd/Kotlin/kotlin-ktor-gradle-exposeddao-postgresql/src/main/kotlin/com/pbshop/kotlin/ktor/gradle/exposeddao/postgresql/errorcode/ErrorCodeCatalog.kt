package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode

data class ErrorCodeRecord(
    val key: String,
    val code: String,
    val message: String,
)

val ERROR_CODE_CATALOG: List<ErrorCodeRecord> =
    listOf(
        ErrorCodeRecord("AUTH_REQUIRED", "AUTH_REQUIRED", "Authentication is required."),
        ErrorCodeRecord("COMMON_004", "COMMON_004", "Rate limit exceeded for the current time window."),
        ErrorCodeRecord("FILE_UPLOAD_FAILED", "FILE_UPLOAD_FAILED", "Failed to upload file."),
        ErrorCodeRecord("FORBIDDEN", "FORBIDDEN", "The current role is not allowed to access this endpoint."),
        ErrorCodeRecord("INTERNAL_SERVER_ERROR", "INTERNAL_SERVER_ERROR", "An unexpected error occurred."),
        ErrorCodeRecord("PRODUCT_NOT_FOUND", "PRODUCT_NOT_FOUND", "Product not found."),
        ErrorCodeRecord("RESOURCE_NOT_FOUND", "RESOURCE_NOT_FOUND", "Resource not found."),
        ErrorCodeRecord("VALIDATION_ERROR", "VALIDATION_ERROR", "Validation failed."),
    )
