package com.pbshop.springshop.adminsettings;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.adminsettings.dto.AdminSettingsDtos.UpdateExtensionsRequest;
import com.pbshop.springshop.adminsettings.dto.AdminSettingsDtos.UpdateReviewPolicyRequest;
import com.pbshop.springshop.adminsettings.dto.AdminSettingsDtos.UpdateUploadLimitsRequest;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/admin/settings")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    @GetMapping("/extensions")
    public ApiResponse<Map<String, Object>> extensions(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(adminSettingsService.extensions(principal));
    }

    @PostMapping("/extensions")
    public ApiResponse<Map<String, Object>> updateExtensions(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdateExtensionsRequest request
    ) {
        return ApiResponse.success(adminSettingsService.updateExtensions(principal, request));
    }

    @GetMapping("/upload-limits")
    public ApiResponse<Map<String, Object>> uploadLimits(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(adminSettingsService.uploadLimits(principal));
    }

    @PatchMapping("/upload-limits")
    public ApiResponse<Map<String, Object>> updateUploadLimits(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdateUploadLimitsRequest request
    ) {
        return ApiResponse.success(adminSettingsService.updateUploadLimits(principal, request));
    }

    @GetMapping("/review-policy")
    public ApiResponse<Map<String, Object>> reviewPolicy(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(adminSettingsService.reviewPolicy(principal));
    }

    @PatchMapping("/review-policy")
    public ApiResponse<Map<String, Object>> updateReviewPolicy(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdateReviewPolicyRequest request
    ) {
        return ApiResponse.success(adminSettingsService.updateReviewPolicy(principal, request));
    }
}
