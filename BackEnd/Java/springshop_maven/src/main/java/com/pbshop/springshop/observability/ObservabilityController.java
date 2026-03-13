package com.pbshop.springshop.observability;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/admin/observability")
public class ObservabilityController {

    private final ObservabilityService observabilityService;

    public ObservabilityController(ObservabilityService observabilityService) {
        this.observabilityService = observabilityService;
    }

    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> metrics(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(observabilityService.metrics(principal));
    }

    @GetMapping("/traces")
    public ApiResponse<Map<String, Object>> traces(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String pathContains
    ) {
        return ApiResponse.success(observabilityService.traces(principal, limit, pathContains));
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(observabilityService.dashboard(principal));
    }
}
