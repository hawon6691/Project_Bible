package com.pbshop.java.spring.maven.jpa.postgresql.push;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.push.dto.PushDtos.RegisterSubscriptionRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.push.dto.PushDtos.UnregisterSubscriptionRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.push.dto.PushDtos.UpdatePreferenceRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/push")
public class PushController {

    private final PushService pushService;

    public PushController(PushService pushService) {
        this.pushService = pushService;
    }

    @PostMapping("/subscriptions")
    public ApiResponse<Map<String, Object>> register(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody RegisterSubscriptionRequest request
    ) {
        return ApiResponse.success(pushService.register(principal, request));
    }

    @PostMapping("/subscriptions/unsubscribe")
    public ApiResponse<Map<String, Object>> unregister(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UnregisterSubscriptionRequest request
    ) {
        return ApiResponse.success(pushService.unregister(principal, request));
    }

    @GetMapping("/subscriptions")
    public ApiResponse<List<Map<String, Object>>> getSubscriptions(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(pushService.getSubscriptions(principal));
    }

    @GetMapping("/preferences")
    public ApiResponse<Map<String, Object>> getPreference(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(pushService.getPreference(principal));
    }

    @PostMapping("/preferences")
    public ApiResponse<Map<String, Object>> updatePreference(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestBody UpdatePreferenceRequest request
    ) {
        return ApiResponse.success(pushService.updatePreference(principal, request));
    }
}
