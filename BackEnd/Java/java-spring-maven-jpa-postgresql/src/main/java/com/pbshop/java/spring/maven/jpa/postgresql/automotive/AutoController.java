package com.pbshop.java.spring.maven.jpa.postgresql.automotive;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.automotive.dto.AutoDtos.EstimateAutoRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/auto")
public class AutoController {

    private final AutoService autoService;

    public AutoController(AutoService autoService) {
        this.autoService = autoService;
    }

    @GetMapping("/models")
    public ApiResponse<List<Map<String, Object>>> models(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String type
    ) {
        return ApiResponse.success(autoService.models(brand, type));
    }

    @GetMapping("/models/{id}/trims")
    public ApiResponse<List<Map<String, Object>>> trims(@PathVariable Long id) {
        return ApiResponse.success(autoService.trims(id));
    }

    @PostMapping("/estimate")
    public ApiResponse<Map<String, Object>> estimate(@Valid @RequestBody EstimateAutoRequest request) {
        return ApiResponse.success(autoService.estimate(request));
    }

    @GetMapping("/models/{id}/lease-offers")
    public ApiResponse<List<Map<String, Object>>> leaseOffers(@PathVariable Long id) {
        return ApiResponse.success(autoService.leaseOffers(id));
    }
}
