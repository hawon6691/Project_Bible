package com.pbshop.springshop.analytics;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/products/{id}/lowest-ever")
    public ApiResponse<Map<String, Object>> lowestEver(@PathVariable Long id) {
        return ApiResponse.success(analyticsService.lowestEver(id));
    }

    @GetMapping("/products/{id}/unit-price")
    public ApiResponse<Map<String, Object>> unitPrice(@PathVariable Long id) {
        return ApiResponse.success(analyticsService.unitPrice(id));
    }
}
