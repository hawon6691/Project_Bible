package com.pbshop.java.spring.maven.jpa.postgresql.usedmarket;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/used-market")
public class UsedMarketController {

    private final UsedMarketService usedMarketService;

    public UsedMarketController(UsedMarketService usedMarketService) {
        this.usedMarketService = usedMarketService;
    }

    @GetMapping("/products/{id}/price")
    public ApiResponse<Map<String, Object>> productPrice(@PathVariable Long id) {
        return ApiResponse.success(usedMarketService.productPrice(id));
    }

    @GetMapping("/categories/{id}/prices")
    public ApiResponse<Map<String, Object>> categoryPrices(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        return ApiResponse.success(usedMarketService.categoryPrices(id, page, limit));
    }

    @PostMapping("/pc-builds/{buildId}/estimate")
    public ApiResponse<Map<String, Object>> estimateBuild(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long buildId
    ) {
        return ApiResponse.success(usedMarketService.estimateBuild(principal, buildId));
    }
}
