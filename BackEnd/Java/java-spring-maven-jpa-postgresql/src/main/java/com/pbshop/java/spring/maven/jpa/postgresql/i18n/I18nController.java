package com.pbshop.java.spring.maven.jpa.postgresql.i18n;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.i18n.dto.I18nDtos.UpsertExchangeRateRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.i18n.dto.I18nDtos.UpsertTranslationRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/i18n")
public class I18nController {

    private final I18nService i18nService;

    public I18nController(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @GetMapping("/translations")
    public ApiResponse<List<Map<String, Object>>> getTranslations(
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) String namespace
    ) {
        return ApiResponse.success(i18nService.getTranslations(locale, namespace));
    }

    @PostMapping("/admin/translations")
    public ApiResponse<Map<String, Object>> upsertTranslation(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpsertTranslationRequest request
    ) {
        return ApiResponse.success(i18nService.upsertTranslation(principal, request));
    }

    @DeleteMapping("/admin/translations/{id}")
    public ApiResponse<Map<String, Object>> deleteTranslation(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(i18nService.deleteTranslation(principal, id));
    }

    @GetMapping("/exchange-rates")
    public ApiResponse<List<Map<String, Object>>> getExchangeRates() {
        return ApiResponse.success(i18nService.getExchangeRates());
    }

    @PostMapping("/admin/exchange-rates")
    public ApiResponse<Map<String, Object>> upsertExchangeRate(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpsertExchangeRateRequest request
    ) {
        return ApiResponse.success(i18nService.upsertExchangeRate(principal, request));
    }

    @GetMapping("/convert")
    public ApiResponse<Map<String, Object>> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to
    ) {
        return ApiResponse.success(i18nService.convert(amount, from, to));
    }
}
