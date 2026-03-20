package com.pbshop.java.spring.maven.jpa.postgresql.fraud;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class FraudController {

    private final FraudService fraudService;

    public FraudController(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @GetMapping("/fraud/alerts")
    public ApiResponse<List<Map<String, Object>>> getAlerts(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(fraudService.getAlerts(principal, status));
    }

    @PatchMapping("/fraud/alerts/{id}/approve")
    public ApiResponse<Map<String, Object>> approve(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(fraudService.approveAlert(principal, id));
    }

    @PatchMapping("/fraud/alerts/{id}/reject")
    public ApiResponse<Map<String, Object>> reject(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(fraudService.rejectAlert(principal, id));
    }

    @GetMapping("/products/{id}/real-price")
    public ApiResponse<Map<String, Object>> getRealPrice(
            @PathVariable Long id,
            @RequestParam(required = false) Long sellerId
    ) {
        return ApiResponse.success(fraudService.getRealPrice(id, sellerId));
    }

    @GetMapping("/fraud/products/{productId}/effective-prices")
    public ApiResponse<List<Map<String, Object>>> getEffectivePrices(@PathVariable Long productId) {
        return ApiResponse.success(fraudService.getEffectivePrices(productId));
    }

    @GetMapping("/fraud/products/{productId}/anomalies")
    public ApiResponse<Map<String, Object>> getAnomalies(@PathVariable Long productId) {
        return ApiResponse.success(fraudService.detectAnomalies(productId, false));
    }

    @PostMapping("/fraud/admin/products/{productId}/scan")
    public ApiResponse<Map<String, Object>> scan(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId
    ) {
        return ApiResponse.success(fraudService.scan(principal, productId));
    }

    @GetMapping("/fraud/admin/products/{productId}/flags")
    public ApiResponse<List<Map<String, Object>>> getFlags(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId
    ) {
        return ApiResponse.success(fraudService.getFlags(principal, productId));
    }
}
