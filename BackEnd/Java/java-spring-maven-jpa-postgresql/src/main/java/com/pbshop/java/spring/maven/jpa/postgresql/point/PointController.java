package com.pbshop.java.spring.maven.jpa.postgresql.point;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.point.dto.PointDtos.GrantPointRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/points/balance")
    public ApiResponse<Map<String, Object>> getBalance(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(pointService.getBalance(principal));
    }

    @GetMapping("/points/transactions")
    public ApiResponse<Map<String, Object>> getTransactions(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(pointService.getTransactions(principal, type, page, limit));
    }

    @PostMapping("/admin/points/grant")
    public ApiResponse<Map<String, Object>> grant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody GrantPointRequest request
    ) {
        return ApiResponse.success(pointService.grant(principal, request));
    }
}
