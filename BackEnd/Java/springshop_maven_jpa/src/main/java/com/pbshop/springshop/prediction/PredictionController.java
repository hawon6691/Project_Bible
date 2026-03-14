package com.pbshop.springshop.prediction;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/products/{productId}/price-trend")
    public ApiResponse<Map<String, Object>> getPriceTrend(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer days
    ) {
        return ApiResponse.success(predictionService.getPriceTrend(productId, days));
    }
}
