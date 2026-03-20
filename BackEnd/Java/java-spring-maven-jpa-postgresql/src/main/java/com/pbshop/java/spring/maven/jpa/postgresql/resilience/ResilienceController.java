package com.pbshop.java.spring.maven.jpa.postgresql.resilience;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/resilience/circuit-breakers")
public class ResilienceController {

    private final ResilienceService resilienceService;

    public ResilienceController(ResilienceService resilienceService) {
        this.resilienceService = resilienceService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(resilienceService.list(principal));
    }

    @GetMapping("/policies")
    public ApiResponse<Map<String, Object>> policies(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(resilienceService.policies(principal));
    }

    @GetMapping("/{name}")
    public ApiResponse<Map<String, Object>> show(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String name
    ) {
        return ApiResponse.success(resilienceService.show(principal, name));
    }

    @PostMapping("/{name}/reset")
    public ApiResponse<Map<String, Object>> reset(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String name
    ) {
        return ApiResponse.success(resilienceService.reset(principal, name));
    }
}
