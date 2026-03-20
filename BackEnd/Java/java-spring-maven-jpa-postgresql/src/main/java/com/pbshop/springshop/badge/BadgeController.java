package com.pbshop.springshop.badge;

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
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.badge.dto.BadgeDtos.CreateBadgeRequest;
import com.pbshop.springshop.badge.dto.BadgeDtos.GrantBadgeRequest;
import com.pbshop.springshop.badge.dto.BadgeDtos.UpdateBadgeRequest;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping("/badges")
    public ApiResponse<List<Map<String, Object>>> getBadges() {
        return ApiResponse.success(badgeService.getBadges());
    }

    @GetMapping("/badges/me")
    public ApiResponse<List<Map<String, Object>>> getMyBadges(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(badgeService.getMyBadges(principal));
    }

    @GetMapping("/users/{id}/badges")
    public ApiResponse<List<Map<String, Object>>> getUserBadges(@PathVariable Long id) {
        return ApiResponse.success(badgeService.getUserBadges(id));
    }

    @PostMapping("/admin/badges")
    public ApiResponse<Map<String, Object>> createBadge(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreateBadgeRequest request
    ) {
        return ApiResponse.success(badgeService.createBadge(principal, request));
    }

    @PatchMapping("/admin/badges/{id}")
    public ApiResponse<Map<String, Object>> updateBadge(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBadgeRequest request
    ) {
        return ApiResponse.success(badgeService.updateBadge(principal, id, request));
    }

    @DeleteMapping("/admin/badges/{id}")
    public ApiResponse<Map<String, Object>> deleteBadge(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(badgeService.deleteBadge(principal, id));
    }

    @PostMapping("/admin/badges/{id}/grant")
    public ApiResponse<Map<String, Object>> grantBadge(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody GrantBadgeRequest request
    ) {
        return ApiResponse.success(badgeService.grantBadge(principal, id, request));
    }

    @DeleteMapping("/admin/badges/{id}/revoke/{userId}")
    public ApiResponse<Map<String, Object>> revokeBadge(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        return ApiResponse.success(badgeService.revokeBadge(principal, id, userId));
    }
}
