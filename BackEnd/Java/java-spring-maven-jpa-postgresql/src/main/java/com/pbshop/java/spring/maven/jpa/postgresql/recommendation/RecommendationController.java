package com.pbshop.java.spring.maven.jpa.postgresql.recommendation;

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
import com.pbshop.java.spring.maven.jpa.postgresql.recommendation.dto.RecommendationDtos.SaveRecommendationRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommendations/trending")
    public ApiResponse<Map<String, Object>> getTrendingRecommendations(
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(recommendationService.getTrendingRecommendations(limit));
    }

    @GetMapping("/recommendations/personal")
    public ApiResponse<Map<String, Object>> getPersonalRecommendations(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(recommendationService.getPersonalRecommendations(principal, limit));
    }

    @GetMapping("/admin/recommendations")
    public ApiResponse<List<Map<String, Object>>> getAdminRecommendations(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(recommendationService.getAdminRecommendations(principal));
    }

    @PostMapping("/admin/recommendations")
    public ApiResponse<Map<String, Object>> createRecommendation(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SaveRecommendationRequest request
    ) {
        return ApiResponse.success(recommendationService.createRecommendation(principal, request));
    }

    @DeleteMapping("/admin/recommendations/{id}")
    public ApiResponse<Map<String, Object>> deleteRecommendation(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(recommendationService.deleteRecommendation(principal, id));
    }
}
