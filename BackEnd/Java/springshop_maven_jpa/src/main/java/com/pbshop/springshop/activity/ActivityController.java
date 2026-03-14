package com.pbshop.springshop.activity;

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
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.activity.dto.ActivityDtos.CreateSearchRequest;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getActivities(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(activityService.getActivities(principal));
    }

    @GetMapping("/recent-products")
    public ApiResponse<List<Map<String, Object>>> getRecentProducts(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(activityService.getRecentProducts(principal));
    }

    @PostMapping("/recent-products/{productId}")
    public ApiResponse<Map<String, Object>> addRecentProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId
    ) {
        return ApiResponse.success(activityService.addRecentProduct(principal, productId));
    }

    @GetMapping("/searches")
    public ApiResponse<List<Map<String, Object>>> getSearches(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(activityService.getSearches(principal));
    }

    @PostMapping("/searches")
    public ApiResponse<Map<String, Object>> createSearch(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreateSearchRequest request
    ) {
        return ApiResponse.success(activityService.createSearch(principal, request));
    }

    @DeleteMapping("/searches/{id}")
    public ApiResponse<Map<String, Object>> deleteSearch(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(activityService.deleteSearch(principal, id));
    }

    @DeleteMapping("/searches")
    public ApiResponse<Map<String, Object>> clearSearches(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(activityService.clearSearches(principal));
    }
}
