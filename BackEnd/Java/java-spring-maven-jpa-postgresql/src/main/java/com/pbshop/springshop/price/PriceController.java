package com.pbshop.springshop.price;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.price.dto.PriceDtos.CreatePriceAlertRequest;
import com.pbshop.springshop.price.dto.PriceDtos.SavePriceEntryRequest;
import com.pbshop.springshop.price.dto.PriceDtos.UpdatePriceEntryRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/products/{productId}/prices")
    public ApiResponse<Map<String, Object>> getProductPrices(@PathVariable Long productId) {
        return ApiResponse.success(priceService.getProductPrices(productId));
    }

    @PostMapping("/products/{productId}/prices")
    public ApiResponse<Map<String, Object>> createPriceEntry(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody SavePriceEntryRequest request
    ) {
        return ApiResponse.success(priceService.createPriceEntry(principal, productId, request));
    }

    @PatchMapping("/prices/{id}")
    public ApiResponse<Map<String, Object>> updatePriceEntry(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriceEntryRequest request
    ) {
        return ApiResponse.success(priceService.updatePriceEntry(principal, id, request));
    }

    @DeleteMapping("/prices/{id}")
    public ApiResponse<Map<String, Object>> deletePriceEntry(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(priceService.deletePriceEntry(principal, id));
    }

    @GetMapping("/products/{productId}/price-history")
    public ApiResponse<Map<String, Object>> getPriceHistory(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer days
    ) {
        return ApiResponse.success(priceService.getPriceHistory(productId, days));
    }

    @GetMapping("/price-alerts")
    public ApiResponse<List<Map<String, Object>>> getPriceAlerts(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(priceService.getAlerts(principal));
    }

    @PostMapping("/price-alerts")
    public ApiResponse<Map<String, Object>> createPriceAlert(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreatePriceAlertRequest request
    ) {
        return ApiResponse.success(priceService.createAlert(principal, request));
    }

    @DeleteMapping("/price-alerts/{id}")
    public ApiResponse<Map<String, Object>> deletePriceAlert(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(priceService.deleteAlert(principal, id));
    }
}
