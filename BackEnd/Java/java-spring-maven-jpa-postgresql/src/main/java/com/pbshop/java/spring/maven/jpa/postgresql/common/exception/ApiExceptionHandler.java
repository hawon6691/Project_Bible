package com.pbshop.java.spring.maven.jpa.postgresql.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();

        return ResponseEntity.status(errorCode.status())
                .body(ApiResponse.failure(errorCode.code(), exception.getMessage(), exception.details()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, Object> details = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.status())
                .body(ApiResponse.failure(
                        ErrorCode.VALIDATION_ERROR.code(),
                        ErrorCode.VALIDATION_ERROR.message(),
                        details
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(405)
                .body(ApiResponse.failure(
                        "COMMON_405",
                        "허용되지 않은 요청 메서드입니다.",
                        Map.of("method", exception.getMethod())
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception exception) {
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status())
                .body(ApiResponse.failure(
                        ErrorCode.INTERNAL_ERROR.code(),
                        ErrorCode.INTERNAL_ERROR.message(),
                        Map.of("exception", exception.getClass().getSimpleName())
                ));
    }
}
